package melt.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import melt.Config;
import melt.test.run.TestRunner;
import melt.test.util.TestCase;

public class HiddenNodeTests {

	@SuppressWarnings("unchecked")
	public static void testHiddenNode1() throws Exception {
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		Profile.printPredicates();
		
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		
		TestCase test1 = new TestCase(new Object[]{3, 0, 0});
		TestRunner.run(test1.getTest());
		Profile.tests.add(test1);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
				
		TestCase test2 = new TestCase(new Object[]{-3, 3, 0});
		TestRunner.run(test2.getTest());
		Profile.tests.add(test2);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		
		TestCase test3 = new TestCase(new Object[]{-3, -3, -2});
		TestRunner.run(test3.getTest());
		Profile.tests.add(test3);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		
		TestCase test4 = new TestCase(new Object[]{-3, -3, 2});
		TestRunner.run(test4.getTest());
		Profile.tests.add(test4);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
	}
	
	@SuppressWarnings("unchecked")
	public static void testHiddenNode2() throws Exception {
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		Profile.printPredicates();
		
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		
		TestCase test1 = new TestCase(new Object[]{3, 0, 0});
		TestRunner.run(test1.getTest());
		Profile.tests.add(test1);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
				
		TestCase test2 = new TestCase(new Object[]{-3, 0, 3});
		TestRunner.run(test2.getTest());
		Profile.tests.add(test2);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		
		TestCase test3 = new TestCase(new Object[]{-3, 3, -3});
		TestRunner.run(test3.getTest());
		Profile.tests.add(test3);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
		
		TestCase test4 = new TestCase(new Object[]{-3, -3, -3});
		TestRunner.run(test4.getTest());
		Profile.tests.add(test4);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
	}
	
	public static void main(String[] args) throws Exception {
		Config.loadProperties("/home/bhchen/workspace/testing/melt/src/tests/melt/core/HiddenNode.melt");
		//melt.MELT.instrument();
		HiddenNodeTests.testHiddenNode2();
	}

}
