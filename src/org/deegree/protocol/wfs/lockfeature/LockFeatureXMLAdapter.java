//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

package org.deegree.protocol.wfs.lockfeature;

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.getfeature.TypeName;

/**
 * Adapter between XML <code>LockFeature</code> requests and {@link LockFeature} objects.
 * <p>
 * TODO code for exporting to XML
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LockFeatureXMLAdapter extends AbstractWFSRequestXMLAdapter {

    /**
     * Parses a WFS <code>LockFeature</code> document into a {@link LockFeature} object.
     * <p>
     * Supported versions:
     * <ul>
     * <li>WFS 1.1.0</li>
     * </ul>
     * 
     * @return parsed {@link LockFeature} request
     * @throws Exception
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws MissingParameterException
     *             if the request version is unsupported
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public LockFeature parse()
                            throws Exception {
        Version version = Version.parseVersion( getNodeAsString( rootElement, new XPath( "@version", nsContext ), null ) );

        LockFeature result = null;

        if ( VERSION_100.equals( version ) )
            result = parse100();
        else if ( VERSION_110.equals( version ) )
            result = parse110();
        else {
            throw new Exception( "Version " + version
                                 + " is not supported for parsing (for now). Only 1.1.0 is supported." );
        }
        return result;
    }

    /**
     * Parses a WFS 1.0.0 <code>LockFeature</code> document into a {@link LockFeature} object.
     * 
     * @return corresponding {@link LockFeature} instance
     */
    @SuppressWarnings("boxing")
    public LockFeature parse100() {

        String handle = getNodeAsString( rootElement, new XPath( "@handle", nsContext ), null );
        int expiry = getNodeAsInt( rootElement, new XPath( "@expiry", nsContext ), -1 );
        String lockActionStr = rootElement.getAttributeValue( new QName( "lockAction" ) );
        Boolean lockAll = null;
        if ( lockActionStr != null ) {
            if ( "ALL".equals( lockActionStr ) ) {
                lockAll = true;
            } else if ( "SOME".equals( lockActionStr ) ) {
                lockAll = false;
            } else {
                String msg = Messages.get( "WFS_UNKNOWN_LOCK_ACTION", lockActionStr, VERSION_100, "ALL or SOME" );
                throw new XMLParsingException( this, rootElement, msg );
            }
        }

        List<OMElement> lockElements = getRequiredElements( rootElement, new XPath( "wfs:Lock", nsContext ) );
        LockOperation[] locks = new LockOperation[lockElements.size()];
        int i = 0;
        for ( OMElement lockElement : lockElements ) {
            locks[i++] = parseLock100( lockElement );
        }
        return new LockFeature( VERSION_100, handle, locks, expiry, lockAll );
    }

    private LockOperation parseLock100( OMElement lockElement ) {

        String handle = getNodeAsString( lockElement, new XPath( "@handle", nsContext ), null );
        // TODO can there be an alias for the typeName ??
        TypeName typeName = new TypeName( getRequiredNodeAsQName( lockElement, new XPath( "@typeName", nsContext ) ),
                                          null );

        Filter filter = null;
        OMElement filterEl = lockElement.getFirstChildWithName( new QName( OGCNS, "Filter" ) );
        if ( filterEl != null ) {
            try {
                // TODO remove usage of wrapper (necessary at the moment to work around problems with AXIOM's
                // XMLStreamReader)
                XMLStreamReader xmlStream = new XMLStreamReaderWrapper( filterEl.getXMLStreamReaderWithoutCaching(),
                                                                        null );
                // skip START_DOCUMENT
                xmlStream.nextTag();
                // TODO use filter 1.0.0 parser
                filter = Filter100XMLDecoder.parse( xmlStream );
            } catch ( XMLStreamException e ) {
                e.printStackTrace();
                throw new XMLParsingException( this, filterEl, e.getMessage() );
            }
        }
        return new FilterLock( handle, typeName, filter );
    }

    /**
     * Parses a WFS 1.1.0 <code>LockFeature</code> document into a {@link LockFeature} object.
     * 
     * @return corresponding {@link LockFeature} instance
     */
    @SuppressWarnings("boxing")
    public LockFeature parse110() {

        String handle = getNodeAsString( rootElement, new XPath( "@handle", nsContext ), null );
        int expiry = getNodeAsInt( rootElement, new XPath( "@expiry", nsContext ), -1 );
        String lockActionStr = rootElement.getAttributeValue( new QName( "lockAction" ) );
        Boolean lockAll = null;
        if ( lockActionStr != null ) {
            if ( "ALL".equals( lockActionStr ) ) {
                lockAll = true;
            } else if ( "SOME".equals( lockActionStr ) ) {
                lockAll = false;
            } else {
                String msg = Messages.get( "WFS_UNKNOWN_LOCK_ACTION", lockActionStr, VERSION_110, "ALL or SOME" );
                throw new XMLParsingException( this, rootElement, msg );
            }
        }

        List<OMElement> lockElements = getRequiredElements( rootElement, new XPath( "wfs:Lock", nsContext ) );
        LockOperation[] locks = new LockOperation[lockElements.size()];
        int i = 0;
        for ( OMElement lockElement : lockElements ) {
            locks[i++] = parseLock110( lockElement );
        }
        return new LockFeature( VERSION_110, handle, locks, expiry, lockAll );
    }

    private LockOperation parseLock110( OMElement lockElement ) {

        String handle = getNodeAsString( lockElement, new XPath( "@handle", nsContext ), null );
        // TODO can there be an alias for the typeName ??
        TypeName typeName = new TypeName( getRequiredNodeAsQName( lockElement, new XPath( "@typeName", nsContext ) ),
                                          null );

        Filter filter = null;
        OMElement filterEl = lockElement.getFirstChildWithName( new QName( OGCNS, "Filter" ) );
        if ( filterEl != null ) {
            try {
                // TODO remove usage of wrapper (necessary at the moment to work around problems with AXIOM's
                // XMLStreamReader)
                XMLStreamReader xmlStream = new XMLStreamReaderWrapper( filterEl.getXMLStreamReaderWithoutCaching(),
                                                                        null );
                // skip START_DOCUMENT
                xmlStream.nextTag();
                filter = Filter110XMLDecoder.parse( xmlStream );
            } catch ( XMLStreamException e ) {
                e.printStackTrace();
                throw new XMLParsingException( this, filterEl, e.getMessage() );
            }
        }
        return new FilterLock( handle, typeName, filter );
    }
}
