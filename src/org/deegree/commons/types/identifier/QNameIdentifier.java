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

package org.deegree.commons.types.identifier;

import javax.xml.namespace.QName;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 *
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public class QNameIdentifier implements Identifier {

    private QName qname;

    private String s1;

    private String s2;

    /**
     *
     * @param qname
     */
    public QNameIdentifier( QName qname ) {
        this.qname = qname;
    }

    /**
     *
     * @param localname
     */
    public QNameIdentifier( String localname ) {
        this.qname = new QName( localname );
    }

    /**
     *
     * @param namespace
     * @param localname
     */
    public QNameIdentifier( String namespace, String localname ) {
        this.qname = new QName( namespace, localname );
    }

    /**
     *
     * @param namespace
     * @param localname
     * @param prefix
     */
    public QNameIdentifier( String namespace, String localname, String prefix ) {
        this.qname = new QName( namespace, localname, prefix );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.model.identifier.Identifier#getAsFormattedString()
     */
    public String getAsFormattedString() {
        if ( s1 == null ) {
            if ( qname.getNamespaceURI() != null ) {
                s1 = '{' + qname.getNamespaceURI() + "}:" + qname.getLocalPart();
            } else {
                s1 = qname.getLocalPart();
            }
        }
        return s1;
    }

    /**
     *
     * @return identifier formatted as prefix:localname
     */
    public String getPrefixedName() {
        if ( s2 == null ) {
            if ( qname.getPrefix() != null ) {
                s2 = qname.getPrefix() + ':' + qname.getLocalPart();
            } else {
                s2 = qname.getLocalPart();
            }
        }
        return s2;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other == null || !( other instanceof QNameIdentifier ) ) {
            return false;
        }
        return qname.equals( ( (QNameIdentifier) other ).qname );
    }

}
