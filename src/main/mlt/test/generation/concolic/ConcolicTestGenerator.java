package mlt.test.generation.concolic;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.vm.Instruction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import mlt.Config;
import mlt.instrument.Predicate;
import mlt.learn.PathLearner;
import mlt.learn.PredicateNode;
import mlt.test.Profiles;
import mlt.test.TestCase;
import mlt.test.Util;
import mlt.test.generation.TestGenerator;

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
			testIndex = target.getSourceTrueBranch().getTests().get(0);
			test = Profiles.tests.get(testIndex).getTest();
		} else if (target.getSourceFalseBranch() != null) {
			testIndex = target.getSourceFalseBranch().getTests().get(0);
			test = Profiles.tests.get(testIndex).getTest();
		} else {
			System.err.println("[ml-testing] error in choosing the test for concolic execution");
		}
		System.out.print("[ml-testing] given test for concolic execution is [" + test[0]);
		for (int i = 1; i < test.length; i++) {
			System.out.print(", " + test[i]);
		}
		System.out.println("]");
		// get the source information for the target branch
		Predicate p = Profiles.predicates.get(target.getPredicate());
		String className = p.getClassName();
		String srcLoc = className + "." + p.getMethodName() + "(" + className.substring(className.lastIndexOf(".") + 1) + ".java:" + p.getLineNumber() + ")";
		System.out.println("[ml-testing] target branch for concolic execution is " + srcLoc);
		// run concolic execution to get tests and branch constraints
		ConcolicExecution jdart = ConcolicExecution.getInstance(Config.JPFCONFIG);
		jdart.run(test);
		HashMap<Instruction, Expression<Boolean>> cons = new HashMap<Instruction, Expression<Boolean>>();
		HashSet<Valuation> vals = jdart.getValuations(srcLoc, Config.TESTS_SIZE, cons);
		System.out.println("[ml-testing] tests generated from concolic execution " + vals + "\n");
		// attach constraints to corresponding nodes
		pathLearner.attachConstraints(testIndex, cons);
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
