package mlt;

public class Config {

	public enum Mode {RANDOM, SYSTEMATIC} 
	public enum Model {J48, NAIVEBAYES, LIBSVM, RANDOMFOREST}

	public static Mode MODE = Mode.SYSTEMATIC; // find an unexplored branch either randomly or systematically
	public static int MAX_ATTEMPTS = 3; // the maximum number of attempts to cover an unexplored branch
	public static Model CMODEL = Model.RANDOMFOREST;  // the applied classification model
	
	public static String PROJECTPATH = "src/tests/mlt/learn/test1/";
	public static String CLASSPATH = "src/tests/";
	public static String MAINCLASS = "mlt.learn.test1.TestAnalyzer";
	public static String ENTRY_METHOD = "void test(int,int,int)";
	
}
