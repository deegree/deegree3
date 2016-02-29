//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence.sql.id;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.types.FeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for analyzing if a given feature or geometry id can be attributed to a certain feature type.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class IdAnalyzer {

    private static Logger LOG = LoggerFactory.getLogger( IdAnalyzer.class );

    private final Map<String, FeatureType> prefixToFt = new HashMap<String, FeatureType>();

    private final MappedAppSchema schema;

    /**
     * Creates a new {@link IdAnalyzer} instance for the given {@link MappedAppSchema}.
     * 
     * @param schema
     *            application schema with mapping information, must not be <code>null</code>
     */
    public IdAnalyzer( MappedAppSchema schema ) {
        this.schema = schema;
        for ( FeatureType ft : schema.getFeatureTypes() ) {
            if ( !ft.isAbstract() ) {
                FeatureTypeMapping ftMapping = schema.getFtMapping( ft.getName() );
                if ( ftMapping != null ) {
                    FIDMapping fidMapping = ftMapping.getFidMapping();
                    if ( fidMapping != null ) {
                        LOG.debug( fidMapping.getPrefix() + " -> " + ft.getName() );
                        prefixToFt.put( fidMapping.getPrefix(), ft );
                    }
                }
            }
        }
    }

    public IdAnalysis analyze( String featureOrGeomId ) {

        FeatureType ft = null;
        // TODO implement this more efficiently
        for ( String prefix : prefixToFt.keySet() ) {
            if ( featureOrGeomId.startsWith( prefix ) ) {
                ft = prefixToFt.get( prefix );
                break;
            }
        }
        if ( ft == null ) {
            StringBuilder sb = new StringBuilder( "Unable to determine feature type for id '" );
            sb.append( featureOrGeomId );
            sb.append( "'. Given id does not start with a configured identifier prefix. Known prefixes are: " );
            boolean first = true;
            for ( String prefix : prefixToFt.keySet() ) {
                if ( !first ) {
                    sb.append( ", " );
                }
                sb.append( "'" );
                sb.append( prefix );
                sb.append( "'" );
                first = false;
            }
            sb.append( "." );
            throw new IllegalArgumentException( sb.toString() );
        }

        FIDMapping fidMapping = schema.getFtMapping( ft.getName() ).getFidMapping();
        String idRemainder = featureOrGeomId.substring( fidMapping.getPrefix().length() );
        return new IdAnalysis( ft, idRemainder, fidMapping );
    }

    public IdAnalysis analyze( final String fid, final QName ftName ) {
        final FIDMapping fidMapping = schema.getFtMapping( ftName ).getFidMapping();
        if ( !fid.startsWith( fidMapping.getPrefix() ) ) {
            StringBuilder sb = new StringBuilder( "Given id '" );
            sb.append( fid );
            sb.append( "' does not match configured identifier prefix for feature type " );
            sb.append( ftName );
            sb.append( "." );
            throw new IllegalArgumentException( sb.toString() );
        }
        final String idRemainder = fid.substring( fidMapping.getPrefix().length() );
        return new IdAnalysis( schema.getFeatureType( ftName ), idRemainder, fidMapping );
    }

}
