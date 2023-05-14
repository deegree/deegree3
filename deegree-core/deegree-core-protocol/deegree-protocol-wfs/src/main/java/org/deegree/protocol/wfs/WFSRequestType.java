package org.deegree.protocol.wfs;

/**
 * Enum type for discriminating between the different requests from the
 * <a href="http://www.opengeospatial.org/standards/wfs">OpenGIS Web Feature Service (WFS)
 * Implementation Specification</a>.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public enum WFSRequestType {

	/** Retrieve the capabilities of the service (1.0.0, 1.1.0, 2.0.0) */
	GetCapabilities,
	/**
	 * Retrieve the data model (schema) for one or more feature types (1.0.0, 1.1.0,
	 * 2.0.0)
	 */
	DescribeFeatureType,
	/**
	 * Query one or more feature types with optional filter expressions (1.0.0, 1.1.0,
	 * 2.0.0)
	 */
	GetFeature,
	/** Insert, update or delete features (1.0.0, 1.1.0, 2.0.0) */
	Transaction,
	/** Query and lock features (1.1.0 and 2.0.0) */
	GetFeatureWithLock,
	/** Retrieve features and elements by ID (1.1.0) */
	GetGmlObject,
	/** Lock features that match a filter expression. */
	LockFeature,
	/**
	 * Retrieve the values of selected feature properties based on query constraints
	 * (2.0.0)
	 */
	GetPropertyValue,
	/** Define persistent parametrized query expressions (2.0.0) */
	CreateStoredQuery,
	/** Drop a stored query from the service (2.0.0) */
	DropStoredQuery,
	/** Retrieve a list of stored queries offered by a service (2.0.0) */
	ListStoredQueries,
	/** Retrieve a description of a stored query expresssion (2.0.0) */
	DescribeStoredQueries

}
