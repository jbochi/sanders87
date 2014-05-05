package projects.mutualExclusion.nodes.messages;

import java.util.Comparator;

public class RequestComparator implements
        Comparator<Request> {

	@Override
    public int compare(Request r1, Request r2) {
		if (r1.timestamp < r2.timestamp) {
			return -1;
		} else if (r1.timestamp > r2.timestamp) {
			return 1;
		} else if (r1.requester.ID < r2.requester.ID) {
			return -1;
		} else if (r1.requester.ID > r2.requester.ID) {
			return 1;
		} else {
			return 0;
		}
    }

}
