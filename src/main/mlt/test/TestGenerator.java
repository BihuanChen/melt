package mlt.test;

import java.util.ArrayList;
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
	
	public HashSet<Object[]> generate() throws Exception {
		return randomTest();
		//return adaptiveTest();
	}
	
	public HashSet<Object[]> randomTest() throws Exception {
		HashSet<Object[]> tests = new HashSet<Object[]>(Config.TESTS_SIZE);
		while (true) {
			Object[] test = test();
			// check if the test is valid
			if (pathLearner == null || pathLearner.isValidTest(test)) {
				tests.add(test);
				if (tests.size() == Config.TESTS_SIZE) {
					return tests;
				}
			}
		}
	}
	
	public HashSet<Object[]> adaptiveTest() throws Exception {
		int k = 10;
		HashSet<Object[]> tests = new HashSet<Object[]>(Config.TESTS_SIZE);
		while (true) {
			// generate the k candidate tests
			ArrayList<Object[]> candidates = new ArrayList<Object[]>(k);
			double[] minDist = new double[k];
			for (int i = 0; i < k; i++) {
				candidates.add(test());
				minDist[i] = Double.MAX_VALUE;
			}
			// compute the minimum distances
			int size = Profiles.tests.size();
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < k; j++) {
					double dist = this.distance(candidates.get(j), Profiles.tests.get(i));
					if (dist < minDist[j]) {
						minDist[j] = dist;
					}
				}
			}
			Iterator<Object[]> iterator = tests.iterator();
			while (iterator.hasNext()) {
				Object[] t = iterator.next();
				for (int j = 0; j < k; j++) {
					double dist = this.distance(candidates.get(j), t);
					if (dist < minDist[j]) {
						minDist[j] = dist;
					}
				}
			}
			// find the candidate with the maximum distance
			int index = 0;
			for (int i = 1; i < k; i++) {
				if (minDist[i] > minDist[index]) {
					index = i;
				}
			}
			// check if the test is valid
			Object[] test = candidates.get(index);
			if (pathLearner == null || pathLearner.isValidTest(test)) {
				tests.add(test);
				if (tests.size() == Config.TESTS_SIZE) {
					return tests;
				}
			}			
		}
	}
	
	// randomly generate a test
	private Object[] test() {
		int size = Config.CLS.length;
		Object[] test = new Object[size];
		for (int i = 0; i < size; i++) {
			if (Config.CLS[i] == byte.class) {
				test[i] = (byte) (new Random().nextInt(Config.MAX_BYTE - Config.MIN_BYTE + 1) + Config.MIN_BYTE);
			} else if (Config.CLS[i] == short.class) {
				test[i] = (short) (new Random().nextInt(Config.MAX_SHORT - Config.MIN_SHORT + 1) + Config.MIN_SHORT);
			} else if (Config.CLS[i] == int.class) {
				test[i] = new Random().nextInt(Config.MAX_INT - Config.MIN_INT + 1) + Config.MIN_INT;
			} else if (Config.CLS[i] == long.class) {
				test[i] = (long) (new Random().nextDouble() * (Config.MAX_LONG - Config.MIN_LONG + 1) + Config.MIN_LONG);
			} else if (Config.CLS[i] == float.class) {
				test[i] = new Random().nextFloat() * (Config.MAX_FLOAT - Config.MIN_FLOAT) + Config.MIN_FLOAT;
			} else if (Config.CLS[i] == double.class) {
				test[i] = new Random().nextDouble() * (Config.MAX_DOUBLE - Config.MIN_DOUBLE) + Config.MIN_DOUBLE;
			} else if (Config.CLS[i] == boolean.class) {
				test[i] = new Random().nextBoolean();
			}
		}
		return test;
	}
	
	// compute the distance of two tests
	private double distance(Object[] t1, Object[] t2) {
		int size = Config.CLS.length;
		double dist = 0;
		for (int i = 0; i < size; i++) {
			if (Config.CLS[i] == byte.class) {
				dist += ((byte)t1[i] - (byte)t2[i]) * ((byte)t1[i] - (byte)t2[i]);
			} else if (Config.CLS[i] == short.class) {
				dist += ((short)t1[i] - (short)t2[i]) * ((short)t1[i] - (short)t2[i]);
			} else if (Config.CLS[i] == int.class) {
				dist += ((int)t1[i] - (int)t2[i]) * ((int)t1[i] - (int)t2[i]);				
			} else if (Config.CLS[i] == long.class) {
				dist += ((long)t1[i] - (long)t2[i]) * ((long)t1[i] - (long)t2[i]);				
			} else if (Config.CLS[i] == float.class) {
				dist += ((float)t1[i] - (float)t2[i]) * ((float)t1[i] - (float)t2[i]);
			} else if (Config.CLS[i] == double.class) {
				dist += ((double)t1[i] - (double)t2[i]) * ((double)t1[i] - (double)t2[i]);				
			} else if (Config.CLS[i] == boolean.class) {
				if ((boolean)t1[i] ^ (boolean)t2[i]) {
					dist += 1.0;
				} else {
					dist += 0.0;
				}
			}
		}
		return Math.sqrt(dist);
	}

}
