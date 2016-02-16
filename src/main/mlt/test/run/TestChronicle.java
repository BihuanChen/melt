package mlt.test.run;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;

import edu.ntu.taint.BranchTaintNode;
import edu.ntu.taint.HashMap;
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
			HashMap taints = (HashMap)reader.readObject();
			Profiles.executedPredicates = (ArrayList<Pair>)reader.readObject();

			BranchTaintNode[] nodes = taints.getRootNodes();
			for (int i = 0; i < nodes.length; i++) {
				BranchTaintNode node = nodes[i];
				while (node != null) {
					String srcLoc = node.getSrcLoc().replace("/", ".");
					int tag = node.getTag();
					Profiles.taints.put(srcLoc, new HashSet<Integer>());
					for (int j = 0; j < Config.CLS.length; j++) {
						int bit = (int)Math.pow(2, j);
						if ((tag & bit) == bit) {
							Profiles.taints.get(srcLoc).add(j);
						}
					}
					node = node.getNext();
				}
			}
		}
		reader.finish();
	}
	
	public void write(Object obj1, Object obj2) {
		writer.startExcerpt();
		if (server) {
			// write taint results
			writer.writeObject(obj1);
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
