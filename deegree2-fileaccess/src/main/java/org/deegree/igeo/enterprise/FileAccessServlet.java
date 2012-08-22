//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.igeo.enterprise;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SQLRegistry;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.model.User;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class FileAccessServlet extends HttpServlet {

    private static final long serialVersionUID = 5150172895633530142L;

    private static final ILogger LOG = LoggerFactory.getLogger( FileAccessServlet.class );

    private static final String PROJECT_EXT = "prj";

    // private static final String MODULE_EXT = "mdx";

    private static final String GML_EXT = "gml";

    // private static final String SHAPE_EXT = "shp";

    private static final String GIF_EXT = "gif";

    private static final String PNG_EXT = "png";

    private static final String TIF_EXT = "tif";

    private static final String TIFF_EXT = "tiff";

    private static final String JPG_EXT = "jpg";

    private static final String JPEG_EXT = "jpeg";
    
    private static final String GPX_EXT = "gpx";

    private String root;

    private boolean readRootDir;

    private Properties secProps = new Properties();

    @Override
    public void init()
                            throws ServletException {
        super.init();
        root = getInitParameter( "rootdirectory" );
        File dir = new File( root );
        if ( !dir.isAbsolute() ) {
            root = getServletContext().getRealPath( root );
        }
        readRootDir = "true".equalsIgnoreCase( getInitParameter( "readRootDir" ) );
        secProps.put( "driver", getInitParameter( "driver" ) );
        secProps.put( "url", getInitParameter( "url" ) );
        secProps.put( "user", getInitParameter( "sec_db_user" ) );
        secProps.put( "password", getInitParameter( "sec_db_password" ) );
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {

        Map<String, String> params = KVP2Map.toMap( req );
        LOG.logDebug( "request parameter: ", params );
        if ( params.get( "USER" ) == null ) {
            params.put( "USER", "" );
        } else {
            try {
                authenticate( params );
            } catch ( Exception e ) {
                sendMessage( "could not authenticate user", resp );
                return;
            }
        }
        String action = params.get( "ACTION" );
        if ( action == null ) {
            LOG.logError( "Action: " + action + " not known" );
            sendMessage( "Action: " + action + " not known", resp );
            return;
        }
        try {
            Method method = getClass().getMethod( action, Map.class, HttpServletRequest.class,
                                                  HttpServletResponse.class );
            method.invoke( this, params, req, resp );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            sendMessage( e.getMessage(), resp );
        }
    }

    /**
     * @param params
     * @throws GeneralSecurityException
     */
    private void authenticate( Map<String, String> params )
                            throws GeneralSecurityException {
        if ( !secProps.get( "driver" ).equals( "dummy" ) ) {
            if ( !SecurityAccessManager.isInitialized() ) {
                SecurityAccessManager.initialize( SQLRegistry.class.getName(), secProps, 5000 );
            }
            SecurityAccessManager sam = SecurityAccessManager.getInstance();
            User user = sam.getUserByName( params.get( "USER" ) );
            user.authenticate( params.get( "PASSWORD" ) );
        }
    }

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        doGet( req, resp );
    }

    /**
     * @param string
     * @param resp
     * @throws IOException
     */
    private void sendMessage( String string, HttpServletResponse resp )
                            throws IOException {
        PrintWriter pw = resp.getWriter();
        if ( string == null ) {
            string = "null point exception";
        }
        pw.write( string );
        pw.close();

    }

    /**
     * returns a list of all projects a user is allowed to read
     * 
     * @param params
     * @param req
     * @param resp
     * @throws IOException
     */
    public void listProjects( Map<String, String> params, HttpServletRequest req, HttpServletResponse resp )
                            throws IOException {
        List<String> list = new ArrayList<String>( 50 );
        File dir = new File( root + "/" + params.get( "USER" ) );
        LOG.logDebug( "read directory: ", dir );
        File[] files = dir.listFiles( new ConvenienceFileFilter( false, PROJECT_EXT ) );
        for ( File file : files ) {
            list.add( file.getName() );
        }
        if ( readRootDir ) {
            dir = new File( root );
            files = dir.listFiles( new ConvenienceFileFilter( false, PROJECT_EXT ) );
            for ( File file : files ) {
                list.add( "../" + file.getName() );
            }
        }
        Collections.sort( list );
        sendMessage( StringTools.listToString( list, ',' ), resp );
    }

    /**
     * returns a list of all data files a user is allowed to read
     * 
     * @param params
     * @param req
     * @param resp
     * @throws IOException
     */
    public void listDataFiles( Map<String, String> params, HttpServletRequest req, HttpServletResponse resp )
                            throws IOException {
        List<String> list = new ArrayList<String>( 50 );
        File dir = new File( root + "/" + params.get( "USER" ) );
        LOG.logDebug( "read directory: ", dir );
        File[] files = dir.listFiles( new ConvenienceFileFilter( false, GML_EXT, GIF_EXT, TIF_EXT, TIFF_EXT, JPEG_EXT,
                                                                 JPG_EXT, PNG_EXT, GPX_EXT ) );
        for ( File file : files ) {
            list.add( file.getName() );
        }
        if ( readRootDir ) {
            dir = new File( root );
            files = dir.listFiles( new ConvenienceFileFilter( false, GML_EXT, GIF_EXT, TIF_EXT, TIFF_EXT, JPEG_EXT,
                                                              JPG_EXT, PNG_EXT, GPX_EXT ) );
            for ( File file : files ) {
                list.add( "../" + file.getName() );
            }
        }
        Collections.sort( list );
        sendMessage( StringTools.listToString( list, ',' ), resp );
    }

    /**
     * reads a file
     * 
     * @param params
     * @param req
     * @param resp
     * @throws IOException
     */
    public void readFile( Map<String, String> params, HttpServletRequest req, HttpServletResponse resp )
                            throws IOException {
        File file = createFileName( params );
        LOG.logDebug( "read file: ", file );
        if ( file != null && file.exists() ) {
            FileInputStream fis = new FileInputStream( file );
            OutputStream os = resp.getOutputStream();
            byte[] buffer = new byte[4096];
            int cnt = 0;
            while ( ( cnt = fis.read( buffer ) ) == buffer.length ) {
                os.write( buffer );
            }
            if ( cnt > 0 ) {
                os.write( buffer, 0, cnt );
            }
            fis.close();
            os.close();
        } else {
            LOG.logError( "file dos not exist: " + file );
        }
    }

    /**
     * writes a file into the users directory
     * 
     * @param params
     * @param req
     * @param resp
     * @throws IOException
     */
    public void writeFile( Map<String, String> params, HttpServletRequest req, HttpServletResponse resp )
                            throws Exception {
        if ( params.get( "USER" ).length() == 0 ) {
            throw new Exception( "user must be set and authenticated" );
        }
        File file = createFileName( params );
        LOG.logDebug( "write file: ", file );
        String s = file.getName().toLowerCase();
        if ( s.endsWith( ".xml" ) || s.endsWith( ".gml" ) || s.endsWith( ".prj" ) ) {
            // project file or gml document
            InputStream is = req.getInputStream();
            XMLFragment xml = new XMLFragment();
            xml.load( is, XMLFragment.DEFAULT_URL );

            FileOutputStream fos = new FileOutputStream( file );
            Properties props = new Properties();
            props.put( OutputKeys.ENCODING, "UTF-8" );
            xml.write( fos, props );
            fos.close();
        } else {
            // raster image
            FileOutputStream fos = new FileOutputStream( file );
            InputStream is = req.getInputStream();
            byte[] buffer = new byte[4096];
            int cnt = is.read( buffer );
            do {
                fos.write( buffer, 0, cnt );
            } while ( ( cnt = is.read( buffer ) ) > 0 );
            is.close();
            fos.close();
        }
    }

    /**
     * returns true if a file already exists
     * 
     * @param params
     * @param req
     * @param resp
     * @throws IOException
     */
    public void fileExists( Map<String, String> params, HttpServletRequest req, HttpServletResponse resp )
                            throws IOException {
        String s = Boolean.toString( createFileName( params ).exists() );
        sendMessage( s, resp );
    }

    private File createFileName( Map<String, String> params ) {
        String fileName = params.get( "FILE" );
        File file = null;
        if ( fileName.startsWith( "../" ) ) {
            file = new File( root + "/" + fileName.substring( 0 ) );
        } else {
            if ( params.get( "USER" ).length() == 0 ) {
                file = new File( root + "/" + fileName );
            } else {
                file = new File( root + "/" + params.get( "USER" ) + "/" + fileName );
            }
        }
        return file;
    }

}
