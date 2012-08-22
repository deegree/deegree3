// $HeadURL: /cvsroot/deegree/src/org/deegree/ogcwebservices/wms/WMPService.java,v
// 1.3 2004/07/12 06:12:11 ap Exp $
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
package org.deegree.ogcwebservices.wmps;

import java.awt.Dimension;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.deegree.enterprise.Proxy;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.TriggerProvider;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.CurrentUpdateSequenceException;
import org.deegree.ogcwebservices.InvalidUpdateSequenceException;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.wmps.configuration.WMPSConfiguration;
import org.deegree.ogcwebservices.wmps.configuration.WMPSDeegreeParams;
import org.deegree.ogcwebservices.wmps.operation.DescribeTemplate;
import org.deegree.ogcwebservices.wmps.operation.DescribeTemplateResponse;
import org.deegree.ogcwebservices.wmps.operation.GetAvailableTemplates;
import org.deegree.ogcwebservices.wmps.operation.GetAvailableTemplatesResponse;
import org.deegree.ogcwebservices.wmps.operation.PrintMap;
import org.deegree.ogcwebservices.wmps.operation.WMPSGetCapabilities;
import org.deegree.ogcwebservices.wmps.operation.WMPSGetCapabilitiesResult;
import org.deegree.ogcwebservices.wmps.operation.WMPSProtocolFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handles saving the PrintMap request to the databank and generating the initial response to be sent to the user.
 * 
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @version 2.0
 */
public class WMPService implements OGCWebService {

    private static final ILogger LOG = LoggerFactory.getLogger( WMPService.class );

    private static final TriggerProvider TP = TriggerProvider.create( WMPService.class );

    private PrintMapHandler printMapHandler;

    private WMPSConfiguration configuration;

    /**
     * Creates a new WMPService object.
     * 
     * @param configuration
     */
    public WMPService( WMPSConfiguration configuration ) {
        this.configuration = configuration;
        this.printMapHandler = new PrintMapHandler( configuration );
        Proxy proxy = this.configuration.getDeegreeParams().getProxy();
        if ( proxy != null ) {
            proxy.setProxy( true );
        }
    }

    /**
     * Return the OGCCapabilities.
     * 
     * @return OGCCapabilities
     */
    public OGCCapabilities getCapabilities() {
        return this.configuration;
    }

    /**
     * the method performs the handling of the passed OGCWebServiceEvent directly and returns the result to the calling
     * class/method
     * 
     * @param request
     *            request to perform
     * @return Object
     * @throws OGCWebServiceException
     */
    public Object doService( OGCWebServiceRequest request )
                            throws OGCWebServiceException {

        request = (OGCWebServiceRequest) TP.doPreTrigger( this, request )[0];

        Object result = null;
        if ( request instanceof PrintMap ) {
            result = handlePrintMap( request );
        } else if ( request instanceof GetAvailableTemplates ) {
            result = handleGetAvailableTemplates();
        } else if ( request instanceof WMPSGetCapabilities ) {
            result = handleGetCapabilities( (WMPSGetCapabilities) request );
        } else if ( request instanceof DescribeTemplate ) {
            result = handleDescribeTemplate( (DescribeTemplate) request );
        }

        return TP.doPostTrigger( this, result )[0];
    }

    /**
     * @param request
     * @return
     * @throws OGCWebServiceException
     */
    private Object handleDescribeTemplate( DescribeTemplate request )
                            throws OGCWebServiceException {

        String template = request.getTemplate();
        String dir = configuration.getDeegreeParams().getPrintMapParam().getTemplateDirectory();
        dir = dir.substring( 5 );
        DescribeTemplateResponse response = null;
        try {
            File file = new File( dir + "/" + template + ".jrxml" );
            XMLFragment doc = new XMLFragment( file );
            NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
            nsc.addNamespace( "jasper", URI.create( "http://jasperreports.sourceforge.net/jasperreports" ) );
            // old jasper format
            List<Element> tmp = XMLTools.getElements( doc.getRootElement(), "//parameter", null );
            // current jasper format
            List<Element> params = XMLTools.getElements( doc.getRootElement(), "//jasper:parameter", nsc );
            for ( Element element : tmp ) {
                params.add( element );
            }
            List<Pair<String, String>> parameter = new ArrayList<Pair<String, String>>();
            for ( Element element : params ) {
                String name = element.getAttribute( "name" );
                String type = element.getAttribute( "class" );
                if ( name.equals( "MAP" ) ) {
                    Dimension dim = parseImageNodes( doc.getRootElement() );
                    parameter.add( new Pair<String, String>( "$MAPWIDTH", Integer.toString( dim.width ) ) );
                    parameter.add( new Pair<String, String>( "$MAPHEIGHT", Integer.toString( dim.height ) ) );
                    continue;
                } else if (name.equals( "LEGEND" ) ) {
                    continue;
                }
                parameter.add( new Pair<String, String>( name, type ) );
            }            
            response = new DescribeTemplateResponse( parameter );
        } catch ( Exception e ) {
            throw new OGCWebServiceException( WMPService.class.getName(),
                                              "can not perform DescribeTemplate operation for template: " + template
                                                                      + "\n" + e.getMessage() );
        }
        try {
            return XMLFactory.export( response );
        } catch ( Exception e ) {
            throw new OGCWebServiceException( "Error handling the GetAvailableTemplates request. " + e.getMessage() );
        }
    }

    /**
     * 
     * @param root
     * @return map dimension in template
     * @throws PrintMapServiceException
     */
    private Dimension parseImageNodes( Element root )
                            throws PrintMapServiceException {

        Dimension dimension = null;
        try {
            NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
            nsc.addNamespace( "jasper", URI.create( "http://jasperreports.sourceforge.net/jasperreports" ) );
            List<Node> images = XMLTools.getNodes( root, "detail/band/image", null );
            if ( images == null || images.size() == 0 ) {
                images = XMLTools.getNodes( root, "jasper:detail/jasper:band/jasper:image", nsc );
            }
            for ( int i = 0; i < images.size(); i++ ) {
                Node image = images.get( i );
                // e.g. $P{MAP}
                String value = XMLTools.getNodeAsString( image, "imageExpression", null, null );
                if ( value == null ) {
                    value = XMLTools.getRequiredNodeAsString( image, "jasper:imageExpression", nsc );
                }
                int idx = value.indexOf( "{" );
                if ( idx != -1 ) {

                    String tmp = value.substring( idx + 1, value.length() - 1 );
                    Element reportElement = (Element) XMLTools.getNode( image, "reportElement", null );
                    if ( reportElement == null ) {
                        reportElement = (Element) XMLTools.getRequiredNode( image, "jasper:reportElement", nsc );
                    }

                    // Templates created by iReport assumes a resolution of 72 dpi
                    if ( tmp.startsWith( "MAP" ) ) {
                        String width = reportElement.getAttribute( "width" );
                        String height = reportElement.getAttribute( "height" );
                        dimension = new Dimension( Integer.parseInt( width ), Integer.parseInt( height ) );
                    }
                }
            }
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PrintMapServiceException( Messages.getMessage( "WMPS_INVALID_JASPER_TEMPLATE" ) );
        }

        return dimension;
    }

    /**
     * @return
     * @throws OGCWebServiceException
     */
    private Object handleGetAvailableTemplates()
                            throws OGCWebServiceException {
        String templateDir = configuration.getDeegreeParams().getPrintMapParam().getTemplateDirectory();
        templateDir = templateDir.substring( 5 );
        File dir = new File( templateDir );
        File[] files = dir.listFiles( new FileFilter() {
            public boolean accept( File pathname ) {
                String s = pathname.getName().toLowerCase();
                return s.endsWith( ".xml" ) || s.endsWith( ".jrxml" );
            }
        } );

        List<String> list = new ArrayList<String>();
        for ( File file : files ) {
            String s = file.getName();
            int pos = s.lastIndexOf( '.' );
            list.add( s.substring( 0, pos ) );
        }
        GetAvailableTemplatesResponse resp = new GetAvailableTemplatesResponse( list );
        try {
            return XMLFactory.export( resp );
        } catch ( Exception e ) {
            throw new OGCWebServiceException( "Error handling the GetAvailableTemplates request. " + e.getMessage() );
        }
    }

    private Object handlePrintMap( OGCWebServiceRequest request )
                            throws OGCWebServiceException {
        Object result;
        String template = ( (PrintMap) request ).getTemplate();
        WMPSDeegreeParams params = this.configuration.getDeegreeParams();
        boolean isSynchronous = params.getSynchronousTemplates().contains( template );

        String handler = HandlerMapping.getString( "WMPService.PRINTMAP" );
        RequestManager rqManager = (RequestManager) createHandler( request, PrintMap.class, handler );

        try {
            rqManager.saveRequestToDB();

            result = rqManager.createInitialResponse( Messages.getMessage( "WMPS_INIT_RESPONSE" ) );
        } catch ( OGCWebServiceException e ) {

            throw new OGCWebServiceException( "Error saving the PrintMap request " + "to the DB. " + e.getMessage() );
        }

        try {
            if ( isSynchronous ) {
                result = this.printMapHandler.runSynchronous( (PrintMap) request );
            } else {
                Thread printMap = new Thread( this.printMapHandler );
                printMap.start();
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( "Error performing the PrintMap request. " + e.getMessage() );
        }
        return result;
    }

    /**
     * creates a handler class for performing the incomming request. The instance that will be created depends on the
     * responsible class the for the submitted request in the WMPS capabilities/configuration.
     * 
     * @param request
     *            request to be performed
     * @param requestClass
     *            of the request (GetStyles, WMSFeatureInfoRequest etc.)
     * @param className
     *            type of the operation to perform by the handler
     * @return Object
     * @throws OGCWebServiceException
     */
    private Object createHandler( OGCWebServiceRequest request, Class<?> requestClass, String className )
                            throws OGCWebServiceException {

        // describes the signature of the required constructor
        Class<?>[] cl = new Class[2];
        cl[0] = WMPSConfiguration.class;
        cl[1] = requestClass;

        // set parameter to submitt to the constructor
        Object[] o = new Object[2];
        o[0] = this.configuration;
        o[1] = request;

        Object handler = null;

        try {
            // get constructor
            Class<?> creator = Class.forName( className );
            Constructor<?> con = creator.getConstructor( cl );
            // call constructor and instantiate a new DataStore
            handler = con.newInstance( o );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( "Couldn't instantiate " + className + '!' );
        }

        return handler;
    }

    /**
     * reads/creates the capabilities of the WMPS.
     * 
     * @param request
     *            capabilities request
     * @return WMPSGetCapabilitiesResult
     * @throws CurrentUpdateSequenceException
     * @throws InvalidUpdateSequenceException
     */
    private WMPSGetCapabilitiesResult handleGetCapabilities( WMPSGetCapabilities request )
                            throws CurrentUpdateSequenceException, InvalidUpdateSequenceException {

        String rUp = request.getUpdateSequence();
        String cUp = this.configuration.getUpdateSequence();

        if ( ( rUp != null ) && ( cUp != null ) && ( rUp.compareTo( cUp ) == 0 ) ) {
            throw new CurrentUpdateSequenceException( "request update sequence: " + rUp + "is equal to capabilities"
                                                      + " update sequence " + cUp );
        }

        if ( ( rUp != null ) && ( cUp != null ) && ( rUp.compareTo( cUp ) > 0 ) ) {
            throw new InvalidUpdateSequenceException( "request update sequence: " + rUp + " is higher then the "
                                                      + "capabilities update sequence " + cUp );
        }
        return WMPSProtocolFactory.createGetCapabilitiesResult( request, null, this.configuration );
    }

}
