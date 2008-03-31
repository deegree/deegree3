package org.deegree.model.geometry.multi;

import java.util.List;

import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.primitive.Point;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public interface MultiGeometry<T> extends Geometry {

    public int getNumberOfGeometries();

    public Point getCentroid();

    public List<T> getGeometries();
    
    public T getGeometryAt(int index);

}