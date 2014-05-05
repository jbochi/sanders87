package projects.mutualExclusion.nodes.messages;

import sinalgo.nodes.Node;

public class Request {
	public int timestamp;
	public Node requester;
	
	public Request(Node req, int ts) {
		timestamp = ts;
		requester = req;
	}
}
