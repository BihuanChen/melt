package melt;

import gov.nasa.jpf.constraints.api.Valuation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import melt.core.Predicate;
import melt.core.PredicateNode;
import melt.core.ProfileAnalyzer;
import melt.core.Profile;
import melt.instrument.Instrumenter;
import melt.learn.OneBranchLearner;
import melt.learn.PathLearner;
import melt.learn.TwoBranchLearner;
import melt.test.generation.concolic.ConcolicExecution;
import melt.test.generation.random.AdaptiveRandomTestGenerator;
import melt.test.generation.random.PureRandomTestGenerator;
import melt.test.generation.search.SearchBasedTestGenerator;
import melt.test.run.TaintRunner;
import melt.test.run.TestRunner;
import melt.test.util.TestCase;
import melt.test.util.Util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

public class MELT {
		
	public static void instrument() throws MalformedTreeException, IOException, BadLocationException {
		// instrument the source code
		long t1 = System.currentTimeMillis();
		Instrumenter instrumenter = new Instrumenter(Config.SOURCEPATH);
		instrumenter.instrument();

		// serialize the predicates
		long t2 = System.currentTimeMillis();
		ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		oout.writeObject(instrumenter.getPredicates());
		oout.close();
		
		long t3 = System.currentTimeMillis();
		System.out.println("[melt] project instrumented in " + (t2 - t1) + " ms");
		System.out.println("[melt] predicates serialized in " + (t3 - t2) + " ms");
	}
	
	@SuppressWarnings("unchecked")
	public static void run() throws Exception {
		// deserialize the predicates
		long t1 = System.currentTimeMillis();
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		System.out.println("[melt] " + Config.FORMAT.format(System.currentTimeMillis()));
		Profile.printPredicates();
		
		// run melt
		long t2 = System.currentTimeMillis();
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		PathLearner learner = null;
		PredicateNode targetNode = null;
		
		long testTime = 0;
		long geneTime = 0;
		int count = 0;
		long testSize = 0;
		int redundant = 0;
		
		while (true) {
			// generate and run tests, and analyze the branch profiles
			long s = System.currentTimeMillis();
			HashSet<TestCase> testCases = new SearchBasedTestGenerator(learner).generate();
			geneTime += System.currentTimeMillis() - s;
			testSize += testCases.size();
			Iterator<TestCase> iterator = testCases.iterator();
			while (iterator.hasNext()) {
				final TestCase testCase = iterator.next();
				System.out.println("[melt]" + testCase);
				if (!Profile.testsSet.contains(testCase)) {
					long t = System.currentTimeMillis();
					// get taint results
					FutureTask<?> task1 = new FutureTask<Void>(new Runnable() {
						@Override
						public void run() {
							try {
								TaintRunner.run(testCase.getTest());
							} catch (ClassNotFoundException | IOException | InterruptedException e) {
								e.printStackTrace();
							}
						}
					}, null);
					new Thread(task1).start();
					// get executed predicates
					FutureTask<?> task2 = new FutureTask<Void>(new Runnable() {
						@Override
						public void run() {
							try {
								TestRunner.run(testCase.getTest());
							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
						}
					}, null);
					new Thread(task2).start();
					while (!task1.isDone() || !task2.isDone()) {} 
					long delta = System.currentTimeMillis() - t;
					testTime += delta;
					Profile.tests.add(testCase);
					Profile.testsSet.add(testCase);
					analyzer.update();
				} else {
					redundant++;
				}
			}
			System.out.println("[melt] " + Config.FORMAT.format(System.currentTimeMillis()));
			System.out.println("[melt] finish the " + (++count) + " th set of tests");
			//analyzer.printNodes();
			analyzer.coverage(targetNode);			
			// find an partially explored branch to be covered
			targetNode = analyzer.findUnexploredBranch();
			System.out.println("[melt] " + Config.FORMAT.format(System.currentTimeMillis()));
			if (targetNode == null) {
				System.out.println("[melt] target branch not found \n");
				break;
			}
			System.out.println("[melt] target branch found " + targetNode.getPredicate());
			// update the classification models from the current node to the root node
			learner = new PathLearner(analyzer.getRoot(), targetNode);
			System.out.println("[melt] prefix traces found " + (learner.getTraces() == null ? "null" : learner.getTraces().size()));
		}
		
		long t3 = System.currentTimeMillis();
		System.out.println("[melt] " + Config.FORMAT.format(t3));
		System.out.println("[melt] predicates deserialized in " + (t2 - t1) + " ms");
		System.out.println("[melt] " + testSize + "(" + redundant + ") tests run in " + testTime + " ms");
		System.out.println("[melt] " + testSize + "(" + redundant + ") tests generated in " + geneTime + " ms");
		System.out.println("[melt] concolic execution in " + SearchBasedTestGenerator.ceTime + " ms");
		System.out.println("[melt] melt in " + (t3 - t2) + " ms");
	}
	
	@SuppressWarnings("unchecked")
	public static void runRandom(long timeout, String algo) throws Exception {
		// deserialize the predicates
		long t1 = System.currentTimeMillis();
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		System.out.println("[melt] " + Config.FORMAT.format(System.currentTimeMillis()));
		Profile.printPredicates();
		
		// run random testing
		long t2 = System.currentTimeMillis();
		ProfileAnalyzer analyzer = new ProfileAnalyzer();

		long testTime = 0;
		int count = 0;
		long testSize = 0;
		
		long endTime = t2 + timeout;
		while (true) {
			// generate and run tests, and analyze the branch profiles
			HashSet<TestCase> testCases = null;
			if (algo.equals("ART")) {
				testCases = new AdaptiveRandomTestGenerator(null).generate();
			} else if (algo.equals("RT")) {
				testCases = new PureRandomTestGenerator(null).generate();
			} else {
				System.out.println("[melt] unsupported random testing tool");
				System.exit(0);
			}

			testSize += testCases.size();
			Iterator<TestCase> iterator = testCases.iterator();
			while (iterator.hasNext()) {
				TestCase testCase = iterator.next();
				long t = System.currentTimeMillis();
				TestRunner.run(testCase.getTest());
				testTime += System.currentTimeMillis() - t;
				Profile.tests.add(testCase);
				analyzer.update();
			}
			System.out.println("[melt] " + Config.FORMAT.format(System.currentTimeMillis()));
			System.out.println("[melt] finish the " + (++count) + " th set of tests");
			//analyzer.printNodes();
			analyzer.coverage(null);
			if (System.currentTimeMillis() > endTime) {
				break;
			}
		}
		
		long t3 = System.currentTimeMillis();
		System.out.println("[melt] " + Config.FORMAT.format(t3));
		System.out.println("[melt] predicates deserialized in " + (t2 - t1) + " ms");
		System.out.println("[melt] " + testSize + " tests run in " + testTime + " ms");
		System.out.println("[melt] random testing in " + (t3 - t2) + " ms" + "\n");
	}
	
	@SuppressWarnings("deprecation")
	public static void runConcolic(long timeout) throws InterruptedException, ExecutionException {
		FutureTask<?> task = new FutureTask<Void>(new Runnable() {
			@Override
			public void run() {
				try {
					MELT.runConcolic();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, null);
		Thread th = new Thread(task);
		th.start();
		try {
			task.get(timeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			System.out.println("[melt] timeout in concolic testing");
		}
		th.stop();
	}
	
	@SuppressWarnings("unchecked")
	private static void runConcolic() throws Exception {
		// deserialize the predicates
		long t1 = System.currentTimeMillis();
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		System.out.println("[melt] " + Config.FORMAT.format(System.currentTimeMillis()));
		Profile.printPredicates();
		
		// running concolic testing
		long t2 = System.currentTimeMillis();
		ProfileAnalyzer analyzer = new ProfileAnalyzer();

		long testTime = 0;
		long testSize = 0;
		
		// generate and run tests, and analyze the branch profiles
		ConcolicExecution jdart = new ConcolicExecution(Config.JPFCONFIG);
		jdart.run();
		HashSet<Valuation> vals = jdart.getValuations();
		testSize += vals.size();
		Iterator<Valuation> iterator = vals.iterator();
		while (iterator.hasNext()) {
			Object[] test = Util.valuationToTest(iterator.next());
			long t = System.currentTimeMillis();
			TestRunner.run(test);
			testTime += System.currentTimeMillis() - t;
			Profile.tests.add(new TestCase(test));
			analyzer.update();
		}
		System.out.println("[melt] " + Config.FORMAT.format(System.currentTimeMillis()));
		//analyzer.printNodes();
		analyzer.coverage(null);
		
		long t3 = System.currentTimeMillis();
		System.out.println("[melt] " + Config.FORMAT.format(t3));
		System.out.println("[melt] predicates deserialized in " + (t2 - t1) + " ms");
		System.out.println("[melt] " + testSize + " tests run in " + testTime + " ms");
		System.out.println("[melt] concolic testing in " + (t3 - t2) + " ms");
	}
	
	@SuppressWarnings("unchecked")
	public static void test1() throws Exception {
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		Profile.printPredicates();
		
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		
		TestCase testInput1 = new TestCase(new Object[]{(byte)-1, (byte)1, (byte)1});
		TestRunner.run(testInput1.getTest());
		Profile.tests.add(testInput1);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		PredicateNode node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		PathLearner pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
				
		TestCase testInput2 = new TestCase(new Object[]{(byte)2, (byte)-1, (byte)1});
		TestRunner.run(testInput2.getTest());
		Profile.tests.add(testInput2);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
		
		TestCase testInput3 = new TestCase(new Object[]{(byte)2, (byte)2, (byte)1});
		TestRunner.run(testInput3.getTest());
		Profile.tests.add(testInput3);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());

		
		TwoBranchLearner twoLearner = analyzer.getNodes().get(3).getTwoBranchesLearner();
		twoLearner.buildInstancesAndClassifier();
		double[] probs = twoLearner.classifiyInstance(new TestCase(new Object[]{3, 1, 4}));
		System.out.println("[melt] " + probs[0] + ", " + probs[1]);
		twoLearner = analyzer.getNodes().get(3).getTwoBranchesLearner();
		twoLearner.buildInstancesAndClassifier();

		
		TestCase testInput4 = new TestCase(new Object[]{(byte)3, (byte)3, (byte)-1});
		TestRunner.run(testInput4.getTest());
		Profile.tests.add(testInput4);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
	
		// use TestAnalyzer.java
		//pl = new PathLearner(analyzer.getRoot(), analyzer.getNodes().get(1));
		//Iterator iterator = pl.getTraces().iterator();
		//while (iterator.hasNext()) {
		//	System.out.println("[melt] " + iterator.next());
		//}
	
		System.out.println("[melt] valid test ? " + pl.isValidTest(new TestCase(new Object[]{3, 3, -1})));
		
		OneBranchLearner oneLearner = node.getOneBranchLearner();
		oneLearner.buildInstancesAndClassifier();
		System.out.println(oneLearner.classifiyInstance(new TestCase(new Object[]{3, 3, -1}))[0]);
		System.out.println(oneLearner.classifiyInstance(new TestCase(new Object[]{3, 1, -1}))[0]);
	}
	
	@SuppressWarnings("unchecked")
	public static void test2() throws Exception {
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		Profile.printPredicates();
		
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		
		TestCase test1 = new TestCase(new Object[]{1, -2});
		TestRunner.run(test1.getTest());
		Profile.tests.add(test1);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		PredicateNode node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		PathLearner pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
				
		TestCase test2 = new TestCase(new Object[]{1, 2});
		TestRunner.run(test2.getTest());
		Profile.tests.add(test2);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
		
		TwoBranchLearner twoLearner = analyzer.getNodes().get(1).getTwoBranchesLearner();
		twoLearner.buildInstancesAndClassifier();
		twoLearner = analyzer.getNodes().get(1).getTwoBranchesLearner();
		twoLearner.buildInstancesAndClassifier();
		
		OneBranchLearner oneLearner = analyzer.getNodes().get(0).getOneBranchLearner();
		oneLearner.buildInstancesAndClassifier();
		oneLearner = analyzer.getNodes().get(0).getOneBranchLearner();
		oneLearner.buildInstancesAndClassifier();
	}
	
	public static void main(String[] args) throws Exception {
		boolean inst = false;
		
		String algo = "MELT";
		String[] program = {"Replace"};
		long[] timeout = {60000};
		
		for (int k = 0; k < program.length; k++) {
			Config.loadProperties("/home/bhchen/workspace/testing/benchmark4-siemens/src/replace/" + program[k] + ".melt");
			
			// instrument the source code
			if (inst) {
				MELT.instrument();
				return;
			}
			
			// run the testing tool
			for (int i = 0; i <= 0; i++) {
				System.out.println("\n[melt] the " + i + " th run");
				if (algo.equals("MELT")) {
					MELT.run();
				} else if (algo.equals("CT")) {
					MELT.runConcolic(timeout[k]);
				} else {
					MELT.runRandom(timeout[k], algo);
				}
				
				ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(new File("/media/bhchen/Data/data/melt/" + program[k] + "/" + algo + "/tests-" + i)));
				for (int j = 0; j < Profile.tests.size(); j++) {
					Profile.tests.get(j).setValuation(null);
				}
				oout.writeObject(Profile.tests);
				oout.close();
				
				Profile.predicates.clear();
				Profile.tests.clear();
				Profile.testsSet.clear();
				SearchBasedTestGenerator.ceTime = 0;
			}
		}
	}

}
