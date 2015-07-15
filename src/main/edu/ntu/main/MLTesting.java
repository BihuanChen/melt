package edu.ntu.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

import edu.ntu.feature.input.DependencyAnalysis;
import edu.ntu.feature.input.test1.TestInputBranchDependencyInter1;
import edu.ntu.instrument.Instrumentor;
import edu.ntu.instrument.PInstance;
import edu.ntu.instrument.Predicate;

public class MLTesting {

	private static String projectPath = "src/tests/edu/ntu/feature/input/test1/";
	private static String classPath = "src/tests/";
	private static String mainClass = "edu.ntu.feature.input.test1.TestInputBranchDependencyInter1";
	private static String entryPointMethod = "void entryPointMain(int,int,int,boolean)";
	
	public static void preparePredicates() throws MalformedTreeException, IOException, BadLocationException {
		long t1 = System.currentTimeMillis();
		File project = new File(projectPath);
		Instrumentor instrumentor = new Instrumentor();
		instrumentor.formatFilesInDir(project);
		
		long t2 = System.currentTimeMillis();
		String entryPoint = "<" + mainClass + ": " + entryPointMethod + ">";
		LinkedHashMap<String, HashSet<Integer>> dependency = DependencyAnalysis.doInterAnalysis(classPath, mainClass, entryPoint);
		
		long t3 = System.currentTimeMillis();
		instrumentor.instrumentFilesInDir(project);
		ArrayList<Predicate> predicates = instrumentor.getPredicates();
		
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
		oout.writeObject(instrumentor);
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
		PInstance.predicates.addAll(((Instrumentor)oin.readObject()).getPredicates());
		oin.close();
		PInstance.printPredicates();
		
		long t2 = System.currentTimeMillis();
		System.out.println("[ml-testing] predicates deserialized in " + (t2 - t1) + " ms");
		
		System.out.println();
		TestInputBranchDependencyInter1.main(null);
		PInstance.printPredicates();
		System.out.println();
		PInstance.resetPredicatesCounters();
		PInstance.printPredicates();
	}
	
	public static void main(String[] args) throws MalformedTreeException, IOException, BadLocationException, ClassNotFoundException {
		MLTesting.preparePredicates();
		//MLTesting.runTests();
	}

}