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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.EchoRequest;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionResponse;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 */

public class Manager_2_0_0 extends AbstractManager {

    private static final ILogger LOG = LoggerFactory.getLogger( Manager_2_0_0.class );

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.csw.manager.Manager#harvestRecords(org.deegree.ogcwebservices.csw.manager.Harvest)
     */
    public EchoRequest harvestRecords( Harvest request )
                            throws OGCWebServiceException {
        try {
            HarvesterFactory hf = new HarvesterFactory( harvester );
            AbstractHarvester h = hf.findHarvester( request );
            h.addRequest( request );
            if ( !h.isRunning() ) {
                h.startHarvesting();
            }
            if ( request.getHarvestInterval() == null ) {
                // h.removeRequest( request );
            }
        } catch ( Exception e ) {
            LOG.logError( "could not perform harvest operation", e );
            throw new OGCWebServiceException( getClass().getName(), "could not perform harvest operation"
                                                                    + e.getMessage() );
        }

        return new EchoRequest( request.getId(), null );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.csw.manager.Manager#transaction(org.deegree.ogcwebservices.csw.manager.Transaction)
     */
    public TransactionResult transaction( Transaction request )
                            throws OGCWebServiceException {
        XMLFragment wfsTransactionDocument = null;
        try {
            XMLFragment transactionDocument = new XMLFragment( XMLFactory.export( request ).getRootElement() );
            StringWriter sww = new StringWriter( 15000 );
            transactionDocument.write( sww );
            transactionDocument.load( new StringReader( sww.getBuffer().toString() ), XMLFragment.DEFAULT_URL );
            wfsTransactionDocument = IN_XSL.transform( transactionDocument );
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                LOG.logDebug( "The (first) resulting wfs:Transaction document: \n "
                              + wfsTransactionDocument.getAsPrettyString() );
            }
        } catch ( SAXException saxe ) {
            String msg = org.deegree.i18n.Messages.get( "CSW_CREATE_TRANSACTION_ERROR", saxe.getMessage() );
            LOG.logError( msg, saxe );
            throw new OGCWebServiceException( msg );
        } catch ( IOException ioe ) {
            String msg = org.deegree.i18n.Messages.get( "CSW_CREATE_TRANSACTION_ERROR", ioe.getMessage() );
            LOG.logError( msg, ioe );
            throw new OGCWebServiceException( msg );
        } catch ( TransformerException te ) {
            String msg = org.deegree.i18n.Messages.get( "CSW_CREATE_TRANSACTION_ERROR", te.getMessage() );
            LOG.logError( msg, te );
            throw new OGCWebServiceException( msg );
        } catch ( XMLParsingException xmle ) {
            String msg = org.deegree.i18n.Messages.get( "CSW_CREATE_TRANSACTION_ERROR", xmle.getMessage() );
            LOG.logError( msg, xmle );
            throw new OGCWebServiceException( msg );
        }

        org.deegree.ogcwebservices.wfs.operation.transaction.Transaction wfstrans = null;
        try {
            LOG.logDebug( "Creating a wfs transaction from the document" );
            wfstrans = org.deegree.ogcwebservices.wfs.operation.transaction.Transaction.create(
                                                                                                request.getId(),
                                                                                                wfsTransactionDocument.getRootElement() );
        } catch ( OGCWebServiceException ogcwe ) {
            LOG.logError( ogcwe.getMessage(), ogcwe );
            String msg = org.deegree.i18n.Messages.get( "CSW_CREATE_TRANSACTION_ERROR2", ogcwe.getMessage() );
            throw new OGCWebServiceException( msg );
        }

        Object wfsResponse = null;
        try {
            LOG.logDebug( "Sending the wfs transaction to the wfservice." );
            wfsResponse = wfsService.doService( wfstrans );
        } catch ( OGCWebServiceException e ) {
            String msg = org.deegree.i18n.Messages.get( "CSW_PERFORMING_TRANSACTION_ERROR", e.getMessage() );
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        if ( !( wfsResponse instanceof org.deegree.ogcwebservices.wfs.operation.transaction.TransactionResponse ) ) {
            String msg = org.deegree.i18n.Messages.get( "CSW_WRONG_TRANSACTION_RESULTTYPE",
                                                        wfsResponse.getClass().getName() );
            LOG.logError( msg );
            throw new OGCWebServiceException( msg );
        }

        TransactionResponse transResp = (TransactionResponse) wfsResponse;
        XMLFragment wfsTransRespDoc = null;
        try {
            LOG.logDebug( "Parsing the wfs response." );
            wfsTransRespDoc = org.deegree.ogcwebservices.wfs.XMLFactory.export( transResp );
        } catch ( IOException e ) {
            String msg = "export of WFS Transaction response as XML failed: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        // --------------------------------------------------------------
        // the following section will replace the feature ids returned by
        // the WFS for Insert requests by the ID of the inserted metadata sets
        List<String> ids = new ArrayList<String>();
        List<Operation> ops = request.getOperations();
        for ( int i = 0; i < ops.size(); i++ ) {
            if ( ops.get( i ) instanceof Insert ) {
                try {
                    ids = extractIdentifiers( ids, (Insert) ops.get( i ) );
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
                }
            }
        }
        try {
            if ( ids.size() > 0 ) {
                wfsTransRespDoc = replaceIds( wfsTransRespDoc, ids );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
        }
        // ---------------------------------------------------------------

        TransactionResultDocument cswTransactionDocument = null;
        try {
            XMLFragment tmp = OUT_XSL.transform( wfsTransRespDoc );
            cswTransactionDocument = new TransactionResultDocument();
            cswTransactionDocument.setRootElement( tmp.getRootElement() );
        } catch ( TransformerException e ) {
            String msg = org.deegree.i18n.Messages.get( "CSW_TRANSACTION_RESULT_TRANS_ERR", e.getMessage() );
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }
        TransactionResult result = null;
        try {
            result = cswTransactionDocument.parseTransactionResponse( request );
        } catch ( XMLParsingException e ) {
            String msg = org.deegree.i18n.Messages.get( "CSW_TRANSACTION_RESULT_PARSE_ERR" );
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }
        return result;
    }

    /**
     * replaces the id values of WFS Insert result with corresponding metadata identifieres
     *
     * @param wfsTransRespDoc
     * @param ids
     * @return an xmlFragment with the gml:Feature ids replaced with the id' s given in the list
     * @throws XMLParsingException
     */
    private XMLFragment replaceIds( XMLFragment wfsTransRespDoc, List<String> ids )
                            throws XMLParsingException {

        List<Element> nodes = XMLTools.getRequiredElements( wfsTransRespDoc.getRootElement(),
                                                            "./wfs:InsertResults/wfs:Feature/ogc:FeatureId",
                                                            CommonNamespaces.getNamespaceContext() );
        for ( int i = 0; i < nodes.size(); i++ ) {
            Element elem = nodes.get( i );
            elem.setAttribute( "fid", ids.get( i ) );
        }

        return wfsTransRespDoc;
    }

    /**
     * extracts all identifiers of the records to be inserted in correct order
     *
     * @param ids
     * @param insert
     * @return a list of identifiers which should be keys to xpaths which are defined in the
     *         messages.properties
     * @throws XMLParsingException
     */
    private List<String> extractIdentifiers( List<String> ids, Insert insert )
                            throws XMLParsingException {
        List<Element> records = insert.getRecords();

        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

        for ( int i = 0; i < records.size(); i++ ) {
            String xpath = getIdentifierXPath( records.get( i ) );
            String fileIdentifier = XMLTools.getRequiredNodeAsString( records.get( i ), xpath, nsc );
            ids.add( fileIdentifier );
        }
        return ids;
    }

    /**
     * returns the XPath the metadata records identifier
     *
     * @param metaData
     * @return the XPath the metadata records identifier
     */
    private String getIdentifierXPath( Element metaData ) {

        // default is iso 19115
        String xpath = "iso19115:fileIdentifier/smXML:CharacterString";
        if ( metaData != null ) {
            String nspace = metaData.getNamespaceURI();
            nspace = StringTools.replace( nspace, "http://", "", true );
            xpath = Messages.getString( "Identifier_" + nspace );
        }
        return xpath;
    }

}
