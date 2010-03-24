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

import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.feature.Feature;
import org.slf4j.Logger;

/**
 * <code>GMLId</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLId {

    private static final Logger LOG = getLogger( GMLId.class );

    /**
     * @param sb
     * @param o
     * @param parent
     */
    public void eval( StringBuilder sb, Object o, Feature parent ) {
        if ( o instanceof Feature ) {
            String id = ( (Feature) o ).getId();
            if ( id != null && !id.isEmpty() ) {
                sb.append( id );
            }
        } else if ( parent != null ) {
            String id = parent.getId();
            if ( id != null && !id.isEmpty() ) {
                sb.append( id );
            }
        } else {
            LOG.warn( "Trying to get GML id from property without parent information." );
        }
    }

}
