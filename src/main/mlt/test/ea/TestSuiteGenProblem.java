package mlt.test.ea;

import java.util.HashSet;

import mlt.learn.PathLearner;
import mlt.learn.PredicateNode;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.util.JMException;

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
		HashSet<Integer> used = new HashSet<Integer>();
		for (int i = 0; i < numberOfObjectives_; i++) {
			int minNumOfViolations = Integer.MAX_VALUE;	
			int index = 0;
			double objValue = 0;
			for (int j = 0; j < numberOfVariables_; j++) {
				if (!used.contains(j)) {
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
			}
			used.add(index);
			((TestVar)suite[index]).setBestIndex(i);
			solution.setObjective(i, objValue);
		}
	}

	@Override
	public void evaluateConstraints(Solution solution) throws JMException {
		int totalNumOfViolations = 0;
		Variable[] suite = solution.getDecisionVariables();
		for (int i = 0; i < numberOfVariables_; i++) {
			TestVar tv = (TestVar)suite[i];
			HashSet<PredicateNode> violations = tv.getViolations().get(tv.getBestIndex());
			totalNumOfViolations += violations == null ? 0 : violations.size();
		}
		solution.setOverallConstraintViolation(-totalNumOfViolations);
		solution.setNumberOfViolatedConstraint(totalNumOfViolations);
	}

}
