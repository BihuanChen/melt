package mlt.learn;

import java.util.ArrayList;

public class PredicateNode {

	private int predicate;
	private ArrayList<Integer> testInputs = null;
	
	private PredicateNode child_t = null;
	private PredicateNode child_f = null;
	private PredicateNode parent = null;

	
	public PredicateNode() {
		this.predicate = -1;
	}

	public PredicateNode(int predicate) {
		this.predicate = predicate;
	}

	public int getPredicate() {
		return predicate;
	}

	public void setPredicate(int predicate) {
		this.predicate = predicate;
	}

	public ArrayList<Integer> getTestInputs() {
		return testInputs;
	}

	public void addTestInput(int index) {
		if (testInputs == null) {
			testInputs = new ArrayList<Integer>();
		}
		testInputs.add(index);
	}

	public PredicateNode getChild_t() {
		return child_t;
	}

	public void setChild_t(PredicateNode child_t) {
		this.child_t = child_t;
	}

	public PredicateNode getChild_f() {
		return child_f;
	}

	public void setChild_f(PredicateNode child_f) {
		this.child_f = child_f;
	}

	public PredicateNode getParent() {
		return parent;
	}

	public void setParent(PredicateNode parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "PredicateNode [ predicate = " + predicate + ", testInputs = " + testInputs
				+ ", child_t = " + (child_t == null ? "null" : child_t.getPredicate())
				+ ", child_f = " + (child_f == null ? "null" : child_f.getPredicate())
				+ ", parent = " + (parent == null ? "null" : parent.getPredicate()) + " ]";
	}

}
