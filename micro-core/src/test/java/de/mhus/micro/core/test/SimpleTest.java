package de.mhus.micro.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.IntValue;
import de.mhus.lib.errors.MException;
import de.mhus.lib.tests.TestCase;
import de.mhus.micro.core.api.C;
import de.mhus.micro.core.fs.ConfigProvider;

public class SimpleTest extends TestCase {

	OperationDescription oper1 = null;
	OperationDescription oper2 = null;
	OperationDescription oper3 = null;

	@Test
	public void testConfigDeploy() throws MException {
		TestApi api = new TestApi();
		
		ConfigProvider config = new ConfigProvider(new File("examples/test1.yaml"));
		api.addProvider(config);
				
		IntValue cnt = new IntValue();
		config.discover(desc -> {
			System.out.println( desc );	
			if (desc.getPath().equals("de.test.operation1") && desc.getVersionString().equals("1.0.0"))
				oper1 = desc;
			if (desc.getPath().equals("de.test.operation1") && desc.getVersionString().equals("1.0.1"))
				oper2 = desc;
			if (desc.getPath().equals("de.test.operation2") && desc.getVersionString().equals("0.0.0"))
				oper3 = desc;
			
			cnt.value++;
			
		} );
		
		assertEquals(3, cnt.value);
		
		assertNotNull(oper1);
		assertNotNull(oper2);
		assertNotNull(oper3);
		
		assertEquals("rest", oper1.getLabels().get(C.LABEL_PROTO));
		assertEquals("rest", oper2.getLabels().get(C.LABEL_PROTO));
		assertEquals("local", oper3.getLabels().get(C.LABEL_PROTO));
		
	}
	
	@Test
	public void testDiscoveryThenPublisher() throws MException {
		
		TestApi api = new TestApi();
		ConfigProvider discovery = new ConfigProvider(new File("examples/test1.yaml"));
		TestPublisher publisher = new TestPublisher();
		
		api.addProvider(discovery);
		api.addPublisher(publisher);
		
		assertEquals(3, publisher.list.size());
		assertEquals("de.test.operation1:1.0.0", publisher.list.get(0).getPathVersion());
		assertEquals("de.test.operation1:1.0.1", publisher.list.get(1).getPathVersion());
		assertEquals("de.test.operation2:0.0.0", publisher.list.get(2).getPathVersion());

	}
	
	@Test
	public void testPublisherThenDiscovery() throws MException {
		
		TestApi api = new TestApi();
		ConfigProvider discovery = new ConfigProvider(new File("examples/test1.yaml"));
		TestPublisher publisher = new TestPublisher();
		
		api.addPublisher(publisher);
		assertEquals(0, publisher.list.size());
		
		api.addProvider(discovery);
		
		assertEquals(3, publisher.list.size());
		assertEquals("de.test.operation1:1.0.0", publisher.list.get(0).getPathVersion());
		assertEquals("de.test.operation1:1.0.1", publisher.list.get(1).getPathVersion());
		assertEquals("de.test.operation2:0.0.0", publisher.list.get(2).getPathVersion());

	}
	
}
