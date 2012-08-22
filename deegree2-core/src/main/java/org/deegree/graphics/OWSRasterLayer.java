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
package org.deegree.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OWSRasterLayer extends AbstractLayer {

    private ILogger LOG = LoggerFactory.getLogger( OWSRasterLayer.class );

    private URL baseURL;

    private Envelope envelope;

    /**
     * 
     * @param name
     * @param baseURL
     * @throws Exception
     */
    public OWSRasterLayer( String name, URL baseURL ) throws Exception {
        super( name );
        this.baseURL = baseURL;
    }

    /**
     * @param crs
     * @throws Exception
     * 
     */
    public void setCoordinatesSystem( CoordinateSystem crs )
                            throws Exception {
        // not supported yet
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.graphics.AbstractLayer#getBoundingBox()
     */
    @Override
    public Envelope getBoundingBox() {
        return envelope;
    }

    /**
     * 
     * @param envelope
     * @param w
     *            width of image
     * @param h
     *            height of image
     * @return grid coverage for envelope and size
     * @throws IOException
     */
    public GridCoverage getRaster( Envelope envelope, double w, double h )
                            throws IOException {

        this.envelope = envelope;
        StringBuffer sb = new StringBuffer( 150 );
        sb.append( baseURL );
        appendBoxParamtersToBuffer( sb, envelope, (int) w, (int) h );

        URL u = new URL( sb.toString() );

        GridCoverage gc = null;
        try {
            BufferedImage img = ImageUtils.loadImage( u );

            gc = new ImageGridCoverage( null, envelope, img );

        } catch ( Exception e ) {
            LOG.logDebug( "Error getting raster data: " + e.getMessage() );
        }
        return gc;
    }

    private void appendBoxParamtersToBuffer( StringBuffer sb, Envelope env, int w, int h ) {

        sb.append( "&BBOX=" ).append( env.getMin().getX() ).append( "," ).append( env.getMin().getY() );
        sb.append( "," ).append( env.getMax().getX() ).append( "," ).append( env.getMax().getY() );
        sb.append( "&WIDTH=" ).append( w ).append( "&HEIGHT=" ).append( h );
    }

}
