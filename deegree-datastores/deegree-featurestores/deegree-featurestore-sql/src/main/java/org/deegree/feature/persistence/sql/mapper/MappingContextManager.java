//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql.mapper;

import java.util.Map;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of {@link MappingContext}s generated during a pass of the {@link AppSchemaMapper}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class MappingContextManager {

    private static Logger LOG = LoggerFactory.getLogger( MappingContextManager.class );

    private int maxLength;

    private int id = 0;

    private int count = 0;

    private final Map<String, String> nsToPrefix;

    private final boolean usePrefix;

    MappingContextManager( Map<String, String> nsToPrefix, int maxLength, boolean usePrefix ) {
        this.nsToPrefix = nsToPrefix;
        this.maxLength = maxLength == -1 ? 64 : maxLength;
        this.usePrefix = usePrefix;
    }

    MappingContext newContext( QName name, String idColumn ) {
        count++;
        return new MappingContext( getSQLIdentifier( "", toString( name ) ), idColumn );
    }

    MappingContext mapOneToOneElement( MappingContext mc, QName childElement ) {
        count++;
        String newColumn = getSQLIdentifier( mc.getColumn(), toString( childElement ) );
        return new MappingContext( mc.getTable(), mc.getIdColumn(), newColumn );
    }

    MappingContext mapOneToOneAttribute( MappingContext mc, QName attribute ) {
        count++;
        String newColumn = getSQLIdentifier( mc.getColumn(), "attr_" + toString( attribute ) );
        return new MappingContext( mc.getTable(), mc.getIdColumn(), newColumn );
    }

    MappingContext mapOneToManyElements( MappingContext mc, QName childElement ) {
        count++;
        String prefix = mc.getTable();
        if ( mc.getColumn() != null && !mc.getColumn().isEmpty() ) {
            prefix += "_" + mc.getColumn();
        }
        String newTable = getSQLIdentifier( prefix, toString( childElement ) );
        return new MappingContext( newTable, "id", mc.getColumn() );
    }

    private String getSQLIdentifier( String prefix, String name ) {
        String id = name;
        if ( !prefix.isEmpty() ) {
            id = prefix + "_" + name;
        }
        if ( id.length() >= maxLength ) {
            String substring = id.substring( 0, maxLength - 6 );
            id = substring + "_" + ( this.id++ );
        }
        return id;
    }

    private String toString( QName qName ) {
        String name = toSQL( qName.getLocalPart() );
        if ( qName.getNamespaceURI() != null && !qName.getNamespaceURI().equals( "" ) ) {
            String nsPrefix = nsToPrefix.get( qName.getNamespaceURI() );
            if ( nsPrefix == null ) {
                LOG.warn( "No prefix for namespace {}!?", qName.getNamespaceURI() );
                nsPrefix = "app";
            }
            if ( usePrefix ) {
                name = toSQL( nsPrefix.toLowerCase() ) + "_" + toSQL( qName.getLocalPart() );
            } else {
                name = toSQL( qName.getLocalPart() );
            }
        }
        return name;
    }

    private String toSQL( String identifier ) {
        String sql = identifier.toLowerCase();
        sql = sql.replace( "-", "_" );
        return sql;
    }

    public int getContextCount() {
        return count;
    }
}