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
package org.deegree.cs;

import org.deegree.commons.tom.Object;

/**
 * Interface describing a CRS or an arbirary component of a CRS.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public interface CRSResource extends Object {

	/**
	 * @return the first of all areasOfUse or <code>null</code> if no areasOfUse were
	 * given.
	 */
	String getAreaOfUse();

	/**
	 * @return the first of all descriptions or <code>null</code> if no descriptions were
	 * given.
	 */
	String getDescription();

	/**
	 * @return the first of all identifiers.
	 */
	CRSCodeType getCode();

	/**
	 * @return the first of all names or <code>null</code> if no names were given.
	 */
	String getName();

	/**
	 * @return the first of all versions or <code>null</code> if no versions were given.
	 */
	String getVersion();

	/**
	 * @return the first id and the name (if given) as id: id, name: name.
	 */
	String getCodeAndName();

	/**
	 * @return the areasOfUse or <code>null</code> if no areasOfUse were given.
	 */
	String[] getAreasOfUse();

	/**
	 * @return the descriptions or <code>null</code> if no descriptions were given.
	 */
	String[] getDescriptions();

	/**
	 * @return the identifiers, each identifiable object has atleast one id.
	 */
	CRSCodeType[] getCodes();

	/**
	 * @return the codetypes as the original strings, each identifiable object has atleast
	 * one id.
	 */
	String[] getOrignalCodeStrings();

	/**
	 * @return the names or <code>null</code> if no names were given.
	 */
	String[] getNames();

	/**
	 * @return the versions or <code>null</code> if no versions were given.
	 */
	String[] getVersions();

	/**
	 * @param id a string which could match this identifiable.
	 * @return true if this identifiable can be identified with the given string, false
	 * otherwise.
	 */
	boolean hasCode(CRSCodeType id);

	/**
	 * Iterates over all Ids (code type originals) and Names and tests if either one
	 * matches the given string.
	 * @param idOrName a String which might be an id or a name.
	 * @param caseSensitive should the match me case sensitive
	 * @param exact should the names and ids contain the given string or match exact.
	 * @return true if any of the names or codes match without case the given string.
	 */
	boolean hasIdOrName(String idOrName, boolean caseSensitive, boolean exact);

	/**
	 * @param id
	 * @param caseSensitive
	 * @param exact
	 * @return true if the given id is present in this objects id's.
	 */
	boolean hasId(String id, boolean caseSensitive, boolean exact);

	/**
	 * Returns the area of use, i.e. the domain where this {@link CRSIdentifiable} is
	 * valid.
	 * @return the domain of validity (EPSG:4326 coordinates), order: minX, minY, maxX,
	 * maxY, never <code>null</code> (-180,-90,180,90) if no such information is available
	 */
	double[] getAreaOfUseBBox();

	/**
	 * @param newCodeType
	 * @param override
	 */
	void setDefaultId(CRSCodeType newCodeType, boolean override);

	/**
	 * @param bbox an envelope of validity in epsg:4326 coordinates, min(lon,lat)
	 * max(lon,lat);
	 */
	void setDefaultAreaOfUse(double[] bbox);

	/**
	 * @param areaOfUse
	 */
	void addAreaOfUse(String areaOfUse);

	/**
	 * @param name
	 */
	void addName(String name);

	/**
	 * @param defaultName the new default name
	 * @param override true if the new name should override the name currently at position
	 * 0
	 */
	void setDefaultName(String defaultName, boolean override);

	/**
	 * @param newDescription the new default description
	 * @param override true if the new description should override the description
	 * currently at position 0
	 */
	void setDefaultDescription(String newDescription, boolean override);

	/**
	 * @param newVersion the new default version
	 * @param override true if the new version should override the version currently at
	 * position 0
	 */
	void setDefaultVersion(String newVersion, boolean override);

}