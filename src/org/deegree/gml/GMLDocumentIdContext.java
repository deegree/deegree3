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
import org.deegree.geometry.Geometry;
import org.deegree.gml.feature.FeatureReference;
import org.deegree.gml.geometry.GML311GeometryDecoder;
import org.deegree.gml.geometry.refs.GeometryReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of {@link GMLObject}s in GML instance documents, their ids and local xlink references during the parsing
 * of GML documents.
 * <p>
 * Can be used for resolving local xlink-references at the end of the parsing process of a GML instance document or to
 * access all encountered objects on any level of the document.
 * </p>
 * 
 * @see GMLStreamReader
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLDocumentIdContext implements GMLObjectResolver {

    private static final Logger LOG = LoggerFactory.getLogger( GMLDocumentIdContext.class );

    private final Map<String, Feature> idToFeature = new HashMap<String, Feature>();

    private final Map<String, Geometry> idToGeometry = new HashMap<String, Geometry>();

    private final List<FeatureReference> featureReferences = new ArrayList<FeatureReference>();

    private final List<GeometryReference<?>> localGeometryReferences = new ArrayList<GeometryReference<?>>();

    private final GMLVersion version;

    /**
     * Creates a new {@link GMLDocumentIdContext} instance for a GML document with the given version.
     * 
     * @param version
     *            gml version, must not be <code>null</code>
     */
    public GMLDocumentIdContext( GMLVersion version ) {
        this.version = version;
    }

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
            try {
                URL resolvedURL = null;
                if ( baseURL != null ) {
                    resolvedURL = new URL( new URL( baseURL ), uri );
                } else {
                    resolvedURL = new URL( uri );
                }
                GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( version, resolvedURL );
                geometry = gmlReader.readGeometry();
                gmlReader.close();
                LOG.debug( "Read GML geometry: '" + geometry.getClass() + "'" );
            } catch ( Exception e ) {
                throw new RuntimeException( "Unable to resolve external geometry reference: " + e.getMessage() );
            }
        }
        return geometry;
    }

    @Override
    public GMLObject getObject( String uri, String baseURL ) {
        GMLObject o = getFeature( uri, baseURL );
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
