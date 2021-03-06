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
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import melt.core.Predicate;
import melt.core.PredicateNode;
import melt.core.ProfileAnalyzer;
import melt.core.Profile;
import melt.instrument.Instrumenter;
import melt.learn.PathLearner;
import melt.test.generation.concolic.ConcolicExecution;
import melt.test.generation.random.AdaptiveRandomTestGenerator;
import melt.test.generation.random.PureRandomTestGenerator;
import melt.test.generation.search2.SearchBasedTestGenerator;
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
			//HashSet<TestCase> testCases = new PureRandomTestGenerator(learner).generate();
			geneTime += System.currentTimeMillis() - s;
			testSize += testCases.size();
			Iterator<TestCase> iterator = testCases.iterator();
			while (iterator.hasNext()) {
				final TestCase testCase = iterator.next();
				//System.out.println("[melt]" + testCase);
				if (!Profile.testsSet.contains(testCase)) {
					long t = System.currentTimeMillis();
					// get taint results
					FutureTask<?> task1 = new FutureTask<Void>(new Runnable() {
						@Override
						public void run() {
							try {
								TaintRunner.run(testCase.getTest());
							} catch (IOException | InterruptedException e) {
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
					// get the potential exception in task1 and task2
					task1.get(); task2.get();
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
			System.out.println("[melt] finish the " + (++count) + " th set of tests (" + testCases.size() + ")");
			//analyzer.printNodes();
			analyzer.computeCoverage(targetNode);			
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
	public static void runHT(long timeout) throws Exception {
		// deserialize the predicates
		long t1 = System.currentTimeMillis();
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		System.out.println("[melt] " + Config.FORMAT.format(System.currentTimeMillis()));
		Profile.printPredicates();

		// run hybrid testing
		Config.DT_ENABLED = false;
		long t2 = System.currentTimeMillis();
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		final ArrayList<PathLearner> learner = new ArrayList<PathLearner>(1);
		learner.add(null);
		PredicateNode targetNode = null;
		
		long testTime = 0;
		long geneTime = 0;
		int count = 0;
		long testSize = 0;
		int redundant = 0;
		
		long endTime = t2 + timeout;
		while (true) {
			// generate and run tests, and analyze the branch profiles
			long s = System.currentTimeMillis();
			
			final HashSet<TestCase> testCases = new HashSet<TestCase>();
			FutureTask<?> task = new FutureTask<Void>(new Runnable() {
				@Override
				public void run() {
					try {
						testCases.addAll(new PureRandomTestGenerator(learner.get(0)).generate());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, null);
			Thread th = new Thread(task);
			th.start();
			try {
				task.get(endTime - s, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				th.stop();
				System.out.println("[melt] timeout in concolic testing");
			}
			
			geneTime += System.currentTimeMillis() - s;
			testSize += testCases.size();
			Iterator<TestCase> iterator = testCases.iterator();
			while (iterator.hasNext()) {
				final TestCase testCase = iterator.next();
				//System.out.println("[melt]" + testCase);
				if (!Profile.testsSet.contains(testCase)) {
					long t = System.currentTimeMillis();
					TestRunner.run(testCase.getTest());
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
			System.out.println("[melt] finish the " + (++count) + " th set of tests (" + testCases.size() + ")");
			//analyzer.printNodes();
			analyzer.computeCoverage(targetNode);			
			// find an partially explored branch to be covered
			targetNode = analyzer.findUnexploredBranchwithoutContext();
			System.out.println("[melt] " + Config.FORMAT.format(System.currentTimeMillis()));
			if (targetNode == null) {
				System.out.println("[melt] target branch not found \n");
				break;
			}
			System.out.println("[melt] target branch found " + targetNode.getPredicate());
			learner.set(0, new PathLearner(targetNode));
			if (System.currentTimeMillis() > endTime) {
				break;
			}
		}
		
		long t3 = System.currentTimeMillis();
		System.out.println("[melt] " + Config.FORMAT.format(t3));
		System.out.println("[melt] predicates deserialized in " + (t2 - t1) + " ms");
		System.out.println("[melt] " + testSize + "(" + redundant + ") tests run in " + testTime + " ms");
		System.out.println("[melt] " + testSize + "(" + redundant + ") tests generated in " + geneTime + " ms");
		System.out.println("[melt] concolic execution in " + PureRandomTestGenerator.ceTime + " ms");
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
			analyzer.computeCoverage(null);
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
	
	/*@SuppressWarnings("deprecation")
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
	}*/
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public static void runConcolic(long timeout) throws Exception {
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
		final ConcolicExecution jdart = new ConcolicExecution(Config.JPFCONFIG);
		
		FutureTask<?> task = new FutureTask<Void>(new Runnable() {
			@Override
			public void run() {
				try {
					jdart.run();
					jdart.statistics();
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
			th.stop();
			System.out.println("[melt] timeout in concolic testing");
		}
		
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
		analyzer.computeCoverage(null);
		
		long t3 = System.currentTimeMillis();
		System.out.println("[melt] " + Config.FORMAT.format(t3));
		System.out.println("[melt] predicates deserialized in " + (t2 - t1) + " ms");
		System.out.println("[melt] " + testSize + " tests run in " + testTime + " ms");
		System.out.println("[melt] concolic testing in " + (t3 - t2) + " ms");
	}
		
	public static void main(String[] args) throws Exception {		
		boolean inst = false;
		
		String algo = "MELT";
		String[] program = {"WBS"};
		long[] timeout = {46000};
		
		for (int k = 0; k < program.length; k++) {
			Config.loadProperties("/home/bhchen/workspace/testing/benchmark2-jpf/src/wbs/" + program[k] + ".melt");
			
			// instrument the source code
			if (inst) {
				MELT.instrument();
				return;
			}
			
			// run the testing tool
			for (int i = 5; i <= 5; i++) {
				System.out.println("\n[melt] the " + i + " th run");
				if (algo.equals("MELT")) {
					MELT.run();
				} else if (algo.equals("HT")){
					MELT.runHT(timeout[k]);
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
				PureRandomTestGenerator.ceTime = 0;
			}
		}
	}

}
