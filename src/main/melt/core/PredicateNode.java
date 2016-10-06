package melt.core;

import gov.nasa.jpf.constraints.api.Expression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import melt.Config;
import melt.learn.OneBranchLearner;
import melt.learn.TwoBranchesLearner;
import melt.test.Profiles;

public class PredicateNode {
	
	private int predicate; // -1 represents a leaf node, -2 represents a hidden node
	private int level; // the distance to the root node
	
	private PredicateArc sourceTrueBranch;
	private PredicateArc sourceFalseBranch;
	
	private ArrayList<PredicateArc> targetTrueBranches;
	private ArrayList<PredicateArc> targetFalseBranches;
	
	private int attempts;
	
	private TwoBranchesLearner twoBranchesLearner;
	private OneBranchLearner oneBranchLearner;
	
	private boolean covered = false;
	
	private LinkedHashMap<String, Expression<Boolean>> constraints;
	private int oldConSize;
	
	private HashSet<Integer> depInputs;
	private HashSet<Integer> notDepInputs;
	private boolean depDirty;
	
	public PredicateNode() {
		this.predicate = -1;
		this.level = -1;
		this.attempts = 0;
		this.oldConSize = 0;
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

	public TwoBranchesLearner getTwoBranchesLearner() throws Exception {
		if (twoBranchesLearner == null && getDepInputs() != null) {
			Predicate.TYPE type = Profiles.predicates.get(predicate).getType();
			if (type == Predicate.TYPE.IF) {
				if (sourceTrueBranch != null && sourceFalseBranch != null && isIfConditionLearnable()) {
					twoBranchesLearner = new TwoBranchesLearner(this);
				}
			} else if (type == Predicate.TYPE.FOR || type == Predicate.TYPE.FOREACH || type == Predicate.TYPE.DO || type == Predicate.TYPE.WHILE) {
				if (sourceTrueBranch != null && sourceFalseBranch != null && isLoopBodyNotExecuted()) {
					twoBranchesLearner = new TwoBranchesLearner(this);
				}
			} else {
				System.err.println("[melt] unknown conditional statement");
			}
		}
		return twoBranchesLearner;
	}
	
	public OneBranchLearner getOneBranchLearner() throws Exception {
		if (!covered && oneBranchLearner == null) {
			oneBranchLearner = new OneBranchLearner(this);
		}
		return oneBranchLearner;
	}

	public void setOneBranchLearner(OneBranchLearner oneBranchLearner) {
		this.oneBranchLearner = oneBranchLearner;
	}

	public boolean isCovered() {
		if (!covered) {
			Predicate.TYPE type = Profiles.predicates.get(predicate).getType();
			if (type == Predicate.TYPE.IF) {
				if (sourceTrueBranch != null && sourceFalseBranch != null) {
					covered = true;
				}
			} else if (type == Predicate.TYPE.FOR || type == Predicate.TYPE.FOREACH || type == Predicate.TYPE.DO || type == Predicate.TYPE.WHILE) {
				if (sourceTrueBranch != null && sourceFalseBranch != null && isLoopBodyNotExecuted()) {
					covered = true;
				}
			} else {
				System.err.println("[melt] unknown conditional statement");
			}
		}
		return covered;
	}

	private boolean isLoopBodyNotExecuted() {
		// may be null for do loops
		if (sourceTrueBranch.getTriggerTests() != null) {
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
	
	private boolean isIfConditionLearnable() {
		ArrayList<Integer> tt = new ArrayList<Integer>(sourceTrueBranch.getTriggerTests());
		ArrayList<Integer> ft = new ArrayList<Integer>(sourceFalseBranch.getTriggerTests());
		
		int ttSize = tt.size();
		int ftSize = ft.size();
		tt.retainAll(ft);
		int size = tt.size();
		
		return size != ttSize && size != ftSize;
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

	public int getOldConSize() {
		return oldConSize;
	}

	public void setOldConSize(int oldConSize) {
		this.oldConSize = oldConSize;
	}

	public HashSet<Integer> getDepInputs() {
		return depInputs;
	}
	
	public HashSet<Integer> getNotDepInputs() {
		return notDepInputs;
	}

	// only called when using dynamic taint analysis
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
