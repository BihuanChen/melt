package mlt.test.run;

import java.io.IOException;
import java.net.MalformedURLException;

import mlt.test.Profiles;

public class TestRunnerClient {

	TestChronicle testChronicle;
	
	public TestRunnerClient() throws IOException {
		testChronicle = new TestChronicle(false);
	}
	
	public void run(Object[] test) throws MalformedURLException {
		testChronicle.write(test, null);
		testChronicle.read();
	}
	
	public static void main(String[] args) throws IOException {
		mlt.Config.loadProperties("/home/bhchen/workspace/testing/phosphor-test/src/phosphor/test/Test1.mlt");
		TestRunnerClient client = new TestRunnerClient();
		Object[] test1 = new Object[]{1, 2};
		client.run(test1);
		Profiles.printExecutedPredicates();
		Profiles.printTaints();
		Profiles.executedPredicates = null;
		Profiles.taints.clear();
		
		Object[] test2 = new Object[]{-1, -2};
		client.run(test2);
		Profiles.printExecutedPredicates();
		Profiles.printTaints();
		Profiles.executedPredicates = null;
		Profiles.taints.clear();
	}
	
}
