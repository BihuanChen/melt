package edu.ntu.learn.feature;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
	
	public static void runInter(String[] args) {
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.ibda", new SceneTransformer() {

			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				IFDSTabulationProblem<Unit, Pair<Value, Set<Value>>, SootMethod, InterproceduralCFG<Unit, SootMethod>> problem = new InputBranchDependencyInterAnalysis(new JimpleBasedInterproceduralCFG());
				IFDSSolver<Unit, Pair<Value, Set<Value>>, SootMethod, InterproceduralCFG<Unit,SootMethod>> solver = new IFDSSolver<Unit, Pair<Value,Set<Value>>, SootMethod, InterproceduralCFG<Unit,SootMethod>>(problem);
				solver.solve();
				Unit u = Scene.v().getMainMethod().getActiveBody().getUnits().getFirst();
				u = Scene.v().getMainMethod().getActiveBody().getUnits().getSuccOf(u);
				u = Scene.v().getMainMethod().getActiveBody().getUnits().getSuccOf(u);
				System.err.println(u);
				for(Pair<Value, Set<Value>> p: solver.ifdsResultsAt(u)) {
					System.err.println(p);
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
		intraOptions[7] = "edu.ntu.learn.feature.test.TestInputBranchDependencyIntra";
		//Main.runIntra(intraOptions);
		
		String[] interOptions = new String[14];
		interOptions[0] = "-cp";
		interOptions[1] = "build/tests/";
		interOptions[2] = "-pp";
		interOptions[3] = "-f";
		interOptions[4] = "J";
		interOptions[5] = "-keep-line-number";
		interOptions[6] = "-coffi";
		interOptions[7] = "-w";
		interOptions[8] = "-p";
		interOptions[9] = "cg.spark";
		interOptions[10] = "on";
		interOptions[11] = "-main-class";
		interOptions[12] = "edu.ntu.learn.feature.test.TestInputBranchDependencyInter";
		interOptions[13] = "edu.ntu.learn.feature.test.TestInputBranchDependencyInter";
		Main.runInter(interOptions);
	}

}
