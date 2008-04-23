package org.deegree.model.geometry.composite;
import java.util.List;

import org.deegree.model.geometry.primitive.Solid;

/**
 * @version 1.0
 * @created 03-Sep-2007 13:58:38
 */
public interface CompositeSolid extends CompositeGeometry, Solid {

	public List<Solid> getSolids();

}