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
package org.deegree.security.owsproxy;

/**
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Request {

    private Condition preConditions;

    private Condition postConditions;

    private String service;

    private String name;

    private boolean any;

    /**
     * @param service
     * @param name
     */
    public Request( String service, String name ) {
        this.service = service;
        this.name = name;
    }

    /**
     * @param service
     * @param name
     * @param all
     */
    public Request( String service, String name, boolean all ) {
        this.service = service;
        this.name = name;
        this.any = all;
    }

    /**
     * @param service
     * @param name
     * @param preConditions
     * @param postConditions
     */
    public Request( String service, String name, Condition preConditions, Condition postConditions ) {
        super();
        this.preConditions = preConditions;
        this.postConditions = postConditions;
        this.service = service;
        this.name = name;
    }

    /**
     * returns the name of the service a <tt>Request</tt> is assigned to
     *
     * @return the name of the service a <tt>Request</tt> is assigned to
     */
    public String getService() {
        return service;
    }

    /**
     * returns the name request
     *
     * @return the name request
     */
    public String getName() {
        return name;
    }

    /**
     * returns the pre-condition assigned to a request. This method may returns <tt>null</tt>
     * which means no pre conditions are defined and all http-requests validated against the
     * conditions of this request will fail.
     *
     * @return the pre-condition assigned to a request. This method may returns <tt>null</tt>
     *         which means no pre conditions are defined and all http-requests validated against the
     *         conditions of this request will fail.
     */
    public Condition getPreConditions() {
        return preConditions;
    }

    /**
     * sets the pre-condition of a <tt>Request</tt>
     *
     * @see #getPreConditions()
     * @param condition
     */
    public void setPreConditions( Condition condition ) {
        this.preConditions = condition;
    }

    /**
     * returns the post-condition assigned to a request. This method may returns <tt>null</tt>
     * which means no post-conditions are defined and all responses to http-requests validated
     * against the conditions of this request will fail.
     *
     * @return the post-condition assigned to a request. This method may returns <tt>null</tt>
     *         which means no post-conditions are defined and all responses to http-requests
     *         validated against the conditions of this request will fail.
     */
    public Condition getPostConditions() {
        return postConditions;
    }

    /**
     * sets the post-condition of a <tt>Request</tt>
     *
     * @see #getPostConditions()
     * @param condition
     */
    public void setPostConditions( Condition condition ) {
        this.postConditions = condition;
    }

    /**
     * @return Returns the all.
     */
    public boolean isAny() {
        return any;
    }

    /**
     * @param any
     */
    public void setAny( boolean any ) {
        this.any = any;
    }

}
