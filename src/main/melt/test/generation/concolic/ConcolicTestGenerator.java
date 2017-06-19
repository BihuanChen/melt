package melt.test.generation.concolic;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.vm.Instruction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import melt.Config;
import melt.core.Predicate;
import melt.core.PredicateNode;
import melt.core.Profile;
import melt.learn.PathLearner;
import melt.test.generation.TestGenerator;
import melt.test.util.TestCase;
import melt.test.util.Util;

public class ConcolicTestGenerator extends TestGenerator {

	public ConcolicTestGenerator(PathLearner pathLearner) {
		super(pathLearner);
	}

	// only used to hit corner cases
	@Override
	public HashSet<TestCase> generate() throws Exception {
		return genConcolicTests();
	}
	
	private HashSet<TestCase> genConcolicTests() throws Exception {
		PredicateNode target = pathLearner.getTarget();
		// get the test for concolic execution
		Object[] test = null;
		int testIndex = -1;
		if (target.getSourceTrueBranch() != null) {
			testIndex = target.getSourceTrueBranch().getTriggerTests().get(0);
			test = Profile.tests.get(testIndex).getTest();
		} else if (target.getSourceFalseBranch() != null) {
			testIndex = target.getSourceFalseBranch().getTriggerTests().get(0);
			test = Profile.tests.get(testIndex).getTest();
		} else {
			System.err.println("[melt] error in choosing the test for concolic execution");
			System.exit(0);
		}
		System.out.print("[melt] given test for concolic execution is [" + test[0]);
		for (int i = 1; i < test.length; i++) {
			System.out.print(", " + test[i]);
		}
		System.out.println("]");
		// get the source information for the target branch
		Predicate p = Profile.predicates.get(target.getPredicate());
		String className = p.getClassName();
		String srcLoc = className + "." + p.getMethodName() + "(" + className.substring(className.lastIndexOf(".") + 1) + ".java:" + p.getLineNumber() + ")";
		System.out.println("[melt] target branch for concolic execution is " + srcLoc + ", " + p.getExpression());
		// run concolic execution to get tests and branch constraints
		ConcolicExecution jdart = ConcolicExecution.getInstance(Config.JPFCONFIG);
		jdart.run(test);
		HashMap<Instruction, Expression<Boolean>> cons = new HashMap<Instruction, Expression<Boolean>>();
		//String solver = jdart.getJpfConf().getString("symbolic.dp");
		//HashSet<Valuation> vals = jdart.getValuations(srcLoc, solver.equals("z3") ? 1 : Config.TESTS_SIZE, cons);
		HashSet<Valuation> vals = jdart.getValuations(srcLoc, 1, cons, pathLearner.getRoot() == null);
		System.out.println("[melt] tests generated from concolic execution " + vals + "\n");
		// attach constraints to corresponding nodes
		if (pathLearner.getRoot() != null) { // except for simple hybrid testing
			pathLearner.attachConstraints(testIndex, cons);
		}
		// convert valuations to tests
		HashSet<TestCase> testCases = new HashSet<TestCase>();
		Iterator<Valuation> iterator = vals.iterator();
		while (iterator.hasNext()) {
			Valuation v = iterator.next();
			testCases.add(new TestCase(Util.valuationToTest(v), v));
		}
		return testCases;
	}

}
