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

import static javax.xml.XMLConstants.NULL_NS_URI;

import java.util.Map;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingContextManager {

    private static Logger LOG = LoggerFactory.getLogger( MappingContextManager.class );

    private int maxLength = 64;

    private int id = 0;

    private final Map<String, String> nsToPrefix;

    public MappingContextManager( Map<String, String> nsToPrefix ) {
        this.nsToPrefix = nsToPrefix;
    }

    public MappingContext newContext( QName name ) {
        return new MappingContext( getSQLIdentifier( "", toString( name ) ) );
    }

    public MappingContext mapOneToOneElement( MappingContext mc, QName childElement ) {
        String newColumn = getSQLIdentifier( mc.getColumn(), toString( childElement ) );
        return new MappingContext( mc.getTable(), newColumn );
    }

    public MappingContext mapOneToOneAttribute( MappingContext mc, QName attribute ) {
        String newColumn = getSQLIdentifier( mc.getColumn(), "attr_" + toString( attribute ) );
        return new MappingContext( mc.getTable(), newColumn );
    }

    public MappingContext mapOneToManyElements( MappingContext mc, QName childElement ) {
        String newTable = getSQLIdentifier( mc.getTable(), toString( childElement ) );
        return new MappingContext( newTable, mc.getColumn() );
    }

    private String getSQLIdentifier( String prefix, String name ) {
        String id = name;
        if ( !prefix.isEmpty() ) {
            id = prefix + "_" + name;
        }
        if ( id.length() > maxLength ) {
            String substring = id.substring( 0, maxLength - 5 );
            id = substring + "_" + ( this.id++ );
        }
        return id;
    }

    private String toString( QName qName ) {
        String name = toSQL( qName.getLocalPart() );
        if ( qName.getNamespaceURI() != null && !qName.getNamespaceURI().equals( NULL_NS_URI ) ) {
            String nsPrefix = nsToPrefix.get( qName.getNamespaceURI() );
            if ( nsPrefix == null ) {
                LOG.warn( "Prefix null!?" );
                nsPrefix = "app";
            }
            name = toSQL( nsPrefix.toLowerCase() ) + "_" + toSQL( qName.getLocalPart() );
        }
        return name;
    }

    private String toSQL( String identifier ) {
        String sql = identifier.toLowerCase();
        sql = sql.replace( "-", "_" );
        return sql;
    }
}