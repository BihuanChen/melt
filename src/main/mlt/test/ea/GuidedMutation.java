package mlt.test.ea;

import java.util.HashMap;
import java.util.HashSet;

import mlt.Config;
import jmetal.core.Solution;
import jmetal.operators.mutation.Mutation;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

public class GuidedMutation extends Mutation {

	private static final long serialVersionUID = -8186478343375466110L;

	private Double mutationProbability_h = null;
	private Double mutationProbability_l = null;
	
	public GuidedMutation(HashMap<String, Object> parameters) {
		super(parameters);
		
		if (parameters.get("probability_h") != null) {
	  		mutationProbability_h = (Double) parameters.get("probability_h");
		}
		if (parameters.get("probability_l") != null) {
	  		mutationProbability_l = (Double) parameters.get("probability_l");
		}
	}

	@Override
	public Object execute(Object object) throws JMException {
		Solution solution = (Solution)object;		
		for (int i = 0; i < solution.numberOfVariables(); i++) {
			TestVar tv = (TestVar)solution.getDecisionVariables()[i];
			if (tv.getObjValues().size() == 0) {
				Object[] test = tv.getTest().getTest();
				for (int j = 0; j < test.length; j++) {
					if (PseudoRandom.randDouble() < mutationProbability_l) {
						doMutation(test, j);
					}
				}
			} else if (!tv.isSatisfiedForOne()) {
				Object[] test = tv.getTest().getTest();
				HashSet<Integer> depInputs = tv.computeDepInputs();
				for (int j = 0; j < test.length; j++) {
					if (depInputs.contains(j)) {
						if (PseudoRandom.randDouble() < mutationProbability_h) {
							doMutation(test, j);
						}
					} else {
						if (PseudoRandom.randDouble() < mutationProbability_l) {
							doMutation(test, j);
						}
					}
				}
			}
			tv.clear();
		}
		return solution;
	}
	
	private void doMutation(Object[] test, int index) {
		@SuppressWarnings("rawtypes")
		Class cls = test[index].getClass();
		if (cls == byte.class) {
			test[index] = (byte)PseudoRandom.randInt(Config.MIN_BYTE, Config.MAX_BYTE);
		} else if (cls == short.class) {
			test[index] = (short)PseudoRandom.randInt(Config.MIN_SHORT, Config.MAX_SHORT);
		} else if (cls == int.class) {
			test[index] = PseudoRandom.randInt(Config.MIN_INT, Config.MAX_INT);
		} else if (cls == long.class) {
			test[index] = (long)PseudoRandom.randDouble(Config.MIN_LONG, Config.MAX_LONG);
		} else if (cls == float.class) {
			test[index] = (float)PseudoRandom.randDouble(Config.MIN_FLOAT, Config.MAX_FLOAT);
		} else if (cls == double.class) {
			test[index] = PseudoRandom.randDouble(Config.MIN_DOUBLE, Config.MAX_DOUBLE);
		} else if (cls == boolean.class) {
			test[index] = PseudoRandom.randInt(0, 1) == 0 ? false : true;
		}
	}

}
