package mlt.test.ea;

import jmetal.core.Problem;
import jmetal.core.SolutionType;
import jmetal.core.Variable;

public class TestSuiteSolutionType extends SolutionType {

	public TestSuiteSolutionType(Problem problem) {
		super(problem);
	}

	@Override
	public Variable[] createVariables() throws ClassNotFoundException {
		
		return null;
	}

}
