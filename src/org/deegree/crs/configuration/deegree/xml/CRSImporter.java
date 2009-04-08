package org.deegree.crs.configuration.deegree.xml;

import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.coordinatesystems.CoordinateSystem;

public interface CRSImporter<T> {

    public CoordinateSystem parseCoordinateSystem();
    
    public CRSIdentifiable parseIdentifiableObject( String id );
    
}
