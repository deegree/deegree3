//$HeadURL$$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn
 and
 - lat/lon GmbH

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

package org.deegree.enterprise.servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.portal.Constants;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DownloadServlet extends HttpServlet {

    private static final long serialVersionUID = -1194881119569788359L;

    private static final ILogger LOG = LoggerFactory.getLogger( DownloadServlet.class );

    private String ipAddress = null;

    private String contentType = "application/zip";

    private String downloadDir = null;

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init( ServletConfig config )
                            throws ServletException {
        super.init( config );
        ipAddress = this.getInitParameter( "ALLOWED_IP_ADDRESS" );
        downloadDir = this.getServletContext().getRealPath( this.getInitParameter( "DOWNLOAD_DIR" ) );
        LOG.logDebug( "************ init download servlet ************" );
        LOG.logDebug( "Download directory: ", downloadDir );
        LOG.logDebug( "ipAddress: ", ipAddress );
        LOG.logDebug( "contentType: ", contentType );
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * 
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     */
    protected void processRequest( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        LOG.logDebug( "Starting to process request" );

        String addr = request.getRemoteAddr();
        LOG.logDebug( "request.remoteAddr: ", addr );

        OutputStream os = response.getOutputStream();
        String docPath = downloadDir;

        ServletContext sc = getServletContext();
        if ( sc != null ) {
            if ( sc.getAttribute( Constants.DOWNLOADDIR ) != null ) {
                docPath = (String) sc.getAttribute( Constants.DOWNLOADDIR );
            }
        }

        File fileDir;
        try {
            fileDir = new File( new URL( docPath ).getFile() );
        } catch ( MalformedURLException e ) {
            LOG.logDebug( "Download dir did not start with 'file', trying just as file now.", e );
            fileDir = new File( docPath );
        }
        LOG.logDebug( "download directory (fileDir): ", fileDir.toString() );

        response.setContentType( contentType );

        RandomAccessFile raf = null;

        // TODO make sure the path exists, otherwise return an error.
        try {
            if ( ipAddress == null || ipAddress.equals( addr ) ) {
                String fileName = request.getParameter( "file" ).trim();
                LOG.logDebug( "zip file name= ", fileName );
                String filename = StringTools.concat( 100, "attachment;filename=\"", fileName, "\"" );
                response.addHeader( "Content-Disposition", filename );
                // response.setHeader( "Content-Disposition", filename );
                // read each file from the local file system
                File f = new File( fileDir, fileName );
                raf = new RandomAccessFile( f, "r" );
                // LOG.logDebug( "raf.name: " + f.toString() );
                long size = raf.length();
                byte[] b = new byte[(int) size];
                // reads the file to the byte array
                raf.read( b );

                // write current byte array (file) to the output stream
                os.write( b );
                LOG.logDebug( "File is written successfully" );
            } else {
                os.write( Messages.getString( "DownloadServlet.ERR_ACC_DENY" ).getBytes( "UTF-8" ) );
            }
        } catch ( Exception e ) {
            LOG.logDebug( "catching error: ", e );
            String message = org.deegree.i18n.Messages.get( "IGEO_STD_CNTXT_WRONG_PATH", fileDir.toString() );
            os.write( message.getBytes( "UTF-8" ) );
            os.write( e.toString().getBytes( "UTF-8" ) );
        } finally {
            try {
                raf.close();
                os.close();
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        processRequest( request, response );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        processRequest( request, response );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#getServletInfo()
     */
    @Override
    public String getServletInfo() {
        return "Servlet for accessing documents assigned to a metadata entry";
    }

}
