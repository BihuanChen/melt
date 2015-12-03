package mlt.test.ea;

import java.util.HashMap;

import jmetal.core.Solution;
import jmetal.operators.mutation.Mutation;
import jmetal.util.JMException;

public class GuidedMutation extends Mutation {

	private static final long serialVersionUID = -8186478343375466110L;

	private Double mutationProbability_ = null;
	
	public GuidedMutation(HashMap<String, Object> parameters) {
		super(parameters);
		
		if (parameters.get("probability") != null) {
	  		mutationProbability_ = (Double) parameters.get("probability");
		}
	}

	@Override
	public Object execute(Object object) throws JMException {
		Solution solution = (Solution)object;
		
		
		return null;
	}

}
