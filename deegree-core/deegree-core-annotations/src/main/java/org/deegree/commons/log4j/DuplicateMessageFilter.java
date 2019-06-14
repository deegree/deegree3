//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.commons.log4j;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 * @deprecated This class is deprecated as of version 3.4 of deegree.
 */
@Deprecated
public class DuplicateMessageFilter extends AbstractFilter {

    private static final Logger LOG = getLogger( DuplicateMessageFilter.class );

    private String last;

    private int count;

    @Override
    public Result filter( LogEvent event ) {
        if ( last == null ) {
            last = event.getMessage().getFormattedMessage();
            count = 0;
            return Result.ACCEPT;
        }

        if ( last.equals( event.getMessage().getFormattedMessage() ) ) {
            ++count;
            // would be cool to log (... repeated 12452 times)
            if ( count % 100 == 0 ) {
                LOG.warn( "Last message repeated 100 times." );
            }
            return Result.DENY;
        }

        last = event.getMessage().getFormattedMessage();
        count = 1;

        return Result.ACCEPT;
    }

}
