package de.mhus.micro.client.redis;

public interface RedisAdmin {

    JedisCon getResource();

    String getNodeName();

}
