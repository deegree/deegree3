package org.deegree.model.geometry.primitive;

import java.util.List;


/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public interface Curve extends Primitive {

	public enum ORIENTATION { positiv, negativ, unknown }; 

	public double[] getX();

	public double[] getY();

	public double[] getZ();

	public double[] getAsArray();

	public List<Point> getPoints();
	
	public ORIENTATION getOrientation();

	public double getLength();

	public List<Point> getBoundary();

	public boolean isClosed();
    
    public List<CurveSegment> getCurveSegments();

}