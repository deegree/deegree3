package org.deegree.gml.feature;

import org.deegree.feature.Feature;

/**
 * Indicates that a {@link FeatureInspector} rejects a {@link Feature}.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureInspectionException extends RuntimeException {

	public FeatureInspectionException() {
		super();
	}

	public FeatureInspectionException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public FeatureInspectionException(String message) {
		super(message);
	}

}