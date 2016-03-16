package melt.dependency;

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

public class StaticDependencyAnalyzer {
	
	private static boolean debug = false;
	
	public static void doIntraAnalysis(String classPath, String inputClass) {
		String[] intraOptions = new String[8];
		intraOptions[0] = "-cp";
		intraOptions[1] = classPath;
		intraOptions[2] = "-pp";
		intraOptions[3] = "-f";
		intraOptions[4] = "J";
		intraOptions[5] = "-keep-line-number";
		intraOptions[6] = "-coffi";
		intraOptions[7] = inputClass;
		
		PackManager.v().getPack("jtp").add(new Transform("jtp.ibda", new BodyTransformer(){

			@Override
			protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
				UnitGraph graph = new ExceptionalUnitGraph(body);
				if (debug) {
					InputBranchDependencyIntraWrapper w = new InputBranchDependencyIntraWrapper(graph);
					Iterator<Unit> i = graph.iterator();
					while (i.hasNext()) {
						Unit u = i.next();
						System.out.println("[melt] " + u);
						System.out.println("[melt] " + w.getLocalsToInputsBefore(u));
						System.out.println("[melt] " + w.getLocalsToInputsAfter(u) + "\n");
					}
				} else {
					InputBranchDependencyIntraAnalysis analysis = new InputBranchDependencyIntraAnalysis(graph);
					LinkedHashMap<Unit, HashSet<Value>> branchesToInputsDependency = analysis.getBranchesToInputsDependency();
					Iterator<Unit> i = branchesToInputsDependency.keySet().iterator();
					while (i.hasNext()) {
						Unit u = i.next();
						System.out.println("[melt] " + u);
						System.out.println("[melt] " + u.getJavaSourceStartLineNumber());
						System.out.println("[melt] " + branchesToInputsDependency.get(u) + "\n");
					}
				}
			}
			
		}));
		soot.Main.main(intraOptions);
	}
	
	//FIXME: throw "Unknown tag in constant pool" if using java 1.8
	public static LinkedHashMap<String, HashSet<Integer>> doInterAnalysis(String classPath, String mainClass, final String entryPoint) {
		String[] interOptions = new String[16];
		interOptions[0] = "-cp";
		interOptions[1] = classPath;
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
		interOptions[13] = mainClass;
		interOptions[14] = mainClass;
		interOptions[15] = "-allow-phantom-refs";
		
		final LinkedHashMap<String, HashSet<Integer>> branchesToInputsDependency = new LinkedHashMap<String, HashSet<Integer>>();
		
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.ibda", new SceneTransformer() {

			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				InputBranchDependencyInterAnalysis problem = new InputBranchDependencyInterAnalysis(new JimpleBasedInterproceduralCFG(), entryPoint);
				IFDSSolver<Unit, Pair<Value, Value>, SootMethod, InterproceduralCFG<Unit,SootMethod>> solver = new IFDSSolver<Unit, Pair<Value, Value>, SootMethod, InterproceduralCFG<Unit,SootMethod>>(problem);
				solver.solve();
				if (debug) {
					//SootMethod m = Scene.v().getMethod("<melt.dependency.test1.TestInputBranchDependencyInter1: void m1(int,int,int)>");
					SootMethod m = Scene.v().getMethod("<dt.original.Median: int driver(int,int,int,int)>");
					Iterator<Unit> i = m.getActiveBody().getUnits().iterator();
					while (i.hasNext()) { 
						Unit u = i.next();
						System.out.println("[melt] " + u);
						for(Pair<Value, Value> p: solver.ifdsResultsAt(u)) {
							System.out.println("[melt] " + p);
						}
						System.out.println();
					}
				} else {
					branchesToInputsDependency.putAll(problem.getBranchesToInputsDependency());
					/*Iterator<String> i = branchesToInputsDependency.keySet().iterator();
					while (i.hasNext()) {
						String u = i.next();
						HashSet<Integer> set = branchesToInputsDependency.get(u);
						System.out.println("[melt] " + u);
						System.out.println("[melt] " + set + "\n");
					}*/
				}
			}

		}));
		soot.Main.main(interOptions);
		return branchesToInputsDependency;
	}
	
	public static void main(String[] args) {
		//StaticDependencyAnalyzer.doIntraAnalysis("src/tests/", "melt.dependency.test2.TestInputBranchDependencyIntra");
		//StaticDependencyAnalyzer.doInterAnalysis("src/tests/", "melt.dependency.test1.TestInputBranchDependencyInter1", "<melt.dependency.test1.TestInputBranchDependencyInter1: void entryPointMain(int,int,int,boolean)>");
		//StaticDependencyAnalyzer.doInterAnalysis("src/examples/", "dt.original.Median", "<dt.original.Median: int driver(int,int,int,int)>");
		StaticDependencyAnalyzer.doInterAnalysis("c:/Users/bhchen/workspace/testing/benchmark1-art-ce/bin/", "dt.original.Bessj", "<dt.original.Bessj: double bessj(int,double)>");
	}

}