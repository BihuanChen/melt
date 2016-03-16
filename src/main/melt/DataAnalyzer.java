package melt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import melt.instrument.Instrumenter;
import melt.learn.ProfileAnalyzer;
import melt.test.Profiles;
import melt.test.TestCase;
import melt.test.run.TestRunnerClient;

public class DataAnalyzer {

	public static void extractTestsFromCELogs() throws IOException, ClassNotFoundException {
		String program = "Gammq";
		String algo = "CT";
		File file = new File("/media/bhchen/Data/data/melt/" + program + "/" + algo + "/results-1");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;

		ArrayList<TestCase> tests = new ArrayList<TestCase>();
		int count = 0;
		while ((line = reader.readLine()) != null) {
			if (line.contains("th run")) {
				if (count > 0) {
					System.out.println("the " + count + " th run");
					ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File(Config.MAINCLASS + ".pred")));
					Profiles.predicates.addAll(((Instrumenter)oin.readObject()).getPredicates());
					oin.close();
					
					ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(new File("/media/bhchen/Data/data/melt/" + program + "/" + algo + "/tests-" + count)));
					oout.writeObject(tests);
					oout.close();
				
					TestRunnerClient runner = new TestRunnerClient(true);
					ProfileAnalyzer analyzer = new ProfileAnalyzer();

					for (int i = 0; i < tests.size(); i++) {
						runner.run(tests.get(i).getTest());
						Profiles.tests.add(tests.get(i));
						analyzer.update();
					}
					analyzer.coverage(null);
				
					MELT.computeMutationScore(tests);
					tests.clear();
					
					Profiles.predicates.clear();
					Profiles.tests.clear();
				}
				count++;
			}
			if (line.contains("Found: SAT")) {
				String[] strs = line.substring(21).split(",");
				HashMap<String, String> map = new HashMap<String, String>();
				for (int i = 0; i < strs.length; i++) {
					String var = strs[i].substring(0, strs[i].indexOf(":"));
					String val = strs[i].substring(strs[i].indexOf("=") + 1, strs[i].length());
					map.put(var, val);
				}
				Object[] test = new Object[Config.PARAMETERS.length];
				for (int i = 0; i < Config.PARAMETERS.length; i++) {
					if (Config.CLS[i] == byte.class) {
						test[i] = Byte.valueOf(map.get(Config.PARAMETERS[i]));
					} else if (Config.CLS[i] == short.class) {
						test[i] = Short.valueOf(map.get(Config.PARAMETERS[i]));
					} else if (Config.CLS[i] == int.class) {
						test[i] = Integer.valueOf(map.get(Config.PARAMETERS[i]));
					} else if (Config.CLS[i] == long.class) {
						test[i] = Long.valueOf(map.get(Config.PARAMETERS[i]));
					} else if (Config.CLS[i] == float.class) {
						test[i] = Float.valueOf(map.get(Config.PARAMETERS[i]));
					} else if (Config.CLS[i] == double.class) {
						test[i] = Double.valueOf(map.get(Config.PARAMETERS[i]));
					} else if (Config.CLS[i] == boolean.class) {
						test[i] = Boolean.valueOf(map.get(Config.PARAMETERS[i]));
					} else {
						System.out.println("error");
					}
				}
				tests.add(new TestCase(test));
			}
		}
		reader.close();
	}
	
	public static void extractFromLogs() throws IOException, ClassNotFoundException {
		String program = "Gammq";
		String algo = "MELT";
		File file = new File("/media/bhchen/Data/data/melt/" + program + "/" + algo + "/results");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;

		/*while ((line = reader.readLine()) != null) {
			if (line.contains("melt in")) {
				System.out.println(Double.valueOf(line.substring(line.lastIndexOf("n") + 2, line.lastIndexOf("m") - 1)) / 1000.0);
			}
		}*/
		
		/*while ((line = reader.readLine()) != null) {
			if (line.contains("concolic execution in")) {
				System.out.println(Double.valueOf(line.substring(line.lastIndexOf("n") + 2, line.lastIndexOf("m") - 1)) / 1000.0);
			}
		}*/
		
		while ((line = reader.readLine()) != null) {
			if (line.contains("mutation score")) {
				System.out.println(Double.valueOf(line.substring(line.indexOf("=") + 2)) * 100);
			}
		}
		
		/*double coverage = 0;
		boolean error = false;
		while ((line = reader.readLine()) != null) {
			if (line.contains("th run")) {
				if (!error) {
					System.out.println(coverage * 100);
				} else {
					System.out.println("error");
				}
			}
			if (line.contains("overall coverage")) {
				try {
					error = false;
					double d1 = Double.valueOf(line.substring(line.lastIndexOf("d") + 2, line.indexOf("/") - 1));
					double d2 = Double.valueOf(line.substring(line.lastIndexOf("/") + 2));
					coverage = d1 / d2;
				} catch (Exception e) {
					//System.err.println(line);
				}
			}
		}
		System.out.println(coverage * 100);*/
		
		reader.close();
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Config.loadProperties("/home/bhchen/workspace/testing/benchmark1-art/src/dt/original/Gammq.melt");
		//DataAnalyzer.extractTestsFromCELogs();
		DataAnalyzer.extractFromLogs();
	}

}
