package com.example.demo.controller;

import com.example.demo.ffmpeg.FFmpegPublisher;
import com.example.demo.ffmpeg.OutPutContextParamter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {

    private static final String URL_PRE = "";


    private FFmpegPublisher ffmpegPublisher;

    @RequestMapping("/push/")
    public Object push1(String name) throws Exception {

        if (ffmpegPublisher == null){
            ffmpegPublisher = new FFmpegPublisher();
            ffmpegPublisher.init("rtsp://admin:ZC000000@192.168.1.64:554/h264/ch1/main/av_stream", null);
        }

        ffmpegPublisher.createOutPipeline(URL_PRE + name, new OutPutContextParamter());

        if (!ffmpegPublisher.isPublish()){
            ffmpegPublisher.pushAsync();
        }

        return name;
    }

    @RequestMapping("/start")
    public Object start() {

        ffmpegPublisher.pushAsync();

        return "ok";
    }

    @RequestMapping("/stop/{name}")
    public Object stop(@PathVariable("name") String name) {

        ffmpegPublisher.closeOutPipeline(name);

        return "ok";
    }

}
