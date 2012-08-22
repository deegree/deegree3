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

package org.deegree.io.datastore.shape;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.AnnotationDocument;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.PropertyPathResolver;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.dbaseapi.DBaseFile;
import org.deegree.io.shpapi.HasNoDBaseFileException;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComparisonOperation;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.filterencoding.FilterTools;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.PropertyIsBetweenOperation;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyIsInstanceOfOperation;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.PropertyIsNullOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryImpl;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.w3c.dom.Element;

/**
 * {@link Datastore} implementation that allows (read-only) access to ESRI shape files.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ShapeDatastore extends Datastore {

    private static final ILogger LOG = LoggerFactory.getLogger( ShapeDatastore.class );

    private static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    // keys: FeatureTypes, values: Property-To-column mappings
    private Map<FeatureType, Map<PropertyType, String>> ftMappings = new HashMap<FeatureType, Map<PropertyType, String>>();

    // NOTE: this is equal for all bound schemas
    private URL shapeFileURL;

    private String srsName;

    @Override
    public AnnotationDocument getAnnotationParser() {
        return new ShapeAnnotationDocument();
    }

    @Override
    public void bindSchema( MappedGMLSchema schema )
                            throws DatastoreException {
        super.bindSchema( schema );
        validate( schema );
        srsName = schema.getDefaultSRS().toString();
    }

    @Override
    public FeatureCollection performQuery( Query query, MappedFeatureType[] rootFts )
                            throws DatastoreException {

        if ( rootFts.length > 1 ) {
            String msg = Messages.getMessage( "DATASTORE_SHAPE_DOES_NOT_SUPPORT_JOINS" );
            throw new DatastoreException( msg );
        }

        MappedFeatureType ft = rootFts[0];

        // perform CRS transformation (if necessary)
        Query transformedQuery = transformQuery( query );

        // determine which properties must be contained in the returned features
        Map<PropertyType, String> fetchPropsToColumns = determineSelectedProps( ft, transformedQuery );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "Selected properties / columns from the shapefile:" );
            for ( PropertyType pt : fetchPropsToColumns.keySet() ) {
                LOG.logDebug( "- " + pt.getName() + " / " + fetchPropsToColumns.get( pt ) );
            }
        }

        // determine the properties that have to be removed after filter evaluation (because they
        // are not requested, but used inside the filter expression)
        Set<PropertyType> filterProps = determineFilterProps( ft, transformedQuery.getFilter() );
        Set<PropertyType> removeProps = new HashSet<PropertyType>( filterProps );
        for ( PropertyType pt : fetchPropsToColumns.keySet() ) {
            removeProps.remove( pt );
        }

        // add all property-to-column mappings for properties needed for the filter evaluation
        Map<PropertyType, String> allPropsToCols = this.ftMappings.get( ft );
        for ( PropertyType pt : filterProps ) {
            fetchPropsToColumns.put( pt, allPropsToCols.get( pt ) );
        }

        FeatureCollection result = null;
        ShapeFile shapeFile = null;
        int startPosition = -1;
        int maxFeatures = -1;

        int record = -1;
        try {
            LOG.logDebug( "Opening shapefile '" + shapeFileURL.getFile() + ".shp'." );
            shapeFile = new ShapeFile( shapeFileURL.getFile() );
            startPosition = transformedQuery.getStartPosition()-1;
            maxFeatures = transformedQuery.getMaxFeatures();
            Filter filter = transformedQuery.getFilter();
            Envelope bbox = null;
            if ( filter instanceof ComplexFilter ) {
                bbox = FilterTools.firstBBOX( (ComplexFilter) filter );
            }
            if ( bbox == null ) {
                bbox = shapeFile.getFileMBR();
            }

            shapeFile.setFeatureType( ft, fetchPropsToColumns );

            int[] idx = shapeFile.getGeoNumbersByRect( bbox );
            // id=identity required
            IDGenerator idg = IDGenerator.getInstance();
            String id = ft.getName().getLocalName();
            id += idg.generateUniqueID();
            if ( idx != null ) {
                // check parameters for sanity
                if ( startPosition < 0 ) {
                    startPosition = 0;
                }
                maxFeatures = maxFeatures + startPosition;
                if ( ( maxFeatures < 0 ) || ( maxFeatures >= idx.length ) ) {
                    maxFeatures = idx.length;
                }
                LOG.logDebug( "Generating ID '" + id + "' for the FeatureCollection." );
                result = FeatureFactory.createFeatureCollection( id, idx.length );

                // TODO: respect startposition

                CoordinateSystem crs = CRSFactory.create( srsName );
                for ( int i = startPosition; i < maxFeatures; i++ ) {
                    record = idx[i];
                    Feature feat = shapeFile.getFeatureByRecNo( idx[i] );
                    if ( filter == null || filter.evaluate( feat ) ) {
                        String msg = StringTools.concat( 200, "Adding feature '", feat.getId(),
                                                         "' to FeatureCollection (with CRS ", srsName, ")." );
                        LOG.logDebug( msg );
                        for ( PropertyType unrequestedPt : removeProps ) {
                            msg = StringTools.concat( 200, "Removing unrequested property '", unrequestedPt.getName(),
                                                      "' from feature: filter expression used it." );
                            LOG.logDebug( msg );
                            feat.removeProperty( unrequestedPt.getName() );
                        }
                        GeometryImpl geom = (GeometryImpl) feat.getDefaultGeometryPropertyValue();
                        geom.setCoordinateSystem( crs );
                        feat.setEnvelopesUpdated();
                        result.add( feat );
                    }
                }

                // update the envelopes
                result.setEnvelopesUpdated();
                result.getBoundedBy();
            } else {
                result = FeatureFactory.createFeatureCollection( id, 1 );
            }
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "DATASTORE_READINGFROMDBF", record );
            throw new DatastoreException( msg, e );
        } catch ( DBaseException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "DATASTORE_READINGFROMDBF", record );
            throw new DatastoreException( msg, e );
        } catch ( HasNoDBaseFileException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "DATASTORE_NODBASEFILE", record );
            throw new DatastoreException( msg, e );
        } catch ( FilterEvaluationException e ) {
            throw new DatastoreException( e.getMessage(), e );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "DATASTORE_READINGFROMDBF", record );
            throw new DatastoreException( msg, e );
        } finally {
            LOG.logDebug( "Closing shapefile." );
            try {
                shapeFile.close();
            } catch ( Exception e ) {
                String msg = Messages.getMessage( "DATASTORE_ERROR_CLOSING_SHAPEFILE", this.shapeFileURL.getFile() );
                throw new DatastoreException( msg );
            }
        }

        // transform result to queried srs if necessary
        String targetSrsName = transformedQuery.getSrsName();
        if ( targetSrsName != null && !targetSrsName.equals( this.srsName ) ) {
            result = transformResult( result, transformedQuery.getSrsName() );
        }

        return result;
    }

    /**
     * Determines the {@link PropertyType}s of the given feature type that are selected by the given {@link Query}
     * implicitly and explicitly, i.e that are either listed or that have a <code>minOccurs</code> value greater than
     * one. *
     *
     * @param ft
     *            feature type
     * @param query
     * @return all properties that need to be fetched, mapped to the shapefile columns that store them
     * @throws PropertyPathResolvingException
     *             if a selected property does not denote a property of the feature type
     */
    private Map<PropertyType, String> determineSelectedProps( MappedFeatureType ft, Query query )
                            throws PropertyPathResolvingException {

        Map<PropertyType, String> allPropsToCols = this.ftMappings.get( ft );
        Map<PropertyType, String> fetchPropsToCols = new HashMap<PropertyType, String>();
        // TODO: respect aliases
        PropertyPath[] selectedPaths = PropertyPathResolver.normalizePropertyPaths( ft, null, query.getPropertyNames() );
        // TODO respect alias
        Map<MappedPropertyType, Collection<PropertyPath>> fetchProps = PropertyPathResolver.determineFetchProperties(
                                                                                                                      ft,
                                                                                                                      null,
                                                                                                                      selectedPaths );
        for ( MappedPropertyType pt : fetchProps.keySet() ) {
            fetchPropsToCols.put( pt, allPropsToCols.get( pt ) );
        }
        return fetchPropsToCols;
    }

    /**
     * Determines the {@link PropertyType}s that are necessary to apply the given {@link Filter} expression, i.e. the
     * <code>PropertyNames</code> that occur in it.
     *
     * @see PropertyPathResolver#determineFetchProperties(MappedFeatureType, String, PropertyPath[])
     * @param ft
     *            feature type on which the filter shall be applicable
     * @param filter
     *            filter expression
     * @return all <code>PropertyType</code>s that are referenced inside the filter
     * @throws PropertyPathResolvingException
     */
    private Set<PropertyType> determineFilterProps( MappedFeatureType ft, Filter filter )
                            throws PropertyPathResolvingException {

        Set<PropertyType> filterPts = new HashSet<PropertyType>();
        if ( filter != null && filter instanceof ComplexFilter ) {
            ComplexFilter complexFilter = (ComplexFilter) filter;
            Operation operation = complexFilter.getOperation();
            addFilterProps( ft, filterPts, operation );
        }
        return filterPts;
    }

    private void addFilterProps( MappedFeatureType ft, Set<PropertyType> filterPts, Operation operation )
                            throws PropertyPathResolvingException {

        if ( operation instanceof ComparisonOperation ) {
            if ( operation instanceof PropertyIsBetweenOperation ) {
                PropertyIsBetweenOperation betweenOperation = (PropertyIsBetweenOperation) operation;
                filterPts.add( getFilterProperty( ft, betweenOperation.getPropertyName() ) );
            } else if ( operation instanceof PropertyIsCOMPOperation ) {
                PropertyIsCOMPOperation compOperation = (PropertyIsCOMPOperation) operation;
                Expression firstExpression = compOperation.getFirstExpression();
                Expression secondExpression = compOperation.getSecondExpression();
                if ( firstExpression instanceof PropertyName ) {
                    filterPts.add( getFilterProperty( ft, (PropertyName) firstExpression ) );
                }
                if ( secondExpression instanceof PropertyName ) {
                    filterPts.add( getFilterProperty( ft, (PropertyName) secondExpression ) );
                }
            } else if ( operation instanceof PropertyIsInstanceOfOperation ) {
                PropertyIsInstanceOfOperation instanceOfOperation = (PropertyIsInstanceOfOperation) operation;
                filterPts.add( getFilterProperty( ft, instanceOfOperation.getPropertyName() ) );
            } else if ( operation instanceof PropertyIsLikeOperation ) {
                PropertyIsLikeOperation likeOperation = (PropertyIsLikeOperation) operation;
                filterPts.add( getFilterProperty( ft, likeOperation.getPropertyName() ) );
            } else if ( operation instanceof PropertyIsNullOperation ) {
                PropertyIsNullOperation nullOperation = (PropertyIsNullOperation) operation;
                filterPts.add( getFilterProperty( ft, nullOperation.getPropertyName() ) );
            } else {
                assert false;
            }
        } else if ( operation instanceof LogicalOperation ) {
            LogicalOperation logicalOperation = (LogicalOperation) operation;
            for ( Operation subOperation : logicalOperation.getArguments() ) {
                addFilterProps( ft, filterPts, subOperation );
            }
        } else if ( operation instanceof SpatialOperation ) {
            SpatialOperation spatialOperation = (SpatialOperation) operation;
            filterPts.add( getFilterProperty( ft, spatialOperation.getPropertyName() ) );
        } else {
            assert false;
        }
    }

    private PropertyType getFilterProperty( MappedFeatureType ft, PropertyName propName )
                            throws PropertyPathResolvingException {

        // TODO respect aliases
        PropertyPath path = PropertyPathResolver.normalizePropertyPath( ft, null, propName.getValue() );

        QualifiedName propStep = path.getStep( 1 ).getPropertyName();
        PropertyType pt = ft.getProperty( propStep );
        if ( pt == null ) {
            String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_RESOLVE4", path, 2, propStep, ft.getName(),
                                              propName );
            throw new PropertyPathResolvingException( msg );
        }
        return pt;
    }

    @Override
    public FeatureCollection performQuery( final Query query, final MappedFeatureType[] rootFts,
                                           final DatastoreTransaction context )
                            throws DatastoreException {
        return performQuery( query, rootFts );
    }

    /**
     * Validates the given {@link MappedGMLSchema} against the available columns in the referenced shape file.
     *
     * @param schema
     * @throws DatastoreException
     */
    private void validate( MappedGMLSchema schema )
                            throws DatastoreException {

        Set<String> columnNames = determineShapeFileColumns( schema );

        FeatureType[] featureTypes = schema.getFeatureTypes();
        for ( int i = 0; i < featureTypes.length; i++ ) {
            Map<PropertyType, String> ftMapping = getFTMapping( featureTypes[i], columnNames );
            ftMappings.put( featureTypes[i], ftMapping );
        }
    }

    private Map<PropertyType, String> getFTMapping( FeatureType ft, Set<String> columnNames )
                            throws DatastoreException {
        Map<PropertyType, String> ftMapping = new HashMap<PropertyType, String>();
        PropertyType[] properties = ft.getProperties();
        for ( int i = 0; i < properties.length; i++ ) {
            MappedPropertyType pt = (MappedPropertyType) properties[i];
            if ( pt instanceof MappedSimplePropertyType ) {
                SimpleContent content = ( (MappedSimplePropertyType) pt ).getContent();
                if ( !( content instanceof MappingField ) ) {
                    String msg = Messages.getMessage( "DATASTORE_UNSUPPORTED_CONTENT", pt.getName() );
                    throw new DatastoreException( msg );
                }
                // ensure that field name is in uppercase
                String field = ( (MappingField) content ).getField().toUpperCase();
                if ( !columnNames.contains( field ) ) {
                    String msg = Messages.getMessage( "DATASTORE_FIELDNOTFOUND", field, pt.getName(),
                                                      shapeFileURL.getFile(), columnNames );
                    throw new DatastoreException( msg );
                }
                ftMapping.put( pt, field );
            } else if ( pt instanceof MappedGeometryPropertyType ) {
                // nothing to do
            } else {
                String msg = Messages.getMessage( "DATASTORE_NO_NESTED_FEATURE_TYPES", pt.getName() );
                throw new DatastoreException( msg );
            }
        }
        return ftMapping;
    }

    /**
     * Determines the column names (in uppercase) of the shape file that is referenced by the given schema.
     *
     * @param schema
     * @return column names (in uppercase)
     * @throws DatastoreException
     */
    private Set<String> determineShapeFileColumns( MappedGMLSchema schema )
                            throws DatastoreException {

        Set<String> columnNames = new HashSet<String>();
        DBaseFile dbfFile = null;

        try {
            Element schemaRoot = schema.getDocument().getRootElement();
            String shapePath = XMLTools.getNodeAsString( schemaRoot, "xs:annotation/xs:appinfo/deegreewfs:File/text()",
                                                         nsContext, null );
            shapeFileURL = schema.getDocument().resolve( shapePath );
            LOG.logDebug( "Opening dbf file '" + shapeFileURL + "'." );
            dbfFile = new DBaseFile( shapeFileURL.getFile() );
            String[] columns = dbfFile.getProperties();
            for ( int i = 0; i < columns.length; i++ ) {
                columnNames.add( columns[i].toUpperCase() );
            }
            String s = "Successfully opened dbf file '" + shapeFileURL.getFile()
                       + "' and retrieved the property columns.";
            LOG.logDebug( s );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new DatastoreException( Messages.getMessage( "DATASTORE_DBACCESSERROR" ) );
        } finally {
            if ( dbfFile != null ) {
                dbfFile.close();
            }
        }

        return columnNames;
    }

    /**
     * Closes the datastore so it can free dependent resources.
     *
     * @throws DatastoreException
     */
    @Override
    public void close()
                            throws DatastoreException {
        // TODO
    }
}
