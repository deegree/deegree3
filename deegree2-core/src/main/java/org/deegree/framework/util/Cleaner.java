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
package org.deegree.framework.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Simple class which performs an action in continuing intervals. per default the garbage collector
 * will be called. But extending classes can change this behavior by overwriting the run() method.
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class Cleaner extends TimerTask {

    /**
     * Creates a new instance of Cleaner, which will have a delay of 60000 milis
     *
     * @param interval
     *            milliseconds the run-method will be continuing called
     */
    public Cleaner( int interval ) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate( this, 60000, interval );
    }

    /**
     * the run mehtod will be called after the interval (milli seconds) passed to the constructor. An
     * extending class can overwrite this method to perform something else then the default action.
     * Per default the garbage collector will be called --> System.gc();
     */
    @Override
    public void run() {
        System.gc();
    }
}
