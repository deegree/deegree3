package org.deegree.model.geometry.primitive;


/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface Solid extends Primitive {

	public double getVolume();

	public double getArea();

	public Surface[][] getBoundary();

}