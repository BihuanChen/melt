package mlt.test.ea;

import java.util.HashMap;
import java.util.HashSet;

import jmetal.core.Solution;
import jmetal.operators.crossover.Crossover;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

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
		
		if (PseudoRandom.randDouble() < crossoverProbability_) {
			int crossoverPoint = PseudoRandom.randInt(0, parents[0].numberOfVariables() - 1);
			for (int i = crossoverPoint; i < parents[0].numberOfVariables(); i++) {
				Object[] p0 = ((TestVar)parents[0].getDecisionVariables()[i]).getTest().getTest();
				Object[] p1 = ((TestVar)parents[1].getDecisionVariables()[i]).getTest().getTest();
				
				Object[] c0 = ((TestVar)offspring[0].getDecisionVariables()[i]).getTest().getTest();
				Object[] c1 = ((TestVar)offspring[1].getDecisionVariables()[i]).getTest().getTest();
				
				HashSet<Integer> depInputs0 = ((TestVar)parents[0].getDecisionVariables()[i]).computeDepInputs();
				HashSet<Integer> depInputs1 = ((TestVar)parents[1].getDecisionVariables()[i]).computeDepInputs();
				
				// TODO not very effective for small-length test cases? 
				boolean flag0 = false;
				boolean flag1 = false;
				for (int j = 0; j < c0.length; j++) {
					if (depInputs0.contains(j)) {
						flag0 = true;
						c0[j] = p1[j];
					} 
					if (depInputs1.contains(j)) {
						flag1 = true;
						c1[j] = p0[j];
					}
				}
				
				if (flag0) {
					((TestVar)offspring[0].getDecisionVariables()[i]).clear();
				}
				if (flag1) {
					((TestVar)offspring[1].getDecisionVariables()[i]).clear();
				}
			}
		}
		
		return offspring;
	}
	
}
