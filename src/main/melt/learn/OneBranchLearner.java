package melt.learn;

import gov.nasa.jpf.constraints.api.Expression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import melt.Config;
import melt.test.Profiles;
import melt.test.TestCase;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.unsupervised.attribute.Remove;

public class OneBranchLearner {

	private PredicateNode node;

	private Instances instances;
	private FilteredClassifier classifier;
			
	private ArrayList<Integer> tests; // the id of tests that correspond to instances
	
	public OneBranchLearner(PredicateNode node) throws Exception {
		this.node = node;
		
		classifier = new FilteredClassifier();
		LibSVM svm = new LibSVM();
		String[] options = Utils.splitOptions("-S 2 -K 2 -D 3 -G 0.5 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1"); // one-class svm, radial basis
		svm.setOptions(options);
		classifier.setClassifier(svm);
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
		if (changed1 || changed3 || (tb != null && tb.getOldSize() == 0) || (fb != null && fb.getOldSize() == 0) || 
				(tb != null && (tb.getTests().size() - tb.getOldSize()) > Config.LEARN_THRESHOLD) || (fb !=null && (fb.getTests().size() - fb.getOldSize()) > Config.LEARN_THRESHOLD)) {
			changed2 = true;
		}
		// load new tests data
		if (changed2) {
			HashSet<Integer> tTests = null;
			if (tb != null) {
				tTests = new HashSet<Integer>(tb.getTests().subList(tb.getOldSize(), tb.getTests().size()));
				tb.setOldSize(tb.getTests().size());
			}
			HashSet<Integer> fTests = null;
			if (fb != null) {
				fTests = new HashSet<Integer>(fb.getTests().subList(fb.getOldSize(), fb.getTests().size()));
				fb.setOldSize(fb.getTests().size());
			}
						
			Iterator<Integer> iterator = tTests != null ? tTests.iterator() : fTests.iterator();
			while (iterator.hasNext()) {
				Integer i = iterator.next();
				createInstance(i, Profiles.tests.get(i));
			}			
		}
		if (changed1 || changed2 || changed3) {
			// build the classifier if new tests data are available
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
			FastVector fvClassVal = new FastVector(1);
			fvClassVal.addElement("IN");
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
		if (constraints != null && constraints.size() > node.getOldConSize()) {
			Iterator<String> iterator = constraints.keySet().iterator();
			int counter = 0;
			ArrayList<Expression<Boolean>> newConstraints = new ArrayList<Expression<Boolean>>();
			// add new attributes
			while (iterator.hasNext()) {
				String id = iterator.next();
				if (counter >= node.getOldConSize()) {
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
			node.setOldConSize(constraints.size());
		}
		return flag;
	}
	
	private void createInstance(int id, TestCase testCase) {
		Object[] test = testCase.getTest();
		int size = test.length;
		LinkedHashMap<String, Expression<Boolean>> constraints = node.getConstraints();
		if (constraints != null) {
			size += constraints.size();
		}
		Instance instance = new Instance(size + 1);
		instance.setDataset(instances);
		instance.setClassValue("IN");
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
				String str = iterator.next();
				System.out.println(constraints.get(str));
				System.out.println(testCase.getValuation());
				boolean b = constraints.get(str).evaluate(testCase.getValuation());
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
