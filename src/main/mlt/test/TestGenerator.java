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
				test[i] = (byte) new Random().nextInt(Config.MAX_BYTE - Config.MIN_BYTE + 1) + Config.MIN_BYTE;
			} else if (Config.CLS[i] == short.class) {
				test[i] = (short) new Random().nextInt(Config.MAX_SHORT - Config.MIN_SHORT + 1) + Config.MIN_SHORT;
			} else if (Config.CLS[i] == int.class) {
				test[i] = new Random().nextInt(Config.MAX_INT - Config.MIN_INT + 1) + Config.MIN_INT;
			} else if (Config.CLS[i] == long.class) {
				test[i] = (long) new Random().nextDouble() * (Config.MAX_LONG - Config.MIN_LONG + 1) + Config.MIN_LONG;
			} else if (Config.CLS[i] == float.class) {
				test[i] = new Random().nextFloat() * (Config.MAX_FLOAT - Config.MIN_FLOAT) + Config.MIN_FLOAT;
			} else if (Config.CLS[i] == double.class) {
				test[i] = new Random().nextDouble() * (Config.MAX_DOUBLE - Config.MIN_DOUBLE) + Config.MIN_DOUBLE;
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
