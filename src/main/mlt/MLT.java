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

import mlt.dependency.DependencyAnalyzer;
import mlt.instrument.Instrumenter;
import mlt.instrument.Predicate;
import mlt.learn.BranchLearner;
import mlt.learn.PathLearner;
import mlt.learn.PredicateNode;
import mlt.learn.ProfileAnalyzer;
import mlt.test.Profiles;
import mlt.test.TestGenerator;
import mlt.test.TestRunner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

public class MLT {
		
	public static void prepare() throws MalformedTreeException, IOException, BadLocationException {
		// format the source code
		long t1 = System.currentTimeMillis();
		File project = new File(Config.SOURCEPATH);
		Instrumenter instrumenter = new Instrumenter();
		instrumenter.formatFilesInDir(project);

		// analyze inputs-branch dependency
		long t2 = System.currentTimeMillis();
		String entryPoint = "<" + Config.MAINCLASS + ": " + Config.ENTRYMETHOD + ">";
		LinkedHashMap<String, HashSet<Integer>> dependency = DependencyAnalyzer.doInterAnalysis(Config.CLASSPATH, Config.MAINCLASS, entryPoint);
	
		// instrument the source code
		long t3 = System.currentTimeMillis();
		instrumenter.instrumentFilesInDir(project);
		ArrayList<Predicate> predicates = instrumenter.getPredicates();

		// map the inputs-branch dependency
		long t4 = System.currentTimeMillis();
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
		
		// serialize the predicates
		long t5 = System.currentTimeMillis();
		ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(new File("predicates.pred")));
		oout.writeObject(instrumenter);
		oout.close();
		
		long t6 = System.currentTimeMillis();
		System.out.println("[ml-testing] project formatted in " + (t2 - t1) + " ms");
		System.out.println("[ml-testing] dependency analyzed in " + (t3 - t2 + t5 - t4) + " ms");
		System.out.println("[ml-testing] project instrumented in " + (t4 - t3) + " ms");
		System.out.println("[ml-testing] predicates serialized in " + (t6 - t5) + " ms");
	}
	
	public static void run() throws Exception {
		// serialize the predicates
		long t1 = System.currentTimeMillis();
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("predicates.pred")));
		Profiles.predicates.addAll(((Instrumenter)oin.readObject()).getPredicates());
		oin.close();
		//Profiles.printPredicates();
		
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
			HashSet<Object[]> tests = new TestGenerator(learner).generate();
			Iterator<Object[]> iterator = tests.iterator();
			while (iterator.hasNext()) {
				Object[] test = iterator.next();
				//System.out.print("[ml-testing] test inputs are");
				//for (int i = 0; i < test.length; i++) {
				//	System.out.print(" " + test[i]);
				//}
				//System.out.println();
				long t = System.currentTimeMillis();
				runner.run(test);
				testTime += System.currentTimeMillis() - t;
				Profiles.tests.add(test);
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
			learner = new PathLearner();
			learner.findSourceNodes(targetNode);
			System.out.println("[ml-testing] prefix nodes found " + learner.getNodes());
			System.out.println("[ml-testing] prefix branches found " + learner.getBranches() + "\n");
		}
		
		long t3 = System.currentTimeMillis();
		System.out.println("[ml-testing] predicates deserialized in " + (t2 - t1) + " ms");
		System.out.println("[ml-testing] ml-testing in " + (t3 - t2) + " ms");
		System.out.println("[ml-testing] tests run in " + testTime + " ms");
	}
	
	public static void runRandom() throws Exception {
		// parameter
		long timeout = 28000;
		
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
			HashSet<Object[]> tests = new TestGenerator(null).generate();
			Iterator<Object[]> iterator = tests.iterator();
			while (iterator.hasNext()) {
				Object[] test = iterator.next();
				//System.out.print("[ml-testing] test inputs are");
				//for (int i = 0; i < test.length; i++) {
				//	System.out.print(" " + test[i]);
				//}
				//System.out.println();
				long t = System.currentTimeMillis();
				runner.run(test);
				testTime += System.currentTimeMillis() - t;
				Profiles.tests.add(test);
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
		
		Object[] testInput1 = new Object[]{(byte)-1, (byte)1, (byte)1};
		runner.run(testInput1);
		Profiles.tests.add(testInput1);
		Profiles.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		PredicateNode node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		PathLearner pl = new PathLearner();
		pl.findSourceNodes(node);
		System.out.println("[ml-testing] prefix nodes found " + pl.getNodes());
		System.out.println("[ml-testing] prefix branches found " + pl.getBranches() + "\n");
				
		Object[] testInput2 = new Object[]{(byte)2, (byte)-1, (byte)1};
		runner.run(testInput2);
		Profiles.tests.add(testInput2);
		Profiles.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();		
		node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		pl = new PathLearner();
		pl.findSourceNodes(node);
		System.out.println("[ml-testing] prefix nodes found " + pl.getNodes());
		System.out.println("[ml-testing] prefix branches found " + pl.getBranches() + "\n");
		
		Object[] testInput3 = new Object[]{(byte)2, (byte)2, (byte)1};
		runner.run(testInput3);
		Profiles.tests.add(testInput3);
		Profiles.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		pl = new PathLearner();
		pl.findSourceNodes(node);
		System.out.println("[ml-testing] prefix nodes found " + pl.getNodes());
		System.out.println("[ml-testing] prefix branches found " + pl.getBranches() + "\n");

		
		BranchLearner learner = analyzer.getNodes().get(3).getLearner();
		learner.buildInstancesAndClassifier();
		learner.classifiyInstance(new Object[]{3, 1, 4});

		learner = analyzer.getNodes().get(3).getLearner();
		learner.buildInstancesAndClassifier();

		
		Object[] testInput4 = new Object[]{(byte)3, (byte)3, (byte)-1};
		runner.run(testInput4);
		Profiles.tests.add(testInput4);
		Profiles.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		pl = new PathLearner();
		pl.findSourceNodes(node);
		System.out.println("[ml-testing] prefix nodes found " + pl.getNodes());
		System.out.println("[ml-testing] prefix branches found " + pl.getBranches() + "\n");
		
		
		System.out.println("[ml-testing] valid test ? " + pl.isValidTest(new Object[]{3, 1, -4}));
	}
	
	public static void main(String[] args) throws Exception {
		Config.loadProperties("src/examples/dt/original/Bessj.mlt");
		//MLT.prepare();
		MLT.runRandom();
	}

}
