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
package org.deegree.portal.cataloguemanager.control;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpMethod;
import org.deegree.framework.concurrent.ExecutionFinishedEvent;
import org.deegree.framework.concurrent.Executor;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.Pair;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.csw.discovery.XMLFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
abstract class AbstractSearchListener extends AbstractMetadataListener {

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractSearchListener.class );

    protected CatalogueManagerConfiguration config;

    protected List<SearchResultInfo> searchResultInfos = new ArrayList<SearchResultInfo>();

    protected static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

    /**
     * 
     * @param getRecords
     * @return
     * @throws Throwable
     */
    protected List<Pair<String, XMLFragment>> performQuery( GetRecords getRecords )
                            throws Throwable {
        XMLFragment gr = XMLFactory.exportWithVersion( getRecords );
        LOG.logDebug( "GetRecords request: ", gr.getAsPrettyString() );
        List<String> csw = config.getSearchableCSW();
        List<Callable<XMLFragment>> callables = new ArrayList<Callable<XMLFragment>>();
        callables.add( new GetRecordsCallable( config.getCatalogueURL(), gr ) );
        for ( String address : csw ) {
            callables.add( new GetRecordsCallable( address, gr ) );
        }

        Executor exe = Executor.getInstance();
        List<ExecutionFinishedEvent<XMLFragment>> events = exe.performSynchronously( callables );

        List<Pair<String, XMLFragment>> result = new ArrayList<Pair<String, XMLFragment>>();

        String xpath1 = "csw202:SearchResults/@numberOfRecordsReturned";
        String xpath2 = "csw202:SearchResults/@numberOfRecordsMatched";
        for ( ExecutionFinishedEvent<XMLFragment> executionFinishedEvent : events ) {
            SearchResultInfo sri = new SearchResultInfo();
            XMLFragment xml = executionFinishedEvent.getResult();
            String cswAddress = ( (GetRecordsCallable) executionFinishedEvent.getTask() ).getCsw();
            // store meta information for search result
            sri.setNumberOfRecordsReturned( XMLTools.getNodeAsInt( xml.getRootElement(), xpath1, nsc, 0 ) );
            sri.setNumberOfRecordsMatched( XMLTools.getNodeAsInt( xml.getRootElement(), xpath2, nsc, 0 ) );
            sri.setCswURL( cswAddress );
            // TODO
            // set catalogue name to search result info
            searchResultInfos.add( sri );
            result.add( new Pair<String, XMLFragment>( cswAddress, xml ) );
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                LOG.logDebug( "queried catalogue: ", cswAddress );
                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    LOG.logDebug( xml.getAsPrettyString() );
                }
            }
        }
        return result;
    }

    /**
     * @param result
     *            first parameter of pair contains CSW URL second one contains GetRecords result
     * @return first string of pair contains CSW URL second one contains formatted GetRecords result
     * @throws Exception
     */
    protected List<SearchResultBean> formatResult( List<Pair<String, XMLFragment>> result )
                            throws Exception {
        List<SearchResultBean> formattedResult = new ArrayList<SearchResultBean>();

        for ( Pair<String, XMLFragment> pair : result ) {
            List<Node> nodes = XMLTools.getNodes( pair.second.getRootElement(), "./csw202:SearchResults/*", nsc );
            for ( Node node : nodes ) {
                Element root = (Element) node;
                try {
                    SearchResultBean srb = new SearchResultBean();
                    srb.setCsw( pair.first );
                    String s = XMLTools.getNodeAsString( root, config.getXPath( "abstract_" ), nsc, "-" );
                    srb.setAbstr( s );

                    s = XMLTools.getNodeAsString( root, config.getXPath( "identifier" ), nsc, "" );
                    if ( s == null ) {
                        s = XMLTools.getNodeAsString( root, config.getXPath( "resourceIdentifier1" ), nsc, null );
                    }
                    if ( s == null ) {
                        s = XMLTools.getNodeAsString( root, config.getXPath( "resourceIdentifier2" ), nsc, null );
                    }
                    if ( s == null ) {
                        s = XMLTools.getNodeAsString( root, config.getXPath( "srvResourceIdentifier1" ), nsc, null );
                    }
                    if ( s == null ) {
                        s = XMLTools.getNodeAsString( root, config.getXPath( "srvResourceIdentifier2" ), nsc, null );
                    }

                    srb.setId( s );
                    s = XMLTools.getNodeAsString( root, config.getXPath( "serviceTitle" ), nsc, null );
                    if ( s == null ) {
                        s = XMLTools.getRequiredNodeAsString( root, config.getXPath( "datasetTitle" ), nsc );
                    }
                    srb.setTitle( s );
                    s = XMLTools.getNodeAsString( root, config.getXPath( "modified" ), nsc, "-" );
                    srb.setModified( s );
                    s = XMLTools.getRequiredNodeAsString( root, config.getXPath( "hlevel" ), nsc );
                    srb.setHierarchyLevel( s );
                    s = XMLTools.getNodeAsString( root, config.getXPath( "overview" ), nsc, null );
                    srb.setOverview( s );
                    String west = XMLTools.getNodeAsString( root, config.getXPath( "srvWest" ), nsc, null );
                    if ( west == null ) {
                        west = XMLTools.getRequiredNodeAsString( root, config.getXPath( "west" ), nsc );
                    }
                    String east = XMLTools.getNodeAsString( root, config.getXPath( "srvEast" ), nsc, null );
                    if ( east == null ) {
                        east = XMLTools.getRequiredNodeAsString( root, config.getXPath( "east" ), nsc );
                    }
                    String south = XMLTools.getNodeAsString( root, config.getXPath( "srvSouth" ), nsc, null );
                    if ( south == null ) {
                        south = XMLTools.getRequiredNodeAsString( root, config.getXPath( "south" ), nsc );
                    }
                    String north = XMLTools.getNodeAsString( root, config.getXPath( "srvNorth" ), nsc, null );
                    if ( north == null ) {
                        north = XMLTools.getRequiredNodeAsString( root, config.getXPath( "north" ), nsc );
                    }
                    srb.setBbox( west + ' ' + south + ' ' + east + ' ' + north );
                    formattedResult.add( srb );
                } catch ( Exception e ) {
                    LOG.logError( e );
                }
            }
        }
        return formattedResult;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    // inner classes
    // ////////////////////////////////////////////////////////////////////////////////////////////////////

    private class GetRecordsCallable implements Callable<XMLFragment> {

        private String csw;

        private XMLFragment getRecords;

        /**
         * 
         * @param csw
         * @param getRecords
         */
        GetRecordsCallable( String csw, XMLFragment getRecords ) {
            this.csw = csw;
            this.getRecords = getRecords;
        }

        /**
         * @return the csw
         */
        public String getCsw() {
            return csw;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @SuppressWarnings("unchecked")
        public XMLFragment call()
                                throws Exception {
            LOG.logDebug( "connect to: ", csw );
            Enumeration<String> en = ( (HttpServletRequest) getRequest() ).getHeaderNames();
            Map<String, String> map = new HashMap<String, String>();
            while ( en.hasMoreElements() ) {
                String name = (String) en.nextElement();
                if ( !name.equalsIgnoreCase( "accept-encoding" ) && !name.equalsIgnoreCase( "content-length" )
                     && !name.equalsIgnoreCase( "user-agent" ) ) {
                    map.put( name, ( (HttpServletRequest) getRequest() ).getHeader( name ) );
                }
            }
            if ( LOG.isDebug() ) {
                LOG.logDebug( "header: " );
                StringTools.printMap( map, null );
            }
            HttpMethod method = HttpUtils.performHttpPost( csw, getRecords, 60000, null, null, map );
            XMLFragment xml = new XMLFragment();
            if ( LOG.isDebug() ) {
                LOG.logDebug( "response content: " );
                String s = method.getResponseBodyAsString();
                LOG.logDebug( s );
                xml.load( new StringReader( s ), csw );
            } else {
                xml.load( method.getResponseBodyAsStream(), csw );
            }
            return xml;
        }

    }

}
