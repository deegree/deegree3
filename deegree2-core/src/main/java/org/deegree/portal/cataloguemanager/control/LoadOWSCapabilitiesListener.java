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
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpMethod;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilitiesDocument;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.wcs.CoverageOfferingBrief;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilities;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.owscommon_new.DCP;
import org.deegree.owscommon_new.HTTP;
import org.deegree.owscommon_new.Operation;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.deegree.portal.cataloguemanager.model.ServiceMetadataBean;
import org.xml.sax.SAXException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LoadOWSCapabilitiesListener extends AbstractMetadataListener {

    private static final ILogger LOG = LoggerFactory.getLogger( LoadOWSCapabilitiesListener.class );

    private HttpSession session;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        String url = (String) event.getParameter().get( "url" );
        String version = (String) event.getParameter().get( "version" );
        String type = (String) event.getParameter().get( "type" );
        String req = "request=GetCapabilities&service=" + type;
        if ( version != null && version.trim().length() > 0 ) {
            req += ( "&version=" + version );
        }
        LOG.logInfo( "URL: ", url );
        LOG.logInfo( "request: ", req );

        HttpMethod get = HttpUtils.performHttpGet( url, req, 60000, null, null, null );

        // check if correct content type
        String contentType = get.getResponseHeader( "Content-Type" ).getValue();
        if ( !contentType.contains( "xml" ) ) {
            String msg = "Error: Response Content is " + contentType + " not xml";
            LOG.logError( msg );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), msg );
            responseHandler.writeAndClose( true, bean );
            return;
        }

        session = event.getSession();
        InputStream is = get.getResponseBodyAsStream();
        try {
            if ( type.equals( "WMS" ) ) {
                handleWMS( is, responseHandler );
            } else if ( type.equals( "WFS" ) ) {
                handleWFS( is, responseHandler );
            } else if ( type.equals( "WCS" ) ) {
                handleWCS( is, responseHandler );
            } else if ( type.equals( "CSW" ) ) {
                handleCSW( is, responseHandler );
            } else {
                is.close();
                String msg = "unknow service type requested: " + url;
                LOG.logError( msg );
                ExceptionBean bean = new ExceptionBean( this.getClass().getName(), msg );
                responseHandler.writeAndClose( true, bean );
                return;
            }
        } catch ( Exception e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, bean );
        }

    }

    /**
     * @param is
     * @param responseHandler
     * @throws Exception
     */
    private void handleWMS( InputStream is, ResponseHandler responseHandler )
                            throws Exception {
        XMLFragment xml = new XMLFragment();
        loadCapabilities( is, responseHandler, xml );

        WMSCapabilitiesDocument doc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( xml.getRootElement() );
        WMSCapabilities capa = (WMSCapabilities) doc.parseCapabilities();
        session.setAttribute( OWSCAPABILITIES, capa );
        ServiceMetadataBean smb = new ServiceMetadataBean();
        smb.setVersion( capa.getVersion() );
        smb.setType( "WMS" );

        // find supported operations and dcp types
        List<Operation> list = capa.getOperationMetadata().getOperations();
        ServiceMetadataBean.Operation[] ops = new ServiceMetadataBean.Operation[list.size()];
        boolean glg = false;
        for ( int i = 0; i < list.size(); i++ ) {
            ops[i] = new ServiceMetadataBean.Operation();
            ops[i].setName( list.get( i ).getName().getLocalName() );
            if ( list.get( i ).getName().getLocalName().equals( "GetLegendGraphic" ) ) {
                glg = true;
            }
            List<DCP> dcps = list.get( i ).getDCP();
            for ( DCP dcp : dcps ) {
                if ( ( (HTTP) dcp ).getGetOnlineResources().size() > 0 ) {
                    ops[i].setDcp_get( ( (HTTP) dcp ).getGetOnlineResources().get( 0 ).toExternalForm() );
                }
                if ( ( (HTTP) dcp ).getPostOnlineResources().size() > 0 ) {
                    ops[i].setDcp_post( ( (HTTP) dcp ).getGetOnlineResources().get( 0 ).toExternalForm() );
                }
            }
        }
        smb.setOperations( ops );

        // find all accessible resources and operations that can be performed on them
        List<Layer> layers = new ArrayList<Layer>( 100 );
        Layer layer = capa.getLayer();
        collectLayers( layer, layers );
        ServiceMetadataBean.Resource[] resources = new ServiceMetadataBean.Resource[layers.size()];
        for ( int i = 0; i < resources.length; i++ ) {
            resources[i] = new ServiceMetadataBean.Resource();
            resources[i].setName( layers.get( i ).getName() );
            resources[i].setTitle( layers.get( i ).getTitle() );
            if ( layers.get( i ).isQueryable() && glg ) {
                resources[i].setOperations( new String[] { "GetMap", "GetFeatureInfo", "GetLegendGraphic" } );
            } else if ( layers.get( i ).isQueryable() && !glg ) {
                resources[i].setOperations( new String[] { "GetMap", "GetFeatureInfo" } );
            } else if ( !layers.get( i ).isQueryable() && glg ) {
                resources[i].setOperations( new String[] { "GetMap", "GetLegendGraphic" } );
            } else {
                resources[i].setOperations( new String[] { "GetMap" } );
            }

        }
        smb.setResources( resources );

        session.setAttribute( SERVICEMETADATABEAN, smb );

        writeResult( responseHandler, smb );
    }

    /**
     * @param layer
     * @param layers
     */
    private void collectLayers( Layer layer, List<Layer> layers ) {
        if ( layer.getName() != null ) {
            layers.add( layer );
        }
        Layer[] l = layer.getLayer();
        for ( int i = 0; i < l.length; i++ ) {
            collectLayers( l[i], layers );
        }
    }

    /**
     * @param is
     * @param responseHandler
     * @throws Exception
     */
    private void handleWFS( InputStream is, ResponseHandler responseHandler )
                            throws Exception {
        XMLFragment xml = new XMLFragment();
        loadCapabilities( is, responseHandler, xml );

        WFSCapabilitiesDocument doc = new WFSCapabilitiesDocument();
        doc.setRootElement( xml.getRootElement() );
        WFSCapabilities capa = (WFSCapabilities) doc.parseCapabilities();
        session.setAttribute( OWSCAPABILITIES, capa );
        ServiceMetadataBean smb = new ServiceMetadataBean();
        smb.setVersion( capa.getVersion() );
        smb.setType( "WFS" );

        // find supported operations and dcp types
        org.deegree.ogcwebservices.getcapabilities.Operation[] list = capa.getOperationsMetadata().getOperations();
        ServiceMetadataBean.Operation[] ops = new ServiceMetadataBean.Operation[list.length];
        boolean getGML = false;
        for ( int i = 0; i < list.length; i++ ) {
            ops[i] = new ServiceMetadataBean.Operation();
            ops[i].setName( list[i].getName() );
            if ( list[i].getName().equals( "GetGmlObject" ) ) {
                getGML = true;
            }
            DCPType[] dcps = list[i].getDCPs();
            for ( DCPType dcpType : dcps ) {
                URL[] urls = ( (org.deegree.ogcwebservices.getcapabilities.HTTP) dcpType.getProtocol() ).getGetOnlineResources();
                if ( urls != null && urls.length > 0 ) {
                    ops[i].setDcp_get( urls[0].toExternalForm() );
                }
                urls = ( (org.deegree.ogcwebservices.getcapabilities.HTTP) dcpType.getProtocol() ).getPostOnlineResources();
                if ( urls != null && urls.length > 0 ) {
                    ops[i].setDcp_post( urls[0].toExternalForm() );
                }
            }
        }
        smb.setOperations( ops );

        // find all accessible resources and operations that can be performed on them
        WFSFeatureType[] featureTypes = capa.getFeatureTypeList().getFeatureTypes();
        ServiceMetadataBean.Resource[] resources = new ServiceMetadataBean.Resource[featureTypes.length];
        for ( int i = 0; i < resources.length; i++ ) {
            resources[i] = new ServiceMetadataBean.Resource();
            resources[i].setName( featureTypes[i].getName().getLocalName() );
            resources[i].setNamespace( featureTypes[i].getName().getNamespace().toASCIIString() );
            resources[i].setTitle( featureTypes[i].getTitle() );
            org.deegree.ogcwebservices.wfs.capabilities.Operation[] ftops = featureTypes[i].getOperations();
            String[] tmp = null;
            if ( getGML && ftops != null && ftops.length > 0 ) {
                tmp = new String[] { "GetFeature", "DescribeFeatureType", "GetGMLObject", "Transaction" };
            } else if ( !getGML && ftops != null && ftops.length > 0 ) {
                tmp = new String[] { "GetFeature", "DescribeFeatureType", "Transaction" };
            } else if ( getGML && ( ftops == null || ftops.length == 0 ) ) {
                tmp = new String[] { "GetFeature", "DescribeFeatureType", "GetGMLObject" };
            } else {
                tmp = new String[] { "GetFeature", "DescribeFeatureType" };
            }
            resources[i].setOperations( tmp );
        }
        smb.setResources( resources );

        session.setAttribute( SERVICEMETADATABEAN, smb );

        writeResult( responseHandler, smb );
    }

    /**
     * @param is
     * @param responseHandler
     * @throws Exception
     */
    private void handleWCS( InputStream is, ResponseHandler responseHandler )
                            throws Exception {
        XMLFragment xml = new XMLFragment();
        loadCapabilities( is, responseHandler, xml );

        WCSCapabilitiesDocument doc = new WCSCapabilitiesDocument();
        doc.setRootElement( xml.getRootElement() );
        WCSCapabilities capa = (WCSCapabilities) doc.parseCapabilities();
        session.setAttribute( OWSCAPABILITIES, capa );
        ServiceMetadataBean smb = new ServiceMetadataBean();
        smb.setVersion( capa.getVersion() );
        smb.setType( "WCS" );

        // find supported operations and dcp types
        org.deegree.ogcwebservices.getcapabilities.Operation[] list = capa.getCapabilitiy().getOperations().getOperations();
        ServiceMetadataBean.Operation[] ops = new ServiceMetadataBean.Operation[list.length];
        for ( int i = 0; i < list.length; i++ ) {
            ops[i] = new ServiceMetadataBean.Operation();
            ops[i].setName( list[i].getName() );
            DCPType[] dcps = list[i].getDCPs();
            for ( DCPType dcpType : dcps ) {
                URL[] urls = ( (org.deegree.ogcwebservices.getcapabilities.HTTP) dcpType.getProtocol() ).getGetOnlineResources();
                if ( urls != null && urls.length > 0 ) {
                    ops[i].setDcp_get( urls[0].toExternalForm() );
                }
                urls = ( (org.deegree.ogcwebservices.getcapabilities.HTTP) dcpType.getProtocol() ).getPostOnlineResources();
                if ( urls != null && urls.length > 0 ) {
                    ops[i].setDcp_post( urls[0].toExternalForm() );
                }
            }
        }
        smb.setOperations( ops );

        // find all accessible resources and operations that can be performed on them
        CoverageOfferingBrief[] coverages = capa.getContentMetadata().getCoverageOfferingBrief();
        ServiceMetadataBean.Resource[] resources = new ServiceMetadataBean.Resource[coverages.length];
        String[] op = new String[] { "GetCoverage, DescribeCoverage" };
        for ( int i = 0; i < resources.length; i++ ) {
            resources[i] = new ServiceMetadataBean.Resource();
            resources[i].setName( coverages[i].getName() );
            resources[i].setTitle( coverages[i].getLabel() );
            resources[i].setOperations( op );
        }
        smb.setResources( resources );

        session.setAttribute( SERVICEMETADATABEAN, smb );

        writeResult( responseHandler, smb );
    }

    /**
     * @param is
     * @param responseHandler
     * @throws Exception
     */
    private void handleCSW( InputStream is, ResponseHandler responseHandler )
                            throws Exception {
        XMLFragment xml = new XMLFragment();
        loadCapabilities( is, responseHandler, xml );

        CatalogueCapabilitiesDocument doc = new CatalogueCapabilitiesDocument();
        doc.setRootElement( xml.getRootElement() );
        CatalogueCapabilities capa = (CatalogueCapabilities) doc.parseCapabilities();
        session.setAttribute( "OWSCAPABILITIES", capa );
        ServiceMetadataBean smb = new ServiceMetadataBean();
        smb.setVersion( capa.getVersion() );
        smb.setType( "CSW" );

        org.deegree.ogcwebservices.getcapabilities.Operation[] list = capa.getOperationsMetadata().getOperations();
        ServiceMetadataBean.Operation[] ops = new ServiceMetadataBean.Operation[list.length];
        for ( int i = 0; i < list.length; i++ ) {
            ops[i] = new ServiceMetadataBean.Operation();
            ops[i].setName( list[i].getName() );
            DCPType[] dcps = list[i].getDCPs();
            for ( DCPType dcpType : dcps ) {
                URL[] urls = ( (org.deegree.ogcwebservices.getcapabilities.HTTP) dcpType.getProtocol() ).getGetOnlineResources();
                if ( urls != null && urls.length > 0 ) {
                    ops[i].setDcp_get( urls[0].toExternalForm() );
                }
                urls = ( (org.deegree.ogcwebservices.getcapabilities.HTTP) dcpType.getProtocol() ).getPostOnlineResources();
                if ( urls != null && urls.length > 0 ) {
                    ops[i].setDcp_post( urls[0].toExternalForm() );
                }
            }
        }
        smb.setResources( new ServiceMetadataBean.Resource[0] );

        session.setAttribute( SERVICEMETADATABEAN, smb );

        writeResult( responseHandler, smb );

    }

    private void loadCapabilities( InputStream is, ResponseHandler responseHandler, XMLFragment xml )
                            throws SAXException, IOException {
        xml.load( is, XMLFragment.DEFAULT_URL );

        if ( xml.getRootElement().getLocalName().toLowerCase().indexOf( "exception" ) > -1 ) {
            String msg = xml.getAsPrettyString();
            LOG.logError( msg );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), msg );
            responseHandler.writeAndClose( true, bean );
            return;
        }
    }

    private void writeResult( ResponseHandler responseHandler, ServiceMetadataBean smb )
                            throws IOException {
        String charEnc = Charset.defaultCharset().displayName();
        responseHandler.setContentType( "application/json; " + charEnc );
        responseHandler.writeAndClose( false, smb );
    }

}
