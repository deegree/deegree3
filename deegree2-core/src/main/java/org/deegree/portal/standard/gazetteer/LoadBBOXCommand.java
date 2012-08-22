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
package org.deegree.portal.standard.gazetteer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.util.Pair;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcbase.ElementStep;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LoadBBOXCommand extends AbstractGazetteerCommand {

    private String geogrId;

    /**
     * 
     * @param gazetteerAddress
     * @param featureType
     * @param properties
     * @param geogrId
     */
    LoadBBOXCommand( String gazetteerAddress, QualifiedName featureType, Map<String, String> properties, String geogrId ) {
        this.gazetteerAddress = gazetteerAddress;
        this.featureType = featureType;
        this.geogrId = geogrId;
        this.properties = properties;
    }

    /**
     * 
     * @return first contains highlight geometry; second geographic extent
     * @throws Exception
     */
    Pair<Geometry, Geometry> execute()
                            throws Exception {

        if ( !capabilitiesMap.containsKey( gazetteerAddress ) ) {
            loadCapabilities();
        }
        WFSCapabilities capabilities = capabilitiesMap.get( gazetteerAddress );

        // create query filter
        String tmp = properties.get( "GeographicIdentifier" );
        PropertyName propertyName;
        if ( tmp.startsWith( "{" ) ) {
            propertyName = new PropertyName( new QualifiedName( tmp ) );
        } else {
            propertyName = new PropertyName( new QualifiedName( tmp, featureType.getNamespace() ) );
        }
        Literal literal = new Literal( geogrId );
        Operation operation = new PropertyIsLikeOperation( propertyName, literal, '*', '?', '/' );
        ComplexFilter filter = new ComplexFilter( operation );

        // select just properties needed to fill result list
        PropertyPath[] propertyNames = getResultProperties( properties );

        // create Query and GetFeature request
        Query query = Query.create( propertyNames, null, null, null, null, new QualifiedName[] { featureType }, null,
                                    null, filter, 500, 0, RESULT_TYPE.RESULTS );
        GetFeature getFeature = GetFeature.create( capabilities.getVersion(), UUID.randomUUID().toString(),
                                                   RESULT_TYPE.RESULTS, GetFeature.FORMAT_GML3, null, 500, 0, -1, -1,
                                                   new Query[] { query } );

        // perform GetFeature request and create resulting GazetteerItems list
        FeatureCollection fc = performGetFeature( capabilities, getFeature );
        if ( fc.size() > 0 ) {
            Feature feat = fc.getFeature( 0 );
            // if no highlight geometry has been configured property name for geographic extent will be returned
            tmp = properties.get( "HighlightGeometry" );
            QualifiedName pn = null;
            if ( tmp.startsWith( "{" ) ) {
                pn = new QualifiedName( tmp );
            } else {
                pn = new QualifiedName( tmp, featureType.getNamespace() );
            }

            Geometry hlGeom = (Geometry) feat.getProperties( pn )[0].getValue();

            // GeographicExtent is mandatory
            tmp = properties.get( "GeographicExtent" );
            if ( tmp.startsWith( "{" ) ) {
                pn = new QualifiedName( tmp );
            } else {
                pn = new QualifiedName( tmp, featureType.getNamespace() );
            }

            Geometry geGeom = (Geometry) feat.getProperties( pn )[0].getValue();
            return new Pair<Geometry, Geometry>( hlGeom, geGeom );
        } else {
            throw new Exception( "no feature with geographic identifier: " + geogrId + " found" );
        }
    }

    /**
     * @param properties
     * @return
     */
    protected PropertyPath[] getResultProperties( Map<String, String> properties ) {
        List<PropertyPath> pathes = new ArrayList<PropertyPath>();

        String tmp = properties.get( "GeographicExtent" );
        QualifiedName sortQn = null;
        if ( tmp.startsWith( "{" ) ) {
            sortQn = new QualifiedName( tmp );
        } else {
            sortQn = new QualifiedName( tmp, featureType.getNamespace() );
        }
        List<PropertyPathStep> steps = new ArrayList<PropertyPathStep>();
        steps.add( new ElementStep( sortQn ) );
        pathes.add( new PropertyPath( steps ) );

        tmp = properties.get( "HighlightGeometry" );
        if ( tmp != null && !tmp.equals( properties.get( "GeographicExtent" ) ) ) {
            if ( tmp.startsWith( "{" ) ) {
                sortQn = new QualifiedName( tmp );
            } else {
                sortQn = new QualifiedName( tmp, featureType.getNamespace() );
            }
            steps = new ArrayList<PropertyPathStep>();
            steps.add( new ElementStep( sortQn ) );
            pathes.add( new PropertyPath( steps ) );
        }

        return pathes.toArray( new PropertyPath[pathes.size()] );
    }
}
