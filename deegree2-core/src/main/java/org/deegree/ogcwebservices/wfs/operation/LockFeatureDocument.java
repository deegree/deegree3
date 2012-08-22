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
package org.deegree.ogcwebservices.wfs.operation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.time.TimeDuration;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.wfs.operation.LockFeature.ALL_SOME_TYPE;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Parser for "wfs:LockFeature" requests.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LockFeatureDocument extends AbstractWFSRequestDocument {

    private static final long serialVersionUID = 7168101158239058596L;

    private static final String XML_TEMPLATE = "LockFeatureTemplate.xml";

    /**
     * Creates a skeleton document that contains the root element and the namespace bindings only.
     *
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = LockFeatureDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    /**
     * Parses the underlying "wfs:LockFeature" document into a {@link LockFeature} object.
     *
     * @param id
     * @return corresponding <code>LockFeature</code> object
     * @throws XMLParsingException
     * @throws InvalidParameterValueException
     */
    public LockFeature parse( String id )
                            throws XMLParsingException, InvalidParameterValueException {

        checkServiceAttribute();
        String version = checkVersionAttribute();

        Element root = this.getRootElement();
        String handle = XMLTools.getNodeAsString( root, "@handle", nsContext, null );

        long expiry = parseExpiry( root );

        String lockActionString = XMLTools.getNodeAsString( root, "@lockAction", nsContext, "ALL" );
        ALL_SOME_TYPE lockAction = ALL_SOME_TYPE.ALL;
        try {
            lockAction = LockFeature.validateLockAction( lockActionString );
        } catch ( InvalidParameterValueException e ) {
            throw new XMLParsingException( e.getMessage() );
        }

        List<Element> lockElements = XMLTools.getRequiredElements( root, "wfs:Lock", nsContext );
        List<Lock> locks = new ArrayList<Lock>( lockElements.size() );
        for ( Element lockElement : lockElements ) {
            locks.add( parseLock( lockElement ) );
        }
        return new LockFeature( version, id, handle, expiry, lockAction, locks );
    }

    /**
     * Parses the value of the expiry-attribute of the given node.
     * <p>
     * Handles both the WFS 1.1.0 and the WFS 1.2.0 formats:
     * <ul>
     * <li>WFS 1.1.0: specified as an xsd:positiveInteger (minutes)</li>
     * <li>WFS 1.2.0: specified as an xsd:duration (identified by the leading P in the string)</li>
     * </ul>
     *
     * @param root
     * @return duration (in milliseconds)
     * @throws XMLParsingException
     */
    static long parseExpiry( Element root )
                            throws XMLParsingException {

        long millis = 0;
        String expiry = XMLTools.getNodeAsString( root, "@expiry", nsContext, LockFeature.DEFAULT_EXPIRY );

        if ( expiry.length() < 1 ) {
            String msg = "Attribute 'expiry' is empty.";
            throw new XMLParsingException( msg );
        }

        if ( expiry.charAt( 0 ) == 'P' ) {
            millis = TimeDuration.createTimeDuration( expiry ).getAsMilliSeconds();
        } else {
            try {
                millis = ( (long) Integer.parseInt( expiry ) ) * 60 * 1000;
                if ( millis < 1 ) {
                    throw new NumberFormatException();
                }
            } catch ( NumberFormatException e ) {
                String msg = Messages.getMessage( "WFS_EXPIRY_ATTRIBUTE_INVALID", expiry );
                throw new XMLParsingException( msg );
            }
        }
        return millis;
    }

    /**
     * Parses the given <code>wfs:Lock</code> element.
     *
     * @param lockElement
     * @return corresponding <code>Lock</code> object
     * @throws XMLParsingException
     */
    private Lock parseLock( Element lockElement )
                            throws XMLParsingException {

        String handle = XMLTools.getNodeAsString( lockElement, "@handle", nsContext, null );
        QualifiedName typeName = XMLTools.getRequiredNodeAsQualifiedName( lockElement, "@typeName", nsContext );
        Filter filter = null;
        Element filterElement = (Element) XMLTools.getNode( lockElement, "ogc:Filter", nsContext );
        if ( filterElement != null ) {
            filter = AbstractFilter.buildFromDOM( filterElement, false );
        }
        return new Lock( handle, typeName, filter );
    }
}
