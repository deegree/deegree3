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
package org.deegree.gml;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.feature.Feature;
import org.deegree.feature.gml.FeatureReference;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.gml.GML311GeometryDecoder;
import org.deegree.geometry.gml.refs.GeometryReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of GML objects (currently features and geometries) in GML instance documents, their ids and local xlink
 * references during the parsing of GML documents.
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
public class GMLDocumentIdContext implements GMLObjectResolver {

    private static final Logger LOG = LoggerFactory.getLogger( GMLDocumentIdContext.class );

    private Map<String, Feature> idToFeature = new HashMap<String, Feature>();

    private Map<String, Geometry> idToGeometry = new HashMap<String, Geometry>();

    private List<FeatureReference> featureReferences = new ArrayList<FeatureReference>();

    private List<GeometryReference<?>> localGeometryReferences = new ArrayList<GeometryReference<?>>();

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
        featureReferences.add( ref );
    }

    public void addGeometryReference( GeometryReference ref ) {
        if ( ref.isLocal() ) {
            localGeometryReferences.add( ref );
        }
    }

    public Feature getFeatureById( String gmlId ) {
        return idToFeature.get( gmlId );
    }

    public Geometry getGeometryById( String gmlId ) {
        return idToGeometry.get( gmlId );
    }

    @Override
    public Feature getFeature( String uri, String baseURL ) {
        if ( uri.startsWith( "#" ) ) {
            return idToFeature.get( uri.substring( 1 ) );
        }
        String msg = "Resolving of remote URIs is not implemented yet.";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public Geometry getGeometry( String uri, String baseURL ) {
        Geometry geometry = null;

        if ( uri.startsWith( "#" ) ) {
            geometry = idToGeometry.get( uri.substring( 1 ) );
        } else {
            GML311GeometryDecoder decoder = new GML311GeometryDecoder();
            try {
                URL resolvedURL = null;
                if ( baseURL != null ) {
                    resolvedURL = new URL( new URL( baseURL ), uri );
                } else {
                    resolvedURL = new URL( uri );
                }
                XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( resolvedURL );
                xmlReader.nextTag();
                geometry = decoder.parse( xmlReader, null );
                LOG.debug( "Read GML geometry: '" + geometry.getClass() + "'" );
                xmlReader.close();
            } catch ( Exception e ) {
                throw new RuntimeException( "Unable to resolve external geometry reference: " + e.getMessage() );
            }
        }
        return geometry;
    }

    @Override
    public Object getObject( String uri, String baseURL ) {
        Object o = getFeature( uri, baseURL );
        if ( o == null ) {
            o = getGeometry( uri, baseURL );
        }
        return o;
    }

    public Map<String, Feature> getFeatures() {
        return idToFeature;
    }

    public Map<String, Geometry> getGeometries() {
        return idToGeometry;
    }

    /**
     * Resolves all local references.
     * 
     * @throws GMLReferenceResolvingException
     *             if a local reference cannot be resolved
     */
    public void resolveLocalRefs()
                            throws GMLReferenceResolvingException {

        for ( FeatureReference ref : featureReferences ) {
            if ( ref.isLocal() ) {
                String fid = ref.getURI().substring( 1 );
                LOG.debug( "Resolving reference to feature '" + fid + "'" );
                if ( ref.getReferencedFeature() == null ) {
                    String msg = "Cannot resolve reference to feature with id '" + fid
                                 + "'. There is no feature with this id in the document.";
                    throw new GMLReferenceResolvingException( msg );
                }
            }
        }

        for ( GeometryReference<?> ref : localGeometryReferences ) {
            if ( ref.isLocal() ) {
                String gid = ref.getURI().substring( 1 );
                LOG.debug( "Resolving reference to geometry '" + gid + "'" );
                if ( ref.getReferencedGeometry() == null ) {
                    String msg = "Cannot resolve reference to feature with id '" + gid
                                 + "'. There is no feature with this id in the document.";
                    throw new GMLReferenceResolvingException( msg );
                }
            }
        }
    }
}
