//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/test/owsproxy/OWSProxyServlet.java $
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
package org.deegree.test.owsproxy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.ImageUtils;
import org.deegree.ogcwebservices.OGCRequestFactory;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.discovery.GetRecordById;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.csw.discovery.GetRepositoryItem;
import org.deegree.ogcwebservices.csw.manager.Harvest;
import org.deegree.ogcwebservices.getcapabilities.GetCapabilities;
import org.deegree.ogcwebservices.wcs.describecoverage.DescribeCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureType;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetMap;

/**
 * dummy servlet for testing oswProxy
 *
 *
 * @version $Revision: 18195 $
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version 1.0. $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */
public class OWSProxyServlet extends HttpServlet {
    private static ILogger LOG = LoggerFactory.getLogger( OWSProxyServlet.class );

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

        try {
            OGCWebServiceRequest req = OGCRequestFactory.create( request );
            String service = req.getServiceName();
            if ( req instanceof GetCapabilities && "WMS".equals( service ) ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "WMSGetCapabilities" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof GetCapabilities && "WFS".equals( service ) ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "WFSGetCapabilities" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof GetCapabilities && "WCS".equals( service ) ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "WCSGetCapabilities" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof GetCapabilities && "CSW".equals( service ) ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "CSWGetCapabilities" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof GetMap ) {
                String path = getInitParameter( "GetMap" );
                BufferedImage result = ImageUtils.loadImage( getFile( path ) );
                OutputStream os = response.getOutputStream();
                response.setContentType( "image/png" );
                ImageUtils.saveImage( result, os, "png", 1 );
                os.close();
            } else if ( req instanceof GetFeatureInfo ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "GetFeatureInfo" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof GetLegendGraphic ) {
                String path = getInitParameter( "GetLegendGraphic" );
                BufferedImage result = ImageUtils.loadImage( getFile( path ) );
                OutputStream os = response.getOutputStream();
                response.setContentType( "image/png" );
                ImageUtils.saveImage( result, os, "image/png", 1 );
                os.close();
            } else if ( req instanceof GetFeature ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "GetFeature" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof DescribeFeatureType ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "DescribeFeatureType" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof Transaction ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "WFSTransaction" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof GetCoverage ) {
                String path = getInitParameter( "GetCoverage" );
                BufferedImage result = ImageUtils.loadImage( getFile( path ) );
                OutputStream os = response.getOutputStream();
                response.setContentType( "image/png" );
                ImageUtils.saveImage( result, os, "image/png", 1 );
                os.close();
            } else if ( req instanceof DescribeCoverage ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "DescribeCoverage" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof GetRecords ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "GetRecords" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof GetRecordById ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "GetRecordById" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof GetRepositoryItem ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "GetRepositoryItem" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof Harvest ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "Harvest" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else if ( req instanceof org.deegree.ogcwebservices.csw.manager.Transaction ) {
                PrintWriter pw = response.getWriter();
                String path = getInitParameter( "CSWTransaction" );
                String result = FileUtils.readTextFile( getFile( path ) ).toString();
                pw.write( result );
                pw.close();
            } else {
                PrintWriter pw = response.getWriter();
                String result = "<error>unknown request/service:" + req + "</error>";
                pw.write( result );
                pw.close();
            }
        } catch ( OGCWebServiceException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            PrintWriter pw = response.getWriter();
            String result = "<error>" + e.getMessage() + "</error>";
            pw.write( result );
            pw.close();
        }

    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
                                                                                     IOException {
        doGet( request, response );
    }

    private File getFile( String path ) {
        File file = new File( path );
        if ( !file.isAbsolute() ) {
            file = new File( getServletContext().getRealPath( path ) );
        }
        return file;
    }

}
