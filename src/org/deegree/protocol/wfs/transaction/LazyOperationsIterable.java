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

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.protocol.wfs.WFSConstants;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class LazyOperationsIterable implements Iterable<TransactionOperation> {

    private Version version;

    private XMLStreamReader xmlStream;

    private boolean createdIterator;

    /**
     * Creates a new {@link LazyOperationsIterable} that provides sequential access to the given XML-encoded
     * {@link TransactionOperation}s.
     * 
     * @param version
     * @param xmlStream
     */
    LazyOperationsIterable( Version version, XMLStreamReader xmlStream ) {
        this.version = version;
        this.xmlStream = xmlStream;
    }

    @Override
    public synchronized Iterator<TransactionOperation> iterator() {
        if ( createdIterator ) {
            throw new RuntimeException( "Iteration over the transaction operations can only be done once." );
        }
        createdIterator = true;
        return new Iterator<TransactionOperation>() {

            @Override
            public boolean hasNext() {
                return xmlStream.isStartElement();
            }

            @Override
            public TransactionOperation next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                TransactionOperation operation = null;
                if ( version == WFSConstants.VERSION_110 ) {
                    try {
                        operation = TransactionXMLAdapter.parseOperation110( xmlStream );
                    }  catch ( XMLStreamException e ) {
                       throw new XMLParsingException( xmlStream, "Error parsing transaction operation: " + e.getMessage() );
                    }
                } else {
                    throw new UnsupportedOperationException ("Only WFS 1.1.0 transaction are implemented at the moment.");
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
