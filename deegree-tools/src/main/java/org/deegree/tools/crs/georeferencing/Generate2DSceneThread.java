//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.tools.crs.georeferencing;

import java.awt.image.BufferedImage;

import org.deegree.geometry.Envelope;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Generate2DSceneThread extends Generate2DScene implements Runnable {

    private final Envelope envelope;

    private BufferedImage cachedImage;

    private final Thread thread;

    // private final String url;

    // private final Rectangle bounds;

    // public Generate2DSceneThread( Envelope envelope, String url, Rectangle bounds ) {
    // this.envelope = envelope;
    // this.url = url;
    // this.bounds = bounds;
    // thread = new Thread( this );
    // thread.start();
    // System.out.println( "\n\n\n ich bin in einem Thread!!\n\n\n" );
    // }

    public Generate2DSceneThread( Envelope envelope ) {
        this.envelope = envelope;
        thread = new Thread( this );
        thread.start();
        System.out.println( "\n\n\n ich bin in einem Thread!!\n\n\n" );
    }

    @Override
    public void run() {
        // setWmsFilename( url );
        // setBounds( bounds );
        if ( cachedEnvelope != null ) {
            cachedImage = getImage( cachedEnvelope, false );
        }
    }

    public BufferedImage getCachedImage() {
        return cachedImage;
    }

    public synchronized void interrupt() {
        if ( thread != null )
            thread.interrupt();
    }

}
