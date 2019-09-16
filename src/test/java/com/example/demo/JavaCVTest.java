package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVIOContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.AVRational;
import org.junit.Test;

import static org.bytedeco.ffmpeg.global.avcodec.av_packet_unref;
import static org.bytedeco.ffmpeg.global.avcodec.avcodec_find_decoder;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avformat.av_read_frame;
import static org.bytedeco.ffmpeg.global.avutil.*;

@Slf4j
public class JavaCVTest {

    private int resultCode;

    private boolean isPublish = true;

    /**
     * <h3></h3>
     * <p> AVRational: 是一个有理数，通过一个分子一个分母来储存一个分数</p>
     */
    public void test() {

        // 初始化网络库
        resultCode = avformat_network_init();
        // 初始化输入输出上下文
        AVFormatContext inContext = avformat_alloc_context();
        AVFormatContext outContext = avformat_alloc_context();

        // 获取输入流信息
        resultCode = avformat_open_input(
                inContext,
                "rtsp://admin:ZC000000@192.168.1.64:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_1",
                null,
                null
        );
//        AVDictionary avDictionary = new AVDictionary();
//        avDictionary.
        resultCode = avformat_find_stream_info(inContext, (AVDictionary) null);

        int videoIndex = 0;
        int audioIndex = 0;
        for (int i = 0; i < inContext.nb_streams(); i++) {
            if (inContext.streams(i).codecpar().codec_type() == AVMEDIA_TYPE_VIDEO) {
                videoIndex = i;
            }else if(inContext.streams(i).codecpar().codec_type() == AVMEDIA_TYPE_AUDIO){
                audioIndex = i;
            }
        }

        // 为输出格式分配AVFormatContext       释放时avformat_free_context()可用于释放上下文以及框架在其中分配的所有内容。
        avformat_alloc_output_context2(outContext, null, "flv", "rtmp://192.168.1.188:1935/live/ch1");

        // 输入 => 输出
        {
            // 获取输入AV流
            AVStream inputVStream = inContext.streams(videoIndex);
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
            AVStream inputAStream = inContext.streams(audioIndex);
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
        }

//av_dump_format(inContext,0,"rtsp://admin:ZC000000@192.168.1.64:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_1",);
        AVIOContext ioContext = new AVIOContext();
        // 打开IO通道
        resultCode = avio_open(ioContext, "rtmp://192.168.1.188:1935/live/ch1", AVIO_FLAG_WRITE);
//        resultCode = avio_open(ioContext, "rtmp://192.168.1.4:1935/live/ch1", AVIO_FLAG_WRITE);
        // 为输出绑定IO上下文
        // Do NOT set this field if AVFMT_NOFILE flag is set in iformat/oformat.flags.
        outContext.pb(ioContext);


        // 数据包写入头
        resultCode = avformat_write_header(outContext, (AVDictionary) null);


        // 当前帧计数器
        long frameIndex = 0;

        // 帧数据包
        AVPacket currentPkt = new AVPacket();
        AVPacket previousPkt = new AVPacket();

        //循环将输入流的帧数据写入到输出流0
        while (isPublish) {
            // 读取输入流一帧
            resultCode = av_read_frame(inContext, currentPkt);

            // TODO ???? PTS 是啥
            if (currentPkt.pts() == AV_NOPTS_VALUE) {
                // 获取当前输入流的 【时间基准】
                AVRational inputTimeBase = inContext.streams(videoIndex).time_base();
                //Duration between 2 frames (us)
                // 计算延迟
                // 流的实际基本帧速率
                AVRational frameRate = inContext.streams(videoIndex).r_frame_rate();
                double frameRateDouble = av_q2d(frameRate);
                //TODO 时间基准/实际基本帧速率 就是延迟？？
                long calcDuration = (long) (AV_TIME_BASE / frameRateDouble);

                //  化简
//                currentPkt.pts((long) ((frameIndex * calcDuration) / (av_q2d(inputTimeBase) * AV_TIME_BASE)));
//                currentPkt.pts((long) ((frameIndex * (AV_TIME_BASE / frameRateDouble)) / (av_q2d(inputTimeBase) * AV_TIME_BASE)));
//                currentPkt.pts((long) ((frameIndex * AV_TIME_BASE * frameRateDouble) / (av_q2d(inputTimeBase) * AV_TIME_BASE)));
//                currentPkt.pts((long) ((frameIndex * frameRateDouble) / (av_q2d(inputTimeBase) )));
                currentPkt.pts((long) (
                        frameIndex * av_q2d(frameRate) / av_q2d(inputTimeBase)
                        // 化简
//                        frameIndex * (frameRate.num()/ frameRate.den()) / ( inputTimeBase.num()/ inputTimeBase.den())
//                        frameIndex * frameRate.num() / frameRate.den() / inputTimeBase.num() * inputTimeBase.den()
                ));

                // TODO ???? DTS 是啥
                currentPkt.dts(currentPkt.pts());
                // 设置延迟
                currentPkt.duration((long) (calcDuration / (av_q2d(inputTimeBase) * AV_TIME_BASE)));
//                currentPkt.duration((long) ( (AV_TIME_BASE / frameRateDouble) / (av_q2d(inputTimeBase) * AV_TIME_BASE)));
//                currentPkt.duration((long) ( (AV_TIME_BASE / av_q2d(frameRate)) / (av_q2d(inputTimeBase) * AV_TIME_BASE)));
//                currentPkt.duration((long) ( AV_TIME_BASE / av_q2d(frameRate) / av_q2d(inputTimeBase) / AV_TIME_BASE));
                // 延迟  =  帧速率的倒数 / 输入流时间基准
                currentPkt.duration((long) (1 / av_q2d(frameRate) / av_q2d(inputTimeBase)));
            }
            // TODO 为什么不是 inContext.streams(videoIndex)
            AVStream inStream = inContext.streams(currentPkt.stream_index());
            AVStream outStream = outContext.streams(currentPkt.stream_index());
//            AVStream inStream = inContext.streams(videoIndex);
//            AVStream outStream = outContext.streams(videoIndex);
            /* copy packet */
            //Convert PTS/DTS
            // a * bq / cq ： pts * 输入时间基准 /输出时间基准
            currentPkt.pts(av_rescale_q_rnd(currentPkt.pts(), inStream.time_base(), outStream.time_base(), (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX)));
            currentPkt.dts(av_rescale_q_rnd(currentPkt.dts(), inStream.time_base(), outStream.time_base(), (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX)));
            // a * bq / cq ： 当前延迟 * 输入时间基准 /输出时间基准
            currentPkt.duration(av_rescale_q(currentPkt.duration(), inStream.time_base(), outStream.time_base()));

            // 流中的字节位置，如果未知则为-1
            currentPkt.pos(-1);

            // ??? TODO
                if (currentPkt.stream_index() == videoIndex) {
//                log.info("currentPkt.stream_index== videoIndex {} ",videoIndex);
                log.info("【2】videoIndex=frameIndex={}",videoIndex);
                frameIndex++;
            }else if (currentPkt.stream_index() == audioIndex){
                log.info("【2】audioIndex=frameIndex={}",audioIndex);
            }
            else {
                log.info("【2】currentPkt.stream_index{}       |videoIndex{}      ",currentPkt.stream_index(),videoIndex);
//                continue;
            }




            // 真实写入操作
            // TODO 有多个outContext就可以多次写入？？
            resultCode = av_interleaved_write_frame(outContext, currentPkt);

            // 缓存当前帧
            previousPkt = currentPkt;
            log.info("【3】currentPkt[stream_index]:{}",currentPkt.stream_index());
            // 擦除帧数据包
            av_packet_unref(currentPkt);
        }

        // 资源回收操作
        av_write_trailer(outContext);
        avio_close(outContext.pb());
        avformat_free_context(outContext);


    }

    public static void main(String[] args) {
        new JavaCVTest().test();
    }
}

