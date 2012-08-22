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

package org.deegree.ogcwebservices.wcts.capabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.transformations.Transformation;
import org.deegree.crs.transformations.TransformationFactory;
import org.deegree.crs.transformations.helmert.Helmert;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.ogcwebservices.wcts.capabilities.mdprofiles.MetadataProfile;
import org.deegree.ogcwebservices.wcts.capabilities.mdprofiles.TransformationMetadata;
import org.deegree.owscommon_1_1_0.Metadata;

/**
 * <code>Content</code> encapsulates the Content element of the WCTS_0.4.0 Capabilities document.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Content {

    private static ILogger LOG = LoggerFactory.getLogger( Content.class );

    private final Map<String, Transformation> transformations;

    private final List<String> methods;

    private List<CoordinateSystem> sourceCRSs;

    private List<CoordinateSystem> targetCRSs;

    private final CoverageAbilities coverageAbilities;

    private final FeatureAbilities featureAbilities;

    private final List<Metadata> metadata;

    private final boolean userDefinedCRS;

    private final List<MetadataProfile<?>> transformMetadata;

    /**
     * @param configuredTransforms
     *            Unordered list of zero or more identifiers of well-known coordinate operations which the server can
     *            perform.
     * @param methods
     *            Unordered list of zero or more identifiers of well-known operation methods which the server can apply
     *            in user-defined coordinate Transformations and Conversions.
     * @param sourceCRSs
     *            Unordered list of one or more identifiers of well-known CRSs in which the server can accept sourceCRS
     *            values.
     * @param targetCRSs
     *            Unordered list of one or more identifiers of well-known CRSs in which the server can accept targetCRS
     *            values.
     * @param coverageAbilities
     *            Specifies coverage transformation abilities of WCTS server.
     * @param featureAbilities
     *            Specifies feature transformation abilities of WCTS server.
     * @param metadata
     *            Optional unordered list of additional metadata about the data served by this WCTS implementation. For
     *            example, this metadata could include more detailed definitions of the Methods, Transformations, and
     *            CRSs known to this server, perhaps in the form of a gml:Dictionary of such information.
     * @param userDefinedCRS
     *            Specifies if this server supports user-defined Coordinate Reference Systems (CRSs).
     * @param transformMetadata
     */
    public Content( Map<String, Transformation> configuredTransforms, List<String> methods,
                    List<CoordinateSystem> sourceCRSs, List<CoordinateSystem> targetCRSs,
                    CoverageAbilities coverageAbilities, FeatureAbilities featureAbilities, List<Metadata> metadata,
                    boolean userDefinedCRS, List<MetadataProfile<?>> transformMetadata ) {
        if ( configuredTransforms == null ) {
            this.transformations = new HashMap<String, Transformation>();
        } else {
            this.transformations = configuredTransforms;
        }
        this.methods = methods;
        this.sourceCRSs = sourceCRSs;
        this.targetCRSs = targetCRSs;
        this.coverageAbilities = coverageAbilities;
        this.featureAbilities = featureAbilities;
        if ( transformMetadata == null ) {
            this.transformMetadata = new ArrayList<MetadataProfile<?>>();
        } else {
            this.transformMetadata = transformMetadata;
        }
        if ( metadata == null ) {
            this.metadata = new ArrayList<Metadata>();
        } else {
            this.metadata = metadata;
        }
        this.userDefinedCRS = userDefinedCRS;
        this.transformMetadata.addAll( createMDForConfiguredTransforms( transformations, transformMetadata ) );
    }

    /**
     * @return the transformations.
     */
    public final Map<String, Transformation> getTransformations() {
        return transformations;
    }

    /**
     * @return the methods.
     */
    public final List<String> getMethods() {
        return methods;
    }

    /**
     * @return the sourceCRSs.
     */
    public final List<CoordinateSystem> getSourceCRSs() {
        return sourceCRSs;
    }

    /**
     * @return the targetCRSs.
     */
    public final List<CoordinateSystem> getTargetCRSs() {
        return targetCRSs;
    }

    /**
     * @return the coverageAbilities.
     */
    public final CoverageAbilities getCoverageAbilities() {
        return coverageAbilities;
    }

    /**
     * @return the featureAbilities.
     */
    public final FeatureAbilities getFeatureAbilities() {
        return featureAbilities;
    }

    /**
     * @return the metadatas, may be empty but will never be <code>null</code>
     */
    public final List<Metadata> getMetadata() {
        return metadata;
    }

    /**
     * @return the userDefinedCRS.
     */
    public final boolean supportsUserDefinedCRS() {
        return userDefinedCRS;
    }

    /**
     * @return the transformMetadata elements.
     */
    public final List<MetadataProfile<?>> getTransformMetadata() {
        return transformMetadata;
    }

    /**
     * Create the transformations and their MetaData for all source / target combinations, e.g. create identifiers for
     * the default transformations of the crs package which is the usage of a wgs 84 pivot crs and the helmert
     * transformation.
     * 
     * @param transformationPrefix
     *            to be used for the transformations
     */
    public void describeDefaultTransformations( String transformationPrefix ) {
        List<TransformationMetadata> result = new ArrayList<TransformationMetadata>( transformMetadata.size() );
        List<String> allKeys = new LinkedList<String>( transformations.keySet() );
        int tCount = 0;
        for ( CoordinateSystem sourceCRS : sourceCRSs ) {
            if ( sourceCRS != null ) {
                // lets create metadatas for all target crs's.
                for ( CoordinateSystem tCRS : targetCRSs ) {
                    if ( !sourceCRS.getCRS().equals( tCRS.getCRS() ) ) {
                        Transformation trans = createTransformation( sourceCRS, tCRS );
                        if ( trans != null ) {
                            // create a unique key.
                            String key = transformationPrefix + tCount;
                            while ( allKeys.contains( key ) ) {
                                key = transformationPrefix + ( ++tCount );
                            }
                            allKeys.add( key );
                            result.add( new TransformationMetadata( trans, key, sourceCRS, tCRS,
                                                                    createTransformationMDDescription( sourceCRS, tCRS ) ) );
                            // add it to the configured transforms as well.
                            transformations.put( key, trans );
                        }
                    }
                }
            }
        }
        transformMetadata.addAll( result );
    }

    /**
     * Create a description saying:<code>
     * Transforming from sourceCRS.getIdentifer (sourceCRS.getName() ) to targetCRS.getIdentifer (targetCRS.getName() ) using the helmert transformation.
     * </code>
     * if either the sourceCRS or the targetCRS are null the String "Transform using the helmert transformation." will
     * be returned.
     * 
     * @param sourceCRS
     * @param targetCRS
     * @return a possible description for a Transformation Metadata.
     */
    protected String createTransformationMDDescription( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        if ( sourceCRS == null || targetCRS == null ) {
            return "Transform using the helmert transformation.";
        }
        return "Transforming from " + sourceCRS.getIdentifier()
               + ( ( sourceCRS.getCRS().getName() == null ) ? "" : " (" + sourceCRS.getCRS().getName() + ")" ) + " to "
               + targetCRS.getIdentifier()
               + ( ( targetCRS.getCRS().getName() == null ) ? "" : " (" + targetCRS.getCRS().getName() + ")" )
               + " using the helmert transformation.";
    }

    /**
     * Creates a {@link TransformationMetadata} for the configured transformations and create Transformations for
     * {@link TransformationMetadata} which do not have a {@link Transformation}.
     * 
     * @param configuredTransformations
     *            to which the transformations will be added
     * @param transformationMetadata
     *            containing all configured {@link TransformationMetadata}
     * 
     * @return The list of {@link TransformationMetadata} that were created.
     */
    private List<TransformationMetadata> createMDForConfiguredTransforms(
                                                                          Map<String, Transformation> configuredTransformations,
                                                                          List<MetadataProfile<?>> transformationMetadata ) {
        List<TransformationMetadata> result = new ArrayList<TransformationMetadata>( transformationMetadata.size() );
        for ( String key : configuredTransformations.keySet() ) {
            if ( key != null && !"".equals( key ) ) {
                LOG.logDebug( "Finding transformation metadata for key:" + key );
                TransformationMetadata sync = null;
                for ( MetadataProfile<?> mp : transformationMetadata ) {
                    if ( mp != null && ( mp instanceof TransformationMetadata ) ) {
                        if ( key.equalsIgnoreCase( ( (TransformationMetadata) mp ).getTransformID() ) ) {
                            if ( sync != null ) {
                                LOG.logWarning( "The key: " + key + " has multiple metadatas, this may not be." );
                            } else {
                                LOG.logDebug( "Found a metadata for key: ", key );
                                sync = (TransformationMetadata) mp;
                                if ( configuredTransformations.get( sync.getTransformID() ) == null
                                     || configuredTransformations.get( sync.getTransformID() ) instanceof Helmert ) {
                                    LOG.logDebug( "The transformation metadata referencing transformation: ",
                                                  sync.getTransformID(),
                                                  " did not have a transformation, creating a default one." );
                                    Transformation trans = createTransformation( sync.getSourceCRS(),
                                                                                 sync.getTargetCRS() );
                                    if ( trans != null ) {
                                        configuredTransformations.put( key, trans );
                                    }
                                }
                            }
                        }
                    }
                }
                if ( sync == null ) {
                    Transformation trans = configuredTransformations.get( key );
                    if ( trans != null ) {
                        LOG.logDebug( "Creating a metadata for key: ", key );
                        result.add( new TransformationMetadata( trans, key, CRSFactory.create( trans.getSourceCRS() ),
                                                                CRSFactory.create( trans.getTargetCRS() ),
                                                                trans.getDescription() ) );
                    } else {
                        LOG.logWarning( "Unable to create metadata from id: " + key
                                        + " because no transformation was given." );
                    }

                }
            }
        }
        return result;
    }

    /**
     * Uses the configured identifiers of the source and targets to create new (cached) coordinatesystems.
     * 
     * @param configuredProvider
     *            may be <code>null</code> to use the default crs provider.
     * 
     */
    public synchronized void updateFromProvider( String configuredProvider ) {
        LOG.logWarning( "Updating the transformations is currently not supported." );
        List<CoordinateSystem> newCRSs = updateCRSs( configuredProvider, sourceCRSs );
        sourceCRSs = newCRSs;

        newCRSs = updateCRSs( configuredProvider, targetCRSs );
        targetCRSs = newCRSs;
    }

    private List<CoordinateSystem> updateCRSs( String configuredProvider, List<CoordinateSystem> crss ) {
        List<CoordinateSystem> newCRSs = new ArrayList<CoordinateSystem>( crss.size() );
        for ( CoordinateSystem crs : crss ) {
            if ( crs != null ) {
                CoordinateSystem newCRS = null;
                try {
                    newCRS = CRSFactory.create( configuredProvider, crs.getIdentifier() );
                } catch ( UnknownCRSException e ) {
                    LOG.logError( e );
                }
                if ( newCRS != null ) {
                    newCRSs.add( newCRS );
                } else {
                    LOG.logWarning( "Removing old crs with id: " + crs.getIdentifier()
                                    + " because it is no longer available in the crs registry." );
                }
            }
        }
        return newCRSs;
    }

    private Transformation createTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        Transformation result = null;
        try {
            result = TransformationFactory.getInstance().createFromCoordinateSystems( sourceCRS.getCRS(),
                                                                                      targetCRS.getCRS() );
        } catch ( IllegalArgumentException e ) {
            LOG.logError( "Error while creating a default transformation for sourceCRS: " + sourceCRS, e );
        } catch ( TransformationException e ) {
            LOG.logError( "Error while creating a default transformation for sourceCRS: " + sourceCRS, e );
        }
        return result;

    }

}
