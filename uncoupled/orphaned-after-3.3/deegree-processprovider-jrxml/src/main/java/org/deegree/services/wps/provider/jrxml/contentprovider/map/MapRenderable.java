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
package org.deegree.services.wps.provider.jrxml.contentprovider.map;

import static org.deegree.services.wps.provider.jrxml.contentprovider.map.RenderUtils.adjustSpan;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.List;

import net.sf.jasperreports.engine.JRAbstractSvgRenderer;
import net.sf.jasperreports.engine.JRException;

import org.deegree.geometry.Envelope;
import org.deegree.services.wps.ProcessletException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class MapRenderable extends JRAbstractSvgRenderer {

    private static final long serialVersionUID = 341218501840541564L;

    private final List<OrderedDatasource<?>> datasources;

    private final Envelope bbox;

    private final int resolution;

    public MapRenderable( List<OrderedDatasource<?>> datasources, Envelope bbox, int resolution ) {
        this.datasources = datasources;
        this.bbox = bbox;
        this.resolution = resolution;
    }

    @Override
    public void render( Graphics2D g, Rectangle2D rectangle )
                            throws JRException {
        int originalWidth = ( (Double) rectangle.getWidth() ).intValue();
        int originalHeight = ( (Double) rectangle.getHeight() ).intValue();
        int width = adjustSpan( originalWidth, resolution );
        int height = adjustSpan( originalHeight, resolution );
        try {
            for ( OrderedDatasource<?> datasource : datasources ) {
                Image image = datasource.getImage( width, height, bbox );
                // TODO: resolution!
                image = image.getScaledInstance( originalWidth, originalHeight, Image.SCALE_SMOOTH );
                if ( image != null ) {
                    g.drawImage( image, 0, 0, null );
                }
            }
        } catch ( ProcessletException e ) {
            throw new JRException( e );
        }
    }

}
