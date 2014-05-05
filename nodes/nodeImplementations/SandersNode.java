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

import sinalgo.configuration.Configuration;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import projects.mutualExclusion.nodes.messages.ReqMessage;
import projects.mutualExclusion.nodes.messages.YesMessage;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.Distribution;

/**
 * Sanders87 Algorithm for Mutual Exclusion
 */
public class SandersNode extends Node {
	private enum State {
	    NOT_IN_CS, WAITING, IN_CS 
	}

	Logging log = Logging.getLogger("sanders_log.txt");
	int clock = 0;
	int votes = 0;
	boolean hasVoted = false;
	private State state = State.NOT_IN_CS;
	
	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message msg = inbox.next();
			Node sender = inbox.getSender(); 
			if (msg instanceof ReqMessage) {
				handleReq(msg, sender);
			} else if (msg instanceof YesMessage) {
				handleYes(msg, sender);
			}
		}
	}

	private void handleReq(Message msg, Node sender) {
		if (!hasVoted) {
			hasVoted = true;
			updateColor();			
			Message reply = new YesMessage();
			send(reply, sender);
		}
	}
	
	private void handleYes(Message msg, Node sender) {
		votes++;
		if (votes == outgoingConnections.size()) {
			state = State.IN_CS;
			updateColor();
		}
	}
	
	private boolean wantToEnterCS() {
		String namespace = "MutualExclusion/CriticalSection";
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
	
	@Override
	public void preStep() {
		if (state == State.NOT_IN_CS && wantToEnterCS()) {
			enterCS();
		}
	}
	
	private void enterCS() {
		state = State.WAITING;
		requestVotes();
		updateColor();		
	}
	
	private void requestVotes() {
		Message msg = new ReqMessage(clock);
		broadcast(msg);
	}
	
	private void updateColor() {
		switch (state) {
			case NOT_IN_CS:
				setColor(hasVoted ? Color.YELLOW : Color.GREEN);
				break;
			case WAITING:
				setColor(Color.BLUE);
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
				super.drawNodeAsSquareWithText(g, pt, highlight, text, 30, Color.WHITE);
				break;
			case NOT_IN_CS: 
				text = hasVoted ? "YES" : "NO";
				super.drawNodeAsSquareWithText(g, pt, highlight, text, 30, Color.WHITE);
				break;
			default:
				super.draw(g, pt, highlight);
				break;
		}
	}

	@Override
	public void init() {
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
		String s = "Node(" + this.ID + ") [";
		Iterator<Edge> edgeIter = this.outgoingConnections.iterator();
		while(edgeIter.hasNext()){
			Edge e = edgeIter.next();
			Node n = e.endNode;
			s+=n.ID+" ";
		}
		return s + "]";
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {}
}
