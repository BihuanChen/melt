package melt.test.generation;

import java.util.HashSet;

import melt.learn.PathLearner;
import melt.test.TestCase;

public abstract class TestGenerator {

	protected PathLearner pathLearner;

	public TestGenerator(PathLearner pathLearner) {
		this.pathLearner = pathLearner;
	}
	
	public abstract HashSet<TestCase> generate() throws Exception;

	public PathLearner getPathLearner() {
		return pathLearner;
	}

	public void setPathLearner(PathLearner pathLearner) {
		this.pathLearner = pathLearner;
	}
	
}
