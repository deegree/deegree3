//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.protocol.wps;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.services.wps.provider.IVectorLayerAdapter;
import org.junit.Test;

import es.unex.sextante.dataObjects.IVectorLayer;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class IVectorAdapterTest {

    private static FeatureCollection readFeatureCollection()
                            throws Exception {

        // URL url = new URL(
        // "http://localhost:8080/geoserver/wfs?request=GetFeature&version=1.1.0&typeName=topp:states&outputFormat=GML2&FEATUREID=states.3"
        // );

        URL url = new URL(
                           "http://demo.deegree.org/deegree-wfs/services?service=WFS&version=1.1.0&request=GetFeature&typename=app:Country&namespace=xmlns%28app=http://www.deegree.org/app%29&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1" );
        GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, url );
        FeatureCollection fc = gmlStreamReader.readFeatureCollection();
        return fc;
    }

    @Test
    public void testToCreateVectorLayerFromFeatureCollection() {

        try {
            // input feature collection
            FeatureCollection fcIn = readFeatureCollection();
            IVectorLayer layer = IVectorLayerAdapter.createVectorLayer( fcIn );

            // output feature collection
            FeatureCollection fcOut = IVectorLayerAdapter.createFeatureCollection( layer );

            // check size
            Assert.assertTrue( fcIn.size() == fcOut.size() );

            // check features
            Iterator<Feature> itIn = fcIn.iterator();
            Iterator<Feature> itOut = fcOut.iterator();
            while ( itIn.hasNext() && itOut.hasNext() ) {
                Feature fIn = itIn.next();
                Feature fOut = itOut.next();

                // check property declarations
                List<PropertyType> proDeclIn = fIn.getType().getPropertyDeclarations();
                List<PropertyType> proDeclOut = fOut.getType().getPropertyDeclarations();
                Iterator<PropertyType> itInProDecl = proDeclIn.iterator();
                Iterator<PropertyType> itOutProDecl = proDeclOut.iterator();
                while ( itInProDecl.hasNext() && itOutProDecl.hasNext() ) {
                    PropertyType ptIn = itInProDecl.next();
                    PropertyType ptOut = itOutProDecl.next();

                    // check simple Properties
                    if ( ptIn instanceof SimplePropertyType && ptOut instanceof SimplePropertyType ) {
                        SimplePropertyType sptIn = (SimplePropertyType) ptIn;
                        SimplePropertyType sptOut = (SimplePropertyType) ptOut;

                        // check property names
                        Assert.assertTrue( sptIn.getName().equals( sptOut.getName() ) );

                        // check value content
                        Assert.assertTrue( fIn.getProperties( sptIn.getName() )[0].getValue().toString().equals(
                                                                                                                 fOut.getProperties( sptOut.getName() )[0].getValue().toString() ) );
                    } else {
                        // check geometry properties
                        if ( ptIn instanceof GeometryPropertyType && ptOut instanceof GeometryPropertyType ) {

                            String geomIn = fIn.getProperties( ptIn.getName() )[0].getValue().toString();
                            String geomOut = fOut.getProperties( ptOut.getName() )[0].getValue().toString();

                            // check geometry content
                            Assert.assertTrue( geomIn.equals( geomOut ) );

                        }
                    }
                }
            }

            int a = 3;
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
