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

import static org.deegree.graphics.sld.StyleUtils.extractRequiredProperties;

import static org.deegree.i18n.Messages.get;
import static org.deegree.model.spatialschema.GMLGeometryAdapter.exportAsEnvelope;
import static org.deegree.ogcbase.ExceptionCode.INVALIDDIMENSIONVALUE;
import static org.deegree.ogcbase.ExceptionCode.MISSINGDIMENSIONVALUE;
import static org.deegree.ogcbase.PropertyPathFactory.createPropertyPath;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.concurrent.DoDatabaseQueryTask;
import org.deegree.framework.concurrent.DoExternalAccessTask;
import org.deegree.framework.concurrent.DoServiceTask;
import org.deegree.framework.concurrent.Executor;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.graphics.MapFactory;
import org.deegree.graphics.Theme;
import org.deegree.graphics.sld.FeatureTypeConstraint;
import org.deegree.graphics.sld.LayerFeatureConstraints;
import org.deegree.graphics.sld.NamedLayer;
import org.deegree.graphics.sld.UserStyle;
import org.deegree.i18n.Messages;
import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.FeatureId;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wcs.WCSException;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
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
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wms.configuration.DatabaseDataSource;
import org.deegree.ogcwebservices.wms.configuration.ExternalDataAccessDataSource;
import org.deegree.ogcwebservices.wms.configuration.LocalWCSDataSource;
import org.deegree.ogcwebservices.wms.configuration.LocalWFSDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWCSDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWMSDataSource;
import org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess;
import org.deegree.ogcwebservices.wms.operation.DimensionValues;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.GetMapResult;
import org.deegree.ogcwebservices.wms.operation.DimensionValues.DimensionValue;
import org.w3c.dom.Document;

/**
 * Class for accessing the data of one layers datasource and creating a <tt>Theme</tt> from it.
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class GetMapServiceInvokerForNL extends GetMapServiceInvoker implements Callable<Object> {

    private static final ILogger LOG = LoggerFactory.getLogger( GetMapServiceInvokerForNL.class );

    private final GetMap request;

    private NamedLayer layer = null;

    private UserStyle style = null;

    private AbstractDataSource datasource = null;

    /**
     * Creates a new ServiceInvokerForNL object.
     * 
     * @param handler
     * @param layer
     * @param datasource
     * @param style
     * @param scale
     */
    GetMapServiceInvokerForNL( DefaultGetMapHandler handler, NamedLayer layer, AbstractDataSource datasource,
                               UserStyle style, double scale ) {

        super( handler, scale );

        this.layer = layer;
        this.request = handler.getRequest();
        this.style = style;
        this.datasource = datasource;
    }

    /**
     * @return the callable actually fetching the data
     * @throws Exception
     */
    public Callable<Object> getWrappedCallable()
                            throws Exception {
        int type = datasource.getType();
        switch ( type ) {
        case AbstractDataSource.LOCALWFS:
        case AbstractDataSource.REMOTEWFS: {
            OGCWebServiceRequest request = createGetFeatureRequest( (LocalWFSDataSource) datasource );
            return new DoServiceTask<Object>( datasource.getOGCWebService(), request );
        }
        case AbstractDataSource.LOCALWCS:
        case AbstractDataSource.REMOTEWCS: {
            OGCWebServiceRequest request = createGetCoverageRequest( datasource, this.request );
            return new DoServiceTask<Object>( datasource.getOGCWebService(), request );
        }
        case AbstractDataSource.REMOTEWMS: {
            String styleName = null;

            if ( style != null ) {
                styleName = style.getName();
            }

            OGCWebServiceRequest request = GetMap.createGetMapRequest( datasource, handler.getRequest(), styleName,
                                                                       layer.getName() );
            LOG.logDebug( "GetMap request: " + request.toString() );
            return new DoServiceTask<Object>( datasource.getOGCWebService(), request );
        }
        case AbstractDataSource.DATABASE: {
            CoordinateSystem crs = CRSFactory.create( this.request.getSrs() );
            Envelope env = this.request.getBoundingBox();
            env = GeometryFactory.createEnvelope( env.getMin(), env.getMax(), crs );

            // set dim default values TODO do so in general?
            for ( Dimension dim : handler.getConfiguration().getLayer( layer.getName() ).getDimension() ) {
                if ( dim.getDefaultValue() != null ) {
                    if ( dim.getName().equals( "time" ) && request.getDimTime() == null ) {
                        request.setDimTime( new DimensionValues( dim.getDefaultValue() ) );
                    } else if ( dim.getName().equals( "elevation" ) && request.getDimElev() == null ) {
                        request.setDimElev( new DimensionValues( dim.getDefaultValue() ) );
                    }
                }
            }

            if ( handler.sqls != null ) {
                return new DoDatabaseQueryTask( (DatabaseDataSource) datasource, env,
                                                handler.sqls.get( layer.getName() ), datasource.getDimProps(), request );
            }
            return new DoDatabaseQueryTask( (DatabaseDataSource) datasource, env, null, datasource.getDimProps(),
                                            request );
        }
        case AbstractDataSource.EXTERNALDATAACCESS: {
            ExternalDataAccess eda = ( (ExternalDataAccessDataSource) datasource ).getExternalDataAccess();
            return new DoExternalAccessTask( eda, request );
        }
        default:
            return null;
        }
    }

    public Object call()
                            throws OGCWebServiceException {

        Object response = null;
        if ( datasource != null ) {
            Callable<Object> task = null;
            try {
                task = getWrappedCallable();
            } catch ( OGCWebServiceException e ) {
                throw e;
            } catch ( Exception e ) {
                LOG.logError( "Exception during fetching data for some data source", e );
                OGCWebServiceException exce = new OGCWebServiceException( getClass().getName(),
                                                                          Messages.getMessage( "WMS_ERRORQUERYCREATE",
                                                                                               e ) );
                // exception can't be re-thrown because responsible GetMapHandler
                // must collect all responses of all data sources
                response = exce;
            }

            try {
                // start reading data with a limited time frame. The time limit
                // read from the data source must be multiplied by 1000 because
                // the method expects milliseconds as time limit
                Executor executor = Executor.getInstance();
                Object o = executor.performSynchronously( task, datasource.getRequestTimeLimit() * 1000 );
                response = handleResponse( o );
            } catch ( CancellationException e ) {
                // exception can't be re-thrown because responsible GetMapHandler
                // must collect all responses of all data sources
                String s = Messages.getMessage( "WMS_TIMEOUTDATASOURCE", new Integer( datasource.getRequestTimeLimit() ) );
                LOG.logError( s, e );
                if ( datasource.isFailOnException() ) {
                    OGCWebServiceException exce = new OGCWebServiceException( getClass().getName(), s );
                    response = exce;
                } else {
                    response = null;
                }
            } catch ( WMSExceptionFromWCS e ) {
                if ( datasource.isFailOnException() ) {
                    response = e;
                }
            } catch ( Throwable e ) {
                // exception can't be re-thrown because responsible GetMapHandler
                // must collect all responses of all data sources
                String s = Messages.getMessage( "WMS_ERRORDOSERVICE", e.getMessage() );
                LOG.logError( s, e );
                if ( datasource.isFailOnException() ) {
                    OGCWebServiceException exce = new OGCWebServiceException( getClass().getName(), s );
                    response = exce;
                } else {
                    response = null;
                }
            }
        }

        LOG.logDebug( "Layer " + layer.getName() + " returned." );

        return response;
    }

    /**
     * creates a getFeature request considering the getMap request and the filterconditions defined in the submitted
     * <tt>DataSource</tt> object. The request will be encapsualted within a <tt>OGCWebServiceEvent</tt>.
     * 
     * @param ds
     * @return GetFeature request object
     * @throws Exception
     */
    private GetFeature createGetFeatureRequest( LocalWFSDataSource ds )
                            throws Exception {

        Envelope bbox = transformBBOX( ds );

        LinkedList<StringBuffer> filters = new LinkedList<StringBuffer>(), sortProperties = new LinkedList<StringBuffer>();

        List<PropertyPath> pp = null;
        if ( style != null ) {
            List<UserStyle> styleList = new ArrayList<UserStyle>();
            styleList.add( style );
            pp = extractRequiredProperties( ds.getName(), styleList, scaleDen );
        } else {
            pp = new ArrayList<PropertyPath>();
        }
        PropertyPath geomPP = PropertyPathFactory.createPropertyPath( ds.getGeometryProperty() );
        if ( !pp.contains( geomPP ) ) {
            pp.add( geomPP );
        }

        // handling of vendor specific parameters "filter property" and "filter value"
        String filterProperty = request.getVendorSpecificParameter( "FILTERPROPERTY" );
        String filterValue = request.getVendorSpecificParameter( "FILTERVALUE" );

        boolean useCustomFilter = handler.getConfiguration().getDeegreeParams().getFiltersAllowed();
        useCustomFilter = useCustomFilter && filterProperty != null && filterValue != null;

        if ( useCustomFilter ) {
            LOG.logDebug( "Using custom filter on " + filterProperty + " = " + filterValue );
            PropertyPath path = createPropertyPath( new QualifiedName( filterProperty ) );
            if ( !pp.contains( path ) ) {
                pp.add( path );
            }
        }

        LOG.logDebug( "required properties: ", pp );
        Map<String, URI> namesp = extractNameSpaceDef( pp );

        // no filter condition has been defined
        StringBuffer sb = new StringBuffer( 5000 );
        sb.append( "<?xml version='1.0' encoding='" + CharsetUtils.getSystemCharset() + "'?>" );
        sb.append( "<GetFeature xmlns='http://www.opengis.net/wfs' " );
        sb.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
        sb.append( "xmlns:gml='http://www.opengis.net/gml' " );
        sb.append( "xmlns:" ).append( ds.getName().getPrefix() ).append( '=' );
        sb.append( "'" ).append( ds.getName().getNamespace() ).append( "' " );
        Iterator<String> iter = namesp.keySet().iterator();
        while ( iter.hasNext() ) {
            String pre = iter.next();
            URI nsp = namesp.get( pre );
            if ( !pre.equals( "xmlns" ) && !pre.equals( ds.getName().getPrefix() ) ) {
                sb.append( "xmlns:" ).append( pre ).append( "='" );
                sb.append( nsp.toASCIIString() ).append( "' " );
            }
        }

        sb.append( "service='WFS' version='1.1.0' " );
        if ( ds.getType() == AbstractDataSource.LOCALWFS ) {
            sb.append( "outputFormat='FEATURECOLLECTION'>" );
        } else {
            sb.append( "outputFormat='text/xml; subtype=gml/3.1.1'>" );
        }
        sb.append( "<Query typeName='" + ds.getName().getPrefixedName() + "'>" );

        // please note that this code CANNOT BE UNCOMMENTED as it severely affects performance in many cases!
        for ( int j = 0; j < pp.size(); j++ ) {
            if ( !pp.get( j ).getAsString().endsWith( "$SCALE" ) ) {
                sb.append( "<PropertyName>" ).append( pp.get( j ).getAsString() );
                sb.append( "</PropertyName>" );
            }
        }

        // add filters to list
        // BBOX filter
        StringBuffer sb2 = new StringBuffer( 512 );
        sb2.append( "<ogc:BBOX>" );
        sb2.append( "<ogc:PropertyName>" );
        sb2.append( ds.getGeometryProperty().getPrefixedName() );
        sb2.append( "</ogc:PropertyName>" );
        sb2.append( exportAsEnvelope( bbox ) );
        sb2.append( "</ogc:BBOX>" );
        filters.add( sb2 );
        // custom filter for property value
        if ( useCustomFilter ) {
            sb2 = new StringBuffer( 512 );
            appendCustomFilter( sb2, filterProperty, filterValue );
            filters.add( sb2 );
        }
        // filters of query
        Query query = ds.getQuery();
        if ( query != null ) {
            Filter filter = query.getFilter();
            filters.addAll( extractFilters( filter ) );
            SortProperty[] sps = query.getSortProperties();
            if ( sps != null ) {
                for ( SortProperty sp : sps ) {
                    sortProperties.add( extractSortProperty( sp ) );
                }
            }
        }
        // filters from SLD
        if ( layer != null ) {
            LayerFeatureConstraints lfc = layer.getLayerFeatureConstraints();
            if ( lfc != null ) {
                FeatureTypeConstraint[] fcs = lfc.getFeatureTypeConstraint();
                if ( fcs != null ) {
                    for ( FeatureTypeConstraint fc : fcs ) {
                        filters.addAll( extractFilters( fc.getFilter() ) );
                    }
                }
            }
        }

        // filters from the dimensions
        Layer lay = handler.getConfiguration().getLayer( layer.getName() );
        Map<String, String> dimProps = ds.getDimProps();
        if ( lay.getDimension() != null && dimProps != null ) {
            for ( Dimension dim : lay.getDimension() ) {
                if ( dim.getName().equals( "time" ) ) {
                    filters.add( handleDimension( request.getDimTime(), dim, dimProps.get( "time" ) ) );
                }
                if ( dim.getName().equals( "elevation" ) ) {
                    filters.add( handleDimension( request.getDimElev(), dim, dimProps.get( "elevation" ) ) );
                }
            }
        }

        // actually append the filters
        sb.append( "<ogc:Filter>" );
        if ( filters.size() > 1 ) {
            sb.append( "<ogc:And>" );
        }
        for ( StringBuffer s : filters ) {
            sb.append( s );
        }
        if ( filters.size() > 1 ) {
            sb.append( "</ogc:And>" );
        }
        sb.append( "</ogc:Filter>" );
        if ( sortProperties.size() > 0 ) {
            sb.append( "<ogc:SortBy>" );
            for ( StringBuffer s : sortProperties ) {
                sb.append( s );
            }
            sb.append( "</ogc:SortBy>" );
        }
        sb.append( "</Query></GetFeature>" );

        // create dom representation of the request
        Document doc = XMLTools.parse( new StringReader( sb.toString() ) );

        if ( LOG.isDebug() ) {
            LOG.logDebug( "GetFeature request: "
                          + new XMLFragment( doc, "http://www.systemid.org" ).getAsPrettyString() );
        }

        // create OGCWebServiceEvent object
        IDGenerator idg = IDGenerator.getInstance();
        GetFeature gfr = GetFeature.create( "" + idg.generateUniqueID(), doc.getDocumentElement() );

        return gfr;
    }

    private StringBuffer handleDimension( DimensionValues values, Dimension dim, String dimProp )
                            throws OGCWebServiceException {
        if ( values == null && dim.getDefaultValue() != null ) {
            values = new DimensionValues( dim.getDefaultValue() );
            request.warningHeaders.add( "99 Default value used: " + dim.getName() + "=" + dim.getDefaultValue() + " "
                                        + dim.getUnits() );
        }

        if ( values == null ) {
            throw new InvalidParameterValueException( "GetMap", get( "WMS_MISSING_DIMENSION_VALUE", dim.getName() ),
                                                      MISSINGDIMENSIONVALUE );
        }
        if ( !dim.isMultipleValues() && values.hasMultipleValues() ) {
            throw new InvalidParameterValueException( "GetMap", get( "WMS_NO_MULTIPLE_VALUES", dim.getName() ),
                                                      INVALIDDIMENSIONVALUE );
        }

        String singleValue = values.values.peek().value;
        if ( values.values.size() == 1 && singleValue != null ) {
            DimensionValues origValues = new DimensionValues( dim.getValues() );
            if ( !origValues.includesValue( singleValue ) ) {
                if ( dim.isNearestValue() ) {
                    String nearestValue = origValues.getNearestValue( singleValue );
                    values.values.peek().value = nearestValue;
                    request.warningHeaders.add( "99 Nearest value used: " + dim.getName() + "=" + nearestValue + " "
                                                + dim.getUnits() );
                }
            }
        }

        LinkedList<StringBuffer> filters = new LinkedList<StringBuffer>();

        for ( DimensionValue val : values.values ) {
            StringBuffer sb = new StringBuffer( 512 );
            if ( val.value != null ) {
                sb.append( "<ogc:PropertyIsEqualTo><ogc:PropertyName>" ).append( dimProp );
                sb.append( "</ogc:PropertyName><ogc:Literal>" ).append( val.value );
                sb.append( "</ogc:Literal></ogc:PropertyIsEqualTo>" );
            } else {
                sb.append( "<ogc:PropertyIsBetween><ogc:PropertyName>" ).append( dimProp );
                sb.append( "</ogc:PropertyName><ogc:LowerBoundary><ogc:Literal>" ).append( val.low );
                sb.append( "</ogc:Literal></ogc:LowerBoundary><ogc:UpperBoundary><ogc:Literal>" ).append( val.high );
                sb.append( "</ogc:Literal></ogc:UpperBoundary></ogc:PropertyIsBetween>" );
            }
            filters.add( sb );
        }

        if ( filters.size() == 1 ) {
            return filters.poll();
        }

        StringBuffer sb = new StringBuffer( 512 * filters.size() );
        sb.append( "<ogc:Or>" );
        while ( filters.size() > 0 ) {
            sb.append( filters.poll() );
        }
        sb.append( "</ogc:Or>" );
        return sb;
    }

    private static StringBuffer extractSortProperty( SortProperty sp ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "<ogc:SortProperty>" );
        sb.append( "<ogc:PropertyName>" );
        sb.append( sp.getSortProperty().toString() );
        sb.append( "</ogc:PropertyName>" );
        sb.append( "<ogc:SortOrder>" );
        sb.append( sp.getSortOrder() ? "ASC" : "DESC" );
        sb.append( "</ogc:SortOrder>" );
        sb.append( "</ogc:SortProperty>" );

        return sb;
    }

    private static LinkedList<StringBuffer> extractFilters( Filter filter ) {
        LinkedList<StringBuffer> filters = new LinkedList<StringBuffer>();

        if ( filter instanceof ComplexFilter ) {
            filters.add( ( ( (ComplexFilter) filter ).getOperation() ).to110XML() );
        }
        if ( filter instanceof FeatureFilter ) {
            ArrayList<FeatureId> featureIds = ( (FeatureFilter) filter ).getFeatureIds();
            for ( int i = 0; i < featureIds.size(); i++ ) {
                FeatureId fid = featureIds.get( i );
                filters.add( fid.toXML() );
            }
        }

        return filters;
    }

    private static void appendCustomFilter( StringBuffer sb, String name, String value ) {
        String[] vals = value.split( "," );
        if ( vals.length > 1 ) {
            sb.append( "<ogc:Or>" );
        }
        for ( String val : vals ) {
            sb.append( "<ogc:PropertyIsEqualTo><ogc:PropertyName>" );
            sb.append( name ).append( "</ogc:PropertyName><ogc:Literal>" );
            sb.append( val ).append( "</ogc:Literal></ogc:PropertyIsEqualTo>" );
        }
        if ( vals.length > 1 ) {
            sb.append( "</ogc:Or>" );
        }
    }

    /**
     * transforms the requested BBOX into the DefaultSRS of the assigned feature type
     * 
     * @param ds
     * @return the envelope
     * @throws OGCWebServiceException
     * @throws CRSTransformationException
     * @throws UnknownCRSException
     */
    private Envelope transformBBOX( LocalWFSDataSource ds )
                            throws OGCWebServiceException, CRSTransformationException, UnknownCRSException {
        Envelope bbox = request.getBoundingBox();
        // transform request bounding box to the coordinate reference
        // system the WFS holds the data if requesting CRS and WFS-Data
        // crs are different
        OGCWebService service = ds.getOGCWebService();
        WFSCapabilities capa;
        if ( service instanceof RemoteWFService ) {
            RemoteWFService wfs = (RemoteWFService) ds.getOGCWebService();
            capa = wfs.getWFSCapabilities();
        } else {
            WFService wfs = (WFService) service;
            capa = wfs.getCapabilities();
        }
        // WFSCapabilities capa = (WFSCapabilities)wfs.getWFSCapabilities();
        QualifiedName gn = ds.getName();
        WFSFeatureType ft = capa.getFeatureTypeList().getFeatureType( gn );

        if ( ft == null ) {
            throw new OGCWebServiceException( Messages.getMessage( "WMS_UNKNOWNFT", ds.getName() ) );
        }

        // enable different formatations of the crs encoding for GML geometries
        String GML_SRS = "http://www.opengis.net/gml/srs/";
        String old_gml_srs = ft.getDefaultSRS().toASCIIString();
        String old_srs;
        if ( old_gml_srs.startsWith( GML_SRS ) ) {
            old_srs = old_gml_srs.substring( 31 ).replace( '#', ':' ).toUpperCase();
        } else {
            old_srs = old_gml_srs;
        }

        String new_srs = request.getSrs();
        String new_gml_srs;
        if ( old_gml_srs.startsWith( GML_SRS ) ) {
            new_gml_srs = GML_SRS + new_srs.replace( ':', '#' ).toLowerCase();
        } else {
            new_gml_srs = new_srs;
        }

        if ( !( old_srs.equalsIgnoreCase( new_gml_srs ) ) ) {
            GeoTransformer transformer = new GeoTransformer( CRSFactory.create( old_srs ) );
            bbox = transformer.transform( bbox, this.handler.getRequestCRS() );
        }
        return bbox;
    }

    /**
     * creates a getCoverage request considering the getMap request and the filterconditions defined in the submitted
     * <tt>DataSource</tt> object The request will be encapsualted within a <tt>OGCWebServiceEvent</tt>.
     * 
     * @param ds
     *            the datasource
     * @param request
     *            the GetMap operation
     * @return GetCoverage request object
     * @throws InconsistentRequestException
     */
    protected static GetCoverage createGetCoverageRequest( AbstractDataSource ds, GetMap request )
                            throws InconsistentRequestException {

        Envelope bbox = request.getBoundingBox();

        double xres = bbox.getWidth() / request.getWidth();
        double yres = bbox.getHeight() / request.getHeight();

        WorldFile tmpWorldFile = new WorldFile( xres, yres, 0.0, 0.0, bbox, WorldFile.TYPE.OUTER );
        bbox = tmpWorldFile.getEnvelope( WorldFile.TYPE.CENTER );

        GetCoverage gcr = ( (LocalWCSDataSource) ds ).getGetCoverageRequest();

        String crs = request.getSrs();
        // if (gcr != null && gcr.getDomainSubset().getRequestSRS() != null) {
        // crs = gcr.getDomainSubset().getRequestSRS().getCode();
        // }
        String format = request.getFormat();
        int pos = format.indexOf( '/' );
        if ( pos > -1 )
            format = format.substring( pos + 1, format.length() );
        if ( gcr != null && !"%default%".equals( gcr.getOutput().getFormat().getCode() ) ) {
            format = gcr.getOutput().getFormat().getCode();
        }
        if ( format.indexOf( "svg" ) > -1 ) {
            format = "tiff";
        }
        if ( format.startsWith( "png" ) ) {
            format = "png";
        }

        String version = "1.0.0";
        if ( gcr != null && gcr.getVersion() != null ) {
            version = gcr.getVersion();
        }
        String lay = ds.getName().getPrefixedName();
        if ( gcr != null && !"%default%".equals( gcr.getSourceCoverage() ) ) {
            lay = gcr.getSourceCoverage();
        }
        String ipm = null;
        if ( gcr != null && gcr.getInterpolationMethod() != null ) {
            ipm = gcr.getInterpolationMethod().value;
        }

        // TODO
        // handle range sets e.g. time
        // note that elevation is now handled after the results are in (see the handleGetCoverageResponse method below)
        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "service=WCS&request=GetCoverage" );
        sb.append( "&version=" ).append( version );
        sb.append( "&COVERAGE=" ).append( lay );
        sb.append( "&crs=" ).append( crs );
        sb.append( "&response_crs=" ).append( crs );
        sb.append( "&BBOX=" ).append( bbox.getMin().getX() ).append( ',' );
        sb.append( bbox.getMin().getY() ).append( ',' ).append( bbox.getMax().getX() );
        sb.append( ',' ).append( bbox.getMax().getY() );
        sb.append( "&WIDTH=" ).append( request.getWidth() );
        sb.append( "&HEIGHT=" ).append( request.getHeight() );
        sb.append( "&FORMAT=" ).append( format );
        sb.append( "&INTERPOLATIONMETHOD=" ).append( ipm );
        try {
            IDGenerator idg = IDGenerator.getInstance();
            gcr = GetCoverage.create( "id" + idg.generateUniqueID(), sb.toString() );
        } catch ( WCSException e ) {
            throw new InconsistentRequestException( e.getMessage() );
        } catch ( org.deegree.ogcwebservices.OGCWebServiceException e ) {
            throw new InconsistentRequestException( e.getMessage() );
        }

        LOG.logDebug( "GetCoverage request: " + sb.toString() );

        return gcr;

    }

    /**
     * 
     * @param result
     * @return the response objects
     * @throws Exception
     */
    public Object handleResponse( Object result )
                            throws Exception {

        Object theme = null;
        if ( result instanceof ResultCoverage ) {
            theme = handleGetCoverageResponse( (ResultCoverage) result );
        } else if ( result instanceof FeatureResult ) {
            theme = handleGetFeatureResponse( (FeatureResult) result );
        } else if ( result instanceof GetMapResult ) {
            theme = handleGetMapResponse( (GetMapResult) result );
        } else {
            String s = Messages.getMessage( "WMS_UNKNOWNRESPONSEFORMAT" );
            if ( datasource.isFailOnException() ) {
                OGCWebServiceException exce = new OGCWebServiceException( getClass().getName(), s );
                theme = exce;
            }
        }
        return theme;
    }

    /**
     * replaces all pixels within the passed image having a color that is defined to be transparent within their
     * datasource with a transparent color.
     * 
     * @param img
     * @return modified image
     */
    private BufferedImage setTransparentColors( BufferedImage img ) {

        Color[] colors = null;
        if ( datasource.getType() == AbstractDataSource.LOCALWCS ) {
            LocalWCSDataSource ds = (LocalWCSDataSource) datasource;
            colors = ds.getTransparentColors();
        } else if ( datasource.getType() == AbstractDataSource.REMOTEWCS ) {
            RemoteWCSDataSource ds = (RemoteWCSDataSource) datasource;
            colors = ds.getTransparentColors();
        } else if ( datasource.getType() == AbstractDataSource.REMOTEWMS ) {
            RemoteWMSDataSource ds = (RemoteWMSDataSource) datasource;
            colors = ds.getTransparentColors();
        }

        if ( colors != null && colors.length > 0 ) {

            int[] clrs = new int[colors.length];
            for ( int i = 0; i < clrs.length; i++ ) {
                clrs[i] = colors[i].getRGB();
            }

            if ( img.getType() != BufferedImage.TYPE_INT_ARGB ) {
                // if the incoming image does not allow transparency
                // it must be copyed to a image of ARGB type
                BufferedImage tmp = new BufferedImage( img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB );
                Graphics g = tmp.getGraphics();
                g.drawImage( img, 0, 0, null );
                g.dispose();
                img = tmp;
            }

            // TODO
            // should be replaced by a JAI operation
            int w = img.getWidth();
            int h = img.getHeight();
            for ( int i = 0; i < w; i++ ) {
                for ( int j = 0; j < h; j++ ) {
                    int col = img.getRGB( i, j );
                    if ( shouldBeTransparent( colors, col ) ) {
                        img.setRGB( i, j, 0x00FFFFFF );
                    }
                }
            }

        }

        return img;
    }

    /**
     * @return true if the distance between the image color and at least of the colors to be truned to be transparent is
     *         less than 3 in an int RGB cube
     * 
     * @param colors
     * @param color
     */
    private boolean shouldBeTransparent( Color[] colors, int color ) {
        Color c2 = new Color( color );
        int r = c2.getRed();
        int g = c2.getGreen();
        int b = c2.getBlue();
        for ( int i = 0; i < colors.length; i++ ) {
            int r1 = colors[i].getRed();
            int g1 = colors[i].getGreen();
            int b1 = colors[i].getBlue();
            if ( Math.sqrt( ( r1 - r ) * ( r1 - r ) + ( g1 - g ) * ( g1 - g ) + ( b1 - b ) * ( b1 - b ) ) < 3 ) {
                return true;
            }
        }
        return false;
    }

    /**
     * handles the response of a cascaded WMS and calls a factory to create <tt>DisplayElement</tt> and a <tt>Theme</tt>
     * from it
     * 
     * @param response
     * @return the response objects
     * @throws Exception
     */
    private Object handleGetMapResponse( GetMapResult response )
                            throws Exception {

        BufferedImage bi = (BufferedImage) response.getMap();

        bi = setTransparentColors( bi );
        GridCoverage gc = new ImageGridCoverage( null, request.getBoundingBox(), bi );
        org.deegree.graphics.Layer rl = MapFactory.createRasterLayer( layer.getName(), gc );
        Theme theme = MapFactory.createTheme( datasource.getName().getPrefixedName(), rl, new UserStyle[] { style } );
        return theme;

    }

    /**
     * handles the response of a WFS and calls a factory to create <tt>DisplayElement</tt> and a <tt>Theme</tt> from it
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
        } else {
            throw new Exception( Messages.getMessage( "WMS_UNKNOWNDATAFORMATFT" ) );
        }
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "result: " + fc );
            for ( int i = 0; i < fc.size(); ++i ) {
                outputGeometries( fc.getFeature( i ) );
            }
        }

        org.deegree.graphics.Layer fl = MapFactory.createFeatureLayer( layer.getName(), this.handler.getRequestCRS(),
                                                                       fc );

        return MapFactory.createTheme( datasource.getName().getPrefixedName(), fl, new UserStyle[] { style } );
    }

    private void outputGeometries( Feature feature ) {
        if ( feature == null ) {
            return;
        }
        FeatureType ft = feature.getFeatureType();
        PropertyType[] propertyTypes = ft.getProperties();
        for ( PropertyType pt : propertyTypes ) {
            if ( pt.getType() == Types.FEATURE ) {
                FeatureProperty[] fp = feature.getProperties( pt.getName() );
                if ( fp != null ) {
                    for ( int i = 0; i < fp.length; i++ ) {
                        outputGeometries( (Feature) fp[i].getValue() );
                    }
                }
            } else if ( pt.getType() == Types.GEOMETRY ) {
                Geometry g = feature.getDefaultGeometryPropertyValue();
                if ( g != null ) {
                    try {
                        LOG.logDebug( "geometrie: " + WKTAdapter.export( g ).toString() );
                    } catch ( GeometryException e ) {
                        LOG.logDebug( "Geometry couldn't be converted to Well Known Text: " + g );
                    }
                }
            }
        }
    }

    /**
     * handles the response of a WCS and calls a factory to create <tt>DisplayElement</tt> and a <tt>Theme</tt> from it
     * 
     * @param response
     * @return the response objects
     * @throws Exception
     */
    private Object handleGetCoverageResponse( ResultCoverage response )
                            throws Exception {
        ImageGridCoverage gc = (ImageGridCoverage) response.getCoverage();
        Object ro = null;
        if ( gc != null ) {
            BufferedImage bi = gc.getAsImage( -1, -1 );

            bi = setTransparentColors( bi );

            gc = new ImageGridCoverage( null, request.getBoundingBox(), bi );

            Dimension[] dims = handler.getConfiguration().getLayer( layer.getName() ).getDimension();
            DimensionValues elev = request.getDimElev();
            if ( dims != null ) {
                for ( Dimension dim : dims ) {
                    if ( dim.getName().equalsIgnoreCase( "elevation" ) ) {
                        if ( elev == null && dim.getDefaultValue() != null ) {
                            request.setDimElev( new DimensionValues( dim.getDefaultValue() ) );
                            request.warningHeaders.add( "99 Default value used: " + dim.getName() + "="
                                                        + dim.getDefaultValue() + " " + dim.getUnits() );
                            elev = request.getDimElev();
                        }
                        if ( elev == null ) {
                            InvalidParameterValueException towrap;
                            towrap = new InvalidParameterValueException( "GetMap", get( "WMS_MISSING_DIMENSION_VALUE",
                                                                                        dim.getName() ),
                                                                         MISSINGDIMENSIONVALUE );
                            throw new WMSExceptionFromWCS( towrap );
                        }
                        if ( !dim.isMultipleValues() && elev.hasMultipleValues() ) {
                            InvalidParameterValueException towrap;
                            towrap = new InvalidParameterValueException( "GetMap", get( "WMS_NO_MULTIPLE_VALUES",
                                                                                        dim.getName() ),
                                                                         INVALIDDIMENSIONVALUE );
                            throw new WMSExceptionFromWCS( towrap );
                        }
                        String singleValue = elev.values.peek().value;
                        if ( elev.values.size() == 1 && singleValue != null ) {
                            DimensionValues origValues = new DimensionValues( dim.getValues() );
                            if ( !origValues.includesValue( singleValue ) ) {
                                if ( dim.isNearestValue() ) {
                                    String nearestValue = origValues.getNearestValue( singleValue );
                                    elev.values.peek().value = nearestValue;
                                    request.warningHeaders.add( "99 Nearest value used: " + dim.getName() + "="
                                                                + nearestValue + " " + dim.getUnits() );
                                }
                            }
                        }

                        if ( !new DimensionValues( dim.getValues() ).includes( elev ) ) {
                            InvalidParameterValueException towrap;
                            towrap = new InvalidParameterValueException( "GetMap", get( "WMS_BAD_DIMENSION_VALUE" ),
                                                                         INVALIDDIMENSIONVALUE );
                            throw new WMSExceptionFromWCS( towrap );
                        }
                    }
                }
            }
            org.deegree.graphics.Layer rl = MapFactory.createRasterLayer( layer.getName(), gc, request );

            ro = MapFactory.createTheme( datasource.getName().getPrefixedName(), rl, new UserStyle[] { style } );
        } else {
            throw new OGCWebServiceException( getClass().getName(), Messages.getMessage( "WMS_NOCOVERAGE",
                                                                                         datasource.getName() ) );
        }
        return ro;
    }

    class WMSExceptionFromWCS extends Exception {

        private static final long serialVersionUID = 8999003296940731523L;

        OGCWebServiceException wrapped;

        WMSExceptionFromWCS( OGCWebServiceException towrap ) {
            wrapped = towrap;
        }

    }

}
