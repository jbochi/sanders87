package projects.mutualExclusion.nodes.messages;

import java.util.Comparator;

public class ReqMessageTimeStampComparator implements
        Comparator<ReqMessage> {

	@Override
    public int compare(ReqMessage msg1, ReqMessage msg2) {
		if (msg1.timestamp < msg2.timestamp) {
			return -1;
		} else if (msg1.timestamp > msg2.timestamp) {
			return 1;
		} else {
			return 0;
		}
    }

}
