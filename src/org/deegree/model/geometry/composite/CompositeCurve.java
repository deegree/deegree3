package org.deegree.model.geometry.composite;
import java.util.List;

import org.deegree.model.geometry.primitive.Curve;

/**
 * @version 1.0
 * @created 03-Sep-2007 13:58:36
 */
public interface CompositeCurve extends CompositeGeometry, Curve {

	public List<Curve> getCurves();

}