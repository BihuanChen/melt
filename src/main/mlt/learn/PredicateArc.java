package mlt.learn;

import java.util.ArrayList;

public class PredicateArc {

	private PredicateNode source;
	private PredicateNode target;
	private ArrayList<Integer> tests;
	
	private int oldSize;
	
	public PredicateArc(PredicateNode source, PredicateNode target) {
		this.source = source;
		this.target = target;
		this.oldSize = 0;
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
	
	public ArrayList<Integer> getTests() {
		return tests;
	}
	
	public void addTest(int index) {
		if (tests == null) {
			tests = new ArrayList<Integer>();
		}
		tests.add(index);
	}

	public int getOldSize() {
		return oldSize;
	}

	public void setOldSize(int oldSize) {
		this.oldSize = oldSize;
	}

	@Override
	public String toString() {
		return "PredicateArc [ source = " + source.getPredicate() + ", target = " + target.getPredicate()
				+ ", oldSize = " + oldSize + ", tests = " + tests + " ]";
	}
	
}
