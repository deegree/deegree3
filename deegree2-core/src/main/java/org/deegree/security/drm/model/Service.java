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

import static java.util.Collections.unmodifiableList;

import java.util.List;

import org.deegree.framework.util.StringPair;

/**
 * <code>Service</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Service extends SecurableObject {

    private final String address;

    private final String title;

    private final List<StringPair> objects;

    private final int id;

    private final String type;

    /**
     * @param id
     * @param address
     * @param title
     * @param objects
     * @param type
     */
    public Service( int id, String address, String title, List<StringPair> objects, String type ) {
        this.id = id;
        this.address = address;
        this.title = title;
        this.objects = objects;
        this.type = type;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @return the title
     */
    public String getServiceTitle() {
        return title;
    }

    /**
     * @return the objects of the service
     */
    public List<StringPair> getObjects() {
        return unmodifiableList( objects );
    }

    /**
     * @return the db id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the service type (WMS/WFS)
     */
    public String getServiceType() {
        return type;
    }

    @Override
    public String toString() {
        return "Service: ID " + id + ", address " + address + ", title '" + title + "'. Objects:\n" + objects;
    }

    @Override
    public boolean equals( Object that ) {
        if ( !( that instanceof Service ) ) {
            return false;
        }
        return ( (Service) that ).id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

}
