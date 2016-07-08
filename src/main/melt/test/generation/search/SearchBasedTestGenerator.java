package melt.test.generation.search;

import java.util.HashMap;
import java.util.HashSet;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.ibea.IBEA;
import jmetal.operators.selection.BinaryTournament;
import jmetal.util.comparators.DominanceComparator;
import melt.Config;
import melt.learn.PathLearner;
import melt.test.TestCase;
import melt.test.generation.TestGenerator;
import melt.test.generation.concolic.ConcolicTestGenerator;
import melt.test.generation.random.PureRandomTestGenerator;

public class SearchBasedTestGenerator extends TestGenerator {

	public static long ceTime = 0;
	
	public SearchBasedTestGenerator(PathLearner pathLearner) {
		super(pathLearner);
	}

	@Override
	public HashSet<TestCase> generate() throws Exception {
		if (pathLearner != null && pathLearner.getTarget().getAttempts() == Config.MAX_ATTEMPTS) {
			long t = System.currentTimeMillis();
			HashSet<TestCase> tcs = new ConcolicTestGenerator(pathLearner).generate();
			ceTime += System.currentTimeMillis() - t;
			if (tcs.size() == 0) {
				return genSearchBasedTests();
			}
			return tcs;
		} else if (pathLearner == null || pathLearner.getTraces() == null) {
			return new PureRandomTestGenerator(pathLearner).generate();
		} else {
			return genSearchBasedTests();
		}		
	}

	private HashSet<TestCase> genSearchBasedTests() throws Exception {
		Problem problem = new TestSuiteGenProblem(pathLearner);
		
		// Algorithm to solve the problem
	    Algorithm algorithm = new IBEA(problem);
	    algorithm.setInputParameter("populationSize", 10);
	    algorithm.setInputParameter("archiveSize", 10);
	    algorithm.setInputParameter("maxEvaluations", 1000);

	    // Operator parameters
	    HashMap<String, Object>  parameters; 

	    // Crossover operator 
	    parameters = new HashMap<String, Object>();
	    parameters.put("probability", 0.9);
	    Operator crossover = new GuidedCrossover(parameters);                   

	    // Mutation operator
	    parameters = new HashMap<String, Object>();
	    parameters.put("probability_h", 0.9);
	    parameters.put("probability_l", 0.1);
	    Operator mutation = new GuidedMutation(parameters);

	    // Selection Operator
	    parameters = new HashMap<String, Object>(); 
	    parameters.put("comparator", new DominanceComparator());
	    Operator selection = new BinaryTournament(parameters);
	    
	    // Add the operators to the algorithm
	    algorithm.addOperator("crossover",crossover);
	    algorithm.addOperator("mutation",mutation);
	    algorithm.addOperator("selection",selection);

	    // Execute the Algorithm
	    SolutionSet population = algorithm.execute();

	    // Get the results
	    HashSet<TestCase> tests = new HashSet<TestCase>();
	    
	    // add all the tests from the population
	    for (int i = 0; i < population.size(); i++) {
	    	Solution solution = population.get(i);
	    	for (int j = 0; j < solution.numberOfVariables(); j++) {
	    		TestVar tv = (TestVar)solution.getDecisionVariables()[j];
	    		tests.add(tv.getTest());
	    	}
	    }
	    
	    /*// selectively choose the tests from the population
	    HashMap<Integer, HashSet<TestCase>> satisfiedTests = new HashMap<Integer, HashSet<TestCase>>(problem.getNumberOfObjectives());
	    for (int i = 0; i < population.size(); i++) {
	    	Solution solution = population.get(i);
	    	for (int j = 0; j < solution.numberOfVariables(); j++) {
	    		TestVar tv = (TestVar)solution.getDecisionVariables()[j];
	    		HashSet<Integer> indexs = tv.getBestObjIndex();
	    		//tests.add(tv.getTest());
	    		if (indexs != null) {
	    			//tests.add(tv.getTest());
	    			Iterator<Integer> iterator = indexs.iterator();
	    			while (iterator.hasNext()) {
	    				int idx = iterator.next();
	    				if (tv.getViolations().get(idx) == null) {
	    					if (satisfiedTests.get(idx) == null) {
	    						satisfiedTests.put(idx, new HashSet<TestCase>());
	    					}
	    					satisfiedTests.get(idx).add(tv.getTest());
	    				}
	    			}
	    		}
	    	}
	    }
	    if (satisfiedTests.size() != 0) {
		    Iterator<Integer> iterator = satisfiedTests.keySet().iterator();
		    while (iterator.hasNext()) {
		    	HashSet<TestCase> tt = satisfiedTests.get(iterator.next());
		    	if (tt != null) {
		    		Iterator<TestCase> ii = tt.iterator();
		    		while (ii.hasNext()) {
		    			tests.add(ii.next());
		    		}
		    	}
		    }
	    }*/
	    
		return tests;
	}
	
}
