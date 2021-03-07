package de.mhus.micro.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import com.github.microwww.redis.RedisServer;

import de.mhus.lib.tests.TestCase;
import de.mhus.micro.core.redis.RedisDiscovery;
import de.mhus.micro.core.redis.RedisPublisher;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisTest extends TestCase {

	@Test
	public void testJdisServer() throws IOException {
		//https://github.com/microwww/jedis-mock/
		try (RedisServer server = new RedisServer()) {
		    server.listener("127.0.0.1", 0); // Redis runs in the background
		    InetSocketAddress address = (InetSocketAddress) server.getServerSocket().getLocalSocketAddress();
		    log().i("Redis start :: [{}:{}]", address.getHostName(), address.getPort());
		    
		    JedisPool pool = new JedisPool(address.getHostName(), address.getPort());
	    	try (Jedis con = pool.getResource()) {
			    con.set("test", "Hello");
			    
			    String value = con.get("test");
			    assertEquals("Hello", value);
	    	}
		}
	}
	
	@Test
	public void testDiscovery() throws IOException {
		//https://github.com/microwww/jedis-mock/
		try (RedisServer server = new RedisServer()) {
		    server.listener("127.0.0.1", 0); // Redis runs in the background
		    InetSocketAddress address = (InetSocketAddress) server.getServerSocket().getLocalSocketAddress();
		    log().i("Redis start :: [{}:{}]", address.getHostName(), address.getPort());
		    
		    JedisPool pool = new JedisPool(address.getHostName(), address.getPort());
		    
		    RedisPublisher publisher = new RedisPublisher();
		    publisher.setPool(pool);
		    RedisDiscovery discovery = new RedisDiscovery();
		    discovery.setPool(pool);
		    TestApi api = new TestApi();
		    api.addPublisher(publisher);
		    api.addDiscovery(discovery);
		    
		    // TODO test
		}
	}
	
	
	
}
