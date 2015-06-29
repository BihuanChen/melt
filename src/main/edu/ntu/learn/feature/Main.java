package edu.ntu.learn.feature;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class Main {
	
	public static void runIntra (String[] args) {
		PackManager.v().getPack("jtp").add(new Transform("jtp.ibd", new BodyTransformer(){

			@Override
			protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
				UnitGraph g = new ExceptionalUnitGraph(body);
				//InputBranchDependencyWrapper w = new InputBranchDependencyWrapper(g);
				Iterator<Unit> i = g.iterator();
				while (i.hasNext()) {
					Unit u = i.next();
					System.out.println(u);
					//w.getInputBranchDependency(u);
					System.out.println("def: " + u.getDefBoxes());
					System.out.println("use: " + u.getUseBoxes() + "\n");
				}
			}
			
		}));
		soot.Main.main(args);
	}
	
	public static void main(String[] args) {
		String[] intraOptions = new String[6];
		intraOptions[0] = "-cp";
		intraOptions[1] = "build/tests/";
		intraOptions[2] = "-pp";
		intraOptions[3] = "-f";
		intraOptions[4] = "J";
		intraOptions[5] = "edu.ntu.learn.feature.test.TestInputBranchDependency";
		Main.runIntra(intraOptions);
	}
	//reference: https://github.com/Sable/soot/tree/master/src/soot/jimple/toolkits/ide

}
