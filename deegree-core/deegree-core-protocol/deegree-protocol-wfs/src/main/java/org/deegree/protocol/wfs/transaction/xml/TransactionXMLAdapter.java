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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getAttributeValue;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getElementTextAsQName;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredAttributeValue;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredAttributeValueAsBoolean;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredAttributeValueAsQName;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.requireNextTag;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.filter.xml.Filter200XMLDecoder;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.transaction.ReleaseAction;
import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.TransactionAction;
import org.deegree.protocol.wfs.transaction.action.Delete;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.protocol.wfs.transaction.action.Insert;
import org.deegree.protocol.wfs.transaction.action.Native;
import org.deegree.protocol.wfs.transaction.action.PropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.Update;

/**
 * Adapter between XML encoded <code>Transaction</code> requests and {@link Transaction} objects.
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
public class TransactionXMLAdapter extends AbstractWFSRequestXMLAdapter {

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
    public static Transaction parse( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        Version version = null;
        if ( WFS_NS.equals( xmlStream.getNamespaceURI() ) ) {
            String s = getAttributeValue( xmlStream, "version" );
            if ( s == null ) {
                s = "1.1.0";
            }
            version = Version.parseVersion( s );
        } else {
            version = Version.parseVersion( getRequiredAttributeValue( xmlStream, "version" ) );
        }

        Transaction result = null;
        if ( VERSION_100.equals( version ) ) {
            result = parse100( xmlStream );
        } else if ( VERSION_110.equals( version ) ) {
            result = parse110( xmlStream );
        } else if ( VERSION_200.equals( version ) ) {
            result = parse200( xmlStream );
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version,
                                       Version.getVersionsString( VERSION_100, VERSION_110, VERSION_200 ) );
            throw new InvalidParameterValueException( msg );
        }
        return result;
    }

    /**
     * Parses a WFS 1.0.0 <code>Transaction</code> document.
     * 
     * @return parsed {@link Transaction} request, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public static Transaction parse100( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        xmlStream.require( START_ELEMENT, WFS_NS, "Transaction" );

        // optional: '@handle'
        String handle = getAttributeValue( xmlStream, "handle" );

        // optional: '@releaseAction'
        String releaseActionString = getAttributeValue( xmlStream, "releaseAction" );
        ReleaseAction releaseAction = parseReleaseAction( releaseActionString );

        // optional: 'wfs:LockId'
        String lockId = null;
        requireNextTag( xmlStream, START_ELEMENT );
        if ( xmlStream.getName().equals( new QName( WFS_NS, "LockId" ) ) ) {
            lockId = xmlStream.getElementText().trim();
            requireNextTag( xmlStream, START_ELEMENT );
        }

        LazyOperationsParser iterable = new LazyOperationsParser( VERSION_100, xmlStream );
        return new Transaction( VERSION_100, handle, lockId, releaseAction, iterable );
    }

    /**
     * Parses a WFS 1.1.0 <code>Transaction</code> document.
     * 
     * @return parsed {@link Transaction} request, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public static Transaction parse110( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        xmlStream.require( START_ELEMENT, WFS_NS, "Transaction" );

        // optional: '@handle'
        String handle = getAttributeValue( xmlStream, "handle" );

        // optional: '@releaseAction'
        String releaseActionString = getAttributeValue( xmlStream, "releaseAction" );
        ReleaseAction releaseAction = parseReleaseAction( releaseActionString );

        // optional: 'wfs:LockId'
        String lockId = null;
        requireNextTag( xmlStream, START_ELEMENT );
        if ( xmlStream.getName().equals( new QName( WFS_NS, "LockId" ) ) ) {
            lockId = xmlStream.getElementText().trim();
            requireNextTag( xmlStream, START_ELEMENT );
        }

        LazyOperationsParser iterable = new LazyOperationsParser( VERSION_110, xmlStream );
        return new Transaction( VERSION_110, handle, lockId, releaseAction, iterable );
    }

    /**
     * Parses a WFS 2.0.0 <code>Transaction</code> document.
     * 
     * @return parsed {@link Transaction} request, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public static Transaction parse200( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        xmlStream.require( START_ELEMENT, WFS_200_NS, "Transaction" );

        // <xsd:attribute name="handle" type="xsd:string"/>
        String handle = getAttributeValue( xmlStream, "handle" );

        // <xsd:attribute name="lockId" type="xsd:string"/>
        String lockId = getAttributeValue( xmlStream, "lockId" );

        // <xsd:attribute name="releaseAction" type="wfs:AllSomeType" default="ALL"/>
        String releaseActionString = getAttributeValue( xmlStream, "releaseAction" );
        ReleaseAction releaseAction = parseReleaseAction( releaseActionString );

        // <xsd:attribute name="srsName" type="xsd:anyURI"/>
        String srsName = getAttributeValue( xmlStream, "srsName" );

        nextElement( xmlStream );
        LazyOperationsParser iterable = new LazyOperationsParser( VERSION_200, xmlStream );
        return new Transaction( VERSION_200, handle, lockId, releaseAction, iterable );
    }

    private static ReleaseAction parseReleaseAction( String releaseActionString ) {
        ReleaseAction releaseAction = null;
        if ( releaseActionString != null ) {
            if ( "SOME".equals( releaseActionString ) ) {
                releaseAction = ReleaseAction.SOME;
            } else if ( "ALL".equals( releaseActionString ) ) {
                releaseAction = ReleaseAction.ALL;
            } else {
                String msg = "Invalid value (=" + releaseActionString
                             + ") for release action parameter. Valid values are 'ALL' or 'SOME'.";
                throw new InvalidParameterValueException( msg, "releaseAction" );
            }
        }
        return releaseAction;
    }

    static TransactionAction parseOperation100( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        if ( !WFS_NS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Unexpected element: " + xmlStream.getName()
                         + "' is not a WFS 1.0.0 operation element. Not in the wfs namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        TransactionAction operation = null;
        String localName = xmlStream.getLocalName();
        if ( "Delete".equals( localName ) ) {
            operation = parseDelete110( xmlStream );
            xmlStream.nextTag();
            xmlStream.require( END_ELEMENT, WFS_NS, "Delete" );
            xmlStream.nextTag();
        } else if ( "Insert".equals( localName ) ) {
            operation = parseInsert100( xmlStream );
        } else if ( "Native".equals( localName ) ) {
            operation = parseNative110( xmlStream );
        } else if ( "Update".equals( localName ) ) {
            operation = parseUpdate100( xmlStream );
        } else {
            throw new XMLParsingException( xmlStream, "Unexpected operation element " + localName + "." );
        }
        return operation;
    }

    static TransactionAction parseOperation110( XMLStreamReader xmlStream )
                            throws XMLParsingException, XMLStreamException {

        if ( !WFS_NS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Unexpected element: " + xmlStream.getName()
                         + "' is not a WFS 1.1.0 operation element. Not in the wfs namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        TransactionAction operation = null;
        String localName = xmlStream.getLocalName();
        if ( "Delete".equals( localName ) ) {
            operation = parseDelete110( xmlStream );
            xmlStream.nextTag();
            xmlStream.require( END_ELEMENT, WFS_NS, "Delete" );
            xmlStream.nextTag();
        } else if ( "Insert".equals( localName ) ) {
            operation = parseInsert110( xmlStream );
        } else if ( "Native".equals( localName ) ) {
            operation = parseNative110( xmlStream );
        } else if ( "Update".equals( localName ) ) {
            operation = parseUpdate110( xmlStream );
        } else {
            throw new XMLParsingException( xmlStream, "Unexpected operation element " + localName + "." );
        }
        return operation;
    }

    static TransactionAction parseOperation200( XMLStreamReader xmlStream )
                            throws XMLParsingException, XMLStreamException {

        if ( !WFS_200_NS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Unexpected element: " + xmlStream.getName()
                         + "' is not a WFS 2.0.0 transaction action element. Not in the WFS 2.0.0 namespace.";
            throw new XMLParsingException( xmlStream, msg );
        }

        TransactionAction operation = null;
        String localName = xmlStream.getLocalName();
        if ( "Delete".equals( localName ) ) {
            operation = parseDelete200( xmlStream );
        } else if ( "Insert".equals( localName ) ) {
            operation = parseInsert200( xmlStream );
        } else if ( "Native".equals( localName ) ) {
            operation = parseNative200( xmlStream );
        } else if ( "Replace".equals( localName ) ) {
            operation = parseReplace200( xmlStream );
        } else if ( "Update".equals( localName ) ) {
            operation = parseUpdate200( xmlStream );
        } else {
            throw new XMLParsingException( xmlStream, "Unexpected operation element " + localName + "." );
        }
        nextElement( xmlStream );
        return operation;
    }

    /**
     * Returns the object representation of a <code>wfs:Delete</code> element. Consumes all corresponding events from
     * the given <code>XMLStream</code>.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;wfs:Delete&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/wfs:Delete&gt;) afterwards
     * @return corresponding {@link Delete} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    static Delete parseDelete110( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        // optional: '@handle'
        String handle = xmlStream.getAttributeValue( null, "handle" );

        // required: '@typeName'
        QName ftName = getRequiredAttributeValueAsQName( xmlStream, null, "typeName" );

        // required: 'ogc:Filter'
        xmlStream.nextTag();

        try {
            xmlStream.require( START_ELEMENT, OGCNS, "Filter" );
        } catch ( XMLStreamException e ) {
            // CITE compliance (wfs:wfs-1.1.0-Transaction-tc12.1)
            throw new MissingParameterException( "Mandatory 'ogc:Filter' element is missing in request." );
        }

        Filter filter = Filter110XMLDecoder.parse( xmlStream );
        xmlStream.require( END_ELEMENT, CommonNamespaces.OGCNS, "Filter" );
        return new Delete( handle, ftName, filter );
    }

    /**
     * Returns the object representation of a <code>wfs:Delete</code> element. Consumes all corresponding events from
     * the given <code>XMLStream</code>.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;wfs:Delete&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/wfs:Delete&gt;) afterwards
     * @return corresponding {@link Delete} object, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    static Delete parseDelete200( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        // <xsd:attribute name="handle" type="xsd:string"/>
        String handle = xmlStream.getAttributeValue( null, "handle" );

        // <xsd:attribute name="typeName" type="xsd:QName" use="required"/>
        QName typeName = getRequiredAttributeValueAsQName( xmlStream, null, "typeName" );

        // required: 'fes:Filter'
        nextElement( xmlStream );
        try {
            xmlStream.require( START_ELEMENT, FES_20_NS, "Filter" );
        } catch ( XMLStreamException e ) {
            throw new MissingParameterException( "Mandatory 'fes:Filter' element is missing in Delete." );
        }
        Filter filter = Filter200XMLDecoder.parse( xmlStream );
        nextElement( xmlStream );
        xmlStream.require( END_ELEMENT, WFS_200_NS, "Delete" );
        return new Delete( handle, typeName, filter );
    }

    private static TransactionAction parseNative200( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException();
    }

    private static TransactionAction parseReplace200( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException();
    }

    private static TransactionAction parseUpdate200( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException();
    }

    private static TransactionAction parseInsert200( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the object representation of a <code>wfs:Insert</code> element. NOTE: Does *not* consume all
     * corresponding events from the given <code>XMLStream</code>.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;wfs:Insert&gt;)
     * @return corresponding {@link Insert} object
     * @throws XMLStreamException
     */
    static Insert parseInsert100( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        // optional: '@handle'
        String handle = xmlStream.getAttributeValue( null, "handle" );

        if ( xmlStream.nextTag() != START_ELEMENT ) {
            throw new XMLParsingException( xmlStream, Messages.get( "WFS_INSERT_MISSING_FEATURE_ELEMENT" ) );
        }
        return new Insert( handle, null, null, null, xmlStream );
    }

    /**
     * Returns the object representation of a <code>wfs:Insert</code> element. NOTE: Does *not* consume all
     * corresponding events from the given <code>XMLStream</code>.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;wfs:Insert&gt;)
     * @return corresponding {@link Insert} object
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    static Insert parseInsert110( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        // optional: '@idgen'
        String idGenString = xmlStream.getAttributeValue( null, "idgen" );
        IDGenMode idGen = null;
        if ( idGenString != null ) {
            if ( "GenerateNew".equals( idGenString ) ) {
                idGen = IDGenMode.GENERATE_NEW;
            } else if ( "ReplaceDuplicate".equals( idGenString ) ) {
                idGen = IDGenMode.REPLACE_DUPLICATE;
            } else if ( "UseExisting".equals( idGenString ) ) {
                idGen = IDGenMode.USE_EXISTING;
            } else {
                String msg = Messages.get( "WFS_UNKNOWN_IDGEN_MODE", idGenString, "1.1.0",
                                           "'GenerateNew', 'ReplaceDuplicate' and 'UseExisting'" );
                throw new XMLParsingException( xmlStream, msg );
            }
        }

        // optional: '@handle'
        String handle = xmlStream.getAttributeValue( null, "handle" );

        // optional: '@inputFormat'
        String inputFormat = xmlStream.getAttributeValue( null, "inputFormat" );

        // optional: '@srsName'
        String srsName = xmlStream.getAttributeValue( null, "srsName" );

        if ( xmlStream.nextTag() != START_ELEMENT ) {
            throw new XMLParsingException( xmlStream, Messages.get( "WFS_INSERT_MISSING_FEATURE_ELEMENT" ) );
        }
        return new Insert( handle, idGen, inputFormat, srsName, xmlStream );
    }

    private static TransactionAction parseUpdate100( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        // optional: '@handle'
        String handle = xmlStream.getAttributeValue( null, "handle" );

        // required: '@typeName'
        QName ftName = getRequiredAttributeValueAsQName( xmlStream, null, "typeName" );

        // skip to first "wfs:Property" element
        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, WFS_NS, "Property" );

        return new Update( handle, VERSION_100, ftName, null, null, xmlStream );
    }

    static Update parseUpdate110( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        // optional: '@handle'
        String handle = xmlStream.getAttributeValue( null, "handle" );

        // required: '@typeName'
        QName ftName = getRequiredAttributeValueAsQName( xmlStream, null, "typeName" );

        // optional: '@inputFormat'
        String inputFormat = xmlStream.getAttributeValue( null, "inputFormat" );

        // optional: '@srsName'
        String srsName = xmlStream.getAttributeValue( null, "srsName" );

        // skip to first "wfs:Property" element
        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, WFS_NS, "Property" );

        return new Update( handle, VERSION_110, ftName, inputFormat, srsName, xmlStream );
    }

    public static PropertyReplacement parseProperty100( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        xmlStream.require( START_ELEMENT, WFS_NS, "Property" );
        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, WFS_NS, "Name" );
        QName propName = getElementTextAsQName( xmlStream );
        xmlStream.nextTag();

        PropertyReplacement replacement = null;
        if ( new QName( WFS_NS, "Value" ).equals( xmlStream.getName() ) ) {
            replacement = new PropertyReplacement( propName, xmlStream );
        } else {
            xmlStream.require( END_ELEMENT, WFS_NS, "Property" );
            replacement = new PropertyReplacement( propName, null );
            xmlStream.nextTag();
        }
        return replacement;
    }

    public static PropertyReplacement parseProperty110( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        xmlStream.require( START_ELEMENT, WFS_NS, "Property" );
        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, WFS_NS, "Name" );
        QName propName = getElementTextAsQName( xmlStream );
        xmlStream.nextTag();

        PropertyReplacement replacement = null;
        if ( new QName( WFS_NS, "Value" ).equals( xmlStream.getName() ) ) {
            replacement = new PropertyReplacement( propName, xmlStream );
        } else {
            // if the wfs:Value element is omitted, the property shall be removed (CITE 1.1.0 test,
            // wfs:wfs-1.1.0-Transaction-tc11.1)
            xmlStream.require( END_ELEMENT, WFS_NS, "Property" );
            replacement = new PropertyReplacement( propName, null );
            xmlStream.nextTag();
        }
        return replacement;
    }

    static Native parseNative110( XMLStreamReader xmlStream ) {
        // optional: '@handle'
        String handle = xmlStream.getAttributeValue( null, "handle" );

        // required: '@vendorId'
        String vendorId = getRequiredAttributeValue( xmlStream, "vendorId" );

        // required: '@safeToIgnore'
        boolean safeToIgnore = getRequiredAttributeValueAsBoolean( xmlStream, null, "safeToIgnore" );
        return new Native( handle, vendorId, safeToIgnore, xmlStream );
    }
}
