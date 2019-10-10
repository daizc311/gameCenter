//package com.example.demo.old.zookeeper;
//
//import com.example.demo.entity.Music;
//import com.google.common.collect.Lists;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.client.ServiceInstance;
//import org.springframework.cloud.client.discovery.DiscoveryClient;
//import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
//import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceInstance;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/zk")
//public class ZkController {
//
//    @Autowired
//    private DiscoveryClient discoveryClient;
//
//    @RequestMapping("/serviceUrl")
//    public List<ServiceInstance> serviceUrl() {
//        List<ServiceInstance> list = discoveryClient.getInstances("application");
//
//        ServiceInstance serviceInstance = list.get(0);
//        Map<String, String> metadata = serviceInstance.getMetadata();
//        ZookeeperServiceInstance zookeeperServiceInstance = (ZookeeperServiceInstance) serviceInstance;
//        org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> zookeeperServiceInstanceServiceInstance = zookeeperServiceInstance.getServiceInstance();
//
////        serviceInstance.
////        serviceInstance.
//        return list;
//    }
//}
