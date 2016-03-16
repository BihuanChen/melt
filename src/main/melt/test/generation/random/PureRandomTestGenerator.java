package melt.test.generation.random;

import java.util.HashSet;

import melt.Config;
import melt.learn.PathLearner;
import melt.test.TestCase;
import melt.test.generation.TestGenerator;
import melt.test.generation.concolic.ConcolicTestGenerator;

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
			TestCase testCase = new TestCase(melt.test.Util.randomTest());
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
