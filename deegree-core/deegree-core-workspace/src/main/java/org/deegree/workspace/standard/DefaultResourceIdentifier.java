/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.workspace.standard;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceProvider;

/**
 * Default implementations for resource identifiers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class DefaultResourceIdentifier<T extends Resource> implements ResourceIdentifier<T> {

	private Class<? extends ResourceProvider<T>> provider;

	private String id;

	public DefaultResourceIdentifier(Class<? extends ResourceProvider<T>> provider, String id) {
		this.provider = provider;
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Class<? extends ResourceProvider<T>> getProvider() {
		return provider;
	}

	/**
	 * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001),
	 * which supplies an even distribution and is relatively fast. It is created from
	 * field <b>f</b> as follows:
	 * <ul>
	 * <li>boolean -- code = (f ? 0 : 1)</li>
	 * <li>byte, char, short, int -- code = (int)f</li>
	 * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
	 * <li>float -- code = Float.floatToIntBits(f);</li>
	 * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt;
	 * 32))</li>
	 * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code
	 * = f.hashCode(&nbsp;)</li>
	 * <li>Array -- Apply above rules to each element</li>
	 * </ul>
	 * <p>
	 * Combining the hash code(s) computed above: result = 37 * result + code;
	 * </p>
	 * @return <code>(int) ( result &gt;&gt;&gt; 32 ) ^ (int) result;</code>
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// the 2nd millionth prime, :-)
		long result = 32452843;
		result = result * 37 + getId().hashCode();
		result = result * 37 + getProvider().hashCode();
		return (int) (result >>> 32) ^ (int) result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ResourceIdentifier)) {
			return false;
		}

		ResourceIdentifier<? extends Resource> id = (ResourceIdentifier<?>) obj;

		return getId().equals(id.getId()) && getProvider().equals(id.getProvider());
	}

	@Override
	public String toString() {
		return provider.getSimpleName() + ":" + id;
	}

	@Override
	public int compareTo(ResourceIdentifier<T> o) {
		return toString().compareTo(o.toString());
	}

}
