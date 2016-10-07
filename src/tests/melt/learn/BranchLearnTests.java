package melt.learn;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import melt.Config;
import melt.core.Predicate;
import melt.core.PredicateNode;
import melt.core.Profile;
import melt.core.ProfileAnalyzer;
import melt.learn.OneBranchLearner;
import melt.learn.PathLearner;
import melt.learn.TwoBranchLearner;
import melt.test.run.TestRunner;
import melt.test.util.TestCase;

public class BranchLearnTests {

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
	
	public static void main(String[] args) {

	}

}
