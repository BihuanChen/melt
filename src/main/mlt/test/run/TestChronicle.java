package mlt.test.run;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import edu.ntu.taint.BranchTaint;
import edu.ntu.taint.LinkedList;
import edu.ntu.taint.LinkedList.Node;
import mlt.Config;
import mlt.test.Pair;
import mlt.test.Profiles;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

public class TestChronicle {

	private Chronicle crncTest;
	private Chronicle crncInfo;
	private boolean server;
	private ExcerptAppender writer;
	private ExcerptTailer reader;
	private TestRunnerUtil runnerUtil;
	
	public TestChronicle(boolean server) throws IOException {
		String basePath = System.getProperty("java.io.tmpdir") + "/crncTest";
		crncTest = ChronicleQueueBuilder.indexed(basePath).build();
		basePath = System.getProperty("java.io.tmpdir") + "/crncInfo";
		crncInfo = ChronicleQueueBuilder.indexed(basePath).build();
		
		this.server = server;
		if (server) {
			writer = crncInfo.createAppender();
			reader = crncTest.createTailer();
			runnerUtil = new TestRunnerUtil();
		} else {
			writer = crncTest.createAppender();
			reader = crncInfo.createTailer();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void read() throws MalformedURLException {
		while(!reader.nextIndex());
		if (server) {
			// read the test case
			Object[] obj = (Object[])reader.readObject();
			runnerUtil.run(obj);
		} else {
			// read taint results and executed predicates
			HashMap<String, HashSet<Integer>> taints = (HashMap<String, HashSet<Integer>>)reader.readObject();
			Profiles.executedPredicates = (ArrayList<Pair>)reader.readObject();

			Iterator<String> iterator1 = taints.keySet().iterator();
			while (iterator1.hasNext()) {
				String key1 = iterator1.next(); 
				String key2 = key1.replace("/", ".");
				if (Profiles.taints.get(key2) == null) {
					Profiles.taints.put(key2, new HashSet<Integer>());
				}
				Iterator<Integer> iterator2 = taints.get(key1).iterator();
				while (iterator2.hasNext()) {
					int tag = iterator2.next();
					int size = Config.CLS.length;
					for (int i = 0; i < size; i++) {
						int bit = (int)Math.pow(2, i);
						if ((tag & bit) == bit) {
							Profiles.taints.get(key2).add(i);
						}
					}
				}
			}
		}
		reader.finish();
	}
	
	public void write(Object obj1, Object obj2) {
		writer.startExcerpt();
		if (server) {
			// write taint results
			@SuppressWarnings("unchecked")
			Node<BranchTaint> node = ((LinkedList<BranchTaint>)obj1).getFirst();
			HashMap<String, HashSet<Integer>> taints = new HashMap<String, HashSet<Integer>>();
			while (node != null) {
				String key = node.entry.getSrcLoc();
				if (key.startsWith(Config.FILTER)) {
					int tag = node.entry.getTag();
					if (taints.get(key) == null) {
						taints.put(key, new HashSet<Integer>());
					}
					taints.get(key).add(tag);
				}
				node = node.next;
			}
			writer.writeObject(taints);
			// write executed predicates
			//TODO process the executed predicates?
			writer.writeObject(obj2);
		} else {
			// write the test case
			writer.writeObject(obj1);
		}
		writer.finish();
	}
	
	public void close() throws IOException {
		writer.close();
		reader.close();
		crncTest.close();
		crncInfo.close();
	}

}
