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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.csw.discovery.Query;
import org.deegree.ogcwebservices.csw.discovery.GetRecords.RESULT_TYPE;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PageingListener extends AbstractSearchListener {

    private static final ILogger LOG = LoggerFactory.getLogger( PageingListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {

        config = getCatalogueManagerConfiguration( event );
        Map<String, String> param = event.getParameter();

        GetRecords getRecords = (GetRecords) event.getSession().getAttribute( CURRENTSEARCH );
        int counter = Integer.parseInt( param.get( "counter" ) );

        if ( counter < 0 ) {
            // '<' button has been pressed
            int tmp = (Integer) event.getSession().getAttribute( COUNTER );
            counter = tmp - 1;
            if ( counter < 0 ) {
                counter = 0;
            }
        } else if ( counter > 999 ) {
            // '>' button has been pressed
            int tmp = (Integer) event.getSession().getAttribute( COUNTER );
            counter = tmp + 1;
            if ( counter > 4 ) {
                counter = 4;
            }
        }
        Query query = getRecords.getQuery();
        // in/decrease start position for query
        getRecords = new GetRecords( UUID.randomUUID().toString(), "2.0.2", null, null, RESULT_TYPE.RESULTS,
                                     "application/xml", "http://www.isotc211.org/2005/gmd",
                                     1 + ( counter * config.getStepSize() ), config.getStepSize(), -1, null, query );

        List<Pair<String, XMLFragment>> result;
        try {
            result = performQuery( getRecords );
        } catch ( Throwable e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, bean );
            return;
        }

        List<SearchResultBean> formattedResult = null;
        try {
            formattedResult = formatResult( result );
        } catch ( Exception e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, bean );
            return;
        }

        // store search meta informations to be used for pageing
        event.getSession().setAttribute( SEARCHRESULTINFO, searchResultInfos );

        int c = 0;
        for ( SearchResultInfo sri : searchResultInfos ) {
            c += sri.getNumberOfRecordsMatched();
        }

        event.getSession().setAttribute( CURRENTSEARCH, getRecords );
        event.getSession().setAttribute( COUNTER, counter );

        responseHandler.writeAndClose( false, new Object[] { formattedResult, new Integer( c ), new Integer( counter ) } );

    }

}
