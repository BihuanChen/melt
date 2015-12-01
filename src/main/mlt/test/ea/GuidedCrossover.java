package mlt.test.ea;

import java.util.HashMap;

import jmetal.operators.crossover.Crossover;
import jmetal.util.JMException;

public class GuidedCrossover extends Crossover {

	private static final long serialVersionUID = -8399740883333248425L;

	public GuidedCrossover(HashMap<String, Object> parameters) {
		super(parameters);
	}

	@Override
	public Object execute(Object object) throws JMException {
		return null;
	}

}
