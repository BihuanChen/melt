package melt.test.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import melt.instrument.Predicate;
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
			testChronicle.write(test);
			testChronicle.read();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		melt.Config.loadProperties("/home/bhchen/workspace/testing/benchmark1-art/src/dt/original/Median.melt");
		
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("/home/bhchen/workspace/testing/melt/dt.original.Median.pred")));
		Profiles.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		Profiles.printPredicates();
		
		TestRunnerClient client = new TestRunnerClient(true);
		Object[] test1 = new Object[]{44961304, -94974295, -83362496, -52795572, -4822247};
		client.run(test1);
		//Profiles.printTaints();
		//Profiles.taints.clear();
		
		//Object[] test2 = new Object[]{-26898996, -2293003, 3.251309129702559E7};
		//client.run(test2);
		//Profiles.printTaints();
		//Profiles.taints.clear();
	}
	
}
