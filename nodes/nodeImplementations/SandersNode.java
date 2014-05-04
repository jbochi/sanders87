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


import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.Distribution;

/**
 * Sanders87 Algorithm for Mutual Exclusion
 */
public class SandersNode extends Node {
	double PSC = 0.02;
	Logging log = Logging.getLogger("sanders_log.txt");
	
	@Override
	public void handleMessages(Inbox inbox) {}

	private boolean wantToEnterCS() {
		Distribution dist;
		double value;
        try {
	        dist = Distribution.getDistributionFromConfigFile("MutualExclusion/criticalSectionDistribution");
	        value = dist.nextSample();
        } catch (CorruptConfigurationEntryException e) {
        	value = 0;
	        e.printStackTrace();
        }
		return value <= PSC;		
	}
	
	@Override
	public void preStep() {
		if (wantToEnterCS()) {
			setColor(Color.RED);
		}
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = Integer.toString(this.ID);
		super.drawNodeAsSquareWithText(g, pt, highlight, text, 30, Color.WHITE);
	}	

	@Override
	public void init() {}

	@Override
	public void neighborhoodChange() {}

	@Override
	public void postStep() {}
	
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
