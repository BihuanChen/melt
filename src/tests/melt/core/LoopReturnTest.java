package melt.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

import melt.Config;
import melt.test.run.TestRunner;
import melt.test.util.TestCase;

public class LoopReturnTest {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, MalformedTreeException, BadLocationException, ClassNotFoundException {
		Config.loadProperties("/home/bhchen/workspace/testing/melt/src/tests/melt/core/LoopReturn.melt");
		//melt.MELT.instrument();
		
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("./pred/" + Config.MAINCLASS + ".pred")));
		Profile.predicates.addAll((ArrayList<Predicate>)oin.readObject());
		oin.close();
		Profile.printPredicates();
		
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		
		TestCase test1 = new TestCase(new Object[]{10, 5, 10, 5});
		TestRunner.run(test1.getTest());
		Profile.tests.add(test1);
		Profile.printExecutedPredicates();
		analyzer.update();
		analyzer.printNodes();
	}

}
