package melt.test.run;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import melt.Config;

public class TaintRunner {

	public static void run(Object[] test) throws IOException, InterruptedException, ClassNotFoundException {
		String path = "/home/bhchen/Desktop/phosphor/jre-inst-cf/bin/java";
		String bootClassPath = "-Xbootclasspath/a:/home/bhchen/Desktop/phosphor/taint-0.0.1-SNAPSHOT.jar:/home/bhchen/Desktop/phosphor/lib-inst-cf/melt-dummy.jar:/home/bhchen/Desktop/phosphor/lib-inst-cf/benchmark4.jar";
		String classPath = "/home/bhchen/Desktop/phosphor/taint-runner.jar";

		ProcessBuilder processBuilder = new ProcessBuilder(path, "-Xss128M", bootClassPath, "-cp", classPath, "edu.ntu.taint.runner.TaintRunner", Config.MAINCLASS, Config.METHOD, "char", "char", "char", "char", "char", "a", "2", "&", "a", "a");
		processBuilder.redirectErrorStream(true);
		processBuilder.redirectOutput(new File(System.getProperty("java.io.tmpdir") + "/output"));

		long t2 = System.currentTimeMillis();
		Process process = processBuilder.start();
		
		BufferedInputStream in = new BufferedInputStream(process.getInputStream());
        byte[] bytes = new byte[4096];
        while (in.read(bytes) != -1) {}
        in.close();
		long t3 = System.currentTimeMillis();
		
		process.waitFor();
		long t4 = System.currentTimeMillis();
		BufferedReader reader = new BufferedReader(new FileReader(new File(System.getProperty("java.io.tmpdir") + "/taint")));
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			/*String srcLoc = "".replace("/", ".");
			int tag = 0;
			Profiles.taints.put(srcLoc, new HashSet<Integer>());
			for (int j = 0; j < Config.CLS.length; j++) {
				int bit = (int)Math.pow(2, j);
				if ((tag & bit) == bit) {
					Profiles.taints.get(srcLoc).add(j);
				}
			}*/
		}
		reader.close();
		long t5 = System.currentTimeMillis();
		System.out.println(t3 - t2);
		System.out.println(t4 - t3);
		System.out.println(t5 - t4);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Config.loadProperties("/home/bhchen/workspace/testing/benchmark4-siemens/src/replace/Replace.melt");
		run(new Object[]{'a', '2', '&', 'a', 'a'});
	}

}
