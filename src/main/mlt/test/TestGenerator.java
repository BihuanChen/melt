package mlt.test;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.vm.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import mlt.Config;
import mlt.concolic.ConcolicExecution;
import mlt.instrument.Predicate;
import mlt.learn.PathLearner;
import mlt.learn.PredicateNode;

public class TestGenerator {

	private PathLearner pathLearner;

	public TestGenerator(PathLearner pathLearner) {
		this.pathLearner = pathLearner;
	}
	
	public HashSet<Object[]> generate() throws Exception {
		if (pathLearner != null && pathLearner.getTarget().getAttempts() == Config.MAX_ATTEMPTS) {
			return concolicTest();
		} else {
			return randomTest();
			//return adaptiveTest();
		}
	}
	
	public HashSet<Object[]> concolicTest() throws Exception {
		PredicateNode target = pathLearner.getTarget();
		// get the test for concolic execution
		Object[] test = null;
		int testIndex = -1;
		if (target.getSourceTrueBranch() != null) {
			testIndex = target.getSourceTrueBranch().getTests().get(0);
			test = Profiles.tests.get(testIndex);
		} else if (target.getSourceFalseBranch() != null) {
			testIndex = target.getSourceFalseBranch().getTests().get(0);
			test = Profiles.tests.get(testIndex);
		} else {
			System.err.println("[ml-testing] error in choosing the test for concolic execution");
		}
		System.out.print("[ml-testing] given test for concolic execution is [" + test[0]);
		for (int i = 1; i < test.length; i++) {
			System.out.print(", " + test[i]);
		}
		System.out.println("]");
		// get the source information for the target branch
		Predicate p = Profiles.predicates.get(target.getPredicate());
		String className = p.getClassName();
		String srcLoc = className + "." + p.getMethodName() + "(" + className.substring(className.lastIndexOf(".") + 1) + ".java:" + p.getLineNumber() + ")";
		System.out.println("[ml-testing] target branch for concolic execution is " + srcLoc);
		// run concolic execution to get tests and branch constraints
		ConcolicExecution jdart = ConcolicExecution.getInstance(Config.JPFCONFIG);
		jdart.run(test);
		HashMap<Instruction, Expression<Boolean>> cons = new HashMap<Instruction, Expression<Boolean>>();
		ArrayList<Valuation> vals = jdart.getValuations(srcLoc, Config.TESTS_SIZE, cons);
		System.out.println("[ml-testing] tests generated from concolic execution " + vals + "\n");
		// attach constraints to corresponding nodes
		pathLearner.attachConstraints(testIndex, cons);
		// convert valuations to tests
		HashSet<Object[]> tests = new HashSet<Object[]>();
		for (int i = 0; i < vals.size(); i++) {
			tests.add(Util.valuationToTest(vals.get(i)));
		}
		return tests;
	}
	
	// TODO stuck when the constraints are too narrow
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
			// generate the k valid candidate tests
			ArrayList<Object[]> candidates = new ArrayList<Object[]>(k);
			double[] minDist = new double[k];
			for (int i = 0; i < k; ) {
				Object[] t = test();
				if (pathLearner == null || pathLearner.isValidTest(t)) { 
					candidates.add(test());
					minDist[i] = Double.MAX_VALUE;
					i++;
				}
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
			// add to the set of tests
			tests.add(candidates.get(index));
			if (tests.size() == Config.TESTS_SIZE) {
				return tests;
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
