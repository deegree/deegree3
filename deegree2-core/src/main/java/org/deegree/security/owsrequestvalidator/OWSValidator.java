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
package org.deegree.security.owsrequestvalidator;

import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public abstract class OWSValidator {

    protected Policy policy = null;

    protected GetCapabilitiesRequestValidator getCapabilitiesValidator = null;

    protected GetCapabilitiesResponseValidator getCapabilitiesValidatorR = null;

    /**
     * @param policy
     * @param proxyURL
     */
    public OWSValidator( Policy policy, String proxyURL ) {
        this.policy = policy;
        getCapabilitiesValidator = new GetCapabilitiesRequestValidator( policy );
        getCapabilitiesValidatorR = new GetCapabilitiesResponseValidator( policy, proxyURL );
    }

    /**
     * validates if the passed request itself and its content is valid against the conditions
     * defined in the policies assigned to a <tt>OWSPolicyValidator</tt>
     *
     * @param request
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    public abstract void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException;

    /**
     * @param request
     * @param response
     * @param mime
     * @param user
     * @return the new response array
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     *
     */
    public abstract byte[] validateResponse( OGCWebServiceRequest request, byte[] response, String mime, User user )
                            throws InvalidParameterValueException, UnauthorizedException;

    /**
     * returns the general condition assigned to the encapsulated policy
     *
     * @return the general condition assigned to the encapsulated policy
     */
    public Condition getGeneralCondtion() {
        return policy.getGeneralCondition();
    }

    /**
     * returns the policy underlying a OWSValidator
     *
     * @return the policy underlying a OWSValidator
     */
    public Policy getPolicy() {
        return policy;
    }

}
