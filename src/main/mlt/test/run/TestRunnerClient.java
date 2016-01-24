package mlt.test.run;

import java.io.IOException;
import java.net.MalformedURLException;

import mlt.test.Profiles;

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
		mlt.Config.loadProperties("/home/bhchen/workspace/testing/benchmark0-test/src/phosphor/test/Test.mlt");
		TestRunnerClient client = new TestRunnerClient(false);
		Object[] test1 = new Object[]{1, 2};
		client.run(test1);
		Profiles.printExecutedPredicates();
		Profiles.printTaints();
		Profiles.executedPredicates.clear();
		Profiles.taints.clear();
		
		Object[] test2 = new Object[]{-1, -2};
		client.run(test2);
		Profiles.printExecutedPredicates();
		Profiles.printTaints();
		Profiles.executedPredicates.clear();
		Profiles.taints.clear();
	}
	
}
