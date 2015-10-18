package mlt.learn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import mlt.test.Profiles;

public class PredicateNode {

	private int predicate; // -1 represents a leaf node
	private int level; // the distance to the root node
	
	private PredicateArc sourceTrueBranch;
	private PredicateArc sourceFalseBranch;
	
	private ArrayList<PredicateArc> targetTrueBranches;
	private ArrayList<PredicateArc> targetFalseBranches;
	
	private int attempts;
	
	private BranchLearner learner;
	
	private boolean covered = false;
	
	public PredicateNode() {
		this.predicate = -1;
		this.level = -1;
		this.attempts = 0;
	}

	public int getPredicate() {
		return predicate;
	}

	public void setPredicate(int predicate) {
		this.predicate = predicate;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public PredicateArc getSourceTrueBranch() {
		return sourceTrueBranch;
	}

	public void setSourceTrueBranch(PredicateArc sourceTrueBranch) {
		this.sourceTrueBranch = sourceTrueBranch;
	}

	public PredicateArc getSourceFalseBranch() {
		return sourceFalseBranch;
	}

	public void setSourceFalseBranch(PredicateArc sourceFalseBranch) {
		this.sourceFalseBranch = sourceFalseBranch;
	}

	public ArrayList<PredicateArc> getTargetTrueBranches() {
		return targetTrueBranches;
	}

	public void addTargetTrueBranch(PredicateArc targetTrueBranch) {
		if (targetTrueBranches == null) {
			targetTrueBranches = new ArrayList<PredicateArc>();
		}
		targetTrueBranches.add(targetTrueBranch);
	}

	public ArrayList<PredicateArc> getTargetFalseBranches() {
		return targetFalseBranches;
	}

	public void addTargetFalseBranch(PredicateArc targetFalseBranch) {
		if (targetFalseBranches == null) {
			targetFalseBranches = new ArrayList<PredicateArc>();
		}
		targetFalseBranches.add(targetFalseBranch);
	}

	public int getAttempts() {
		return attempts;
	}

	public void incAttempts() {
		this.attempts++;
	}

	public BranchLearner getLearner() {
		if (learner == null && Profiles.predicates.get(predicate).getDepInputs() != null) {
			String type = Profiles.predicates.get(predicate).getType();
			if (type.equals("if")) {
				if (sourceTrueBranch != null && sourceFalseBranch != null && isIfConditionLearnable()) {
					learner = new BranchLearner(this);
				}
			} else if (type.equals("for") || type.equals("do") || type.equals("while")) {
				if (sourceTrueBranch != null && sourceFalseBranch != null && isLoopBodyNotExecuted()) {
					learner = new BranchLearner(this);
				}
			} else {
				System.err.println("[ml-testing] unknown conditional statement");
			}
		}
		return learner;
	}
	
	public boolean isCovered() {
		if (!covered) {
			String type = Profiles.predicates.get(predicate).getType();
			if (type.equals("if")) {
				if (sourceTrueBranch != null && sourceFalseBranch != null) {
					covered = true;
				}
			} else if (type.equals("for") || type.equals("do") || type.equals("while")) {
				if (sourceTrueBranch != null && sourceFalseBranch != null && isLoopBodyNotExecuted()) {
					covered = true;
				}
			} else {
				System.err.println("[ml-testing] unknown conditional statement");
			}
		}
		return covered;
	}

	private boolean isLoopBodyNotExecuted() {
		HashSet<Integer> tTests = new HashSet<Integer>(sourceTrueBranch.getTests());
		Iterator<Integer> iterator = sourceFalseBranch.getTests().iterator();
		while (iterator.hasNext()) {
			Integer i = iterator.next();
			if (!tTests.contains(i)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isIfConditionLearnable() {
		ArrayList<Integer> tt = new ArrayList<Integer>(sourceTrueBranch.getTests());
		ArrayList<Integer> ft = new ArrayList<Integer>(sourceFalseBranch.getTests());
		
		int ttSize = tt.size();
		int ftSize = ft.size();
		tt.retainAll(ft);
		int size = tt.size();
		
		return size != ttSize && size != ftSize;
	}
	
	@Override
	public String toString() {
		return "PredicateNode [ predicate = " + predicate + ", level = " + level + ", attempts = " + attempts + 
				", sourceTrueBranch = " + sourceTrueBranch + ", sourceFalseBranch = " + sourceFalseBranch + 
				", targetTrueBranches = " + targetTrueBranches + ", targetFalseBranches = " + targetFalseBranches + " ]";
	}

}
