package org.deegree.model.geometry.composite;
import java.util.List;

import org.deegree.model.geometry.primitive.Surface;

/**
 * @version 1.0
 * @created 03-Sep-2007 13:58:39
 */
public interface CompositeSurface extends CompositeGeometry, Surface {

	public List<Surface> getSurfaces();

}