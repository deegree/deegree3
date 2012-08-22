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

import org.deegree.ogcwebservices.DefaultOGCWebServiceResponse;

/**
 * Class representation of a <code>GetRecordsResponse/code>.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class GetRecordsResult extends DefaultOGCWebServiceResponse {

    private String version = "2.0.0";

    private SearchStatus searchStatus;

    private SearchResults searchResults;

    /**
     *
     * @param request
     * @param version
     * @param status
     * @param results
     */
    GetRecordsResult( GetRecords request, String version, SearchStatus status, SearchResults results ) {
        super( request );
        this.searchStatus = status;
        this.searchResults = results;
        this.version = version;
    }

    /**
     * The SearchStatus must be present and indicates the status of the
     * response. The status attribute is used to indicate the completion status
     * of the GetRecords operation.
     * @return the SearchResult as a status of the response.
     */
    public SearchStatus getSearchStatus() {
        return this.searchStatus;
    }

    /**
     * @return the actual SeachResults
     */
    public SearchResults getSearchResults() {
        return this.searchResults;
    }

    /**
     * @return the current version String of the GetRecordResult.
     */
    public String getVersion() {
        return version;
    }

}
