package melt.test.generation.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import melt.learn.PathLearner;
import melt.learn.PredicateNode;
import melt.test.Profiles;
import melt.test.TestCase;
import melt.test.Util;
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
		if (learner.getTraces() == null) {
			numberOfVariables_ = 1;
			numberOfObjectives_ = 1;
		} else {
			numberOfVariables_ = learner.getTraces().size();
			numberOfObjectives_ = learner.getTraces().size();
		}
		numberOfConstraints_ = 0;
		problemName_ = "multi-path test suite generation";
		solutionType_ = new TestVarSolutionType(this);
	}

	@Override
	public void evaluate(Solution solution) throws JMException {
		Variable[] suite = solution.getDecisionVariables();
		for (int i = 0; i < numberOfVariables_; i++) {
			TestVar tv = (TestVar)suite[i];
			try {
				learner.evaluateTest(tv);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < numberOfObjectives_; i++) {
			int minNumOfViolations = Integer.MAX_VALUE;	
			int index = 0;
			double objValue = 0;
			for (int j = 0; j < numberOfVariables_; j++) {
				TestVar tv = (TestVar)suite[j];
				HashSet<PredicateNode> violations = tv.getViolations().get(i);
				int numOfViolations = violations == null ? 0 : violations.size();
				if (numOfViolations < minNumOfViolations) {
					minNumOfViolations = numOfViolations;
					index = j;
					objValue = tv.getObjValues().get(i);
				} else if (numOfViolations == minNumOfViolations) {
					if (tv.getObjValues().get(i) < objValue) {
						index = j;
						objValue = tv.getObjValues().get(i);
					}
				}
			}
			((TestVar)suite[index]).addBestIndex(i);
			solution.setObjective(i, objValue);
		}
	}

	@Override
	public void evaluateConstraints(Solution solution) throws JMException {
		int totalNumOfViolations = 0;
		Variable[] suite = solution.getDecisionVariables();
		for (int i = 0; i < numberOfVariables_; i++) {
			TestVar tv = (TestVar)suite[i];
			HashSet<Integer> set = tv.getBestObjIndex();
			if (set != null) {
				Iterator<Integer> iterator = set.iterator();
				while (iterator.hasNext()) {
					HashSet<PredicateNode> violations = tv.getViolations().get(iterator.next());
					totalNumOfViolations += violations == null ? 0 : violations.size();
				}
			}
		}
		solution.setOverallConstraintViolation(-totalNumOfViolations);
		solution.setNumberOfViolatedConstraint(totalNumOfViolations);
	}

	private ArrayList<TestCase> initialTests = null;
	private static int initialTestsSize = 100;
	
	// TODO more techniques to generate initial tests?
	public Variable[] getInitialSolution() {
		if (initialTests == null) {
			initialTests = new ArrayList<TestCase>();
			PredicateNode target = learner.getTarget();
			if (target.getSourceTrueBranch() != null) {
				int size = target.getSourceTrueBranch().getTests().size();
				for (int i = 0; i < size; i++) {
					initialTests.add(Profiles.tests.get(target.getSourceTrueBranch().getTests().get(i)));
				}
			} else if (target.getSourceFalseBranch() != null) {
				int size = target.getSourceFalseBranch().getTests().size();
				for (int i = 0; i < size; i++) {
					initialTests.add(Profiles.tests.get(target.getSourceFalseBranch().getTests().get(i)));
				}
			} else {
				System.err.println("[melt] error in create the initial population in ea-based test generation");
			}
			// add randomly generated test cases
			for (int i = initialTests.size(); i < initialTestsSize; i++) {
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
		return variables;
	}

}
