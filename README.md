# Spring boot 快速实现 分布式缓存 分布式锁
## 1.0.0版本 

作用：
1. 提供静态工具类CnaRedisUtil ，提供分布式数据结构及分布式锁静态实现，使其API更接近redis,从而让使用者能够将精力更集中地放在处理业务逻辑上。而不必学习过多的新概念。

2. 底层采用 redisson ，方法上对特有概念做出注释及使用场景介绍。redisson是一个在Redis的基础上实现的Java驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的Java常用对象，还提供了许多分布式服务。其中包括(`BitSet`, `Set`, `Multimap`, `SortedSet`, `Map`, `List`, `Queue`, `BlockingQueue`, `Deque`, `BlockingDeque`, `Semaphore`, `Lock`, `AtomicLong`, `CountDownLatch`, `Publish / Subscribe`, `Bloom filter`, `Remote service`, `Spring cache`, `Executor service`, `Live Object service`, `Scheduler service`) Redisson提供了使用Redis的最简单和最便捷的方法。

   Redisson底层采用的是[Netty](http://netty.io/) 框架。支持[Redis](http://redis.cn) 2.8以上版本，支持Java1.6+以上版本。

   具体介绍请移步官网[Redisson](https://github.com/redisson/redisson/wiki/Redisson%E9%A1%B9%E7%9B%AE%E4%BB%8B%E7%BB%8D)

3. CnaRedisUtil 及 redisson 客户端实例是否加载受 cnaworld.redis.enable 开关影响，默认开启，false关闭加载。

4. CnaRedisUtil 提供了`Redisson`、`RedissonReactive和`RedissonRx`实例本身和Redisson提供的所有分布式对象都是线程安全的。RedissonReactive和RedissonRx为响应式异步调用。

5. 客户端配置

   pom.xml 引入依赖

   ```xml
   <dependency>
       <groupId>cn.cnaworld.framework</groupId>
       <artifactId>redis</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

   application.yml 配置

   ```yaml
   cnaworld:
     redis:
       enable: true #默认为true ，false 将关闭CnaRedisUtil实例创建
   
   spring:
     # redis 配置
     redis:
       redisson:
         file: classpath:redisson.yml
   ```

   redisson.yml 配置

   ```yaml
   singleServerConfig: #单机版配置
     idleConnectionTimeout: 10000
     connectTimeout: 10000
     timeout: 3000
     retryAttempts: 3
     retryInterval: 1500
     subscriptionsPerConnection: 5
     clientName: null
     address: "redis://127.0.0.1:6379"
     subscriptionConnectionMinimumIdleSize: 1
     subscriptionConnectionPoolSize: 50
     connectionMinimumIdleSize: 32
     connectionPoolSize: 64
     database: 0
     dnsMonitoringInterval: 5000
   threads: 0
   nettyThreads: 0
   codec: !<org.redisson.codec.JsonJacksonCodec> {}
   "transportMode": "NIO"
   ```

   或者application.yml 配置

   ```yaml
   spring:
     # redis 配置
     redis:
       redisson:
         config: |
          singleServerConfig:
            idleConnectionTimeout: 10000
            connectTimeout: 10000
            timeout: 3000
            retryAttempts: 3
            retryInterval: 1500
            subscriptionsPerConnection: 5
            clientName: null
            address: "redis://127.0.0.1:6379"
            subscriptionConnectionMinimumIdleSize: 1
            subscriptionConnectionPoolSize: 50
            connectionMinimumIdleSize: 32
            connectionPoolSize: 64
            database: 0
            dnsMonitoringInterval: 5000
          threads: 0
          nettyThreads: 0
          codec: !<org.redisson.codec.JsonJacksonCodec> {}
          "transportMode": "NIO"
   ```

   其他集群配置、哨兵配置等配置方式请移步官网

   https://github.com/redisson/redisson/wiki/2.-Configuration

6. 项目启动时进行注册

   ```lua
   2023-03-04 15:24:34.561  INFO 27988 --- [           main] org.redisson.Version                     : Redisson 3.18.1
   2023-03-04 15:24:35.350  INFO 27988 --- [isson-netty-2-7] o.r.c.pool.MasterPubSubConnectionPool    : 1 connections initialized for 127.0.0.1/127.0.0.1:6379
   2023-03-04 15:24:35.395  INFO 27988 --- [isson-netty-2-2] o.r.c.pool.MasterConnectionPool          : 32 connections initialized for 127.0.0.1/127.0.0.1:6379
   2023-03-04 15:24:35.496  INFO 27988 --- [           main] c.c.f.i.common.utils.redis.CnaRedisUtil  : CnaRedisUtil  initialized ！
   ```

7. 调用方式

   [官网使用方式](https://github.com/redisson/redisson/wiki/6.-distributed-objects)

   ```java
   package cn.cnaworld.cnaworldaoptest;
   
   import cn.cnaworld.framework.infrastructure.common.utils.redis.CnaRedisUtil;
   import org.junit.jupiter.api.Test;
   import org.redisson.api.RBloomFilter;
   import org.redisson.api.RLock;
   import org.springframework.boot.test.context.SpringBootTest;
   
   import java.io.IOException;
   import java.time.Duration;
   import java.util.ArrayList;
   import java.util.HashMap;
   import java.util.List;
   import java.util.Map;
   import java.util.concurrent.TimeUnit;
   
   @SpringBootTest
   class CnaworldAopTestApplicationTests {
   
       @Test
       void getSetAndType() {
           //int long string map list set object
           Map<String,String> map1 = new HashMap<>();
           map1.put("map1","map1");
           map1.put("map2","map2");
   
           Map<Object,Object> map2 = new HashMap<>();
           JavaTestBean javaTestBean = new JavaTestBean();
           map2.put(javaTestBean,javaTestBean);
           List<List<JavaTestBean>> list =  new ArrayList<>();
           List<JavaTestBean> javaTestBeanlist =  new ArrayList<>();
           javaTestBeanlist.add(javaTestBean);
           list.add(javaTestBeanlist);
           Map<String,List<List<JavaTestBean>>> map3 = new HashMap<>();
           map3.put("map3",list);
   
           //int
           CnaRedisUtil.set("k1",1);
           //long
           CnaRedisUtil.set("k2",1L);
           //string
           CnaRedisUtil.set("k3","test");
           //map
           CnaRedisUtil.set("k4",map1);
           //map
           CnaRedisUtil.set("k5",map2);
           //map
           CnaRedisUtil.set("k6",map3);
           //list
           CnaRedisUtil.set("k7",list);
           //list
           CnaRedisUtil.set("k8",javaTestBean);
           //超时
           CnaRedisUtil.set("k9",1,100L, TimeUnit.MILLISECONDS);
           //设置新值并且返回旧值
           int k1 =CnaRedisUtil.getAndSet("k1", 2);
           //设置新值并且返回旧值
           CnaRedisUtil.getAndSet("k1", 2, 10, TimeUnit.SECONDS);
           //CAS
           CnaRedisUtil.compareAndSet("k1",1L,2L);
           //当且仅当 key 不存在，将 key 的值设为 value , 并设置有效期
           CnaRedisUtil.setIfAbsent("k12","k12", Duration.ofSeconds(60));
           //当且仅当 key 不存在，将 key 的值设为 value
           CnaRedisUtil.setIfAbsent("k13","k13");
   
   
           System.out.println(CnaRedisUtil.get("k1").toString());
           System.out.println(CnaRedisUtil.get("k2").toString());
           System.out.println(CnaRedisUtil.get("k3").toString());
           System.out.println(CnaRedisUtil.get("k4").toString());
           System.out.println(CnaRedisUtil.get("k5").toString());
           System.out.println(CnaRedisUtil.get("k6").toString());
           System.out.println(CnaRedisUtil.get("k7").toString());
           System.out.println(CnaRedisUtil.get("k8").toString());
           System.out.println(CnaRedisUtil.get("k9").toString());
           System.out.println(k1);
   
       }
   
       @Test
       void getsSetsAndType() {
           //批量存取
           Map<String, JavaTestBean> kvMap = new HashMap<>();
           kvMap.put("k1",new JavaTestBean());
           kvMap.put("k2",new JavaTestBean());
           CnaRedisUtil.sets(kvMap);
           Map<String, String> gets = CnaRedisUtil.gets("k1", "k2");
           System.out.println(gets);
       }
   
       @Test
       void lock(){
           //加锁解锁
           RLock lock = CnaRedisUtil.getLock("lock");
           lock.lock();
           try{
               System.out.println("业务！");
           }catch (Exception e){
               System.out.println("异常!");
           }finally {
               lock.unlock();
           }
       }
   
       public static class JavaTestBean{
   
           public String name="张三";
   
       }
   
   }
   
   ```
