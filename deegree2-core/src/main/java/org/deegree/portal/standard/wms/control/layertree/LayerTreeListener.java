//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.portal.standard.wms.control.layertree;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.util.MapUtils;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.portal.Constants;
import org.deegree.portal.context.LayerGroup;
import org.deegree.portal.context.MMLayer;
import org.deegree.portal.context.MapModel;
import org.deegree.portal.context.MapModelEntry;
import org.deegree.portal.context.ViewContext;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LayerTreeListener extends AbstractListener {

    private double scale = 0;

    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        ViewContext vc = (ViewContext) event.getSession().getAttribute( Constants.CURRENTMAPCONTEXT );
        Point[] points = vc.getGeneral().getBoundingBox();
        Envelope bbox = GeometryFactory.createEnvelope( points[0].getPosition(), points[1].getPosition(),
                                                        points[0].getCoordinateSystem() );
        scale = MapUtils.calcScaleWMS111( vc.getGeneral().getWindow().width, vc.getGeneral().getWindow().height, bbox,
                                          bbox.getCoordinateSystem() );

        StringBuilder sb = new StringBuilder( 10000 );
        sb.append( '[' );
        // return all nodes for root
        MapModel mapModel = vc.getGeneral().getExtension().getMapModel();
        List<LayerGroup> layerGroups = mapModel.getLayerGroups();
        for ( LayerGroup lg : layerGroups ) {
            sb.append( "{'text' : " );
            sb.append( "'" ).append( lg.getTitle() ).append( "'," );
            sb.append( "'id' : '" ).append( lg.getIdentifier() ).append( "'," );
            sb.append( "'checked': true," );
            sb.append( "'expanded': " ).append( lg.isExpanded() ).append( "," );
            sb.append( "'leaf' : false, 'cls' : 'folder' " );
            appendChildren( sb, lg );
            sb.append( "}" );

        }
        sb.append( ']' );
        String charEnc = Charset.defaultCharset().displayName();
        responseHandler.setContentType( "application/json; " + charEnc );
        responseHandler.writeAndClose( sb.toString() );
    }

    /**
     * @param sb
     * @param n
     * @param vc
     */
    private void appendChildren( StringBuilder sb, LayerGroup layerGroup ) {

        List<MapModelEntry> mme = layerGroup.getMapModelEntries();

        if ( mme.size() > 0 ) {

            sb.append( ", 'children' : [" );
            for ( int i = 0; i < mme.size(); i++ ) {
                if ( mme.get( i ) instanceof MMLayer ) {
                    MMLayer layer = (MMLayer) mme.get( i );
                    if ( layer.getLayer().getExtension().isValid() ) {
                        URL s = null;
                        if ( layer.getLayer().getStyleList().getCurrentStyle().getLegendURL() != null ) {
                            s = layer.getLayer().getStyleList().getCurrentStyle().getLegendURL().getOnlineResource();
                        }
                        sb.append( "{'text' : " );
                        sb.append( "'" ).append( layer.getTitle() ).append( "'," );
                        sb.append( "'id' : '" ).append( layer.getLayer().getExtension().getIdentifier() ).append( "'," );
                        if ( s != null ) {
                            sb.append( "'img' : '" ).append( s.toExternalForm() ).append( "'," );
                        }
                        if ( layer.getLayer().getAbstract() != null ) {
                            sb.append( "'qtip': '" ).append( layer.getLayer().getAbstract() ).append( "'," );
                        } else {
                            sb.append( "'qtip': '" ).append( layer.getTitle() ).append( "'," );
                        }
                        if ( scale < layer.getLayer().getExtension().getMinScaleHint()
                             || scale > layer.getLayer().getExtension().getMaxScaleHint() ) {
                            sb.append( "'disabled': true," );
                        }
                        sb.append( "'checked': " ).append( layer.isHidden() ? "false," : "true," );
                        sb.append( "'leaf' : true, 'cls' : 'file'}" );
                        if ( i < mme.size() - 1 ) {
                            sb.append( ',' );
                        }
                    }
                } else {
                    LayerGroup lg = (LayerGroup) mme.get( i );
                    sb.append( "{'text' : " );
                    sb.append( "'" ).append( lg.getTitle() ).append( "'," );
                    sb.append( "'id' : '" ).append( lg.getIdentifier() ).append( "'," );
                    sb.append( "'checked': " ).append( !lg.isHidden() ).append( "," );
                    sb.append( "'expanded': " ).append( lg.isExpanded() ).append( "," );
                    sb.append( "'leaf' : false, 'cls' : 'folder' " );
                    appendChildren( sb, lg );
                    sb.append( "}" );
                    if ( i < mme.size() - 1 ) {
                        sb.append( ',' );
                    }
                }
            }
            sb.append( "]" );
        }

    }

}
