package com.example.demo;

import com.example.demo.entity.Video;
import org.springframework.stereotype.Service;

@Service
public class BConnectService implements DeviceConnectService<Video> {


    @Override
    public Video getInstance(Integer num, Class clazz) {



        return new Video(1,1000,"视频1");
    }
}
