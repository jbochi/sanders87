/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.mutualExclusion.nodes.nodeImplementations;

import java.util.Comparator;
import java.util.PriorityQueue;

import sinalgo.configuration.Configuration;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import projects.mutualExclusion.nodes.messages.InqMessage;
import projects.mutualExclusion.nodes.messages.ReleaseMessage;
import projects.mutualExclusion.nodes.messages.RelinquishMessage;
import projects.mutualExclusion.nodes.messages.ReqMessage;
import projects.mutualExclusion.nodes.messages.Request;
import projects.mutualExclusion.nodes.messages.RequestComparator;
import projects.mutualExclusion.nodes.messages.YesMessage;
import projects.mutualExclusion.nodes.timers.leaveCSTimer;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.statistics.Distribution;

/**
 * Sanders87 Algorithm for Mutual Exclusion
 */
public class SandersNode extends Node {
	public enum State {
	    NOT_IN_CS, WAITING, IN_CS 
	}

	public int messageCount = 0;
	public int reliquishMessageCount = 0; 
	
	int clock = 0;
	int votes = 0;
	boolean hasVoted = false;
	boolean inquired = false;
	Node candidate;
	int candidateTS;
	int myTS;
	public State state = State.NOT_IN_CS;
	PriorityQueue<Request> deferedQueue;
	
	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message msg = inbox.next();
			Node sender = inbox.getSender();
			if (msg instanceof ReqMessage) {
				handleReq((ReqMessage) msg, sender);
			} else if (msg instanceof YesMessage) {
				handleYes((YesMessage) msg, sender);
			} else if (msg instanceof InqMessage) {
				handleInq((InqMessage) msg, sender);
			} else if (msg instanceof RelinquishMessage) {
				handleRelinquish((RelinquishMessage) msg, sender);
			} else if (msg instanceof ReleaseMessage) {
				handleRelease((ReleaseMessage) msg, sender);
			}
		}
	}

	private void sendAndCount(Message msg, Node target) {
		if (msg instanceof RelinquishMessage) {
			reliquishMessageCount += 1;
		}
		messageCount += 1;
		send(msg, target);
	}

	private void broadcastAndCount(Message msg) {
		messageCount += outgoingConnections.size();
		broadcast(msg);
	}
	
	private void handleReq(ReqMessage msg, Node sender) {
		if (!hasVoted) {
			Message reply = new YesMessage();
			sendAndCount(reply, sender);
			hasVoted = true;
			candidate = sender;
			candidateTS = msg.timestamp;
		} else {
			deferedQueue.add(new Request(sender, msg.timestamp));
			if (!inquired && 
				(
				 (msg.timestamp < candidateTS) || 
				 (msg.timestamp == candidateTS && sender.ID < candidate.ID))
				) {
				Message reply = new InqMessage(candidateTS);
				sendAndCount(reply, candidate);
				inquired = true;
			}
		}
		updateColor();		
	}
	
	private void handleRelinquish(RelinquishMessage msg, Node sender) {
		deferedQueue.add(new Request(sender, msg.timestamp));
		castVote();
	}
	
	private void handleRelease(ReleaseMessage msg, Node sender) {
		castVote();
	}

	private void castVote() {
		Request req = deferedQueue.poll();
		if (req != null) {
			Message reply = new YesMessage();
			sendAndCount(reply, req.requester);
			candidate = req.requester;
			candidateTS = req.timestamp; 			
		} else {
			hasVoted = false;
		}
		inquired = false;
		updateColor();
	}
	
	private void handleYes(YesMessage msg, Node sender) {
		votes++;
		if (votes == outgoingConnections.size()) {
			state = State.IN_CS;
			updateColor();
			leaveCSTimer timer = new leaveCSTimer(); 
			timer.startRelative(timeToLeaveCS(), this);
		}
	}
	
	private void handleInq(InqMessage msg, Node sender) {
		if (state == State.WAITING && msg.timestamp == myTS) {
			Message reply = new RelinquishMessage(myTS);
			sendAndCount(reply, sender);
			votes--;
		}
		updateColor();
	}
	
	private void enterCS() {
		state = State.WAITING;
		requestVotes();
		updateColor();		
	}
	
	public void leaveCS() {
		state = State.NOT_IN_CS;
		releaseVotes();
		updateColor();
	}
	
	private void requestVotes() {
		myTS = clock;		
		Message msg = new ReqMessage(myTS);		
		broadcastAndCount(msg);
	}
	
	private void releaseVotes() {
		votes = 0;		
		Message msg = new ReleaseMessage();
		broadcastAndCount(msg);
	}

	@Override
	public void preStep() {
		if (state == State.NOT_IN_CS && wantToEnterCS()) {
			enterCS();
		}
	}

	private boolean wantToEnterCS() {
		String namespace = "MutualExclusion/CriticalSection/Enter";
		Distribution dist;
		double value;
        try {
	        dist = Distribution.getDistributionFromConfigFile(namespace + "/Distribution");
	        value = dist.nextSample();
        } catch (CorruptConfigurationEntryException e) {
        	value = 0;
	        e.printStackTrace();
        }
		try {
	        return value <= Configuration.getDoubleParameter(namespace + "/Threshold");
        } catch (CorruptConfigurationEntryException e) {
	        e.printStackTrace();
	        return false;
        }
	}

	private double timeToLeaveCS() {
		String namespace = "MutualExclusion/CriticalSection/Leave";
		Distribution dist;
        try {
	        dist = Distribution.getDistributionFromConfigFile(namespace + "/Distribution");
	        return dist.nextSample();
        } catch (CorruptConfigurationEntryException e) {
	        e.printStackTrace();
	        return 3;
        }
	}
		
	private void updateColor() {
		switch (state) {
			case NOT_IN_CS:
				if (inquired) {
					setColor(Color.RED);
				} else if (hasVoted) {
					setColor(Color.BLACK);
				} else {
					setColor(Color.GREEN);
				}
				break;
			case WAITING:
				if (inquired) {
					setColor(Color.CYAN);
				} else {
					setColor(Color.BLUE);
				}
				break;
			case IN_CS:
				setColor(Color.RED);
				break;
		}
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text;
		switch (state) {
			case WAITING: 
				text = Integer.toString(votes);
				if (hasVoted) {
					text += " (" + candidate.ID + ")";
				}				
				super.drawNodeAsSquareWithText(g, pt, highlight, text, 25, Color.WHITE);
				break;
			case NOT_IN_CS:
				text = hasVoted ? ((inquired ? "INQ" : "Y") + " (" + candidate.ID + ")") : "NO";
				super.drawNodeAsSquareWithText(g, pt, highlight, text, 20, Color.WHITE);
				break;
			default:
				super.draw(g, pt, highlight);
				break;
		}
	}

	@Override
	public void init() {
		Comparator<Request> comparator = new RequestComparator();
		deferedQueue = new PriorityQueue<Request>(10, comparator);
		updateColor();
	}

	@Override
	public void neighborhoodChange() {}

	@Override
	public void postStep() {
		clock++;
	}
	
	@Override
	public String toString() {
		String s = "Node(" + this.ID + ") " + state;
		if (state == State.WAITING) {
			s += " (votes: " + votes + "; ts: " + myTS + ")";
		}		
		if (hasVoted) {
			s += " vote: (" + candidate.ID + ", ts: " + candidateTS;
			if (inquired) {
				s += ", inquired";
			}
			s += ")";
		}
		s += " [";
		Iterator<Edge> edgeIter = this.outgoingConnections.iterator();
		while(edgeIter.hasNext()){
			Edge e = edgeIter.next();
			Node n = e.endNode;
			s+=n.ID + (edgeIter.hasNext() ? " " : "");
		}
		return s + "]";

	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {}
}
