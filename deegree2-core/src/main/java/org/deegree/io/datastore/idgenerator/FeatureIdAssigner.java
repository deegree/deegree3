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
package org.deegree.io.datastore.idgenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.TimeTools;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLId;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert.ID_GEN;

/**
 * Responsible for the assigning of valid {@link FeatureId}s which are a prerequisite to the insertion of features in a
 * {@link Datastore}. For each {@link Insert} operation, a new <code>FeatureIdAssigner</code> instance is created.
 * <p>
 * The behaviour of {@link #assignFID(Feature, DatastoreTransaction)}} depends on the {@link ID_GEN} mode in use:
 * <table>
 * <tr>
 * <td>GenerateNew</td>
 * <td>Prior to the assigning of new feature ids, "equal" features are looked up in the datastore and their feature ids
 * are used.</td>
 * </tr>
 * <tr>
 * <td>UseExisting</td>
 * <td>
 * <ol>
 * <li>For every root feature, it is checked that a feature id is present and that no feature with the same id already
 * exists in the datastore.</li>
 * <li>"Equal" subfeatures are looked up in the datastore and their feature ids are used instead of the given fids --
 * if however an "equal" root feature is identified, an exception is thrown.</li>
 * </ol>
 * </td>
 * </tr>
 * <tr>
 * <td>ReplaceDuplicate</td>
 * <td>not supported yet</td>
 * </tr>
 * </table>
 *
 * @see DatastoreTransaction#performInsert(List)
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FeatureIdAssigner {

    /** if an assigned feature id starts with this, it is already stored */
    public static final String EXISTS_MARKER = "!";

    private static final ILogger LOG = LoggerFactory.getLogger( FeatureIdAssigner.class );

    private ID_GEN idGenMode;

    private Map<String, FeatureId> oldFid2NewFidMap = new HashMap<String, FeatureId>();

    private Set<Feature> reassignedFeatures = new HashSet<Feature>();

    private Set<Feature> storedFeatures = new HashSet<Feature>();

    /**
     * Creates a new <code>FeatureIdAssigner</code> instance that generates new feature ids as specified.
     *
     * @param idGenMode
     */
    public FeatureIdAssigner( ID_GEN idGenMode ) {
        this.idGenMode = idGenMode;
    }

    /**
     * Assigns valid {@link FeatureId}s to the given feature instance and it's subfeatures.
     *
     * @param feature
     * @param ta
     * @throws IdGenerationException
     */
    public void assignFID( Feature feature, DatastoreTransaction ta )
                            throws IdGenerationException {

        switch ( this.idGenMode ) {
        case GENERATE_NEW: {
            identifyStoredFeatures( feature, ta, new HashSet<Feature>() );
            generateAndAssignNewFIDs( feature, null, ta );
            break;
        }
        case REPLACE_DUPLICATE: {
            LOG.logInfo( "Idgen mode 'ReplaceDuplicate' is not implemented!" );
            break;
        }
        case USE_EXISTING: {
            checkForExistingFid( feature, ta );
            String oldFid = feature.getId();
            String equalFeature = identifyStoredFeatures( feature, ta, new HashSet<Feature>() );
            if ( equalFeature != null ) {
                String msg = "Cannot perform insert: a feature equal to a feature to be inserted (fid: '" + oldFid
                             + "') already exists in the datastore (existing fid: '" + equalFeature + "').";
                throw new IdGenerationException( msg );
            }
            break;
        }
        default: {
            throw new IdGenerationException( "Internal error: Unhandled fid generation mode: " + this.idGenMode );
        }
        }
    }

    /**
     * TODO mark stored features a better way
     */
    public void markStoredFeatures() {
        // hack: mark stored features (with "!")
        for ( Feature f : this.storedFeatures ) {
            String fid = f.getId();
            if ( !fid.startsWith( EXISTS_MARKER ) ) {
                f.setId( EXISTS_MARKER + fid );
            }
        }
    }

    private String identifyStoredFeatures( Feature feature, DatastoreTransaction ta, Set<Feature> inProcessing )
                            throws IdGenerationException {

        if ( this.reassignedFeatures.contains( feature ) ) {
            return feature.getId();
        }

        inProcessing.add( feature );

        boolean maybeEqual = true;
        String existingFID = null;

        LOG.logDebug( "Checking for existing feature that equals feature with type: '" + feature.getName()
                      + "' and fid: '" + feature.getId() + "'." );

        // build the comparison operations that are needed to select "equal" feature instances
        List<Operation> compOperations = new ArrayList<Operation>();

        FeatureProperty[] properties = feature.getProperties();
        MappedFeatureType ft = (MappedFeatureType) feature.getFeatureType();

        for ( int i = 0; i < properties.length; i++ ) {
            QualifiedName propertyName = properties[i].getName();
            MappedPropertyType propertyType = (MappedPropertyType) ft.getProperty( propertyName );

            Object propertyValue = properties[i].getValue();
            if ( propertyValue instanceof Feature ) {

                if ( inProcessing.contains( propertyValue ) ) {
                    LOG.logDebug( "Stopping recursion at property with '" + propertyName + "'. Cycle detected." );
                    continue;
                }

                LOG.logDebug( "Recursing on feature property: " + properties[i].getName() );
                String subFeatureId = identifyStoredFeatures( (Feature) propertyValue, ta, inProcessing );
                if ( propertyType.isIdentityPart() ) {
                    if ( subFeatureId == null ) {
                        maybeEqual = false;
                    } else {
                        LOG.logDebug( "Need to check for feature property '" + propertyName + "' with fid '"
                                      + subFeatureId + "'." );

                        // build path that selects subfeature 'gml:id' attribute
                        PropertyPath fidSelectPath = PropertyPathFactory.createPropertyPath( feature.getName() );
                        fidSelectPath.append( PropertyPathFactory.createPropertyPathStep( propertyName ) );
                        fidSelectPath.append( PropertyPathFactory.createPropertyPathStep( ( (Feature) propertyValue ).getName() ) );
                        QualifiedName qn = new QualifiedName( CommonNamespaces.GML_PREFIX, "id", CommonNamespaces.GMLNS );
                        fidSelectPath.append( PropertyPathFactory.createAttributePropertyPathStep( qn ) );

                        // hack that remove's the gml id prefix
                        MappedFeatureType subFeatureType = (MappedFeatureType) ( (Feature) propertyValue ).getFeatureType();
                        MappedGMLId gmlId = subFeatureType.getGMLId();
                        String prefix = gmlId.getPrefix();
                        if ( subFeatureId.indexOf( prefix ) != 0 ) {
                            throw new IdGenerationException( "Internal error: subfeature id '" + subFeatureId
                                                             + "' does not begin with the expected prefix." );
                        }
                        String plainIdValue = subFeatureId.substring( prefix.length() );
                        PropertyIsCOMPOperation propertyTestOperation = new PropertyIsCOMPOperation(
                                                                                                     OperationDefines.PROPERTYISEQUALTO,
                                                                                                     new PropertyName(
                                                                                                                       fidSelectPath ),
                                                                                                     new Literal(
                                                                                                                  plainIdValue ) );

                        compOperations.add( propertyTestOperation );
                    }
                } else
                    LOG.logDebug( "Skipping property '" + propertyName
                                  + "': not a part of the feature type's identity." );
            } else if ( propertyValue instanceof Geometry ) {

                if ( propertyType.isIdentityPart() ) {
                    throw new IdGenerationException( "Check for equal geometry properties "
                                                     + "is not implemented yet. Do not set "
                                                     + "identityPart to true for geometry properties." );
                }

            } else {
                if ( propertyType.isIdentityPart() ) {
                    LOG.logDebug( "Need to check for simple property '" + propertyName + "' with value '"
                                  + propertyValue + "'." );

                    String value = propertyValue.toString();
                    if ( propertyValue instanceof Date ) {
                        value = TimeTools.getISOFormattedTime( (Date) propertyValue );
                    }

                    PropertyIsCOMPOperation propertyTestOperation = new PropertyIsCOMPOperation(
                                                                                                 OperationDefines.PROPERTYISEQUALTO,
                                                                                                 new PropertyName(
                                                                                                                   propertyName ),
                                                                                                 new Literal( value ) );
                    compOperations.add( propertyTestOperation );
                } else {
                    LOG.logDebug( "Skipping property '" + propertyName
                                  + "': not a part of the feature type's identity." );
                }
            }
        }

        if ( ft.getGMLId().isIdentityPart() ) {
            maybeEqual = false;
            LOG.logDebug( "Skipping check for identical features: feature id is part of " + "the feature identity." );
        }
        if ( maybeEqual ) {
            // build the filter from the comparison operations
            Filter filter = null;
            if ( compOperations.size() == 0 ) {
                // no constraints, so any feature of this type will do
            } else if ( compOperations.size() == 1 ) {
                filter = new ComplexFilter( compOperations.get( 0 ) );
            } else {
                LogicalOperation andOperation = new LogicalOperation( OperationDefines.AND, compOperations );
                filter = new ComplexFilter( andOperation );
            }
            if ( filter != null ) {
                LOG.logDebug( "Performing query with filter: " + filter.to110XML() );
            } else {
                LOG.logDebug( "Performing unrestricted query." );
            }
            Query query = Query.create( new PropertyPath[0], null, null, null, null,
                                        new QualifiedName[] { feature.getName() }, null, null, filter, 1, 0,
                                        GetFeature.RESULT_TYPE.RESULTS );

            try {
                FeatureCollection fc = ft.performQuery( query, ta );
                if ( fc.size() > 0 ) {
                    existingFID = fc.getFeature( 0 ).getId();
                    LOG.logDebug( "Found existing + matching feature with fid: '" + existingFID + "'." );
                } else {
                    LOG.logDebug( "No matching feature found." );
                }
            } catch ( DatastoreException e ) {
                throw new IdGenerationException( "Could not perform query to check for "
                                                 + "existing feature instances: " + e.getMessage(), e );
            } catch ( UnknownCRSException e ) {
                LOG.logError( e.getMessage(), e );
            }
        }

        if ( existingFID != null ) {
            LOG.logDebug( "Feature '" + feature.getName() + "', FID '" + feature.getId() + "' -> existing FID '"
                          + existingFID + "'" );
            feature.setId( existingFID );
            this.storedFeatures.add( feature );
            this.reassignedFeatures.add( feature );
            changeValueForMappedIDProperties( ft, feature );
        }

        return existingFID;
    }

    /**
     * TODO: remove parentFID hack
     *
     * @param feature
     * @param parentFID
     * @throws IdGenerationException
     */
    private void generateAndAssignNewFIDs( Feature feature, FeatureId parentFID, DatastoreTransaction ta )
                            throws IdGenerationException {

        FeatureId newFid = null;
        MappedFeatureType ft = (MappedFeatureType) feature.getFeatureType();

        if ( this.reassignedFeatures.contains( feature ) ) {
            LOG.logDebug( "Skipping feature with fid '" + feature.getId() + "'. Already reassigned." );
            return;
        }

        this.reassignedFeatures.add( feature );
        String oldFidValue = feature.getId();
        if ( oldFidValue == null || "".equals( oldFidValue ) ) {
            LOG.logDebug( "Feature has no FID. Assigning a new one." );
        } else {
            newFid = this.oldFid2NewFidMap.get( oldFidValue );
        }
        if ( newFid == null ) {
            // TODO remove these hacks
            if ( ft.getGMLId().getIdGenerator() instanceof ParentIDGenerator ) {
                newFid = new FeatureId( ft, parentFID.getValues() );
            } else {
                newFid = ft.generateFid( ta );
            }
            this.oldFid2NewFidMap.put( oldFidValue, newFid );
        }

        LOG.logDebug( "Feature '" + feature.getName() + "', FID '" + oldFidValue + "' -> new FID '" + newFid + "'" );
        // TODO use FeatureId, not it's String value
        feature.setId( newFid.getAsString() );
        changeValueForMappedIDProperties( ft, feature );

        FeatureProperty[] properties = feature.getProperties();
        for ( int i = 0; i < properties.length; i++ ) {
            Object propertyValue = properties[i].getValue();
            if ( propertyValue instanceof Feature ) {
                generateAndAssignNewFIDs( (Feature) propertyValue, newFid, ta );
            }
        }
    }

    /**
     * After reassigning a feature id, this method updates all properties of the feature that are mapped to the same
     * column as the feature id.
     *
     * TODO: find a better way to do this
     *
     * @param ft
     * @param feature
     */
    private void changeValueForMappedIDProperties( MappedFeatureType ft, Feature feature ) {
        // TODO remove this hack as well
        String pkColumn = ft.getGMLId().getIdFields()[0].getField();

        FeatureProperty[] properties = feature.getProperties();
        for ( int i = 0; i < properties.length; i++ ) {
            MappedPropertyType propertyType = (MappedPropertyType) ft.getProperty( properties[i].getName() );
            if ( propertyType instanceof MappedSimplePropertyType ) {
                SimpleContent content = ( (MappedSimplePropertyType) propertyType ).getContent();
                if ( content.isUpdateable() ) {
                    if ( content instanceof MappingField ) {
                        String column = ( (MappingField) content ).getField();
                        if ( column.equalsIgnoreCase( pkColumn ) ) {
                            Object fid = null;
                            try {
                                fid = FeatureId.removeFIDPrefix( feature.getId(), ft.getGMLId() );
                            } catch ( DatastoreException e ) {
                                e.printStackTrace();
                            }
                            properties[i].setValue( fid );
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks that the {@link Datastore} contains no feature with the same id as the given feature.
     *
     * @param feature
     * @param ta
     * @throws IdGenerationException
     */
    private void checkForExistingFid( Feature feature, DatastoreTransaction ta )
                            throws IdGenerationException {

        MappedFeatureType ft = (MappedFeatureType) feature.getFeatureType();
        LOG.logDebug( "Checking for existing feature of type: '" + ft.getName() + "' and with fid: '" + feature.getId()
                      + "'." );

        // build a filter that matches the feature id
        FeatureFilter filter = new FeatureFilter();
        filter.addFeatureId( new org.deegree.model.filterencoding.FeatureId( feature.getId() ) );
        Query query = Query.create( new PropertyPath[0], null, null, null, null, new QualifiedName[] { ft.getName() },
                                    null, null, filter, 1, 0, GetFeature.RESULT_TYPE.HITS );
        try {
            FeatureCollection fc = ft.performQuery( query, ta );
            int numFeatures = Integer.parseInt( fc.getAttribute( "numberOfFeatures" ) );
            if ( numFeatures > 0 ) {
                LOG.logInfo( "Found existing feature with fid '" + feature.getId() + "'." );
                String msg = "Cannot perform insert: a feature with fid '" + feature.getId()
                             + "' already exists in the datastore (and idGen='UseExisting').";
                throw new IdGenerationException( msg );
            }
            LOG.logDebug( "No feature with fid '" + feature.getId() + "' found." );
        } catch (IdGenerationException e) {
            throw e;
        } catch ( DatastoreException e ) {
            throw new IdGenerationException( "Could not perform query to check for existing feature instance: "
                                             + e.getMessage(), e );
        } catch ( UnknownCRSException e ) {
            LOG.logDebug (e.getMessage(), e);
        }
    }
}
