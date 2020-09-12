package de.mhus.micro.client.redis;

import redis.clients.jedis.Jedis;

public interface RedisAdmin {

    Jedis getResource();

    String getNodeName();

}
