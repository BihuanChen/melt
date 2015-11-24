package mlt.learn;

import java.util.HashSet;
import java.util.Iterator;

import mlt.Config;
import mlt.test.Profiles;
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
			
	public OneBranchLearner(PredicateNode node) throws Exception {
		this.node = node;
		
		classifier = new FilteredClassifier();
		LibSVM svm = new LibSVM();
		String[] options = Utils.splitOptions("-S 2 -K 3 -D 3 -G 0.1 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1"); // one-class svm, sigmoid
		svm.setOptions(options);
		classifier.setClassifier(svm);
		
		int size = Config.CLS.length;
		FastVector attrs = new FastVector(size + 1);
		// declare the class attribute
		FastVector fvClassVal = new FastVector(1);
		fvClassVal.addElement("IN");
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
	
	public void buildInstancesAndClassifier() throws Exception {
		// get new tests data
		PredicateArc tb = node.getSourceTrueBranch();
		HashSet<Integer> tTests = null;
		if (tb != null) {
			tTests = new HashSet<Integer>(tb.getTests().subList(tb.getOldSize(), tb.getTests().size()));
			tb.setOldSize(tb.getTests().size());
		}
		PredicateArc fb = node.getSourceFalseBranch();
		HashSet<Integer> fTests = null;
		if (fb != null) {
			fTests = new HashSet<Integer>(fb.getTests().subList(fb.getOldSize(), fb.getTests().size()));
			fb.setOldSize(fb.getTests().size());
		}
		boolean changed = (tTests != null && tTests.size() != 0) || (fTests != null && fTests.size() != 0);
		// load new tests data
		if (changed) {
			Iterator<Integer> iterator = tTests != null ? tTests.iterator() : fTests.iterator();
			while (iterator.hasNext()) {
				createInstance(Profiles.tests.get(iterator.next()));
			}
			// build the classifier if new tests data are available
			classifier.buildClassifier(instances);
			//System.out.println("[ml-testing] instances \n" + instances + "\n");			
		}
	}
	
	public double classifiyInstance(Object[] test) throws Exception {
		int size = test.length;
		Instance instance = new Instance(size + 1);
		instance.setDataset(instances);
		for (int i = 0; i < size; i++) {
			if (test[i] instanceof Boolean) {
				instance.setValue(i + 1, (boolean)test[i] ? "true" : "false");
			} else {
				instance.setValue(i + 1, convert(test[i]));
			}
		}
		return classifier.classifyInstance(instance);
	}
	
	private void createInstance(Object[] test) {
		int size = test.length;
		Instance instance = new Instance(size + 1);
		instance.setDataset(instances);
		instance.setClassValue("IN");
		for (int i = 0; i < size; i++) {
			if (test[i] instanceof Boolean) {
				instance.setValue(i + 1, (boolean)test[i] ? "true" : "false");
			} else {
				instance.setValue(i + 1, convert(test[i]));
			}
		}
		// add to instances after setting attribute values
		instances.add(instance);
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
