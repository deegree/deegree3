//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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

package org.deegree.protocol.wfs.transaction.xml;

import static org.deegree.commons.tom.ows.Version.parseVersion;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getAttributeValue;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredAttributeValue;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.transaction.Transaction;

/**
 * Reader for XML encoded WFS <code>Transaction</code> requests.
 * <p>
 * Supported versions:
 * <ul>
 * <li>WFS 1.0.0</li>
 * <li>WFS 1.1.0</li>
 * <li>WFS 2.0.0</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TransactionXmlReader extends AbstractWFSRequestXMLAdapter {

    /**
     * Parses a WFS <code>Transaction</code> document.
     * <p>
     * Supported versions:
     * <ul>
     * <li>WFS 1.0.0</li>
     * <li>WFS 1.1.0</li>
     * <li>WFS 2.0.0</li>
     * </ul>
     * </p>
     * 
     * @return parsed {@link Transaction} request, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public Transaction read( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        Version version = null;
        if ( WFS_NS.equals( xmlStream.getNamespaceURI() ) ) {
            String s = getAttributeValue( xmlStream, "version" );
            if ( s == null ) {
                s = "1.1.0";
            }
            version = parseVersion( s );
        } else {
            version = parseVersion( getRequiredAttributeValue( xmlStream, "version" ) );
        }

        Transaction result = null;
        if ( VERSION_100.equals( version ) ) {
            result = new TransactionXmlReader100().read( xmlStream );
        } else if ( VERSION_110.equals( version ) ) {
            result = new TransactionXmlReader110().read( xmlStream );
        } else if ( VERSION_200.equals( version ) ) {
            result = new TransactionXmlReader200().read( xmlStream );
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version,
                                       Version.getVersionsString( VERSION_100, VERSION_110, VERSION_200 ) );
            throw new InvalidParameterValueException( msg );
        }
        return result;
    }
}
