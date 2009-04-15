//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
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
package org.deegree.feature.gml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.feature.Feature;
import org.deegree.feature.refs.FeatureReference;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.refs.GeometryReference;
import org.deegree.geometry.refs.PointReference;
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

    public void addFeatureReference( FeatureReference refFeature ) {
        featureReferences.add (refFeature);
    }

    public PointReference addPointReference( String targetId ) {
        PointReference ref = new PointReference( targetId );
        geometryReferences.add( ref );
        return ref;
    }

    public Feature getFeature( String fid ) {
        return idToFeature.get( fid );
    }    
    
    /**
     * @throws XMLProcessingException
     */
    public void resolveXLinks( ApplicationSchema schema )
                            throws XMLProcessingException {
        for ( FeatureReference ref : featureReferences ) {
            LOG.info( "Resolving feature reference to feature '" + ref.getId() + "'" );
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
    }
}
