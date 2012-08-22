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
package org.deegree.portal.standard.wms.util;

import org.deegree.framework.util.StringTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.wms.capabilities.Layer;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ClientHelper {

    /**
     * 
     * @param root
     * @return String representation of Layers as tree
     */
    public static String getLayersAsTree( Layer root ) {
        StringBuffer sb = new StringBuffer( 10000 );
        sb.append( Messages.getMessage( "IGEO_STD_WMSLAYERSELECT_LAYERTREE_HEADER", root.getTitle() ) );
        Layer[] layers = root.getLayer();
        int indent = 0;
        for ( int i = 0; i < layers.length; i++ ) {
            appendLayer( layers[i], indent, sb );
        }
        return sb.toString();
    }

    private static void appendLayer( Layer layer, int indent, StringBuffer target ) {
        indent++;
        String s = "";
        for ( int i = 0; i < indent; i++ ) {
            s = s + "&nbsp;";
        }
        target.append( s );
        if ( layer.getName() != null ) {

            // get MetadataURL
            String metadataUrl = null;
            if ( layer.getMetadataURL() != null ) {
                MetadataURL[] mdURLs = layer.getMetadataURL();
                MetadataURL mdUrl = null;
                if ( mdURLs != null && mdURLs.length > 0 ) {
                    mdUrl = (MetadataURL) mdURLs[0];
                    metadataUrl = mdUrl.getOnlineResource().toExternalForm();
                }
            }

            target.append( s ).append( "<input name='LAYER' type='checkbox' " );
            target.append( "value='" );
            appendHTMLEntityEncode( layer.getName(), target ); // value_0
            target.append( '|' );
            appendHTMLEntityEncode( layer.getTitle(), target ); // value_1
            target.append( '|' );
            target.append( layer.isQueryable() ); // value_2
            target.append( '|' );
            // TODO test new stuff (value_3, value_4, value_5):
            if ( metadataUrl != null ) {
                metadataUrl = StringTools.replace( metadataUrl, "&", "&amp;", true );
                target.append( metadataUrl ); // value_3
            }
            target.append( '|' );
            if ( layer.getScaleHint() != null ) {
                target.append( layer.getScaleHint().getMin() ).append( '|' ); // value_4
                target.append( layer.getScaleHint().getMax() ); // value_5
            } else {
                // no scaleHint provided in Layer
                target.append( '0' ).append( '|' ).append( Double.MAX_VALUE );
            }
            target.append( "' >" );
            appendHTMLEntityEncode( layer.getTitle(), target );
            target.append( "<br/>\n" );
        } else {
            target.append( "<b>" );
            appendHTMLEntityEncode( layer.getTitle(), target );
            target.append( "</b><br/>" );
            Layer[] layers = layer.getLayer();
            for ( int i = 0; i < layers.length; i++ ) {
                appendLayer( layers[i], indent, target );
            }
            target.append( "<br/>" );
        }
    }

    /**
     * @param s
     * @param sb
     * @return the new string
     */
    public static String appendHTMLEntityEncode( String s, StringBuffer sb ) {
        StringBuffer buf = new StringBuffer();
        for ( int i = 0; i < s.length(); i++ ) {
            char c = s.charAt( i );
            if ( c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' ) {
                buf.append( c );
            } else {
                buf.append( "&#" + (int) c + ";" );
            }
        }

        return sb.append( buf ).toString();
    }

}
