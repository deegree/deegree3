/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence.sql.mapper;

import static java.lang.Boolean.TRUE;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_ELEMENT;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_MIXED;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_SIMPLE;
import static org.deegree.commons.tom.primitive.BaseType.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.feature.types.AppSchema;
import org.deegree.filter.expression.ValueReference;
import org.jaxen.expr.AllNodeStep;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.NumberExpr;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.TextNodeStep;
import org.jaxen.saxpath.Axis;

/**
 * Helper class for determining the element declarations / primitive types targeted by
 * mapping expressions.
 *
 * TODO Where should this functionality go in the end?
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class XPathSchemaWalker {

	private final AppSchema appSchema;

	private final NamespaceBindings nsBindings;

	public XPathSchemaWalker(AppSchema appSchema, NamespaceBindings nsBindings) {
		this.appSchema = appSchema;
		this.nsBindings = nsBindings;
	}

	public Pair<XSElementDeclaration, Boolean> getTargetElement(Pair<XSElementDeclaration, Boolean> context,
			ValueReference propName) {

		Expr path = propName.getAsXPath();
		if (!(path instanceof LocationPath)) {
			throw new IllegalArgumentException("XPath '" + propName + "' does not denote a location path.");
		}

		Pair<XSElementDeclaration, Boolean> currentEl = context;
		for (Object o : ((LocationPath) path).getSteps()) {
			if (o instanceof NameStep) {
				NameStep step = (NameStep) o;
				if (step.getAxis() == Axis.CHILD) {
					// TODO check predicates
					QName qName = getQName(step);
					int num = getNumber(step);
					currentEl = getTargetElement(currentEl, qName, num);
					if (currentEl == null) {
						throw new IllegalArgumentException("Unable to match XPath '" + propName.getAsText()
								+ "' to application schema. Step '" + qName + "' cannot be resolved.");
					}
				}
				else {
					throw new IllegalArgumentException("Unable to match XPath '" + propName.getAsText()
							+ "'to application schema. Only child element steps are supported.");
				}
			}
			else if (o instanceof AllNodeStep) {
				// self()
			}
			else {
				throw new IllegalArgumentException("Unable to infer type for XPath '" + propName.getAsText()
						+ "'. Expression may only contain name steps.");
			}
		}
		return currentEl;
	}

	private int getNumber(NameStep step) {
		int num = 0;
		if (!step.getPredicates().isEmpty()) {
			List<?> predicates = step.getPredicates();
			if (predicates.size() == 1) {
				Expr predicate = ((Predicate) predicates.get(0)).getExpr();
				if (predicate instanceof NumberExpr) {
					num = ((NumberExpr) predicate).getNumber().intValue();
				}
			}
		}
		return num;
	}

	public Pair<PrimitiveType, Boolean> getTargetType(Pair<XSElementDeclaration, Boolean> context,
			ValueReference propName) {

		Expr path = propName.getAsXPath();
		if (!(path instanceof LocationPath)) {
			throw new IllegalArgumentException("XPath '" + propName + "' does not denote a location path.");
		}

		Pair<XSElementDeclaration, Boolean> currentEl = context;
		for (Object o : ((LocationPath) path).getSteps()) {
			if (o instanceof NameStep) {
				NameStep step = (NameStep) o;
				if (step.getAxis() == Axis.CHILD) {
					// TODO check predicates
					QName qName = getQName(step);
					int num = getNumber(step);
					currentEl = getTargetElement(currentEl, qName, num);
					if (currentEl == null) {
						throw new IllegalArgumentException("Unable to match XPath '" + propName
								+ "' to application schema. Step '" + qName + "' cannot be resolved.");
					}
				}
				else if (step.getAxis() == Axis.ATTRIBUTE) {
					QName qName = getQName(step);
					XSTypeDefinition typeDef = currentEl.first.getTypeDefinition();
					if (!(typeDef instanceof XSComplexTypeDefinition)) {
						throw new IllegalArgumentException("Unable to match XPath '" + propName
								+ "' to application schema. Referenced attribute does not exist.");
					}
					if (new QName(XSINS, "nil").equals(qName)) {
						if (!currentEl.first.getNillable()) {
							throw new IllegalArgumentException("Unable to match XPath '" + propName
									+ "' to application schema. Referenced element is not nillable.");
						}
						return new Pair<PrimitiveType, Boolean>(new PrimitiveType(BOOLEAN), TRUE);
					}
					XSComplexTypeDefinition complexTypeDef = (XSComplexTypeDefinition) typeDef;
					XSObjectList attrUses = complexTypeDef.getAttributeUses();
					for (int i = 0; i < attrUses.getLength(); i++) {
						XSAttributeUse attrUse = (XSAttributeUse) attrUses.item(i);
						QName attrName = getQName(attrUse.getAttrDeclaration());
						if (qName.equals(attrName)) {
							return new Pair<PrimitiveType, Boolean>(
									new PrimitiveType(attrUse.getAttrDeclaration().getTypeDefinition()),
									!attrUse.getRequired());
						}
					}
					throw new IllegalArgumentException("Unable to match XPath '" + propName
							+ "' to application schema. Referenced attribute does not exist.");
				}
				else {
					throw new IllegalArgumentException("Unable to match XPath '" + propName
							+ "'to application schema. Only child and attribute axis steps are supported.");
				}
			}
			else if (o instanceof AllNodeStep) {
				// self()
			}
			else if (o instanceof TextNodeStep) {
				// nothing to do
			}
			else {
				throw new IllegalArgumentException("Unable to infer type for XPath '" + propName
						+ "'. Expression may only contain name and text node steps.");
			}
		}

		XSTypeDefinition typeDef = currentEl.first.getTypeDefinition();
		if (typeDef instanceof XSComplexTypeDefinition) {
			PrimitiveType pt = getPrimitiveInterpretation(propName, (XSComplexTypeDefinition) typeDef);
			return new Pair<PrimitiveType, Boolean>(pt, currentEl.second);
		}
		return new Pair<PrimitiveType, Boolean>(new PrimitiveType((XSSimpleTypeDefinition) typeDef), currentEl.second);
	}

	private PrimitiveType getPrimitiveInterpretation(ValueReference propName, XSComplexTypeDefinition complexType) {
		short contentType = complexType.getContentType();
		if (contentType == CONTENTTYPE_SIMPLE) {
			return new PrimitiveType(complexType.getSimpleType());
		}
		if (contentType == CONTENTTYPE_MIXED) {
			return new PrimitiveType(STRING);
		}
		if (contentType == CONTENTTYPE_ELEMENT) {
			String msg = "XPath '" + propName + "' refers to a complex type with complex content.";
			throw new IllegalArgumentException(msg);
		}
		String msg = "XPath '" + propName + "' refers to an empty element type.";
		throw new IllegalArgumentException(msg);
	}

	private QName getQName(NameStep step) {
		String prefix = step.getPrefix();
		QName qName;
		if (prefix.isEmpty()) {
			qName = new QName(step.getLocalName());
		}
		else {
			String ns = nsBindings.translateNamespacePrefixToUri(prefix);
			qName = new QName(ns, step.getLocalName(), prefix);
		}
		return qName;
	}

	private QName getQName(XSAttributeDeclaration attrUse) {
		if (attrUse.getNamespace() == null || attrUse.getNamespace().isEmpty()) {
			return new QName(attrUse.getName());
		}
		return new QName(attrUse.getNamespace(), attrUse.getName());
	}

	private Pair<XSElementDeclaration, Boolean> getTargetElement(Pair<XSElementDeclaration, Boolean> context,
			QName elName, int num) {
		XSTypeDefinition typeDef = context.getFirst().getTypeDefinition();
		if (!(typeDef instanceof XSComplexTypeDefinition)) {
			throw new IllegalArgumentException("XPath refers to a simple type definition.");
		}
		XSParticle particle = ((XSComplexTypeDefinition) typeDef).getParticle();
		if (particle == null) {
			throw new IllegalArgumentException("XPath refers to an empty type definition.");
		}
		return getTargetElementTerm(new Pair<XSTerm, Boolean>(particle.getTerm(), null), elName, num);
	}

	private Pair<XSElementDeclaration, Boolean> getTargetElementTerm(Pair<XSTerm, Boolean> term, QName elName,
			int num) {
		if (term.first instanceof XSElementDeclaration) {
			XSElementDeclaration elDecl = (XSElementDeclaration) term.first;
			if (elDecl.getScope() == XSConstants.SCOPE_GLOBAL) {
				for (XSElementDeclaration substitution : appSchema.getGMLSchema()
					.getSubstitutions(elDecl, null, true, false)) {
					QName elDeclName = getQName(substitution);
					if (elName.equals(elDeclName)) {
						return new Pair<XSElementDeclaration, Boolean>(substitution, term.second);
					}
				}
			}
			else {
				QName elDeclName = getQName(elDecl);
				if (elName.equals(elDeclName)) {
					return new Pair<XSElementDeclaration, Boolean>(elDecl, term.second);
				}
			}
		}
		else if (term.first instanceof XSModelGroup) {
			XSModelGroup mg = (XSModelGroup) term.first;
			XSObjectList ol = mg.getParticles();
			for (int i = 0; i < ol.getLength(); i++) {
				XSParticle o = (XSParticle) ol.item(i);
				boolean voidable = o.getMinOccurs() == 0 || o.getMinOccurs() < num;
				Pair<XSElementDeclaration, Boolean> elDecl = getTargetElementTerm(
						new Pair<XSTerm, Boolean>(o.getTerm(), voidable), elName, num);
				if (elDecl != null) {
					return elDecl;
				}
			}
		}
		else if (term.first instanceof XSWildcard) {
			throw new IllegalArgumentException("Matching of wildcard elements not supported.");
		}
		else {
			throw new RuntimeException("Unexpected term type: " + term.getClass());
		}
		return null;
	}

	private QName getQName(XSElementDeclaration elDecl) {
		if (elDecl.getNamespace() == null || elDecl.getNamespace().isEmpty()) {
			return new QName(elDecl.getName());
		}
		return new QName(elDecl.getNamespace(), elDecl.getName());
	}

}