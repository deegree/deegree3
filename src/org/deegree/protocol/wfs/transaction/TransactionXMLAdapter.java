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

package org.deegree.protocol.wfs.transaction;

import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;

/**
 * Adapter between XML encoded <code>Transaction</code> requests and {@link Transaction} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TransactionXMLAdapter extends AbstractWFSRequestXMLAdapter {

    /**
     * Parses a WFS <code>Transaction</code> document into a {@link Transaction} request.
     * <p>
     * Supported versions:
     * <ul>
     * </ul>
     * 
     * @return parsed {@link Transaction} request
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws MissingParameterException
     *             if the request version is unsupported
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public Transaction parse() {
        throw new UnsupportedOperationException();
    }
}
