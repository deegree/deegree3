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
package org.deegree.feature.utils.templating.lang;

import static org.deegree.commons.utils.JavaUtils.generateToString;
import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.feature.property.Property;
import org.slf4j.Logger;

/**
 * <code>Link</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Link {

    private static final Logger LOG = getLogger( Link.class );

    private String prefix;

    private String text;

    /**
     * @param prefix
     */
    public Link( String prefix ) {
        this.prefix = prefix;
    }

    /**
     * @param prefix
     * @param text
     */
    public Link( String prefix, String text ) {
        this.prefix = prefix;
        if ( text != null ) {
            // TODO price question: what's the Java Way to sgml-quote?
            text = text.replace( "&", "&amp;" );
        }
        this.text = text;
    }

    /**
     * @param sb
     * @param o
     */
    public void eval( StringBuilder sb, Object o ) {
        if ( !( o instanceof Property ) ) {
            LOG.warn( "Trying to get value as link while current object is a feature." );
            return;
        }
        String val = ( (Property) o ).getValue().toString();
        if ( val == null || val.isEmpty() ) {
            return;
        }
        // TODO: what is wanted is a real check for validity. org.apache.xerces.util.URI.isWellFormedAddress has been
        // tried and seems not to work
        if ( !val.startsWith( "http://" ) && !val.startsWith( "https://" ) && !val.startsWith( "ftp://" ) ) {
            val = prefix == null ? val : ( prefix + val );
        }
        // TODO price question: what's the Java Way to sgml-quote?
        val = val.replace( "&", "&amp;" );
        sb.append( "<a target='_blank' href='" ).append( val ).append( "'>" );
        sb.append( text == null ? val : text );
        sb.append( "</a>" );
    }

    @Override
    public String toString() {
        return generateToString( this );
    }

}
