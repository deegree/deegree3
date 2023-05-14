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
import org.deegree.gml.GMLVersion;

/**
 * Provides convenient access to the names of geometry elements defined in an
 * {@link AppSchema}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class AppSchemaGeometryHierarchy {

	private final Set<QName> pointElements;

	private final Set<QName> abstractCurveElements;

	private final Set<QName> curveElements;

	private final Set<QName> lineStringElements;

	private final Set<QName> compositeCurveElements;

	private final Set<QName> orientableCurveElements;

	private final Set<QName> abstractSurfaceElements;

	private final Set<QName> surfaceElements;

	private final Set<QName> compositeSurfaceElements;

	private final Set<QName> solidElements;

	private final Set<QName> ringElements;

	private final Set<QName> primitiveElements;

	AppSchemaGeometryHierarchy(AppSchema appSchema, GMLVersion gmlVersion) {

		QName elName = new QName(gmlVersion.getNamespace(), "Point");
		pointElements = getConcreteSubstitutions(appSchema, elName);

		elName = getAbstractElementName("Curve", gmlVersion);
		abstractCurveElements = getConcreteSubstitutions(appSchema, elName);

		elName = new QName(gmlVersion.getNamespace(), "Curve");
		curveElements = getConcreteSubstitutions(appSchema, elName);

		elName = new QName(gmlVersion.getNamespace(), "LineString");
		lineStringElements = getConcreteSubstitutions(appSchema, elName);

		elName = new QName(gmlVersion.getNamespace(), "CompositeCurve");
		compositeCurveElements = getConcreteSubstitutions(appSchema, elName);

		elName = new QName(gmlVersion.getNamespace(), "OrientableCurve");
		orientableCurveElements = getConcreteSubstitutions(appSchema, elName);

		elName = getAbstractElementName("Ring", gmlVersion);
		ringElements = getConcreteSubstitutions(appSchema, elName);

		elName = getAbstractElementName("Surface", gmlVersion);
		abstractSurfaceElements = getConcreteSubstitutions(appSchema, elName);

		elName = new QName(gmlVersion.getNamespace(), "Surface");
		surfaceElements = getConcreteSubstitutions(appSchema, elName);

		elName = new QName(gmlVersion.getNamespace(), "CompositeSurface");
		compositeSurfaceElements = getConcreteSubstitutions(appSchema, elName);

		elName = getAbstractElementName("Solid", gmlVersion);
		solidElements = getConcreteSubstitutions(appSchema, elName);

		elName = getAbstractElementName("GeometricPrimitive", gmlVersion);
		primitiveElements = getConcreteSubstitutions(appSchema, elName);
	}

	private QName getAbstractElementName(String localPart, GMLVersion version) {
		if (version == GML_32) {
			return new QName(version.getNamespace(), "Abstract" + localPart);
		}
		return new QName(version.getNamespace(), "_" + localPart);
	}

	private Set<QName> getConcreteSubstitutions(AppSchema appSchema, QName elName) {
		Set<QName> elNames = new HashSet<QName>();
		GMLObjectType type = appSchema.getGeometryType(elName);
		if (type != null) {
			if (!type.isAbstract()) {
				elNames.add(type.getName());
			}
			for (GMLObjectType substitution : appSchema.getSubstitutions(type.getName())) {
				if (!substitution.isAbstract()) {
					elNames.add(substitution.getName());
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

	public Set<QName> getAbstractCurveSubstitutions() {
		return abstractCurveElements;
	}

	public Set<QName> getCurveSubstitutions() {
		return curveElements;
	}

	public Set<QName> getLineStringSubstitutions() {
		return lineStringElements;
	}

	public Set<QName> getCompositeCurveSubstitutions() {
		return compositeCurveElements;
	}

	public Set<QName> getOrientableCurveSubstitutions() {
		return orientableCurveElements;
	}

	public Set<QName> getAbstractSurfaceElementNames() {
		return abstractSurfaceElements;
	}

	public Set<QName> getSurfaceSubstitutions() {
		return surfaceElements;
	}

	public Set<QName> getCompositeSurfaceSubstitutions() {
		return compositeSurfaceElements;
	}

	public Set<QName> getSolidElementNames() {
		return solidElements;
	}

	public Set<QName> getRingElementNames() {
		return ringElements;
	}

}
