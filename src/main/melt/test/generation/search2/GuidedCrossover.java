package melt.test.generation.search2;

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
		//System.err.println("crossover");
		Solution[] parents = (Solution [])object;
		//printSolution(parents[0]);
		//printSolution(parents[1]);
		Solution[] offspring = new Solution[2];
		offspring[0] = new Solution(parents[0]);
		offspring[1] = new Solution(parents[1]);
		
		Object[] p0 = ((TestVar)parents[0].getDecisionVariables()[0]).getTest().getTest();
		Object[] p1 = ((TestVar)parents[1].getDecisionVariables()[0]).getTest().getTest();
		
		Object[] c0 = ((TestVar)offspring[0].getDecisionVariables()[0]).getTest().getTest();
		Object[] c1 = ((TestVar)offspring[1].getDecisionVariables()[0]).getTest().getTest();
				
		HashSet<Integer> depInputs0 = ((TestVar)parents[0].getDecisionVariables()[0]).computeDepInputs();
		HashSet<Integer> depInputs1 = ((TestVar)parents[1].getDecisionVariables()[0]).computeDepInputs();
				
		//System.err.println(depInputs0);
		//System.err.println(depInputs1);
				
		boolean flag0 = false;
		boolean flag1 = false;
		for (int j = 0; j < c0.length; j++) {
			if (depInputs0.contains(j)) {
				if (PseudoRandom.randDouble() < crossoverProbability_) {
					flag0 = true;
					//System.err.println("crossed at " + j + " for c0");
					c0[j] = p1[j];
				}
			} 
			if (depInputs1.contains(j)) {
				if (PseudoRandom.randDouble() < crossoverProbability_) {
					flag1 = true;
					//System.err.println("crossed at " + j + " for c1");
					c1[j] = p0[j];
				}
			}
				
			if (flag0) {
				((TestVar)offspring[0].getDecisionVariables()[0]).clear();
			}
			if (flag1) {
				((TestVar)offspring[1].getDecisionVariables()[0]).clear();
			}
		}
		//printSolution(offspring[0]);
		//printSolution(offspring[1]);
		return offspring;
	}
	
	public void printSolution(Solution sol) {
		for (int i = 0; i < sol.numberOfVariables(); i++) {
			System.err.println(((TestVar)sol.getDecisionVariables()[i]).getTest());
		}
		System.err.println();
	}
	
}
