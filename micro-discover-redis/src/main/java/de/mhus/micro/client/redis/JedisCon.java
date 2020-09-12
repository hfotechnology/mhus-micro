package de.mhus.micro.client.redis;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.Client;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.PipelineBlock;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.util.Slowlog;

@SuppressWarnings("deprecation")
public class JedisCon implements Closeable {

    private Jedis inst;
    private JedisPool pool;

    public JedisCon(JedisPool pool) {
        this.inst = pool.getResource();
        this.pool = pool;
    }

    @Override
    public void close() {
        pool.returnResource(inst);
        inst = null;
    }


    public String set(String key, String value) {
        return inst.set(key, value);
    }

    public String set(String key, String value, String nxxx, String expx, long time) {
        return inst.set(key, value, nxxx, expx, time);
    }

    public String ping() {
        return inst.ping();
    }

    public String set(byte[] key, byte[] value) {
        return inst.set(key, value);
    }

    public String get(String key) {
        return inst.get(key);
    }

    public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time) {
        return inst.set(key, value, nxxx, expx, time);
    }

    public Boolean exists(String key) {
        return inst.exists(key);
    }

    public Long del(String... keys) {
        return inst.del(keys);
    }

    public byte[] get(byte[] key) {
        return inst.get(key);
    }

    public Long del(String key) {
        return inst.del(key);
    }

    public String type(String key) {
        return inst.type(key);
    }

    public String quit() {
        return inst.quit();
    }

    public Boolean exists(byte[] key) {
        return inst.exists(key);
    }

    public Long del(byte[]... keys) {
        return inst.del(keys);
    }

    public Set<String> keys(String pattern) {
        return inst.keys(pattern);
    }

    @Override
    public boolean equals(Object obj) {
        return inst.equals(obj);
    }

    public Long del(byte[] key) {
        return inst.del(key);
    }

    public String type(byte[] key) {
        return inst.type(key);
    }

    public String flushDB() {
        return inst.flushDB();
    }

    public Set<byte[]> keys(byte[] pattern) {
        return inst.keys(pattern);
    }

    public String randomKey() {
        return inst.randomKey();
    }

    public String rename(String oldkey, String newkey) {
        return inst.rename(oldkey, newkey);
    }

    public Long renamenx(String oldkey, String newkey) {
        return inst.renamenx(oldkey, newkey);
    }

    public Long expire(String key, int seconds) {
        return inst.expire(key, seconds);
    }

    public byte[] randomBinaryKey() {
        return inst.randomBinaryKey();
    }

    public String rename(byte[] oldkey, byte[] newkey) {
        return inst.rename(oldkey, newkey);
    }

    public Long renamenx(byte[] oldkey, byte[] newkey) {
        return inst.renamenx(oldkey, newkey);
    }

    public Long expireAt(String key, long unixTime) {
        return inst.expireAt(key, unixTime);
    }

    public Long dbSize() {
        return inst.dbSize();
    }

    public Long expire(byte[] key, int seconds) {
        return inst.expire(key, seconds);
    }

    @Override
    public String toString() {
        return inst.toString();
    }

    public Long ttl(String key) {
        return inst.ttl(key);
    }

    public Long expireAt(byte[] key, long unixTime) {
        return inst.expireAt(key, unixTime);
    }

    public Long move(String key, int dbIndex) {
        return inst.move(key, dbIndex);
    }

    public String getSet(String key, String value) {
        return inst.getSet(key, value);
    }

    public Long ttl(byte[] key) {
        return inst.ttl(key);
    }

    public List<String> mget(String... keys) {
        return inst.mget(keys);
    }

    public Long setnx(String key, String value) {
        return inst.setnx(key, value);
    }

    public String select(int index) {
        return inst.select(index);
    }

    public Long move(byte[] key, int dbIndex) {
        return inst.move(key, dbIndex);
    }

    public String setex(String key, int seconds, String value) {
        return inst.setex(key, seconds, value);
    }

    public String mset(String... keysvalues) {
        return inst.mset(keysvalues);
    }

    public String flushAll() {
        return inst.flushAll();
    }

    public byte[] getSet(byte[] key, byte[] value) {
        return inst.getSet(key, value);
    }

    public List<byte[]> mget(byte[]... keys) {
        return inst.mget(keys);
    }

    public Long msetnx(String... keysvalues) {
        return inst.msetnx(keysvalues);
    }

    public Long setnx(byte[] key, byte[] value) {
        return inst.setnx(key, value);
    }

    public String setex(byte[] key, int seconds, byte[] value) {
        return inst.setex(key, seconds, value);
    }

    public Long decrBy(String key, long integer) {
        return inst.decrBy(key, integer);
    }

    public String mset(byte[]... keysvalues) {
        return inst.mset(keysvalues);
    }

    public Long decr(String key) {
        return inst.decr(key);
    }

    public Long msetnx(byte[]... keysvalues) {
        return inst.msetnx(keysvalues);
    }

    public Long incrBy(String key, long integer) {
        return inst.incrBy(key, integer);
    }

    public Long decrBy(byte[] key, long integer) {
        return inst.decrBy(key, integer);
    }

    public Long incr(String key) {
        return inst.incr(key);
    }

    public Long decr(byte[] key) {
        return inst.decr(key);
    }

    public Long append(String key, String value) {
        return inst.append(key, value);
    }

    public Long incrBy(byte[] key, long integer) {
        return inst.incrBy(key, integer);
    }

    public String substr(String key, int start, int end) {
        return inst.substr(key, start, end);
    }

    public Long incr(byte[] key) {
        return inst.incr(key);
    }

    public Long hset(String key, String field, String value) {
        return inst.hset(key, field, value);
    }

    public String hget(String key, String field) {
        return inst.hget(key, field);
    }

    public Long append(byte[] key, byte[] value) {
        return inst.append(key, value);
    }

    public Long hsetnx(String key, String field, String value) {
        return inst.hsetnx(key, field, value);
    }

    public byte[] substr(byte[] key, int start, int end) {
        return inst.substr(key, start, end);
    }

    public String hmset(String key, Map<String, String> hash) {
        return inst.hmset(key, hash);
    }

    public List<String> hmget(String key, String... fields) {
        return inst.hmget(key, fields);
    }

    public Long hset(byte[] key, byte[] field, byte[] value) {
        return inst.hset(key, field, value);
    }

    public Long hincrBy(String key, String field, long value) {
        return inst.hincrBy(key, field, value);
    }

    public byte[] hget(byte[] key, byte[] field) {
        return inst.hget(key, field);
    }

    public Long hsetnx(byte[] key, byte[] field, byte[] value) {
        return inst.hsetnx(key, field, value);
    }

    public Boolean hexists(String key, String field) {
        return inst.hexists(key, field);
    }

    public String hmset(byte[] key, Map<byte[], byte[]> hash) {
        return inst.hmset(key, hash);
    }

    public Long hdel(String key, String... fields) {
        return inst.hdel(key, fields);
    }

    public Long hlen(String key) {
        return inst.hlen(key);
    }

    public List<byte[]> hmget(byte[] key, byte[]... fields) {
        return inst.hmget(key, fields);
    }

    public Set<String> hkeys(String key) {
        return inst.hkeys(key);
    }

    public Long hincrBy(byte[] key, byte[] field, long value) {
        return inst.hincrBy(key, field, value);
    }

    public List<String> hvals(String key) {
        return inst.hvals(key);
    }

    public Map<String, String> hgetAll(String key) {
        return inst.hgetAll(key);
    }

    public Boolean hexists(byte[] key, byte[] field) {
        return inst.hexists(key, field);
    }

    public Long rpush(String key, String... strings) {
        return inst.rpush(key, strings);
    }

    public Long hdel(byte[] key, byte[]... fields) {
        return inst.hdel(key, fields);
    }

    public Long lpush(String key, String... strings) {
        return inst.lpush(key, strings);
    }

    public Long hlen(byte[] key) {
        return inst.hlen(key);
    }

    public Set<byte[]> hkeys(byte[] key) {
        return inst.hkeys(key);
    }

    public Long llen(String key) {
        return inst.llen(key);
    }

    public List<byte[]> hvals(byte[] key) {
        return inst.hvals(key);
    }

    public List<String> lrange(String key, long start, long end) {
        return inst.lrange(key, start, end);
    }

    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return inst.hgetAll(key);
    }

    public Long rpush(byte[] key, byte[]... strings) {
        return inst.rpush(key, strings);
    }

    public Long lpush(byte[] key, byte[]... strings) {
        return inst.lpush(key, strings);
    }

    public String ltrim(String key, long start, long end) {
        return inst.ltrim(key, start, end);
    }

    public Long llen(byte[] key) {
        return inst.llen(key);
    }

    public List<byte[]> lrange(byte[] key, long start, long end) {
        return inst.lrange(key, start, end);
    }

    public String lindex(String key, long index) {
        return inst.lindex(key, index);
    }

    public String lset(String key, long index, String value) {
        return inst.lset(key, index, value);
    }

    public String ltrim(byte[] key, long start, long end) {
        return inst.ltrim(key, start, end);
    }

    public Long lrem(String key, long count, String value) {
        return inst.lrem(key, count, value);
    }

    public byte[] lindex(byte[] key, long index) {
        return inst.lindex(key, index);
    }

    public String lpop(String key) {
        return inst.lpop(key);
    }

    public String rpop(String key) {
        return inst.rpop(key);
    }

    public String lset(byte[] key, long index, byte[] value) {
        return inst.lset(key, index, value);
    }

    public String rpoplpush(String srckey, String dstkey) {
        return inst.rpoplpush(srckey, dstkey);
    }

    public Long lrem(byte[] key, long count, byte[] value) {
        return inst.lrem(key, count, value);
    }

    public Long sadd(String key, String... members) {
        return inst.sadd(key, members);
    }

    public Set<String> smembers(String key) {
        return inst.smembers(key);
    }

    public byte[] lpop(byte[] key) {
        return inst.lpop(key);
    }

    public Long srem(String key, String... members) {
        return inst.srem(key, members);
    }

    public byte[] rpop(byte[] key) {
        return inst.rpop(key);
    }

    public String spop(String key) {
        return inst.spop(key);
    }

    public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
        return inst.rpoplpush(srckey, dstkey);
    }

    public Long smove(String srckey, String dstkey, String member) {
        return inst.smove(srckey, dstkey, member);
    }

    public Long sadd(byte[] key, byte[]... members) {
        return inst.sadd(key, members);
    }

    public Long scard(String key) {
        return inst.scard(key);
    }

    public Set<byte[]> smembers(byte[] key) {
        return inst.smembers(key);
    }

    public Boolean sismember(String key, String member) {
        return inst.sismember(key, member);
    }

    public Long srem(byte[] key, byte[]... member) {
        return inst.srem(key, member);
    }

    public Set<String> sinter(String... keys) {
        return inst.sinter(keys);
    }

    public byte[] spop(byte[] key) {
        return inst.spop(key);
    }

    public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
        return inst.smove(srckey, dstkey, member);
    }

    public Long sinterstore(String dstkey, String... keys) {
        return inst.sinterstore(dstkey, keys);
    }

    public Set<String> sunion(String... keys) {
        return inst.sunion(keys);
    }

    public Long scard(byte[] key) {
        return inst.scard(key);
    }

    public Boolean sismember(byte[] key, byte[] member) {
        return inst.sismember(key, member);
    }

    public Long sunionstore(String dstkey, String... keys) {
        return inst.sunionstore(dstkey, keys);
    }

    public Set<byte[]> sinter(byte[]... keys) {
        return inst.sinter(keys);
    }

    public Set<String> sdiff(String... keys) {
        return inst.sdiff(keys);
    }

    public Long sdiffstore(String dstkey, String... keys) {
        return inst.sdiffstore(dstkey, keys);
    }

    public Long sinterstore(byte[] dstkey, byte[]... keys) {
        return inst.sinterstore(dstkey, keys);
    }

    public String srandmember(String key) {
        return inst.srandmember(key);
    }

    public Set<byte[]> sunion(byte[]... keys) {
        return inst.sunion(keys);
    }

    public List<String> srandmember(String key, int count) {
        return inst.srandmember(key, count);
    }

    public Long zadd(String key, double score, String member) {
        return inst.zadd(key, score, member);
    }

    public Long sunionstore(byte[] dstkey, byte[]... keys) {
        return inst.sunionstore(dstkey, keys);
    }

    public Set<byte[]> sdiff(byte[]... keys) {
        return inst.sdiff(keys);
    }

    public Long zadd(String key, Map<String, Double> scoreMembers) {
        return inst.zadd(key, scoreMembers);
    }

    public Set<String> zrange(String key, long start, long end) {
        return inst.zrange(key, start, end);
    }

    public Long zrem(String key, String... members) {
        return inst.zrem(key, members);
    }

    public Long sdiffstore(byte[] dstkey, byte[]... keys) {
        return inst.sdiffstore(dstkey, keys);
    }

    public Double zincrby(String key, double score, String member) {
        return inst.zincrby(key, score, member);
    }

    public byte[] srandmember(byte[] key) {
        return inst.srandmember(key);
    }

    public List<byte[]> srandmember(byte[] key, int count) {
        return inst.srandmember(key, count);
    }

    public Long zadd(byte[] key, double score, byte[] member) {
        return inst.zadd(key, score, member);
    }

    public Long zrank(String key, String member) {
        return inst.zrank(key, member);
    }

    public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
        return inst.zadd(key, scoreMembers);
    }

    public Set<byte[]> zrange(byte[] key, long start, long end) {
        return inst.zrange(key, start, end);
    }

    public Long zrevrank(String key, String member) {
        return inst.zrevrank(key, member);
    }

    public Long zrem(byte[] key, byte[]... members) {
        return inst.zrem(key, members);
    }

    public Double zincrby(byte[] key, double score, byte[] member) {
        return inst.zincrby(key, score, member);
    }

    public Set<String> zrevrange(String key, long start, long end) {
        return inst.zrevrange(key, start, end);
    }

    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        return inst.zrangeWithScores(key, start, end);
    }

    public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
        return inst.zrevrangeWithScores(key, start, end);
    }

    public Long zcard(String key) {
        return inst.zcard(key);
    }

    public Double zscore(String key, String member) {
        return inst.zscore(key, member);
    }

    public Long zrank(byte[] key, byte[] member) {
        return inst.zrank(key, member);
    }

    public String watch(String... keys) {
        return inst.watch(keys);
    }

    public List<String> sort(String key) {
        return inst.sort(key);
    }

    public Long zrevrank(byte[] key, byte[] member) {
        return inst.zrevrank(key, member);
    }

    public List<String> sort(String key, SortingParams sortingParameters) {
        return inst.sort(key, sortingParameters);
    }

    public Set<byte[]> zrevrange(byte[] key, long start, long end) {
        return inst.zrevrange(key, start, end);
    }

    public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
        return inst.zrangeWithScores(key, start, end);
    }

    public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end) {
        return inst.zrevrangeWithScores(key, start, end);
    }

    public Long zcard(byte[] key) {
        return inst.zcard(key);
    }

    public Double zscore(byte[] key, byte[] member) {
        return inst.zscore(key, member);
    }

    public List<String> blpop(int timeout, String... keys) {
        return inst.blpop(timeout, keys);
    }

    public Transaction multi() {
        return inst.multi();
    }

    public List<Object> multi(TransactionBlock jedisTransaction) {
        return inst.multi(jedisTransaction);
    }

    public void connect() {
        inst.connect();
    }

    public void disconnect() {
        inst.disconnect();
    }

    public void resetState() {
        inst.resetState();
    }

    public String watch(byte[]... keys) {
        return inst.watch(keys);
    }

    public String unwatch() {
        return inst.unwatch();
    }

    public List<byte[]> sort(byte[] key) {
        return inst.sort(key);
    }

    public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
        return inst.sort(key, sortingParameters);
    }

    public List<String> blpop(String... args) {
        return inst.blpop(args);
    }

    public List<byte[]> blpop(int timeout, byte[]... keys) {
        return inst.blpop(timeout, keys);
    }

    public List<String> brpop(String... args) {
        return inst.brpop(args);
    }

    public List<String> blpop(String arg) {
        return inst.blpop(arg);
    }

    public List<String> brpop(String arg) {
        return inst.brpop(arg);
    }

    public Long sort(String key, SortingParams sortingParameters, String dstkey) {
        return inst.sort(key, sortingParameters, dstkey);
    }

    public Long sort(String key, String dstkey) {
        return inst.sort(key, dstkey);
    }

    public List<String> brpop(int timeout, String... keys) {
        return inst.brpop(timeout, keys);
    }

    public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
        return inst.sort(key, sortingParameters, dstkey);
    }

    public Long sort(byte[] key, byte[] dstkey) {
        return inst.sort(key, dstkey);
    }

    public List<byte[]> brpop(int timeout, byte[]... keys) {
        return inst.brpop(timeout, keys);
    }

    public Long zcount(String key, double min, double max) {
        return inst.zcount(key, min, max);
    }

    public Long zcount(String key, String min, String max) {
        return inst.zcount(key, min, max);
    }

    public Set<String> zrangeByScore(String key, double min, double max) {
        return inst.zrangeByScore(key, min, max);
    }

    public List<byte[]> blpop(byte[] arg) {
        return inst.blpop(arg);
    }

    public Set<String> zrangeByScore(String key, String min, String max) {
        return inst.zrangeByScore(key, min, max);
    }

    public List<byte[]> brpop(byte[] arg) {
        return inst.brpop(arg);
    }

    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return inst.zrangeByScore(key, min, max, offset, count);
    }

    public List<byte[]> blpop(byte[]... args) {
        return inst.blpop(args);
    }

    public List<byte[]> brpop(byte[]... args) {
        return inst.brpop(args);
    }

    public String auth(String password) {
        return inst.auth(password);
    }

    public List<Object> pipelined(PipelineBlock jedisPipeline) {
        return inst.pipelined(jedisPipeline);
    }

    public Pipeline pipelined() {
        return inst.pipelined();
    }

    public Long zcount(byte[] key, double min, double max) {
        return inst.zcount(key, min, max);
    }

    public Long zcount(byte[] key, byte[] min, byte[] max) {
        return inst.zcount(key, min, max);
    }

    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        return inst.zrangeByScore(key, min, max);
    }

    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return inst.zrangeByScore(key, min, max, offset, count);
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return inst.zrangeByScoreWithScores(key, min, max);
    }

    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
        return inst.zrangeByScore(key, min, max);
    }

    public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
        return inst.zrangeByScore(key, min, max, offset, count);
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return inst.zrangeByScoreWithScores(key, min, max);
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return inst.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return inst.zrangeByScore(key, min, max, offset, count);
    }

    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return inst.zrangeByScoreWithScores(key, min, max);
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return inst.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return inst.zrevrangeByScore(key, max, min);
    }

    public Set<String> zrevrangeByScore(String key, String max, String min) {
        return inst.zrevrangeByScore(key, max, min);
    }

    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return inst.zrevrangeByScore(key, max, min, offset, count);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return inst.zrevrangeByScoreWithScores(key, max, min);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return inst.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return inst.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return inst.zrevrangeByScore(key, max, min, offset, count);
    }

    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
        return inst.zrangeByScoreWithScores(key, min, max);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return inst.zrevrangeByScoreWithScores(key, max, min);
    }

    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return inst.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    public Long zremrangeByRank(String key, long start, long end) {
        return inst.zremrangeByRank(key, start, end);
    }

    public Long zremrangeByScore(String key, double start, double end) {
        return inst.zremrangeByScore(key, start, end);
    }

    public Long zremrangeByScore(String key, String start, String end) {
        return inst.zremrangeByScore(key, start, end);
    }

    public Long zunionstore(String dstkey, String... sets) {
        return inst.zunionstore(dstkey, sets);
    }

    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return inst.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
        return inst.zrevrangeByScore(key, max, min);
    }

    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
        return inst.zrevrangeByScore(key, max, min);
    }

    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
        return inst.zrevrangeByScore(key, max, min, offset, count);
    }

    public Long zunionstore(String dstkey, ZParams params, String... sets) {
        return inst.zunionstore(dstkey, params, sets);
    }

    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return inst.zrevrangeByScore(key, max, min, offset, count);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
        return inst.zrevrangeByScoreWithScores(key, max, min);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
        return inst.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
        return inst.zrevrangeByScoreWithScores(key, max, min);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return inst.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    public Long zremrangeByRank(byte[] key, long start, long end) {
        return inst.zremrangeByRank(key, start, end);
    }

    public Long zinterstore(String dstkey, String... sets) {
        return inst.zinterstore(dstkey, sets);
    }

    public Long zremrangeByScore(byte[] key, double start, double end) {
        return inst.zremrangeByScore(key, start, end);
    }

    public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
        return inst.zremrangeByScore(key, start, end);
    }

    public Long zunionstore(byte[] dstkey, byte[]... sets) {
        return inst.zunionstore(dstkey, sets);
    }

    public Long zinterstore(String dstkey, ZParams params, String... sets) {
        return inst.zinterstore(dstkey, params, sets);
    }

    public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return inst.zunionstore(dstkey, params, sets);
    }

    public Long strlen(String key) {
        return inst.strlen(key);
    }

    public Long lpushx(String key, String... string) {
        return inst.lpushx(key, string);
    }

    public Long persist(String key) {
        return inst.persist(key);
    }

    public Long rpushx(String key, String... string) {
        return inst.rpushx(key, string);
    }

    public String echo(String string) {
        return inst.echo(string);
    }

    public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
        return inst.linsert(key, where, pivot, value);
    }

    public Long zinterstore(byte[] dstkey, byte[]... sets) {
        return inst.zinterstore(dstkey, sets);
    }

    public String brpoplpush(String source, String destination, int timeout) {
        return inst.brpoplpush(source, destination, timeout);
    }

    public Boolean setbit(String key, long offset, boolean value) {
        return inst.setbit(key, offset, value);
    }

    public Boolean setbit(String key, long offset, String value) {
        return inst.setbit(key, offset, value);
    }

    public Boolean getbit(String key, long offset) {
        return inst.getbit(key, offset);
    }

    public Long setrange(String key, long offset, String value) {
        return inst.setrange(key, offset, value);
    }

    public String getrange(String key, long startOffset, long endOffset) {
        return inst.getrange(key, startOffset, endOffset);
    }

    public List<String> configGet(String pattern) {
        return inst.configGet(pattern);
    }

    public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return inst.zinterstore(dstkey, params, sets);
    }

    public String configSet(String parameter, String value) {
        return inst.configSet(parameter, value);
    }

    public String save() {
        return inst.save();
    }

    public Object eval(String script, int keyCount, String... params) {
        return inst.eval(script, keyCount, params);
    }

    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        inst.subscribe(jedisPubSub, channels);
    }

    public Long publish(String channel, String message) {
        return inst.publish(channel, message);
    }

    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        inst.psubscribe(jedisPubSub, patterns);
    }

    public String bgsave() {
        return inst.bgsave();
    }

    public String bgrewriteaof() {
        return inst.bgrewriteaof();
    }

    public Object eval(String script, List<String> keys, List<String> args) {
        return inst.eval(script, keys, args);
    }

    public Object eval(String script) {
        return inst.eval(script);
    }

    public Object evalsha(String script) {
        return inst.evalsha(script);
    }

    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return inst.evalsha(sha1, keys, args);
    }

    public Object evalsha(String sha1, int keyCount, String... params) {
        return inst.evalsha(sha1, keyCount, params);
    }

    public Long lastsave() {
        return inst.lastsave();
    }

    public Boolean scriptExists(String sha1) {
        return inst.scriptExists(sha1);
    }

    public List<Boolean> scriptExists(String... sha1) {
        return inst.scriptExists(sha1);
    }

    public String scriptLoad(String script) {
        return inst.scriptLoad(script);
    }

    public String shutdown() {
        return inst.shutdown();
    }

    public List<Slowlog> slowlogGet() {
        return inst.slowlogGet();
    }

    public List<Slowlog> slowlogGet(long entries) {
        return inst.slowlogGet(entries);
    }

    public Long objectRefcount(String string) {
        return inst.objectRefcount(string);
    }

    public String objectEncoding(String string) {
        return inst.objectEncoding(string);
    }

    public Long objectIdletime(String string) {
        return inst.objectIdletime(string);
    }

    public Long bitcount(String key) {
        return inst.bitcount(key);
    }

    public String info() {
        return inst.info();
    }

    public Long bitcount(String key, long start, long end) {
        return inst.bitcount(key, start, end);
    }

    public Long bitop(BitOP op, String destKey, String... srcKeys) {
        return inst.bitop(op, destKey, srcKeys);
    }

    public List<Map<String, String>> sentinelMasters() {
        return inst.sentinelMasters();
    }

    public String info(String section) {
        return inst.info(section);
    }

    public List<String> sentinelGetMasterAddrByName(String masterName) {
        return inst.sentinelGetMasterAddrByName(masterName);
    }

    public void monitor(JedisMonitor jedisMonitor) {
        inst.monitor(jedisMonitor);
    }

    public Long sentinelReset(String pattern) {
        return inst.sentinelReset(pattern);
    }

    public String slaveof(String host, int port) {
        return inst.slaveof(host, port);
    }

    public List<Map<String, String>> sentinelSlaves(String masterName) {
        return inst.sentinelSlaves(masterName);
    }

    public String slaveofNoOne() {
        return inst.slaveofNoOne();
    }

    public List<byte[]> configGet(byte[] pattern) {
        return inst.configGet(pattern);
    }

    public String sentinelFailover(String masterName) {
        return inst.sentinelFailover(masterName);
    }

    public String sentinelMonitor(String masterName, String ip, int port, int quorum) {
        return inst.sentinelMonitor(masterName, ip, port, quorum);
    }

    public String sentinelRemove(String masterName) {
        return inst.sentinelRemove(masterName);
    }

    public String sentinelSet(String masterName, Map<String, String> parameterMap) {
        return inst.sentinelSet(masterName, parameterMap);
    }

    public String configResetStat() {
        return inst.configResetStat();
    }

    public byte[] dump(String key) {
        return inst.dump(key);
    }

    public byte[] configSet(byte[] parameter, byte[] value) {
        return inst.configSet(parameter, value);
    }

    public String restore(String key, int ttl, byte[] serializedValue) {
        return inst.restore(key, ttl, serializedValue);
    }

    public Long pexpire(String key, int milliseconds) {
        return inst.pexpire(key, milliseconds);
    }

    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return inst.pexpireAt(key, millisecondsTimestamp);
    }

    public Long pttl(String key) {
        return inst.pttl(key);
    }

    public Double incrByFloat(String key, double increment) {
        return inst.incrByFloat(key, increment);
    }

    public String psetex(String key, int milliseconds, String value) {
        return inst.psetex(key, milliseconds, value);
    }

    public String set(String key, String value, String nxxx) {
        return inst.set(key, value, nxxx);
    }

    public String set(String key, String value, String nxxx, String expx, int time) {
        return inst.set(key, value, nxxx, expx, time);
    }

    public String clientKill(String client) {
        return inst.clientKill(client);
    }

    public boolean isConnected() {
        return inst.isConnected();
    }

    public String clientSetname(String name) {
        return inst.clientSetname(name);
    }

    public Long strlen(byte[] key) {
        return inst.strlen(key);
    }

    public void sync() {
        inst.sync();
    }

    public String migrate(String host, int port, String key, int destinationDb, int timeout) {
        return inst.migrate(host, port, key, destinationDb, timeout);
    }

    public Long lpushx(byte[] key, byte[]... string) {
        return inst.lpushx(key, string);
    }

    public Long persist(byte[] key) {
        return inst.persist(key);
    }

    public Double hincrByFloat(String key, String field, double increment) {
        return inst.hincrByFloat(key, field, increment);
    }

    public ScanResult<String> scan(int cursor) {
        return inst.scan(cursor);
    }

    public Long rpushx(byte[] key, byte[]... string) {
        return inst.rpushx(key, string);
    }

    public byte[] echo(byte[] string) {
        return inst.echo(string);
    }

    public ScanResult<String> scan(int cursor, ScanParams params) {
        return inst.scan(cursor, params);
    }

    public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot, byte[] value) {
        return inst.linsert(key, where, pivot, value);
    }

    public String debug(DebugParams params) {
        return inst.debug(params);
    }

    public Client getClient() {
        return inst.getClient();
    }

    public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
        return inst.brpoplpush(source, destination, timeout);
    }

    public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
        return inst.hscan(key, cursor);
    }

    public Boolean setbit(byte[] key, long offset, boolean value) {
        return inst.setbit(key, offset, value);
    }

    public ScanResult<Entry<String, String>> hscan(String key, int cursor, ScanParams params) {
        return inst.hscan(key, cursor, params);
    }

    public Boolean setbit(byte[] key, long offset, byte[] value) {
        return inst.setbit(key, offset, value);
    }

    public Boolean getbit(byte[] key, long offset) {
        return inst.getbit(key, offset);
    }

    public Long setrange(byte[] key, long offset, byte[] value) {
        return inst.setrange(key, offset, value);
    }

    public byte[] getrange(byte[] key, long startOffset, long endOffset) {
        return inst.getrange(key, startOffset, endOffset);
    }

    public Long publish(byte[] channel, byte[] message) {
        return inst.publish(channel, message);
    }

    public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
        inst.subscribe(jedisPubSub, channels);
    }

    public ScanResult<String> sscan(String key, int cursor) {
        return inst.sscan(key, cursor);
    }

    public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
        inst.psubscribe(jedisPubSub, patterns);
    }

    public ScanResult<String> sscan(String key, int cursor, ScanParams params) {
        return inst.sscan(key, cursor, params);
    }

    public Long getDB() {
        return inst.getDB();
    }

    public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
        return inst.eval(script, keys, args);
    }

    public ScanResult<Tuple> zscan(String key, int cursor) {
        return inst.zscan(key, cursor);
    }

    public Object eval(byte[] script, byte[] keyCount, byte[]... params) {
        return inst.eval(script, keyCount, params);
    }

    public Object eval(byte[] script, int keyCount, byte[]... params) {
        return inst.eval(script, keyCount, params);
    }

    public ScanResult<Tuple> zscan(String key, int cursor, ScanParams params) {
        return inst.zscan(key, cursor, params);
    }

    public Object eval(byte[] script) {
        return inst.eval(script);
    }

    public Object evalsha(byte[] sha1) {
        return inst.evalsha(sha1);
    }

    public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
        return inst.evalsha(sha1, keys, args);
    }

    public Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
        return inst.evalsha(sha1, keyCount, params);
    }

    public ScanResult<String> scan(String cursor) {
        return inst.scan(cursor);
    }

    public String scriptFlush() {
        return inst.scriptFlush();
    }

    public ScanResult<String> scan(String cursor, ScanParams params) {
        return inst.scan(cursor, params);
    }

    public List<Long> scriptExists(byte[]... sha1) {
        return inst.scriptExists(sha1);
    }

    public byte[] scriptLoad(byte[] script) {
        return inst.scriptLoad(script);
    }

    public String scriptKill() {
        return inst.scriptKill();
    }

    public String slowlogReset() {
        return inst.slowlogReset();
    }

    public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
        return inst.hscan(key, cursor);
    }

    public Long slowlogLen() {
        return inst.slowlogLen();
    }

    public List<byte[]> slowlogGetBinary() {
        return inst.slowlogGetBinary();
    }

    public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        return inst.hscan(key, cursor, params);
    }

    public List<byte[]> slowlogGetBinary(long entries) {
        return inst.slowlogGetBinary(entries);
    }

    public Long objectRefcount(byte[] key) {
        return inst.objectRefcount(key);
    }

    public byte[] objectEncoding(byte[] key) {
        return inst.objectEncoding(key);
    }

    public Long objectIdletime(byte[] key) {
        return inst.objectIdletime(key);
    }

    public Long bitcount(byte[] key) {
        return inst.bitcount(key);
    }

    public Long bitcount(byte[] key, long start, long end) {
        return inst.bitcount(key, start, end);
    }

    public ScanResult<String> sscan(String key, String cursor) {
        return inst.sscan(key, cursor);
    }

    public Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
        return inst.bitop(op, destKey, srcKeys);
    }

    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        return inst.sscan(key, cursor, params);
    }

    public byte[] dump(byte[] key) {
        return inst.dump(key);
    }

    public String restore(byte[] key, int ttl, byte[] serializedValue) {
        return inst.restore(key, ttl, serializedValue);
    }

    public Long pexpire(byte[] key, int milliseconds) {
        return inst.pexpire(key, milliseconds);
    }

    public ScanResult<Tuple> zscan(String key, String cursor) {
        return inst.zscan(key, cursor);
    }

    public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
        return inst.pexpireAt(key, millisecondsTimestamp);
    }

    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        return inst.zscan(key, cursor, params);
    }

    public Long pttl(byte[] key) {
        return inst.pttl(key);
    }

    public Double incrByFloat(byte[] key, double increment) {
        return inst.incrByFloat(key, increment);
    }

    public String psetex(byte[] key, int milliseconds, byte[] value) {
        return inst.psetex(key, milliseconds, value);
    }

    public String clusterNodes() {
        return inst.clusterNodes();
    }

    public String set(byte[] key, byte[] value, byte[] nxxx) {
        return inst.set(key, value, nxxx);
    }

    public String clusterMeet(String ip, int port) {
        return inst.clusterMeet(ip, port);
    }

    public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, int time) {
        return inst.set(key, value, nxxx, expx, time);
    }

    public String clusterAddSlots(int... slots) {
        return inst.clusterAddSlots(slots);
    }

    public String clientKill(byte[] client) {
        return inst.clientKill(client);
    }

    public String clusterDelSlots(int... slots) {
        return inst.clusterDelSlots(slots);
    }

    public String clusterInfo() {
        return inst.clusterInfo();
    }

    public String clientGetname() {
        return inst.clientGetname();
    }

    public String clientList() {
        return inst.clientList();
    }

    public List<String> clusterGetKeysInSlot(int slot, int count) {
        return inst.clusterGetKeysInSlot(slot, count);
    }

    public String clientSetname(byte[] name) {
        return inst.clientSetname(name);
    }

    public String clusterSetSlotNode(int slot, String nodeId) {
        return inst.clusterSetSlotNode(slot, nodeId);
    }

    public List<String> time() {
        return inst.time();
    }

    public String migrate(byte[] host, int port, byte[] key, int destinationDb, int timeout) {
        return inst.migrate(host, port, key, destinationDb, timeout);
    }

    public String clusterSetSlotMigrating(int slot, String nodeId) {
        return inst.clusterSetSlotMigrating(slot, nodeId);
    }

    public String clusterSetSlotImporting(int slot, String nodeId) {
        return inst.clusterSetSlotImporting(slot, nodeId);
    }

    public Double hincrByFloat(byte[] key, byte[] field, double increment) {
        return inst.hincrByFloat(key, field, increment);
    }

    public String asking() {
        return inst.asking();
    }

    public List<String> pubsubChannels(String pattern) {
        return inst.pubsubChannels(pattern);
    }

    public Long waitReplicas(int replicas, long timeout) {
        return inst.waitReplicas(replicas, timeout);
    }

    public Long pubsubNumPat() {
        return inst.pubsubNumPat();
    }

    public Map<String, String> pubsubNumSub(String... channels) {
        return inst.pubsubNumSub(channels);
    }
}
