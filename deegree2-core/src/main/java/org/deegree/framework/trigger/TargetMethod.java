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
package org.deegree.framework.trigger;

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
public class TargetMethod {

    private String name;

    private TriggerCapability preTrigger;

    private TriggerCapability postTrigger;

    /**
     *
     * @param name
     *            name of the method
     * @param preTrigger
     *            may be <code>null</code>
     * @param postTrigger
     *            may be <code>null</code>
     */
    public TargetMethod( String name, TriggerCapability preTrigger, TriggerCapability postTrigger ) {
        this.name = name;
        this.preTrigger = preTrigger;
        this.postTrigger = postTrigger;
    }

    /**
     * returns the name of the method
     *
     * @return the name of the method
     */
    public String getName() {
        return name;
    }

    /**
     * returns the capabilities of pre trigger assigned to the method. If no pre trigger is assigned
     * <code>null</code> will be returned.
     *
     * @return the capabilities of pre trigger assigned to the method. If no pre trigger is assigned
     *         <code>null</code> will be returned.
     */
    public TriggerCapability getPostTrigger() {
        return postTrigger;
    }

    /**
     * returns the capabilities of post trigger assigned to the method. If no post trigger is
     * assigned <code>null</code> will be returned.
     *
     * @return the capabilities of post trigger assigned to the method. If no post trigger is
     *         assigned <code>null</code> will be returned.
     */
    public TriggerCapability getPreTrigger() {
        return preTrigger;
    }

}
