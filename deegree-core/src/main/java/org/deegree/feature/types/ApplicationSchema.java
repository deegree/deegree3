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
package org.deegree.feature.types;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSNamespaceItemList;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.gml.schema.GMLSchemaAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a number of {@link FeatureType}s and their derivation hierarchy.
 * <p>
 * Some notes:
 * <ul>
 * <li>There is no default head for the feature type substitution relation as in GML (prior to GML 3.2: element
 * <code>gml:_Feature</code>, since 3.2: <code>gml:AbstractFeature</code>). This is not necessary, as each
 * {@link FeatureType} object is already identified being a feature type by its class.</li>
 * </ul>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class ApplicationSchema {

    private static final Logger LOG = LoggerFactory.getLogger( ApplicationSchema.class );

    private final Map<QName, FeatureType> ftNameToFt = new LinkedHashMap<QName, FeatureType>();

    // key: feature type A, value: feature type B (A is in substitutionGroup B)
    private final Map<FeatureType, FeatureType> ftToSuperFt = new HashMap<FeatureType, FeatureType>();

    // key: feature type A, value: feature types B0...Bn (A is in
    // substitutionGroup B0,
    // B0 is in substitutionGroup B1, ..., B(n-1) is in substitutionGroup Bn)
    private final Map<FeatureType, List<FeatureType>> ftToSuperFts = new HashMap<FeatureType, List<FeatureType>>();

    private GMLSchemaAnalyzer xsModel;

    /**
     * Creates a new {@link ApplicationSchema} instance from the given {@link FeatureType}s and their derivation
     * hierarchy.
     * 
     * @param fts
     *            all application feature types (abstract and non-abstract), this must not include any GML base feature
     *            types (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>), must not be
     *            <code>null</code>
     * @param ftToSuperFt
     *            key: feature type A, value: feature type B (A extends B), this must not include any GML base feature
     *            types (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>), can be <code>null</code>
     * @throws IllegalArgumentException
     *             if a feature type cannot be resolved (i.e. it is referenced in a property type, but not defined)
     */
    public ApplicationSchema( FeatureType[] fts, Map<FeatureType, FeatureType> ftToSuperFt )
                            throws IllegalArgumentException {

        for ( FeatureType ft : fts ) {
            ftNameToFt.put( ft.getName(), ft );
            ft.setSchema( this );
        }

        // build substitution group lookup maps
        if ( ftToSuperFt != null ) {
            this.ftToSuperFt.putAll( ftToSuperFt );

            for ( FeatureType ft : fts ) {
                List<FeatureType> substitutionGroups = new ArrayList<FeatureType>();
                FeatureType substitutionGroup = ftToSuperFt.get( ft );
                while ( substitutionGroup != null ) {
                    substitutionGroups.add( substitutionGroup );
                    substitutionGroup = ftToSuperFt.get( substitutionGroup );
                }
                ftToSuperFts.put( ft, substitutionGroups );
            }
        }

        // resolve values in feature property declarations
        for ( FeatureType ft : fts ) {
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                if ( pt instanceof FeaturePropertyType ) {
                    QName referencedFtName = ( (FeaturePropertyType) pt ).getFTName();
                    if ( referencedFtName != null ) {
                        FeatureType referencedFt = ftNameToFt.get( referencedFtName );
                        if ( referencedFt == null ) {
                            String msg = Messages.getMessage( "ERROR_SCHEMA_UNKNOWN_FEATURE_TYPE_IN_PROPERTY",
                                                              referencedFtName, pt.getName() );
                            LOG.warn( msg );
                            ( (FeaturePropertyType) pt ).resolve( null );
                        } else {
                            ( (FeaturePropertyType) pt ).resolve( referencedFt );
                        }
                    }
                }
            }
        }
    }

    public ApplicationSchema( FeatureType[] fts, Map<FeatureType, FeatureType> ftToSuperFt, GMLSchemaAnalyzer xsModel ) {
        this( fts, ftToSuperFt );
        this.xsModel = xsModel;
    }

    /**
     * Returns all feature types that are defined in this application schema.
     * 
     * @return all feature types, never <code>null</code>
     */
    public FeatureType[] getFeatureTypes() {
        FeatureType[] fts = new FeatureType[ftNameToFt.values().size()];
        int i = 0;
        for ( FeatureType ft : ftNameToFt.values() ) {
            fts[i++] = ft;
        }
        return fts;
    }

    /**
     * Returns all feature types that are defined in this application schema, limited by the options.
     * 
     * @param namespace
     *            may be <code>null</code> (include all feature types from all namespaces)
     * @param includeCollections
     * @param includeAbstracts
     * 
     * @return all feature types, never <code>null</code>
     */
    public List<FeatureType> getFeatureTypes( String namespace, boolean includeCollections, boolean includeAbstracts ) {
        List<FeatureType> fts = new ArrayList<FeatureType>( ftNameToFt.values().size() );

        for ( FeatureType ft : ftNameToFt.values() ) {
            if ( namespace == null || namespace.equals( ft.getName().getNamespaceURI() ) ) {
                if ( ( includeAbstracts || !ft.isAbstract() )
                     && ( includeCollections || !( ft instanceof FeatureCollectionType ) ) ) {
                    fts.add( ft );
                }
            }
        }
        return fts;
    }

    /**
     * Returns all root feature types that are defined in this application schema.
     * 
     * @return all root feature types, never <code>null</code>
     */
    public FeatureType[] getRootFeatureTypes() {
        // start with all feature types
        Set<FeatureType> fts = new HashSet<FeatureType>( ftNameToFt.values() );
        // remove all that have a super type
        fts.removeAll( ftToSuperFt.keySet() );
        return fts.toArray( new FeatureType[fts.size()] );
    }

    /**
     * Retrieves the feature type with the given name.
     * 
     * @param ftName
     *            feature type name to look up
     * @return the feature type with the given name, or null if no such feature type exists
     */
    public FeatureType getFeatureType( QName ftName ) {
        return ftNameToFt.get( ftName );
    }

    /**
     * Retrieves the direct subtypes for the given feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return the direct subtypes of the given feature type (abstract and non-abstract)
     */
    public FeatureType[] getDirectSubtypes( FeatureType ft ) {
        List<FeatureType> fts = new ArrayList<FeatureType>( ftNameToFt.size() );
        for ( FeatureType ft2 : ftToSuperFt.keySet() ) {
            if ( ftToSuperFt.get( ft2 ) == ft ) {
                fts.add( ft2 );
            }
        }
        return fts.toArray( new FeatureType[fts.size()] );
    }

    /**
     * Retrieves the parent feature type for the specified feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return parent feature type, can be <code>null</code>
     */
    public FeatureType getParentFt( FeatureType ft ) {
        return ftToSuperFt.get( ft );
    }

    /**
     * Retrieves all substitutions (abstract and non-abstract ones) for the given feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return all substitutions for the given feature type
     */
    public FeatureType[] getSubtypes( FeatureType ft ) {
        FeatureType[] directSubtypes = getDirectSubtypes( ft );
        List<FeatureType> result = new ArrayList<FeatureType>( ftNameToFt.size() );
        if ( directSubtypes != null && directSubtypes.length > 0 ) {
            for ( FeatureType son : directSubtypes ) {
                if ( son != null ) {
                    result.add( son );
                    FeatureType[] sons = getSubtypes( son );
                    if ( sons != null && sons.length > 0 ) {
                        Collections.addAll( result, sons );
                    }
                }
            }
        }
        return result.toArray( new FeatureType[result.size()] );
    }

    /**
     * Retrieves all concrete substitutions for the given feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return all concrete substitutions for the given feature type
     */
    public FeatureType[] getConcreteSubtypes( FeatureType ft ) {
        FeatureType[] directSubtypes = getDirectSubtypes( ft );
        List<FeatureType> result = new ArrayList<FeatureType>( ftNameToFt.size() );
        if ( directSubtypes != null && directSubtypes.length > 0 ) {
            for ( FeatureType son : directSubtypes ) {
                if ( son != null ) {
                    if ( !son.isAbstract() ) {
                        result.add( son );
                    }
                    FeatureType[] sons = getSubtypes( son );
                    if ( sons != null && sons.length > 0 ) {
                        for ( FeatureType sonSon : sons ) {
                            if ( !sonSon.isAbstract() ) {
                                result.add( sonSon );
                            }
                        }
                    }
                }
            }
        }
        return result.toArray( new FeatureType[result.size()] );
    }

    /**
     * Returns the underlying XML schema.
     * 
     * @return the underlying XML schema, can be <code>null</code>
     */
    public GMLSchemaAnalyzer getXSModel() {
        return xsModel;
    }

    /**
     * Determines whether a feature type is substitutable for another feature type.
     * <p>
     * This is true, iff <code>substitution</code> is either:
     * <ul>
     * <li>equal to <code>ft</code></li>
     * <li>a direct subtype of <code>ft</code></li>
     * <li>a transititive subtype of <code>ft</code></li>
     * </ul>
     * 
     * @param ft
     *            base feature type, must be part of this schema
     * @param substitution
     *            feature type to be checked, must be part of this schema
     * @return true, if the first feature type is a valid substitution for the second
     */
    public boolean isSubType( FeatureType ft, FeatureType substitution ) {
        if ( substitution == null || ft == null ) {
            LOG.debug( "Testing substitutability against null feature type." );
            return true;
        }
        LOG.debug( "ft: " + ft.getName() + ", substitution: " + substitution.getName() );
        if ( ft == substitution ) {
            return true;
        }
        List<FeatureType> substitutionGroups = ftToSuperFts.get( substitution );
        if ( substitutionGroups != null ) {
            for ( FeatureType substitutionGroup : substitutionGroups ) {
                if ( ft == substitutionGroup ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the {@link PropertyType}s from the specified {@link FeatureType} declaration that are *not* present in
     * the parent {@link FeatureType} or its ancestors.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return list of property declarations, may be empty, but never <code>null</code>
     */
    public List<PropertyType> getNewPropertyDeclarations( FeatureType ft ) {

        List<PropertyType> propDecls = ft.getPropertyDeclarations();
        FeatureType parentFt = getParentFt( ft );
        int firstNewIdx = 0;
        if ( parentFt != null ) {
            for ( PropertyType parentPropDecl : parentFt.getPropertyDeclarations() ) {
                if ( parentPropDecl.getName().equals( propDecls.get( firstNewIdx ).getName() ) ) {
                    firstNewIdx++;
                } else {
                    throw new RuntimeException( "Content model of feature type '" + ft.getName()
                                                + "' is not compatible with parent type '" + parentFt.getName() + "'." );
                }
            }
        }
        return propDecls.subList( firstNewIdx, propDecls.size() );
    }

    public Map<FeatureType, FeatureType> getFtToSuperFt() {
        return ftToSuperFt;
    }

    public Collection<String> getNamespaces() {
        Set<String> namespaces = new TreeSet<String>();
        for ( FeatureType ft : getFeatureTypes() ) {
            namespaces.add( ft.getName().getNamespaceURI() );
        }
        if ( xsModel != null ) {
            XSNamespaceItemList nsItems = xsModel.getNamespaces();
            for ( int i = 0; i < nsItems.getLength(); i++ ) {
                String ns = nsItems.item( i ).getSchemaNamespace();
                if ( ns.equals( GMLNS ) ) {
                    continue;
                } else if ( ns.equals( GML3_2_NS ) ) {
                    continue;
                } else if ( ns.equals( "http://www.w3.org/XML/1998/namespace" ) ) {
                    continue;
                } else if ( ns.equals( "http://www.w3.org/2001/XMLSchema" ) ) {
                    continue;
                }
                namespaces.add( ns );
            }
        }
        return namespaces;
    }

    /**
     * TODO implement this properly
     * 
     * @return
     */
    public Map<String, String> getNamespaceBindings() {
        Map<String, String> prefixToNs = new HashMap<String, String>();
        for ( FeatureType ft : getFeatureTypes() ) {
            prefixToNs.put( ft.getName().getPrefix(), ft.getName().getNamespaceURI() );
        }
        return prefixToNs;
    }
}
