package org.deegree.cql2;

import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.tom.primitive.BaseType;

/**
 * Enhances {@link BaseType} by GEOMETRY and UNKNOWN (as fallback).
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public enum FilterPropertyType {

	STRING, BOOLEAN, DECIMAL, DOUBLE, INTEGER, DATE, DATE_TIME, TIME, GEOMETRY, UNKNOWN;

	/**
	 * @param baseType may be <code>null</code> (returns STRING)
	 * @return FilterPropertyType derived from BaseType, fallback to STRING
	 */
	public static FilterPropertyType fromBaseType(BaseType baseType) {
		try {
			return FilterPropertyType.valueOf(baseType.name());
		}
		catch (IllegalArgumentException e) {
			return STRING;
		}
	}

	/**
	 * @param typeDefinition may be <code>null</code> (returns null)
	 * @return FilterPropertyType derived from XSTypeDefinition, <code>null</code>> of not
	 * a mappable type
	 */
	public static FilterPropertyType fromXsdTypeDefinition(XSTypeDefinition typeDefinition) {
		if (typeDefinition != null) {
			String type = typeDefinition.getName();
			switch (type) {
				case "string":
				case "anyURI":
					return STRING;
				case "boolean":
					return BOOLEAN;
				case "decimal":
					return DECIMAL;
				case "integer":
					return INTEGER;
				case "dateTime":
					return DATE_TIME;
				case "time":
					return TIME;
				case "date":
					return DATE;
			}
		}
		return null;
	}

}
