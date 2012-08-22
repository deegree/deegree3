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
package org.deegree.ogcwebservices.csw.discovery;

import java.util.Date;

import org.deegree.framework.util.TimeTools;

/**
 * Class representation of a &lt;csw:SearchStatus&gt;-element.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:tfr@users.sourceforge.net">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 */
public class SearchStatus {

    private static final String[] STATES = { "complete", "subset", "interim", "processing", "none" };

    private String status;

    private Date timestamp;

    /**
     *
     *
     */
    private SearchStatus() {
        this.timestamp = new Date( System.currentTimeMillis() );
    }

    /**
     *
     * @param status
     */
    SearchStatus( String status ) {
        this();
        for ( int i = 0; i < STATES.length; i++ ) {
            String aState = STATES[i];
            if ( aState.equalsIgnoreCase( status ) ) {
                this.status = status;
            }
        }
    }

    /**
     *
     * @param status
     * @param timestamp
     */
    SearchStatus( String status, Date timestamp ) {
        this( status );
        this.timestamp = timestamp;
    }

    /**
     * Create a new instance from status-String and timestamp-String.
     *
     * TODO: parse timestampString
     *
     * @param status
     * @param timestampString
     */
    SearchStatus( String status, String timestampString ) {
        this( status );
        this.timestamp = TimeTools.createCalendar( timestampString ).getTime();
    }

    /**
     * possible values are:
     * <ul>
     * <li>complete: The request was successfully completed and valid results are available or have
     * been returned.
     * <li>subset: The request was successfully completed and partial valid results are available
     * or have been returned. In this case subsequest queries with new start positions may be used
     * to see more results.
     * <li>interim: The request was successfully completed and partial results are available or
     * have been returned but the results may not be valid. For example, an intermediate server in a
     * distributed search may have failed cause the partial, invalid result set to be generated.
     * <li>processing: The request is still processing. When completed, the response will be sent
     * to the specified response handler.
     * <li>none: No records found.
     * </ul>
     *
     * @return request processing status
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * @return datestamp of processing
     */
    public Date getTimestamp() {
        return this.timestamp;
    }

}
