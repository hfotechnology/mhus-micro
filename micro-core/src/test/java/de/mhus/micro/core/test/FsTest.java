package de.mhus.micro.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.tests.TestCase;
import de.mhus.micro.core.fs.FsDiscovery;
import de.mhus.micro.core.fs.FsPublisher;

public class FsTest extends TestCase {

	@Test
	public void test() {
		
		File dir = new File("target/fstest");
		MFile.deleteDir(dir);
		
		TestApi      pubApi       = new TestApi();
		TestProvider pubProvider  = new TestProvider();
		FsPublisher  pubPublisher = new FsPublisher(dir);
		pubApi.addProvider(pubProvider);
		pubApi.addPublisher(pubPublisher);
		
		TestApi disApi = new TestApi();
		FsDiscovery disDiscovery = new FsDiscovery(dir);
		disApi.addDiscovery(disDiscovery);
		
		OperationDescription pubDesc1 = new OperationDescription(
				UUID.randomUUID(), 
				"de.test.Operation1", 
				Version.V_1_0_0, 
				"Operation 1",
				null);
		
		pubProvider.add(pubDesc1);
		
		disDiscovery.check();
		
		ArrayList<OperationDescription> list = new ArrayList<>(1);
		disApi.discover(o -> list.add(o));
		
		assertEquals(1, list.size());
		
	}
	
	
}
