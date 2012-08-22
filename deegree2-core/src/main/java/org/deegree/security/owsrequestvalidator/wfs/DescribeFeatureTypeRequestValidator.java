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
package org.deegree.security.owsrequestvalidator.wfs;

import static org.deegree.portal.standard.security.control.ClientHelper.TYPE_FEATURETYPE;
import static org.deegree.security.drm.model.RightType.DESCRIBEFEATURETYPE;

import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureType;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.OperationParameter;
import org.deegree.security.owsproxy.Request;
import org.deegree.security.owsrequestvalidator.Policy;

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
 * @since 2.0
 */
class DescribeFeatureTypeRequestValidator extends AbstractWFSRequestValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( DescribeFeatureTypeRequestValidator.class );

    // known condition parameter
    private static final String FORMAT = "format";

    private static FeatureType gfFT = null;

    static {
        if ( gfFT == null ) {
            gfFT = DescribeFeatureTypeRequestValidator.createFeatureType();
        }
    }

    /**
     * @param policy
     */
    public DescribeFeatureTypeRequestValidator( Policy policy ) {
        super( policy );
    }

    @Override
    public void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException {
        userCoupled = false;
        Request req = policy.getRequest( "WFS", "DescribeFeatureType" );

        if ( req == null ) {
            String msg = "Did you define a DescribeFeatureType section in your WFS policy file?";
            LOG.logWarning( msg );
            throw new InvalidParameterValueException( msg );
        }

        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPreConditions().isAny() ) {
            return;
        }
        Condition condition = req.getPreConditions();

        DescribeFeatureType wfsreq = (DescribeFeatureType) request;

        validateVersion( condition, wfsreq.getVersion() );

        QualifiedName[] typeNames = wfsreq.getTypeNames();
        String[] ft = new String[typeNames.length];
        for ( int i = 0; i < ft.length; i++ ) {
            StringBuffer sb = new StringBuffer( 200 );
            sb.append( '{' ).append( typeNames[i].getNamespace().toASCIIString() );
            sb.append( "}:" ).append( typeNames[i].getLocalName() );
            ft[i] = sb.toString();
        }
        validateFeatureTypes( condition, ft );
        validateFormat( condition, wfsreq.getOutputFormat() );

        if ( userCoupled ) {
            validateAgainstRightsDB( wfsreq, user );
        }
    }

    /**
     * valides if the format you in a GetFeature request is valid against the policy assigned to Validator. If the
     * passed user is not <tt>null</tt> and the format parameter is user coupled the format will be validated against a
     * users and rights management system.
     *
     * @param condition
     * @param format
     * @throws InvalidParameterValueException
     */
    private void validateFormat( Condition condition, String format )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( FORMAT );

        if ( op == null ) {
            String msg = "You forgot to add a format parameter to your DescribeFeatureType section in your policy.";
            LOG.logWarning( msg );
            throw new InvalidParameterValueException( msg );
        }

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> validLayers = op.getValues();
        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            if ( !validLayers.contains( format ) ) {
                String s = Messages.getMessage( "OWSPROXY_DESCRIBEFEATURETYPE_FORMAT", format );
                throw new InvalidParameterValueException( s );
            }
        }

    }

    /**
     * validates the passed WMS GetMap request against a User- and Rights-Management DB.
     *
     * @param wfsreq
     * @param user
     * @throws InvalidParameterValueException
     */
    private void validateAgainstRightsDB( DescribeFeatureType wfsreq, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( "no access to anonymous user" );
        }

        // create feature that describes the map request
        FeatureProperty[] fps = new FeatureProperty[2];
        fps[0] = FeatureFactory.createFeatureProperty( new QualifiedName( "version" ), wfsreq.getVersion() );
        fps[1] = FeatureFactory.createFeatureProperty( new QualifiedName( "outputformat" ), wfsreq.getOutputFormat() );

        Feature feature = FeatureFactory.createFeature( "id", gfFT, fps );
        QualifiedName[] typeNames = wfsreq.getTypeNames();
        for ( int i = 0; i < typeNames.length; i++ ) {
            StringBuffer sb = new StringBuffer( 200 );
            sb.append( '{' ).append( typeNames[i].getNamespace().toASCIIString() );
            sb.append( "}:" ).append( typeNames[i].getLocalName() );
            LOG.logDebug( "validating feature type (DESCRIBEFEATURETYPE) against rights database: ", sb );
            if ( securityConfig.getProxiedUrl() == null ) {
                handleUserCoupledRules( user, feature, sb.toString(), TYPE_FEATURETYPE, DESCRIBEFEATURETYPE );
            } else {
                handleUserCoupledRules( user, feature, "[" + securityConfig.getProxiedUrl() + "]:" + sb,
                                        TYPE_FEATURETYPE, DESCRIBEFEATURETYPE );
            }
        }

    }

    /**
     * creates a feature type that matches the parameters of a GetLagendGraphic request
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createFeatureType() {
        PropertyType[] ftps = new PropertyType[2];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "version" ), Types.VARCHAR, false );
        ftps[1] = FeatureFactory.createSimplePropertyType( new QualifiedName( "outputformat" ), Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "DescribeFeatureType", false, ftps );
    }

}
