package mlt.test;

import mlt.learn.PathLearner;

public class TestGenerator {

	private PathLearner pathLearner;

	public TestGenerator(PathLearner pathLearner) {
		this.pathLearner = pathLearner;
	}
	
	public Object[] generate(@SuppressWarnings("rawtypes") Class[] cls) throws Exception {
		while (true) {
			Object[] test = new Object[cls.length];
			//TODO random generation
			if (pathLearner.isValidTest(test)) {
				return test;
			}
		}
	}

}
