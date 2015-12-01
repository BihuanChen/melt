package mlt.test.ea;

import java.util.HashMap;

import jmetal.operators.mutation.Mutation;
import jmetal.util.JMException;

public class GuidedMutation extends Mutation {

	private static final long serialVersionUID = -8186478343375466110L;

	public GuidedMutation(HashMap<String, Object> parameters) {
		super(parameters);
	}

	@Override
	public Object execute(Object object) throws JMException {
		return null;
	}

}
