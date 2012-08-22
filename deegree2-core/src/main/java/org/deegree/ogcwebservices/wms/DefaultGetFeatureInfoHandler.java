//$HeadURL$
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
package org.deegree.ogcwebservices.wms;

import static java.lang.System.currentTimeMillis;
import static org.deegree.ogcwebservices.wms.operation.WMSProtocolFactory.createGetFeatureInfoResponse;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.datatypes.values.Values;
import org.deegree.framework.concurrent.DoDatabaseQueryTask;
import org.deegree.framework.concurrent.DoExternalAccessTask;
import org.deegree.framework.concurrent.DoServiceTask;
import org.deegree.framework.concurrent.ExecutionFinishedEvent;
import org.deegree.framework.concurrent.Executor;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.i18n.Messages;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.FeatureId;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.InvalidSRSException;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wcs.getcoverage.ResultCoverage;
import org.deegree.ogcwebservices.wfs.RemoteWFService;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wms.capabilities.Dimension;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.ScaleHint;
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wms.configuration.DatabaseDataSource;
import org.deegree.ogcwebservices.wms.configuration.ExternalDataAccessDataSource;
import org.deegree.ogcwebservices.wms.configuration.LocalWFSDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWMSDataSource;
import org.deegree.ogcwebservices.wms.configuration.WMSConfigurationType;
import org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess;
import org.deegree.ogcwebservices.wms.operation.DimensionValues;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfoResult;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.WMSProtocolFactory;
import org.deegree.processing.raster.converter.Image2RawData;
import org.w3c.dom.Document;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 */
class DefaultGetFeatureInfoHandler implements GetFeatureInfoHandler {

    protected static final ILogger LOG = LoggerFactory.getLogger( DefaultGetFeatureInfoHandler.class );

    private static final double DEFAULT_PIXEL_SIZE = 0.00028;

    protected GetFeatureInfo request = null;

    protected GetMap getMapRequest = null;

    protected WMSConfigurationType configuration = null;

    // collects the reponse for each layer
    private Object[] featCol = null;

    // scale of the map
    protected double scale = 0;

    // CRS of the request
    protected CoordinateSystem reqCRS = null;

    protected static final QualifiedName VALUE = new QualifiedName( "value" );

    /**
     * Creates a new GetMapHandler object.
     *
     * @param capabilities
     * @param request
     *            request to perform
     * @throws OGCWebServiceException
     */
    public DefaultGetFeatureInfoHandler( WMSConfigurationType capabilities, GetFeatureInfo request )
                            throws OGCWebServiceException {

        this.request = request;
        this.configuration = capabilities;
        getMapRequest = request.getGetMapRequestCopy();
        try {
            reqCRS = CRSFactory.create( getMapRequest.getSrs() );
            if ( reqCRS == null ) {
                throw new InvalidSRSException( Messages.getMessage( "WMS_UNKNOWN_CRS", getMapRequest.getSrs() ) );
            }
        } catch ( Exception e ) {
            throw new InvalidSRSException( Messages.getMessage( "WMS_UNKNOWN_CRS", getMapRequest.getSrs() ) );
        }
        try {
            Envelope bbox = getMapRequest.getBoundingBox();
            double pixelSize = 1;
            if ( request.getVersion().equals( "1.3.0" ) ) {
                // required because for WMS 1.3.0 'scale' represents the ScaleDenominator
                // and for WMS < 1.3.0 it represents the size of a pixel diagonal in meter
                pixelSize = DEFAULT_PIXEL_SIZE;
            }
            scale = MapUtils.calcScale( getMapRequest.getWidth(), getMapRequest.getHeight(), bbox, reqCRS, pixelSize );
        } catch ( Exception e ) {
            LOG.logDebug( e.getLocalizedMessage(), e );
            throw new OGCWebServiceException( Messages.getMessage( "WMS_SCALECALC", e ) );
        }

    }

    /**
     * performs a GetFeatureInfo request and retruns the result encapsulated within a <tt>WMSFeatureInfoResponse</tt>
     * object.
     * <p>
     * The method throws an WebServiceException that only shall be thrown if an fatal error occurs that makes it
     * imposible to return a result. If something wents wrong performing the request (none fatal error) The exception
     * shall be encapsulated within the response object to be returned to the client as requested
     * (GetFeatureInfo-Request EXCEPTION-Parameter).
     *
     * <p>
     * All sublayers of the queried layer will be added automatically. Non-queryable sublayers are then ignored in the
     * response.
     * </p>
     *
     * @return response to the GetFeatureInfo response
     */
    public GetFeatureInfoResult performGetFeatureInfo()
                            throws OGCWebServiceException {
        long runningTime = currentTimeMillis();

        String[] qlayers = request.getQueryLayers();

        List<Layer> allLayers = new ArrayList<Layer>();

        // here, the explicitly queried layers are checked for being queryable and known
        for ( int i = 0; i < qlayers.length; i++ ) {
            Layer layer = configuration.getLayer( qlayers[i] );

            if ( layer == null ) {
                throw new LayerNotDefinedException( Messages.getMessage( "WMS_UNKNOWNLAYER", qlayers[i] ) );
            }
            if ( !layer.isQueryable() ) {
                throw new LayerNotQueryableException( Messages.getMessage( "WMS_LAYER_NOT_QUERYABLE", qlayers[i] ) );
            }
            if ( !layer.isSrsSupported( getMapRequest.getSrs() ) ) {
                throw new InvalidSRSException( Messages.getMessage( "WMS_UNKNOWN_CRS_FOR_LAYER",
                                                                    getMapRequest.getSrs(), qlayers[i] ) );
            }

            allLayers.add( layer );

            // sub layers are added WITHOUT being checked for being queryable
            // This is desirable for example in the following scenario:
            // Suppose one queryable layer contains a lot of other layers,
            // that are mostly queryable. Then you can query all of those layers
            // at once by just querying the enclosing layer (the unqueryable
            // sub layers are ignored).
            allLayers.addAll( Arrays.asList( layer.getLayer() ) );
        }

        Layer[] layerList = allLayers.toArray( new Layer[allLayers.size()] );

        if ( layerList.length == 1 && layerList[0].getDataSource().length == 1
             && layerList[0].getDataSource()[0].getFeatureInfoURL() != null ) {
            LOG.logDebug( "GetFeatureInfo predefined response workaround enabled." );
            try {
                InputStream stream = layerList[0].getDataSource()[0].getFeatureInfoURL().openStream();
                BufferedReader in = new BufferedReader( new InputStreamReader( stream ) );
                StringBuilder sb = new StringBuilder();

                while ( in.ready() ) {
                    sb.append( in.readLine() ).append( "\n" );
                }

                in.close();

                return createGetFeatureInfoResponse( request, null, sb.toString() );
            } catch ( IOException e ) {
                LOG.logError( "Predefined GetFeatureInfo response could not be read/found.", e );
            }
        }

        LinkedList<Callable<Object>> tasks = new LinkedList<Callable<Object>>();
        for ( int i = 0; i < layerList.length; i++ ) {
            if ( validate( layerList[i] ) ) {
                AbstractDataSource datasource[] = layerList[i].getDataSource();
                for ( int j = 0; j < datasource.length; j++ ) {
                    ScaleHint hint = datasource[j].getScaleHint();
                    if ( datasource[j].isQueryable() && isValidArea( datasource[j].getValidArea() )
                         && scale >= hint.getMin() && scale < hint.getMax() ) {
                        ServiceInvoker si = new ServiceInvoker( layerList[i], datasource[j] );
                        tasks.add( si );
                    } else {
                        LOG.logDebug( "Not using a datasource of layer " + layerList[i].getName()
                                      + " due to scale or bbox limits." );
                    }
                }
            } else {
                LOG.logDebug( "Layer " + layerList[i].getName() + " not validated." );
            }
        }

        // was: subtract 1 second for architecture overhead and image creation
        // am not going to do this!
        long timeLimit = 1000 * ( configuration.getDeegreeParams().getRequestTimeLimit() );
        timeLimit -= ( runningTime - currentTimeMillis() );
        try {
            List<ExecutionFinishedEvent<Object>> list = Executor.getInstance().performSynchronously( tasks, timeLimit );
            featCol = new Object[list.size()];
            int i = 0;
            for ( ExecutionFinishedEvent<Object> evt : list ) {
                try {
                    Object o = evt.getResult();
                    featCol[i++] = o;
                } catch ( CancellationException e ) {
                    createExceptionResponse( e );
                } catch ( Exception e ) {
                    createExceptionResponse( e );
                } catch ( Throwable e ) {
                    LOG.logError( "Unknown error", e );
                }
            }
        } catch ( InterruptedException e ) {
            createExceptionResponse( e );
        }

        GetFeatureInfoResult res = createFeatureInfoResponse();

        return res;
    }

    /**
     * returns true if the requested boundingbox intersects with the valid area of a datasource
     *
     * @param validArea
     */
    private boolean isValidArea( Geometry validArea ) {

        if ( validArea != null ) {
            try {
                Envelope env = request.getGetMapRequestCopy().getBoundingBox();
                Geometry geom = GeometryFactory.createSurface( env, reqCRS );
                if ( !reqCRS.getIdentifier().equals( validArea.getCoordinateSystem().getIdentifier() ) ) {
                    // if requested CRS is not identical to the CRS of the valid area
                    // a transformation must be performed before intersection can
                    // be checked
                    GeoTransformer gt = new GeoTransformer( validArea.getCoordinateSystem() );
                    geom = gt.transform( geom );
                }
                return geom.intersects( validArea );
            } catch ( Exception e ) {
                // should never happen
                LOG.logError( "Could not validate WMS datasource area", e );
            }
        }
        return true;
    }

    /**
     * validates if the requested layer matches the conditions of the request if not a <tt>WebServiceException</tt> will
     * be thrown. If the layer matches the request, but isn't able to deliver data for the requested area and/or scale
     * false will be returned. If the layer matches the request and contains data for the requested area and/or scale
     * true will be returned.
     *
     * @param layer
     *            layer as defined at the capabilities/configuration
     */
    private boolean validate( Layer layer )
                            throws OGCWebServiceException {

        if ( layer == null ) {
            return false;
        }

        // why the layer can be null here is not known, but just in case:
        String name = layer.getName();

        // check for valid coordinated reference system
        String[] srs = layer.getSrs();
        boolean tmp = false;
        for ( int i = 0; i < srs.length; i++ ) {
            if ( srs[i].equalsIgnoreCase( getMapRequest.getSrs() ) ) {
                tmp = true;
                break;
            }
        }

        if ( !tmp ) {
            throw new InvalidSRSException( Messages.getMessage( "WMS_INVALIDSRS", name, getMapRequest.getSrs() ) );
        }

        // check bounding box
        try {

            Envelope bbox = getMapRequest.getBoundingBox();
            Envelope layerBbox = layer.getLatLonBoundingBox();
            if ( !getMapRequest.getSrs().equalsIgnoreCase( "EPSG:4326" ) ) {
                // transform the bounding box of the request to EPSG:4326
                GeoTransformer gt = new GeoTransformer( CRSFactory.create( "EPSG:4326" ) );
                bbox = gt.transform( bbox, reqCRS );
            }

            if ( !bbox.intersects( layerBbox ) ) {
                return false;
            }

        } catch ( Exception e ) {
            throw new OGCWebServiceException( Messages.getMessage( "WMS_BBOXCOMPARSION" ) );
        }

        return true;
    }

    /**
     * creates a <tt>GetFeatureInfoResult</tt> containing an <tt>OGCWebServiceException</tt>
     *
     * @param e
     *            exception to encapsulate into the response
     */
    private GetFeatureInfoResult createExceptionResponse( Exception e ) {

        OGCWebServiceException exce = null;

        // default --> application/vnd.ogc.se_xml
        exce = new OGCWebServiceException( getClass().getName(), e.getMessage() );

        GetFeatureInfoResult res = WMSProtocolFactory.createGetFeatureInfoResponse( request, exce, null );

        return res;
    }

    /**
     * generates the desired output from the GMLs
     *
     * @return the result object
     * @throws OGCWebServiceException
     */
    private GetFeatureInfoResult createFeatureInfoResponse()
                            throws OGCWebServiceException {

        Envelope bbox = getMapRequest.getBoundingBox();

        StringBuffer sb = new StringBuffer( 20000 );
        sb.append( "<ll:FeatureCollection " );

        URL schemaLoc = configuration.getDeegreeParams().getFeatureSchemaLocation();
        if ( schemaLoc != null ) {
            sb.append( "xsi:schemaLocation='" );
            sb.append( configuration.getDeegreeParams().getFeatureSchemaNamespace() );
            sb.append( " " );
            sb.append( schemaLoc.toExternalForm() );
            sb.append( "'" );
        }

        sb.append( " xmlns:gml='http://www.opengis.net/gml' " );
        sb.append( "xmlns:ll='http://www.lat-lon.de' " );
        sb.append( "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " );
        URL url = configuration.getDeegreeParams().getSchemaLocation();
        if ( url != null ) {
            sb.append( "xsi:schemaLocation='" );
            sb.append( "http://www.lat-lon.de " + url.toExternalForm() + "'" );
        }
        sb.append( "><gml:boundedBy>" );
        sb.append( "<gml:Box srsName='" + getMapRequest.getSrs() + "'>" );
        sb.append( "<gml:coordinates>" + bbox.getMin().getX() + "," );
        sb.append( bbox.getMin().getY() + " " + bbox.getMax().getX() + "," );
        sb.append( bbox.getMax().getY() + "</gml:coordinates>" );
        sb.append( "</gml:Box></gml:boundedBy>" );

        int cnt = 0;

        for ( int i = 0; i < featCol.length; i++ ) {
            if ( featCol[i] instanceof OGCWebServiceException ) {
                throw (OGCWebServiceException) featCol[i];
            }
            if ( featCol[i] != null ) {
                FeatureCollection fc = (FeatureCollection) featCol[i];
                cnt = appendFeatureCollection( fc, sb, cnt );
            }

            // if ( cnt >= request.getFeatureCount() ) break;
        }
        sb.append( "</ll:FeatureCollection>" );
        LOG.logDebug( "original feature info response: ", sb );
        GetFeatureInfoResult response = WMSProtocolFactory.createGetFeatureInfoResponse( request, null, sb.toString() );

        return response;
    }

    /**
     *
     * @param col
     * @param sb
     * @param cnt
     * @return a counter, probably the same that is given as argument
     */
    private int appendFeatureCollection( FeatureCollection col, StringBuffer sb, int cnt ) {

        Feature[] feat = col.toArray();
        if ( feat != null ) {
            for ( int j = 0; j < feat.length; j++ ) {
                FeatureType ft = feat[j].getFeatureType();
                PropertyType[] ftp = ft.getProperties();
                cnt++;
                sb.append( "<gml:featureMember>" );
                sb.append( "<ll:" ).append( ft.getName().getLocalName() );
                sb.append( " fid='" ).append( feat[j].getId().replace( ' ', '_' ) ).append( "'>" );
                for ( int i = 0; i < ftp.length; i++ ) {
                    if ( ftp[i].getType() != Types.GEOMETRY && ftp[i].getType() != Types.POINT
                         && ftp[i].getType() != Types.CURVE && ftp[i].getType() != Types.SURFACE
                         && ftp[i].getType() != Types.MULTIPOINT && ftp[i].getType() != Types.MULTICURVE
                         && ftp[i].getType() != Types.MULTISURFACE ) {

                        FeatureProperty[] props = feat[j].getProperties( ftp[i].getName() );
                        if ( props != null ) {
                            for ( FeatureProperty property : props ) {
                                Object value = property.getValue();
                                sb.append( "<ll:" + ftp[i].getName().getLocalName() + ">" );
                                if ( value instanceof FeatureCollection ) {
                                    FeatureCollection fc = (FeatureCollection) value;
                                    appendFeatureCollection( fc, sb, cnt );
                                } else {
                                    sb.append( "<![CDATA[" ).append( value ).append( "]]>" );
                                }
                                sb.append( "</ll:" + ftp[i].getName().getLocalName() + ">" );
                            }
                        }
                    }
                }
                sb.append( "</ll:" ).append( ft.getName().getLocalName() ).append( '>' );
                sb.append( "</gml:featureMember>" );
                if ( cnt >= request.getFeatureCount() )
                    break;
            }
        }

        return cnt;
    }

    // //////////////////////////////////////////////////////////////////////////
    // inner classes //
    // //////////////////////////////////////////////////////////////////////////

    /**
     * Inner class for accessing the data of one layer and creating a GML document from it. The class extends
     * <tt>Thread</tt> and implements the run method, so that a parallel data accessing from several layers is possible.
     *
     * @version $Revision$
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
     */
    public class ServiceInvoker implements Callable<Object> {
        private Layer layer = null;

        private AbstractDataSource datasource = null;

        /**
         * Creates a new ServiceInvoker object.
         *
         * @param layer
         * @param datasource
         */
        ServiceInvoker( Layer layer, AbstractDataSource datasource ) {
            this.layer = layer;
            this.datasource = datasource;
        }

        /**
         * central method for access the data assigned to a data source
         *
         * @return result of feature info query
         */
        public Object call() {
            Object response = null;
            if ( datasource != null ) {
                Callable<Object> task = null;
                try {
                    int type = datasource.getType();
                    switch ( type ) {
                    case AbstractDataSource.LOCALWFS:
                    case AbstractDataSource.REMOTEWFS: {
                        OGCWebServiceRequest request = createGetFeatureRequest( (LocalWFSDataSource) datasource );
                        task = new DoServiceTask<Object>( datasource.getOGCWebService(), request );
                        break;
                    }
                    case AbstractDataSource.LOCALWCS:
                    case AbstractDataSource.REMOTEWCS: {
                        OGCWebServiceRequest request = GetMapServiceInvokerForNL.createGetCoverageRequest( datasource,
                                                                                                           getMapRequest );
                        task = new DoServiceTask<Object>( datasource.getOGCWebService(), request );
                        break;
                    }
                    case AbstractDataSource.REMOTEWMS: {
                        OGCWebServiceRequest request = createGetFeatureInfo( datasource );
                        task = new DoServiceTask<Object>( datasource.getOGCWebService(), request );
                        break;
                    }
                    case AbstractDataSource.DATABASE: {
                        DatabaseDataSource dbds = (DatabaseDataSource) datasource;
                        Envelope target = calcTargetArea( dbds, dbds.getNativeCRS().getPrefixedName() );

                        for ( Dimension dim : configuration.getLayer( layer.getName() ).getDimension() ) {
                            if ( dim.getDefaultValue() != null ) {
                                if ( dim.getName().equals( "time" )
                                     && request.getGetMapRequestCopy().getDimTime() == null ) {
                                    request.getGetMapRequestCopy().setDimTime(
                                                                               new DimensionValues(
                                                                                                    dim.getDefaultValue() ) );
                                } else if ( dim.getName().equals( "elevation" )
                                            && request.getGetMapRequestCopy().getDimElev() == null ) {
                                    request.getGetMapRequestCopy().setDimElev(
                                                                               new DimensionValues(
                                                                                                    dim.getDefaultValue() ) );
                                }
                            }
                        }

                        task = new DoDatabaseQueryTask( (DatabaseDataSource) datasource, target, null,
                                                        datasource.getDimProps(), request.getGetMapRequestCopy() );
                        break;
                    }
                    case AbstractDataSource.EXTERNALDATAACCESS: {
                        ExternalDataAccess eda = ( (ExternalDataAccessDataSource) datasource ).getExternalDataAccess();
                        task = new DoExternalAccessTask( eda, request );
                        break;
                    }
                    }
                } catch ( Exception e ) {
                    OGCWebServiceException exce = new OGCWebServiceException( "ServiceInvoker: " + layer.getName(),
                                                                              Messages.getMessage( "WMS_CREATE_QUERY" ) );
                    response = exce;
                    LOG.logError( Messages.getMessage( "WMS_CREATE_QUERY" ) + ": " + e.getMessage(), e );
                    throw new RuntimeException( e );
                }

                try {
                    Executor executor = Executor.getInstance();
                    Object o = executor.performSynchronously( task, datasource.getRequestTimeLimit() * 1000 );
                    response = handleResponse( o );
                } catch ( CancellationException e ) {
                    // exception can't be re-thrown because responsible GetMapHandler
                    // must collect all responses of all data sources
                    String s = Messages.getMessage( "WMS_TIMEOUTDATASOURCE",
                                                    new Integer( datasource.getRequestTimeLimit() ) );
                    LOG.logError( s, e );
                    if ( datasource.isFailOnException() ) {
                        OGCWebServiceException exce = new OGCWebServiceException( getClass().getName(), s );
                        response = exce;
                    } else {
                        response = null;
                    }
                } catch ( Throwable t ) {
                    // exception can't be re-thrown because responsible GetMapHandler
                    // must collect all responses of all data sources
                    String s = Messages.getMessage( "WMS_ERRORDOSERVICE", t.getMessage() );
                    LOG.logError( s, t );
                    if ( datasource.isFailOnException() ) {
                        OGCWebServiceException exce = new OGCWebServiceException( getClass().getName(), s );
                        response = exce;
                    } else {
                        response = null;
                    }
                }

            }

            return response;

        }

        /**
         * creates a getFeature request considering the getMap request and the filter conditions defined in the
         * submitted <tt>DataSource</tt> object. The request will be encapsulated within a <tt>OGCWebServiceEvent</tt>.
         *
         * @param ds
         * @return GetFeature request object
         */
        private GetFeature createGetFeatureRequest( LocalWFSDataSource ds )
                                throws Exception {

            WFSCapabilities capa;
            if ( ds.getOGCWebService() instanceof WFService ) {
                WFService se = (WFService) ds.getOGCWebService();
                capa = se.getCapabilities();
            } else {
                RemoteWFService wfs = (RemoteWFService) ds.getOGCWebService();
                capa = wfs.getWFSCapabilities();
            }
            QualifiedName gn = ds.getName();
            WFSFeatureType ft = capa.getFeatureTypeList().getFeatureType( gn );

            if ( ft == null ) {
                throw new OGCWebServiceException( Messages.getMessage( "WMS_UNKNOWNFT", ds.getName() ) );
            }
            String crs = ft.getDefaultSRS().toASCIIString();
            Envelope targetArea = calcTargetArea( ds, crs );

            // no filter condition has been defined
            StringBuffer sb = new StringBuffer( 2000 );
            sb.append( "<?xml version='1.0' encoding='" + CharsetUtils.getSystemCharset() + "'?>" );
            sb.append( "<GetFeature xmlns='http://www.opengis.net/wfs' " );
            sb.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
            sb.append( "xmlns:wfs='http://www.opengis.net/wfs' " );
            sb.append( "xmlns:gml='http://www.opengis.net/gml' " );
            sb.append( "xmlns:" ).append( ds.getName().getPrefix() ).append( '=' );
            sb.append( "'" ).append( ds.getName().getNamespace() ).append( "' " );
            sb.append( "service='WFS' version='1.1.0' " );
            if ( ds.getType() == AbstractDataSource.LOCALWFS ) {
                sb.append( "outputFormat='FEATURECOLLECTION'>" );
            } else {
                sb.append( "outputFormat='text/xml; subtype=gml/3.1.1'>" );
            }
            sb.append( "<Query typeName='" + ds.getName().getPrefixedName() + "'>" );

            Query query = ds.getQuery();

            // append <wfs:PropertyName> elements
            if ( query != null && query.getPropertyNames() != null ) {
                PropertyPath[] propertyNames = query.getPropertyNames();
                for ( PropertyPath path : propertyNames ) {
                    NamespaceContext nsContext = path.getNamespaceContext();
                    sb.append( "<wfs:PropertyName" );
                    Map<String, URI> namespaceMap = nsContext.getNamespaceMap();
                    Iterator<String> prefixIter = namespaceMap.keySet().iterator();
                    while ( prefixIter.hasNext() ) {
                        String prefix = prefixIter.next();
                        if ( !CommonNamespaces.XMLNS_PREFIX.equals( prefix ) ) {
                            URI namespace = namespaceMap.get( prefix );
                            sb.append( " xmlns:" + prefix + "=\"" + namespace + "\"" );
                        }
                    }
                    sb.append( '>' );
                    sb.append( path.getAsString() );
                    sb.append( "</wfs:PropertyName>" );
                }
            }

            sb.append( "<ogc:Filter>" );
            if ( query == null ) {
                // BBOX operation for speeding up the search at simple datasources
                // like shapes
                sb.append( "<ogc:BBOX><PropertyName>" );
                sb.append( ds.getGeometryProperty().getPrefixedName() );
                sb.append( "</PropertyName>" );
                sb.append( GMLGeometryAdapter.exportAsBox( targetArea ) );
                sb.append( "</ogc:BBOX>" );
                sb.append( "</ogc:Filter></Query></GetFeature>" );
            } else {
                Filter filter = query.getFilter();

                sb.append( "<ogc:And>" );
                // BBOX operation for speeding up the search at simple datasources
                // like shapes
                sb.append( "<ogc:BBOX><PropertyName>" + ds.getGeometryProperty().getPrefixedName() );
                sb.append( "</PropertyName>" );
                sb.append( GMLGeometryAdapter.exportAsBox( targetArea ) );
                sb.append( "</ogc:BBOX>" );

                if ( filter instanceof ComplexFilter ) {
                    org.deegree.model.filterencoding.Operation op = ( (ComplexFilter) filter ).getOperation();
                    sb.append( op.to110XML() );
                } else {
                    ArrayList<FeatureId> featureIds = ( (FeatureFilter) filter ).getFeatureIds();
                    for ( int i = 0; i < featureIds.size(); i++ ) {
                        FeatureId fid = featureIds.get( i );
                        sb.append( fid.toXML() );
                    }
                }
                sb.append( "</ogc:And></ogc:Filter></Query></GetFeature>" );
            }

            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                LOG.logDebug( "GetFeature-request:\n" + sb );
            }

            // create dom representation of the request
            StringReader sr = new StringReader( sb.toString() );
            Document doc = XMLTools.parse( sr );

            // create OGCWebServiceEvent object
            IDGenerator idg = IDGenerator.getInstance();
            GetFeature gfr = GetFeature.create( "" + idg.generateUniqueID(), doc.getDocumentElement() );

            return gfr;
        }

        /**
         * calculates the target area for the getfeatureinfo request from the maps bounding box, the its size and the
         * image coordinates of interest. An area is calculated instead of using a point because to consider
         * uncertainties determining the point of interest
         *
         * @param ds
         *            <tt>DataSource</tt> of the layer that is requested for feature infos (each layer may be offered in
         *            its own crs)
         * @param crs
         */
        private Envelope calcTargetArea( AbstractDataSource ds, String crs )
                                throws OGCWebServiceException {

            int width = request.getGetMapRequestCopy().getWidth();
            int height = request.getGetMapRequestCopy().getHeight();
            int x = request.getClickPoint().x;
            int y = request.getClickPoint().y;

            Envelope bbox = request.getGetMapRequestCopy().getBoundingBox();

            // transform request bounding box to the coordinate reference
            // system the WFS holds the data if requesting CRS and WFS-Data
            // crs are different
            Envelope tBbox = null;
            try {
                GeoTransform gt = new WorldToScreenTransform( bbox.getMin().getX(), bbox.getMin().getY(),
                                                              bbox.getMax().getX(), bbox.getMax().getY(), 0, 0,
                                                              width - 1, height - 1 );
                double[] target = new double[4];
                int rad = configuration.getDeegreeParams().getFeatureInfoRadius();
                target[0] = gt.getSourceX( x - rad );
                target[1] = gt.getSourceY( y + rad );
                target[2] = gt.getSourceX( x + rad );
                target[3] = gt.getSourceY( y - rad );

                tBbox = GeometryFactory.createEnvelope( target[0], target[1], target[2], target[3], null );
                if ( !( crs.equalsIgnoreCase( request.getGetMapRequestCopy().getSrs() ) ) ) {
                    GeoTransformer transformer = new GeoTransformer( CRSFactory.create( crs ) );
                    tBbox = transformer.transform( tBbox, reqCRS );
                }

            } catch ( Exception e ) {
                throw new OGCWebServiceException( e.toString() );
            }

            return tBbox;
        }

        /**
         * creates a GetFeatureInfo request for requesting a cascaded remote WMS The request will be encapsulated within
         * a <tt>OGCWebServiceEvent</tt>.
         *
         * @param ds
         * @return GetFeatureInfo request object
         */
        private GetFeatureInfo createGetFeatureInfo( AbstractDataSource ds ) {

            // create embbeded map request
            GetMap gmr = ( (RemoteWMSDataSource) ds ).getGetMapRequest();

            String format = getMapRequest.getFormat();

            if ( gmr != null && !"%default%".equals( gmr.getFormat() ) ) {
                format = gmr.getFormat();
            }

            org.deegree.ogcwebservices.wms.operation.GetMap.Layer[] lys = null;
            lys = new org.deegree.ogcwebservices.wms.operation.GetMap.Layer[1];
            lys[0] = GetMap.createLayer( layer.getName(), "$DEFAULT" );

            if ( gmr != null && gmr.getLayers() != null ) {
                lys = gmr.getLayers();
            }
            Color bgColor = getMapRequest.getBGColor();
            if ( gmr != null && gmr.getBGColor() != null ) {
                bgColor = gmr.getBGColor();
            }
            Values time = getMapRequest.getTime();
            if ( gmr != null && gmr.getTime() != null ) {
                time = gmr.getTime();
            }
            Map<String, String> vendorSpecificParameter = getMapRequest.getVendorSpecificParameters();
            if ( gmr != null && gmr.getVendorSpecificParameters() != null
                 && gmr.getVendorSpecificParameters().size() > 0 ) {
                vendorSpecificParameter = gmr.getVendorSpecificParameters();
            }
            String version = "1.1.0";
            if ( gmr != null && gmr.getVersion() != null ) {
                version = gmr.getVersion();
            }
            Values elevation = getMapRequest.getElevation();
            if ( gmr != null && gmr.getElevation() != null ) {
                elevation = gmr.getElevation();
            }
            Map<String, Values> sampleDim = null;
            if ( gmr != null && gmr.getSampleDimension() != null ) {
                sampleDim = gmr.getSampleDimension();
            }

            IDGenerator idg = IDGenerator.getInstance();
            gmr = GetMap.create( version, "" + idg.generateUniqueID(), lys, elevation, sampleDim, format,
                                 getMapRequest.getWidth(), getMapRequest.getHeight(), getMapRequest.getSrs(),
                                 getMapRequest.getBoundingBox(), getMapRequest.getTransparency(), bgColor,
                                 getMapRequest.getExceptions(), time, null, null, vendorSpecificParameter );

            // create GetFeatureInfo request for cascaded/remote WMS
            String[] queryLayers = new String[] { ds.getName().getPrefixedName() };
            GetFeatureInfo req = GetFeatureInfo.create( version, this.toString(), queryLayers, gmr,
                                                        "application/vnd.ogc.gml", request.getFeatureCount(),
                                                        request.getClickPoint(), request.getExceptions(), null,
                                                        request.getVendorSpecificParameters() );

            try {
                LOG.logDebug( "cascaded GetFeatureInfo request: ", req.getRequestParameter() );
            } catch ( OGCWebServiceException e ) {
                LOG.logError( e.getMessage(), e );
            }

            return req;
        }

        /**
         * The method implements the <tt>OGCWebServiceClient</tt> interface. So a deegree OWS implementation accessed by
         * this class is able to return the result of a request by calling the write-method.
         *
         * @param result
         *            to a GetXXX request
         * @return the response object
         * @throws Exception
         */
        private Object handleResponse( Object result )
                                throws Exception {
            Object response = null;
            if ( result instanceof FeatureResult ) {
                response = handleGetFeatureResponse( (FeatureResult) result );
            } else if ( result instanceof ResultCoverage ) {
                response = handleGetCoverageResponse( (ResultCoverage) result );
            } else if ( result instanceof GetFeatureInfoResult ) {
                response = handleGetFeatureInfoResult( (GetFeatureInfoResult) result );
            } else {
                throw new Exception( Messages.getMessage( "WMS_UNKNOWNRESPONSEFORMAT" ) );
            }
            return response;
        }

        /**
         * handles the response of a WFS and calls a factory to create <tt>DisplayElement</tt> and a <tt>Theme</tt> from
         * it
         *
         * @param response
         * @return the response objects
         * @throws Exception
         */
        private Object handleGetFeatureResponse( FeatureResult response )
                                throws Exception {
            FeatureCollection fc = null;

            Object o = response.getResponse();
            if ( o instanceof FeatureCollection ) {
                fc = (FeatureCollection) o;
            } else if ( o.getClass() == byte[].class ) {
                Reader reader = new InputStreamReader( new ByteArrayInputStream( (byte[]) o ) );
                GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
                doc.load( reader, XMLFragment.DEFAULT_URL );
                fc = doc.parse();
            } else {
                throw new Exception( Messages.getMessage( "WMS_UNKNOWNDATAFORMATFT" ) );
            }
            return fc;
        }

        /**
         *
         * @param res
         */
        private Object handleGetFeatureInfoResult( GetFeatureInfoResult res )
                                throws Exception {

            FeatureCollection fc = null;
            StringReader sr = new StringReader( res.getFeatureInfo() );
            XMLFragment xml = new XMLFragment( sr, XMLFragment.DEFAULT_URL );

            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                LOG.logDebug( "GetFeature-response (before transformation): " + xml.getAsPrettyString() );
            }

            if ( datasource.getType() == AbstractDataSource.REMOTEWMS ) {
                URL url = ( (RemoteWMSDataSource) datasource ).getFeatureInfoTransform();
                if ( url != null ) {
                    // transform incoming GML/XML to a GML application schema
                    // that is understood by deegree
                    XSLTDocument xslt = new XSLTDocument();
                    xslt.load( url );
                    xml = xslt.transform( xml, null, null, null );
                }
            }
            GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
            doc.setRootElement( xml.getRootElement() );
            fc = doc.parse();
            return fc;
        }

        private Object handleGetCoverageResponse( ResultCoverage response )
                                throws OGCWebServiceException {
            ImageGridCoverage gc = (ImageGridCoverage) response.getCoverage();
            if ( gc != null ) {
                BufferedImage bi = gc.getAsImage( -1, -1 );

                float[][] data = new Image2RawData( bi ).parse();

                double scaleX = (double) data[0].length / (double) getMapRequest.getWidth();
                double scaleY = (double) data.length / (double) getMapRequest.getHeight();
                int pxSizeX = (int) Math.round( scaleX );
                int pxSizeY = (int) Math.round( scaleY );
                if ( pxSizeX == 0 ) {
                    pxSizeX = 1;
                }
                if ( pxSizeY == 0 ) {
                    pxSizeY = 1;
                }

                LOG.logDebug( "Size of grid coverage is " + data[0].length + "x" + data.length + "." );
                LOG.logDebug( "Returning an area of " + pxSizeX + "x" + pxSizeY + " pixels." );

                int ix = (int) ( request.getClickPoint().x * scaleX ) - pxSizeX / 2;
                int iy = (int) ( request.getClickPoint().y * scaleY ) - pxSizeY / 2;

                // some checks to avoid areas that are not requestable
                if ( ix < 0 ) {
                    ix = 0;
                }
                if ( iy < 0 ) {
                    iy = 0;
                }
                if ( ix >= ( data[0].length - pxSizeX ) ) {
                    ix = data[0].length - pxSizeX - 1;
                }
                if ( iy >= ( data.length - pxSizeY ) ) {
                    iy = data.length - pxSizeY - 1;
                }

                FeatureCollection fc = FeatureFactory.createFeatureCollection( gc.getCoverageOffering().getName(),
                                                                               pxSizeX * pxSizeY );

                PropertyType pt = null;
                try {
                    pt = FeatureFactory.createPropertyType( VALUE, new QualifiedName( "xsd", "double",
                                                                                      CommonNamespaces.XSNS ), 1, 1 );
                } catch ( UnknownTypeException e ) {
                    LOG.logError( "The xsd:double type is not known?!? Get a new deegree.jar!", e );
                }
                FeatureType ft = FeatureFactory.createFeatureType( gc.getCoverageOffering().getName(), false,
                                                                   new PropertyType[] { pt } );

                for ( int x = ix; x < ix + pxSizeX; ++x ) {
                    for ( int y = iy; y < iy + pxSizeY; ++y ) {
                        FeatureProperty p = FeatureFactory.createFeatureProperty( VALUE, new Double( data[y][x] ) );
                        Feature f = FeatureFactory.createFeature( "ID_faked_for_" + x + "x" + y, ft,
                                                                  new FeatureProperty[] { p } );
                        fc.add( f );
                    }
                }

                return fc;
            }

            throw new OGCWebServiceException( getClass().getName(), Messages.getMessage( "WMS_NOCOVERAGE",
                                                                                         datasource.getName() ) );

        }

    }

}
