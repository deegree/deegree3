package org.deegree.model.geometry.primitive;

import java.util.List;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Surface extends Primitive {
	
	public double getArea();

	public double getPerimeter();

	public Point getCentroid();

	public List<Curve> getBoundary();
    
    public List<SurfacePatch> getPatches();

}