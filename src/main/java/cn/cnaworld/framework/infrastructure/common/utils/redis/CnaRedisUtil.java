package cn.cnaworld.framework.infrastructure.common.utils.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * redis客户端实现
 * @author Lucifer
 * @date 2023/2/10
 * @since 1.0.0
 */
@Slf4j
@ConditionalOnExpression("#{environment['cnaworld.redis.enable'] ==null || !environment['cnaworld.redis.enable'].contains('false')}")
public class CnaRedisUtil{

	private CnaRedisUtil() {
	}

	@Autowired
	private RedissonClient redissonClient;
	@Autowired
	private RedissonReactiveClient redissonReactiveClient;
	@Autowired
	private RedissonRxClient redissonRxClient;

	private static RedissonClient redisson;
	private static RedissonReactiveClient redissonReactive;
	private static RedissonRxClient redissonRx;

	public static RedissonClient redisson() {
		Assert.notNull(redisson,"redisson 加载失败!");
		return redisson;
	}

	public static RedissonReactiveClient redissonReactive() {
		Assert.notNull(redisson,"redisson 加载失败!");
		return redissonReactive;
	}

	public static RedissonRxClient redissonRx() {
		Assert.notNull(redisson,"redisson 加载失败!");
		return redissonRx;
	}

    @PostConstruct
    private void init() {
		if (redissonClient == null || redissonReactiveClient == null || redissonRxClient == null){
			log.warn("CnaRedisUtil 加载失败！");
		}else {
			redisson=redissonClient;
			redissonReactive=redissonReactiveClient;
			redissonRx=redissonRxClient;
			log.info("CnaRedisUtil  initialized ！");
		}
    }

	/**
	 * 设置值
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @param value V
	 */
	public static <V> void set(String key, V value) {
		RBucket<V> bucket = redisson().getBucket(key);
		bucket.set(value);
	}

	/**
	 * 设置值及过期时间
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @param value V
	 * @param time long 过期时间
	 * @param timeUnit TimeUnit 时间单位
	 */
	public static <V> void set(String key, V value, long time, TimeUnit timeUnit) {
		RBucket<V> bucket = redisson().getBucket(key);
		bucket.set(value,time,timeUnit);
	}

	/**
	 * 设置新值并且返回旧值
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key   String
	 * @param value V
	 */
	public static <V> V getAndSet(String key, V value) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.getAndSet(value);
	}

	/**
	 * 设置新值及过期时间并且返回旧值

	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key   String
	 * @param value V
	 * @param time long 过期时间
	 * @param timeUnit TimeUnit 时间单位
	 */
	public static <V> V getAndSet(String key, V value, long time, TimeUnit timeUnit) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.getAndSet(value,time,timeUnit);
	}

	/**
	 * 与旧值比较之后设置新值
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @param oldValue V
	 * @param value V
	 */
	public static <V> boolean compareAndSet(String key, V oldValue, V value) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.compareAndSet(oldValue,value);
	}

	/**
	 * 当且仅当 key 不存在，将 key 的值设为 value
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @param value V
	 */
	public static <V> boolean setIfAbsent(String key , V value) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.setIfAbsent(value);
	}

	/**
	 * 当且仅当 key 不存在，将 key 的值设为 value , 并设置有效期
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @param value V
	 * @param duration 有效期工具
	 */
	public static <V> boolean setIfAbsent(String key , V value, Duration duration) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.setIfAbsent(value,duration);
	}

	/**
	 * 当且仅当 key 存在，将 key 的值设为 value
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @param value V
	 */
	public static <V> boolean setIfExists(String key , V value) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.setIfExists(value);
	}

	/**
	 * 当且仅当 key 存在，将 key 的值设为 value过期时间
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @param value V
	 * @param time long 过期时间
	 * @param timeUnit TimeUnit 时间单位
	 */
	public static <V> boolean setIfExists(String key ,V value, long time, TimeUnit timeUnit) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.setIfExists(value,time,timeUnit);
	}

	/**
	 * 获取值
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 */
	public static <V> V get(String key) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.get();
	}

	/**
	 * 获取值并设置到期时间
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @param duration 有效期工具
	 */
	public static <V> V getAndExpire(String key,Duration duration) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.getAndExpire(duration);
	}

	/**
	 * 获取值并设置到期时间
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @param instant 有效期工具
	 */
	public static <V> V getAndExpire(String key,Instant instant) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.getAndExpire(instant);
	}

	/**
	 * 获取值并清理到期时间
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 */
	public static <V> V getAndClearExpire(String key) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.getAndClearExpire();
	}

	/**
	 * 获取值并删除KEY
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 */
	public static <V> V getAndDelete(String key) {
		RBucket<V> bucket = redisson().getBucket(key);
		return bucket.getAndDelete();
	}

	/**
	 * 批量设置值
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param kvMap K-V容器
	 */
	public static <V> void sets(Map<String, V> kvMap) {
		RBuckets buckets = redisson().getBuckets();
		buckets.set(kvMap);
	}

	/**
	 * 同时保存所有的容器中的键值，如果任意一个已经存在则放弃保存其他所有数据
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param kvMap K-V容器
	 */
	public static <V> void trySets(Map<String, V> kvMap) {
		RBuckets buckets = redisson().getBuckets();
		buckets.trySet(kvMap);
	}

	/**
	 * 批量获取值
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param keys String
	 */
	public static <V> Map<String, V> gets(String... keys) {
		RBuckets buckets = redisson().getBuckets();
		return buckets.get(keys);
	}

	/**
	 * 获取可操作BitSet , 二进制位图 ， 可对对应的二进制位设置true 或者 false
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RBitSet
	 */
	public static RBitSet getBitSet(String key) {
		return redisson().getBitSet(key);
	}

	/**
	 * 获取可操作原子Long
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RAtomicLong
	 */
	public static RAtomicLong getAtomicLong(String key) {
		return redisson().getAtomicLong(key);
	}

	/**
	 * 获取可操作原子Double
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RAtomicDouble
	 */
	public static RAtomicDouble getAtomicDouble(String key) {
		return redisson().getAtomicDouble(key);
	}

	/**
	 * 获取可操作原子Long,较Atomic性能更好快 12000 倍，推荐使用
	 * 当不再使用整长型累加器对象的时候应该自行手动销毁
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RLongAdder
	 */
	public static RLongAdder getLongAdder(String key) {
		return redisson().getLongAdder(key);
	}

	/**
	 * 获取可操作原子Double,较Atomic性能更好快 12000 倍，推荐使用
	 * 当不再使用整长型累加器对象的时候应该自行手动销毁
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RDoubleAdder
	 */
	public static RDoubleAdder getDoubleAdder(String key) {
		return redisson().getDoubleAdder(key);
	}

	/**
	 * 获得订阅主题
	 * 在Redis节点故障转移（主从切换）或断线重连以后，所有的话题监听器将自动完成话题的重新订阅。
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @return RTopic
	 */
	public static RTopic getTopic(String key) {
		return redisson().getTopic(key);
	}

	/**
	 * 获得模糊订阅主题 例如 ： topic1.*
	 * 在Redis节点故障转移（主从切换）或断线重连以后，所有的模糊话题监听器将自动完成话题的重新订阅。
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @return RPatternTopic
	 */
	public static RPatternTopic getPatternTopic(String key) {
		return redisson().getPatternTopic(key);
	}

	/**
	 * 概率算法统计大量的数据。大量重复数据，计算去重后独立数据个数。
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @return RHyperLogLog<Integer>
	 */
	public static RHyperLogLog<Integer> getHyperLogLog(String key) {
		return redisson().getHyperLogLog(key);
	}

	/**
	 * 限流器
	 * 默认最大流速 = 每1秒钟产生10个令牌
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0.0
	 * @param key String
	 * @return RRateLimiter
	 */
	public static RRateLimiter getRateLimiter(String key) {
		return redisson().getRateLimiter(key);
	}

	/**
	 * 缓存映射
	 * 实现了Map的功能同时支持过期清理，依赖EvictionScheduler
	 * map.setMaxSize()后即采用LRU算法，对其中元素按使用时间排序处理的方式，主动移除超过规定容量限制的元素
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RMapCache<K, V>
	 */
	public static <K, V> RMapCache<K, V> getMapCache(String key) {
		return redisson().getMapCache(key);
	}

	/**
	 * 本地缓存映射
	 * 实现了Map的功能同时支持本地缓存,各个客户端之间通过发布订阅同步数据
	 * 当不再使用Map本地缓存对象的时候应该手动销毁，如果Redisson对象被关闭（shutdown）了，则不用手动销毁。
	 * 这样的设计的好处是它能将读取速度提高最多 45倍 。 所有同名的本地缓存共用一个订阅发布话题，所有更新和过期消息都将通过该话题共享。
	 *     LocalCachedMapOptions options = LocalCachedMapOptions.defaults()
	 *           // 用于淘汰清除本地缓存内的元素
	 *           // 共有以下几种选择:
	 *           // LFU - 统计元素的使用频率，淘汰用得最少（最不常用）的。
	 *           // LRU - 按元素使用时间排序比较，淘汰最早（最久远）的。
	 *           // SOFT - 元素用Java的WeakReference来保存，缓存元素通过GC过程清除。
	 *           // WEAK - 元素用Java的SoftReference来保存, 缓存元素通过GC过程清除。
	 *           // NONE - 永不淘汰清除缓存元素。
	 *          .evictionPolicy(EvictionPolicy.NONE)
	 *          // 如果缓存容量值为0表示不限制本地缓存容量大小
	 *          .cacheSize(1000)
	 *           // 以下选项适用于断线原因造成了未收到本地缓存更新消息的情况。
	 *           // 断线重连的策略有以下几种：
	 *           // CLEAR - 如果断线一段时间以后则在重新建立连接以后清空本地缓存
	 *           // LOAD - 在服务端保存一份10分钟的作废日志
	 *           //        如果10分钟内重新建立连接，则按照作废日志内的记录清空本地缓存的元素
	 *           //        如果断线时间超过了这个时间，则将清空本地缓存中所有的内容
	 *           // NONE - 默认值。断线重连时不做处理。
	 *          .reconnectionStrategy(ReconnectionStrategy.NONE)
	 *           // 以下选项适用于不同本地缓存之间相互保持同步的情况
	 *           // 缓存同步策略有以下几种：
	 *           // INVALIDATE - 默认值。当本地缓存映射的某条元素发生变动时，同时驱逐所有相同本地缓存映射内的该元素
	 *           // UPDATE - 当本地缓存映射的某条元素发生变动时，同时更新所有相同本地缓存映射内的该元素
	 *           // NONE - 不做任何同步处理
	 *          .syncStrategy(SyncStrategy.INVALIDATE)
	 *           // 每个Map本地缓存里元素的有效时间，默认毫秒为单位
	 *          .timeToLive(10000)
	 *           // 或者
	 *          .timeToLive(10, TimeUnit.SECONDS)
	 *           // 每个Map本地缓存里元素的最长闲置时间，默认毫秒为单位
	 *          .maxIdle(10000)
	 *           // 或者
	 *          .maxIdle(10, TimeUnit.SECONDS);
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @param localCachedMapOptions 本地缓存配置
	 * @return RLocalCachedMap<K, V>
	 */
	public static <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String key,LocalCachedMapOptions<K, V> localCachedMapOptions) {
		return redisson().getLocalCachedMap(key,localCachedMapOptions);
	}

	/**
	 * 获取可操作map
	 * 保持了元素的插入顺序
	 * 支持Lock CountDownLatch
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RMap<K, V>
	 */
	public static <K,V> RMap<K, V> getMap(String key) {
		return redisson().getMap(key);
	}


	/**
	 * 多值映射
	 * 基于Set的Multimap不允许一个字段值包含有重复的元素,不保证顺序
	 * 相当于多个set集合被一个Map容器包裹,set集合的泛型跟随Map
	 * 调用put方法相当于往名字是key的set集合中add一个value
	 * 支持超时过期
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RSetMultimap<K, V>
	 */
	public static <K,V> RSetMultimap<K, V> getSetMultimap(String key) {
		return redisson().getSetMultimap(key);
	}

	/**
	 * 多值映射
	 * 基于List的Multimap在保持插入顺序的同时允许一个字段下包含重复的元素。
	 * 相当于多个List集合被一个Map容器包裹,List集合的泛型跟随Map
	 * 调用put方法相当于往名字是key的List集合中add一个value
	 * 支持超时过期
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RListMultimap<K, V>
	 */
	public static <K,V> RListMultimap<K, V> getListMultimap(String key) {
		return redisson().getListMultimap(key);
	}

	/**
	 * Set
	 * 通过元素的相互状态比较保证了每个元素的唯一性
	 * 支持超时过期
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RSet<V>
	 */
	public static <V> RSet<V> getSet(String key) {
		return redisson().getSet(key);
	}

	/**
	 * 有序集 Set
	 * 在保证元素唯一性的前提下，通过比较器（Comparator）接口实现了对元素的排序
	 * set.trySetComparator(new MyComparator()); // 配置元素比较器
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RSortedSet<V>
	 */
	public static <V> RSortedSet<V> getSortedSet(String key) {
		return redisson().getSortedSet(key);
	}

	/**
	 * 计分排序集 Set
	 * 可以按插入时指定的元素评分排序的集合。它同时还保证了元素的唯一性。
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RScoredSortedSet<V>
	 */
	public static <V> RScoredSortedSet<V> getScoredSortedSet(String key) {
		return redisson().getScoredSortedSet(key);
	}

	/**
	 * 字典排序集 Set
	 * 将其中的所有字符串元素按照字典顺序排列。它公式还保证了字符串元素的唯一性
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RScoredSortedSet<V>
	 */
	public static RLexSortedSet getLexSortedSet(String key) {
		return redisson().getLexSortedSet(key);
	}

	/**
	 * 列表 List
	 * 确保了元素插入时的顺序
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RList<V>
	 */
	public static <V> RList<V> getList(String key) {
		return redisson().getList(key);
	}

	/**
	 * 无界队列 Queue
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RQueue<V>
	 */
	public static <V> RQueue<V> getQueue(String key) {
		return redisson().getQueue(key);
	}

	/**
	 * 无界双端队列 Deque
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RDeque<V>
	 */
	public static <V> RDeque<V> getDeque(String key) {
		return redisson().getDeque(key);
	}

	/**
	 * 无界阻塞队列 BlockingQueue
	 * 方法内部采用话题订阅发布实现，在Redis节点故障转移（主从切换）或断线重连以后，内置的相关话题监听器将自动完成话题的重新订阅
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RBlockingQueue<V>
	 */
	public static <V> RBlockingQueue<V> getBlockingQueue(String key) {
		return redisson().getBlockingQueue(key);
	}

	/**
	 * 有界阻塞队列Bounded Blocking Queue
	 * 方法内部采用话题订阅发布实现，在Redis节点故障转移（主从切换）或断线重连以后，内置的相关话题监听器将自动完成话题的重新订阅
	 * 容量已满，插入代码将会被阻塞，直到有空闲为止。
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RBoundedBlockingQueue<V>
	 */
	public static <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String key) {
		return redisson().getBoundedBlockingQueue(key);
	}

	/**
	 * 无界阻塞双端队列 Blocking Deque
	 * 方法内部采用话题订阅发布实现，在Redis节点故障转移（主从切换）或断线重连以后，内置的相关话题监听器将自动完成话题的重新订阅
	 * 容量已满，插入代码将会被阻塞，直到有空闲为止。
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RBlockingDeque<V>
	 */
	public static <V> RBlockingDeque<V> getBlockingDeque(String key) {
		return redisson().getBlockingDeque(key);
	}

	/**
	 * 分布式延迟队列 Delayed Queue
	 * 队列按要求延迟添加项目的功能。该功能可以用来实现消息传送延迟按几何增长或几何衰减的发送策略。
	 * 延迟以后将消息发送到指定队列
	 * 在该对象不再需要的情况下，应该主动销毁
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param rQueue RQueue
	 * @return RDelayedQueue<V>
	 */
	public static <V> RDelayedQueue<V> getDelayedQueue(RQueue<V> rQueue) {
		return redisson().getDelayedQueue(rQueue);
	}

	/**
	 * 优先队列 Priority Queue
	 * 可以通过比较器（Comparator）接口来对元素排序
	 * queue.trySetComparator(new MyComparator()); // 指定对象比较器
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RPriorityQueue<V>
	 */
	public static <V> RPriorityQueue<V> getPriorityQueue(String key) {
		return redisson().getPriorityQueue(key);
	}

	/**
	 * 优先双端队列 Priority Deque
	 * 可以通过比较器（Comparator）接口来对元素排序
	 * queue.trySetComparator(new MyComparator()); // 指定对象比较器
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RPriorityDeque<V>
	 */
	public static <V> RPriorityDeque<V> getPriorityDeque(String key) {
		return redisson().getPriorityDeque(key);
	}

	/**
	 * 无界优先阻塞队列 Priority Blocking Queue
	 * 可以通过比较器（Comparator）接口来对元素排序
	 * queue.trySetComparator(new MyComparator()); // 指定对象比较器
	 * 方法内部采用话题订阅发布实现，在Redis节点故障转移（主从切换）或断线重连以后，内置的相关话题监听器将自动完成话题的重新订阅
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RPriorityBlockingQueue<V>
	 */
	public static <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String key) {
		return redisson().getPriorityBlockingQueue(key);
	}

	/**
	 * 无界优先阻塞双端队列 Priority Blocking Deque
	 * 可以通过比较器（Comparator）接口来对元素排序
	 * queue.trySetComparator(new MyComparator()); // 指定对象比较器
	 * 方法内部采用话题订阅发布实现，在Redis节点故障转移（主从切换）或断线重连以后，内置的相关话题监听器将自动完成话题的重新订阅
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RPriorityBlockingDeque<V>
	 */
	public static <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String key) {
		return redisson().getPriorityBlockingDeque(key);
	}

	/**
	 * 可重入锁 Reentrant Lock
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RLock
	 */
	public static RLock getLock(String key) {
		return redisson().getLock(key);
	}

	/**
	 * 可重入公平锁 Fair Lock
	 * 它保证了当多个Redisson客户端线程同时请求加锁时，优先分配给先发出请求的线程。
	 * 所有请求线程会在一个队列中排队，当某个线程出现宕机时，Redisson会等待5秒后继续下一个线程，
	 * 也就是说如果前面有5个线程都处于等待状态，那么后面的线程会等待至少25秒
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RLock
	 */
	public static RLock getFairLock(String key) {
		return redisson().getFairLock(key);
	}

	/**
	 * 联锁 MultiLock
	 * 以将多个RLock对象关联为一个联锁
	 * 同时加锁,所有的锁都上锁成功才算成功。
	 * 每个RLock对象实例可以来自于不同的Redisson实例
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param locks RLockS
	 * @return RedissonMultiLock
	 */
	public static RedissonMultiLock getMultiLock(RLock... locks) {
		return new RedissonMultiLock(locks);
	}

	/**
	 * 红锁 RedLock
	 * 对象实现了Red-lock介绍的加锁算法。
	 * 该对象也可以用来将多个RLock对象关联为一个红锁，
	 * 同时加锁,红锁在大部分节点上加锁成功就算成功。
	 * 每个RLock对象实例可以来自于不同的Redisson实例
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param locks RLockS
	 * @return RedissonRedLock
	 */
	public static RedissonRedLock getRedLock(RLock... locks) {
		return new RedissonRedLock(locks);
	}

	/**
	 * 可重入读写锁 ReadWriteLock
	 * 分布式可重入读写锁允许同时有多个读锁和一个写锁处于加锁状态
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return ReadWriteLock
	 */
	public static ReadWriteLock getReadWriteLock(String key) {
		return redisson().getReadWriteLock(key);
	}

	/**
	 * 信号量 Semaphore
	 * 但是如果遇到需要其他进程也能解锁的情况,可使用信号量
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RSemaphore
	 */
	public static RSemaphore getSemaphore(String key) {
		return redisson().getSemaphore(key);
	}

	/**
	 * 可过期性信号量 Permit Expirable Semaphore
	 * 为每个信号增加了一个过期时间
	 * 每个信号可以通过独立的ID来辨识，释放时只能通过提交这个ID才能释放
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RPermitExpirableSemaphore
	 */
	public static RPermitExpirableSemaphore getPermitExpirableSemaphore(String key) {
		return redisson().getPermitExpirableSemaphore(key);
	}

	/**
	 * 闭锁 CountDownLatch
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RCountDownLatch
	 */
	public static RCountDownLatch getCountDownLatch(String key) {
		return redisson().getCountDownLatch(key);
	}

	/**
	 * 批量执行命令
	 * BatchOptions options = BatchOptions.defaults()
	 * 指定执行模式
	 * ExecutionMode.REDIS_READ_ATOMIC - 所有命令缓存在Redis节点中，以原子性事务的方式执行。
	 * ExecutionMode.REDIS_WRITE_ATOMIC - 所有命令缓存在Redis节点中，以原子性事务的方式执行。
	 * ExecutionMode.IN_MEMORY - 所有命令缓存在Redisson本机内存中统一发送，但逐一执行（非事务）。默认模式。
	 * ExecutionMode.IN_MEMORY_ATOMIC - 所有命令缓存在Redisson本机内存中统一发送，并以原子性事务的方式执行。
	 * .executionMode(ExecutionMode.IN_MEMORY)
	 * 告知Redis不用返回结果（可以减少网络用量）
	 * .skipResult()
	 * 将写入操作同步到从节点
	 * 同步到2个从节点，等待时间为1秒钟
	 * .syncSlaves(2, 1, TimeUnit.SECONDS)
	 * 处理结果超时为2秒钟
	 * .responseTimeout(2, TimeUnit.SECONDS)
	 * 命令重试等待间隔时间为2秒钟
	 * .retryInterval(2, TimeUnit.SECONDS);
	 * 命令重试次数。仅适用于未发送成功的命令
	 * .retryAttempts(4);
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @return RBatch
	 */
	public static RBatch createBatch() {
		return redisson.createBatch();
	}

	/**
	 * 事务
	 * Redisson事务通过分布式锁保证了连续写入的原子性，同时在内部通过操作指令队列实现了Redis原本没有的提交与滚回功能。
	 * 当提交与滚回遇到问题的时候，将通过org.redisson.transaction.TransactionException告知用户
	 * TransactionOptions options = TransactionOptions.defaults() BatchOptions.defaults()
	 * 设置参与本次事务的主节点与其从节点同步的超时时间。
	 * 默认值是5秒。AD_ATOMIC - 所有命令缓存在Redis节点中，以原子性事务的方式执行。
	 * .syncSlavesTimeout(5, TimeUnit.SECONDS)ITE_ATOMIC - 所有命令缓存在Redis节点中，以原子性事务的方式执行。
	 * 处理结果超时。Y - 所有命令缓存在Redisson本机内存中统一发送，但逐一执行（非事务）。默认模式。
	 * 默认值是3秒。Y_ATOMIC - 所有命令缓存在Redisson本机内存中统一发送，并以原子性事务的方式执行。
	 * .responseTimeout(3, TimeUnit.SECONDS)onMode.IN_MEMORY)
	 * 命令重试等待间隔时间。仅适用于未发送成功的命令。）
	 * 默认值是1.5秒。
	 * .retryInterval(2, TimeUnit.SECONDS)
	 * 命令重试次数。仅适用于未发送成功的命令。
	 * 默认值是3次。Unit.SECONDS)
	 * .retryAttempts(3)
	 * 事务超时时间。如果规定时间内没有提交该事务则自动滚回。meUnit.SECONDS)
	 * 默认值是5秒。
	 * .timeout(5, TimeUnit.SECONDS);Unit.SECONDS);
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @return RTransaction
	 */
	public static RTransaction createTransaction(TransactionOptions transactionOptions) {
		return redisson.createTransaction(transactionOptions);
	}

	/**
	 * 布隆过滤器
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RBloomFilter<V>
	 */
	public static <V> RBloomFilter<V> getBloomFilter(String key) {
		RBloomFilter<V> bloomFilter = redisson().getBloomFilter(key);
		// 初始化布隆过滤器，预计统计元素数量为55000000，期望误差率为0.03
		bloomFilter.tryInit(55000000L, 0.03);
		return bloomFilter;
	}

	/**
	 * 布隆过滤器
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @param key String
	 * @return RBloomFilter<V>
	 */
	public static <V> RBloomFilter<V> getBloomFilter(String key,long expectedInsertions, double falseProbability) {
		RBloomFilter<V> bloomFilter = redisson().getBloomFilter(key);
		bloomFilter.tryInit(expectedInsertions, falseProbability);
		return bloomFilter;
	}

	/**
	 * 获取keys
	 * @author Lucifer
	 * @date 2023/2/10
	 * @since 1.0
	 * @return keys
	 */
	public static RKeys getKeys() {
		RKeys keys = redisson().getKeys();
		return keys;
	}

}
