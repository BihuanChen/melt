package melt.core;

import java.util.ArrayList;

public class PredicateArc {

	private PredicateNode source;
	private PredicateNode target;
	
	private ArrayList<Integer> triggerTests;
	private int index; // the starting index of the to-be-modeled tests for training 
	
	public PredicateArc(PredicateNode source, PredicateNode target) {
		this.source = source;
		this.target = target;
		this.index = 0;
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
	
	public ArrayList<Integer> getTriggerTests() {
		return triggerTests;
	}
	
	public void addToTriggerTests(int index) {
		if (triggerTests == null) {
			triggerTests = new ArrayList<Integer>();
		}
		triggerTests.add(index);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "PredicateArc [ source = " + source.getPredicate() + ", target = " + target.getPredicate()
				+ ", index = " + index + " ]"; // trigger tests = " + triggerTests + " ]";
	}
	
}
