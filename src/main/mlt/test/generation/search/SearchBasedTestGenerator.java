package mlt.test.generation.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.ibea.IBEA;
import jmetal.operators.selection.BinaryTournament;
import jmetal.util.comparators.DominanceComparator;
import mlt.Config;
import mlt.learn.PathLearner;
import mlt.test.TestCase;
import mlt.test.generation.TestGenerator;
import mlt.test.generation.concolic.ConcolicTestGenerator;
import mlt.test.generation.random.PureRandomTestGenerator;

public class SearchBasedTestGenerator extends TestGenerator {

	public SearchBasedTestGenerator(PathLearner pathLearner) {
		super(pathLearner);
	}

	@Override
	public HashSet<TestCase> generate() throws Exception {
		if (pathLearner != null && pathLearner.getTarget().getAttempts() == Config.MAX_ATTEMPTS) {
			HashSet<TestCase> tcs = new ConcolicTestGenerator(pathLearner).generate();
			if (tcs.size() == 0) {
				return genSearchBasedTests();
			}
			return tcs;
		} else if (pathLearner == null) {
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
	    HashMap<Integer, HashSet<TestCase>> satisfiedTests = new HashMap<Integer, HashSet<TestCase>>(problem.getNumberOfObjectives());
	    for (int i = 0; i < population.size(); i++) {
	    	Solution solution = population.get(i);
	    	for (int j = 0; j < solution.numberOfVariables(); j++) {
	    		TestVar tv = (TestVar)solution.getDecisionVariables()[j];
	    		HashSet<Integer> indexs = tv.getBestObjIndex();
	    		if (indexs != null) {
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
	    HashSet<TestCase> tests = new HashSet<TestCase>();
	    if (satisfiedTests.size() == 0) {
	    	return tests;
	    }
	    int n = (Config.TESTS_SIZE / satisfiedTests.size()) + 1;
	    Iterator<Integer> iterator = satisfiedTests.keySet().iterator();
	    while (iterator.hasNext()) {
	    	HashSet<TestCase> tt = satisfiedTests.get(iterator.next());
	    	if (tt != null) {
	    		// TODO use all the generated tests?
	    		// add the first case
	    		//tests.add(tt.iterator().next());
	    		// add equal number of test cases
	    		Iterator<TestCase> ii = tt.iterator();
	    		int count = 0;
	    		while (ii.hasNext() && count < n) {
	    			count++;
	    			tests.add(ii.next());
	    		}
	    	}
	    }
		return tests;
	}
	
}
