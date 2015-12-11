package mlt.test.generation;

import java.util.HashSet;

import mlt.learn.PathLearner;
import mlt.test.TestCase;

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
