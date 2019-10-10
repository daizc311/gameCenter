package com.example.demo.ffmpeg;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVIOContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.springframework.scheduling.annotation.Async;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private String inputUri;
    private AVFormatContext inAVideoContext;

    private volatile Map<String, AVFormatContext> outAVContextMap;
    // private volatile Map<String, AVPacket> outAVPacketMap;

    private Thread currentThread;

    private volatile boolean isPublish = false;


    // 输入解析指针
    private int videoContextStreamIndex;
    private int audioContextStreamIndex;

    public void init(String inputUri, InPutContextParamter param) {

        Map<String, AVFormatContext> formatContextHashMap = Maps.newHashMap();

        formatContextHashMap = Collections.synchronizedMap(formatContextHashMap);

        outAVContextMap = Collections.synchronizedMap(formatContextHashMap);
//        outAVContextMap = new ConcurrentHashMap<>();
        //outAVPacketMap = new ConcurrentHashMap<>();
        int resultCode;

        resultCode = avformat_network_init();

        inAVideoContext = avformat_alloc_context();

//        outVideoContext = avformat_alloc_context();

        resultCode = avformat_open_input(
                inAVideoContext,
                inputUri,
                null,
                null
        );

        resultCode = avformat_find_stream_info(inAVideoContext, (AVDictionary) null);

        for (int i = 0; i < inAVideoContext.nb_streams(); i++) {
            int codecType = inAVideoContext.streams(i).codecpar().codec_type();
            if (codecType == AVMEDIA_TYPE_VIDEO) {
                videoContextStreamIndex = i;
            } else if (codecType == AVMEDIA_TYPE_AUDIO) {
                audioContextStreamIndex = i;
            }
        }
    }

    public FFmpegPublisher(String inputUri, InPutContextParamter param) {
        this.inputUri = inputUri;
        init(inputUri, param);
    }

    public FFmpegPublisher(String inputUri) {
        this.inputUri = inputUri;
        init(inputUri, null);
    }

    public FFmpegPublisher() {
    }

    private boolean isInitialize() {
        return Objects.nonNull(inputUri) && Objects.nonNull(inAVideoContext);
    }

    public FFmpegPublisher createOutPipeline(String outputUri, OutPutContextParamter param) {

        if (isInitialize()) {
            log.error("FFmpegPublish[{}]尚未执行初始化方法", toString());
            return null;
        }

//        outputUri = "rtmp://192.168.1.188:1935/live/ch1";

        //创建输出上下文
        AVFormatContext outContext = avformat_alloc_context();
        avformat_alloc_output_context2(outContext, null, "flv", outputUri);


        // 输入 => 输出

        // 获取输入AV流
        AVStream inputVStream = inAVideoContext.streams(videoContextStreamIndex);
        // 获取输入AV流的解码器
        AVCodec avCodec = avcodec_find_decoder(inputVStream.codecpar().codec_id());

        // 创建输出流
        AVStream outputAVStream = avformat_new_stream(outContext, avCodec);
        if (outputAVStream.isNull()) {
            System.out.println("Failed allocating output stream");
        }
        // 设置输出流编解码器参数
        outputAVStream.codecpar(inputVStream.codecpar());
        // 设置【时间基准】（必须在 avformat_write_header 之前）
        outputAVStream.time_base().den(inputVStream.time_base().den());
        outputAVStream.time_base().num(inputVStream.time_base().num());


        // 获取输入A流
        AVStream inputAStream = inAVideoContext.streams(audioContextStreamIndex);
        // 获取输入A流的解码器
        AVCodec aCodec = avcodec_find_decoder(inputAStream.codecpar().codec_id());

        // 创建输出流
        AVStream outputAStream = avformat_new_stream(outContext, avCodec);
        if (outputAVStream.isNull()) {
            System.out.println("Failed allocating output stream");
        }
        // 设置输出流编解码器参数
        outputAStream.codecpar(inputAStream.codecpar());
        // 设置【时间基准】（必须在 avformat_write_header 之前）
        outputAStream.time_base().den(inputAStream.time_base().den());
        outputAStream.time_base().num(inputAStream.time_base().num());

        AVIOContext ioContext = new AVIOContext();
        int resultCode = avio_open(ioContext, outputUri, AVIO_FLAG_WRITE);
        outContext.pb(ioContext);
        resultCode = avformat_write_header(outContext, (AVDictionary) null);


        outAVContextMap.put(outputUri, outContext);
        return this;
    }

    public boolean isPublish() {
        return isPublish;
    }

    @Async
    public void pushAsync() {

        // 确保上次推送的线程安全退出

//        if (currentThread.getState() == Thread.State.RUNNABLE) {
//            currentThread.interrupt();
//        }
//        currentThread = Thread.currentThread();

        log.info("[{}]开始推流", Thread.currentThread().getName());

        // 当前帧计数器
        long frameIndex = 0;

        // 准备推流flag
        isPublish = true;
        //AtomicReference<AVFormatContext> tempOutContext = new AtomicReference<>();
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


            outAVContextMap.forEach((s, outContext) -> {
                //tempOutContext.set(outContext);
                AVPacket currentPkt = av_packet_clone(publicPacket);

                // 流中的字节位置，如果未知则为-1
                currentPkt.pos(-1);

                AVStream inStream = inAVideoContext.streams(currentPkt.stream_index());


                AVStream outStream = outContext.streams(currentPkt.stream_index());
                /* copy packet */
                //Convert PTS/DTS
                // a * bq / cq ： pts * 输入时间基准 /输出时间基准
                currentPkt.pts(av_rescale_q_rnd(currentPkt.pts(), inStream.time_base(), outStream.time_base(), (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX)));
                currentPkt.dts(av_rescale_q_rnd(currentPkt.dts(), inStream.time_base(), outStream.time_base(), (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX)));
                // a * bq / cq ： 当前延迟 * 输入时间基准 /输出时间基准
                currentPkt.duration(av_rescale_q(currentPkt.duration(), inStream.time_base(), outStream.time_base()));

                // 真实写入操作
                int resultCode2 = av_interleaved_write_frame(outContext, currentPkt);
                if (resultCode2 < 0) {
//                    throw new PlatFormBusinessException(resultCode2 + "");
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

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.error("结束推流！！！！" + currentThread.getName());
        //avformat_free_context(tempOutContext.get());

    }


    public void pausePublish() {

        this.isPublish = false;
    }

    public void stopPublish() {

        outAVContextMap.keySet().forEach(this::closeOutPipeline);
    }

    public void closeOutPipeline(String outputUri) {

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

    public Thread getCurrentThread() {
        return currentThread;
    }
}
