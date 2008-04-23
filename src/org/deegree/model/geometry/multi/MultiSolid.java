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
public interface MultiSolid<T> extends MultiGeometry<T> {

    public double getVolume();
       
}