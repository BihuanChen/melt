package mlt.test.run;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;

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
	
	public TestChronicle(boolean server) throws IOException {
		String basePath = System.getProperty("java.io.tmpdir") + "/crncTest";
		crncTest = ChronicleQueueBuilder.indexed(basePath).build();
		basePath = System.getProperty("java.io.tmpdir") + "/crncInfo";
		crncInfo = ChronicleQueueBuilder.indexed(basePath).build();
		
		this.server = server;
		if (server) {
			writer = crncInfo.createAppender();
			reader = crncTest.createTailer();
		} else {
			writer = crncTest.createAppender();
			reader = crncInfo.createTailer();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void read() throws MalformedURLException {
		while(!reader.nextIndex());
		if (server) {
			Object[] obj = (Object[])reader.readObject();
			TestRunnerUtil.run(obj);
		} else {
			LinkedList<BranchTaint> taints = (LinkedList<BranchTaint>)reader.readObject();
			Profiles.executedPredicates = (ArrayList<Pair>)reader.readObject();

			Node<BranchTaint> node = taints.getFirst();
			while (node != null) {
				String key = node.entry.getSrcLoc().replace("/", ".");
				int tag = node.entry.getTag();
				if (Profiles.taints.get(key) == null) {
					Profiles.taints.put(key, new HashSet<Integer>());
				}
				int size = Config.CLS.length;
				for (int i = 0; i < size; i++) {
					int bit = (int)Math.pow(2, i);
					if ((tag & bit) == bit) {
						Profiles.taints.get(key).add(i);
					}
				}
				node = node.next;
			}
		}
		reader.finish();
	}
	
	public void write(Object obj1, Object obj2) {
		writer.startExcerpt();
		if (server) {
			writer.writeObject(obj1);
			writer.writeObject(obj2);
		} else {
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
