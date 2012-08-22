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
package org.deegree.graphics.sld;

import static org.deegree.framework.xml.XMLTools.escape;

import org.deegree.framework.xml.Marshallable;

/**
 * A NamedLayer uses the "name" attribute to identify a layer known to the WMS and can contain zero
 * or more styles, either NamedStyles or UserStyles. In the absence of any styles the default style
 * for the layer is used.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @version $Revision$ $Date$
 */
public class NamedLayer extends AbstractLayer implements Marshallable {
    /**
     * constructor initializing the class with the <NamedLayer>
     *
     * @param name
     * @param layerFeatureConstraints
     * @param styles
     */
    public NamedLayer( String name, LayerFeatureConstraints layerFeatureConstraints, AbstractStyle[] styles ) {
        super( name, layerFeatureConstraints, styles );
    }

    /**
     * exports the content of the Font as XML formated String
     *
     * @return xml representation of the Font
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<NamedLayer>" );
        sb.append( "<Name>" ).append( escape( name ) ).append( "</Name>" );

        if ( layerFeatureConstraints != null ) {
            sb.append( ( (Marshallable) layerFeatureConstraints ).exportAsXML() );
        }

        for ( int i = 0; i < styles.size(); i++ ) {
            sb.append( ( (Marshallable) styles.get( i ) ).exportAsXML() );
        }
        sb.append( "</NamedLayer>" );

        return sb.toString();
    }
}
