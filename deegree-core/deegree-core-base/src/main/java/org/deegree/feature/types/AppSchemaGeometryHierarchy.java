//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.feature.types;

import static org.deegree.gml.GMLVersion.GML_32;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLVersion;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@company.com">Your Name</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class AppSchemaGeometryHierarchy {

    private final Set<QName> pointElements;

    private final Set<QName> curveElements;

    private final Set<QName> surfaceElements;

    private final Set<QName> solidElements;
    
    private final Set<QName> ringElements;

    private final Set<QName> primitiveElements;

    AppSchemaGeometryHierarchy( AppSchema appSchema, GMLVersion gmlVersion ) {

        QName elName = new QName( gmlVersion.getNamespace(), "Point" );
        pointElements = getConcreteSubstitutions( appSchema, elName );

        elName = getAbstractElementName( "Curve", gmlVersion );
        curveElements = getConcreteSubstitutions( appSchema, elName );

        elName = getAbstractElementName( "Ring", gmlVersion );
        ringElements = getConcreteSubstitutions( appSchema, elName );

        elName = getAbstractElementName( "Surface", gmlVersion );
        surfaceElements = getConcreteSubstitutions( appSchema, elName );

        elName = getAbstractElementName( "Solid", gmlVersion );
        solidElements = getConcreteSubstitutions( appSchema, elName );
        
        elName = getAbstractElementName( "GeometricPrimitive", gmlVersion );
        primitiveElements = getConcreteSubstitutions( appSchema, elName );
    }

    private QName getAbstractElementName( String localPart, GMLVersion version ) {
        if ( version == GML_32 ) {
            return new QName (version.getNamespace(), "Abstract" + localPart);
        }
        return new QName (version.getNamespace(), "_" + localPart);
    }

    private Set<QName> getConcreteSubstitutions( AppSchema appSchema, QName elName ) {
        Set<QName> elNames = new HashSet<QName>();
        GMLObjectType type = appSchema.getGeometryType( elName );
        if ( type != null ) {
            if ( !type.isAbstract() ) {
                elNames.add( type.getName() );
            }
            for ( GMLObjectType substitution : appSchema.getSubstitutions( type.getName() ) ) {
                if ( !substitution.isAbstract() ) {
                    elNames.add( substitution.getName() );
                }
            }
        }
        return elNames;
    }

    public Set<QName> getPrimitiveElementNames() {
        return primitiveElements;
    }

    public Set<QName> getPointElementNames() {
        return pointElements;
    }

    public Set<QName> getCurveElementNames() {
        return curveElements;
    }

    public Set<QName> getSurfaceElementNames() {
        return surfaceElements;
    }

    public Set<QName> getSolidElementNames() {
        return solidElements;
    }

    public Set<QName> getRingElementNames() {
        return ringElements;
    }
}
