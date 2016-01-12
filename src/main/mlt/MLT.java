package mlt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import mlt.dependency.StaticDependencyAnalyzer;
import mlt.instrument.Instrumenter;
import mlt.instrument.Predicate;
import mlt.learn.OneBranchLearner;
import mlt.learn.TwoBranchesLearner;
import mlt.learn.PathLearner;
import mlt.learn.PredicateNode;
import mlt.learn.ProfileAnalyzer;
import mlt.test.Profiles;
import mlt.test.TestCase;
import mlt.test.generation.random.PureRandomTestGenerator;
import mlt.test.generation.search.SearchBasedTestGenerator;
import mlt.test.run.TestRunner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

public class MLT {
		
	public static void prepare() throws MalformedTreeException, IOException, BadLocationException {
		// format the source code
		long t1 = System.currentTimeMillis();
		File project = new File(Config.SOURCEPATH);
		Instrumenter instrumenter = new Instrumenter();
		instrumenter.format(project);
		
		// instrument the source code
		long t2 = System.currentTimeMillis();
		instrumenter.instrument(project);
		
		// update line number information
		long t3 = System.currentTimeMillis();
		instrumenter.updateLineNumbers(project);
				
		// analyze inputs-branch dependency statically
		long t4 = System.currentTimeMillis();
		LinkedHashMap<String, HashSet<Integer>> dependency = null;
		if (Config.TAINT.equals("static")) {
			String entryPoint = "<" + Config.MAINCLASS + ": " + Config.ENTRYMETHOD + ">";
			dependency = StaticDependencyAnalyzer.doInterAnalysis(Config.CLASSPATH, Config.MAINCLASS, entryPoint);
		}
		
		Iterator<String> iterator = dependency.keySet().iterator();
		while (iterator.hasNext()) {
			String u = iterator.next();
			HashSet<Integer> set = dependency.get(u);
			System.out.println("[ml-testing] " + u);
			System.out.println("[ml-testing] " + set + "\n");
		}
		
		// map the inputs-branch dependency statically
		if (Config.TAINT.equals("static")) {
			ArrayList<Predicate> predicates = instrumenter.getPredicates();
			int size = predicates.size();
			for (int i = 0; i < size; i++) {
				Predicate p = predicates.get(i);
				String id = p.getClassName() + " " + p.getLineNumber();
				HashSet<Integer> set = dependency.get(id);
				if (set != null) {
					p.setDepInputs(set);				
				} else {
					System.err.println("[ml-testing] inputs-branch dependency not found " + p.getExpression());
				}
			}
		}
		
		// serialize the predicates
		long t5 = System.currentTimeMillis();
		ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(new File("predicates.pred")));
		oout.writeObject(instrumenter);
		oout.close();
		
		long t6 = System.currentTimeMillis();
		System.out.println("[ml-testing] project formatted in " + (t2 - t1) + " ms");
		System.out.println("[ml-testing] project instrumented in " + (t3 - t2) + " ms");
		System.out.println("[ml-testing] line number updated in " + (t4 - t3) + " ms");
		if (Config.TAINT.equals("static")) {
			System.out.println("[ml-testing] dependency analyzed in " + (t5 - t4) + " ms");
		}
		System.out.println("[ml-testing] predicates serialized in " + (t6 - t5) + " ms");
	}
	
	public static void run() throws Exception {
		// serialize the predicates
		long t1 = System.currentTimeMillis();
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("predicates.pred")));
		Profiles.predicates.addAll(((Instrumenter)oin.readObject()).getPredicates());
		oin.close();
		Profiles.printPredicates();
		
		// running ml-testing
		long t2 = System.currentTimeMillis();
		TestRunner runner = new TestRunner();
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		PathLearner learner = null;
		PredicateNode targetNode = null;
		
		long testTime = 0;
		int count = 0;

		while (true) {
			// generate and run tests, and analyze the branch profiles
			HashSet<TestCase> testCases = new SearchBasedTestGenerator(learner).generate();
			Iterator<TestCase> iterator = testCases.iterator();
			while (iterator.hasNext()) {
				TestCase testCase = iterator.next();
				long t = System.currentTimeMillis();
				runner.run(testCase);
				testTime += System.currentTimeMillis() - t;
				Profiles.tests.add(testCase);
				analyzer.update();
			}
			System.out.println("[ml-testing] the " + (++count) + " th set of tests");
			//analyzer.printNodes();
			analyzer.coverage(targetNode);			
			// find an partially explored branch to be covered
			targetNode = analyzer.findUnexploredBranch();
			if (targetNode == null) {
				System.out.println("[ml-testing] target branch not found \n");
				break;
			}
			System.out.println("[ml-testing] target branch found " + targetNode);
			// update the classification models from the current node to the root node
			learner = new PathLearner(analyzer.getRoot(), targetNode);
			System.out.println("[ml-testing] prefix traces found " + learner.getTraces());
		}
		
		long t3 = System.currentTimeMillis();
		System.out.println("[ml-testing] predicates deserialized in " + (t2 - t1) + " ms");
		System.out.println("[ml-testing] ml-testing in " + (t3 - t2) + " ms");
		System.out.println("[ml-testing] tests run in " + testTime + " ms");
	}
	
	public static void runRandom() throws Exception {
		// parameter
		long timeout = 17500;
		
		// serialize the predicates
		long t1 = System.currentTimeMillis();
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("predicates.pred")));
		Profiles.predicates.addAll(((Instrumenter)oin.readObject()).getPredicates());
		oin.close();
		//Profiles.printPredicates();
		
		// running random testing
		long t2 = System.currentTimeMillis();
		TestRunner runner = new TestRunner();
		ProfileAnalyzer analyzer = new ProfileAnalyzer();

		long testTime = 0;
		int count = 0;

		long endTime = t2 + timeout;
		while (true) {
			// generate and run tests, and analyze the branch profiles
			HashSet<TestCase> testCases = new PureRandomTestGenerator(null).generate();
			Iterator<TestCase> iterator = testCases.iterator();
			while (iterator.hasNext()) {
				TestCase testCase = iterator.next();
				long t = System.currentTimeMillis();
				runner.run(testCase);
				testTime += System.currentTimeMillis() - t;
				Profiles.tests.add(testCase);
				analyzer.update();
			}
			System.out.println("[ml-testing] the " + (++count) + " th set of tests");
			//analyzer.printNodes();
			analyzer.coverage(null);
			if (System.currentTimeMillis() > endTime) {
				break;
			}
		}
		
		long t3 = System.currentTimeMillis();
		System.out.println("[ml-testing] predicates deserialized in " + (t2 - t1) + " ms");
		System.out.println("[ml-testing] random testing in " + (t3 - t2) + " ms");
		System.out.println("[ml-testing] tests run in " + testTime + " ms");
	}
	
	public static void test() throws Exception {
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("predicates.pred")));
		Profiles.predicates.addAll(((Instrumenter)oin.readObject()).getPredicates());
		oin.close();
		Profiles.printPredicates();
		
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		TestRunner runner = new TestRunner();
		
		TestCase testInput1 = new TestCase(new Object[]{(byte)-1, (byte)1, (byte)1});
		runner.run(testInput1);
		Profiles.tests.add(testInput1);
		Profiles.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		PredicateNode node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		PathLearner pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[ml-testing] prefix traces found " + pl.getTraces());
				
		TestCase testInput2 = new TestCase(new Object[]{(byte)2, (byte)-1, (byte)1});
		runner.run(testInput2);
		Profiles.tests.add(testInput2);
		Profiles.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[ml-testing] prefix traces found " + pl.getTraces());
		
		TestCase testInput3 = new TestCase(new Object[]{(byte)2, (byte)2, (byte)1});
		runner.run(testInput3);
		Profiles.tests.add(testInput3);
		Profiles.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[ml-testing] prefix traces found " + pl.getTraces());

		
		TwoBranchesLearner twoLearner = analyzer.getNodes().get(3).getTwoBranchesLearner();
		twoLearner.buildInstancesAndClassifier();
		double[] probs = twoLearner.classifiyInstance(new TestCase(new Object[]{3, 1, 4}));
		System.out.println(probs[0] + ", " + probs[1]);
		twoLearner = analyzer.getNodes().get(3).getTwoBranchesLearner();
		twoLearner.buildInstancesAndClassifier();

		
		TestCase testInput4 = new TestCase(new Object[]{(byte)3, (byte)3, (byte)-1});
		runner.run(testInput4);
		Profiles.tests.add(testInput4);
		Profiles.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[ml-testing] prefix traces found " + pl.getTraces());

		
		// use TestAnalyzer.java
		//pl = new PathLearner(analyzer.getRoot(), analyzer.getNodes().get(1));
		//Iterator iterator = pl.getTraces().iterator();
		//while (iterator.hasNext()) {
		//	System.out.println(iterator.next());
		//}
		
		
		System.out.println("[ml-testing] valid test ? " + pl.isValidTest(new TestCase(new Object[]{3, 3, -1})));
		
		OneBranchLearner oneLearner = node.getOneBranchLearner();
		oneLearner.buildInstancesAndClassifier();
		System.out.println(oneLearner.classifiyInstance(new TestCase(new Object[]{3, 3, -1}))[0]);
		System.out.println(oneLearner.classifiyInstance(new TestCase(new Object[]{3, 1, -1}))[0]);
	}
	
	public static void main(String[] args) throws Exception {
		//Config.loadProperties("/home/bhchen/workspace/testing/benchmark1-art/src/dt/original/Triangle2.mlt");
		Config.loadProperties("/home/bhchen/workspace/testing/phosphor-test/src/phosphor/test/Test1.mlt");
		MLT.prepare();
		//MLT.run();
	}

}
