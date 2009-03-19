package org.deegree.model.crs.configuration.deegree.xml;

import org.deegree.model.crs.CRSIdentifiable;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;

public interface CRSImporter<T> {

    public CoordinateSystem parseCoordinateSystem();
    
    public CRSIdentifiable parseIdentifiableObject( String id );
    
}
