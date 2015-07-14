package edu.ntu.feature.input;

import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;

public class Main {
	
	private static boolean debug = false;
	private final static LinkedHashMap<Unit, HashSet<Value>> branchesToInputsDependency = new LinkedHashMap<Unit, HashSet<Value>>();
	
	public static void runIntra (String[] args) {
		PackManager.v().getPack("jtp").add(new Transform("jtp.ibda", new BodyTransformer(){

			@Override
			protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
				UnitGraph graph = new ExceptionalUnitGraph(body);
				if (debug) {
					InputBranchDependencyIntraWrapper w = new InputBranchDependencyIntraWrapper(graph);
					Iterator<Unit> i = graph.iterator();
					while (i.hasNext()) {
						Unit u = i.next();
						System.out.println(u);
						System.out.println(w.getLocalsToInputsBefore(u));
						System.out.println(w.getLocalsToInputsAfter(u) + "\n");
					}
				} else {
					InputBranchDependencyIntraAnalysis analysis = new InputBranchDependencyIntraAnalysis(graph);
					branchesToInputsDependency.putAll(analysis.getBranchesToInputsDependency());
					Iterator<Unit> i = branchesToInputsDependency.keySet().iterator();
					while (i.hasNext()) {
						Unit u = i.next();
						System.out.println(u);
						System.out.println(u.getJavaSourceStartLineNumber());
						System.out.println(branchesToInputsDependency.get(u) + "\n");
					}
				}
			}
			
		}));
		soot.Main.main(args);
	}
	
	public static void runInter(String[] args, final String entryPoint) {
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.ibda", new SceneTransformer() {

			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				InputBranchDependencyInterAnalysis problem = new InputBranchDependencyInterAnalysis(new JimpleBasedInterproceduralCFG(), entryPoint);
				IFDSSolver<Unit, Pair<Value, Value>, SootMethod, InterproceduralCFG<Unit,SootMethod>> solver = new IFDSSolver<Unit, Pair<Value, Value>, SootMethod, InterproceduralCFG<Unit,SootMethod>>(problem);
				solver.solve();
				if (debug) {
					SootMethod m = Scene.v().getMethod("<edu.ntu.feature.input.test.TestInputBranchDependencyInter: void m1(int,int,int)>");
					Iterator<Unit> i = m.getActiveBody().getUnits().iterator();
					while (i.hasNext()) { 
						Unit u = i.next();
						System.err.println(u);
						for(Pair<Value, Value> p: solver.ifdsResultsAt(u)) {
							System.err.println(p);
						}
						System.err.println();
					}
				} else {
					branchesToInputsDependency.putAll(problem.getBranchesToInputsDependency());
					Iterator<Unit> i = branchesToInputsDependency.keySet().iterator();
					while (i.hasNext()) {
						Unit u = i.next();
						System.out.println(u);
						System.out.println(u.getJavaSourceStartLineNumber());
						System.out.println(branchesToInputsDependency.get(u) + "\n");
					}
				}
			}

		}));
		soot.Main.main(args);
	}
	
	public static void main(String[] args) {
		String[] intraOptions = new String[8];
		intraOptions[0] = "-cp";
		intraOptions[1] = "build/tests/";
		intraOptions[2] = "-pp";
		intraOptions[3] = "-f";
		intraOptions[4] = "J";
		intraOptions[5] = "-keep-line-number";
		intraOptions[6] = "-coffi";
		intraOptions[7] = "edu.ntu.feature.input.test.TestInputBranchDependencyIntra";
		//Main.runIntra(intraOptions);
		
		String[] interOptions = new String[15];
		interOptions[0] = "-cp";
		interOptions[1] = "build/tests/";
		interOptions[2] = "-pp";
		interOptions[3] = "-f";
		interOptions[4] = "J";
		interOptions[5] = "-keep-line-number";
		interOptions[6] = "-coffi";
		interOptions[7] = "-app";
		interOptions[8] = "-w";
		interOptions[9] = "-p";
		interOptions[10] = "cg.spark";
		interOptions[11] = "on";
		interOptions[12] = "-main-class";
		interOptions[13] = "edu.ntu.feature.input.test.TestInputBranchDependencyInter1";
		interOptions[14] = "edu.ntu.feature.input.test.TestInputBranchDependencyInter1";
		Main.runInter(interOptions, "<edu.ntu.feature.input.test.TestInputBranchDependencyInter1: void entryPointMain(int,int,int,boolean)>");
	}

}
