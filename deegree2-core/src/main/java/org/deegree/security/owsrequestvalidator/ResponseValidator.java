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
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.1, $Revision$, $Date$
 *
 * @since 1.1
 */

public abstract class ResponseValidator {

    protected static final String UNKNOWNMIMETYPE = Messages.getString( "ResponseValidator.UNKNOWNMIMETYPE" );

    protected Policy policy = null;

    protected GeneralPolicyValidator gpv = null;

    /**
     * @param policy
     */
    public ResponseValidator( Policy policy ) {
        this.policy = policy;
        Condition cond = policy.getGeneralCondition();
        gpv = new GeneralPolicyValidator( cond );
    }

    /**
     * @return Returns the policy.
     *
     */
    public Policy getPolicy() {
        return policy;
    }

    /**
     * @param policy
     *            The policy to set.
     */
    public void setPolicy( Policy policy ) {
        this.policy = policy;
    }

    /**
     * validates if the passed response itself and its content is valid against the conditions
     * defined in the policies assigned to a <tt>OWSPolicyValidator</tt>
     *
     * @param service
     *            service which produced the response (WMS, WFS ...)
     * @param response
     * @param mime
     *            mime-type of the response
     * @param user
     */
    public abstract byte[] validateResponse( String service, byte[] response, String mime, User user )
                            throws InvalidParameterValueException, UnauthorizedException;

}
