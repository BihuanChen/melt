package mlt.learn;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import mlt.Config;
import mlt.test.Profiles;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

public class ConstraintLearner {

	private PredicateNode node;

	private Instances instances;
	private FilteredClassifier classifier;
			
	public ConstraintLearner(PredicateNode node) {
		this.node = node;
		
		classifier = new FilteredClassifier();
		if (Config.CMODEL == Config.Model.J48) {
			J48 j48 = new J48();
			classifier.setClassifier(j48);
		} else if (Config.CMODEL == Config.Model.NAIVEBAYES) {
			NaiveBayes nb = new NaiveBayes();
			classifier.setClassifier(nb);
		} else if (Config.CMODEL == Config.Model.LIBSVM) {
			LibSVM svm = new LibSVM();
			classifier.setClassifier(svm);
		} else { //model == Model.RandomForest
			RandomForest rf = new RandomForest();
			classifier.setClassifier(rf);
		}
	}
	
	public void buildInstancesAndClassifier(int numOfAttrs) throws Exception {
		if (instances == null) {
			FastVector attrs = new FastVector(numOfAttrs + 1);
			// declare the class attribute
			FastVector fvClassVal = new FastVector(2);
			fvClassVal.addElement("F");
			fvClassVal.addElement("T");
			Attribute classAttr = new Attribute("branch", fvClassVal);
			attrs.addElement(classAttr);
			// declare each input as an attribute
			HashSet<Integer> depInputs = Profiles.predicates.get(node.getPredicate()).getDepInputs();
			int[] delAttrs = new int[numOfAttrs - depInputs.size()];
			for (int i = 0, j = 0; i < numOfAttrs; i++) {
				Attribute attr = new Attribute("input_" + i, i);
				attrs.addElement(attr);
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
		// get new tests data
		PredicateArc tb = node.getSourceTrueBranch();
		PredicateArc fb = node.getSourceFalseBranch();
		List<Integer> tTests = tb.getTests().subList(tb.getOldSize(), tb.getTests().size());
		tb.setOldSize(tb.getTests().size());
		List<Integer> fTests = fb.getTests().subList(fb.getOldSize(), fb.getTests().size());
		fb.setOldSize(fb.getTests().size());
		boolean changed = tTests.size() == 0 && fTests.size() == 0 ? false : true;
		// load new tests data
		if (changed) {
			String type = Profiles.predicates.get(node.getPredicate()).getType();
			if (type.equals("if")) {
				Iterator<Integer> iterator = tTests.iterator();
				while (iterator.hasNext()) {
					createInstance(Profiles.tests.get(iterator.next()), "T");
				}
				iterator = fTests.iterator();
				while (iterator.hasNext()) {
					createInstance(Profiles.tests.get(iterator.next()), "F");
				}
			} else if (type.equals("for") || type.equals("do") || type.equals("while")) {
				Iterator<Integer> iterator = tTests.iterator();
				while (iterator.hasNext()) {
					createInstance(Profiles.tests.get(iterator.next()), "T");
				}
				iterator = fTests.iterator();
				while (iterator.hasNext()) {
					Integer i = iterator.next();
					if (!tTests.contains(i)) {
						createInstance(Profiles.tests.get(i), "F");
					}
				}
			} else {
				System.err.println("[ml-testing] unknown conditional statement");
			}
			// build the classifier if new tests data are available
			classifier.buildClassifier(instances);
			System.out.println(instances);
		}
	}
	
	public double classifiyInstance(Object[] values) throws Exception {
		int size = values.length;
		Instance instance = new Instance(size + 1);
		instance.setDataset(instances);
		for (int i = 0; i < size; i++) {
			instance.setValue(i + 1, (double)(int)values[i]);
		}		
		return classifier.classifyInstance(instance);
	}
	
	private void createInstance(Object[] values, String branch) {
		int size = values.length;
		Instance instance = new Instance(size + 1);
		instance.setDataset(instances);
		instance.setClassValue(branch);
		for (int i = 0; i < size; i++) {
			instance.setValue(i + 1, (double)(int)values[i]);
		}
		// add to instances after setting attribute values
		instances.add(instance);
	}

}
