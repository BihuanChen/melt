package mlt;

public class Config {

	public enum Mode {RANDOM, SYSTEMATIC} 
	public enum Model {J48, NAIVEBAYES, LIBSVM, RANDOMFOREST}

	public static Mode MODE = Mode.SYSTEMATIC; // find an unexplored branch either randomly or systematically
	public static int MAX_ATTEMPTS = 3; // the maximum number of attempts to cover an unexplored branch
	public static Model CMODEL = Model.RANDOMFOREST;  // the applied classification model
	public static int TESTS_SIZE = 10; // the number of test cases that need to be generated at a time
	
	public static String PROJECTPATH = "src/tests/mlt/learn/test1/";
	public static String CLASSPATH = "src/tests/";
	public static String MAINCLASS = "mlt.learn.test1.TestAnalyzer";
	public static String ENTRY_METHOD = "void test(int,int,int)";
	
	public static String METHOD_NAME = Config.ENTRY_METHOD.substring(Config.ENTRY_METHOD.indexOf(" ") + 1, Config.ENTRY_METHOD.indexOf("("));
	@SuppressWarnings("rawtypes")
	public static Class[] CLS;
	
	static {
		String[] clsStr = Config.ENTRY_METHOD.substring(Config.ENTRY_METHOD.indexOf("(") + 1, Config.ENTRY_METHOD.indexOf(")")).split(",");
		int size = clsStr.length;
		CLS = new Class[size];
		for (int i = 0; i < size; i++) {
			if (clsStr[i].equals("byte")) {
				CLS[i] = byte.class;
			} else if (clsStr[i].equals("short")) {
				CLS[i] = short.class;
			} else if (clsStr[i].equals("int")) {
				CLS[i] = int.class;
			} else if (clsStr[i].equals("long")) {
				CLS[i] = long.class;
			} else if (clsStr[i].equals("boolean")) {
				CLS[i] = boolean.class;
			} else if (clsStr[i].equals("float")) {
				CLS[i] = float.class;
			} else if (clsStr[i].equals("double")) {
				CLS[i] = double.class;
			} else {
				System.err.println("[ml-testing] unsupported input type " + clsStr[i]);
			}
		}
	}
	
}
