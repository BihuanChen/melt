package edu.ntu.learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Cobweb;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddCluster;

public class PathClusterer {
	
	private Instances instances;
	private Cobweb cw;
	private AddCluster add;
	
	public void buildClusterer(ArrayList<String> attributes) throws Exception {
		FastVector attrs = new FastVector();
		Iterator<String> iterator = attributes.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			Attribute attr = new Attribute(iterator.next(), index++);
			attrs.addElement(attr);
		}
		instances = new Instances("", attrs, 0);		
		cw = new Cobweb();
		cw.setAcuity(1.0);
		cw.setCutoff(0.045);
		cw.setSeed(42);
		cw.buildClusterer(instances);
		add = new AddCluster();
	}
	
	public void updateClusterer(int[] values) throws Exception {
		Instance newInstance = createInstance(values);
		newInstance.setDataset(instances);
		instances.add(newInstance);
		cw.updateClusterer(newInstance);
	}
	
	public HashMap<String, HashSet<Integer>> updateFinished() throws Exception {
		cw.updateFinished();
		System.out.println("\n" + cw.toString());
		
		add.setInputFormat(instances);
		add.setClusterer(cw);
		Instances clusteredInsts = Filter.useFilter(instances, add);
		System.out.println(clusteredInsts);
		
		return getAssignments(clusteredInsts);
	}
	
	public void evaluateClusterer() throws Exception {
		ClusterEvaluation eval = new ClusterEvaluation();
		eval.setClusterer(cw);
		eval.evaluateClusterer(instances);
		System.out.println(eval.clusterResultsToString());
	}
	
	private Instance createInstance(int[] values) {
		Instance instance = new Instance(values.length);
		for (int i = 0; i < values.length; i++) {
			instance.setValue(i, values[i]);
		}
		return instance;
	}
	
	private HashMap<String, HashSet<Integer>> getAssignments(Instances clusteredInsts) {
		int num = clusteredInsts.numInstances();
		Attribute attr = clusteredInsts.attribute("cluster");
		HashMap<String, HashSet<Integer>> assignments = new HashMap<String, HashSet<Integer>>();
		for (int i = 0; i < num; i++) {
			String cluster = clusteredInsts.instance(i).stringValue(attr);
			if (assignments.get(cluster) == null) {
				assignments.put(cluster, new HashSet<Integer>());
			}
			assignments.get(cluster).add(i);
		}
		return assignments;
	}
	
	public static void main(String[] args) throws Exception {
		ArrayList<String> attrs = new ArrayList<String>();
		attrs.add("outlook");
		attrs.add("temperature");
		attrs.add("humidity");
		attrs.add("windy");
		
		int[][] insts = {{1,85,85,0}, {1,80,90,1}, {2,83,86,0}, {3,70,96,0}, {3,68,80,0}, {3,65,70,1}, {2,64,65,1}, 
						 {1,72,95,0}, {1,69,70,0}, {3,75,80,0}, {1,75,70,1}, {2,72,90,1}, {2,81,75,0}, {3,71,91,1}};
		
		PathClusterer pc = new PathClusterer();
		pc.buildClusterer(attrs);
		for (int i = 0; i < insts.length; i++) {
			pc.updateClusterer(insts[i]);
			pc.updateFinished();
		}
	}

}
