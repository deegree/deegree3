package org.deegree.gml.feature;

import org.deegree.feature.Feature;

/**
 * Enables the inspection of {@link Feature} objects parsed by a {@link GMLFeatureReader}.
 * <p>
 * Implementations can perform such tasks as validation or repairing of defects.
 * </p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public interface FeatureInspector {

	/**
	 * Invokes the inspection of the given {@link Feature}.
	 * @param feature feature to be inspected, never <code>null</code>
	 * @return inspected feature, may be a different (repaired) instance, but must have
	 * exactly the same subinterface
	 * @throws FeatureInspectionException if the inspector rejects the {@link Feature}
	 */
	Feature inspect(Feature feature) throws FeatureInspectionException;

}
