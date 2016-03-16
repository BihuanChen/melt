package melt.test.run;

import java.io.IOException;
import java.net.MalformedURLException;

import melt.test.Profiles;

public class TestRunnerClient {

	private boolean isLocal;
	private TestChronicle testChronicle;
	private TestRunnerUtil runnerUtil;
	
	public TestRunnerClient(boolean isLocal) throws IOException {
		this.isLocal = isLocal;
		if (isLocal) {
			runnerUtil = new TestRunnerUtil();
		} else {
			testChronicle = new TestChronicle(false);
		}
	}
	
	public void run(Object[] test) throws MalformedURLException {
		if (isLocal) {
			runnerUtil.run(test);
		} else {
			testChronicle.write(test, null);
			testChronicle.read();
		}
	}
	
	public static void main(String[] args) throws IOException {
		melt.Config.loadProperties("/home/bhchen/workspace/testing/benchmark1-art/src/dt/original/Fisher.melt");
		TestRunnerClient client = new TestRunnerClient(false);
		Object[] test1 = new Object[]{-68424864, 93502598, 6.488842318872845E7};
		client.run(test1);
		Profiles.printExecutedPredicates();
		Profiles.printTaints();
		Profiles.executedPredicates.clear();
		Profiles.taints.clear();
		
		Object[] test2 = new Object[]{-26898996, -2293003, 3.251309129702559E7};
		client.run(test2);
		Profiles.printExecutedPredicates();
		Profiles.printTaints();
		Profiles.executedPredicates.clear();
		Profiles.taints.clear();
	}
	
}
