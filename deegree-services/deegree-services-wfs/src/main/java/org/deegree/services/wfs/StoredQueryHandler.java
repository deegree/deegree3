//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.services.wfs;

import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.deegree.protocol.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;
import static org.deegree.services.wfs.WebFeatureService.getXMLResponseWriter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wfs.storedquery.CreateStoredQuery;
import org.deegree.protocol.wfs.storedquery.DescribeStoredQueries;
import org.deegree.protocol.wfs.storedquery.DropStoredQuery;
import org.deegree.protocol.wfs.storedquery.ListStoredQueries;
import org.deegree.protocol.wfs.storedquery.StoredQueryDefinition;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link CreateStoredQuery}, {@link DescribeStoredQueries}, {@link DropStoredQuery} and
 * {@link ListStoredQueries} requests for the {@link WebFeatureService}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class StoredQueryHandler {

    private static final Logger LOG = LoggerFactory.getLogger( StoredQueryHandler.class );

    public static final String GET_FEATURE_BY_ID = "urn:ogc:def:query:OGC-WFS::GetFeatureById";

    private final Map<String, StoredQueryDefinition> idToQuery = Collections.synchronizedMap( new TreeMap<String, StoredQueryDefinition>() );

    StoredQueryHandler( WebFeatureService webFeatureService ) {
        // add mandatory GetFeatureById query
        URL url = StoredQueryHandler.class.getResource( "idquery.xml" );
        XMLAdapter xmlAdapter = new XMLAdapter( url );
        StoredQueryDefinition queryDefinition = new StoredQueryDefinition( xmlAdapter.getRootElement() );
        addStoredQuery( queryDefinition );
    }

    private void addStoredQuery( StoredQueryDefinition queryDefinition ) {
        LOG.info( "Adding stored query definition with id '" + queryDefinition.getId() + "'" );
        idToQuery.put( queryDefinition.getId(), queryDefinition );
    }

    /**
     * Performs the given {@link CreateStoredQuery} request.
     * 
     * @param request
     *            request to be handled, must not be <code>null</code>
     * @param response
     *            response that is used to write the result, must not be <code>null</code>
     * @throws IOException
     * @throws XMLStreamException
     */
    void doCreateStoredQuery( CreateStoredQuery request, HttpResponseBuffer response ) {
        throw new UnsupportedOperationException( "Performing CreateStoredQuery requests is not implemented yet." );
    }

    /**
     * Performs the given {@link DescribeStoredQueries} request.
     * 
     * @param request
     *            request to be handled, must not be <code>null</code>
     * @param response
     *            response that is used to write the result, must not be <code>null</code>
     * @throws IOException
     * @throws XMLStreamException
     * @throws OWSException
     */
    void doDescribeStoredQueries( DescribeStoredQueries request, HttpResponseBuffer response )
                            throws XMLStreamException, IOException, OWSException {

        List<StoredQueryDefinition> returnedDescriptions = new ArrayList<StoredQueryDefinition>();
        if ( request.getStoredQueryIds().length == 0 ) {
            returnedDescriptions.addAll( idToQuery.values() );
        } else {
            for ( String id : request.getStoredQueryIds() ) {
                StoredQueryDefinition queryDef = idToQuery.get( id );
                if ( queryDef == null ) {
                    String msg = "No StoredQuery with id '" + id + "' is known to this server.";
                    throw new OWSException( msg, INVALID_PARAMETER_VALUE );
                }
                returnedDescriptions.add( queryDef );
            }
        }

        String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
        XMLStreamWriter writer = getXMLResponseWriter( response, "text/xml", schemaLocation );
        writer.setDefaultNamespace( WFS_200_NS );
        writer.writeStartElement( WFS_200_NS, "DescribeStoredQueriesResponse" );
        for ( StoredQueryDefinition queryDef : returnedDescriptions ) {
            XMLStreamReader reader = queryDef.getRootElement().getXMLStreamReader();
            skipStartDocument( reader );
            writeElement( writer, reader );
        }
        writer.writeEndElement();
    }

    /**
     * Performs the given {@link DropStoredQuery} request.
     * 
     * @param request
     *            request to be handled, must not be <code>null</code>
     * @param response
     *            response that is used to write the result, must not be <code>null</code>
     * @throws IOException
     * @throws XMLStreamException
     */
    void doDropStoredQuery( DropStoredQuery dropStoredQuery, HttpResponseBuffer response ) {
        throw new UnsupportedOperationException( "Performing DropStoredQuery requests is not implemented yet." );
    }

    /**
     * Performs the given {@link ListStoredQueries} request.
     * 
     * @param request
     *            request to be handled, must not be <code>null</code>
     * @param response
     *            response that is used to write the result, must not be <code>null</code>
     * @throws IOException
     * @throws XMLStreamException
     */
    void doListStoredQueries( ListStoredQueries listStoredQueries, HttpResponseBuffer response )
                            throws XMLStreamException, IOException {
        String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
        XMLStreamWriter writer = getXMLResponseWriter( response, "text/xml", schemaLocation );
        writer.setDefaultNamespace( WFS_200_NS );
        writer.writeStartElement( WFS_200_NS, "ListStoredQueriesResponse" );
        for ( StoredQueryDefinition queryDef : idToQuery.values() ) {
            writer.writeStartElement( WFS_200_NS, "StoredQuery" );
            writer.writeAttribute( "id", queryDef.getId() );
            for ( LanguageString title : queryDef.getTitles() ) {
                writer.writeStartElement( WFS_200_NS, "Title" );
                if ( title.getLanguage() != null ) {
                    writer.writeAttribute( "xml:lang", title.getLanguage() );
                }
                writer.writeCharacters( title.getString() );
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }
}
