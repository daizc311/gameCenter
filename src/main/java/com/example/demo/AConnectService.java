package com.example.demo;

import com.example.demo.entity.Music;
import org.springframework.stereotype.Service;

@Service
public class AConnectService implements DeviceConnectService<Music> {


    @Override
    public Music getInstance(Integer num, Class clazz) {



        return new Music(1,1000,"音乐1");
    }
}
