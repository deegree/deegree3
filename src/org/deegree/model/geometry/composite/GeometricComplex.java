package org.deegree.model.geometry.composite;
import java.util.List;

import org.deegree.model.geometry.Geometry;


/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public interface GeometricComplex extends CompositeGeometry {

	public List<Geometry> getGeometries();

}