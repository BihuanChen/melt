package mlt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import mlt.dependency.DependencyAnalyzer;
import mlt.instrument.Instrumenter;
import mlt.instrument.Predicate;
import mlt.learn.PredicateNode;
import mlt.learn.ProfileAnalyzer;
import mlt.test.Profiles;
import mlt.test.TestCaseRunner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

public class MLT {

	private static String projectPath = "src/tests/mlt/learn/test1/";
	private static String classPath = "src/tests/";
	private static String mainClass = "mlt.learn.test1.TestAnalyzer";
	private static String entryPointMethod = "void test(int,int,int)";
		
	public static void preparePredicates() throws MalformedTreeException, IOException, BadLocationException {
		long t1 = System.currentTimeMillis();
		File project = new File(projectPath);
		Instrumenter instrumenter = new Instrumenter();
		instrumenter.formatFilesInDir(project);
		
		long t2 = System.currentTimeMillis();
		String entryPoint = "<" + mainClass + ": " + entryPointMethod + ">";
		LinkedHashMap<String, HashSet<Integer>> dependency = DependencyAnalyzer.doInterAnalysis(classPath, mainClass, entryPoint);
		
		long t3 = System.currentTimeMillis();
		instrumenter.instrumentFilesInDir(project);
		ArrayList<Predicate> predicates = instrumenter.getPredicates();
		
		long t4 = System.currentTimeMillis();
		int size = predicates.size();
		for (int i = 0; i < size; i++) {
			Predicate p = predicates.get(i);
			String id = p.getClassName() + " " + p.getLineNumber();
			HashSet<Integer> set = dependency.get(id);
			if (set != null) {
				p.setDepInputs(set);				
			} else {
				System.err.println("[ml-testing] inputs-branch dependency not found");
			}
		}
		
		long t5 = System.currentTimeMillis();
		ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(new File("predicates.out")));
		oout.writeObject(instrumenter);
		oout.close();
		
		long t6 = System.currentTimeMillis();
		System.out.println("[ml-testing] project formatted in " + (t2 - t1) + " ms");
		System.out.println("[ml-testing] dependency analyzed in " + (t3 - t2 + t5 - t4) + " ms");
		System.out.println("[ml-testing] project instrumented in " + (t4 - t3) + " ms");
		System.out.println("[ml-testing] predicates serialized in " + (t6 - t5) + " ms");
	}
	
	public static void runTests() throws IOException, ClassNotFoundException {
		long t1 = System.currentTimeMillis();
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File("predicates.out")));
		Profiles.predicates.addAll(((Instrumenter)oin.readObject()).getPredicates());
		oin.close();
		Profiles.printPredicates();
		
		long t2 = System.currentTimeMillis();
		System.out.println("[ml-testing] predicates deserialized in " + (t2 - t1) + " ms");

		String className = mainClass;
		String methodName = entryPointMethod.substring(entryPointMethod.indexOf(" ") + 1, entryPointMethod.indexOf("("));
		String[] clsStr = entryPointMethod.substring(entryPointMethod.indexOf("(") + 1, entryPointMethod.indexOf(")")).split(",");
		int size = clsStr.length;
		@SuppressWarnings("rawtypes")
		Class[] cls = new Class[size];
		for (int i = 0; i < size; i++) {
			if (clsStr[i].equals("int")) {
				cls[i] = int.class;
			} else if (clsStr[i].equals("long")) {
				cls[i] = long.class;
			} else if (clsStr[i].equals("boolean")) {
				cls[i] = boolean.class;
			} else if (clsStr[i].equals("float")) {
				cls[i] = float.class;
			} else if (clsStr[i].equals("double")) {
				cls[i] = double.class;
			} else {
				System.err.println("[ml-testing] unsupported input type");
			}
		}

		test2(className, methodName, cls);
	}
	
	public static void test1(String className, String methodName, @SuppressWarnings("rawtypes") Class[] cls) {
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		
		Object[] testInput1 = new Object[]{-1, 2, 1};
		TestCaseRunner runner = new TestCaseRunner(className, methodName, cls);
		runner.run(testInput1);
		Profiles.testInputs.add(testInput1);
		Profiles.printExecutedPridicates();
		analyzer.update();
		analyzer.printNodes();
		System.out.println();
		
		Object[] testInput2 = new Object[]{1, 1, -1};
		runner.run(testInput2);
		Profiles.testInputs.add(testInput2);
		Profiles.printExecutedPridicates();
		analyzer.update();
		analyzer.printNodes();
		System.out.println();

		Object[] testInput3 = new Object[]{1, 1, 1};
		runner.run(testInput3);
		Profiles.testInputs.add(testInput3);
		Profiles.printExecutedPridicates();
		analyzer.update();
		analyzer.printNodes();
		System.out.println();
		
		Object[] testInput4 = new Object[]{-1, -1, 1};
		runner.run(testInput4);
		Profiles.testInputs.add(testInput4);
		Profiles.printExecutedPridicates();
		analyzer.update();
		analyzer.printNodes();
		System.out.println();
	}
	
	public static void test2(String className, String methodName, @SuppressWarnings("rawtypes") Class[] cls) {
		ProfileAnalyzer analyzer = new ProfileAnalyzer();
		
		Object[] testInput1 = new Object[]{-1, 1, 1};
		TestCaseRunner runner = new TestCaseRunner(className, methodName, cls);
		runner.run(testInput1);
		Profiles.testInputs.add(testInput1);
		Profiles.printExecutedPridicates();
		analyzer.update();
		analyzer.printNodes();
		PredicateNode node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		System.out.println();
		
		Object[] testInput2 = new Object[]{2, -1, 1};
		runner.run(testInput2);
		Profiles.testInputs.add(testInput2);
		Profiles.printExecutedPridicates();
		analyzer.update();
		analyzer.printNodes();		
		node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		System.out.println();

		Object[] testInput3 = new Object[]{2, 2, 1};
		runner.run(testInput3);
		Profiles.testInputs.add(testInput3);
		Profiles.printExecutedPridicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		System.out.println();
		
		Object[] testInput4 = new Object[]{3, 3, -1};
		runner.run(testInput4);
		Profiles.testInputs.add(testInput4);
		Profiles.printExecutedPridicates();
		analyzer.update();
		analyzer.printNodes();
		node = analyzer.findUnexploredBranch();
		System.out.println("[ml-testing] target branch found " + node);
		System.out.println();
	}
	
	public static void main(String[] args) throws MalformedTreeException, IOException, BadLocationException, ClassNotFoundException {
		//MLT.preparePredicates();
		MLT.runTests();
	}

}
