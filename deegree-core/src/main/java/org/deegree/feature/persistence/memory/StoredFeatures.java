//$HeadURL$
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
package org.deegree.feature.persistence.memory;

import static org.deegree.gml.GMLVersion.GML_31;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.index.RTree;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.MemoryFeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.gml.GMLObject;
import org.deegree.gml.utils.GMLObjectVisitor;
import org.deegree.gml.utils.GMLObjectWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the feature instances stored by a {@link MemoryFeatureStore} instance plus index structures.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class StoredFeatures {

    private static final Logger LOG = LoggerFactory.getLogger( StoredFeatures.class );

    private final ApplicationSchema schema;

    private final CRS storageCRS;

    final FeatureXPathEvaluator evaluator = new FeatureXPathEvaluator( GML_31 );

    final Map<FeatureType, FeatureCollection> ftToFeatures = new HashMap<FeatureType, FeatureCollection>();

    final Map<String, GMLObject> idToObject;

    final Map<FeatureType, RTree<Feature>> ftToIndex;

    StoredFeatures( ApplicationSchema schema, CRS storageCRS, StoredFeatures former ) {
        this.schema = schema;
        this.storageCRS = storageCRS;
        for ( FeatureType ft : schema.getFeatureTypes( null, true, false ) ) {
            FeatureCollection fc = new GenericFeatureCollection();
            if ( former != null ) {
                FeatureCollection oldFc = former.ftToFeatures.get( ft );
                fc.addAll( oldFc );
            }
            ftToFeatures.put( ft, fc );
        }
        if ( former != null ) {
            idToObject = former.idToObject;
            ftToIndex = former.ftToIndex;
        } else {
            idToObject = new HashMap<String, GMLObject>();
            ftToIndex = new HashMap<FeatureType, RTree<Feature>>();
        }
    }

    /**
     * Adds the given {@link Feature} instances.
     * 
     * @param features
     *            features to be added, never <code>null</code> and every feature and geometry must have an id
     */
    void addFeatures( Collection<Feature> features ) {
        for ( Feature feature : features ) {
            FeatureType ft = feature.getType();
            // TODO check if served
            FeatureCollection fc = ftToFeatures.get( ft );
            fc.add( feature );
        }
    }

    void buildMaps()
                            throws UnknownCRSException {

        long begin = System.currentTimeMillis();
        // (re-) build RTree
        for ( FeatureType ft : ftToFeatures.keySet() ) {
            FeatureCollection fc = ftToFeatures.get( ft );
            Envelope env = fc.getEnvelope();
            if ( env != null ) {
                RTree<Feature> index = new RTree<Feature>( toFloats( env ), 16 );
                List<Pair<float[], Feature>> fBboxes = new ArrayList<Pair<float[], Feature>>( fc.size() );
                for ( Feature f : fc ) {
                    Envelope fEnv = f.getEnvelope();
                    if ( fEnv != null ) {
                        float[] floats = toFloats( fEnv );
                        fBboxes.add( new Pair<float[], Feature>( floats, f ) );
                    }
                }
                index.insertBulk( fBboxes );
                ftToIndex.put( ft, index );
            }
        }
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Building spatial index took {} [ms]", elapsed );

        // (re-) build id lookup table
        begin = System.currentTimeMillis();
        idToObject.clear();
        GMLObjectVisitor visitor = new GMLObjectVisitor() {

            @Override
            public boolean visitGeometry( Geometry geom ) {
                idToObject.put( geom.getId(), geom );
                return true;
            }

            @Override
            public boolean visitFeature( Feature feature ) {
                idToObject.put( feature.getId(), feature );
                return true;
            }
        };

        for ( FeatureCollection fc : ftToFeatures.values() ) {
            for ( Feature f : fc ) {
                new GMLObjectWalker( visitor ).traverse( f );
            }
        }
        elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Building id lookup table took {} [ms]", elapsed );
    }

    FeatureResultSet query( Query query )
                            throws FilterEvaluationException, FeatureStoreException {

        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Join queries between multiple feature types are currently not supported.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureCollection fc = null;
        if ( query.getTypeNames().length == 1 ) {
            QName ftName = query.getTypeNames()[0].getFeatureTypeName();
            FeatureType ft = schema.getFeatureType( ftName );
            if ( ft == null ) {
                String msg = "Feature type '" + ftName + "' is not served by this feature store.";
                throw new FeatureStoreException( msg );
            }

            // determine / filter features
            fc = ftToFeatures.get( ft );

            // perform index filtering
            Envelope ftEnv = ftToFeatures.get( ft ).getEnvelope();
            if ( query.getPrefilterBBox() != null && ftEnv != null && storageCRS != null ) {
                Envelope prefilterBox = query.getPrefilterBBox();
                if ( prefilterBox.getCoordinateSystem() != null
                     && !prefilterBox.getCoordinateSystem().equals( storageCRS ) ) {
                    try {
                        GeometryTransformer t = new GeometryTransformer( storageCRS );
                        prefilterBox = t.transform( prefilterBox );
                    } catch ( Exception e ) {
                        throw new FeatureStoreException( e.getMessage(), e );
                    }
                }

                float[] floats = toFloats( prefilterBox );
                RTree<Feature> index = ftToIndex.get( ft );
                fc = new GenericFeatureCollection( null, index.query( floats ) );
            }

            if ( query.getFilter() != null ) {
                fc = fc.getMembers( query.getFilter(), evaluator );
            }
        } else {
            // must be an id filter based query
            if ( query.getFilter() == null || !( query.getFilter() instanceof IdFilter ) ) {
                String msg = "Invalid query. If no type names are specified, it must contain an IdFilter.";
                throw new FilterEvaluationException( msg );
            }
            Set<Feature> features = new HashSet<Feature>();
            for ( String id : ( (IdFilter) query.getFilter() ).getMatchingIds() ) {
                GMLObject object = idToObject.get( id );
                if ( object != null && object instanceof Feature ) {
                    features.add( (Feature) object );
                }
            }
            fc = new GenericFeatureCollection( null, features );
        }

        // sort features
        SortProperty[] sortCrit = query.getSortProperties();
        if ( sortCrit != null ) {
            fc = Features.sortFc( fc, sortCrit );
        }

        return new MemoryFeatureResultSet( fc );
    }

    GMLObject getObjectById( String id ) {
        return idToObject.get( id );
    }

    Envelope getEnvelope( QName ftName ) {
        return ftToFeatures.get( schema.getFeatureType( ftName ) ).getEnvelope();
    }

    private float[] toFloats( Envelope env ) {
        return new float[] { (float) env.getMin().get0(), (float) env.getMin().get1(), (float) env.getMax().get0(),
                            (float) env.getMax().get1() };
    }
}