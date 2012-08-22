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
package org.deegree.security.drm.model;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.feature.Feature;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.security.GeneralSecurityException;

/**
 * A <code>Right</code> instance encapsulates a <code>SecurableObject</code>, a
 * <code>RightType</code> and optional constraints which restrict it's applicability.
 * <p>
 * For example, one instance of a <code>RightSet</code> may be the 'access'-Right to a geographic
 * dataset restricted to a certain area and weekdays. The situation (requested area and current
 * time) is coded as a <code>Feature</code> object.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$
 */

public class Right {

    private ILogger LOG = LoggerFactory.getLogger( Right.class );

    private SecurableObject object;

    private RightType type;

    private Filter constraints;

    /**
     * Creates a new <code>Right</code> -instance (with no constraints).
     *
     * @param object
     * @param type
     */
    public Right( SecurableObject object, RightType type ) {
        this.object = object;
        this.type = type;
    }

    /**
     * Creates a new <code>Right</code> -instance.
     *
     * @param object
     * @param type
     * @param constraints
     *            null means that no constraints are defined
     */
    public Right( SecurableObject object, RightType type, Filter constraints ) {
        this( object, type );
        this.constraints = constraints;
    }

    /**
     * @return the associated <code>SecurableObject</code>.
     */
    public SecurableObject getSecurableObject() {
        return object;
    }

    /**
     * @return the associated <code>RightType</code>.
     *
     */
    public RightType getType() {
        return type;
    }

    /**
     * Returns the restrictions (the parameters) of this <code>Right</code>.
     *
     * @return null, if no constraints are defined
     *
     */
    public Filter getConstraints() {
        return constraints;
    }

    /**
     * @return the disjunctive combination of the instance and the submitted <code>Right</code>,
     *         so that the new <code>Right</code> has the permissions of both instances.
     *
     * @param that
     * @throws GeneralSecurityException
     */
    public Right merge( Right that )
                            throws GeneralSecurityException {
        Right combinedRight = null;

        if ( !this.object.equals( that.object ) ) {
            throw new GeneralSecurityException( "Trying to merge right on securable object '" + this.object.id
                                                + "' and on object '" + that.object.id
                                                + "', but only rights on the same object may be merged." );
        }
        if ( this.type.getID() != that.type.getID() ) {
            throw new GeneralSecurityException( "Trying to merge right of type '" + this.type.getName()
                                                + "' and right of type '" + that.type.getName()
                                                + "', but only rights of the same type may be merged." );
        }

        // check if any of the rights has no constraints
        if ( this.constraints == null && that.constraints == null ) {
            combinedRight = new Right( object, type, null );
        } else if ( this.constraints == null && that.constraints != null ) {
            combinedRight = new Right( object, type, that.constraints );
        } else if ( this.constraints != null && that.constraints == null ) {
            combinedRight = new Right( object, type, this.constraints );
        } else if ( that.constraints == null ) {
            combinedRight = that;
        } else {
            Filter combinedConstraints = new ComplexFilter( (ComplexFilter) this.constraints,
                                                            (ComplexFilter) that.constraints, OperationDefines.OR );
            combinedRight = new Right( object, type, combinedConstraints );

        }
        return combinedRight;
    }

    /**
     * @return true if the <code>Right</code> applies on the given <code>SecurableObject</code>
     *         and in a concrete situation (the situation is represented by the given
     *         <code>Feature</code>).
     *
     * @param object
     * @param situation
     * @throws GeneralSecurityException
     */
    public boolean applies( SecurableObject object, Feature situation )
                            throws GeneralSecurityException {
        boolean applies = false;
        if ( object.equals( this.object ) ) {
            try {
                if ( constraints != null ) {
                    if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                        LOG.logDebug( "constraints", constraints.to110XML() );
                    }
                    applies = constraints.evaluate( situation );
                } else {
                    LOG.logDebug( "no constraints" );
                    applies = true;
                }
            } catch ( FilterEvaluationException e ) {
                LOG.logError( e.getMessage(), e );
                throw new GeneralSecurityException( "Error evaluating parameters (filter expression): "
                                                    + e.getMessage() );
            }
        }
        LOG.logDebug( "situation", situation );
        LOG.logDebug( "object", object );
        LOG.logDebug( applies ? "The right applies." : "The right does not apply." );

        return applies;
    }

    /**
     * @return true if the <code>Right</code> applies on the given <code>SecurableObject</code>
     *         and in unrestricted manner (w/o constraints).
     *
     * @param object
     */
    public boolean applies( SecurableObject object ) {
        boolean applies = false;
        if ( object.equals( this.object ) ) {
            if ( constraints == null ) {
                applies = true;
            }
        }
        return applies;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( "Id: " ).append( type.getID() ).append( ", Name: " ).append( type.getName() ).append(
                                                                                                                                  ", " );
        if ( constraints != null ) {
            sb.append( "Constraints: " + constraints.to110XML() );
        } else {
            sb.append( "Constraints: none" );
        }
        return sb.toString();
    }
}
