package mlt.test.ea;

import java.util.HashMap;

import jmetal.core.Solution;
import jmetal.operators.crossover.Crossover;
import jmetal.util.JMException;

public class GuidedCrossover extends Crossover {

	private static final long serialVersionUID = -8399740883333248425L;

	private Double crossoverProbability_ = null;
			
	public GuidedCrossover(HashMap<String, Object> parameters) {
		super(parameters);
		
		if (parameters.get("probability") != null) {
	  		crossoverProbability_ = (Double) parameters.get("probability");
		}
	}

	@Override
	public Object execute(Object object) throws JMException {
		Solution[] parents = (Solution [])object;
		Solution[] offspring = new Solution[2];
		offspring[0] = new Solution(parents[0]);
		offspring[1] = new Solution(parents[1]);
		
		return offspring;
	}

}
