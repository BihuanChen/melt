package mlt.test;

import java.util.HashSet;
import java.util.Random;

import mlt.Config;
import mlt.learn.PathLearner;

public class TestGenerator {

	private PathLearner pathLearner;

	public TestGenerator(PathLearner pathLearner) {
		this.pathLearner = pathLearner;
	}
	
	public HashSet<Object[]> generate() throws Exception {
		HashSet<Object[]> tests = new HashSet<Object[]>();
		while (true) {
			Object[] test = generateRandom();
			if (test != null) {
				tests.add(test);
				if (tests.size() == Config.TESTS_SIZE) {
					return tests;
				}
			}
		}
	}
	
	private Object[] generateRandom() throws Exception {
		// randomly generate a test case
		int size = Config.CLS.length;
		Object[] test = new Object[size];
		for (int i = 0; i < size; i++) {
			if (Config.CLS[i] == byte.class) {
				test[i] = (byte) new Random().nextInt(1 << 8);
			} else if (Config.CLS[i] == short.class) {
				test[i] = (short) new Random().nextInt(1 << 16);
			} else if (Config.CLS[i] == int.class) {
				test[i] = new Random().nextInt();
			} else if (Config.CLS[i] == long.class) {
				test[i] = new Random().nextLong();
			} else if (Config.CLS[i] == float.class) {
				test[i] = new Random().nextBoolean() ? new Random().nextFloat() * Float.MAX_VALUE : new Random().nextFloat() * -Float.MAX_VALUE;
			} else if (Config.CLS[i] == double.class) {
				test[i] = new Random().nextBoolean() ? new Random().nextDouble() * Double.MAX_VALUE : new Random().nextDouble() * -Double.MAX_VALUE;
			} else if (Config.CLS[i] == boolean.class) {
				test[i] = new Random().nextBoolean();
			}
		}
		// check if the test case is valid
		if (pathLearner == null || pathLearner.isValidTest(test)) {
			return test;
		} else {
			return null;
		}
	}

}
