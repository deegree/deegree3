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

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.discovery.DescribeRecord;
import org.deegree.portal.standard.security.control.ClientHelper;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;
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
class DescribeRecordRequestValidator extends AbstractCSWRequestValidator {

    private static FeatureType drtFT = null;

    static {
        if ( drtFT == null ) {
            drtFT = DescribeRecordRequestValidator.createFeatureType();
        }
    }

    /**
     * @param policy
     */
    public DescribeRecordRequestValidator( Policy policy ) {
        super( policy );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree_impl.security.RequestValidator#validateRequest(org.deegree.services.OGCWebServiceRequest,
     *      java.lang.String)
     */
    @Override
    public void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException {
        userCoupled = false;
        Request req = policy.getRequest( "CSW", "DescribeRecord" );
        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPreConditions().isAny() ) {
            return;
        }
        Condition condition = req.getPreConditions();

        DescribeRecord cswreq = (DescribeRecord) request;

        validateVersion( condition, cswreq.getVersion() );

        if ( userCoupled ) {
            validateAgainstRightsDB( cswreq, user );
        }
    }

    /**
     * validates the passed WMS GetMap request against a User- and Rights-Management DB.
     *
     * @param wmsreq
     * @param user
     * @throws InvalidParameterValueException
     */
    private void validateAgainstRightsDB( DescribeRecord wfsreq, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( "no access to anonymous user" );
        }

        // create feature that describes the map request
        FeatureProperty[] fps = new FeatureProperty[2];
        fps[0] = FeatureFactory.createFeatureProperty( new QualifiedName( "version" ), wfsreq.getVersion() );
        fps[1] = FeatureFactory.createFeatureProperty( new QualifiedName( "outputformat" ), wfsreq.getOutputFormat() );

        Feature feature = FeatureFactory.createFeature( "id", drtFT, fps );
        handleUserCoupledRules( user, feature, "", ClientHelper.TYPE_METADATASCHEMA, RightType.DESCRIBERECORDTYPE );

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

        return FeatureFactory.createFeatureType( "DescribeRecord", false, ftps );
    }

}
