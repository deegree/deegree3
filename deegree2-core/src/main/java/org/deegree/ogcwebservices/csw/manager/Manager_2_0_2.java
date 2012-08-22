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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.EchoRequest;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 */

public class Manager_2_0_2 extends AbstractManager {

    private static final ILogger LOG = LoggerFactory.getLogger( Manager_2_0_2.class );

    private static final URL xsltURL = Manager_2_0_2.class.getResource( "iso_ap_1_0_full2brief.xsl" );

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
            XMLFragment transactionDocument = XMLFactory.export( request );
            String nsp = getAllNamespaceDeclarations( transactionDocument.getRootElement() );
            StringWriter sww = new StringWriter( 15000 );
            transactionDocument.write( sww );
            transactionDocument.load( new StringReader( sww.getBuffer().toString() ), XMLFragment.DEFAULT_URL );

            synchronized ( IN_XSL ) {
                Map<String, String> param = new HashMap<String, String>();
                param.put( "NSP", nsp );
                try {
                    wfsTransactionDocument = IN_XSL.transform( transactionDocument, XMLFragment.DEFAULT_URL, null,
                                                               param );
                } catch ( MalformedURLException e ) {
                    LOG.logError( e.getMessage(), e );
                }
            }

            if ( LOG.isDebug() ) {
                LOG.logDebug( "The (first) resulting wfs:Transaction document will be written to file" );
                LOG.logDebugXMLFile( "first", wfsTransactionDocument );
                // LOG.logDebug( "*****First Generated WFS GetFeature request:\n"
                // + getFeatureDocument.getAsPrettyString() );
            }
            // LOG.logDebug( "The (first) resulting wfs:Transaction document: \n "
            // + wfsTransactionDocument.getAsPrettyString() );

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
        List<Document> briefDocs = new ArrayList<Document>();
        List<Operation> ops = request.getOperations();
        for ( int i = 0; i < ops.size(); i++ ) {
            if ( ops.get( i ) instanceof Insert ) {
                try {
                    briefDocs = getAsBriefDocuments( briefDocs, (Insert) ops.get( i ) );
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
                }
            }
        }
        try {
            if ( briefDocs.size() > 0 ) {
                wfsTransRespDoc = replaceIds( wfsTransRespDoc, briefDocs );
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

    private String getAllNamespaceDeclarations( Element doc ) {
        Map<String, String> nsp = new HashMap<String, String>();
        nsp = collect( nsp, doc );
        Iterator<String> iter = nsp.keySet().iterator();
        StringBuffer sb = new StringBuffer( 1000 );
        while ( iter.hasNext() ) {
            String s = iter.next();
            String val = nsp.get( s );
            sb.append( s ).append( ":" ).append( val );
            if ( iter.hasNext() ) {
                sb.append( ';' );
            }
        }
        return sb.toString();
    }

    private Map<String, String> collect( Map<String, String> nsp, Node node ) {
        NamedNodeMap nnm = node.getAttributes();
        if ( nnm != null ) {
            for ( int i = 0; i < nnm.getLength(); i++ ) {
                String s = nnm.item( i ).getNodeName();
                if ( s.startsWith( "xmlns:" ) ) {
                    nsp.put( s.substring( 6, s.length() ), nnm.item( i ).getNodeValue() );
                }
            }
        }

        NodeList nl = node.getChildNodes();
        if ( nl != null ) {
            for ( int i = 0; i < nl.getLength(); i++ ) {
                collect( nsp, nl.item( i ) );
            }
        }
        return nsp;
    }

    /**
     * replaces the id values of WFS Insert result with corresponding metadata brief representations
     *
     * @param wfsTransRespDoc
     * @param briefDocs
     * @return an xmlFragment with the gml:Feature ids replaced with the id' s given in the list
     * @throws XMLParsingException
     */
    private XMLFragment replaceIds( XMLFragment wfsTransRespDoc, List<Document> briefDocs )
                            throws XMLParsingException {

        List<Node> nodes = XMLTools.getRequiredNodes( wfsTransRespDoc.getRootElement(),
                                                      "./wfs:InsertResults/wfs:Feature/ogc:FeatureId",
                                                      CommonNamespaces.getNamespaceContext() );
        Element parent = null;
        for ( int i = 0; i < nodes.size(); i++ ) {
            Element elem = (Element) nodes.get( i );
            parent = (Element) elem.getParentNode();
            parent.removeChild( elem );
        }
        if ( parent != null ) {
            for ( int i = 0; i < briefDocs.size(); i++ ) {
                parent = (Element) XMLTools.insertNodeInto( briefDocs.get( i ).getDocumentElement(), parent );
            }
        }

        return wfsTransRespDoc;
    }

    /**
     * all inserted records into threir brief representation
     *
     * @param docs
     * @param insert
     * @return a list of brief metadata records
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    private List<Document> getAsBriefDocuments( List<Document> docs, Insert insert )
                            throws IOException, SAXException, TransformerException {
        List<Element> records = insert.getRecords();

        XSLTDocument xslt = new XSLTDocument( xsltURL );

        for ( int i = 0; i < records.size(); i++ ) {
            XMLFragment xml = new XMLFragment();
            xml.setRootElement( records.get( i ) );
            xml = xslt.transform( xml );
            docs.add( xml.getRootElement().getOwnerDocument() );
        }
        return docs;
    }

}
