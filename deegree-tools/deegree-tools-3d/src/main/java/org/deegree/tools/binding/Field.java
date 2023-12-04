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
package org.deegree.tools.binding;

import java.util.Set;

import javax.xml.namespace.QName;

/**
 * A field in a resulting class.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class Field {

	public enum Modifier {

		private_("private"), protected_("protected"), public_("public");

		private final String val;

		private Modifier(String name) {
			this.val = name;
		}

		/**
		 * @return
		 */
		@Override
		public String toString() {
			return val;
		}

	}

	private String fieldName;

	private String typeName;

	private boolean isList;

	private QName propertyName;

	private Set<Field> possibleSubstitute;

	private final boolean isStatic;

	private final boolean isFinal;

	private final Modifier modifier;

	private final String value;

	/**
	 * @return the value
	 */
	public final String getValue() {
		return value;
	}

	public Field(String fieldName, String typeQName, String value) {
		this(Modifier.private_, fieldName, typeQName, value, false, null, null, false, false);
	}

	public Field(String fieldName, String typeQName) {
		this(Modifier.private_, fieldName, typeQName, null, false, null, null, false, false);
	}

	public Field(String fieldName, String typeQName, boolean isList, QName propertyName) {
		this(Modifier.private_, fieldName, typeQName, null, isList, null, propertyName, false, false);
	}

	public Field(String fieldName, String typeQName, boolean isList, Set<Field> possibleSubstitute,
			QName propertyName) {
		this(Modifier.private_, fieldName, typeQName, null, isList, possibleSubstitute, propertyName, false, false);
	}

	/**
	 * @param fieldName
	 * @param typeQName
	 * @param isList
	 * @param possibleSubstitute
	 * @param propertyName
	 * @param isStatic
	 * @param isFinal
	 */
	public Field(Modifier modifier, String fieldName, String typeQName, String value, boolean isList,
			Set<Field> possibleSubstitute, QName propertyName, boolean isStatic, boolean isFinal) {
		this.modifier = modifier;
		this.fieldName = fieldName;
		this.typeName = typeQName;
		this.isList = isList;
		this.possibleSubstitute = possibleSubstitute;
		this.propertyName = propertyName;
		this.isStatic = isStatic;
		this.isFinal = isFinal;
		this.value = value;
	}

	/**
	 * @param string
	 * @param canonicalName
	 * @param b
	 * @param object
	 * @param c
	 * @param d
	 */
	public Field(String fieldName, String typeQName, String value, boolean isStatic, boolean isFinal) {
		this(Modifier.private_, fieldName, typeQName, value, false, null, null, isStatic, isFinal);
	}

	/**
	 * @return the fieldName
	 */
	public final String getFieldName() {
		return fieldName;
	}

	/**
	 * @return the typeName
	 */
	public final String getTypeName() {
		return typeName.substring(typeName.lastIndexOf('.') + 1);
	}

	/**
	 * @return the typeName
	 */
	public final String getCanonicalTypeName() {
		return typeName;
	}

	/**
	 * @return the isList
	 */
	public final boolean isList() {
		return isList;
	}

	/**
	 * @return the hasSubstitutions
	 */
	public final boolean hasSubstitutions() {
		return possibleSubstitute != null;
	}

	/**
	 * @return the propertyName
	 */
	public final QName getPropertyName() {
		return propertyName;
	}

	/**
	 * @return the isStatic
	 */
	public final boolean isStatic() {
		return isStatic;
	}

	/**
	 * @return the isFinal
	 */
	public final boolean isFinal() {
		return isFinal;
	}

	/**
	 * @return the modifier
	 */
	public final Modifier getModifier() {
		return modifier;
	}

	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof Field) {
			final Field that = (Field) other;
			return this.typeName.equals(that.typeName) && this.fieldName.equals(that.fieldName)
					&& this.isList == that.isList;
		}
		return false;
	}

	@Override
	public int hashCode() {
		// the 2nd millionth prime, :-)
		long result = 32452843;
		// example for a double field
		// long code = (int) ( dField ^ ( dField >>> 32 ) );
		result = result * 37 + fieldName.hashCode();
		result = result * 37 + typeName.hashCode();
		result = result * 37 + (isList ? 1 : 0);
		return (int) (result >>> 32) ^ (int) result;
	}

}
