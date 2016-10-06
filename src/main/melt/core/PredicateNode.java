package melt.core;

import gov.nasa.jpf.constraints.api.Expression;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import melt.Config;
import melt.learn.OneBranchLearner;
import melt.learn.TwoBranchLearner;

public class PredicateNode {
	
	private int predicate; // -1 represents a leaf node, -2 represents a hidden node
	private int level; // the distance to the root node
	
	private PredicateArc sourceTrueBranch;
	private PredicateArc sourceFalseBranch;
	
	private LinkedList<PredicateArc> targetTrueBranches;
	private LinkedList<PredicateArc> targetFalseBranches;
	
	private int attempts;
	
	private OneBranchLearner oneBranchLearner;
	private TwoBranchLearner twoBranchLearner;
	
	private boolean covered = false;
	
	private LinkedHashMap<String, Expression<Boolean>> constraints;
	private int conIndex; // the starting index of the to-be-modeled constraints as features in trainting
	
	private HashSet<Integer> depInputs;
	private HashSet<Integer> notDepInputs;
	private boolean depDirty;
	
	public PredicateNode() {
		this.predicate = -1;
		this.level = -1;
		this.attempts = 0;
		this.conIndex = 0;
		this.depDirty = true;
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

	public LinkedList<PredicateArc> getTargetTrueBranches() {
		return targetTrueBranches;
	}

	public void addTargetTrueBranch(PredicateArc targetTrueBranch) {
		if (targetTrueBranches == null) {
			targetTrueBranches = new LinkedList<PredicateArc>();
		}
		targetTrueBranches.add(targetTrueBranch);
	}

	public LinkedList<PredicateArc> getTargetFalseBranches() {
		return targetFalseBranches;
	}

	public void addTargetFalseBranch(PredicateArc targetFalseBranch) {
		if (targetFalseBranches == null) {
			targetFalseBranches = new LinkedList<PredicateArc>();
		}
		targetFalseBranches.add(targetFalseBranch);
	}

	public int getAttempts() {
		return attempts;
	}

	public void incAttempts() {
		this.attempts++;
	}

	public TwoBranchLearner getTwoBranchesLearner() throws Exception {
		if (twoBranchLearner == null && getDepInputs() != null) {
			Predicate.TYPE type = Profile.predicates.get(predicate).getType();
			if (type == Predicate.TYPE.IF) {
				if (sourceTrueBranch != null && sourceFalseBranch != null && isIfLearnable()) {
					twoBranchLearner = new TwoBranchLearner(this);
				}
			} else {
				if (sourceTrueBranch != null && sourceFalseBranch != null && isLoopBodyNotExecuted()) {
					twoBranchLearner = new TwoBranchLearner(this);
				}
			}
		}
		return twoBranchLearner;
	}
	
	// the true and false branch may contain the same tests when if conditionals are in loops
	private boolean isIfLearnable() {
		HashSet<Integer> tt = new HashSet<Integer>(sourceTrueBranch.getTriggerTests());
		HashSet<Integer> ft = new HashSet<Integer>(sourceFalseBranch.getTriggerTests());
		
		int ttSize = tt.size();
		int ftSize = ft.size();
		tt.retainAll(ft);
		int size = tt.size();
		
		return size != ttSize && size != ftSize;
	}
	
	// if there exists a test that only executes the false branch of a loop
	private boolean isLoopBodyNotExecuted() {
		if (sourceTrueBranch.getTriggerTests() != null) { 	// may be null for do loops
			HashSet<Integer> tTests = new HashSet<Integer>(sourceTrueBranch.getTriggerTests());
			Iterator<Integer> iterator = sourceFalseBranch.getTriggerTests().iterator();
			while (iterator.hasNext()) {
				Integer i = iterator.next();
				if (!tTests.contains(i)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public OneBranchLearner getOneBranchLearner() throws Exception {
		if (!isCovered() && oneBranchLearner == null) {
			oneBranchLearner = new OneBranchLearner(this);
		}
		return oneBranchLearner;
	}

	public void setOneBranchLearner(OneBranchLearner oneBranchLearner) {
		this.oneBranchLearner = oneBranchLearner;
	}

	public boolean isCovered() {
		if (!covered) {
			Predicate.TYPE type = Profile.predicates.get(predicate).getType();
			if (type == Predicate.TYPE.IF) {
				if (sourceTrueBranch != null && sourceFalseBranch != null) {
					covered = true;
				}
			} else {
				if (sourceTrueBranch != null && sourceFalseBranch != null && isLoopBodyNotExecuted()) {
					covered = true;
				}
			}
		}
		return covered;
	}

	public LinkedHashMap<String, Expression<Boolean>> getConstraints() {
		return constraints;
	}

	public void addConstraint(String instID, Expression<Boolean> constraint) {
		if (constraints == null) {
			constraints = new LinkedHashMap<String, Expression<Boolean>>();
		}
		// TODO more possibilities to update constraints?
		if (constraints.get(instID) == null) {
			constraints.put(instID, constraint);
		}
	}

	public int getConIndex() {
		return conIndex;
	}

	public void setConIndex(int conIndex) {
		this.conIndex = conIndex;
	}

	public HashSet<Integer> getDepInputs() {
		return depInputs;
	}
	
	public HashSet<Integer> getNotDepInputs() {
		return notDepInputs;
	}

	public void addToDepInputs(HashSet<Integer> newDepInputs) {
		if (newDepInputs == null) {
			return;
		}
		if (depInputs == null) {
			depInputs = new HashSet<Integer>();
		}
		if (notDepInputs == null) {
			notDepInputs = new HashSet<Integer>();
			for (int i = 0; i < Config.CLS.length; i++) {
				notDepInputs.add(i);
			}
		}
		Iterator<Integer> iterator = newDepInputs.iterator();
		while (iterator.hasNext()) {
			Integer i = iterator.next();
			boolean exist = depInputs.add(i);
			if (exist) {
				notDepInputs.remove(i);
				depDirty = true;
			}
		}
	}

	public boolean isDepDirty() {
		return depDirty;
	}

	public void setDepDirty(boolean depDirty) {
		this.depDirty = depDirty;
	}

	@Override
	public String toString() {
		return "PredicateNode [ predicate = " + predicate + ", level = " + level + ", attempts = " + attempts + 
				", sourceTrueBranch = " + sourceTrueBranch + ", sourceFalseBranch = " + sourceFalseBranch + 
				", targetTrueBranches = " + targetTrueBranches + ", targetFalseBranches = " + targetFalseBranches + 
				", constraints = " + constraints + ", depInputs = " + depInputs + ", notDepInputs = " + notDepInputs + " ]";
	}

}
