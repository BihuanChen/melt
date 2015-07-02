package edu.ntu.learn.feature;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class Main {
	
	private static boolean debug = false;
	private final static LinkedHashMap<Unit, HashSet<Value>> branchesToInputsDependency = new LinkedHashMap<Unit, HashSet<Value>>();
	
	public static void runIntra (String[] args) {
		PackManager.v().getPack("jtp").add(new Transform("jtp.ibda", new BodyTransformer(){

			@Override
			protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
				UnitGraph graph = new ExceptionalUnitGraph(body);
				if (debug) {
					InputBranchDependencyWrapper w = new InputBranchDependencyWrapper(graph);
					Iterator<Unit> i = graph.iterator();
					while (i.hasNext()) {
						Unit u = i.next();
						System.out.println(u);
						System.out.println(w.getLocalsToInputsBefore(u));
						System.out.println(w.getLocalsToInputsAfter(u) + "\n");
					}
				} else {
					InputBranchDependencyAnalysis analysis = new InputBranchDependencyAnalysis(graph);
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
	
	public static void main(String[] args) {
		String[] intraOptions = new String[8];
		intraOptions[0] = "-cp";
		intraOptions[1] = "build/tests/";
		intraOptions[2] = "-pp";
		intraOptions[3] = "-f";
		intraOptions[4] = "J";
		intraOptions[5] = "-keep-line-number";
		intraOptions[6] = "-coffi";
		intraOptions[7] = "edu.ntu.learn.feature.test.TestInputBranchDependency";
		Main.runIntra(intraOptions);
	}
	//reference: https://github.com/Sable/soot/tree/master/src/soot/jimple/toolkits/ide

}
