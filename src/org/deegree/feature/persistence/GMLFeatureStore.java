//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.feature.persistence;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.filter.FilterEvaluationException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.gml.GMLFeatureParser;
import org.deegree.feature.gml.GMLIdContext;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Geometry;
import org.deegree.protocol.wfs.getfeature.FilterQuery;

/**
 * {@link FeatureStore} that is backed by a GML file which is kept in memory.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GMLFeatureStore implements FeatureStore {

    private final ApplicationSchema schema;

    private final Map<String, Object> idToObject = new HashMap<String, Object>();

    private final Map<FeatureType, FeatureCollection> ftToFeatures = new HashMap<FeatureType, FeatureCollection>();

    /**
     * Creates a new {@link GMLFeatureStore} that is backed by the given GML file.
     * 
     * @param docURL
     * @param schema
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    public GMLFeatureStore( URL docURL, ApplicationSchema schema ) throws XMLStreamException, XMLParsingException,
                            UnknownCRSException, FactoryConfigurationError, IOException {
        this.schema = schema;
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureParser parser = new GMLFeatureParser( schema, idContext );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        FeatureCollection fc = (FeatureCollection) parser.parseFeature(
                                                                        new XMLStreamReaderWrapper( xmlReader,
                                                                                                    docURL.toString() ),
                                                                        null );
        idContext.resolveXLinks( schema );
        xmlReader.close();

        // add features
        Map<String, Feature> idToFeature = idContext.getFeatures();
        for ( String id : idToFeature.keySet() ) {
            Feature feature = idToFeature.get( id );
            FeatureType ft = feature.getType();
            FeatureCollection fc2 = ftToFeatures.get( ft );
            if ( fc2 == null ) {
                fc2 = new GenericFeatureCollection();
                ftToFeatures.put( ft, fc2 );
            }
            fc2.add( feature );

            idToObject.put( id, feature );
        }

        // add geometries
        Map<String, Geometry> idToGeometry = idContext.getGeometries();
        for ( String id : idToGeometry.keySet() ) {
            idToObject.put( id, idToGeometry.get( id ) );
        }
    }

    @Override
    public ApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public FeatureCollection performQuery( FilterQuery query ) {

        if ( query.getTypeNames() == null || query.getTypeNames().length != 1 ) {
            String msg = "Only queries with exactly one type name are supported.";
            throw new UnsupportedOperationException( msg );
        }

        QName ftName = query.getTypeNames()[0].getFeatureTypeName();
        FeatureType ft = schema.getFeatureType( ftName );
        if ( ft == null ) {
            String msg = "Feature type '" + ftName + "' is not served by this datastore.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureCollection fc = ftToFeatures.get( ft );
        if ( query.getFilter() != null ) {
            try {
                fc = fc.getMembers( query.getFilter() );
            } catch ( FilterEvaluationException e ) {
                throw new RuntimeException( e.getMessage() );
            }
        }

        return fc;
    }

    @Override
    public Object getObjectById( String id ) {
        return idToObject.get( id );
    }
}
