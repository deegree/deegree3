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

import static org.deegree.security.drm.model.RightType.GETFEATUREINFO;

import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.OperationParameter;
import org.deegree.security.owsproxy.Request;
import org.deegree.security.owsrequestvalidator.Messages;
import org.deegree.security.owsrequestvalidator.Policy;
import org.deegree.security.owsrequestvalidator.RequestValidator;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.1, $Revision$, $Date$
 *
 * @since 1.1
 */

class GetFeatureInfoRequestValidator extends RequestValidator {

    // known condition parameter
    private static final String INFOLAYERS = "featureInfoLayers";

    private static final String INFOFORMAT = "infoFormat";

    private static final String FEATURECOUNT = "maxFeatureCount";

    private static final String INVALIDCLICKPOINT = Messages.getString( "GetFeatureInfoRequestValidator.INVALIDCLICKPOINT" );

    private static final String INVALIDLAYER = Messages.getString( "GetFeatureInfoRequestValidator.INVALIDLAYER" );

    private static final String INVALIDFORMAT = Messages.getString( "GetFeatureInfoRequestValidator.INVALIDFORMAT" );

    private static final String INAVLIDFEATURECOUNT = Messages.getString( "GetFeatureInfoRequestValidator.INAVLIDFEATURECOUNT" );

    private static FeatureType gfiFT = null;

    static {
        if ( gfiFT == null ) {
            gfiFT = GetFeatureInfoRequestValidator.createFeatureType();
        }
    }

    /**
     * @param policy
     */
    public GetFeatureInfoRequestValidator( Policy policy ) {
        super( policy );
    }

    /**
     * validates the incoming GetFeatureInfo request against the policy assigned to a validator
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
        Request req = policy.getRequest( "WMS", "GetFeatureInfo" );
        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPreConditions().isAny() ) {
            return;
        }
        Condition condition = req.getPreConditions();

        GetFeatureInfo wmsreq = (GetFeatureInfo) request;

        // validate the GetMap request contained in the
        // GetFeatureInfo request
        GetMapRequestValidator gmrv = new GetMapRequestValidator( policy );
        GetMap gmr = wmsreq.getGetMapRequestCopy();
        gmrv.validateRequest( gmr, user );

        validateXY( gmr, wmsreq );
        validateInfoLayers( condition, wmsreq.getQueryLayers() );
        validateInfoFormat( condition, wmsreq.getInfoFormat() );
        validateFeatureCount( condition, wmsreq.getFeatureCount() );

        if ( userCoupled ) {
            validateAgainstRightsDB( wmsreq, user );
        }

    }

    /**
     * validates the click point (x,y coordinate) to be located within the map image that has been base of the
     * GetFeatureInfo request
     *
     * @param gmr
     * @param gfir
     * @throws InvalidParameterValueException
     */
    private void validateXY( GetMap gmr, GetFeatureInfo gfir )
                            throws InvalidParameterValueException {

        int x = gfir.getClickPoint().x;
        int y = gfir.getClickPoint().y;

        int width = gmr.getWidth();
        int height = gmr.getHeight();

        if ( x < 0 || x >= width || y < 0 || y >= height ) {
            throw new InvalidParameterValueException( INVALIDCLICKPOINT );
        }

    }

    /**
     * validates if the requested info layers layers are valid against the policy/condition. If the passed user <>null
     * this is checked against the user- and rights-management system/repository
     *
     * @param condition
     * @param layers
     * @throws InvalidParameterValueException
     */
    private void validateInfoLayers( Condition condition, String[] layers )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( INFOLAYERS );

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> validLayers = op.getValues();
        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            for ( int i = 0; i < layers.length; i++ ) {
                if ( !validLayers.contains( layers[i] ) ) {
                    throw new InvalidParameterValueException( INVALIDLAYER + layers[i] );
                }
            }
        }
    }

    /**
     * checks if the passed format is valid against the format defined in the policy. If <tt>user</ff> != <tt>null</tt>
     * format will be compared against the user/rights repository
     *
     * @param condition
     *            condition containing the definition of the valid format
     * @param format
     * @throws InvalidParameterValueException
     */
    private void validateInfoFormat( Condition condition, String format )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( INFOFORMAT );

        // version is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }

        List<String> list = op.getValues();
        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            if ( !list.contains( format ) ) {
                throw new InvalidParameterValueException( INVALIDFORMAT + format );
            }
        }

    }

    /**
     * checks if the passed featureCount is valid against the featureCount defined in the policy and if it is greater
     * zero. If <tt>user</ff> != <tt>null</tt> featureCount will be compared against the user/rights repository
     *
     * @param condition
     * @param featureCount
     * @throws InvalidParameterValueException
     */
    private void validateFeatureCount( Condition condition, int featureCount )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( FEATURECOUNT );

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            if ( featureCount < 1 || featureCount > op.getFirstAsInt() ) {
                throw new InvalidParameterValueException( INAVLIDFEATURECOUNT + featureCount );
            }
        }
    }

    /**
     * validates the passed WMS GetMap request against a User- and Rights-Management DB.
     *
     * @param wmsreq
     * @param user
     * @throws InvalidParameterValueException
     */
    private void validateAgainstRightsDB( GetFeatureInfo wmsreq, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( "no access to anonymous user" );
        }

        // create feature that describes the map request
        FeatureProperty[] fps = new FeatureProperty[6];
        fps[0] = FeatureFactory.createFeatureProperty( new QualifiedName( "version" ), wmsreq.getVersion() );
        Integer x = new Integer( wmsreq.getClickPoint().x );
        fps[1] = FeatureFactory.createFeatureProperty( new QualifiedName( "x" ), x );
        Integer y = new Integer( wmsreq.getClickPoint().y );
        fps[2] = FeatureFactory.createFeatureProperty( new QualifiedName( "y" ), y );
        fps[3] = FeatureFactory.createFeatureProperty( new QualifiedName( "infoformat" ), wmsreq.getInfoFormat() );
        fps[4] = FeatureFactory.createFeatureProperty( new QualifiedName( "exceptions" ), wmsreq.getExceptions() );
        Integer fc = new Integer( wmsreq.getFeatureCount() );
        fps[5] = FeatureFactory.createFeatureProperty( new QualifiedName( "featurecount" ), fc );

        Feature feature = FeatureFactory.createFeature( "id", gfiFT, fps );
        String[] layer = wmsreq.getQueryLayers();
        for ( int i = 0; i < layer.length; i++ ) {
            if ( securityConfig.getProxiedUrl() == null ) {
                handleUserCoupledRules( user, feature, layer[i], "Layer", GETFEATUREINFO );
            } else {
                handleUserCoupledRules( user, feature, "[" + securityConfig.getProxiedUrl() + "]:" + layer[i], "Layer",
                                        GETFEATUREINFO );
            }
        }
    }

    private static FeatureType createFeatureType() {
        PropertyType[] ftps = new PropertyType[6];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "version" ), Types.INTEGER, false );
        ftps[1] = FeatureFactory.createSimplePropertyType( new QualifiedName( "x" ), Types.INTEGER, false );
        ftps[2] = FeatureFactory.createSimplePropertyType( new QualifiedName( "y" ), Types.INTEGER, false );
        ftps[3] = FeatureFactory.createSimplePropertyType( new QualifiedName( "infoformat" ), Types.VARCHAR, false );
        ftps[4] = FeatureFactory.createSimplePropertyType( new QualifiedName( "exceptions" ), Types.VARCHAR, false );
        ftps[5] = FeatureFactory.createSimplePropertyType( new QualifiedName( "featurecount" ), Types.INTEGER, false );

        return FeatureFactory.createFeatureType( "GetFeatureInfo", false, ftps );
    }

}
