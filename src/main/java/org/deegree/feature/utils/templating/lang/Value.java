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
 * <code>Value</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Value {

    private static final Logger LOG = getLogger( Value.class );

    /**
     * @param sb
     * @param o
     */
    public void eval( StringBuilder sb, Object o ) {
        if ( o instanceof Property ) {
            try {
                sb.append( ( (Property) o ).getValue() );
            } catch ( UnsupportedOperationException e ) {
                LOG.error( "The error '{}' occurred while converting a property to a string, "
                           + "probably the WKT writer cannot convert a geometry.", e.getLocalizedMessage() );
                LOG.debug( "Stack trace:", e );
            }
        } else {
            LOG.warn( "Trying to get value while current object is a feature." );
        }
    }

    @Override
    public String toString() {
        return generateToString( this );
    }

}
