//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.enterprise.control.ajax;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.datatypes.parameter.ParameterValueIm;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;

/**
 * The abstract listener allows the reuse of basic functionality.
 * 
 * @author <a href="mailto:tfriebe@gmx.net">Torsten Friebe</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * 
 * @version $Revision$
 */

public abstract class AbstractListener {

    private static ILogger LOG = LoggerFactory.getLogger( AbstractListener.class );

    private WebEvent event;

    private List<ParameterValueIm> params;

    private String nextPage;

    protected static int timeout = 10000;

    static {
        if ( System.getProperty( "timeout" ) != null ) {
            timeout = Integer.parseInt( System.getProperty( "timeout" ) );
        }
    }

    /**
     * 
     * @param event
     * @param responseHandler
     */
    public abstract void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException;

    /**
     * 
     * @param event
     * @param responseHandler
     */
    final void handle( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        this.event = event;
        this.actionPerformed( event, responseHandler );
    }

    /**
     * 
     * 
     * @return the servlet request
     */
    protected ServletRequest getRequest() {
        Object source = this.event.getSource();
        return (ServletRequest) source;
    }

    /**
     * @return the path from the servlet context
     */
    protected String getHomePath() {
        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        String path2Dir = session.getServletContext().getRealPath( "/" );
        if ( !path2Dir.startsWith( "/" ) ) {
            path2Dir = '/' + path2Dir;
        }
        return path2Dir;
    }

    /**
     * sets the list of assigned initialization parameters
     * 
     * @param params
     */
    void setInitParameterList( List<ParameterValueIm> params ) {
        this.params = params;
    }

    /**
     * @see #setInitParameterList(List)
     * @return the list of assigned initialization parameters
     */
    protected List<ParameterValueIm> getInitParameterList() {
        return params;
    }

    /**
     * returns a named init parameter or <code>null</code> if the parameter is not known
     * 
     * @param name
     * @return a named init parameter or <code>null</code> if the parameter is not known
     */
    protected String getInitParameter( String name ) {
        for ( int i = 0; i < params.size(); i++ ) {
            ParameterValueIm param = params.get( i );
            if ( param.getDescriptor().getName().equals( name ) ) {
                return (String) param.getValue();
            }
        }
        return null;
    }

    /**
     * @param nextPage
     */
    void setNextPage( String nextPage ) {
        this.nextPage = nextPage;

    }

    /**
     * 
     * @return
     */
    public String getNextPage() {
        return nextPage;
    }

    /**
     * writes an exception bean as a JSON object into output stream of the {@link ResponseHandler}
     * @param responseHandler
     * @param e
     * @throws IOException
     */
    protected void handleException( ResponseHandler responseHandler, Exception e )
                            throws IOException {
        LOG.logError( e.getMessage(), e );
        ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
        responseHandler.writeAndClose( true, eb );
    }
}
