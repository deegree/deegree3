//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.wfs.getfeature;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;

/**
 * A feature type name with an optional alias, as it may be used in WFS 1.1.0 or 2.0.0 queries.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class TypeName {

    private final QName ftName;

    private final String alias;

    /**
     * Creates a new {@link TypeName} with optional alias.
     * 
     * @param ftName
     *            name of the feature, must not be null
     * @param alias
     *            alias for the feature type, may be null
     */
    public TypeName( QName ftName, String alias ) {
        if ( ftName == null ) {
            throw new InvalidParameterValueException( "Type name cannot be null", "typeName" );
        }
        this.ftName = ftName;
        this.alias = alias;
    }

    /**
     * Extracts an array of {@link TypeNames} from the TypeNameList string, whose pattern is ((\w:)?\w(=\w)?){1,}.
     * Example: typeName="ns1:Inwatera_1m=A, ns2:CoastL_1M=B" where A is an alias for ns1:Inwatera_1m and B is an alias
     * for ns2:CoastL_1M. (taken from http://schemas.opengis.net/wfs/1.1.0/wfs.xsd )
     * 
     * @param context
     *            the query element in which the TypeNameList attribute is defined, used to resolve the namespaceURIs
     *            for the prefixes
     * @param typeNameStr
     *            the string that will be parsed
     * @return an array of {@link TypeName}
     */
    public static TypeName[] valuesOf( OMElement context, String typeNameStr ) {
        List<TypeName> resultList = new ArrayList<TypeName>();

        String[] typeNameComma = typeNameStr.split( "," );
        for ( int i = 0; i < typeNameComma.length; i++ ) {
            String[] typeNameEqual = typeNameComma[i].split( "=" );
            if ( typeNameEqual.length == 2 ) {
                resultList.add( new TypeName( resolveQName( context, typeNameEqual[0] ), typeNameEqual[1] ) );
            } else if ( typeNameEqual.length == 1 ) { // no alias
                resultList.add( new TypeName( resolveQName( context, typeNameEqual[0] ), null ) );
            } else {
                // TODO find a suitable exception
                System.err.println( "More than one equal sign(=) in the declaration of TypeNameList" );
            }
        }

        TypeName[] resultArray = new TypeName[resultList.size()];
        return resultList.toArray( resultArray );
    }

    private static QName resolveQName( OMElement context, String name ) {
        QName qName = null;
        int colonIdx = name.indexOf( ":" ); 
        if ( colonIdx != -1 ) {
            qName = context.resolveQName( name );
            if ( qName == null ) {
                // AXIOM appears to return null for context.resolveQName( name ) for unbound prefices!?
                String prefix = name.substring( 0,  colonIdx);
                String localPart = name.substring( colonIdx + 1);
                qName = new QName (XMLConstants.NULL_NS_URI, localPart, prefix);
            }
        } else {
            qName = new QName( name );
        }
        return qName;
    }

    /**
     * Returns the feature type name.
     * 
     * @return the feature type name, never null
     */
    public QName getFeatureTypeName() {
        return ftName;
    }

    /**
     * Returns the alias for the feature type.
     * 
     * @return the alias for the feature type, or null if it has none
     */
    public String getAlias() {
        return alias;
    }
}
