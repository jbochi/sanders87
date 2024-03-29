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
package projects.mutualExclusion.models.connectivityModels;

import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;
import sinalgo.runtime.Runtime;

public class Grid extends ConnectivityModelHelper {
	
	/* (non-Javadoc)
	 * @see connectivityModels.ConnectivityModel#isConnected(nodes.Node, nodes.Node)
	 */
	protected boolean isConnected(Node from, Node to) {
		return row(from) == row(to) || col(from) == col(to);
	}
	
	private int row(Node n) {
		return (n.ID - 1) % gridSize();
	}

	private int col(Node n) {
		return (n.ID - 1) / gridSize();
	}

	private int gridSize() {
		return (int) Math.sqrt(Runtime.nodes.size());
	};

	boolean initialized = false;	
	
	/**
	 * The default constructor for this class.  
	 * 
	 * The first time this constructor is called, it initializes the static parameters of this class. 
	 * @throws CorruptConfigurationEntryException If one of the initialization steps fails.
	 */
	public Grid() throws CorruptConfigurationEntryException {
		if(! initialized) {
			initialized = true;
		}
	}
}
