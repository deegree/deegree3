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

package org.deegree.protocol.wfs.transaction.xml;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.transaction.xml.TransactionXMLAdapter.parseOperation100;
import static org.deegree.protocol.wfs.transaction.xml.TransactionXMLAdapter.parseOperation110;
import static org.deegree.protocol.wfs.transaction.xml.TransactionXMLAdapter.parseOperation200;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.protocol.wfs.transaction.TransactionAction;

/**
 * Parser for the actions contained in a WFS <code>Transaction</code> document.
 * 
 * @see TransactionXMLAdapter
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class LazyOperationsParser implements Iterable<TransactionAction> {

    private final Version version;

    private final XMLStreamReader xmlStream;

    private boolean createdIterator;

    /**
     * Creates a new {@link LazyOperationsParser} that provides sequential access to the given XML-encoded
     * {@link TransactionAction}s.
     * 
     * @param wfsVersion
     * @param xmlStream
     */
    LazyOperationsParser( Version wfsVersion, XMLStreamReader xmlStream ) {
        this.version = wfsVersion;
        this.xmlStream = xmlStream;
    }

    @Override
    public synchronized Iterator<TransactionAction> iterator() {
        if ( createdIterator ) {
            throw new RuntimeException( "Iteration over the transaction operations can only be done once." );
        }
        createdIterator = true;
        return new Iterator<TransactionAction>() {

            @Override
            public boolean hasNext() {
                return xmlStream.isStartElement();
            }

            @Override
            public TransactionAction next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                TransactionAction operation = null;
                try {
                    if ( version.equals( VERSION_100 ) ) {
                        operation = parseOperation100( xmlStream );
                    } else if ( version.equals( VERSION_110 ) ) {
                        operation = parseOperation110( xmlStream );
                    } else if ( version.equals( VERSION_200 ) ) {
                        operation = parseOperation200( xmlStream );
                    } else {
                        String msg = "Unsupported WFS version: " + version
                                     + ". Supported WFS versions are 1.0.0, 1.1.0 and 2.0.0.";
                        throw new UnsupportedOperationException( msg );
                    }
                } catch ( XMLStreamException e ) {
                    throw new XMLParsingException( xmlStream, "Error parsing transaction operation: " + e.getMessage() );
                }
                return operation;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
