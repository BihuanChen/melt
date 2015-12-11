package mlt.test.generation.random;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import mlt.Config;
import mlt.learn.PathLearner;
import mlt.test.Profiles;
import mlt.test.TestCase;
import mlt.test.Util;
import mlt.test.generation.TestGenerator;
import mlt.test.generation.concolic.ConcolicTestGenerator;

public class AdaptiveRandomTestGenerator extends TestGenerator {

	private int k = 10;
	
	public AdaptiveRandomTestGenerator(PathLearner pathLearner) {
		super(pathLearner);
	}

	@Override
	public HashSet<TestCase> generate() throws Exception {
		if (pathLearner != null && pathLearner.getTarget().getAttempts() == Config.MAX_ATTEMPTS) {
			return new ConcolicTestGenerator(pathLearner).generate();
		} else {
			return genAdaptiveRandomTests();
		}
	}

	// might stuck when the constraints are too narrow
	private HashSet<TestCase> genAdaptiveRandomTests() throws Exception {
		HashSet<TestCase> testCases = new HashSet<TestCase>(Config.TESTS_SIZE);
		while (true) {
			// generate the k valid candidate tests
			ArrayList<TestCase> candidates = new ArrayList<TestCase>(k);
			double[] minDist = new double[k];
			for (int i = 0; i < k; ) {
				TestCase testCase = new TestCase(Util.randomTest());
				if (pathLearner == null || pathLearner.isValidTest(testCase)) { 
					candidates.add(testCase);
					minDist[i] = Double.MAX_VALUE;
					i++;
				}
			}
			// compute the minimum distances
			int size = Profiles.tests.size();
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < k; j++) {
					double dist = this.distance(candidates.get(j).getTest(), Profiles.tests.get(i).getTest());
					if (dist < minDist[j]) {
						minDist[j] = dist;
					}
				}
			}
			Iterator<TestCase> iterator = testCases.iterator();
			while (iterator.hasNext()) {
				TestCase t = iterator.next();
				for (int j = 0; j < k; j++) {
					double dist = this.distance(candidates.get(j).getTest(), t.getTest());
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
			// add to the set of tests
			testCases.add(candidates.get(index));
			if (testCases.size() == Config.TESTS_SIZE) {
				return testCases;
			}			
		}
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
