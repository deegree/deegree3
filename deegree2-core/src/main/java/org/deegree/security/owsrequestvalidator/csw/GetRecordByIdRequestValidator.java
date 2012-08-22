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
package org.deegree.security.owsrequestvalidator.csw;

import java.util.ArrayList;
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
import org.deegree.ogcwebservices.csw.discovery.GetRecordById;
import org.deegree.portal.standard.security.control.ClientHelper;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.OperationParameter;
import org.deegree.security.owsproxy.Request;
import org.deegree.security.owsrequestvalidator.Messages;
import org.deegree.security.owsrequestvalidator.Policy;

/**
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
public class GetRecordByIdRequestValidator extends AbstractCSWRequestValidator {

    private static final String ELEMENTSETNAME = "elementSetName";


    private static FeatureType grFT = null;

    static {
        if ( grFT == null ) {
            grFT = GetRecordByIdRequestValidator.createFeatureType();
        }
    }

    /**
     * @param policy
     */
    public GetRecordByIdRequestValidator( Policy policy ) {
        super( policy );
    }

    /**
     * @param request
     * @param user
     */
    @Override
    public void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        userCoupled = false;
        Request req = policy.getRequest( "CSW", "GetRecordById" );
        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPreConditions().isAny() ) {
            return;
        }
        Condition condition = req.getPreConditions();

        GetRecordById casreq = (GetRecordById) request;

        validateVersion( condition, casreq.getVersion() );
        validateElementSetName( condition, casreq.getElementSetName() );

        if ( userCoupled ) {
            validateAgainstRightsDB( casreq, user );
        }

    }

    /**
     * validates the passed CSW GetRecordById request against a User- and
     * Rights-Management DB.
     *
     * @param casreq
     * @param user
     */
    private void validateAgainstRightsDB( GetRecordById casreq, User user )
                        throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( Messages.getString( "RequestValidator.NOACCESS" ) );
        }

        List<FeatureProperty> fp = new ArrayList<FeatureProperty>();
        fp.add( FeatureFactory.createFeatureProperty( new QualifiedName( "version" ), casreq.getVersion() ) );
        fp.add( FeatureFactory.createFeatureProperty( new QualifiedName( "elementSetName" ), casreq.getElementSetName() ) );

        Feature feature = FeatureFactory.createFeature( "id", grFT, fp );
        // TODO
        // substitue csw:profile by a dynamicly determined value
        handleUserCoupledRules( user, feature, "csw:profile", ClientHelper.TYPE_METADATASCHEMA,
                                RightType.GETRECORDBYID );

    }


    /**
     * valides if the elementSetName parameter in a GetRecords request is valid against
     * the policy assigned to Validator.
     *
     * @param condition
     * @param elementSetName
     * @throws InvalidParameterValueException
     */
    private void validateElementSetName( Condition condition, String elementSetName )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( ELEMENTSETNAME );

        // is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> list = op.getValues();

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            if ( !list.contains( elementSetName ) ) {
                String s = Messages.format( "GetRecordByIdRequestValidator.INVALIDELEMENTSETNAME",
                                            elementSetName );
                throw new InvalidParameterValueException( s );
            }
        }

    }


    /**
     * creates a feature type that matches the parameters of a GetRecords
     * request
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createFeatureType() {
        PropertyType[] ftps = new PropertyType[2];
        QualifiedName qn = new QualifiedName( "version" );
        ftps[0] = FeatureFactory.createSimplePropertyType( qn, Types.VARCHAR, false );

        qn = new QualifiedName( "elementSetName" );
        ftps[1] = FeatureFactory.createSimplePropertyType( qn, Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "GetRecordById", false, ftps );
    }

}
