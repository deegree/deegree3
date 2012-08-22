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
package org.deegree.io.datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcbase.ElementStep;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;

/**
 * Helper class that resolves {@link PropertyPath} instances (e.g. PropertyName elements in {@link GetFeature}) against
 * the property structure of {@link MappedFeatureType}s.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class PropertyPathResolver {

    private static final ILogger LOG = LoggerFactory.getLogger( PropertyPathResolver.class );

    /**
     * Ensures that the requested property begins with a "feature type step" (this may also be an alias).
     * <p>
     * This is necessary, as section 7.4.2 of the Web Feature Implementation Specification 1.1.0 states: <br/>
     * <br/>
     * The first step of a relative location path <b>may</b> correspond to the root element of the feature property
     * being referenced <b>or</b> to the root element of the feature type with the next step corresponding to the root
     * element of the feature property being referenced.
     * </p>
     *
     * @param ft
     *            featureType that the requested properties refer to
     * @param alias
     *            alias for the feature type (may be null)
     * @param path
     *            requested property
     * @return normalized path, i.e. it begins with a feature type step
     */
    public static PropertyPath normalizePropertyPath( MappedFeatureType ft, String alias, PropertyPath path ) {
        QualifiedName ftName = ft.getName();
        QualifiedName firstStep = path.getStep( 0 ).getPropertyName();
        if ( !( firstStep.equals( ftName ) || ( alias != null && firstStep.equals( new QualifiedName( '$' + alias ) ) ) ) ) {
            if ( alias != null ) {
                path.prepend( PropertyPathFactory.createPropertyPathStep( new QualifiedName( '$' + alias ) ) );
            } else {
                path.prepend( PropertyPathFactory.createPropertyPathStep( ftName ) );
            }
        }
        return path;
    }

    /**
     * Ensures that all requested properties begin with a feature type step or an alias.
     * <p>
     * If no properties are specified at all, a single PropertyPath entry is created that selects the whole feature
     * type.
     * </p>
     *
     * @param ft
     *            feature type that the requested properties refer to
     * @param alias
     *            alias for the feature type, may be null
     * @param paths
     *            requested properties, may not be null
     * @return normalized paths
     */
    public static PropertyPath[] normalizePropertyPaths( MappedFeatureType ft, String alias, PropertyPath[] paths ) {

        PropertyPath[] normalizedPaths = new PropertyPath[paths.length];

        for ( int i = 0; i < paths.length; i++ ) {
            normalizedPaths[i] = normalizePropertyPath( ft, alias, paths[i] );
        }
        if ( paths.length == 0 ) {
            QualifiedName firstStep = ft.getName();
            if ( alias != null ) {
                firstStep = new QualifiedName( "$" + alias );
            }
            normalizedPaths = new PropertyPath[] { PropertyPathFactory.createPropertyPath( firstStep ) };
        }
        return normalizedPaths;
    }

    /**
     * Ensures that all requested properties begin with a feature type (or alias) step.
     * <p>
     * If no properties are specified for a feature type, a single {@link PropertyPath} entry is created that selects
     * the whole feature type.
     * </p>
     *
     * @param fts
     *            feature types that the requested properties refer to
     * @param paths
     *            requested properties, may not be null
     * @return normalized paths
     * @throws PropertyPathResolvingException
     */
    public static List<PropertyPath>[] normalizePropertyPaths( MappedFeatureType[] fts, String[] ftAliases,
                                                               PropertyPath[] paths )
                            throws PropertyPathResolvingException {

        List<PropertyPath>[] propLists = new List[fts.length];

        if ( fts.length == 1 ) {
            PropertyPath[] normalizedPaths = normalizePropertyPaths( fts[0], ftAliases != null ? ftAliases[0] : null,
                                                                     paths );
            List<PropertyPath> pathList = new ArrayList<PropertyPath>( normalizedPaths.length );
            for ( PropertyPath path : normalizedPaths ) {
                pathList.add( path );
            }
            propLists[0] = pathList;
        } else {
            for ( PropertyPath path : paths ) {
                QualifiedName firstStep = path.getStep( 0 ).getPropertyName();
                int i = 0;

                if ( ftAliases == null ) {
                    for ( i = 0; i < fts.length; i++ ) {
                        if ( fts[i].getName().equals( firstStep ) ) {
                            break;
                        }
                    }
                } else {
                    String localName = firstStep.getLocalName();
                    for ( i = 0; i < fts.length; i++ ) {
                        String fullAlias = '$' + ftAliases[i];
                        if ( fullAlias.equals( localName ) ) {
                            break;
                        }
                    }
                }

                if ( i < fts.length ) {
                    List props = propLists[i];
                    if ( props == null ) {
                        props = new ArrayList<PropertyPath>();
                        propLists[i] = props;
                    }
                    props.add( path );
                } else {
                    String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_RESOLVE5", path, firstStep );
                    throw new PropertyPathResolvingException( msg );
                }
            }

            for ( int i = 0; i < propLists.length; i++ ) {
                List<PropertyPath> list = propLists[i];
                if ( list == null ) {
                    list = new ArrayList<PropertyPath>( 1 );
                    propLists[i] = list;
                    if ( paths.length == 0 ) {
                        // only assume selection of every feature type if no feature type is
                        // selected explicitly
                        QualifiedName firstStep = fts[i].getName();
                        if ( ftAliases != null ) {
                            firstStep = new QualifiedName( "$" + ftAliases[i] );
                        }
                        PropertyPath ftSelect = PropertyPathFactory.createPropertyPath( firstStep );
                        list.add( ftSelect );
                    }
                }
            }
        }
        return propLists;
    }

    /**
     * Determines the properties of the given feature type that have to be fetched based on the requested property
     * paths.
     * <p>
     * Returns a helper <code>Map</code> that associates each (requested) property of the feature with the property
     * paths that request it. Note that the returned helper map may contain more properties than specified. This
     * behaviour is due to section 9.2 of the Web Feature Implementation Specification 1.1.0:
     * </p>
     * <p>
     * In the event that a WFS encounters a query that does not select all mandatory properties of a feature, the WFS
     * will internally augment the property name list to include all mandatory property names.
     * </p>
     * <p>
     * Note that every requested property path must begin with a step that selects the given feature type.
     * </p>
     *
     * @param ft
     *            feature type
     * @param alias
     *            alias for the feature type (may be null)
     * @param requestedPaths
     * @return <code>Map</code>, key class: <code>MappedPropertyType</code>, value class: <code>Collection</code> (of
     *         <code>PropertyPath</code> instances)
     * @throws PropertyPathResolvingException
     */
    public static Map<MappedPropertyType, Collection<PropertyPath>> determineFetchProperties(
                                                                                              MappedFeatureType ft,
                                                                                              String alias,
                                                                                              PropertyPath[] requestedPaths )
                            throws PropertyPathResolvingException {

        Map<MappedPropertyType, Collection<PropertyPath>> requestedMap = null;
        requestedMap = determineRequestedProperties( ft, alias, requestedPaths );
        if ( requestedPaths.length != 0 ) {
            // only augment mandatory properties, if any property is selected
            requestedMap = augmentFetchProperties( ft, alias, requestedMap );
        }
        return requestedMap;
    }

    /**
     * Builds a map that associates each requested property of the feature with the property paths that request it. The
     * property path may well select a property of a subfeature, but the key values in the map are always (direct)
     * properties of the given feature type.
     *
     * @param ft
     *            feature type
     * @param alias
     *            alias for the feature type (may be null)
     * @param requestedPaths
     * @return map that associates each requested property with the property paths that request it
     * @throws PropertyPathResolvingException
     */
    private static Map<MappedPropertyType, Collection<PropertyPath>> determineRequestedProperties(
                                                                                                   MappedFeatureType ft,
                                                                                                   String alias,

                                                                                                   PropertyPath[] requestedPaths )
                            throws PropertyPathResolvingException {

        Map<MappedPropertyType, Collection<PropertyPath>> propertyMap = new LinkedHashMap<MappedPropertyType, Collection<PropertyPath>>();

        LOG.logDebug( "Alias: " + alias );
        for ( int i = 0; i < requestedPaths.length; i++ ) {
            PropertyPath requestedPath = requestedPaths[i];
            QualifiedName firstStep = requestedPath.getStep( 0 ).getPropertyName();
            LOG.logDebug( "path " + i + ": " + requestedPaths[i] );
            if ( firstStep.equals( ft.getName() )
                 || ( alias != null && firstStep.equals( new QualifiedName( '$' + alias ) ) ) ) {
                if ( requestedPath.getSteps() == 1 ) {
                    // path requests the whole feature
                    PropertyType[] allProperties = ft.getProperties();
                    for ( int j = 0; j < allProperties.length; j++ ) {
                        Collection<PropertyPath> paths = propertyMap.get( allProperties[j] );
                        if ( paths == null ) {
                            paths = new ArrayList<PropertyPath>();
                        }
                        PropertyPath newPropertyPath = PropertyPathFactory.createPropertyPath( ft.getName() );
                        newPropertyPath.append( PropertyPathFactory.createPropertyPathStep( allProperties[j].getName() ) );
                        paths.add( newPropertyPath );
                        propertyMap.put( (MappedPropertyType) allProperties[j], paths );
                    }
                } else {
                    // path requests a certain property
                    QualifiedName propertyName = requestedPath.getStep( 1 ).getPropertyName();
                    PropertyType property = ft.getProperty( propertyName );
                    // quirk mode...
                    if ( property == null ) {
                        for ( PropertyType type : ft.getProperties() ) {
                            if ( type.getName().getLocalName().equals( propertyName.getLocalName() ) ) {
                                property = type;
                            }
                        }
                    }
                    if ( property == null ) {
                        // workaround for gml:boundedBy
                        if ("boundedBy".equals (propertyName.getLocalName())) {                            
                            continue;
                        }
                        String msg = Messages.getMessage( "DATASTORE_NO_SUCH_PROPERTY2", requestedPath, ft.getName(),
                                                          propertyName );
                        throw new PropertyPathResolvingException( msg );
                    }
                    Collection<PropertyPath> paths = propertyMap.get( property );
                    if ( paths == null ) {
                        paths = new ArrayList<PropertyPath>();
                    }
                    paths.add( requestedPath );
                    propertyMap.put( (MappedPropertyType) property, paths );
                }
            } else {
                String msg = "Internal error in PropertyPathResolver: no property with name '" + requestedPath
                             + "' in feature type '" + ft.getName() + "'.";
                throw new PropertyPathResolvingException( msg );
            }
        }
        return propertyMap;
    }

    /**
     * Returns an augmented version of the input map that contains additional entries for all mandatory properties of
     * the feature type.
     *
     * @param ft
     *            feature type
     * @param alias
     *            alias for the feature type (may be null)
     * @param requestedMap
     * @return augmented version of the input map
     */
    private static Map<MappedPropertyType, Collection<PropertyPath>> augmentFetchProperties(
                                                                                             MappedFeatureType ft,
                                                                                             String alias,
                                                                                             Map<MappedPropertyType, Collection<PropertyPath>> requestedMap ) {

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "Properties to be fetched for feature type '" + ft.getName() + "' (alias=" + alias + "):" );
        }

        Map<MappedPropertyType, Collection<PropertyPath>> augmentedMap = new LinkedHashMap<MappedPropertyType, Collection<PropertyPath>>();
        PropertyType[] allProperties = ft.getProperties();
        for ( int i = 0; i < allProperties.length; i++ ) {
            MappedPropertyType property = (MappedPropertyType) allProperties[i];
            Collection<PropertyPath> requestingPaths = requestedMap.get( property );
            if ( requestingPaths != null ) {
                LOG.logDebug( "- " + property.getName() );
                augmentedMap.put( property, requestingPaths );
                for ( PropertyPath path : requestingPaths ) {
                    LOG.logDebug( "  - Requested by path: '" + path + "'" );
                }
            } else if ( property.getMinOccurs() > 0 ) {
                LOG.logDebug( "- " + property.getName() + " (augmented)" );
                Collection<PropertyPath> mandatoryPaths = new ArrayList<PropertyPath>();
                List<PropertyPathStep> stepList = new ArrayList<PropertyPathStep>( 2 );
                stepList.add( new ElementStep( ft.getName() ) );
                stepList.add( new ElementStep( property.getName() ) );
                PropertyPath mandatoryPath = new PropertyPath( stepList );
                mandatoryPaths.add( mandatoryPath );
                augmentedMap.put( property, mandatoryPaths );
            }
        }
        return augmentedMap;
    }

    /**
     * Determines the sub property paths that are needed to fetch the given property paths for the also given property.
     *
     * @param featureType
     * @param propertyPaths
     * @return sub property paths that are needed to fetch the given property paths
     */
    public static PropertyPath[] determineSubPropertyPaths( MappedFeatureType featureType,
                                                            Collection<PropertyPath> propertyPaths ) {
        Collection<PropertyPath> subPropertyPaths = new ArrayList<PropertyPath>();

        Iterator<PropertyPath> iter = propertyPaths.iterator();
        while ( iter.hasNext() ) {
            PropertyPath path = iter.next();
            if ( path.getSteps() > 2 ) {
                subPropertyPaths.add( PropertyPathFactory.createPropertyPath( path, 2, path.getSteps() ) );
            } else {
                PropertyType[] subProperties = featureType.getProperties();
                for ( int i = 0; i < subProperties.length; i++ ) {
                    PropertyPath subPropertyPath = PropertyPathFactory.createPropertyPath( featureType.getName() );
                    subPropertyPath.append( PropertyPathFactory.createPropertyPathStep( subProperties[i].getName() ) );
                    subPropertyPaths.add( subPropertyPath );
                }
            }
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "Original property paths:" );
            for ( PropertyPath path : propertyPaths ) {
                LOG.logDebug( "- '" + path + "'" );
            }
            LOG.logDebug( "Sub feature property paths:" );
            for ( PropertyPath path : subPropertyPaths ) {
                LOG.logDebug( "- '" + path + "'" );
            }
        }
        PropertyPath[] subPaths = subPropertyPaths.toArray( new PropertyPath[subPropertyPaths.size()] );
        return subPaths;
    }
}
