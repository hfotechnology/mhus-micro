package de.mhus.micro.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.mhus.lib.tests.TestCase;
import de.mhus.lib.tests.TestUtil;
import de.mhus.micro.core.api.MicroResult;
import de.mhus.micro.core.impl.AbstractApi;
import de.mhus.micro.core.oper.LocalOperationProtocol;
import de.mhus.micro.core.oper.LocalOperationProvider;
import de.mhus.micro.core.oper.OperationProvider;

public class OperationTest extends TestCase {

	@Test
	public void testLocalCall() throws Exception {
		TestUtil.enableDebug();
		
		AbstractApi api = new AbstractApi();
		OperationProvider provider = new LocalOperationProvider();
		TestPublisher publisher = new TestPublisher();
		LocalOperationProtocol executor = new LocalOperationProtocol();
		
		api.addProvider(provider);
		api.addPublisher(publisher);
		api.addProtocol(executor);

		assertEquals(0, publisher.list.size());
		
		TestOperation operation = new TestOperation();
		provider.add(operation);
		executor.add(operation);

		assertEquals(1, publisher.list.size());
		
		assertFalse(operation.done);
		
		MicroResult res = api.execute("de.mhus.micro.core.test.TestOperation:0.0.0", null, null);
		System.out.println(res);
		
		assertTrue(res.isTransportSuccessful());
		
		assertTrue(operation.done);
		

	}
}
