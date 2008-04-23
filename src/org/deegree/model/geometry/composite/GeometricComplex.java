package org.deegree.model.geometry.composite;
import java.util.List;

import org.deegree.model.geometry.Geometry;


/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface GeometricComplex extends CompositeGeometry {

	public List<Geometry> getGeometries();

}