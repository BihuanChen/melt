package edu.ntu.learn.feature;

import java.util.List;

import soot.Unit;

public interface InputBranchDependency {
	
	public List<?> getDependencyBefore(Unit u);
	public List<?> getDependencyAfter(Unit u);
	
}
