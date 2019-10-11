package com.example.demo.ffmpeg;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVIOContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.springframework.scheduling.annotation.Async;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.*;

/**
 * <h2>FFmpegPublish</h2>
 * <p>ffmpeg推流工具</p>
 *
 * @author lc && Daizc
 * @date 2019/9/12
 */
@Slf4j
public class FFmpegPublisher {

    private int resultCode;

    private String inputUri;
    private AVFormatContext inAVideoContext;

    private volatile Map<String, AVFormatContext> outAVContextMap;
    // private volatile Map<String, AVPacket> outAVPacketMap;

    private volatile boolean isPublish = false;

    // 输入解析指针组合
    private int[] streamIndexes;

    public void init(String inputUri, InPutContextParamter param) throws Exception {

        outAVContextMap = new ConcurrentHashMap<>();

        resultCode = avformat_network_init();
        checkError(resultCode);

        inAVideoContext = avformat_alloc_context();
        if (Objects.isNull(inAVideoContext)) {
            throw new Exception("分配AVFormatContext[Input]失败");
        }

        resultCode = avformat_open_input(inAVideoContext, inputUri, null, null);
        checkError(resultCode);

        resultCode = avformat_find_stream_info(inAVideoContext, (AVDictionary) null);
        checkError(resultCode);

        // 设置需要读取的流格式
        LinkedList<Integer> readStreamIndexList = Lists.newLinkedList();
        for (int i = 0; i < inAVideoContext.nb_streams(); i++) {
            int codecType = inAVideoContext.streams(i).codecpar().codec_type();
            readStreamIndexList.add(codecType);
        }
        streamIndexes = readStreamIndexList.stream()
                .mapToInt(Integer::intValue)
                .filter(codecType -> {
                    if (codecType == AVMEDIA_TYPE_UNKNOWN) {
                        return false;
                    } else if (codecType == AVMEDIA_TYPE_VIDEO) {
                        return true;
                    } else if (codecType == AVMEDIA_TYPE_AUDIO) {
                        return true;
                    } else if (codecType == AVMEDIA_TYPE_DATA) {
                        return false;
                    } else if (codecType == AVMEDIA_TYPE_SUBTITLE) {
                        return false;
                    } else if (codecType == AVMEDIA_TYPE_ATTACHMENT) {
                        return false;
                    } else if (codecType == AVMEDIA_TYPE_NB) {
                        return false;
                    }
                    return false;
                }).toArray();

    }

    private boolean isInitialize() {
        return !(Objects.nonNull(inputUri) && Objects.nonNull(inAVideoContext));
    }

    //  测试地址 rtmp://192.168.1.188:1935/live/ch1";
    public FFmpegPublisher createOutPipeline(String outputUri, OutPutContextParamter param) throws Exception {

        if (!isInitialize()) {
            throw new Exception("FFmpegPublish[{}]尚未执行初始化方法");
        }

        //创建输出上下文
        AVFormatContext outContext = avformat_alloc_context();
        if (outContext.isNull()) {
            throw new Exception("分配AVFormatContext[Output]失败");
        }

        resultCode = avformat_alloc_output_context2(outContext, null, "flv", outputUri);
        checkError(resultCode);

        // 为每个流设置编解码器
        for (int streamIndex : streamIndexes) {
            // 获取输入AV流
            AVStream inStream = inAVideoContext.streams(streamIndex);
            // 获取输入AV流的解码器
            AVCodec avCodec = avcodec_find_decoder(inStream.codecpar().codec_id());
            // 通过outContext申请输出流
            AVStream outStream = avformat_new_stream(outContext, avCodec);
            if (outStream.isNull()) {
                throw new Exception("申请输出流[{}]失败");
            }
            // 设置输出流编解码器参数
            resultCode = avcodec_parameters_copy(outStream.codecpar(), inStream.codecpar());
            checkError(resultCode);
            // 设置【时间基准】（必须在 avformat_write_header 之前）
            outStream.time_base().den(inStream.time_base().den());
            outStream.time_base().num(inStream.time_base().num());
        }

        // 开启IO
        AVIOContext ioContext = new AVIOContext();
        resultCode = avio_open(ioContext, outputUri, AVIO_FLAG_WRITE);
        checkError(resultCode);
        outContext.pb(ioContext);
        resultCode = avformat_write_header(outContext, (AVDictionary) null);
        checkError(resultCode);

        outAVContextMap.put(outputUri, outContext);
        return this;
    }

    public boolean isPublish() {
        return isPublish;
    }

    @Async("ffmpegTheadPool")
    public void pushAsync() {

        log.info("[{}]开始推流", Thread.currentThread().getName());

        // 当前帧计数器
        long frameIndex = 0;

        // 准备推流flag
        isPublish = true;
        AtomicReference<AVFormatContext> tempOutContext = new AtomicReference<>();
        //循环将输入流的帧数据写入到输出流0
        AVPacket publicPacket = new AVPacket();
        int currentStreamFlag = 0;
        while (isPublish) {

            if (0 == outAVContextMap.size()) {
                isPublish = false;
            }
            if (frameIndex % (25 * 2 * 5) == 0) {
                outAVContextMap.keySet().forEach(log::error);
            }


            av_packet_unref(publicPacket);

            // 读取输入流一帧存入公共数据包
            int resultCode = av_read_frame(inAVideoContext, publicPacket);


            // 流中的字节位置，如果未知则为-1
            /* copy packet */
            //Convert PTS/DTS
            // a * bq / cq ： pts * 输入时间基准 /输出时间基准
            // a * bq / cq ： 当前延迟 * 输入时间基准 /输出时间基准
            // 真实写入操作
            outAVContextMap.values().forEach(outContext -> {
                AVPacket currentPkt = av_packet_clone(publicPacket);
                currentPkt.pos(-1);
                AVStream inStream = inAVideoContext.streams(currentPkt.stream_index());
                AVStream outStream = outContext.streams(currentPkt.stream_index());
                currentPkt.pts(av_rescale_q_rnd(currentPkt.pts(), inStream.time_base(), outStream.time_base(), (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX)));
                currentPkt.dts(av_rescale_q_rnd(currentPkt.dts(), inStream.time_base(), outStream.time_base(), (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX)));
                currentPkt.duration(av_rescale_q(currentPkt.duration(), inStream.time_base(), outStream.time_base()));
                int resultCode2 = av_interleaved_write_frame(outContext, currentPkt);
                if (resultCode2 < 0) {
//                    throw new Exception(resultCode2 + "");
                    log.error(resultCode2 + "");
                }
            });

            //擦除公共数据包
            av_packet_unref(publicPacket);


            if (currentStreamFlag > 0 && (currentStreamFlag >= inAVideoContext.nb_streams())) {
                if (frameIndex == Long.MAX_VALUE) {
                    frameIndex -= Long.MAX_VALUE;
                }
                frameIndex++;
                currentStreamFlag = 0;
            } else {
                currentStreamFlag++;
            }

        }

        log.error("结束推流！！！！" + Thread.currentThread().getName());
        //avformat_free_context(tempOutContext.get());

    }


    public void pausePublish() {

        this.isPublish = false;
    }

    public void stopPublish() {

        outAVContextMap.keySet().forEach(this::closeOutPipeline);
    }

    public void closeOutPipeline(String outputUri) {

        log.error("关闭{}", outputUri);
        AVFormatContext outputContext = outAVContextMap.get(outputUri);
//        outAVPacketMap.remove(outputUri);
        outAVContextMap.remove(outputUri);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        av_write_trailer(outputContext);
        avio_close(outputContext.pb());
        avformat_free_context(outputContext);
    }

    @Override
    protected void finalize() throws Throwable {

        log.error("[资源被释放]" + toString());

        outAVContextMap.keySet().forEach(this::closeOutPipeline);

        avformat_close_input(this.inAVideoContext);

        avformat_free_context(this.inAVideoContext);

        super.finalize();
    }

    public String getInputUri() {
        return inputUri;
    }

    public List<String> getOutPutList() {
        return Lists.newArrayList(outAVContextMap.keySet());
    }


    /**
     * 根据错误编码获取Ffmpeg中错误字符串
     *
     * @param result 错误编码
     * @return 错误字符串
     */
    private String getErrorString(int result) {
        int bufferLength = 0;
        byte[] errorString = new byte[1024];
        av_strerror(result, errorString, 1024);
        for (int i = 0; i < errorString.length; ++i) {
            if (errorString[i] == 0) {
                bufferLength = i;
                break;
            }
        }
        try {
            return new String(errorString, 0, bufferLength, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void checkError(int resultCode) throws Exception {
        if (-1 == resultCode) {
            throw new Exception(getErrorString(resultCode));
        } else if (resultCode < 0) {
            log.error(getErrorString(resultCode));
        }
    }


}
