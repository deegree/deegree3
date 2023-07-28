/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.cs.refs;

import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.commons.tom.Reference;
import org.deegree.commons.tom.ReferenceResolver;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSResource;
import org.slf4j.Logger;

/**
 * Represents a reference to a {@link CRSResource}, which is usually expressed using an
 * <code>xlink:href</code> attribute in GML (may be document-local or remote).
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public abstract class CRSResourceRef<T extends CRSResource> extends Reference<T> implements CRSResource {

	private static final Logger LOG = getLogger(CRSResourceRef.class);

	/**
	 * @param resolver
	 * @param uri
	 * @param baseURL
	 */
	public CRSResourceRef(ReferenceResolver resolver, String uri, String baseURL) {
		super(resolver, uri, baseURL);
	}

	public String getAreaOfUse() {
		return getReferencedObject().getAreaOfUse();
	}

	public String getName() {
		return getReferencedObject().getName();
	}

	public double[] getAreaOfUseBBox() {
		return getReferencedObject().getAreaOfUseBBox();
	}

	public String getDescription() {
		return getReferencedObject().getDescription();
	}

	public CRSCodeType getCode() {
		return getReferencedObject().getCode();
	}

	public String getVersion() {
		return getReferencedObject().getVersion();
	}

	public String getCodeAndName() {
		return getReferencedObject().getCodeAndName();
	}

	public String[] getAreasOfUse() {
		return getReferencedObject().getAreasOfUse();
	}

	public String[] getDescriptions() {
		return getReferencedObject().getDescriptions();
	}

	public CRSCodeType[] getCodes() {
		return getReferencedObject().getCodes();
	}

	public String[] getOrignalCodeStrings() {
		return getReferencedObject().getOrignalCodeStrings();
	}

	public String[] getNames() {
		return getReferencedObject().getNames();
	}

	public String[] getVersions() {
		return getReferencedObject().getVersions();
	}

	public boolean hasCode(CRSCodeType id) {
		return getReferencedObject().hasCode(id);
	}

	public boolean hasIdOrName(String idOrName, boolean caseSensitive, boolean exact) {
		return getReferencedObject().hasIdOrName(idOrName, caseSensitive, exact);
	}

	public boolean hasId(String id, boolean caseSensitive, boolean exact) {
		return getReferencedObject().hasId(id, caseSensitive, exact);
	}

	public void setDefaultId(CRSCodeType newCodeType, boolean override) {
		getReferencedObject().setDefaultId(newCodeType, override);
	}

	public void setDefaultAreaOfUse(double[] bbox) {
		getReferencedObject().setDefaultAreaOfUse(bbox);
	}

	public void addAreaOfUse(String areaOfUse) {
		getReferencedObject().addAreaOfUse(areaOfUse);
	}

	public void addName(String name) {
		getReferencedObject().addName(name);
	}

	public void setDefaultName(String defaultName, boolean override) {
		getReferencedObject().setDefaultName(defaultName, override);
	}

	public void setDefaultDescription(String newDescription, boolean override) {
		getReferencedObject().setDefaultDescription(newDescription, override);
	}

	public void setDefaultVersion(String newVersion, boolean override) {
		getReferencedObject().setDefaultVersion(newVersion, override);
	}

	@Override
	public boolean equals(java.lang.Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		T referencedObject = getReferencedObject();
		try {
			if (referencedObject != null) {
				return referencedObject.equals(obj);
			}
		}
		catch (ReferenceResolvingException e) {
			LOG.debug("CRSResource reference could not be resolved: {}", e.getLocalizedMessage());
		}
		if (obj instanceof Reference<?>) {
			Reference<?> other = (Reference<?>) obj;
			if (getURI() == null) {
				if (other.getURI() != null)
					return false;
			}
			else if (!getURI().equals(other.getURI()))
				return false;
		}
		return getURI().equals(obj);
	}

	@Override
	public int hashCode() {
		try {
			if (getReferencedObject() != null) {
				return getReferencedObject().hashCode();
			}
		}
		catch (ReferenceResolvingException e) {
			LOG.debug("CRSResource reference could not be resolved: {}", e.getLocalizedMessage());
		}
		return getURI().hashCode();
	}

}