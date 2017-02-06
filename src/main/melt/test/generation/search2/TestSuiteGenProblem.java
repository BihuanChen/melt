package melt.test.generation.search2;

import java.util.ArrayList;

import melt.Config;
import melt.core.PredicateNode;
import melt.core.Profile;
import melt.learn.PathLearner;
import melt.test.util.TestCase;
import melt.test.util.Util;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

public class TestSuiteGenProblem extends Problem {

	private static final long serialVersionUID = 5419978931848602516L;

	private PathLearner learner;

	public TestSuiteGenProblem(PathLearner pathLearner) {
		learner = pathLearner;
		numberOfVariables_ = 1;
		numberOfObjectives_ = 2;
		numberOfConstraints_ = 0;
		problemName_ = "single-path test suite generation";
		solutionType_ = new TestVarSolutionType(this);
	}

	@Override
	public void evaluate(Solution solution) throws JMException {
		TestVar tv = (TestVar)solution.getDecisionVariables()[0];
		try {
			learner.evaluateTest2(tv);
		} catch (Exception e) {
			e.printStackTrace();
		}
		solution.setObjective(0, tv.getObjValue());
		solution.setObjective(1, tv.getViolations() == null ? 0 : tv.getViolations().size());
	}

	/*@Override
	public void evaluateConstraints(Solution solution) throws JMException {
		int totalNumOfViolations = 0;
		TestVar tv = (TestVar)solution.getDecisionVariables()[0];
		totalNumOfViolations += tv.getViolations() == null ? 0 : tv.getViolations().size();
		solution.setOverallConstraintViolation(-totalNumOfViolations);
		solution.setNumberOfViolatedConstraint(totalNumOfViolations);
	}*/

	private ArrayList<TestCase> initialTests = null;
	
	// TODO more techniques to generate initial tests?
	public Variable[] getInitialSolution() {
		if (initialTests == null) {
			initialTests = new ArrayList<TestCase>();
			PredicateNode target = learner.getTarget();
			if (target.getSourceTrueBranch() != null) {
				int size = target.getSourceTrueBranch().getTriggerTests().size();
				for (int i = 0; i < size; i++) {
					initialTests.add(Profile.tests.get(target.getSourceTrueBranch().getTriggerTests().get(i)));
				}
			} else if (target.getSourceFalseBranch() != null) {
				int size = target.getSourceFalseBranch().getTriggerTests().size();
				for (int i = 0; i < size; i++) {
					initialTests.add(Profile.tests.get(target.getSourceFalseBranch().getTriggerTests().get(i)));
				}
			} else {
				System.err.println("[melt] error in create the initial population in ea-based test generation");
				System.exit(0);
			}
			// add randomly generated test cases
			for (int i = initialTests.size(); i < 10 * Config.TESTS_SIZE; i++) {
				initialTests.add(new TestCase(Util.randomTest()));
			}
			// print the initial tests
			//System.err.println("debugging " + initialTests.size() + " initial tests");
			//for (int i = 0; i < initialTests.size(); i++) {
			//	System.err.println("debugging " + initialTests.get(i));
			//}
		}
		Variable[] variables = new Variable[numberOfVariables_];
		int size = initialTests.size();
		for (int i = 0; i < numberOfVariables_; i++) {
			int rdm = PseudoRandom.randInt(0, size - 1);
			variables[i] = new TestVar(initialTests.get(rdm).deepCopy());
		}
		//System.err.println("Initial " + ((TestVar)variables[0]).getTest());
		return variables;
	}

}
