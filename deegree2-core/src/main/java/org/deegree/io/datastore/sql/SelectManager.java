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

package org.deegree.io.datastore.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.PropertyPathResolver;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * Responsible for managing the mapping of properties to table columns SQL and their position in the SQL result set.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class SelectManager {

    private static final ILogger LOG = LoggerFactory.getLogger( SelectManager.class );

    private MappedFeatureType[] fts;

    private Map<MappedPropertyType, Collection<PropertyPath>>[] allFetchProps;

    private List<List<SimpleContent>>[] allFetchContents;

    private Map<SimpleContent, Integer>[] allResultPosMaps;

    private int fetchContentsCount;

    // requested properties, must contain exactly one list for each targeted feature type
    private List<PropertyPath>[] selectedProps;

    private String[] ftAliases;

    // TODO remove this
    private FeatureFetcher fetcher;

    List<PropertyPath> augmentedGeometryProps = new ArrayList<PropertyPath>();

    SelectManager( Query query, MappedFeatureType[] rootFts, FeatureFetcher fetcher ) throws DatastoreException {

        this.fts = rootFts;
        this.ftAliases = query.getAliases();
        this.selectedProps = PropertyPathResolver.normalizePropertyPaths( this.fts, this.ftAliases,
                                                                          query.getPropertyNames() );

        // hack that ensures that all geometry properties are fetched for correct boundedBy information
        if ( fts.length == 1 ) {
            PropertyPathStep ftStep = PropertyPathFactory.createPropertyPathStep( fts[0].getName() );
            PropertyPath fullFeature = PropertyPathFactory.createPropertyPath( fts[0].getName() );
            for ( GeometryPropertyType geoProp : fts[0].getGeometryProperties() ) {
                PropertyPathStep[] steps = new PropertyPathStep[] {
                                                                   ftStep,
                                                                   PropertyPathFactory.createPropertyPathStep( geoProp.getName() ) };
                PropertyPath geoPropPath = PropertyPathFactory.createPropertyPath( Arrays.asList( steps ) );
                boolean found = geoProp.getMinOccurs() > 0;
                if ( !found ) {
                    for ( PropertyPath selectedPath : selectedProps[0] ) {
                        if ( selectedPath.equals( fullFeature ) || selectedPath.equals( geoPropPath ) ) {
                            found = true;
                            break;
                        }
                    }
                }
                if ( !found ) {
                    LOG.logDebug( "Augmenting geometry property '" + geoPropPath
                                  + "' to ensure that all geometry properties are selected (boundedBy-hack)." );
                    augmentedGeometryProps.add( geoPropPath );
                    selectedProps[0].add( geoPropPath );
                }
            }
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "Selected properties (normalized): " );
            for ( int i = 0; i < fts.length; i++ ) {
                List<PropertyPath> props = this.selectedProps[i];
                LOG.logDebug( "Selected properties (normalized) for feature type '" + fts[i].getName() + "'." );
                for ( PropertyPath path : props ) {
                    LOG.logDebug( " - " + path );
                }
            }
        }

        this.allFetchProps = new Map[this.fts.length];
        this.allFetchContents = new List[this.fts.length];
        this.allResultPosMaps = new Map[this.fts.length];
        this.fetcher = fetcher;

        determineInitialFetchProperties();
        determineFetchContents();
        buildResultPosMaps();
    }

    private void determineInitialFetchProperties()
                            throws PropertyPathResolvingException {

        LOG.logDebug( "Determining fetch properties for all requested feature types." );

        for ( int i = 0; i < this.fts.length; i++ ) {
            MappedFeatureType rootFt = this.fts[i];
            LOG.logDebug( "Feature type: " + rootFt.getName() + " (alias: "
                          + ( this.ftAliases != null ? this.ftAliases[i] : "-" ) + ")" );
            PropertyPath[] requestedProps = this.selectedProps[i].toArray( new PropertyPath[this.selectedProps[i].size()] );
            for ( PropertyPath path : requestedProps ) {
                LOG.logDebug( "Requested property: " + path );
            }
            Map<MappedPropertyType, Collection<PropertyPath>> ftRequestedProps = PropertyPathResolver.determineFetchProperties(
                                                                                                                                rootFt,
                                                                                                                                this.ftAliases != null ? this.ftAliases[i]
                                                                                                                                                      : null,
                                                                                                                                requestedProps );
            LOG.logDebug( "All properties needed for feature type: " + rootFt.getName() );
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                for ( MappedPropertyType pt : ftRequestedProps.keySet() ) {
                    LOG.logDebug( "-" + pt.getName() );
                }
            }
            this.allFetchProps[i] = ftRequestedProps;
        }
    }

    private void determineFetchContents()
                            throws DatastoreException {
        LOG.logDebug( "Determining initial fetch contents for all requested feature types..." );

        for ( int i = 0; i < this.fts.length; i++ ) {
            MappedFeatureType rootFt = this.fts[i];
            MappedPropertyType[] requestedProps = new MappedPropertyType[this.allFetchProps[i].size()];
            requestedProps = this.allFetchProps[i].keySet().toArray( requestedProps );
            List<List<SimpleContent>> ftFetchContents = null;
            if ( requestedProps.length > 0 ) {
                ftFetchContents = fetcher.determineFetchContents( rootFt, requestedProps );
            } else {
                ftFetchContents = new ArrayList<List<SimpleContent>>();
            }
            this.allFetchContents[i] = ftFetchContents;

            LOG.logDebug( "Will fetch the following columns initially (for feature type " + rootFt.getName() + "):" );
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                for ( List<SimpleContent> list : ftFetchContents ) {
                    SimpleContent representer = list.get( 0 );
                    LOG.logDebug( "-" + representer );
                }
            }
            this.fetchContentsCount += ftFetchContents.size();
        }
    }

    private void buildResultPosMaps() {

        int currentRSPos = 0;
        for ( int i = 0; i < this.allFetchContents.length; i++ ) {
            List<List<SimpleContent>> ftFetchContents = this.allFetchContents[i];
            Map<SimpleContent, Integer> ftResultPosMap = new HashMap<SimpleContent, Integer>();
            for ( int j = 0; j < ftFetchContents.size(); j++ ) {
                for ( SimpleContent content : ftFetchContents.get( j ) ) {
                    ftResultPosMap.put( content, j + currentRSPos );
                }
            }
            this.allResultPosMaps[i] = ftResultPosMap;
            currentRSPos += ftFetchContents.size();
        }
    }

    List<List<SimpleContent>>[] getAllFetchContents() {
        return this.allFetchContents;
    }

    Map<SimpleContent, Integer>[] getResultPosMaps() {
        return this.allResultPosMaps;
    }

    Map<MappedPropertyType, Collection<PropertyPath>>[] getAllFetchProps() {
        return this.allFetchProps;
    }

    int getFetchContentCount() {
        return this.fetchContentsCount;
    }

    private int getActualFeatureTupleLength() {
        int i = 0;
        for ( List<List<SimpleContent>> ftFetchContents : this.allFetchContents ) {
            if ( ftFetchContents.size() > 0 ) {
                i++;
            }
        }
        return i;
    }

    int[] getIncludedFtIdx() {
        int[] includedFtIdx = new int[getActualFeatureTupleLength()];
        int i = 0;
        int idx = 0;
        for ( List<List<SimpleContent>> ftFetchContents : this.allFetchContents ) {
            if ( ftFetchContents.size() > 0 ) {
                includedFtIdx[idx] = i;
                idx++;
            }
            i++;
        }
        return includedFtIdx;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        int rsOffset = 0;

        for ( int i = 0; i < this.fts.length; i++ ) {
            sb.append( "Properties needed for feature type '" + this.fts[i].getName() + "':\n" );
            Map<MappedPropertyType, Collection<PropertyPath>> ftFetchProps = this.allFetchProps[i];
            for ( MappedPropertyType pt : ftFetchProps.keySet() ) {
                sb.append( " - " );
                sb.append( pt.getName().getLocalName() );
                sb.append( ", requesting PropertyNames: " );
                Collection<PropertyPath> requestingPaths = ftFetchProps.get( pt );
                for ( PropertyPath path : requestingPaths ) {
                    sb.append( path );
                    sb.append( " " );
                }
                sb.append( '\n' );
            }

            sb.append( "Fields to be fetched for feature type '" + this.fts[i].getName() + "':\n" );
            List<List<SimpleContent>> ftFetchContents = this.allFetchContents[i];
            for ( int j = 0; j < ftFetchContents.size(); j++ ) {
                List<SimpleContent> sameField = ftFetchContents.get( j );
                sb.append( " - ResultSet[" );
                sb.append( j + rsOffset );
                sb.append( "], SimpleContent: " );
                sb.append( '\'' );
                sb.append( sameField.get( 0 ) );
                sb.append( '\'' );
                sb.append( '\n' );
            }
            rsOffset += ftFetchContents.size();
        }
        return sb.toString();
    }
}
