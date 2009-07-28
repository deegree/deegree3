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

import org.deegree.commons.gml.GMLIdContext;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.gml.GMLFeatureDecoder;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Geometry;
import org.deegree.protocol.wfs.getfeature.FilterQuery;
import org.deegree.protocol.wfs.getfeature.Query;

/**
 * {@link FeatureStore} implementation that is backed by a GML file which is kept in memory.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class GMLMemoryStore implements FeatureStore {

    private final ApplicationSchema schema;

    private final Map<String, Object> idToObject = new HashMap<String, Object>();

    private final Map<FeatureType, FeatureCollection> ftToFeatures = new HashMap<FeatureType, FeatureCollection>();

    /**
     * Creates a new {@link GMLMemoryStore} that is backed by the given GML file.
     *
     * @param docURL
     * @param schema
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    public GMLMemoryStore( URL docURL, ApplicationSchema schema ) throws XMLStreamException, XMLParsingException,
                            UnknownCRSException, FactoryConfigurationError, IOException {
        this.schema = schema;
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder parser = new GMLFeatureDecoder( schema, idContext );
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
    public FeatureCollection performQuery( Query query ) {

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
        if (query instanceof FilterQuery) {
        if ( ((FilterQuery)query).getFilter() != null ) {
            try {
                fc = fc.getMembers( ((FilterQuery)query).getFilter() );
            } catch ( FilterEvaluationException e ) {
                throw new RuntimeException( e.getMessage() );
            }
        }
        }

        return fc;
    }

    @Override
    public Object getObjectById( String id ) {
        return idToObject.get( id );
    }
}
