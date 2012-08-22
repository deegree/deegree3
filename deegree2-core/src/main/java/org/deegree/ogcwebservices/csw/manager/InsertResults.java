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
package org.deegree.ogcwebservices.csw.manager;

import java.util.List;

import org.w3c.dom.Node;

/**
 * An InsertResult object may appear zero or more times in the transaction response. It is used to
 * report to the client a brief representation of each new record, including the record identifier,
 * created in the catalogue. The records must be reported in the same order in which a Insert object
 * appear in a transaction request and must map 1-to-1. Optionally, the handle attribute may be used
 * to correlate a particular Insert object in the Transaction request with an InsertResult obejt
 * found in the transaction response.
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
public class InsertResults {

    private List<Node> records = null;

    /**
     * @param records
     *            records inserted into a backend by a CS-W Insert operation
     */
    public InsertResults( List<Node> records ) {
        this.records = records;
    }

    /**
     * returns the records inserted into a backend by a CS-W Insert operation
     *
     * @return the records inserted into a backend by a CS-W Insert operation
     */
    public List<Node> getRecords() {
        return records;
    }

}
