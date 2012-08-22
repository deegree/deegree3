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
package org.deegree.security.owsrequestvalidator.wms;

import static org.deegree.security.drm.model.RightType.GETMAP;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ColorUtils;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.graphics.sld.AbstractStyle;
import org.deegree.graphics.sld.NamedLayer;
import org.deegree.graphics.sld.NamedStyle;
import org.deegree.graphics.sld.SLDFactory;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.Service;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.OperationParameter;
import org.deegree.security.owsproxy.Request;
import org.deegree.security.owsrequestvalidator.Messages;
import org.deegree.security.owsrequestvalidator.Policy;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 * 
 * @version 1.1, $Revision$, $Date$
 * 
 * @since 1.1
 */

public class GetMapRequestValidator extends AbstractWMSRequestValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( GetMapRequestValidator.class );

    // known condition parameter
    private static final String BBOX = "bbox";

    private static final String LAYERS = "layers";

    private static final String BGCOLOR = "bgcolor";

    private static final String TRANSPARENCY = "transparency";

    private static final String RESOLUTION = "resolution";

    private static final String SLD = "sld";

    private static final String INVALIDBBOX = Messages.getString( "GetMapRequestValidator.INVALIDBBOX" );

    private static final String INVALIDLAYER = Messages.getString( "GetMapRequestValidator.INVALIDLAYER" );

    private static final String INVALIDSTYLE = Messages.getString( "GetMapRequestValidator.INVALIDSTYLE" );

    private static final String INVALIDBGCOLOR = Messages.getString( "GetMapRequestValidator.INVALIDBGCOLOR" );

    private static final String INVALIDTRANSPARENCY = Messages.getString( "GetMapRequestValidator.INVALIDTRANSPARENCY" );

    private static final String INVALIDRESOLUTION = Messages.getString( "GetMapRequestValidator.INVALIDRESOLUTION" );

    private static final String INVALIDSLD = Messages.getString( "GetMapRequestValidator.INVALIDSLD" );

    private static final String MISSINGCRS = Messages.getString( "GetMapRequestValidator.MISSINGCRS" );

    private List<String> accessdRes = new ArrayList<String>();

    private static FeatureType mapFT = null;

    private GeoTransformer gt = null;

    static {
        if ( mapFT == null ) {
            mapFT = GetMapRequestValidator.createFeatureType();
        }
    }

    /**
     * @param policy
     */
    public GetMapRequestValidator( Policy policy ) {
        super( policy );
        try {
            gt = new GeoTransformer( "EPSG:4326" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * validates the incoming GetMap request against the policy assigned to a validator
     * 
     * @param request
     *            request to validate
     * @param user
     *            name of the user who likes to perform the request (can be null)
     */
    @Override
    public void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        accessdRes.clear();
        userCoupled = false;
        Request req = policy.getRequest( "WMS", "GetMap" );
        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPreConditions().isAny() ) {
            return;
        }
        Condition condition = req.getPreConditions();

        GetMap wmsreq = (GetMap) request;

        validateVersion( condition, wmsreq.getVersion() );
        Envelope env = wmsreq.getBoundingBox();
        try {
            env = gt.transform( env, wmsreq.getSrs() );
        } catch ( Exception e ) {
            throw new InvalidParameterValueException( "condition envelope isn't in the right CRS ", e );
        }
        validateBBOX( condition, env );
        validateLayers( condition, wmsreq.getLayers() );
        validateBGColor( condition, ColorUtils.toHexCode( "0x", wmsreq.getBGColor() ) );
        validateTransparency( condition, wmsreq.getTransparency() );
        validateExceptions( condition, wmsreq.getExceptions() );
        validateFormat( condition, wmsreq.getFormat() );
        validateMaxWidth( condition, wmsreq.getWidth() );
        validateMaxHeight( condition, wmsreq.getHeight() );
        validateResolution( condition, wmsreq );
        validateSLD( condition, wmsreq.getSLD_URL() );
        validateSLD_Body( condition, wmsreq.getStyledLayerDescriptor() );

        if ( userCoupled ) {
            validateAgainstRightsDB( wmsreq, user );
        }

    }

    /**
     * checks if the passed envelope is valid against the maximum bounding box defined in the policy. If
     * <tt>user</ff> != <tt>null</tt> the maximu valid BBOX will be read from the user/rights repository
     * 
     * @param condition
     *            condition containing the definition of the valid BBOX
     * @param envelope
     * @throws InvalidParameterValueException
     */
    private void validateBBOX( Condition condition, Envelope envelope )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( BBOX );

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        String v = op.getFirstAsString();
        String[] d = StringTools.toArray( v, ",", false );
        Envelope env = GeometryFactory.createEnvelope( Double.parseDouble( d[0] ), Double.parseDouble( d[1] ),
                                                       Double.parseDouble( d[2] ), Double.parseDouble( d[3] ), null );

        try {
            env = gt.transform( env, d[4] );
        } catch ( Exception e ) {
            throw new InvalidParameterValueException( MISSINGCRS, e );
        }

        if ( !env.contains( envelope ) ) {
            if ( !op.isUserCoupled() ) {
                // if not user coupled the validation has failed
                throw new InvalidParameterValueException( INVALIDBBOX + op.getFirstAsString() );
            }
            userCoupled = true;
            accessdRes.add( "BBOX: " + v );
        }
    }

    /**
     * checks if the passed layres/styles are valid against the layers/styles list defined in the policy. If
     * <tt>user</ff> != <tt>null</tt> the valid layers/styles will be read from the user/rights repository
     * 
     * @param condition
     *            condition containing the definition of the valid layers/styles
     * @param layers
     * @throws InvalidParameterValueException
     */
    private void validateLayers( Condition condition, GetMap.Layer[] layers )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( LAYERS );

        // version is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }

        List<String> v = op.getValues();
        // seperate layers from assigned styles
        Map<String, String> map = new HashMap<String, String>();
        for ( int i = 0; i < v.size(); i++ ) {
            String[] tmp = StringTools.toArray( v.get( i ), "|", false );
            map.put( tmp[0], tmp[1] );
        }

        for ( int i = 0; i < layers.length; i++ ) {
            String style = layers[i].getStyleName();
            String vs = map.get( layers[i].getName() );
            if ( vs == null ) {
                if ( !op.isUserCoupled() ) {
                    throw new InvalidParameterValueException( INVALIDLAYER + layers[i].getName() );
                }
                accessdRes.add( "Layers: " + layers[i].getName() );
                userCoupled = true;
            } else if ( !style.equalsIgnoreCase( "default" ) && vs.indexOf( "$any$" ) < 0 && vs.indexOf( style ) < 0 ) {
                // a style is valid for a layer if it's the default style
                // or the layer accepts any style or a style is explicit defined
                // to be valid
                if ( !op.isUserCoupled() ) {
                    throw new InvalidParameterValueException( INVALIDSTYLE + layers[i].getName() + ':' + style );
                }
                userCoupled = true;
                accessdRes.add( "Styles: " + style );
            }
        }

    }

    /**
     * checks if the passed bgcolor is valid against the bgcolor(s) defined in the policy. If
     * <tt>user</ff> != <tt>null</tt> the valid bgcolors will be read from the user/rights repository
     * 
     * @param condition
     *            condition containing the definition of the valid bgcolors
     * @param bgcolor
     * @throws InvalidParameterValueException
     */
    private void validateBGColor( Condition condition, String bgcolor )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( BGCOLOR );

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> list = op.getValues();

        if ( !list.contains( bgcolor ) ) {
            if ( !op.isUserCoupled() ) {
                throw new InvalidParameterValueException( INVALIDBGCOLOR + bgcolor );
            }
            accessdRes.add( "BGCOLOR" + bgcolor );
            userCoupled = true;
        }

    }

    /**
     * checks if the passed transparency is valid against the transparency defined in the policy. If
     * <tt>user</ff> != <tt>null</tt> the valid transparency will be read from the user/rights repository
     * 
     * @param condition
     *            condition containing the definition of the valid transparency
     * @param transparency
     * @throws InvalidParameterValueException
     */
    private void validateTransparency( Condition condition, boolean transparency )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( TRANSPARENCY );

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> v = op.getValues();
        String s = "" + transparency;
        if ( !v.get( 0 ).equals( s ) && !v.get( v.size() - 1 ).equals( s ) ) {
            if ( !op.isUserCoupled() ) {
                throw new InvalidParameterValueException( INVALIDTRANSPARENCY + transparency );
            }
            userCoupled = true;
            accessdRes.add( "Transparency: " + transparency );
        }

    }

    /**
     * checks if the requested map area/size is valid against the minimum resolution defined in the policy. If
     * <tt>user</ff> != <tt>null</tt> the valid resolution will be read from the user/rights repository
     * 
     * @param condition
     *            condition containing the definition of the valid resolution
     * @throws InvalidParameterValueException
     */
    private void validateResolution( Condition condition, GetMap gmr )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( RESOLUTION );

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        double scale = 0;
        try {
            scale = calcScale( gmr );
        } catch ( Exception e ) {
            throw new InvalidParameterValueException( StringTools.stackTraceToString( e ) );
        }
        double compareRes = 0;
        compareRes = op.getFirstAsDouble();
        if ( scale < compareRes ) {
            if ( !op.isUserCoupled() ) {
                throw new InvalidParameterValueException( INVALIDRESOLUTION + scale );
            }
            userCoupled = true;
            accessdRes.add( "resolution: " + scale );
        }
    }

    /**
     * checks if the passed reference to a SLD document is valid against the defined in the policy. If
     * <tt>user</ff> != <tt>null</tt> the valid sld reference addresses will be read from the user/rights repository
     * 
     * @param condition
     *            condition containing the definition of the valid sldRef
     * @param sldRef
     * @throws InvalidParameterValueException
     */
    private void validateSLD( Condition condition, URL sldRef )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( SLD );
        OperationParameter gmop = condition.getOperationParameter( LAYERS );

        if ( op == null && sldRef != null ) {
            throw new InvalidParameterValueException( INVALIDSLD + sldRef );
        }
        // sldRef is valid because no restrictions are made
        if ( sldRef == null || op.isAny() ) {
            return;
        }

        // validate reference base of the SLD
        List<String> list = op.getValues();
        String port = null;
        if ( sldRef.getPort() != -1 ) {
            port = ":" + sldRef.getPort();
        } else {
            port = ":80";
        }
        String addr = sldRef.getProtocol() + "://" + sldRef.getHost() + port;
        if ( !list.contains( addr ) ) {
            if ( !op.isUserCoupled() ) {
                throw new InvalidParameterValueException( INVALIDSLD + sldRef );
            }
            userCoupled = true;
        }

        // validate referenced dacument to be a valid SLD
        StyledLayerDescriptor sld = null;
        try {
            sld = SLDFactory.createSLD( sldRef );
        } catch ( XMLParsingException e ) {
            String s = org.deegree.i18n.Messages.getMessage( "WMS_SLD_IS_NOT_VALID", sldRef );
            throw new InvalidParameterValueException( s );
        }

        // validate NamedLayers referenced by the SLD
        NamedLayer[] nl = sld.getNamedLayers();
        List<String> v = gmop.getValues();
        // seperate layers from assigned styles
        Map<String, String> map = new HashMap<String, String>();
        for ( int i = 0; i < v.size(); i++ ) {
            String[] tmp = StringTools.toArray( v.get( i ), "|", false );
            map.put( tmp[0], tmp[1] );
        }
        if ( !userCoupled ) {
            for ( int i = 0; i < nl.length; i++ ) {
                AbstractStyle st = nl[i].getStyles()[0];
                String style = null;
                if ( st instanceof NamedStyle ) {
                    style = ( (NamedStyle) st ).getName();
                } else {
                    // use default as name if a UserStyle is defined
                    // to ensure that the style will be accepted by
                    // the validator
                    style = "default";
                }
                String vs = map.get( nl[i].getName() );
                if ( vs == null ) {
                    if ( !op.isUserCoupled() ) {
                        throw new InvalidParameterValueException( INVALIDLAYER + nl[i].getName() );
                    }
                    accessdRes.add( "Layers: " + nl[i].getName() );
                    userCoupled = true;
                } else if ( !style.equalsIgnoreCase( "default" ) && vs.indexOf( "$any$" ) < 0
                            && vs.indexOf( style ) < 0 ) {
                    // a style is valid for a layer if it's the default style
                    // or the layer accepts any style or a style is explicit defined
                    // to be valid
                    if ( !op.isUserCoupled() ) {
                        throw new InvalidParameterValueException( INVALIDSTYLE + nl[i].getName() + ':' + style );
                    }
                    userCoupled = true;
                    accessdRes.add( "Styles: " + style );
                }
            }
        }

    }

    /**
     * checks if the passed user is allowed to perform a GetMap request containing a SLD_BODY parameter.
     * 
     * @param condition
     *            condition containing when SLD_BODY is valid or nots
     * @param sld_body
     */
    private void validateSLD_Body( Condition condition, StyledLayerDescriptor sld_body ) {

        /*
         * 
         * OperationParameter op = condition.getOperationParameter( SLD_BODY ); // version is valid because no
         * restrictions are made if ( sld_body == null ||op.isAny() ) return; // at the moment it is just evaluated if
         * the user is allowed // to perform a SLD request or not. no content validation will // be made boolean
         * isAllowed = false; if ( op.isUserCoupled() ) { //TODO // get comparator list from security registry } if
         * (!isAllowed ) { throw new InvalidParameterValueException( INVALIDSLD_BODY ); }
         */
    }

    /**
     * validates the passed WMS GetMap request against a User- and Rights-Management DB.
     * 
     * @param wmsreq
     * @param user
     * @throws InvalidParameterValueException
     */
    private void validateAgainstRightsDB( GetMap wmsreq, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            StringBuffer sb = new StringBuffer( 1000 );
            sb.append( ' ' );
            for ( int i = 0; i < accessdRes.size(); i++ ) {
                sb.append( accessdRes.get( i ) ).append( "; " );
            }
            throw new UnauthorizedException( Messages.format( "RequestValidator.NOACCESS", sb ) );
        }

        Double scale = null;
        try {
            scale = new Double( calcScale( wmsreq ) );
        } catch ( Exception e ) {
            throw new InvalidParameterValueException( e );
        }

        // create feature that describes the map request
        FeatureProperty[] fps = new FeatureProperty[11];
        fps[0] = FeatureFactory.createFeatureProperty( new QualifiedName( "version" ), wmsreq.getVersion() );
        fps[1] = FeatureFactory.createFeatureProperty( new QualifiedName( "width" ), new Integer( wmsreq.getWidth() ) );
        fps[2] = FeatureFactory.createFeatureProperty( new QualifiedName( "height" ), new Integer( wmsreq.getHeight() ) );
        Envelope env = wmsreq.getBoundingBox();
        try {
            env = gt.transform( env, wmsreq.getSrs() );
        } catch ( Exception e ) {
            throw new InvalidParameterValueException( "A:condition envelope isn't in the right CRS ", e );
        }
        Object geom = null;
        try {
            geom = GeometryFactory.createSurface( env, null );
        } catch ( Exception e1 ) {
            e1.printStackTrace();
        }
        fps[3] = FeatureFactory.createFeatureProperty( new QualifiedName( "GEOM" ), geom );
        fps[4] = FeatureFactory.createFeatureProperty( new QualifiedName( "format" ), wmsreq.getFormat() );
        fps[5] = FeatureFactory.createFeatureProperty( new QualifiedName( "bgcolor" ),
                                                       ColorUtils.toHexCode( "0x", wmsreq.getBGColor() ) );
        fps[6] = FeatureFactory.createFeatureProperty( new QualifiedName( "transparent" ),
                                                       "" + wmsreq.getTransparency() );
        fps[7] = FeatureFactory.createFeatureProperty( new QualifiedName( "exceptions" ), wmsreq.getExceptions() );
        fps[8] = FeatureFactory.createFeatureProperty( new QualifiedName( "resolution" ), scale );
        fps[9] = FeatureFactory.createFeatureProperty( new QualifiedName( "sld" ), wmsreq.getSLD_URL() );

        GetMap.Layer[] layers = wmsreq.getLayers();
        for ( int i = 0; i < layers.length; i++ ) {
            fps[10] = FeatureFactory.createFeatureProperty( new QualifiedName( "style" ), layers[i].getStyleName() );
            Feature feature = FeatureFactory.createFeature( "id", mapFT, fps );

            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                for ( int ii = 0; ii < fps.length; ii++ ) {
                    LOG.logDebug( "compared property: ", fps[ii] );
                }
            }

            if ( securityConfig.getProxiedUrl() == null ) {
                handleUserCoupledRules( user, feature, layers[i].getName(), "Layer", GETMAP );
            } else {
                handleUserCoupledRules( user, feature,
                                        "[" + securityConfig.getProxiedUrl() + "]:" + layers[i].getName(), "Layer",
                                        GETMAP );
                // handle custom constraints (currently only maxwidth/height)
                try {
                    SecurityAccessManager sam = SecurityAccessManager.getInstance();
                    SecurityAccess access = sam.acquireAccess( user );
                    Service service = access.getServiceByAddress( securityConfig.getProxiedUrl() );
                    boolean authorized = false;
                    for ( Role r : user.getRoles( access ) ) {
                        String constr = access.getConstraints( r, service );
                        if ( constr == null ) {
                            authorized = true;
                            break;
                        }
                        Pattern p = Pattern.compile( "maxWidth: ([0-9]+), maxHeight: ([0-9]+)" );
                        Matcher m = p.matcher( constr );
                        if ( m.find() ) {
                            int width = Integer.valueOf( m.group( 1 ) );
                            int height = Integer.valueOf( m.group( 2 ) );
                            if ( width == 0 && height == 0 ) {
                                authorized = true;
                                break;
                            }
                            if ( width >= wmsreq.getWidth() && height >= wmsreq.getHeight() ) {
                                authorized = true;
                                break;
                            }
                        }
                    }
                    if ( !authorized ) {
                        throw new UnauthorizedException( Messages.getString( "RequestValidator.UNAUTORIZEDACCESS" ) );
                    }
                } catch ( UnauthorizedException e ) {
                    throw e;
                } catch ( Throwable e ) {
                    throw new UnauthorizedException( e );
                }
            }
        }

    }

    /**
     * calculates the map scale as defined in the OGC WMS 1.1.1 specifications
     * 
     * @return scale of the map
     */
    private static double calcScale( GetMap request )
                            throws Exception {

        Envelope bbox = request.getBoundingBox();

        CoordinateSystem crs = CRSFactory.create( request.getSrs() );
        return MapUtils.calcScale( request.getWidth(), request.getHeight(), bbox, crs, 1 );
        // would return scale denominator
        // return MapUtils.calcScale( request.getWidth(), request.getHeight(), bbox, crs, DEFAULT_PIXEL_SIZE );
    }

    private static FeatureType createFeatureType() {
        PropertyType[] ftps = new PropertyType[11];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "version" ), Types.VARCHAR, false );
        ftps[1] = FeatureFactory.createSimplePropertyType( new QualifiedName( "width" ), Types.INTEGER, false );
        ftps[2] = FeatureFactory.createSimplePropertyType( new QualifiedName( "height" ), Types.INTEGER, false );
        ftps[3] = FeatureFactory.createSimplePropertyType( new QualifiedName( "GEOM" ), Types.GEOMETRY, false );
        ftps[4] = FeatureFactory.createSimplePropertyType( new QualifiedName( "format" ), Types.VARCHAR, false );
        ftps[5] = FeatureFactory.createSimplePropertyType( new QualifiedName( "bgcolor" ), Types.VARCHAR, false );
        ftps[6] = FeatureFactory.createSimplePropertyType( new QualifiedName( "transparent" ), Types.VARCHAR, false );
        ftps[7] = FeatureFactory.createSimplePropertyType( new QualifiedName( "exceptions" ), Types.VARCHAR, false );
        ftps[8] = FeatureFactory.createSimplePropertyType( new QualifiedName( "resolution" ), Types.DOUBLE, false );
        ftps[9] = FeatureFactory.createSimplePropertyType( new QualifiedName( "sld" ), Types.VARCHAR, false );
        ftps[10] = FeatureFactory.createSimplePropertyType( new QualifiedName( "style" ), Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "GetMap", false, ftps );
    }

}
