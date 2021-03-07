package de.mhus.micro.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.mhus.lib.core.util.IntValue;
import de.mhus.lib.errors.MException;
import de.mhus.lib.tests.TestCase;
import de.mhus.micro.core.api.MicroFilter;
import de.mhus.micro.core.fs.ConfigDiscovery;

public class SimpleTest extends TestCase {

	@Test
	public void testHello() throws MException {
		TestApi api = new TestApi();
		
		ConfigDiscovery config = new ConfigDiscovery(new File("examples/test1.yaml"));
		api.addDiscovery(config);
		
		IntValue cnt = new IntValue();
		config.discover(MicroFilter.ALL, desc -> {
			System.out.println( desc );	
			cnt.value++;
			
		} );
		
		assertEquals(3, cnt.value);
		
	}
}
