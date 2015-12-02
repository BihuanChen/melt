package mlt.test.ea;

import jmetal.core.Problem;
import jmetal.core.SolutionType;
import jmetal.core.Variable;

public class TestVarSolutionType extends SolutionType {

	public TestVarSolutionType(Problem problem) {
		super(problem);
	}

	@Override
	public Variable[] createVariables() throws ClassNotFoundException {
		
		return null;
	}

}
