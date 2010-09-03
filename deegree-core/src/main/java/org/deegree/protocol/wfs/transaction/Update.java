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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.wfs.WFSConstants;

/**
 * Represents a WFS <code>Update</code> operation (part of a {@link Transaction} request).
 * 
 * @see Transaction
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Update extends TransactionOperation {

    private final Version version;

    private final QName ftName;

    private final String inputFormat;

    private final String srsName;

    private final XMLStreamReader xmlStream;

    private boolean createdIterator;

    /**
     * Creates a new {@link Update} instance for a stream-based access strategy.
     * 
     * @param handle
     *            identifier for the operation, may be null
     * @param version
     *            protocol version, must not be null
     * @param ftName
     *            name of the targeted feature type, must not be null
     * @param inputFormat
     *            the format of encoded property values, may be null (unspecified)
     * @param srsName
     *            the coordinate references system used for the geometries, may be null (unspecified)
     * @param xmlStream
     *            provides access to the XML encoded replacement properties and the filter, must point at the
     *            <code>START_ELEMENT</code> event of the first "wfs:Property"
     */
    public Update( String handle, Version version, QName ftName, String inputFormat, String srsName,
                   XMLStreamReader xmlStream ) {
        super( handle );
        this.version = version;
        this.ftName = ftName;
        this.inputFormat = inputFormat;
        this.srsName = srsName;
        this.xmlStream = xmlStream;
    }

    /**
     * Always returns {@link TransactionOperation.Type#UPDATE}.
     * 
     * @return {@link TransactionOperation.Type#UPDATE}
     */
    @Override
    public Type getType() {
        return Type.UPDATE;
    }

    /**
     * Returns the name of the targeted feature type.
     * 
     * @return the name of the targeted feature type, never null
     */
    public QName getTypeName() {
        return this.ftName;
    }

    /**
     * Returns the format of the encoded property values.
     * 
     * @return the format of the encoded property values, may be null (unspecified)
     */
    public String getInputFormat() {
        return inputFormat;
    }

    /**
     * Returns the specified coordinate reference system for geometries to be updated.
     * 
     * @return the specified coordinate reference system, can be null (unspecified)
     */
    public String getSRSName() {
        return srsName;
    }

    public Iterator<PropertyReplacement> getReplacementProps() {
        if ( createdIterator ) {
            throw new RuntimeException( "Iteration over the transaction operations can only be done once." );
        }
        createdIterator = true;
        return new Iterator<PropertyReplacement>() {

            @Override
            public boolean hasNext() {
                return xmlStream.isStartElement() && new QName( WFS_NS, "Property" ).equals( xmlStream.getName() );
            }

            @Override
            public PropertyReplacement next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                PropertyReplacement replacement = null;
                if ( version.equals( WFSConstants.VERSION_100 ) ) {
                    try {
                        replacement = TransactionXMLAdapter.parseProperty100( xmlStream );
                    } catch ( XMLStreamException e ) {
                        throw new XMLParsingException( xmlStream, "Error parsing transaction operation: "
                                                                  + e.getMessage() );
                    }
                } else if ( version.equals( WFSConstants.VERSION_110 ) ) {
                    try {
                        replacement = TransactionXMLAdapter.parseProperty110( xmlStream );
                    } catch ( XMLStreamException e ) {
                        throw new XMLParsingException( xmlStream, "Error parsing transaction operation: "
                                                                  + e.getMessage() );
                    }
                } else {
                    throw new UnsupportedOperationException(
                                                             "Only WFS 1.1.0 transaction are implemented at the moment." );
                }
                return replacement;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns the filter that selects the feature instances to be updated.
     * <p>
     * NOTE: Due to streaming acccess strategy, there are some rules for using this method:
     * <ul>
     * <li>#getReplacementProps() must have been called before</li>
     * <li>the client must have iterated over all returned properties</li>
     * <li>the method must be called exactly once</li>
     * </ul>
     * </p>
     * 
     * @return Filter that selects the feature instances to be updated, can be <code>null</code>
     * @throws XMLStreamException
     */
    public Filter getFilter()
                            throws XMLStreamException {
        // optional: 'ogc:Filter'
        Filter filter = null;
        if ( xmlStream.isStartElement() ) {
            xmlStream.require( START_ELEMENT, CommonNamespaces.OGCNS, "Filter" );
            if ( version.equals( WFSConstants.VERSION_100 ) ) {
                filter = Filter100XMLDecoder.parse( xmlStream );
            } else if ( version.equals( WFSConstants.VERSION_110 ) ) {
                filter = Filter110XMLDecoder.parse( xmlStream );
            }
            xmlStream.require( END_ELEMENT, CommonNamespaces.OGCNS, "Filter" );
            // contract: skip to wfs:Update END_ELEMENT
            xmlStream.nextTag();
            // contract: skip to next operation START_ELEMENT
            xmlStream.nextTag();
        }
        return filter;
    }
}
