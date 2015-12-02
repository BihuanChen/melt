package mlt.learn;

import gov.nasa.jpf.constraints.api.Expression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import mlt.Config;
import mlt.test.Profiles;
import mlt.test.TestCase;
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

public class TwoBranchesLearner {

	private PredicateNode node;

	private Instances instances;
	private FilteredClassifier classifier;
	
	private ArrayList<Integer> tests; // the id of tests that correspond to instances
		
	public TwoBranchesLearner(PredicateNode node) throws Exception {
		this.node = node;
		this.node.setOldSize(0);
		
		this.node.getSourceTrueBranch().setOldSize(0);
		this.node.getSourceFalseBranch().setOldSize(0);
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
		// get new tests data
		PredicateArc tb = node.getSourceTrueBranch();
		PredicateArc fb = node.getSourceFalseBranch();
		HashSet<Integer> tTests = new HashSet<Integer>(tb.getTests().subList(tb.getOldSize(), tb.getTests().size()));
		tb.setOldSize(tb.getTests().size());
		HashSet<Integer> fTests = new HashSet<Integer>(fb.getTests().subList(fb.getOldSize(), fb.getTests().size()));
		fb.setOldSize(fb.getTests().size());
		boolean changed2 = tTests.size() == 0 && fTests.size() == 0 ? false : true;
		// load new tests data
		if (changed2) {
			String type = Profiles.predicates.get(node.getPredicate()).getType();
			if (type.equals("if")) {
				Iterator<Integer> iterator = tTests.iterator();
				while (iterator.hasNext()) {
					Integer i = iterator.next();
					if (!fTests.contains(i)) {
						createInstance(i, Profiles.tests.get(i), "T");
					} else {
						fTests.remove(i);
					}
				}
				iterator = fTests.iterator();
				while (iterator.hasNext()) {
					Integer i = iterator.next();
					createInstance(i, Profiles.tests.get(i), "F");
				}
			} else if (type.equals("for") || type.equals("do") || type.equals("while")) {
				Iterator<Integer> iterator = tTests.iterator();
				while (iterator.hasNext()) {
					Integer i = iterator.next();
					createInstance(i, Profiles.tests.get(i), "T");
				}
				iterator = fTests.iterator();
				while (iterator.hasNext()) {
					Integer i = iterator.next();
					if (!tTests.contains(i)) {
						createInstance(i, Profiles.tests.get(i), "F");
					}
				}
			} else {
				System.err.println("[ml-testing] unknown conditional statement");
			}			
		}
		if (changed1 || changed2) {
			// build the classifier if instances are changed
			classifier.buildClassifier(instances);
			//System.out.println("[ml-testing] instances \n" + instances + "\n");
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
			HashSet<Integer> depInputs = Profiles.predicates.get(node.getPredicate()).getDepInputs();
			int[] delAttrs = new int[size - depInputs.size()];
			for (int i = 0, j = 0; i < size; i++) {
				if (Config.CLS[i] == boolean.class) {
					FastVector fv = new FastVector(2);
					fv.addElement("false");
					fv.addElement("true");
					attrs.addElement(new Attribute("input_" + i, fv, i));
				} else {
					attrs.addElement(new Attribute("input_" + i, i));
				}
				if (!depInputs.contains(i)) {
					delAttrs[j++] = i + 1;
				}
			}
			// initialize data
			instances = new Instances("", attrs, 0);
			instances.setClassIndex(0);
			// initial the filter
			Remove rm = new Remove();
			rm.setAttributeIndicesArray(delAttrs);
			classifier.setFilter(rm);
		}
		// add new attributes and update instances
		boolean flag = false;
		LinkedHashMap<String, Expression<Boolean>> constraints = node.getConstraints();
		if (constraints != null && constraints.size() > node.getOldSize()) {
			Iterator<String> iterator = constraints.keySet().iterator();
			int counter = 0;
			ArrayList<Expression<Boolean>> newConstraints = new ArrayList<Expression<Boolean>>();
			// add new attributes
			while (iterator.hasNext()) {
				String id = iterator.next();
				if (counter >= node.getOldSize()) {
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
					boolean b = newConstraints.get(j).evaluate(Profiles.tests.get(tests.get(i)).getValuation());
					instances.instance(i).setValue(instances.numAttributes() - newConstraints.size() + j, b ? "true" : "false");
				}
			}
			node.setOldSize(constraints.size());
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
				boolean b = constraints.get(iterator.next()).evaluate(testCase.getValuation());
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
