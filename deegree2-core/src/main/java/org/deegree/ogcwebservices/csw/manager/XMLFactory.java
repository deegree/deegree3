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

import java.net.URI;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class XMLFactory {

    private static ILogger LOG = LoggerFactory.getLogger( XMLFactory.class );

    private static final URI CSWNS = CommonNamespaces.CSWNS;

    /**
     * @return a XML representation of a {@link Transaction} object
     *
     * @param transaction
     * @throws XMLParsingException
     * @throws XMLException
     * @throws OGCWebServiceException
     */
    public static final TransactionDocument export( Transaction transaction )
                            throws XMLParsingException, OGCWebServiceException {

        TransactionDocument transDoc = new TransactionDocument();
        try {
            transDoc.createEmptyDocument();
        } catch ( Exception e ) {
            throw new XMLParsingException( e.getMessage() );
        }

        transDoc.getRootElement().setAttribute( "service", "CSW" );
        String version = transaction.getVersion();
        if ( version == null || "".equals( version.trim() ) ) {
            version = "2.0.0";
        }
        transDoc.getRootElement().setAttribute( "version", version );
        transDoc.getRootElement().setAttribute( "verboseResponse", "" + transaction.verboseResponse() );

        List<Operation> ops = transaction.getOperations();
        for ( int i = 0; i < ops.size(); i++ ) {
            Operation operation = ops.get( i );
            appendOperation( transDoc.getRootElement(), operation );
        }

        return transDoc;

    }

    /**
     * @return an XML representation of a {@link TransactionResponse} object
     *
     * @param response
     * @throws XMLParsingException
     */
    public static final HarvetResultDocument export( HarvestResult response )
                            throws XMLParsingException {

        String version = response.getRequest().getVersion();
        if ( version == null || "".equals( version.trim() ) ) {
            version = "2.0.1";
        }

        HarvetResultDocument harvestRespDoc = new HarvetResultDocument();
        try {
            harvestRespDoc.createEmptyDocument( version );
        } catch ( Exception e ) {
            throw new XMLParsingException( e.getMessage() );
        }

        Element root = harvestRespDoc.getRootElement();
        root.setAttribute( "version", version );

        URI namespaceURI = ( version.equals( "2.0.2" ) ) ? CommonNamespaces.CSW202NS : CommonNamespaces.CSWNS;

        Element elem = XMLTools.appendElement( root, namespaceURI, "csw:TransactionSummary" );
        root.appendChild( elem );
        XMLTools.appendElement( elem, namespaceURI, "csw:totalInserted", Integer.toString( response.getTotalInserted() ) );
        XMLTools.appendElement( elem, namespaceURI, "csw:totalUpdated", Integer.toString( response.getTotalUpdated() ) );
        XMLTools.appendElement( elem, namespaceURI, "csw:totalDeleted", Integer.toString( response.getTotalDeleted() ) );

        List<Node> records = response.getResults().getRecords();
        if ( records.size() > 0 ) {
            elem = XMLTools.appendElement( root, namespaceURI, "csw:InsertResult" );
            Element briefRecord = XMLTools.appendElement( elem, namespaceURI, "csw:BriefRecord" );
            Document owner = root.getOwnerDocument();
            for ( int i = 0; i < records.size(); ++i ) {
                LOG.logDebug( "(" + i + " of " + records.size() + ") trying to insert xmlnode: "
                              + records.get( i ).getNodeName() );
                NodeList childs = records.get( i ).getChildNodes();
                for ( int j = 0; j < childs.getLength(); j++ ) {
                    Node a = owner.importNode( childs.item( j ), true );
                    briefRecord.appendChild( a );
                }
            }
        }

        return harvestRespDoc;
    }

    /**
     * @return an XML representation of a {@link TransactionResponse} object
     *
     * @param response
     * @throws XMLParsingException
     */
    public static final TransactionResultDocument export( TransactionResult response )
                            throws XMLParsingException {

        String version = response.getRequest().getVersion();
        if ( version == null || "".equals( version.trim() ) ) {
            version = "2.0.1";
        }

        TransactionResultDocument transRespDoc = new TransactionResultDocument();
        try {
            transRespDoc.createEmptyDocument( version );
        } catch ( Exception e ) {
            throw new XMLParsingException( e.getMessage() );
        }

        Element root = transRespDoc.getRootElement();
        root.setAttribute( "version", version );

        URI namespaceURI = ( version.equals( "2.0.2" ) ) ? CommonNamespaces.CSW202NS : CommonNamespaces.CSWNS;

        Element elem = XMLTools.appendElement( root, namespaceURI, "csw:TransactionSummary" );
        root.appendChild( elem );
        XMLTools.appendElement( elem, namespaceURI, "csw:totalInserted", Integer.toString( response.getTotalInserted() ) );
        XMLTools.appendElement( elem, namespaceURI, "csw:totalUpdated", Integer.toString( response.getTotalUpdated() ) );
        XMLTools.appendElement( elem, namespaceURI, "csw:totalDeleted", Integer.toString( response.getTotalDeleted() ) );

        List<Node> records = response.getResults().getRecords();
        if ( records.size() > 0 ) {
            elem = XMLTools.appendElement( root, namespaceURI, "csw:InsertResult" );
            Element briefRecord = XMLTools.appendElement( elem, namespaceURI, "csw:BriefRecord" );
            Document owner = root.getOwnerDocument();
            for ( int i = 0; i < records.size(); ++i ) {
                LOG.logDebug( "(" + i + " of " + records.size() + ") trying to insert xmlnode: "
                              + records.get( i ).getNodeName() );
                NodeList childs = records.get( i ).getChildNodes();
                for ( int j = 0; j < childs.getLength(); j++ ) {
                    Node a = owner.importNode( childs.item( j ), true );
                    briefRecord.appendChild( a );
                }
            }
            LOG.logDebug( "Successfully inserted " + records.size() + " brief records into the result documents" );
            // root.appendChild( elem );
        }

        return transRespDoc;

    }

    /**
     *
     * @param root
     * @param operation
     * @throws OGCWebServiceException
     */
    public static void appendOperation( Element root, Operation operation )
                            throws OGCWebServiceException {

        if ( "Insert".equals( operation.getName() ) ) {
            appendInsert( root, (Insert) operation );
        } else if ( "Update".equals( operation.getName() ) ) {
            appendUpdate( root, (Update) operation );
        } else if ( "Delete".equals( operation.getName() ) ) {
            appendDelete( root, (Delete) operation );
        } else {
            throw new OGCWebServiceException( "unknown CS-W transaction operation: " + operation.getName() );
        }
    }

    /**
     * appends an Delete operation to the passed root element
     *
     * @param root
     * @param delete
     */
    public static void appendDelete( Element root, Delete delete ) {
        Document doc = root.getOwnerDocument();
        Element op = doc.createElementNS( CSWNS.toASCIIString(), "csw:" + delete.getName() );
        if ( delete.getHandle() != null ) {
            op.setAttribute( "handle", delete.getHandle() );
        }
        if ( delete.getTypeName() != null ) {

            op.setAttribute( "typeName", delete.getTypeName().toASCIIString() );
        }

        Filter filter = delete.getConstraint();
        Element constraint = doc.createElementNS( CSWNS.toASCIIString(), "csw:Constraint" );
        constraint.setAttribute( "version", "1.1.0" );
        op.appendChild( constraint );
        org.deegree.model.filterencoding.XMLFactory.appendFilter( constraint, filter );
        root.appendChild( op );

    }

    /**
     * appends an Update operation to the passed root element
     *
     * @param root
     * @param update
     */
    public static void appendUpdate( Element root, Update update ) {
        Document doc = root.getOwnerDocument();
        Element op = doc.createElementNS( CSWNS.toASCIIString(), "csw:" + update.getName() );
        if ( update.getHandle() != null ) {
            op.setAttribute( "handle", update.getHandle() );
        }
        if ( update.getTypeName() != null ) {
            op.setAttribute( "typeName", update.getTypeName().toASCIIString() );
        }
        XMLTools.insertNodeInto( update.getRecord(), op );
        Filter filter = update.getConstraint();
        Element constraint = doc.createElementNS( CSWNS.toASCIIString(), "csw:Constraint" );
        constraint.setAttribute( "version", "1.1.0" );
        op.appendChild( constraint );
        org.deegree.model.filterencoding.XMLFactory.appendFilter( constraint, filter );
        root.appendChild( op );
    }

    /**
     * appends an Insert operation to the passed root element
     *
     * @param root
     * @param insert
     */
    public static void appendInsert( Element root, Insert insert ) {
        Document doc = root.getOwnerDocument();
        Element tmp = doc.createElementNS( CSWNS.toASCIIString(), "csw:" + insert.getName() );
        Element op = (Element) root.appendChild( tmp );
        if ( insert.getHandle() != null ) {
            op.setAttribute( "handle", insert.getHandle() );
        }
        List<Element> list = insert.getRecords();
        for ( Element e : list ) {
            Node copy = doc.importNode( e, true );
            op.appendChild( copy );
        }
        // for ( int i = 0; i < list.size(); i++ ) {
        // XMLTools.insertNodeInto( list.get( i ), op );
        // }

    }
}
