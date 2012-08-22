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

import java.util.List;
import java.util.Properties;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.WrongCredentialsException;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.SecuredObject;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.DefaultDBConnection;
import org.deegree.security.owsproxy.OperationParameter;
import org.deegree.security.owsproxy.SecurityConfig;

/**
 * basic class for validating OWS requests
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public abstract class RequestValidator {

    private static ILogger LOG = LoggerFactory.getLogger( RequestValidator.class );

    private static final String VERSION = "version";

    private static final String EXCEPTION = "exception";

    // message strings
    private static final String INVALIDEXCEPTIONS = Messages.getString( "RequestValidator.INVALIDEXCEPTIONS" );

    private static final String UNAUTORIZEDACCESS = Messages.getString( "RequestValidator.UNAUTORIZEDACCESS" );

    protected Policy policy = null;

    protected GeneralPolicyValidator gpv = null;

    protected boolean userCoupled = false;

    protected SecurityConfig securityConfig = null;

    /**
     * @param policy
     */
    public RequestValidator( Policy policy ) {
        this.policy = policy;
        Condition cond = policy.getGeneralCondition();
        gpv = new GeneralPolicyValidator( cond );
        securityConfig = policy.getSecurityConfig();
        if ( securityConfig != null ) {
            DefaultDBConnection db = securityConfig.getRegistryConfig().getDbConnection();
            Properties properties = new Properties();
            properties.setProperty( "driver", db.getDirver() );
            properties.setProperty( "url", db.getUrl() );
            properties.setProperty( "user", db.getUser() );
            properties.setProperty( "password", db.getPassword() );
            try {
                if ( !SecurityAccessManager.isInitialized() ) {
                    SecurityAccessManager.initialize( securityConfig.getRegistryClass(), properties,
                                                      securityConfig.getReadWriteTimeout() * 1000 );
                }
            } catch ( GeneralSecurityException e1 ) {
                LOG.logError( e1.getMessage(), e1 );
                e1.printStackTrace();
            }
        }
    }

    /**
     * @return Returns the policy.
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
     * validates if the passed request itself and its content is valid against the conditions defined in the policies
     * assigned to a <tt>OWSPolicyValidator</tt>
     *
     * @param request
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    public abstract void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException;

    /**
     *
     * @param condition
     * @param version
     * @throws InvalidParameterValueException
     */
    protected void validateVersion( Condition condition, String version )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( VERSION );

        // version is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }
        List list = op.getValues();
        if ( !list.contains( version ) ) {
            if ( !op.isUserCoupled() ) {
                String INVALIDVERSION = Messages.format( "RequestValidator.INVALIDVERSION", version );
                throw new InvalidParameterValueException( INVALIDVERSION );
            }
            userCoupled = true;
        }

    }

    /**
     * checks if the passed exceptions format is valid against the exceptions formats defined in the policy. If
     * <tt>user</ff> != <tt>null</tt> the valid exceptions formats will be read from the user/rights repository
     *
     * @param condition
     *            condition containing the definition of the valid exceptions
     * @param exceptions
     * @throws InvalidParameterValueException
     */
    protected void validateExceptions( Condition condition, String exceptions )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( EXCEPTION );

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List list = op.getValues();
        if ( !list.contains( exceptions ) ) {
            if ( !op.isUserCoupled() ) {
                throw new InvalidParameterValueException( INVALIDEXCEPTIONS + exceptions );
            }
            userCoupled = true;
        }

    }

    /**
     * handles the validation of user coupled parameters of a request
     *
     * @param user
     * @param feature
     * @param secObjName
     * @param secObjType
     * @param rightType
     * @throws UnauthorizedException
     * @throws InvalidParameterValueException
     */
    protected void handleUserCoupledRules( User user, Feature feature, String secObjName, String secObjType,
                                           RightType rightType )
                            throws UnauthorizedException, InvalidParameterValueException {
        try {
            SecurityAccessManager sam = SecurityAccessManager.getInstance();
            SecurityAccess access = sam.acquireAccess( user );
            SecuredObject secObj = access.getSecuredObjectByName( secObjName, secObjType );

            if ( LOG.isDebug() ) {
                LOG.logDebug( "Checking rule for", secObjName + " -> " + secObjType );
                LOG.logDebug( "Right type is", rightType.getName() );
                for ( FeatureProperty p : feature.getProperties() ) {
                    LOG.logDebug( "Feature property name: " + p.getName().getPrefixedName() );
                    LOG.logDebug( "Feature property value: ", p.getValue() );
                }
                LOG.logDebug( "For user", user );
            }

            if ( !user.hasRight( access, rightType, feature, secObj ) ) {
                if ( securityConfig.getProxiedUrl() != null ) {
                    String name = secObjName.substring( secObjName.indexOf( "]" ) + 1 );
                    throw new UnauthorizedException( UNAUTORIZEDACCESS + name + ':' + feature );
                }
                throw new UnauthorizedException( UNAUTORIZEDACCESS + secObjName + ':' + feature );
            }
        } catch ( WrongCredentialsException e ) {
            throw new UnauthorizedException( e.getMessage() );
        } catch ( GeneralSecurityException e ) {
            e.printStackTrace();
            throw new UnauthorizedException( e.getMessage() );
        } catch ( Exception e ) {
            throw new InvalidParameterValueException( StringTools.stackTraceToString( e ) );
        }
    }

}
