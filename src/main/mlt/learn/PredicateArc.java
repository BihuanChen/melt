package mlt.learn;

import java.util.ArrayList;

public class PredicateArc {

	private PredicateNode source;
	private PredicateNode target;
	private ArrayList<Integer> testInputs;
	
	public PredicateArc(PredicateNode source, PredicateNode target) {
		this.source = source;
		this.target = target;
	}

	public PredicateNode getSource() {
		return source;
	}
	
	public void setSource(PredicateNode source) {
		this.source = source;
	}
	
	public PredicateNode getTarget() {
		return target;
	}
	
	public void setTarget(PredicateNode target) {
		this.target = target;
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

	@Override
	public String toString() {
		return "PredicateArc [ source = " + source.getPredicate()
				+ ", target = " + target.getPredicate()
				+ ", testInputs = " + testInputs + " ]";
	}
	
}
