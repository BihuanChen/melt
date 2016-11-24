package melt.learn;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import melt.Config;
import melt.core.Predicate;
import melt.core.PredicateNode;
import melt.core.Profile;
import melt.core.ProfileAnalyzer;
import melt.learn.OneBranchLearner;
import melt.learn.PathLearner;
import melt.learn.TwoBranchLearner;
import melt.test.run.TaintRunner;
import melt.test.run.TestRunner;
import melt.test.util.TestCase;

public class BranchLearnerTests {

	// test the learning part for programs that do not have hidden nodes
	@SuppressWarnings("unchecked")
	public static void test1() throws Exception {
		// obtain the branch information
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		Profile.printPredicates();

		// construct the profile analyzer
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		
		// run the first test case and build the branch execution tree
		TestCase testInput1 = new TestCase(new Object[]{-1, 1, 1});
		TestRunner.run(testInput1.getTest());
		TaintRunner.run(testInput1.getTest());
		Profile.tests.add(testInput1);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		PredicateNode node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		PathLearner pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
				
		TestCase testInput2 = new TestCase(new Object[]{2, -1, 1});
		TestRunner.run(testInput2.getTest());
		TaintRunner.run(testInput2.getTest());
		Profile.tests.add(testInput2);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
		
		TestCase testInput3 = new TestCase(new Object[]{2, 2, 1});
		TestRunner.run(testInput3.getTest());
		TaintRunner.run(testInput3.getTest());
		Profile.tests.add(testInput3);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());

		// test the two-class classifier
		TwoBranchLearner twoLearner = analyzer.getNodes().get(3).getTwoBranchesLearner();
		twoLearner.buildInstancesAndClassifier();
		double[] probs = twoLearner.classifiyInstance(new TestCase(new Object[]{3, 1, 4}));
		System.out.println("[melt] " + probs[0] + " vs. " + probs[1]);
		// test if the two-class classifier will rebuild when no instance is added
		twoLearner = analyzer.getNodes().get(3).getTwoBranchesLearner();
		twoLearner.buildInstancesAndClassifier();
		
		TestCase testInput4 = new TestCase(new Object[]{3, 3, -1});
		TestRunner.run(testInput4.getTest());
		TaintRunner.run(testInput4.getTest());
		Profile.tests.add(testInput4);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());

		// test if a new test case can reach the target node
		System.out.println("[melt] valid test ? " + pl.isValidTest(new TestCase(new Object[]{3, 3, -1})));
		
		// test the one-class classifier
		OneBranchLearner oneLearner = node.getOneBranchLearner();
		oneLearner.buildInstancesAndClassifier();
		System.out.println("[melt] " + oneLearner.classifiyInstance(new TestCase(new Object[]{3, 3, -1}))[0]);
		// test if the one-class classifier will rebuild when no instance is added
		System.out.println("[melt] " + oneLearner.classifiyInstance(new TestCase(new Object[]{3, 1, -1}))[0]);
		
		// test the collecting of traces to a target branch
		pl = new PathLearner(analyzer.getRoot(), analyzer.getNodes().get(1));
		Iterator<ArrayList<Step>> iterator = pl.getTraces().iterator();
		while (iterator.hasNext()) {
			System.out.println("[melt] " + iterator.next());
		}
	}
		
	// test the learning part for programs that have hidden nodes	
	@SuppressWarnings("unchecked")
	public static void test2_1() throws Exception {
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		Profile.printPredicates();
		
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		
		TestCase test1 = new TestCase(new Object[]{3, 0, 0});
		TestRunner.run(test1.getTest());
		TaintRunner.run(test1.getTest());
		Profile.tests.add(test1);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		PredicateNode node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		PathLearner pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
				
		TestCase test2 = new TestCase(new Object[]{-3, 0, 3});
		TestRunner.run(test2.getTest());
		TaintRunner.run(test2.getTest());
		Profile.tests.add(test2);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
		
		TestCase test3 = new TestCase(new Object[]{-3, 3, -3});
		TestRunner.run(test3.getTest());
		TaintRunner.run(test3.getTest());
		Profile.tests.add(test3);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
		
		TestCase test4 = new TestCase(new Object[]{-3, -3, -3});
		TestRunner.run(test4.getTest());
		TaintRunner.run(test4.getTest());
		Profile.tests.add(test4);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
		
		// test the collecting of traces to a target branch
		pl = new PathLearner(analyzer.getRoot(), analyzer.getNodes().get(7));
		Iterator<ArrayList<Step>> iterator = pl.getTraces().iterator();
		while (iterator.hasNext()) {
			System.out.println("[melt] " + iterator.next());
		}
	}
	
	// test the learning part for programs that have hidden nodes	
	@SuppressWarnings("unchecked")
	public static void test2_2() throws Exception {
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		Profile.printPredicates();
		
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		
		TestCase test1 = new TestCase(new Object[]{3, 0, 0, 2});
		TestRunner.run(test1.getTest());
		TaintRunner.run(test1.getTest());
		Profile.tests.add(test1);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		PredicateNode node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		PathLearner pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
				
		TestCase test2 = new TestCase(new Object[]{-3, 0, 3, 2});
		TestRunner.run(test2.getTest());
		TaintRunner.run(test2.getTest());
		Profile.tests.add(test2);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
		
		TestCase test3 = new TestCase(new Object[]{-3, 3, -3, 2});
		TestRunner.run(test3.getTest());
		TaintRunner.run(test3.getTest());
		Profile.tests.add(test3);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
		
		TestCase test4 = new TestCase(new Object[]{-3, -3, -3, 2});
		TestRunner.run(test4.getTest());
		TaintRunner.run(test4.getTest());
		Profile.tests.add(test4);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[melt] target branch found " + node);
		pl = new PathLearner(analyzer.getRoot(), node);
		System.out.println("[melt] prefix traces found " + pl.getTraces());
		
		// test the collecting of traces to a target branch
		pl = new PathLearner(analyzer.getRoot(), analyzer.getNodes().get(3));
		Iterator<ArrayList<Step>> iterator = pl.getTraces().iterator();
		while (iterator.hasNext()) {
			System.out.println("[melt] " + iterator.next());
		}
	}
	
	public static void main(String[] args) throws Exception {
		// configuration for test1
		//Config.loadProperties("/home/bhchen/workspace/testing/melt/src/tests/melt/learn/BranchLearner1.melt");
		// configuration for test2_1 and test2_2
		Config.loadProperties("/home/bhchen/workspace/testing/melt/src/tests/melt/learn/BranchLearner2.melt");
		//melt.MELT.instrument();
		//test1();
		test2_2();
	}

}
