//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/
package org.deegree.graphics.optimizers;

import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 * Allows the chaining of {@link Optimizer}s. Implements the {@link Optimizer} interface as well.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class OptimizerChain extends AbstractOptimizer {

    // stores the Optimizers in the chain
    private ArrayList<Optimizer> optimizers = new ArrayList<Optimizer>();

    /**
     * Creates an empty instance of {@link OptimizerChain}.
     */
    public OptimizerChain() {
        // nothing to do
    }

    /**
     * Constructs a new {@link OptimizerChain} that contains the given {@link Optimizer} instances.
     *
     * @param optimizers
     */
    public OptimizerChain( Optimizer[] optimizers ) {
        for ( int i = 0; i < optimizers.length; i++ ) {
            this.optimizers.add( optimizers[i] );
        }
    }

    /**
     * Appends an {@link Optimizer} to the end of the processing chain.
     *
     * @param optimizer
     *            {@link Optimizer} to be added
     */
    public void addOptimizer( Optimizer optimizer ) {
        optimizers.add( optimizer );
    }

    /**
     * Performs the optimization for all contained {@link Optimizer} instances. Calls
     * {@link Optimizer#optimize(Graphics2D)} for all contained {@link Optimizer} instances. subsequently.
     *
     * @param g
     */
    public void optimize( Graphics2D g )
                            throws Exception {
        for ( Optimizer optimizer : optimizers ) {
            optimizer.optimize( g );
        }
    }
}
