package melt.learn;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import melt.Config;
import melt.core.Predicate;
import melt.core.PredicateArc;
import melt.core.PredicateNode;
import melt.core.Profile;
import melt.test.util.TestCase;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.unsupervised.attribute.Remove;

public class TwoBranchLearner {

	private PredicateNode node;

	private Instances instances;
	private FilteredClassifier classifier;
	
	private ArrayList<Integer> tests; // the id of tests that correspond to instances
		
	public TwoBranchLearner(PredicateNode node) throws Exception {
		this.node = node;
		this.node.setConIndex(0);
		
		this.node.getSourceTrueBranch().setIndex(0);
		this.node.getSourceFalseBranch().setIndex(0);
		this.node.setOneBranchLearner(null);
		
		classifier = new FilteredClassifier();
		String[] options;
		if (Config.MODEL == Config.Model.J48) {
			J48 j48 = new J48();
			classifier.setClassifier(j48);
		} else if (Config.MODEL == Config.Model.NAIVEBAYES) {
			NaiveBayes nb = new NaiveBayes();
			classifier.setClassifier(nb);
		} else if (Config.MODEL == Config.Model.LIBSVM) {
			LibSVM svm = new LibSVM();
			options = Utils.splitOptions("-K 1"); // polynomial
			svm.setOptions(options);
			classifier.setClassifier(svm);
		} else { //model == Model.RandomForest
			RandomForest rf = new RandomForest();
			classifier.setClassifier(rf);
		}
	}
	
	public void buildInstancesAndClassifier() throws Exception {
		// create or update instances
		boolean changed1 = setupInstances();
		// set the classifier filter
		boolean changed3 = false;
		if (node.isDepDirty()) {
			int size = node.getNotDepInputs().size();
			int[] index = new int[size];
			Iterator<Integer> iterator = node.getNotDepInputs().iterator();
			int i = 0;
			while (iterator.hasNext()) {
				index[i++] = iterator.next() + 1;
			}
			Remove rm = new Remove();			
			rm.setAttributeIndicesArray(index);
			classifier.setFilter(rm);
			
			node.setDepDirty(false);
			changed3 = true;
		}
		// decide if new tests data needs to be loaded (in a lazy manner)
		PredicateArc tb = node.getSourceTrueBranch();
		PredicateArc fb = node.getSourceFalseBranch();
		boolean changed2 = false;
		if (changed1 || changed3 || (tb.getIndex() == 0 && fb.getIndex() == 0) || 
				((tb.getTriggerTests().size() - tb.getIndex()) > Config.LEARN_THRESHOLD || (fb.getTriggerTests().size() - fb.getIndex()) > Config.LEARN_THRESHOLD)) {
			changed2 = true;
		}
		// load new tests data
		if (changed2) {
			HashSet<Integer> tTests = new HashSet<Integer>(tb.getTriggerTests().subList(tb.getIndex(), tb.getTriggerTests().size()));
			tb.setIndex(tb.getTriggerTests().size());
			HashSet<Integer> fTests = new HashSet<Integer>(fb.getTriggerTests().subList(fb.getIndex(), fb.getTriggerTests().size()));
			fb.setIndex(fb.getTriggerTests().size());
			
			Predicate.TYPE type = Profile.predicates.get(node.getPredicate()).getType();
			if (type == Predicate.TYPE.IF) {
				Iterator<Integer> iterator = tTests.iterator();
				while (iterator.hasNext()) {
					Integer i = iterator.next();
					if (!fTests.contains(i)) {
						createInstance(i, Profile.tests.get(i), "T");
					} else {
						fTests.remove(i);
					}
				}
				iterator = fTests.iterator();
				while (iterator.hasNext()) {
					Integer i = iterator.next();
					createInstance(i, Profile.tests.get(i), "F");
				}
			} else if (type == Predicate.TYPE.FOR || type == Predicate.TYPE.FOREACH || type == Predicate.TYPE.DO || type == Predicate.TYPE.WHILE) {
				Iterator<Integer> iterator = tTests.iterator();
				while (iterator.hasNext()) {
					Integer i = iterator.next();
					createInstance(i, Profile.tests.get(i), "T");
				}
				iterator = fTests.iterator();
				while (iterator.hasNext()) {
					Integer i = iterator.next();
					if (!tTests.contains(i)) {
						createInstance(i, Profile.tests.get(i), "F");
					}
				}
			} else {
				System.err.println("[melt] unknown conditional statement");
			}			
		}
		
		if (changed1 || changed2 || changed3) {
			// build the classifier if instances are changed
			classifier.buildClassifier(instances);
			//System.out.println("[melt] instances \n" + classifier + "\n");
		}
	}
	
	public double[] classifiyInstance(TestCase testCase) throws Exception {
		Object[] test = testCase.getTest();
		int size = test.length;
		LinkedHashMap<String, Expression<Boolean>> constraints = node.getConstraints();
		if (constraints != null) {
			size += constraints.size();
		}
		Instance instance = new Instance(size + 1);
		instance.setDataset(instances);
		// set attributes about inputs
		for (int i = 0; i < test.length; i++) {
			if (test[i] instanceof Boolean) {
				instance.setValue(i + 1, (boolean)test[i] ? "true" : "false");
			} else {
				instance.setValue(i + 1, convert(test[i]));
			}
		}
		// set attributes about constraints
		if (constraints != null) {
			Iterator<String> iterator = constraints.keySet().iterator();
			int counter = 0;
			while (iterator.hasNext()) {
				boolean b = constraints.get(iterator.next()).evaluate(testCase.getValuation());
				instance.setValue(test.length + 1 + counter, b ? "true" : "false");
				counter++;
			}
		}
		// classify the instance
		return classifier.distributionForInstance(instance);
		//return classifier.classifyInstance(instance);
	}
	
	private boolean setupInstances() {
		if (instances == null) {
			tests = new ArrayList<Integer>();
			int size = Config.CLS.length;
			FastVector attrs = new FastVector(size + 1);
			// declare the class attribute
			FastVector fvClassVal = new FastVector(2);
			fvClassVal.addElement("F");
			fvClassVal.addElement("T");
			Attribute classAttr = new Attribute("branch", fvClassVal);
			attrs.addElement(classAttr);
			// declare each input as an attribute
			for (int i = 0; i < size; i++) {
				if (Config.CLS[i] == boolean.class) {
					FastVector fv = new FastVector(2);
					fv.addElement("false");
					fv.addElement("true");
					attrs.addElement(new Attribute("input_" + i, fv, i));
				} else {
					attrs.addElement(new Attribute("input_" + i, i));
				}
			}
			// initialize data
			instances = new Instances("", attrs, 0);
			instances.setClassIndex(0);
		}
		// add new attributes and update instances
		boolean flag = false;
		LinkedHashMap<String, Expression<Boolean>> constraints = node.getConstraints();
		if (constraints != null && constraints.size() > node.getConIndex()) {
			Iterator<String> iterator = constraints.keySet().iterator();
			int counter = 0;
			ArrayList<Expression<Boolean>> newConstraints = new ArrayList<Expression<Boolean>>();
			// add new attributes
			while (iterator.hasNext()) {
				String id = iterator.next();
				if (counter >= node.getConIndex()) {
					newConstraints.add(constraints.get(id));
					FastVector fv = new FastVector(2);
					fv.addElement("false");
					fv.addElement("true");
					instances.insertAttributeAt(new Attribute("cons_" + counter, fv), instances.numAttributes());
				}
				counter++;
			}
			// update instances
			for (int i = 0; i < instances.numInstances(); i++) {
				for (int j = 0; j < newConstraints.size(); j++) {
					flag = true;
					boolean b = newConstraints.get(j).evaluate(Profile.tests.get(tests.get(i)).getValuation());
					instances.instance(i).setValue(instances.numAttributes() - newConstraints.size() + j, b ? "true" : "false");
				}
			}
			node.setConIndex(constraints.size());
		}
		return flag;
	}
	
	private void createInstance(int id, TestCase testCase, String branch) {
		Object[] test = testCase.getTest();
		int size = test.length;
		LinkedHashMap<String, Expression<Boolean>> constraints = node.getConstraints();
		if (constraints != null) {
			size += constraints.size();
		}
		Instance instance = new Instance(size + 1);
		instance.setDataset(instances);
		instance.setClassValue(branch);
		// set attributes about inputs
		for (int i = 0; i < test.length; i++) {
			if (test[i] instanceof Boolean) {
				instance.setValue(i + 1, (boolean)test[i] ? "true" : "false");
			} else {
				instance.setValue(i + 1, convert(test[i]));
			}
		}
		// set attributes about constraints
		if (constraints != null) {
			Iterator<String> iterator = constraints.keySet().iterator();
			int counter = 0;
			while (iterator.hasNext()) {
				Expression<Boolean> exp = constraints.get(iterator.next());
				Valuation val = testCase.getValuation();
				boolean b = exp.evaluate(val);
				instance.setValue(test.length + 1 + counter, b ? "true" : "false");
				counter++;
			}
		}
		// add to instances after setting attribute values
		instances.add(instance);
		tests.add(id);
	}
	
	private double convert(Object obj) {
		if (obj instanceof Byte) {
			return (double)(byte)obj;
		} else if (obj instanceof Short) {
			return (double)(short)obj;
		} else if (obj instanceof Character) {
			return (double)(char)obj;
		} else if (obj instanceof Integer) {
			return (double)(int)obj;
		} else if (obj instanceof Long) {
			return (double)(long)obj;
		} else if (obj instanceof Float) {
			return (double)(float)obj;
		} else {// if (obj instanceof Double) {
			return (double)obj;
		}
	}

	public Classifier getClassifier() {
		return classifier.getClassifier();
	}

}
