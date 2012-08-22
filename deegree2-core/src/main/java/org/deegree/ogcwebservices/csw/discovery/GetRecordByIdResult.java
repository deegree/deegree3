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

import java.util.ArrayList;
import java.util.List;

import org.deegree.ogcwebservices.DefaultOGCWebServiceResponse;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class GetRecordByIdResult extends DefaultOGCWebServiceResponse {

    private List<Node> records = new ArrayList<Node>();

    /**
     * 
     * @param request
     * @param record
     *            result node
     */
    public GetRecordByIdResult( GetRecordById request, Element record ) {
        super( request );
        this.records.add( record );
    }

    /**
     * @param request
     * @param records
     */
    public GetRecordByIdResult( GetRecordById request, List<Node> records ) {
        super( request );
        this.records = records;
    }

    /**
     * @return the first record node that is the result content of a GetRecordById request
     */
    public Element getRecord() {
        return records != null && records.size() > 0 ? (Element)records.get( 0 ) : null;
    }

    /**
     * @return the record nodes that is the result content of a GetRecordById request
     */
    public List<Node> getRecords() {
        return records;
    }
}
