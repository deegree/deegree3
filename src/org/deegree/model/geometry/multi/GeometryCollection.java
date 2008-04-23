package org.deegree.model.geometry.multi;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public interface GeometryCollection<T> extends MultiGeometry<T> {

    public boolean containsPoints();
    
    public boolean containsCurves();
    
    public boolean containsSurfaces();
    
    public boolean containsSolids();
    
    public boolean containsComplexes();
    
    public boolean containsCollections();
	
}