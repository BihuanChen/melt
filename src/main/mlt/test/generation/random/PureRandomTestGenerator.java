package mlt.test.generation.random;

import java.util.HashSet;

import mlt.Config;
import mlt.learn.PathLearner;
import mlt.test.TestCase;
import mlt.test.generation.TestGenerator;
import mlt.test.generation.concolic.ConcolicTestGenerator;

public class PureRandomTestGenerator extends TestGenerator {

	public PureRandomTestGenerator(PathLearner pathLearner) {
		super(pathLearner);
	}

	@Override
	public HashSet<TestCase> generate() throws Exception {
		if (pathLearner != null && pathLearner.getTarget().getAttempts() == Config.MAX_ATTEMPTS) {
			return new ConcolicTestGenerator(pathLearner).generate();
		} else {
			return genPureRandomTests();
		}
	}
	
	// might get stuck when the constraints are too narrow
	private HashSet<TestCase> genPureRandomTests() throws Exception {
		HashSet<TestCase> testCases = new HashSet<TestCase>(Config.TESTS_SIZE);
		while (true) {
			TestCase testCase = new TestCase(mlt.test.Util.randomTest());
			// check if the test is valid
			if (pathLearner == null || pathLearner.isValidTest(testCase)) {
				testCases.add(testCase);
				if (testCases.size() == Config.TESTS_SIZE) {
					return testCases;
				}
			}
		}
	}

}
