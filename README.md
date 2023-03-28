# Spring boot 快速实现 分布式缓存 分布式锁
## 1.0.9版本 

作用：
1. 提供静态工具类CnaRedisUtil ，提供分布式数据结构及分布式锁静态实现，使其API更接近redis,从而让使用者能够将精力更集中地放在处理业务逻辑上。而不必学习过多的新概念。

2. 提供分布式锁注解 @CnaRedisLock , 配置在方法上可自动获取方法入参拼接成分布式锁

3. 底层采用 redisson ，方法上对特有概念做出注释及使用场景介绍。redisson是一个在Redis的基础上实现的Java驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的Java常用对象，还提供了许多分布式服务。其中包括(`BitSet`, `Set`, `Multimap`, `SortedSet`, `Map`, `List`, `Queue`, `BlockingQueue`, `Deque`, `BlockingDeque`, `Semaphore`, `Lock`, `AtomicLong`, `CountDownLatch`, `Publish / Subscribe`, `Bloom filter`, `Remote service`, `Spring cache`, `Executor service`, `Live Object service`, `Scheduler service`) Redisson提供了使用Redis的最简单和最便捷的方法。

   Redisson底层采用的是[Netty](http://netty.io/) 框架。支持[Redis](http://redis.cn) 2.8以上版本，支持Java1.6+以上版本。

   具体介绍请移步官网[Redisson](https://github.com/redisson/redisson/wiki/Redisson%E9%A1%B9%E7%9B%AE%E4%BB%8B%E7%BB%8D)

4. CnaRedisUtil 、@CnaRedisLock 及 redisson 客户端实例是否加载受 cnaworld.redis.enable 开关影响，默认开启，false关闭加载。


5. CnaRedisUtil 提供了`Redisson`、`RedissonReactive和`RedissonRx`实例本身和Redisson提供的所有分布式对象都是线程安全的。RedissonReactive和RedissonRx为响应式异步调用。

6. 客户端配置

   pom.xml 引入依赖

   ```xml
   <dependency>
       <groupId>cn.cnaworld.framework</groupId>
       <artifactId>redis</artifactId>
       <version>{latest}</version>
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

7. 项目启动时进行注册

   ```lua
   2023-03-04 15:24:34.561  INFO 27988 --- [           main] org.redisson.Version                     : Redisson 3.18.1
   2023-03-04 15:24:35.350  INFO 27988 --- [isson-netty-2-7] o.r.c.pool.MasterPubSubConnectionPool    : 1 connections initialized for 127.0.0.1/127.0.0.1:6379
   2023-03-04 15:24:35.395  INFO 27988 --- [isson-netty-2-2] o.r.c.pool.MasterConnectionPool          : 32 connections initialized for 127.0.0.1/127.0.0.1:6379
   2023-03-04 15:24:35.496  INFO 27988 --- [           main] c.c.f.i.common.utils.redis.CnaRedisUtil  : CnaRedisUtil  initialized ！
   ```

8. 调用方式

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

9、@CnaRedisLock  使用方式

```java
    @ApiOperation("查询学生列表")
    @GetMapping("/list")
    @CnaRedisLock(key = "静态值",prefix = "前缀",lockType = LockType.Lock,paramsAsKey = {"studentId","studentNameAndAge"},sync = true,waitTime = 10,timeUnit = TimeUnit.SECONDS,exceptionCallBack = ExceptionCallBack.class)
    public ResponseResult<List<Student>> list(@RequestParam(required = false) String studentId, @RequestParam(required = false) String studentName) {}

//注解说明：影响分布式锁key拼接的字段有
//1、String prefix ：前缀
//2、String key ：静态值
//3、String[] paramsAsKey ：方法参数作为key的数组
//规则：
//1、若存在prefix，自动拼接到最前方。
//2、若存在key则忽略paramsAsKey，单以prefix+key 作为分布式key。
//3、若key未配置，则根据paramsAsKey中配置的参数名称与方法参数名称做匹配，例如studentId匹配到@RequestParam String studentId，但是studentNameAndAge 未匹配上@RequestParam String studentName。则只使用prefix+studentId的实际入参作为分布式Key。注意参数为空则拼接null。支持引用对象，但是引用对象需要重写object的toString方法，提供出可支持幂等操作的值。

//锁类型：
//1、Lock：单节点的分布式重入锁 Reentrant
//2、FairLock ：单节点的分布式公平可重入锁 Reentrant
//3、集群模式下的红锁和联锁有时间会参考redisson pro源码提供

//阻塞
//1、sync 默认为true, 阻塞等待获取锁 ， 默认仅阻塞等待10S ， 若10S还未获取到锁则进入加锁失败处理
//2、可使用waitTime 和 timeUnit 控制最大阻塞等待时间。waitTime 可配置为-1 启动无上限阻塞等待直到获取到锁为止
//3、sync 配置为false时，会尝试获取一次锁，若获取失败，则直接进入加锁失败处理

//加锁失败处理
//exceptionCallBack 默认实现为ExceptionCallBack.class。具体实现是打印失败日志。
//log.error("方法全限定名：{} , redisKey ：{} , 动作 : {} ,分布式锁定失败" , declaringName, redisKey,action,e);
//可继承ExceptionCallBack此类并提供客户端实现。

//注意：
//若出现了网络原因等未知因素导致加锁失败，解锁失败，由业务自行使用失败处理回调判断是抛出异常终止业务还是，继续执行业务。
//若注释的是Controller方法，想要处理http返回信息可在失败处理回调中，抛出自定义异常，交由全局异常捕获器处理。也可通过上下文对象获取request对象处理返回信息封装。
```

10、补充
开关关闭后若出现异常可同步关闭检测

   ```yaml
management:
  health:
    redis:
      enabled: false
   ```