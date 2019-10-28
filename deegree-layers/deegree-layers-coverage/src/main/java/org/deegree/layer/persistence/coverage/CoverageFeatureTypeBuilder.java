//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.coverage;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;

import javax.xml.namespace.QName;

import org.deegree.commons.xml.GenericLSInput;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.slf4j.Logger;
import org.w3c.dom.ls.LSInput;

/**
 * Builds the standard coverage feature type for feature info.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class CoverageFeatureTypeBuilder {

    private static final Logger LOG = getLogger( CoverageFeatureTypeBuilder.class );

    static FeatureType buildFeatureType() {
        try {
            LSInput input = new GenericLSInput();
            InputStream schema = CoverageFeatureTypeBuilder.class.getResourceAsStream( "gfiSchema.xsd" );
            input.setByteStream( schema );
            GMLAppSchemaReader decoder = new GMLAppSchemaReader( null, null, input );
            AppSchema extractAppSchema = decoder.extractAppSchema();
            return extractAppSchema.getFeatureType( new QName( "http://www.deegree.org/app", "data", "app" ) );
        } catch ( Exception e ) {
            LOG.error( "Could not read schema for GFI response on CoverageFeatureType", e );
            return null;
        }
    }

}