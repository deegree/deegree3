//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.feature.gml.schema;

import junit.framework.Assert;

import org.deegree.commons.gml.GMLVersion;
import org.deegree.feature.types.FeatureType;
import org.junit.Test;

public class GMLApplicationSchemaXSDDecoderTest {

    @Test
    public void testParsing()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/Philosopher.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( schemaURL, GMLVersion.GML_31 );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        Assert.assertEquals( 19, fts.length );
        
        // TODO do more thorough testing
    }
}
