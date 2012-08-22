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
package org.deegree.ogcwebservices.wcs.getcoverage;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.time.TimeSequence;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.coverage.grid.Grid;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcbase.GMLDocument;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wcs.InterpolationMethod;
import org.deegree.ogcwebservices.wcs.WCSException;
import org.deegree.ogcwebservices.wcs.WCSRequestBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * encapsulates a WCS GetCoverage request
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class GetCoverage extends WCSRequestBase {

    private static final ILogger LOG = LoggerFactory.getLogger( GetCoverage.class );

    private static final long serialVersionUID = 44735033754048955L;

    private static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private String sourceCoverage = null;

    private DomainSubset domainSubset = null;

    private RangeSubset rangeSubset = null;

    private InterpolationMethod interpolationMethod = null;

    private Output output = null;

    /**
     * @param id
     * @param version
     * @param sourceCoverage
     * @param domainSubset
     * @param output
     * @throws WCSException
     * @throws OGCWebServiceException
     */
    public GetCoverage( String id, String version, String sourceCoverage, DomainSubset domainSubset, Output output )
                            throws WCSException, OGCWebServiceException {
        this( id, version, sourceCoverage, domainSubset, null, null, output );
    }

    /**
     * @param id
     * @param version
     * @param sourceCoverage
     * @param domainSubset
     * @param interpolationMethod
     * @param output
     * @throws WCSException
     * @throws OGCWebServiceException
     */
    public GetCoverage( String id, String version, String sourceCoverage, DomainSubset domainSubset,
                        InterpolationMethod interpolationMethod, Output output ) throws WCSException,
                            OGCWebServiceException {
        this( id, version, sourceCoverage, domainSubset, null, interpolationMethod, output );
    }

    /**
     * @param id
     * @param version
     * @param sourceCoverage
     * @param domainSubset
     * @param rangeSubset
     * @param output
     * @throws WCSException
     * @throws OGCWebServiceException
     */
    public GetCoverage( String id, String version, String sourceCoverage, DomainSubset domainSubset,
                        RangeSubset rangeSubset, Output output ) throws WCSException, OGCWebServiceException {
        this( id, version, sourceCoverage, domainSubset, rangeSubset, null, output );
    }

    /**
     * @param id
     * @param version
     * @param sourceCoverage
     * @param domainSubset
     * @param rangeSubset
     * @param interpolationMethod
     * @param output
     * @throws WCSException
     * @throws OGCWebServiceException
     */
    public GetCoverage( String id, String version, String sourceCoverage, DomainSubset domainSubset,
                        RangeSubset rangeSubset, InterpolationMethod interpolationMethod, Output output )
                            throws WCSException, OGCWebServiceException {
        super( id, version );
        if ( sourceCoverage == null || sourceCoverage.length() == 0 ) {
            throw new WCSException( "sourceCoverage must be a valid string with length > 0" );
        }
        if ( domainSubset == null ) {
            throw new WCSException( "domainSubset must be <> null in GetCoverage" );
        }
        if ( output == null ) {
            throw new WCSException( "output must be <> null in GetCoverage" );
        }
        this.sourceCoverage = sourceCoverage;
        this.domainSubset = domainSubset;
        this.rangeSubset = rangeSubset;
        this.interpolationMethod = interpolationMethod;
        this.output = output;
    }

    /**
     * creates a GetCoverage request from its KVP representation
     *
     * @param id
     *            unique ID of the request
     * @param kvp
     *            request
     * @return created <tt>GetCoverage</tt>
     * @throws OGCWebServiceException
     *             will be thrown if something general is wrong
     * @throws WCSException
     *             will be thrown if a WCS/GetCoverage specific part of the request is erroreous
     */
    public static GetCoverage create( String id, String kvp )
                            throws OGCWebServiceException, WCSException {
        Map<String, String> map = KVP2Map.toMap( kvp );
        map.put( "ID", id );
        return create( map );
    }

    /**
     * creates a GetCoverage request from its KVP representation
     *
     * @param map
     *            request
     * @return created <tt>GetCoverage</tt>
     * @throws OGCWebServiceException
     *             will be thrown if something general is wrong
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     * @throws WCSException
     *             will be thrown if a WCS/GetCoverage specific part of the request is erroreous
     */
    public static GetCoverage create( Map<String, String> map )
                            throws OGCWebServiceException, MissingParameterValueException,
                            InvalidParameterValueException {

        String version = map.remove( "VERSION" );
        if ( version == null ) {
            throw new MissingParameterValueException( "WCS", "'version' must be set" );
        }
        if ( !"1.0.0".equals( version ) ) {
            ExceptionCode ecode = ExceptionCode.INVALIDPARAMETERVALUE;
            throw new InvalidParameterValueException( "WCS", "'version' must be 1.0.0", ecode );
        }
        String coverage = map.remove( "COVERAGE" );
        String crs = map.remove( "CRS" );
        if ( crs == null ) {
            ExceptionCode code = ExceptionCode.MISSINGPARAMETERVALUE;
            throw new MissingParameterValueException( "WCS", "'crs' is missing", code );
        }
        String response_crs = map.remove( "RESPONSE_CRS" );
        if ( response_crs == null ) {
            response_crs = crs;
        }
        String format = map.remove( "FORMAT" );
        Output output = createOutput( response_crs, null, format, null );
        SpatialSubset sps = createSpatialSubset( map, crs );

        String time = map.remove( "TIME" );
        TimeSequence temporalSubset = null;
        if ( time != null ) {
            temporalSubset = new TimeSequence( time );
        }

        Code code = new Code( crs, null );
        DomainSubset domainSubset = new DomainSubset( code, sps, temporalSubset );

        String except = map.remove( "EXCEPTIONS" );
        if ( except == null ) {
            except = "application/vnd.ogc.se_xml";
        } else if ( !except.equals( "application/vnd.ogc.se_xml" ) ) {
            ExceptionCode ecode = ExceptionCode.INVALIDPARAMETERVALUE;
            throw new InvalidParameterValueException( "WCS", "exceptions != application/vnd.ogc.se_xml", ecode );
        }
        String id = map.remove( "ID" );

        GetCoverage gc = new GetCoverage( id, version, coverage, domainSubset, null, null, output );
        gc.validate();
        return gc;
    }

    /**
     * creates a GetCoverage request from its XML representation
     *
     * @param id
     *            unique ID of the request
     * @param doc
     *            XML representation of the request
     * @return created <tt>DescribeCoverage</tt>
     * @throws OGCWebServiceException
     *             will be thrown if something general is wrong
     * @throws WCSException
     *             will be thrown if a WCS/GetCoverage specific part of the request is erroreous
     */
    public static GetCoverage create( String id, Document doc )
                            throws OGCWebServiceException, WCSException {

        GetCoverage gc = null;
        try {

            String version = XMLTools.getNodeAsString( doc, "/wcs:GetCoverage/@version", nsContext, null );
            if ( version == null ) {
                throw new MissingParameterValueException( "WCS", "'version' must be set" );
            }
            if ( !"1.0.0".equals( version ) ) {
                ExceptionCode ecode = ExceptionCode.INVALIDPARAMETERVALUE;
                throw new InvalidParameterValueException( "WCS", "'version' must be 1.0.0", ecode );
            }

            String coverage = XMLTools.getRequiredNodeAsString( doc, "/wcs:GetCoverage/wcs:sourceCoverage", nsContext );
            String interpol = XMLTools.getNodeAsString( doc, "/wcs:GetCoverage/wcs:interpolationMethod", nsContext,
                                                        null );
            InterpolationMethod interpolMeth = null;
            if ( interpol == null || "nearest neighbor".equals( interpol ) ) {
                interpolMeth = new InterpolationMethod( "nearest neighbor" );
            }
            String path = "/wcs:GetCoverage/wcs:domainSubset/wcs:spatialSubset";
            List<Node> nl = XMLTools.getNodes( doc, path, nsContext );
            SpatialSubset sp = null;
            if ( nl.size() > 0 ) {
                Node node = (Node) nl.get( 0 );
                sp = createSpatialSubset( (Element) node );
            } else {
                // TODO
                // temporal subset
            }
            // TODO
            // path = "/wcs:GetCoverage/wcs:rangeSubset/wcs:axisSubset";
            // nl = XMLTools.getXPath(path, doc, nsContext);
            // evaluate possible ranges; e.g.time, extent
            String format = XMLTools.getRequiredNodeAsString( doc, "/wcs:GetCoverage/wcs:output/wcs:format", nsContext );
            // use crs defined for the requested envelope if no CRS is defined
            // in the request
            String crsName = "EPSG:4326";
            if ( sp.getEnvelope().getCoordinateSystem() != null ) {
                crsName = sp.getEnvelope().getCoordinateSystem().getPrefixedName();
            }
            String crs = XMLTools.getNodeAsString( doc, "/wcs:GetCoverage/wcs:output/wcs:crs", nsContext, crsName );

            String ipm = XMLTools.getNodeAsString( doc, "/wcs:GetCoverage/wcs:interpolationMethod", nsContext, null );
            if ( ipm != null && !ipm.equals( "nearest neighbor" ) ) {
                throw new InvalidParameterValueException( "interpolationMethod must "
                                                          + "have the value 'nearest neighbor'" );
            }

            Output output = createOutput( crs, null, format, null );
            DomainSubset domainSubset = new DomainSubset( new Code( crsName ), sp );

            gc = new GetCoverage( id, version, coverage, domainSubset, null, interpolMeth, output );
        } catch ( Exception e ) {
            ExceptionCode code = ExceptionCode.INVALID_FORMAT;
            throw new WCSException( "WCS", StringTools.stackTraceToString( e ), code );
        }

        gc.validate();
        return gc;
    }

    /**
     * @param element
     * @return a new Spatial subset
     * @throws WCSException
     */
    private static SpatialSubset createSpatialSubset( Element element )
                            throws WCSException {
        SpatialSubset sp = null;
        try {
            List<Node> nl = XMLTools.getNodes( element, "gml:Envelope", nsContext );
            Envelope env = GMLDocument.parseEnvelope( (Element) nl.get( 0 ) );
            nl = XMLTools.getNodes( element, "gml:Grid", nsContext );
            Grid grid = GMLDocument.parseGrid( (Element) nl.get( 0 ) );
            sp = new SpatialSubset( env, grid.getGridEnvelope() );
        } catch ( Exception e ) {
            ExceptionCode code = ExceptionCode.INVALID_FORMAT;
            throw new WCSException( "WCS", StringTools.stackTraceToString( e ), code );
        }
        return sp;
    }

    /**
     * @param map
     * @param crs
     * @return a new SpatialSubset with given crs
     * @throws WCSException
     */
    public static final SpatialSubset createSpatialSubset( Map<String,String> map, String crs )
                            throws WCSException {
        Envelope envelope = createEnvelope( map, crs );

        int width = (int) getNumber( (String) map.remove( "WIDTH" ), "WIDTH" );
        int height = (int) getNumber( (String) map.remove( "HEIGHT" ), "HEIGHT" );
        int depth = (int) getNumber( (String) map.remove( "DEPTH" ), "DEPTH" );

        double resx = getNumber( (String) map.remove( "RESX" ), "RESX" );
        double resy = getNumber( (String) map.remove( "RESY" ), "RESY" );
        double resz = getNumber( (String) map.remove( "RESZ" ), "RESZ" );

        Position low = null;
        Position high = null;
        if ( width > 0 && height > 0 ) {
            if ( depth > 0 ) {
                low = GeometryFactory.createPosition( 0, 0, 0 );
                high = GeometryFactory.createPosition( width - 1, height - 1, depth );
            } else {
                low = GeometryFactory.createPosition( 0, 0 );
                high = GeometryFactory.createPosition( width - 1, height - 1 );
            }
        } else if ( resx > 0 && resy > 0 ) {
            if ( resz > 0 ) {
                ExceptionCode code = ExceptionCode.INVALIDPARAMETERVALUE;
                throw new WCSException( "WCS", "resz is not supported yet", code );
            }
            width = (int) Math.round( envelope.getWidth() / resx );
            height = (int) Math.round( envelope.getHeight() / resy );
            low = GeometryFactory.createPosition( 0, 0 );
            high = GeometryFactory.createPosition( width, height );
        } else {
            ExceptionCode code = ExceptionCode.MISSINGPARAMETERVALUE;
            throw new WCSException( "WCS", "width/height or resx/resy must be set", code );
        }

        Envelope grid = GeometryFactory.createEnvelope( low, high, null );

        return new SpatialSubset( envelope, grid );

    }

    /**
     * @param map
     * @return an envelope.
     * @throws WCSException
     */
    private static Envelope createEnvelope( Map<String,String> map, String crs )
                            throws WCSException {
        String tmp = (String) map.remove( "BBOX" );
        double[] bbox = null;
        if ( tmp != null ) {
            try {
                bbox = StringTools.toArrayDouble( tmp, "," );
            } catch ( Exception e ) {
                ExceptionCode code = ExceptionCode.INVALIDPARAMETERVALUE;
                throw new WCSException( "WCS", "can't read BBOX", code );
            }

            Position min = null;
            Position max = null;
            if ( bbox.length == 4 ) {
                min = GeometryFactory.createPosition( bbox[0], bbox[1] );
                max = GeometryFactory.createPosition( bbox[2], bbox[3] );
            } else {
                min = GeometryFactory.createPosition( bbox[0], bbox[1], bbox[2] );
                max = GeometryFactory.createPosition( bbox[3], bbox[4], bbox[5] );
            }
            CoordinateSystem srs;
            try {
                srs = CRSFactory.create( crs );
            } catch ( UnknownCRSException e ) {
                throw new WCSException( GetCoverage.class.getName(), e.getMessage() );
            }
            return GeometryFactory.createEnvelope( min, max, srs );
        }
        return null;

    }

    /**
     * creates an <tt>Output</tt> object for a GetCoverage request
     *
     * @param response_crs
     * @param crsNS
     * @param format
     * @param formatNS
     * @return an Output
     * @throws WCSException
     *             will be thrown if the response_crs prefix isn't a valid URI
     */
    public static final Output createOutput( String response_crs, String crsNS, String format, String formatNS )
                            throws WCSException {
        URI crsURI = null;
        if ( crsNS != null ) {
            try {
                crsURI = new URI( crsNS );
            } catch ( Exception e ) {
                throw new WCSException( "invalid response crs namespace: " + crsNS );
            }
        }

        URI formatURI = null;
        if ( formatNS != null ) {
            try {
                formatURI = new URI( formatNS );
            } catch ( Exception e ) {
                throw new WCSException( "invalid response crs namespace: " + formatNS );
            }
        }

        Code crs = new Code( response_crs, crsURI );
        Code cformat = new Code( format, formatURI );
        return new Output( crs, cformat );
    }

    /**
     * @param val
     * @param name
     * @return a Number
     * @throws WCSException
     */
    private static double getNumber( String val, String name )
                            throws WCSException {
        if ( val == null )
            return -1;
        double d = -1;
        try {
            d = Double.parseDouble( val );
        } catch ( Exception e ) {
            ExceptionCode code = ExceptionCode.INVALIDPARAMETERVALUE;
            throw new WCSException( "WCS", name + " isn't a valid number format", code );
        }
        return d;
    }

    /**
     * @return Returns the domainSubset.
     */
    public DomainSubset getDomainSubset() {
        return domainSubset;
    }

    /**
     * @return Returns the interpolationMethod.
     */
    public InterpolationMethod getInterpolationMethod() {
        return interpolationMethod;
    }

    /**
     * @return Returns the output.
     */
    public Output getOutput() {
        return output;
    }

    /**
     * @return Returns the rangeSubset.
     */
    public RangeSubset getRangeSubset() {
        return rangeSubset;
    }

    /**
     * @return Returns the sourceCoverage.
     *
     */
    public String getSourceCoverage() {
        return sourceCoverage;
    }

    /**
     * @throws WCSException
     */
    protected void validate()
                            throws WCSException {

        if ( getVersion() == null ) {
            ExceptionCode code = ExceptionCode.MISSINGPARAMETERVALUE;
            throw new WCSException( "WCS", "'version' is missing", code );
        }

        if ( getSourceCoverage() == null ) {
            ExceptionCode code = ExceptionCode.MISSINGPARAMETERVALUE;
            throw new WCSException( "WCS", "'coverage' is missing", code );
        }

        DomainSubset ds = getDomainSubset();
        if ( ds.getRequestSRS() == null ) {
            ExceptionCode code = ExceptionCode.MISSINGPARAMETERVALUE;
            throw new WCSException( "WCS", "'crs' is missing", code );
        }

        if ( ds.getSpatialSubset() == null && ds.getTemporalSubset() == null ) {
            ExceptionCode code = ExceptionCode.MISSINGPARAMETERVALUE;
            throw new WCSException( "WCS", "either temporal subset or spatial " + "subset must be defined", code );
        }

        if ( getOutput().getFormat() == null ) {
            ExceptionCode code = ExceptionCode.MISSINGPARAMETERVALUE;
            throw new WCSException( "WCS", "'format' is missing", code );
        }

    }



    @Override
    public String getRequestParameter()
                            throws OGCWebServiceException {
        StringBuffer sb = new StringBuffer(1000);
        sb.append( "REQUEST=GetCoverage&VERSION=1.0.0&coverage=" );
        sb.append( getSourceCoverage() ).append( "&TRANSPARENT=true&Format=" );
        sb.append( getOutput().getFormat().getCode() ).append( "&EXCEPTIONS=application/vnd.ogc.se_xml&Width=" );
        Envelope grid = (Envelope)getDomainSubset().getSpatialSubset().getGrid();
        sb.append( Math.round( grid.getWidth( )) ).append( "&height=" ).append( Math.round( grid.getHeight() ) );
        sb.append( "&crs=" ).append( getOutput().getCrs().getCode() ).append( "&bbox=" );
        Envelope bbox = getDomainSubset().getSpatialSubset().getEnvelope();
        sb.append( bbox.getMin().getX() ).append( ',' ).append( bbox.getMin().getY() ).append( ',' );
        sb.append( bbox.getMax().getX() ).append( ',' ).append( bbox.getMax().getY() );

        LOG.logDebug( "GetCoverage request parameters", sb  );

        return sb.toString();

    }

    @Override
    public String toString() {
        String response = super.toString();
        response += "\nOutput: " + output;
        response += "\ndomainSubset: " + domainSubset;
        response += "\nsourceCoverage: " + sourceCoverage;
        response += "\ninterpolationMethod: " + interpolationMethod;
        return response;
    }

}
