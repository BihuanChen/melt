package mlt.concolic;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jdart.JDart;

public class ConcolicExecution {

	public static void main(String[] args) {
		String[] a = {"C:/Users/bhchen/workspace/testing/jdart/src/examples/features/nested/test_bar.jpf"};
		Config config = JPF.createConfig(a);
		config.initClassLoader(JDart.class.getClassLoader());
		JDart dart = new JDart(config);
		dart.run();
	}

}
