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

import org.deegree.security.drm.SecurityRegistry;

/**
 * Abstract superclass of objects that are securable, i.e. which carry
 * information about which <code>Role</code>s have which <code>Right</code>s
 * concerning these objects.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$
 */

public abstract class SecurableObject {

	protected int id;
	protected int type;
	protected String name;
	protected String title;
    protected SecurityRegistry registry;


	/**
	 * Returns the unique identifier of this <code>SecurableObject</code>.
	 */
	public int getID () {
		return id;
	}

    /**
     * Returns the type of this <code>SecurableObject</code>.
     * <p>
     * NOTE: Unique in conjunction with name field.
     *
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the name of this <code>SecurableObject</code>.
     * <p>
     * NOTE: Unique in conjunction with type field.
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the human readable name of this <code>SecurableObject</code>.
     * <p>
     * NOTE: This may not be unique.
     *
     */
    public String getTitle() {
        return title;
    }

	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * @param that
	 */
	public boolean equals (Object that) {
		if (that instanceof SecurableObject) {
			return (((SecurableObject) that).getID () == getID ());
		}
		return false;
	}

	/**
	 * Returns a hash code value for the object. This method is supported
	 * for the benefit of hashtables such as those provided by
	 * java.util.Hashtable.
	 */
	public int hashCode () {
		return id;
	}

	/**
	 * Returns a <code>String</code> representation of this object.
	 */
	public String toString () {
		StringBuffer sb = new StringBuffer ("Id: ").
			append (id).append (", Name: ").append (name);
		return sb.toString ();
	}
}
