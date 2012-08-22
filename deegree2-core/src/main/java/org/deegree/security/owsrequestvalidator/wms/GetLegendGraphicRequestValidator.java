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

import static org.deegree.security.drm.model.RightType.GETLEGENDGRAPHIC;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.graphics.sld.SLDFactory;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.security.UnauthorizedException;
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

class GetLegendGraphicRequestValidator extends AbstractWMSRequestValidator {

    // known condition parameter
    private static final String LAYER = "layers";

    private static final String SLD = "sld";

    private static final String INVALIDSLD = Messages.getString( "GetLegendGraphicRequestValidator.INVALIDSLD" );

    private static final String INVALIDLAYER = Messages.getString( "GetLegendGraphicRequestValidator.INVALIDLAYER" );

    private static final String INVALIDSTYLE = Messages.getString( "GetLegendGraphicRequestValidator.INVALIDSTYLE" );

    private static FeatureType glgFT = null;

    static {
        if ( glgFT == null ) {
            glgFT = GetLegendGraphicRequestValidator.createFeatureType();
        }
    }

    /**
     * @param policy
     */
    public GetLegendGraphicRequestValidator( Policy policy ) {
        super( policy );
    }

    /**
     * validates the incomming GetLegendGraphic request against the policy assigend to a validator
     *
     * @param request
     *            request to validate
     * @param user
     *            name of the user who likes to perform the request (can be null)
     */
    @Override
    public void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        userCoupled = false;
        Request req = policy.getRequest( "WMS", "GetLegendGraphic" );
        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPreConditions().isAny() ) {
            return;
        }
        Condition condition = req.getPreConditions();

        GetLegendGraphic wmsreq = (GetLegendGraphic) request;

        validateVersion( condition, wmsreq.getVersion() );
        validateLayer( condition, wmsreq.getLayer(), wmsreq.getStyle() );
        validateExceptions( condition, wmsreq.getExceptions() );
        validateFormat( condition, wmsreq.getFormat() );
        validateMaxWidth( condition, wmsreq.getWidth() );
        validateMaxHeight( condition, wmsreq.getHeight() );
        validateSLD( condition, wmsreq.getSLD() );

        if ( userCoupled ) {
            validateAgainstRightsDB( wmsreq, user );
        }

    }

    /**
     * validates if the requested layer is valid against the policy/condition. If the passed user <> null this is
     * checked against the user- and rights-management system/repository
     *
     * @param condition
     * @param layer
     * @throws InvalidParameterValueException
     */
    private void validateLayer( Condition condition, String layer, String style )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( LAYER );

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

        String vs = map.get( layer );

        if ( vs == null ) {
            if ( !op.isUserCoupled() ) {
                throw new InvalidParameterValueException( INVALIDLAYER + layer );
            }
            userCoupled = true;
        } else if ( !style.equalsIgnoreCase( "default" ) && vs.indexOf( "$any$" ) < 0 && vs.indexOf( style ) < 0 ) {
            if ( !op.isUserCoupled() ) {
                // a style is valid for a layer if it's the default style
                // or the layer accepts any style or a style is explicit defined
                // to be valid
                throw new InvalidParameterValueException( INVALIDSTYLE + layer + ':' + style );
            }
            userCoupled = true;
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

        if ( op == null && sldRef != null ) {
            throw new InvalidParameterValueException( INVALIDSLD + sldRef );
        }

        // sldRef is valid because no restrictions are made
        if ( sldRef == null || op.isAny() )
            return;

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

        try {
            SLDFactory.createSLD( sldRef );
        } catch ( XMLParsingException e ) {
            String s = org.deegree.i18n.Messages.getMessage( "WMS_SLD_IS_NOT_VALID", sldRef );
            throw new InvalidParameterValueException( s );
        }
    }

    /**
     * validates the passed WMS GetMap request against a User- and Rights-Management DB.
     *
     * @param wmsreq
     * @throws InvalidParameterValueException
     */
    private void validateAgainstRightsDB( GetLegendGraphic wmsreq, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( "no access to anonymous user" );
        }

        // create feature that describes the map request
        FeatureProperty[] fps = new FeatureProperty[7];
        fps[0] = FeatureFactory.createFeatureProperty( new QualifiedName( "version" ), wmsreq.getVersion() );
        fps[1] = FeatureFactory.createFeatureProperty( new QualifiedName( "width" ), new Integer( wmsreq.getWidth() ) );
        fps[2] = FeatureFactory.createFeatureProperty( new QualifiedName( "height" ), new Integer( wmsreq.getHeight() ) );
        fps[3] = FeatureFactory.createFeatureProperty( new QualifiedName( "format" ), wmsreq.getFormat() );
        fps[4] = FeatureFactory.createFeatureProperty( new QualifiedName( "exceptions" ), wmsreq.getExceptions() );
        fps[5] = FeatureFactory.createFeatureProperty( new QualifiedName( "sld" ), wmsreq.getSLD() );
        fps[6] = FeatureFactory.createFeatureProperty( new QualifiedName( "style" ), wmsreq.getStyle() );
        Feature feature = FeatureFactory.createFeature( "id", glgFT, fps );
        if ( securityConfig.getProxiedUrl() == null ) {
            handleUserCoupledRules( user, feature, wmsreq.getLayer(), "Layer", GETLEGENDGRAPHIC );
        } else {
            handleUserCoupledRules( user, feature, "[" + securityConfig.getProxiedUrl() + "]:" + wmsreq.getLayer(),
                                    "Layer", GETLEGENDGRAPHIC );
        }

    }

    /**
     * creates a feature type that matches the parameters of a GetLagendGraphic request
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createFeatureType() {
        PropertyType[] ftps = new PropertyType[7];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "version" ), Types.VARCHAR, false );
        ftps[1] = FeatureFactory.createSimplePropertyType( new QualifiedName( "width" ), Types.INTEGER, false );
        ftps[2] = FeatureFactory.createSimplePropertyType( new QualifiedName( "height" ), Types.INTEGER, false );
        ftps[3] = FeatureFactory.createSimplePropertyType( new QualifiedName( "format" ), Types.VARCHAR, false );
        ftps[4] = FeatureFactory.createSimplePropertyType( new QualifiedName( "exceptions" ), Types.VARCHAR, false );
        ftps[5] = FeatureFactory.createSimplePropertyType( new QualifiedName( "sld" ), Types.VARCHAR, false );
        ftps[6] = FeatureFactory.createSimplePropertyType( new QualifiedName( "style" ), Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "GetLegendGraphic", false, ftps );
    }

}
