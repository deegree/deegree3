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

package org.deegree.portal.standard.context.control;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.mail.EMailMessage;
import org.deegree.framework.mail.MailHelper;
import org.deegree.framework.mail.MailMessage;
import org.deegree.framework.mail.SendMailException;
import org.deegree.framework.util.FeatureUtils;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.ZipUtils;
import org.deegree.framework.xml.Marshallable;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.BaseURL;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSOperationsMetadata;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureDocument;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.deegree.owscommon.OWSDomainType;
import org.deegree.portal.Constants;
import org.deegree.portal.PortalException;
import org.deegree.portal.context.GeneralExtension;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.LayerExtension;
import org.deegree.portal.context.ViewContext;
import org.xml.sax.SAXException;

/**
 * This Listener is used when a user likes to download the WFS data behind a WMS layer.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$ $Date$
 */
public class DownloadListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( DownloadListener.class );

    private static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private String mailAddr = "";

    private String downloadDir = "";

    private URL downloadURL = null;

    /**
     * default is false. A value may be provided as TEST_MAX_HITS in initParameters of controller.xml
     */
    private boolean isMaxFeaturesTestEnabled = false;

    /**
     * default value for maximum number of features that shall be retreived from a WFS, as provided in initParameters of
     * controller.xml
     */
    private int defaultMaxFeatures = 0;

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.enterprise.control.AbstractListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        // set init parameter as provided in controller.xml
        if ( Integer.getInteger( getInitParameter( "DEFAULT_MAX_FEATURES" ) ) != null ) {
            defaultMaxFeatures = Integer.getInteger( getInitParameter( "DEFAULT_MAX_FEATURES" ) ).intValue();
        }
        isMaxFeaturesTestEnabled = "true".equalsIgnoreCase( getInitParameter( "TEST_MAX_HITS" ) );

        RPCWebEvent rpc = (RPCWebEvent) event;
        try {
            validate( rpc );
        } catch ( PortalException e ) {
            gotoErrorPage( Messages.get( "IGEO_STD_CNTXT_INVALID_RPC", "download", e.getMessage() ) );
            return;
        }

        try {
            // get users email address
            mailAddr = getUserEmail( rpc );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( e.getMessage() );
            return;
        }
        if ( mailAddr == null ) {
            gotoErrorPage( "Unknown user and/or email address. Download not possible." );
            return;
        }

        // read base context
        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );

        downloadDir = ( (GeneralExtension) vc.getGeneral().getExtension() ).getIOSettings().getDownloadDirectory().getDirectoryName();
        downloadURL = ( (GeneralExtension) vc.getGeneral().getExtension() ).getIOSettings().getDownloadDirectory().getOnlineResource();

        ServletContext sc = session.getServletContext();
        sc.setAttribute( Constants.DOWNLOADDIR, downloadDir );

        String msg = "";
        if ( !msg.equals( "" ) ) {
            // user doesn't have the authorization to download the ordered datasets
            this.setNextPage( this.getAlternativeNextPage() );
            this.getRequest().setAttribute( Constants.MESSAGE, msg );
        } else {
            try {
                ArrayList<RequestBean> gfrl = new ArrayList<RequestBean>();
                ArrayList<FeatureTemplate> fids = createFeatureTemplates( rpc );
                Iterator<FeatureTemplate> iterator = fids.iterator();

                // create a GetFeature request for each ordered dataset
                while ( iterator.hasNext() ) {
                    FeatureTemplate ft = iterator.next();
                    RequestBean gfRequest = getWFSGetFeatureCalls( ft, vc );

                    if ( gfRequest == null ) {
                        StringBuffer s = new StringBuffer( 500 );
                        try {
                            s.append( Messages.get( "IGEO_STD_CNTXT_MISSING_DATA" ) );
                            s.append( "<BR><BR>" );
                        } catch ( Exception e ) {
                            LOG.logError( e.getMessage(), e );
                        }
                        s.append( "<b>" ).append( ft.getTitle() ).append( "</b>" );
                        throw new Exception( s.toString() );
                    } else {
                        gfrl.add( gfRequest );
                    }
                }

                // perform dataloading in another thread
                RPCStruct struct = (RPCStruct) rpc.getRPCMethodCall().getParameters()[0].getValue();
                String format = (String) struct.getMember( "format" ).getValue();
                LoadController cntr = new LoadController( gfrl, format );
                cntr.start();
                String s = Messages.get( "IGEO_STD_CNTXT_INFO_EMAIL_CONFIRM", mailAddr );
                this.setNextPage( "message.jsp" );
                getRequest().setAttribute( Constants.MESSAGE, s );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                this.getRequest().setAttribute( Constants.MESSAGE, e.toString() );
                gotoErrorPage( e.toString() );
            }
        }
    }

    private String getUserEmail( RPCWebEvent rpc )
                            throws Exception {
        String email = null;

        RPCStruct struct = (RPCStruct) rpc.getRPCMethodCall().getParameters()[0].getValue();
        String sessionId = (String) struct.getMember( "sessionID" ).getValue(); // this is the userName
        String rpcMail = (String) struct.getMember( "email" ).getValue();

        if ( ( sessionId == null || "null".equals( sessionId ) ) && ( rpcMail == null || "".equals( rpcMail ) ) ) {
            // unknown session AND unknown email address.
            // -> no way to find out where to send the download-info.
            LOG.logDebug( "Neither sessionID nor email was set in RPC params" );
            return null;
        }

        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        if ( vc == null ) {
            return null;
        }
        GeneralExtension ge = vc.getGeneral().getExtension();

        if ( sessionId != null && !"null".equals( sessionId ) && ge.getAuthentificationSettings() != null ) {
            LOG.logDebug( "try getting user information (email address) from WAS/sessionID" );
            BaseURL baseUrl = ge.getAuthentificationSettings().getAuthentificationURL();
            String url = OWSUtils.validateHTTPGetBaseURL( baseUrl.getOnlineResource().toExternalForm() );
            StringBuffer sb = new StringBuffer( url );
            sb.append( "request=DescribeUser&SESSIONID=" ).append( sessionId );

            XMLFragment xml = new XMLFragment();
            xml.load( new URL( sb.toString() ) );

            email = XMLTools.getRequiredNodeAsString( xml.getRootElement(), "/User/EMailAddress", nsContext );
        }
        if ( email == null || ( "" ).equals( email ) ) {
            LOG.logDebug( "Taking email address from rpc request!" );
            email = rpcMail;
        }

        LOG.logDebug( "Email: " + email );
        return email;
    }

    /**
     * gets the user name assigned to the passed session ID from a authentification service. If no user is assigned to
     * the session ID <tt>null</tt> will be returned. If the session is closed or expired an exception will be thrown
     * 
     * @param sessionId
     * @return name of the user assigned to the passed session ID
     */
    protected String getUserName( String sessionId )
                            throws XMLParsingException, IOException, SAXException {

        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        if ( vc == null ) {
            return null;
        }
        GeneralExtension ge = vc.getGeneral().getExtension();
        String userName = null;
        if ( sessionId != null && ge.getAuthentificationSettings() != null ) {
            LOG.logDebug( "try getting user from WAS/sessionID" );
            BaseURL baseUrl = ge.getAuthentificationSettings().getAuthentificationURL();
            String url = OWSUtils.validateHTTPGetBaseURL( baseUrl.getOnlineResource().toExternalForm() );
            StringBuffer sb = new StringBuffer( url );
            sb.append( "request=DescribeUser&SESSIONID=" ).append( sessionId );

            XMLFragment xml = new XMLFragment();
            xml.load( new URL( sb.toString() ) );

            userName = XMLTools.getRequiredNodeAsString( xml.getRootElement(), "/User/UserName", nsContext );
        } else {
            LOG.logDebug( "try getting user from getUserPrincipal()" );
            if ( ( (HttpServletRequest) getRequest() ).getUserPrincipal() != null ) {
                userName = ( (HttpServletRequest) getRequest() ).getUserPrincipal().getName();
                if ( userName.indexOf( "\\" ) > 1 ) {
                    String[] us = StringTools.toArray( userName, "\\", false );
                    userName = us[us.length - 1];
                }
            }
        }
        LOG.logDebug( "userName: " + userName );
        return userName;
    }

    /**
     * Convenience method to extract the boundig box from an rpc fragment.
     * 
     * @param bboxStruct
     *            the <code>RPCStruct</code> containing the bounding box. For example,
     *            <code>&lt;member&gt;&lt;name&gt;boundingBox&lt;/name&gt;etc...</code>.
     * 
     * @return an envelope with the boundaries defined in the rpc structure
     */
    protected Envelope extractBBox( RPCStruct bboxStruct ) {

        // read base context
        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        Double minx = (Double) bboxStruct.getMember( Constants.RPC_BBOXMINX ).getValue();
        Double miny = (Double) bboxStruct.getMember( Constants.RPC_BBOXMINY ).getValue();
        Double maxx = (Double) bboxStruct.getMember( Constants.RPC_BBOXMAXX ).getValue();
        Double maxy = (Double) bboxStruct.getMember( Constants.RPC_BBOXMAXY ).getValue();

        Envelope bbox = GeometryFactory.createEnvelope( minx.doubleValue(), miny.doubleValue(), maxx.doubleValue(),
                                                        maxy.doubleValue(),
                                                        vc.getGeneral().getBoundingBox()[0].getCoordinateSystem() );
        return bbox;
    }

    /**
     * validates the request to be performed
     */
    private void validate( RPCWebEvent rpc )
                            throws PortalException {

        RPCMethodCall mc = rpc.getRPCMethodCall();
        RPCParameter param = mc.getParameters()[0];
        RPCStruct struct = (RPCStruct) param.getValue();

        RPCMember layerList = struct.getMember( "layerList" );
        RPCMember sessionID = struct.getMember( "sessionID" );
        RPCMember email = struct.getMember( "email" );
        RPCMember format = struct.getMember( "format" );
        RPCMember boundingBox = struct.getMember( "boundingBox" );

        if ( layerList == null ) {
            throw new PortalException( Messages.get( "IGEO_STD_CNTXT_MISSING_PARAM", "'layerList'", "Download" ) );
        }
        if ( sessionID == null ) {
            throw new PortalException( Messages.get( "IGEO_STD_CNTXT_MISSING_PARAM", "'sessionID'", "Download" ) );
        }
        if ( email == null ) {
            // has been added to RPC in deegree 2.4
            throw new PortalException( Messages.get( "IGEO_STD_CNTXT_MISSING_PARAM", "'email'", "Download" ) );
        }
        if ( format == null ) {
            // has been added to RPC in deegree 2.4
            throw new PortalException( Messages.get( "IGEO_STD_CNTXT_MISSING_PARAM", "'format'", "Download" ) );
        }
        if ( boundingBox == null ) {
            throw new PortalException( Messages.get( "IGEO_STD_CNTXT_MISSING_PARAM", "'boundingBox'", "Download" ) );
        }

        // DEBUG STATEMENTS
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            RPCStruct layerStruct = (RPCStruct) layerList.getValue();
            RPCMember[] layers = layerStruct.getMembers();
            for ( int i = 0; i < layers.length; i++ ) {
                // key = "layername,layertitle" , value = "WMS-Service-Url"
                String tmp = layers[i].getName();
                String layerName = tmp.substring( 0, tmp.indexOf( "," ) );
                String layerTitle = tmp.substring( tmp.indexOf( "," ) + 1 );
                String layerServiceURL = (String) layers[i].getValue();
                LOG.logDebug( "layername: ", layerName, "  layertitle: ", layerTitle, "  url: ", layerServiceURL );
            }
            LOG.logDebug( "sessionID=" + (String) sessionID.getValue() );
            LOG.logDebug( "email=" + (String) email.getValue() );
            LOG.logDebug( "format=" + (String) format.getValue() );
            RPCStruct bboxStruct = (RPCStruct) boundingBox.getValue();
            LOG.logDebug( "bbox minx=" + (Double) bboxStruct.getMember( Constants.RPC_BBOXMINX ).getValue() );
            LOG.logDebug( "bbox miny=" + (Double) bboxStruct.getMember( Constants.RPC_BBOXMINY ).getValue() );
            LOG.logDebug( "bbox maxx=" + (Double) bboxStruct.getMember( Constants.RPC_BBOXMAXX ).getValue() );
            LOG.logDebug( "bbox maxy=" + (Double) bboxStruct.getMember( Constants.RPC_BBOXMAXY ).getValue() );
        }
    }

    /**
     * performs the access to the data marked at the shopping card
     */
    protected ArrayList<FeatureTemplate> createFeatureTemplates( RPCWebEvent event )
                            throws PortalException, Exception {

        RPCParameter[] params = event.getRPCMethodCall().getParameters();
        RPCStruct struct = (RPCStruct) params[0].getValue();
        RPCStruct layerStruct = (RPCStruct) struct.getMember( "layerList" ).getValue();

        RPCStruct bboxStruct = (RPCStruct) struct.getMember( Constants.RPC_BBOX ).getValue();
        Envelope bbox = extractBBox( bboxStruct );

        RPCMember[] layers = layerStruct.getMembers();

        ArrayList<FeatureTemplate> fids = new ArrayList<FeatureTemplate>( layers.length );
        for ( int i = 0; i < layers.length; i++ ) {
            // key = "layername,layertitle" , value = "WMS-Service-Url"
            String tmp = layers[i].getName();
            String layerName = tmp.substring( 0, tmp.indexOf( "," ) ); // assuming there is no "," within the layername
            String layerTitle = tmp.substring( tmp.indexOf( "," ) + 1 );
            String layerServiceURL = (String) layers[i].getValue();
            FeatureTemplate ft = new FeatureTemplate( layerName, layerTitle, bbox, layerServiceURL );
            fids.add( i, ft );
        }
        LOG.logDebug( "created FeatureTemplate.length=", fids.size() );
        return fids;
    }

    /**
     * returns the call to be used to perform a GetFeature request for the passed feature template.
     * 
     * @param layer
     *            used
     * @param vc
     * @return the call to be used to perform a GetFeature request for the passed feature template
     * @throws PortalException
     */
    protected RequestBean getWFSGetFeatureCalls( FeatureTemplate layer, ViewContext vc )
                            throws PortalException {

        String href = null;
        String version = null;
        RequestBean result = null;

        try {
            Layer contxtLayer = vc.getLayerList().getLayer( layer.getId(), layer.getServerURL() );
            LayerExtension lExt = null;
            try {
                lExt = (LayerExtension) contxtLayer.getExtension();
            } catch ( ClassCastException e ) {
                throw new PortalException( e.getMessage() );
            }

            href = lExt.getDataService().getServer().getOnlineResource().toString();
            version = lExt.getDataService().getServer().getVersion();

            // feature from layer extension
            String featureType = lExt.getDataService().getFeatureType();
            String nsFeature = featureType.substring( featureType.indexOf( '{' ) + 1, featureType.indexOf( '}' ) );
            String featureLocalName = featureType.substring( featureType.lastIndexOf( ':' ) + 1 );

            // geometry from layer extension
            String geometryType = lExt.getDataService().getGeometryType();
            String nsGeometry = geometryType.substring( geometryType.indexOf( '{' ) + 1, geometryType.indexOf( '}' ) );
            String geometryLocalName = geometryType.substring( geometryType.lastIndexOf( ':' ) + 1 );

            if ( href != null ) {
                // init RequestBean values
                GetFeature gfrHits = null;
                GetFeature gfr = null;
                int maxRequest = defaultMaxFeatures;

                if ( isMaxFeaturesTestEnabled ) {
                    // get the DefaultMaxFeatures from the capabilities of the wfs.
                    if ( !( lExt.getDataService().getServer().getCapabilities() instanceof WFSCapabilities ) ) {
                        LOG.logDebug( "very strange, should be a wfs capa." );
                        // FIXME do something intelligent
                    } else {
                        WFSCapabilities cap = ( (WFSCapabilities) lExt.getDataService().getServer().getCapabilities() );
                        WFSOperationsMetadata opm = ( (WFSOperationsMetadata) cap.getOperationsMetadata() );
                        OWSDomainType[] constraints = opm.getConstraints();
                        for ( OWSDomainType cons : constraints ) {
                            if ( "DefaultMaxFeatures".equals( cons.getName() ) ) {
                                String[] values = cons.getValues();
                                if ( values != null ) {
                                    // take the freaking first.
                                    try {
                                        maxRequest = Integer.parseInt( values[0] );
                                    } catch ( NumberFormatException e ) {
                                        // do nothing
                                    }
                                }
                            }
                        }
                    }
                    // RESULT_TYPE = HITS
                    gfrHits = createGetFeatureRequest( layer, version, new QualifiedName( "app", featureLocalName,
                                                                                          new URI( nsFeature ) ),
                                                       new QualifiedName( "app", geometryLocalName,
                                                                          new URI( nsGeometry ) ), RESULT_TYPE.HITS );
                }
                // RESULT_TYPE = RESULTS
                gfr = createGetFeatureRequest( layer, version, new QualifiedName( "app", featureLocalName,
                                                                                  new URI( nsFeature ) ),
                                               new QualifiedName( "app", geometryLocalName, new URI( nsGeometry ) ),
                                               RESULT_TYPE.RESULTS );

                result = new RequestBean( href, gfrHits, gfr, maxRequest, layer.getId(), layer.getTitle() );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( e.getMessage(), e );
        }
        return result;
    }

    /**
     * creates a GetFeature request considering the feature type (ID) and the bounding box encapsulated in the passed
     * <tt>FeatureTemplate</tt>
     * 
     * @param ft
     *            FeatureTemplate
     * @param ftName
     *            a Qualified Name representing the feature type name of the requested feature, ex: app:ZipCodes
     * @param gtName
     *            a Qualified Name representing the geometry type name of the requested feature, ex: app:geometry
     * @param resultType
     *            the type of result (GetFeature.RESULT_TYPE.HITS or GetFeature.RESULT_TYPE.RESULTS)
     * @return {@link GetFeature} request to access data for download
     * @throws PortalException
     */
    protected GetFeature createGetFeatureRequest( FeatureTemplate ft, String version, QualifiedName ftName,
                                                  QualifiedName gtName, GetFeature.RESULT_TYPE resultType )
                            throws PortalException {

        // read base context
        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        String srcName = vc.getGeneral().getBoundingBox()[0].getCoordinateSystem().getIdentifier();

        GetFeature gfr = null;

        try {
            Geometry geom = GeometryFactory.createSurface( ft.getEnvelope(), ft.getEnvelope().getCoordinateSystem() );
            Operation op = new SpatialOperation( OperationDefines.BBOX, new PropertyName( gtName ), geom );
            Filter filter = new ComplexFilter( op );
            IDGenerator idg = IDGenerator.getInstance();

            // create WFS GetFeature request for either HITS or RESULTS
            Query query = Query.create( null, null, null, null, version, new QualifiedName[] { ftName }, null, srcName,
                                        filter, -1, 0, resultType );
            gfr = GetFeature.create( "1.1.0", "" + idg.generateUniqueID(), resultType, "text/xml; subtype=gml/3.1.1",
                                     null, -1, 0, -1, 0, new Query[] { query } );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( Messages.get( "IGEO_STD_CNTXT_ERROR_CREATE_GETFEATURE", ft.getTitle(),
                                                     ft.getEnvelope() ) );
        }
        return gfr;
    }

    // /////////////////////////////////////////////////////////////////////////
    // inner class //
    // /////////////////////////////////////////////////////////////////////////

    /**
     * class that handles the loading of the requested data from the responsible WFS. The class also informs the user
     * who had requested the data via email that the data are ready for download if everything had worked fine.
     * otherwise the the user and the administrator will be informed about the problem that had occured.
     */
    private class LoadController extends Thread {

        private ArrayList<RequestBean> gfrbl = null;

        private String errorFeatures = null;

        private String errorHitsException = null;

        /**
         * default format for data download is SHP
         */
        private String format = "SHP";

        /**
         * initialize the LoadController with an array list of GetFeature requests beans and the download format
         */
        LoadController( ArrayList<RequestBean> gfrl, String format ) {
            this.gfrbl = gfrl;
            this.format = format;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            ArrayList<String> zipFiles = new ArrayList<String>();
            Iterator<RequestBean> iterator = gfrbl.iterator();

            while ( iterator.hasNext() ) {
                RequestBean request = iterator.next();
                String wfsAddr = request.serverLocation;
                GetFeature gfHits = request.hitsRequest; // is NULL if !isMaxFeaturesTestEnabled
                GetFeature gfResults = request.request;
                String layerName = request.layerName;
                String layerTitle = request.layerTitle;

                FeatureCollection fcHits = null;
                FeatureCollection fcResults = null;
                GMLFeatureCollectionDocument gmlDoc = null;
                boolean isHitsException = false;

                try {
                    URL url = new URL( wfsAddr );

                    if ( isMaxFeaturesTestEnabled ) {
                        int numberOfHits = request.maxFeatures;
                        // get number of hits of features in a given BBOX from WFS
                        GetFeatureDocument doc = org.deegree.ogcwebservices.wfs.XMLFactory.export( gfHits );
                        LOG.logDebug( "GetFeature request:\n", doc.getAsPrettyString() );
                        NetWorker nw = new NetWorker( url, doc.getAsString() );
                        // extract numberOfFeatures
                        gmlDoc = new GMLFeatureCollectionDocument();
                        gmlDoc.load( new InputStreamReader( nw.getInputStream() ), XMLFragment.DEFAULT_URL );

                        if ( "ServiceExceptionReport".equals( gmlDoc.getRootElement().getLocalName() )
                             || "ExceptionReport".equals( gmlDoc.getRootElement().getLocalName() ) ) {
                            // if number of hits cannot be retreived from the WFS, then it is unsure whether the
                            // reseult set will contain all feature objects within the given BBOX, or merely a subset
                            // defined by the WFS settings.
                            LOG.logDebug( "Unknown number of Features in the selected area." );
                            isHitsException = true; // -> inform the user to contact the admin
                        } else {
                            fcHits = gmlDoc.parse();
                            numberOfHits = Integer.parseInt( fcHits.getAttribute( "numberOfFeatures" ) );
                        }
                        if ( isHitsException ) {
                            // inform the user to contact the admin
                            LOG.logDebug( "ServiceException for layer " + layerName );
                            if ( errorHitsException != null && errorHitsException.length() > 0 ) {
                                errorHitsException += "\n" + layerName;
                            } else {
                                errorHitsException = layerName;
                            }
                        } else if ( numberOfHits > request.maxFeatures ) {
                            // store info on featureTypes with to many features requested
                            LOG.logDebug( Messages.get( "IGEO_STD_CNTXT_TO_MANY_OBJECTS" ), " - ", layerTitle );

                            if ( errorFeatures != null && errorFeatures.length() > 0 ) {
                                errorFeatures += "\n" + layerTitle;
                            } else {
                                errorFeatures = layerTitle;
                            }
                        } else {
                            // get feature data from WFS
                            doc = org.deegree.ogcwebservices.wfs.XMLFactory.export( gfResults );
                            LOG.logDebug( "GetFeature request:\n", doc.getAsPrettyString() );
                            nw = new NetWorker( url, doc.getAsString() );
                            // transform data and store as shape file
                            gmlDoc = new GMLFeatureCollectionDocument();
                            gmlDoc.load( new InputStreamReader( nw.getInputStream() ), XMLFragment.DEFAULT_URL );
                            fcResults = gmlDoc.parse();
                        }

                    } else {
                        // gfHits = NULL && !isMaxFeaturesTestEnabled
                        // get feature data from WFS
                        GetFeatureDocument doc = org.deegree.ogcwebservices.wfs.XMLFactory.export( gfResults );
                        LOG.logDebug( "GetFeature request:\n", doc.getAsPrettyString() );
                        NetWorker nw = new NetWorker( url, doc.getAsString() );
                        // transform data and store as shape file
                        gmlDoc = new GMLFeatureCollectionDocument();
                        gmlDoc.load( new InputStreamReader( nw.getInputStream() ), XMLFragment.DEFAULT_URL );
                        fcResults = gmlDoc.parse();
                    }
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                    // remove all created zip files
                    // rollback( zipFiles, downloadDir );
                    // LOG.logError( "create shape file: " + gfResults.getQuery()[0].getTypeNames(), e );
                    try {
                        sendErrorMail( e, wfsAddr, ( (Marshallable) gfResults ).exportAsXML() );
                    } catch ( Exception e2 ) {
                        LOG.logError( e2.getMessage(), e2 );
                    }
                    return;
                }

                File fileDir = null;
                try {
                    fileDir = new File( new URL( downloadDir ).getFile() );
                } catch ( MalformedURLException e ) {
                    LOG.logDebug( "Download dir did not start with 'file', trying just as file now.", e );
                    fileDir = new File( downloadDir );
                }
                LOG.logDebug( "download directory (fileDir): ", fileDir.toString() );

                try {
                    if ( fcResults != null && !isHitsException && !( fcResults.size() == 0 ) ) {
                        // add name of the created zipfile to an ArrayList to inform the user where to download the data
                        if ( "GML".equals( format ) ) {
                            String filename = storeGML( gmlDoc, layerName, fileDir );
                            zipFiles.add( filename );
                        } else if ( "SHP".equals( format ) ) {
                            List<String> filenames = storeFC( fcResults, layerName, fileDir );
                            zipFiles.addAll( filenames );
                        } else {
                            // fall back: default behaviour = SHAPE-Download
                            List<String> filenames = storeFC( fcResults, layerName, fileDir );
                            zipFiles.addAll( filenames );
                        }
                    }
                } catch ( Exception e ) {
                    // remove all created zip files
                    rollback( zipFiles, downloadDir );
                    LOG.logError( "create zip file", e );
                    try {
                        String typeNames = arrayToString( gfResults.getQuery()[0].getTypeNames(), "," );
                        sendErrorMail( e, null, typeNames );
                    } catch ( Exception e2 ) {
                        LOG.logError( e2.getMessage(), e );
                    }
                    return;
                }
            } // END WHILE

            try {
                sendSuccessMail( zipFiles );
            } catch ( Exception e ) {
                LOG.logError( "sendSuccessMail", e );
            }
        }

        private String arrayToString( QualifiedName[] strings, String delimiter ) {
            StringBuffer sb = new StringBuffer( 200 );
            for ( int i = 0; i < strings.length; i++ ) {
                sb.append( strings[i] );
                if ( i < strings.length ) {
                    sb.append( delimiter );
                }
            }
            return sb.toString();
        }

        /**
         * stores the passed feature collection as zipped shape and returns the file name(s)
         * 
         * @param fc
         *            feature collection to be stored
         * @param layerName
         *            name of the WMS layer corresponding to the feature type requested from the WFS. It is used for
         *            file name.
         * @param fileDir
         *            the name of the download directory, as set in the WMC
         * @return a list of strings containing all the file names for the input feature collection. These may be more
         *         than on, as a feature collection might contain several geometry types which will be turned into
         *         different shape files: one for point, one for curves, ...
         * @throws PortalException
         */
        private List<String> storeFC( FeatureCollection fc, String layerName, File fileDir )
                                throws PortalException {

            String zipName = null;
            String[] shapeFileNames = null;

            truncatePropertyValues( fc, 127, "..." ); // these values could be externalised to init params, if need be.
            // prevent creating shapes with multiple geometry types
            FeatureCollection[] fcs = FeatureUtils.separateFeaturesForShapes( fc );

            List<String> files = new ArrayList<String>();
            for ( int i = 0; i < fcs.length - 1; i++ ) {
                // only using the first four geometry types: point, multipoint, (multi-)curve, (multi-)surface.
                if ( fcs[i].size() > 0 ) {
                    try {
                        // create and write shape
                        String shapeBaseName = layerName + System.currentTimeMillis() + fcs[i].getId();
                        File fileBaseName = new File( fileDir, shapeBaseName );
                        ShapeFile shp = new ShapeFile( fileBaseName.toString(), "rw" );
                        shp.writeShape( fcs[i] );
                        shp.close();

                        // create zip file from .shp, .shx and .dbf
                        zipName = shapeBaseName + ".zip";
                        shapeFileNames = new String[] { shapeBaseName + ".shp", shapeBaseName + ".shx",
                                                       shapeBaseName + ".dbf" };

                        // create zip archive
                        ZipUtils zu = new ZipUtils();
                        zu.doZip( fileDir.toString(), zipName, shapeFileNames, true, false );
                        LOG.logDebug( "zip was created: " + zipName );
                        files.add( zipName );
                    } catch ( Exception e ) {
                        LOG.logError( e.getMessage(), e );
                        // remove the created shape files
                        for ( int j = 0; j < 3; i++ ) {
                            File file = new File( fileDir, shapeFileNames[i] );
                            file.delete();
                        }
                        // remove the zip file
                        File file = new File( fileDir, zipName );
                        file.delete();
                        throw new PortalException( Messages.get( "IGEO_STD_CNTXT_ERROR_SAVE_SHAPEFILE", file.getName() ),
                                                   e );
                    }
                }
            }
            return files;
        }

        /**
         * @param fc
         * @param maxLength
         *            (e.g. "127")
         * @param postfix
         *            (e.g. "...")
         */
        private void truncatePropertyValues( FeatureCollection fc, int maxLength, String postfix ) {
            postfix = postfix == null ? "" : postfix;
            StringBuffer truncValue = new StringBuffer();

            for ( int i = 0; i < fc.size(); i++ ) {
                Feature feat = fc.getFeature( i );
                FeatureProperty[] fps = feat.getProperties();
                for ( int j = 0; j < fps.length; j++ ) {
                    if ( fps[j].getValue() instanceof String ) {
                        truncValue.setLength( 0 );
                        truncValue.append( (String) fps[j].getValue() );
                        if ( truncValue.length() > maxLength ) {
                            while ( ( truncValue + postfix ).getBytes().length > maxLength ) {
                                truncValue.setLength( truncValue.length() - 1 );
                            }
                            fps[j].setValue( truncValue + postfix );
                        }
                    }
                }
            }
        }

        /**
         * stores the passed feature collection as zipped GML and returns the file name
         * 
         * @param gmlDoc
         *            the gml feature collection document to be stored
         * @param layerName
         *            name of the WMS layer corresponding to the feature type requested from the WFS. It is used for
         *            file names.
         * @param fileDir
         *            the name of the download directory, as set in the WMC
         * @return
         * @throws PortalException
         */
        private String storeGML( GMLFeatureCollectionDocument gmlDoc, String layerName, File fileDir )
                                throws PortalException {

            String zipName = null;
            String[] gmlFileNames = null;

            try {
                String gmlBaseName = layerName + System.currentTimeMillis() + UUID.randomUUID().toString();

                // create and write gml file
                FileWriter fw = new FileWriter( new File( fileDir, gmlBaseName + ".xml" ) );
                fw.write( gmlDoc.getAsPrettyString() );
                fw.close();

                // create zip file
                zipName = gmlBaseName + ".zip";
                gmlFileNames = new String[] { gmlBaseName + ".xml" };

                // create zip archive
                ZipUtils zu = new ZipUtils();
                zu.doZip( fileDir.toString(), zipName, gmlFileNames, true, false );
                LOG.logDebug( "zip was created: " + zipName );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                // remove the created shape files
                for ( int i = 0; i < 3; i++ ) {
                    File file = new File( fileDir, gmlFileNames[i] );
                    file.delete();
                }
                // remove the zip file
                File file = new File( fileDir, zipName );
                file.delete();
                throw new PortalException( Messages.get( "IGEO_STD_CNTXT_ERROR_SAVE_SHAPEFILE", file.getName() ), e );
            }
            return zipName;
        }

        /**
         * deletes all files associated to the download order
         * 
         * @param zipFiles
         *            list of zip files associated to the download order
         */
        private void rollback( ArrayList<String> zipFiles, String dir ) {

            try {
                for ( int i = 0; i < zipFiles.size(); i++ ) {
                    String fn = (String) zipFiles.get( i );
                    File file = new File( dir + "/" + fn );
                    file.delete();
                }
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
        }

        /**
         * sends a mail to inform the administrator and the user about an error that raised performing the data access
         */
        private void sendErrorMail( Exception e, String addr, String target )
                                throws SendMailException {

            String mailAddress = mailAddr;
            String mailHost = ContextMessages.getString( "Email.MailHost" );
            String mailHostUser = ContextMessages.getString( "Email.MailHostUser" );
            String mailHostPassword = ContextMessages.getString( "Email.MailHostPassword" );
            String sender = ContextMessages.getString( "Email.Sender" );
            String subject = ContextMessages.getString( "Email.Header" );

            // create mesage for informing the user
            String st = StringTools.stackTraceToString( e.getStackTrace() );
            String message = null;
            if ( addr == null ) {
                message = Messages.get( "IGEO_STD_CNTXT_ERROR_CREATE_SHAPEFILE", target, st );
            } else {
                message = Messages.get( "IGEO_STD_CNTXT_ERROR_PERFORM_GETFEATURE", addr, target, st );
            }

            // send message to the user
            MailMessage mm = new EMailMessage( sender, mailAddress, subject, message );
            if ( "!Email.MailHostUser!".equals( mailHostUser ) || "!Email.MailHostPassword!".equals( mailHostPassword ) ) {
                MailHelper.createAndSendMail( mm, mailHost );
            } else {
                MailHelper.createAndSendMail( mm, mailHost, mailHostUser, mailHostPassword );
            }
        }

        /**
         * sends a mail to the user that the data download succeded and informs him where to download the created files
         * 
         * @param files
         *            list of created files (names)
         * 
         * @throws SendMailException
         */
        private void sendSuccessMail( ArrayList<String> files )
                                throws SendMailException {

            String mailAddress = mailAddr;
            String mailHost = ContextMessages.getString( "Email.MailHost" );
            String mailHostUser = ContextMessages.getString( "Email.MailHostUser" );
            String mailHostPassword = ContextMessages.getString( "Email.MailHostPassword" );
            String senderMail = ContextMessages.getString( "Email.Sender" );
            String emailSubject = ContextMessages.getString( "Email.Header" );

            // create mesage for informing the user
            String address = NetWorker.url2String( downloadURL );
            if ( !address.endsWith( "/" ) ) {
                address += "/";
            }
            StringBuffer sb = new StringBuffer( Messages.get( "IGEO_STD_CNTXT_INFO_EMAIL_DATA_CREATED" ) );
            for ( int i = 0; i < files.size(); i++ ) {
                String file = (String) files.get( i );
                sb.append( address + "download?file=" + file + "\n" );
            }

            // add info on features with to many results
            if ( errorFeatures != null ) {
                sb.append( "\n" ).append( Messages.get( "IGEO_STD_CNTXT_TO_MANY_OBJECTS" ) ).append( "\n" );
                sb.append( errorFeatures );
            }

            // inform the user to contact the admin for layers that caused a serviceException
            if ( errorHitsException != null ) {
                sb.append( "\n\n" ).append( Messages.get( "IGEO_STD_CNTXT_DOWNLOAD_SERVICE_EXCEPTION" ) ).append( "\n" );
                sb.append( errorHitsException );
            }

            // send message to the user
            MailMessage mm = new EMailMessage( senderMail, mailAddress, emailSubject, sb.toString() );
            if ( "!Email.MailHostUser!".equals( mailHostUser ) || "!Email.MailHostPassword!".equals( mailHostPassword ) ) {
                // if these variables are not set in the properties file, they are set by the getString() method above.
                MailHelper.createAndSendMail( mm, mailHost );
            } else {
                MailHelper.createAndSendMail( mm, mailHost, mailHostUser, mailHostPassword );
            }
        }
    }

    private class RequestBean {

        String serverLocation;

        /**
         * GetFeature request for result_type = hits (returning number of hits). may be null.
         */
        GetFeature hitsRequest;

        /**
         * GetFeature request for result_type = results
         */
        GetFeature request;

        String layerName;

        String layerTitle;

        /**
         * maximum number of results returned by this WFS server, as specified in the WFS capabilities (<ows:Constraint
         * name="DefaultMaxFeatures">). The default value is taken from init parameters, in case the WFS does not
         * provide the value in its capabilities
         */
        int maxFeatures;

        /**
         * @param server
         *            this WFS server address
         * @param hitsRequest
         *            GetFeature request for result_type = hits (returning number of hits). may be null.
         * @param request
         *            GetFeature request for result_type = results
         * @param maxRequest
         *            maximum number of results returned by this WFS server, as specified in the WFS capabilities
         *            (<ows:Constraint name="DefaultMaxFeatures">). The default value is taken from init parameters, in
         *            case the WFS does not provide the value in its capabilities.
         * @param layerName
         *            the name (identifier) of the corresponding WMS layer
         * @param layerTitle
         *            the title of the corresponding WMS layer
         */
        RequestBean( String server, GetFeature hitsRequest, GetFeature request, int maxRequest, String layerName,
                     String layerTitle ) {
            this.serverLocation = server;
            this.request = request;
            this.maxFeatures = maxRequest;
            this.hitsRequest = hitsRequest;
            this.layerName = layerName;
            this.layerTitle = layerTitle;
        }
    }

    /**
     * little helper class to store association between IDs and bounding boxes
     */
    protected class FeatureTemplate {

        /**
         * WMS Layer name
         */
        private String id = null;

        /**
         * WMS Layer title
         */
        private String title = null;

        /**
         * WMS address for this layer
         */
        private String serviceURL = null;

        /**
         * current/requested bbox
         */
        private Envelope bbox = null;

        FeatureTemplate( String id, String title, Envelope bbox, String serviceURL ) {
            this.id = id;
            this.bbox = bbox;
            this.title = title;
            this.serviceURL = serviceURL;
        }

        /**
         * @return id (the WMS layer name)
         */
        public String getId() {
            return id;
        }

        /**
         * @return Title (the WMS layer title)
         */
        public String getTitle() {
            return title;
        }

        /**
         * @return Envelope (current bbox)
         */
        public Envelope getEnvelope() {
            return bbox;
        }

        /**
         * @return ServiceURL (of WFS)
         */
        public String getServerURL() {
            return serviceURL;
        }
    }
}
