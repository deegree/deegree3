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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.AbstractCSWRequestDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The Transaction operation defines an interface for creating, modifying and deleting catalogue
 * records. The specific payload being manipulated must be defined in a profile.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class TransactionDocument extends AbstractCSWRequestDocument {

    private static final long serialVersionUID = 7914686453810419662L;

    protected static final ILogger LOG = LoggerFactory.getLogger( TransactionDocument.class );

    /**
     * initializes an empty TransactionDocument
     *
     */
    public TransactionDocument() {
        try {
            setSystemId( XMLFragment.DEFAULT_URL );
        } catch ( MalformedURLException e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     *
     */
    public void createEmptyDocument() {
        Document doc = XMLTools.create();
        Element root = doc.createElementNS( CommonNamespaces.CSWNS.toASCIIString(), "csw:Transaction" );
        setRootElement( root );

    }

    /**
     * initializes a TransactionDocument by reading a DOM object from the passed
     *
     * @see InputStream
     *
     * @param transRoot
     * @throws XMLException
     */
    public TransactionDocument( Element transRoot ) throws XMLException {
        setRootElement( transRoot );
        // setSystemId( XMLFragment.DEFAULT_URL );
    }

    /**
     * parses a CS-W 2.0 transaction request
     *
     * @param id
     *            of the TransactionRequest
     *
     * @return a new transaction parsed from the this xml-encoded request.
     * @throws XMLParsingException
     * @throws OGCWebServiceException
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public Transaction parse( String id )
                            throws XMLParsingException, OGCWebServiceException {

        LOG.logDebug( "parsing CS-W Transaction request" );
        String version = XMLTools.getNodeAsString( getRootElement(), "@version", nsContext, null );
        boolean verbose = XMLTools.getNodeAsBoolean( getRootElement(), "./@verboseResponse", nsContext, false );

        List<Operation> ops = new ArrayList<Operation>();

        ElementList el = XMLTools.getChildElements( getRootElement() );
        for ( int i = 0; i < el.getLength(); i++ ) {
            Element e = el.item( i );
            // TODO check for qualified name
            if ( "Insert".equals( e.getLocalName() ) ) {
                ops.add( parseInsert( e ) );
            } else if ( "Update".equals( e.getLocalName() ) ) {
                ops.add( parseUpdate( e ) );
            } else if ( "Delete".equals( e.getLocalName() ) ) {
                ops.add( parseDelete( e ) );
            }
        }

        // in the future the vendorSpecificParameters
        Map<String, String> vendorSpecificParameters = parseDRMParams( this.getRootElement() );

        return new Transaction( version, id, vendorSpecificParameters, ops, verbose );
    }

    /**
     * parses a Delete element contained in a CS-W Transaction.
     *
     * @param element
     * @return the Delete class parsed from the given Delete element.
     * @throws XMLParsingException
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     */
    private Delete parseDelete( Element element )
                            throws XMLParsingException, MissingParameterValueException, InvalidParameterValueException {

        LOG.logDebug( "parsing CS-W Transaction-Delete" );

        String handle = XMLTools.getNodeAsString( element, "@handle", nsContext, null );
        String tmp = XMLTools.getNodeAsString( element, "@typeName", nsContext, null );
        URI typeName = null;
        if ( tmp != null ) {
            // part of the corrected CS-W 2.0 spec
            try {
                typeName = new URI( tmp );
            } catch ( Exception e ) {
                throw new XMLParsingException( "if defined attribute 'typeName' must be " + "a valid URI" );
            }
        }

        Element elem = (Element) XMLTools.getRequiredNode( element, "./csw:Constraint", nsContext );
        String ver = XMLTools.getNodeAsString( elem, "@version", nsContext, null );
        if ( ver == null ) {
            String s = Messages.getMessage( "CSW_MISSING_CONSTRAINT_VERSION" );
            throw new MissingParameterValueException( s );
        }
        if ( !"1.0.0".equals( ver ) && !"1.1.0".equals( ver ) ) {
            String s = Messages.getMessage( "CSW_INVALID_CONSTRAINT_VERSION", ver );
            throw new InvalidParameterValueException( s );
        }

        elem = (Element) XMLTools.getRequiredNode( elem, "./ogc:Filter", nsContext );

        Filter constraint = AbstractFilter.buildFromDOM( elem, "1.0.0".equals( ver ) );
        return new Delete( handle, typeName, constraint );
    }

    /**
     * parses a Update element contained in a CS-W Transaction.
     *
     * @param element
     * @return the update class containing all parsed values
     * @throws XMLParsingException
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     */
    private Update parseUpdate( Element element )
                            throws XMLParsingException, MissingParameterValueException, InvalidParameterValueException {

        LOG.logDebug( "parsing CS-W Transaction-Update" );

        String handle = XMLTools.getNodeAsString( element, "@handle", nsContext, null );
        String tmp = XMLTools.getNodeAsString( element, "@typeName", nsContext, null );
        URI typeName = null;
        if ( tmp != null ) {
            // part of the corrected CS-W 2.0 spec
            try {
                typeName = new URI( tmp );
            } catch ( Exception e ) {
                throw new XMLParsingException( "if defined attribute 'typeName' must be a valid URI" );
            }
        }
        Element elem = (Element) XMLTools.getRequiredNode( element, "./csw:Constraint", nsContext );
        String ver = XMLTools.getNodeAsString( elem, "@version", nsContext, null );
        if ( ver == null ) {
            String s = Messages.getMessage( "CSW_MISSING_CONSTRAINT_VERSION" );
            throw new MissingParameterValueException( s );
        }
        if ( !"1.0.0".equals( ver ) && !"1.1.0".equals( ver ) ) {
            String s = Messages.getMessage( "CSW_INVALID_CONSTRAINT_VERSION", ver );
            throw new InvalidParameterValueException( s );
        }

        elem = (Element) XMLTools.getRequiredNode( elem, "./ogc:Filter", nsContext );

        Filter constraint = AbstractFilter.buildFromDOM( elem, "1.0.0".equals( ver ) );

        List<Node> children = null;
        List<Node> rp = XMLTools.getNodes( getRootElement(), "./csw:RecordProperty", nsContext );
        if ( rp.size() != 0 ) {
            // at the moment will always be null because it is part of the
            // CS-W 2.0 corrected version that will not be implemented yet
        } else {
            children = XMLTools.getNodes( element, "./child::*", nsContext );
            if ( children.size() == 0 ) {
                throw new XMLParsingException( "one record must be defined within a CS-W update element" );
            }
        }
        return new Update( handle, typeName, constraint, (Element) children.get( 0 ), null );
    }

    /**
     * parses a Insert element contained in a CS-W Transaction.
     *
     * @param element
     * @return an Insert instance
     * @throws XMLParsingException
     */
    private Insert parseInsert( Element element )
                            throws XMLParsingException {

        LOG.logDebug( "parsing CS-W Transaction-Insert" );

        String handle = XMLTools.getNodeAsString( element, "@handle", nsContext, "" );
        List<Element> recList = new ArrayList<Element>();
        List<Node> children = XMLTools.getNodes( element, "*", nsContext );
        if ( children.size() == 0 ) {
            LOG.logError( "at least one record must be defined within a CS-W insert element" );
            throw new XMLParsingException( "at least one record must be defined " + "within a CS-W insert element" );
        }

        String prefixForRim = null;
        for ( Object n : children ) {
            LOG.logDebug( "TransactionDocument(insert): adding the element: " + element.getLocalName()
                          + " to the records list. " );
            if ( prefixForRim == null ) {
                prefixForRim = ( (Element) n ).lookupPrefix( CommonNamespaces.OASIS_EBRIMNS.toASCIIString() );
            }
            recList.add( (Element) n );
        }

        // check if the rim namespace is used
        // nsForRim = XMLTools.getNamespaceForPrefix( "rim", element );

        LOG.logDebug( "TransactionDocument: for the insert operation found following prefix: " + prefixForRim );
        if ( prefixForRim != null ) {
            List<Element> nl = XMLTools.getElements( element, "rim:ExtrinsicObject", nsContext );
            Map<String, Element> extrinsicObjects = new HashMap<String, Element>( nl.size() );
            LOG.logDebug( "TransactionDocument: number of ExtrinsicObjects found: " + extrinsicObjects.size() );
            for ( Element n : nl ) {
                String id = XMLTools.getRequiredNodeAsString( n, "@id", nsContext );
                if ( "".equals( id.trim() ) ) {
                    throw new XMLParsingException( "The value of the id attribute in a {"
                                                   + CommonNamespaces.OASIS_EBRIMNS.toASCIIString()
                                                   + "}extrinsicObject may not be empty" );
                }
                LOG.logDebug( "TransactionDocument: found the id " + id );
                extrinsicObjects.put( id.trim(), n );
            }
            return new Insert( handle, recList, extrinsicObjects );
        }
        // if no ebrim is done, create the old insert class.
        return new Insert( handle, recList );
    }
}
