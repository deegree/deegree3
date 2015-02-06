//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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

import static org.deegree.protocol.wfs.transaction.action.UpdateAction.REMOVE;
import static org.deegree.protocol.wfs.transaction.action.UpdateAction.REPLACE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.index.RTree;
import org.deegree.commons.tom.Reference;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.stream.MemoryFeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.ResourceId;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.primitive.Surface;
import org.deegree.gml.utils.GMLObjectVisitor;
import org.deegree.gml.utils.GMLObjectWalker;
import org.deegree.protocol.wfs.transaction.action.ParsedPropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.UpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates stored feature instances plus index structures for id and spatial queries.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class StoredFeatures {

    private static final Logger LOG = LoggerFactory.getLogger( StoredFeatures.class );

    private final AppSchema schema;

    private final ICRS storageCRS;

    private final TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();

    private final Map<FeatureType, FeatureCollection> ftToFeatures = new HashMap<FeatureType, FeatureCollection>();

    private final Map<String, GMLObject> idToObject = new HashMap<String, GMLObject>();

    private final Map<FeatureType, RTree<Feature>> ftToIndex = new HashMap<FeatureType, RTree<Feature>>();

    /**
     * Creates a new {@link StoredFeatures} instance.
     *
     * @param schema
     *            application schema, must not be <code>null</code>
     * @param storageCRS
     *            target CRS for stored geometries, can be <code>null</code> (no CRS normalization)
     * @param former
     *            stored features to copy from, can be <code>null</code> (new instance will be empty)
     * @throws FeatureStoreException
     */
    StoredFeatures( AppSchema schema, ICRS storageCRS, StoredFeatures former ) throws FeatureStoreException {
        this.schema = schema;
        this.storageCRS = storageCRS;
        initFtToFeaturesMap( schema, former );
        try {
            rebuildIndexes();
        } catch ( UnknownCRSException e ) {
            throw new FeatureStoreException( e.getMessage(), e );
        }
    }

    private void initFtToFeaturesMap( AppSchema schema, StoredFeatures former ) {
        for ( FeatureType ft : schema.getFeatureTypes( null, true, false ) ) {
            FeatureCollection fc = new GenericFeatureCollection();
            if ( former != null ) {
                FeatureCollection oldFc = former.ftToFeatures.get( ft );
                fc.addAll( oldFc );
            }
            ftToFeatures.put( ft, fc );
        }
    }

    /**
     * Returns the stored features of the given type.
     *
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return stored features of the given type, never <code>null</code>
     */
    FeatureCollection getFeatures( FeatureType ft ) {
        return ftToFeatures.get( ft );
    }

    /**
     * Performs the given {@link Query} on the stored features.
     *
     * @param query
     *            query to be performed, must not be <code>null</code>
     * @return resulting features, never <code>null</code>
     * @throws FilterEvaluationException
     * @throws FeatureStoreException
     */
    FeatureInputStream query( Query query )
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
            if ( query.getPrefilterBBoxEnvelope() != null && ftEnv != null && storageCRS != null ) {
                Envelope prefilterBox = query.getPrefilterBBoxEnvelope();
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
            for ( ResourceId id : ( (IdFilter) query.getFilter() ).getSelectedIds() ) {
                GMLObject object = idToObject.get( id.getRid() );
                if ( object != null && object instanceof Feature ) {
                    features.add( (Feature) object );
                }
            }
            fc = new GenericFeatureCollection( null, features );
        }

        // sort features
        SortProperty[] sortCrit = query.getSortProperties();
        if ( sortCrit.length > 0 ) {
            fc = Features.sortFc( fc, sortCrit );
        }

        return new MemoryFeatureInputStream( fc );
    }

    GMLObject getObjectById( String id ) {
        return idToObject.get( id );
    }

    /**
     * Returns the {@link Envelope} for the stored features of the specified type.
     *
     * @param ftName
     *            feature type name, must not be <code>null</code>
     * @return envelope, can be <code>null</code>
     */
    Envelope getEnvelope( QName ftName ) {
        return ftToFeatures.get( schema.getFeatureType( ftName ) ).getEnvelope();
    }

    /**
     * Adds the given {@link Feature} instance and updates the index structures.
     *
     * @param features
     *            feature to be added, must not be <code>null</code> and must have an id (as well as every geometry)
     */
    void addFeature( Feature feature ) {
        FeatureType ft = feature.getType();
        FeatureCollection fc = ftToFeatures.get( ft );
        if ( fc == null ) {
            fc = new GenericFeatureCollection();
            ftToFeatures.put( ft, fc );
        }
        fc.add( feature );
        idToObject.put( feature.getId(), feature );
        if ( feature.getEnvelope() != null ) {
            RTree<Feature> rTree = ftToIndex.get( ft );
            float[] insertBox = toFloats( feature.getEnvelope() );
            if ( rTree == null ) {
                rTree = new RTree<Feature>( insertBox, 16 );
            }
            rTree.insert( insertBox, feature );
        }
    }

    /**
     * Removes the given {@link Feature} instance and updates the index structures.
     *
     * @param feature
     *            feature to be removed, must not be <code>null</code>
     */
    void removeFeature( Feature feature ) {
        idToObject.remove( feature.getId() );
        FeatureType ft = feature.getType();
        RTree<Feature> rTree = ftToIndex.get( ft );
        if ( rTree != null ) {
            rTree.remove( feature );
        }
        FeatureCollection fc = ftToFeatures.get( ft );
        if ( fc != null ) {
            fc.remove( feature );
        }
    }

    /**
     * Updates the given {@link Feature} instance and updates the index structures.
     *
     * TODO Use a copy of the original feature to avoid modifications on rollback. Difficult part: Consider updating of
     * references.
     *
     * @param feature
     *            feature to be updated, must not be <code>null</code>
     * @param replacementProps
     *            properties to be replaced, must not be <code>null</code>
     */
    void updateFeature( Feature feature, List<ParsedPropertyReplacement> replacementProps )
                            throws FeatureStoreException {

        for ( ParsedPropertyReplacement replacement : replacementProps ) {
            Property prop = replacement.getNewValue();
            UpdateAction updateAction = replacement.getUpdateAction();
            GenericProperty newProp = new GenericProperty( prop.getType(), null );
            if ( prop.getValue() != null ) {
                newProp.setValue( prop.getValue() );
            } else if ( !prop.getChildren().isEmpty() && prop.getChildren().get( 0 ) != null ) {
                newProp.setChildren( prop.getChildren() );
            } else if ( updateAction == null ) {
                updateAction = REMOVE;
            }

            if ( updateAction == null ) {
                updateAction = REPLACE;
            }

            int idx = replacement.getIndex();
            switch ( updateAction ) {
            case INSERT_AFTER:
                List<Property> ps = feature.getProperties();
                ListIterator<Property> iter = ps.listIterator();
                while ( iter.hasNext() ) {
                    if ( iter.next().getType().getName().equals( prop.getType().getName() ) ) {
                        --idx;
                    }
                    if ( idx < 0 ) {
                        iter.add( newProp );
                        break;
                    }
                }
                break;
            case INSERT_BEFORE:
                ps = feature.getProperties();
                iter = ps.listIterator();
                while ( iter.hasNext() ) {
                    if ( iter.next().getType().getName().equals( prop.getType().getName() ) ) {
                        --idx;
                    }
                    if ( idx == 0 ) {
                        iter.add( newProp );
                        break;
                    }
                }
                break;
            case REMOVE:
                ps = feature.getProperties();
                iter = ps.listIterator();
                while ( iter.hasNext() ) {
                    if ( iter.next().getType().getName().equals( prop.getType().getName() ) ) {
                        iter.remove();
                    }
                }
                break;
            case REPLACE:
                ps = feature.getProperties();
                iter = ps.listIterator();
                while ( iter.hasNext() ) {
                    if ( iter.next().getType().getName().equals( prop.getType().getName() ) ) {
                        --idx;
                    }
                    if ( idx < 0 ) {
                        iter.set( newProp );
                        break;
                    }
                }
                break;
            }
            validateProperties( feature, feature.getProperties() );
        }
    }

    private void validateProperties( Feature feature, List<Property> props ) {
        Map<PropertyType, Integer> ptToCount = new HashMap<PropertyType, Integer>();
        for ( Property prop : props ) {

            if ( prop.getValue() instanceof Geometry ) {
                Geometry geom = (Geometry) prop.getValue();
                if ( geom != null ) {
                    Property current = feature.getProperties( prop.getType().getName() ).get( 0 );
                    Geometry currentGeom = current != null ? ( (Geometry) current.getValue() ) : null;
                    // check compatibility (CRS) for geometry replacements (CITE
                    // wfs:wfs-1.1.0-Transaction-tc7.2)
                    if ( currentGeom != null && currentGeom.getCoordinateDimension() != geom.getCoordinateDimension() ) {
                        String msg = "Cannot replace given geometry property '" + prop.getType().getName()
                                     + "' with given value (wrong dimension).";
                        throw new InvalidParameterValueException( msg );
                    }
                    // check compatibility (geometry type) for geometry replacements (CITE
                    // wfs:wfs-1.1.0-Transaction-tc10.1)
                    QName qname = new QName( "http://cite.opengeospatial.org/gmlsf", "surfaceProperty" );
                    if ( !( geom instanceof Surface ) && prop.getType().getName().equals( qname ) ) {
                        String msg = "Cannot replace given geometry property '" + prop.getType().getName()
                                     + "' with given value (wrong type).";
                        throw new InvalidParameterValueException( msg );
                    }
                }
            }

            Integer count = ptToCount.get( prop.getType() );
            if ( count == null ) {
                count = 1;
            } else {
                count++;
            }
            ptToCount.put( prop.getType(), count );
        }
        for ( PropertyType pt : feature.getType().getPropertyDeclarations() ) {
            int count = ptToCount.get( pt ) == null ? 0 : ptToCount.get( pt );
            if ( count < pt.getMinOccurs() ) {
                String msg = "Update would result in invalid feature: property '" + pt.getName()
                             + "' must be present at least " + pt.getMinOccurs() + " time(s).";
                throw new InvalidParameterValueException( msg );
            } else if ( pt.getMaxOccurs() != -1 && count > pt.getMaxOccurs() ) {
                String msg = "Update would result in invalid feature: property '" + pt.getName()
                             + "' must be present no more than " + pt.getMaxOccurs() + " time(s).";
                throw new InvalidParameterValueException( msg );
            }
        }
    }

    void rebuildIndexes()
                            throws UnknownCRSException {

        long begin = System.currentTimeMillis();
        rebuildFeatureCollectionEnvelopes();
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Recalculating bounding boxes took {} [ms]", elapsed );

        begin = System.currentTimeMillis();
        rebuildRtrees();
        elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Building spatial indexes took {} [ms]", elapsed );

        begin = System.currentTimeMillis();
        rebuildIdToObjectMap();
        elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Building id lookup table took {} [ms]", elapsed );
    }

    private void rebuildFeatureCollectionEnvelopes() {
        for ( FeatureCollection fc : ftToFeatures.values() ) {
            fc.setEnvelope( fc.calcEnvelope() );
        }
    }

    private void rebuildRtrees() {
        ftToIndex.clear();
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
    }

    private void rebuildIdToObjectMap() {
        idToObject.clear();
        GMLObjectVisitor visitor = new GMLObjectVisitor() {
            @Override
            public boolean visitGeometry( Geometry geom ) {
                idToObject.put( geom.getId(), geom );
                return true;
            }

            @Override
            public boolean visitFeature( Feature feature ) {
                if ( feature instanceof Reference<?> ) {
                    return false;
                }
                idToObject.put( feature.getId(), feature );
                return true;
            }

            @Override
            public boolean visitObject( GMLObject o ) {
                return true;
            }

            @Override
            public boolean visitReference( Reference<?> ref ) {
                return false;
            }
        };

        for ( FeatureCollection fc : ftToFeatures.values() ) {
            for ( Feature f : fc ) {
                new GMLObjectWalker( visitor ).traverse( f );
            }
        }
    }

    private float[] toFloats( Envelope env ) {
        return new float[] { (float) env.getMin().get0(), (float) env.getMin().get1(), (float) env.getMax().get0(),
                            (float) env.getMax().get1() };
    }
}
