//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/
package org.deegree.services.sos.model;

import org.deegree.geometry.Geometry;


/**
 * This class encapsulates a observation and measurement process.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class Procedure {
    private final String name;
    private final String descriptionDocument;
    private final String featureRef;
    private final Geometry geometry;

    /**
     * @param name
     * @param descriptionDocument
     */
    public Procedure( String name, String descriptionDocument ) {
        this( name, descriptionDocument, "" );
    }

    /**
     * @param name
     * @param descriptionDocument
     * @param feaureRef
     */
    public Procedure( String name, String descriptionDocument, String feaureRef ) {
        this( name, descriptionDocument, feaureRef, null );
    }

    /**
     * @param name
     * @param descriptionDocument
     * @param feaureRef
     * @param geometry
     */
    public Procedure( String name, String descriptionDocument, String feaureRef, Geometry geometry ) {
        this.name = name;
        this.descriptionDocument = descriptionDocument;
        this.featureRef = feaureRef;
        this.geometry = geometry;
    }

    /**
     * @param name
     */
    public Procedure( String name ) {
        this( name, "" );
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return a reference to the feature of this procedure
     */
    public String getFeatureRef() {
        return featureRef;
    }

    /**
     * @return the descriptionDocument
     */
    public String getDescriptionDocument() {
        return descriptionDocument;
    }

    /**
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public String toString() {
        return name;
    }

}
