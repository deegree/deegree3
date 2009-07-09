//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
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
package org.deegree.feature.gml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.feature.Feature;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.gml.refs.GeometryReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of GML objects (currently features and geometries), their ids and local xlink references during the
 * parsing of GML documents.
 * <p>
 * Essential for resolving local xlink-references (to {@link Feature} or {@link Geometry} objects) at the end of the
 * parsing process of a GML instance document.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class GMLIdContext {

    private static final Logger LOG = LoggerFactory.getLogger( GMLIdContext.class );

    private Map<String, Feature> idToFeature = new HashMap<String, Feature>();

    private Map<String, Geometry> idToGeometry = new HashMap<String, Geometry>();

    private List<FeatureReference> featureReferences = new ArrayList<FeatureReference>();

    private List<GeometryReference> geometryReferences = new ArrayList<GeometryReference>();

    public void addFeature( Feature feature ) {
        String id = feature.getId();
        if ( id != null && id.length() > 0 ) {
            idToFeature.put( feature.getId(), feature );
        }
    }

    public void addGeometry( Geometry geometry ) {
        String id = geometry.getId();
        if ( id != null && id.length() > 0 ) {
            idToGeometry.put( geometry.getId(), geometry );
        }
    }

    public void addFeatureReference( FeatureReference ref ) {
        featureReferences.add (ref);
    }

    public void addGeometryReference( GeometryReference ref ) {
        geometryReferences.add( ref );
    }

    public Feature getFeature( String fid ) {
        return idToFeature.get( fid );
    }

    public Map<String,Feature> getFeatures () {
        return idToFeature;
    }

    public Map<String,Geometry> getGeometries () {
        return idToGeometry;
    }

    /**
     * @throws XMLProcessingException
     */
    public void resolveXLinks( ApplicationSchema schema )
                            throws XMLProcessingException {

        for ( FeatureReference ref : featureReferences ) {
            LOG.info( "Resolving reference to feature '" + ref.getId() + "'" );
            Feature targetObject = idToFeature.get( ref.getId() );
            if ( targetObject == null ) {
                String msg = "Cannot resolve reference to feature with id '" + ref.getId()
                             + "'. There is no feature with this id in the document.";
                throw new XMLProcessingException( msg );
            }

            FeatureType presentFt = targetObject.getType();
            if ( !schema.isValidSubstitution( ref.getType(), presentFt ) ) {
                String msg = "Cannot resolve reference to feature with id '" + ref.getId()
                             + "'. Property requires a feature of type '" + ref.getType().getName()
                             + "', but referenced object is of type '" + presentFt.getName() + "'.";
                throw new XMLProcessingException( msg );
            }
            ref.resolve( targetObject );
        }

        for ( GeometryReference ref : geometryReferences ) {
            LOG.info( "Resolving reference to geometry '" + ref.getId() + "'" );
            Geometry targetObject = idToGeometry.get( ref.getId() );
            if ( targetObject == null ) {
                String msg = "Cannot resolve reference to geometry with id '" + ref.getId()
                             + "'. There is no geometry with this id in the document.";
                throw new XMLProcessingException( msg );
            }

            // TODO check geometry type
            System.out.println ("Resolving " + ref);
            ref.resolve( targetObject );
        }
    }
}
