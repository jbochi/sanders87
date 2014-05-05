package projects.mutualExclusion.nodes.timers;

import projects.mutualExclusion.nodes.nodeImplementations.SandersNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;

/**
 * A timer that sends a message to a given node when it fires, 
 * independent of the underlying network graph. I.e. the timer
 * uses the sendDirect() method to deliver the message.
 * <p>
 * The message is sent by the node who starts the timer.
 */
public class leaveCSTimer extends Timer {
	public leaveCSTimer() {}
	
	@Override
	public void fire() {
		((SandersNode) this.node).leaveCS();
	}
}
