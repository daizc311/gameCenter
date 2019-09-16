package com.example.demo;

import com.example.demo.async.AsyncServiceImpl;
import com.example.demo.entity.BaseDemoEntity;
import com.example.demo.entity.Music;
import com.example.demo.entity.Video;
import com.example.demo.ffmpeg.FFmpegPublish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>TestController</p>
 *
 * @author Daizc
 * @date 2019/8/27
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    RedisTemplate redisTemplate;


    @Autowired
    private DeviceConnectService<Video> connectServiceVideo;

    @Autowired
    private DeviceConnectService<Music> connectServiceMusic;

    @Autowired
    private AsyncServiceImpl asyncService;


    /**
     * <h3>泛型注入实验</h3>
     *
     * @return java.lang.String java.lang.String
     */
    @RequestMapping("/test1")
    public String test1() {

        Video video = connectServiceVideo.getInstance(1, null);
        Music music = connectServiceMusic.getInstance(1, null);
        return video.playVideo() + music.playMusic();
    }


    /**
     * <h3>Redis操作</h3>
     */
    @RequestMapping("/test2")
    public void test2() {
        redisTemplate.opsForList().rightPush("musicList:musicList1", new Music(111, 111, "111"));
        redisTemplate.opsForList().rightPush("musicList:musicList1", new Music(222, 222, "222"));
        redisTemplate.opsForList().rightPush("musicList:musicList1", new Music(333, 333, "333"));
        redisTemplate.opsForList().rightPush("musicList:musicList2", new Music(111, 111, "111"));
        redisTemplate.opsForList().rightPush("musicList:musicList2", new Music(222, 222, "222"));
        redisTemplate.opsForList().rightPush("musicList:musicList2", new Music(333, 333, "333"));
    }


    /**
     * <h3>Async</h3>
     */
    @RequestMapping("/taskStart")
    public String taskStart(String str) throws Exception {

        for (int i = 0; i < 200; i++) {
            asyncService.runTask("task-" + i);
        }
        return "OK";
    }

    /**
     * <h3>Async</h3>
     */
    @RequestMapping("/taskStop")
    public String taskStop(String str) throws Exception {

        for (int i = 0; i < 200; i++) {
            try {
                asyncService.stopTask("task-" + i);
            } catch (Exception e) {
            }
        }


        return "OK";
    }


    /**
     * <h2> super 类型下界通配符 </h2>
     * <p> <? super BaseDemoEntity> => BaseDemoEntity的任意一种父类 </p>
     *
     * <ul>
     *     <li>
     *         List中只能存储一种元素，此处声明的<? super BaseDemoEntity>,表明这个List中存放的元素属于 BaseDemoEntity<b>其中一种</b>父类类型
     *     </li>
     *     <li>
     *         BaseDemoEntity的父类可能有复数个，比如 Object，BaseEntity。取出时不知道到底List里到底装的啥，但一定是继承于Object，所以统一强转为Object类型
     *     </li>
     *     <li>
     *       存入时只能放入 BaseDemoEntity 和它的子类，因为声明的<? super BaseDemoEntity>，BaseDemoEntity 可以被安全的强转为<? super BaseDemoEntity>此处如果传入BaseDemoEntity的父类，那么运行时jvm将传入的类型转为<? super BaseDemoEntity>时可能会因为类型不一致报错
     *     </li>
     * </ul>
     */
    @RequestMapping("/superBaseDemoEntity")
    public void superBaseDemoEntity() {

        List<? super BaseDemoEntity> appList = new ArrayList<>();
        appList.add(new Music());
        appList.add(new BaseDemoEntity());
        Object object1 = appList.get(0);

    }

    /**
     * <h2> extends 类型上界通配符 </h2>
     * <p> <? extends BaseDemoEntity> => BaseDemoEntity的任意一种子类 </p>
     *
     * <ul>
     *     <li>
     *         List中只能存储一种元素，此处声明的<? extends BaseDemoEntity>表明这个List中存放的元素可能是继承与 BaseDemoEntity 的<b>其中一种</b>元素
     *     </li>
     *     <li>
     *         取出时可以被安全的强转为 BaseDemoEntity
     *     </li>
     *     <li>
     *       存入时由于不知道你存入的类型是否跟<? extends BaseDemoEntity>类型是一个类型，因此不能存入
     *     </li>
     * </ul>
     */
    @RequestMapping("/extendsBaseDemoEntity")
    public void extendsBaseDemoEntity() {

        List<? extends BaseDemoEntity> appList2 = new ArrayList<>();
        BaseDemoEntity baseDemoEntity = appList2.get(0);


    }


    @Autowired
    private FFmpegPublish ffmpegPublish;


    private FFmpegPublish cachePublish;




    @RequestMapping("startStream")
    public void startStream() {
        cachePublish = ffmpegPublish;

//        FFmpegPublish ffmpegPublish = new FFmpegPublish("rtsp://admin:ZC000000@192.168.1.64:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_1");
        cachePublish.init("rtsp://admin:ZC000000@192.168.1.64:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_1", null);
        cachePublish.createOutPipeline("rtmp://192.168.1.188:1935/live/ch1", null);
        cachePublish.publishAsync();
        return;
    }

    @RequestMapping("stopStream")
    public void stopStream() {
        if (null != cachePublish) {
            cachePublish.stopPublish();
        }
    }

    @RequestMapping("addStream")
    public void addStream(String name) {
        cachePublish.createOutPipeline("rtmp://192.168.1.188:1935/live/" + name, null);
    }

    @RequestMapping("removeStream")
    public void removeStream(String name) {
        cachePublish.closeOutPipeline("rtmp://192.168.1.188:1935/live/" + name);
    }


//
//    @RequestMapping("/test1")
//    public Object test1(){
//
//        DiscoveryManager discoveryManager = new DiscoveryManager();
//        discoveryManager.discover(DiscoveryMode.ONVIF, new DiscoveryListener() {
//            @Override
//            public void onDiscoveryStarted() {
//                System.out.println("开始搜索");
//            }
//
//            @Override
//            public void onDevicesFound(List<Device> devices) {
//
//                devices.forEach(device -> {
//                    System.out.println(device);
//                });
//            }
//        });
//
//
//        return true;
//    }


}
