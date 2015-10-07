package mlt.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import mlt.Config;
import mlt.learn.PathLearner;

public class TestGenerator {

	private PathLearner pathLearner;

	public TestGenerator(PathLearner pathLearner) {
		this.pathLearner = pathLearner;
	}
	
	public HashSet<Object[]> generate(@SuppressWarnings("rawtypes") Class[] cls) throws Exception {
		HashSet<Object[]> tests = new HashSet<Object[]>();
		while (true) {
			// randomly generate a test case
			int size = cls.length;
			Object[] test = new Object[size];
			for (int i = 0; i < size; i++) {
				if (cls[i] == byte.class) {
					test[i] = (byte) new Random().nextInt(1 << 8);
				} else if (cls[i] == short.class) {
					test[i] = (short) new Random().nextInt(1 << 16);
				} else if (cls[i] == int.class) {
					test[i] = new Random().nextInt();
				} else if (cls[i] == long.class) {
					test[i] = new Random().nextLong();
				} else if (cls[i] == float.class) {
					test[i] = new Random().nextFloat() * Float.MAX_VALUE * 2.0f - Float.MAX_VALUE;
				} else if (cls[i] == double.class) {
					test[i] = new Random().nextDouble() * Double.MAX_VALUE * 2.0 - Double.MAX_VALUE;
				} else if (cls[i] == boolean.class) {
					test[i] = new Random().nextBoolean();
				}
			}
			// check if the test case is valid
			if (pathLearner == null || pathLearner.isValidTest(test)) {
				tests.add(test);
				if (tests.size() == Config.TESTS_SIZE) {
					return tests;
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		HashSet<Object[]> tests = new TestGenerator(null).generate(new Class[]{byte.class, short.class, int.class, long.class, boolean.class, float.class, double.class});
		Iterator<Object[]> i = tests.iterator();
		while (i.hasNext()) {
			Object[] obj = i.next();
			for(int j = 0; j < obj.length; j++) {
				System.out.print(obj[j] + " ");
			}
			System.out.println();
		}
	}

}
