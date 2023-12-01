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
package org.deegree.filter.expression;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XPathUtils;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.saxpath.Axis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Expression} that contains an XPath 1.0 expression, a simple property name or an
 * arbitrary identifier. Before Filter Encoding 2.0.0, this kind of expression was known
 * as <code>PropertyName</code>.
 * <p>
 * Depending on the content, the targeted property can be accessed as follows:
 * <ul>
 * <li>Arbitrary identifier: Use {@link #getAsText()}. This method always return not
 * <code>null<code>.</li>
 * <li>An XPath (1.0) expression: {@link #getAsXPath()} returns not <code>null<code>.</li>
 * <li>A (qualified) name: {@link #getAsQName()} returns not <code>null<code>.</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class ValueReference implements Expression {

	private static Logger LOG = LoggerFactory.getLogger(ValueReference.class);

	private NamespaceBindings bindings = new NamespaceBindings();

	private String text;

	private Expr xpath;

	private QName qName;

	/**
	 * Creates a new {@link ValueReference} instance from an encoded XPath-expression and
	 * the namespace bindings.
	 * @param text must be a valid XPath 1.0-expression, must not be <code>null</code>
	 * @param nsContext binding of the namespaces used in the XPath expression, may be
	 * <code>null</code>
	 */
	public ValueReference(String text, NamespaceContext nsContext) throws IllegalArgumentException {
		this.text = text;
		init(nsContext);
	}

	/**
	 * Creates a new {@link ValueReference} instance that selects a property.
	 * @param name qualified name of the property, never <code>null</code>
	 */
	public ValueReference(QName name) {
		NamespaceBindings nsContext = new NamespaceBindings();
		if (name.getNamespaceURI() != null) {
			String prefix = (name.getPrefix() != null && !"".equals(name.getPrefix())) ? name.getPrefix() : "app";
			nsContext.addNamespace(prefix, name.getNamespaceURI());
			this.text = prefix + ":" + name.getLocalPart();
		}
		else {
			this.text = name.getLocalPart();
		}
		init(nsContext);
	}

	private void init(NamespaceContext nsContext) {

		try {
			xpath = new BaseXPath(text, null).getRootExpr();
			LOG.debug("XPath: " + xpath);
		}
		catch (JaxenException e) {
			LOG.debug("'" + text + "' does not denote a valid XPath 1.0 expression.");
			return;
		}

		for (String prefix : XPathUtils.extractPrefixes(xpath)) {
			String ns = nsContext == null ? null : nsContext.translateNamespacePrefixToUri(prefix);
			LOG.debug(prefix + " -> " + ns);
			bindings.addNamespace(prefix, ns);
		}

		// check if it is a QName
		if (xpath instanceof LocationPath) {
			LocationPath lpath = (LocationPath) xpath;
			if (lpath.getSteps().size() == 1) {
				if (lpath.getSteps().get(0) instanceof NameStep) {
					NameStep step = (NameStep) lpath.getSteps().get(0);
					if (step.getAxis() == Axis.CHILD && step.getPredicates().isEmpty()
							&& !step.getLocalName().equals("*")) {
						String prefix = step.getPrefix();
						if (prefix.isEmpty()) {
							qName = new QName(step.getLocalName());
						}
						else {
							String ns = this.bindings.translateNamespacePrefixToUri(prefix);
							qName = new QName(ns, step.getLocalName(), prefix);
						}
						LOG.debug("QName: " + qName);
					}
				}
			}
		}
	}

	// TODO check if this should stay here
	public void set(String text, NamespaceContext nsContext) {
		this.text = text;
		init(nsContext);
	}

	/**
	 * Returns the <a href="http://jaxen.codehaus.org/">Jaxen</a> representation of the
	 * XPath expression, which provides access to the syntax tree.
	 * @return the compiled expression, or <code>null</code> if the property name is not
	 * an XPath expression
	 */
	public Expr getAsXPath() {
		return xpath;
	}

	/**
	 * Returns the property name value (an XPath-expression).
	 * @return the XPath property name, this may be an empty string, but never
	 * <code>null</code>
	 */
	public String getAsText() {
		return text;
	}

	/**
	 * If the property name is simple, the element name is returned.
	 * @return the qualified name value, or <code>null</code> if the property name is not
	 * simple
	 */
	public QName getAsQName() {
		return qName;
	}

	/**
	 * Returns the bindings for the namespaces used in the XPath expression.
	 * @return the namespace bindings, never <code>null</code>
	 */
	public NamespaceBindings getNsContext() {
		return bindings;
	}

	@Override
	public Type getType() {
		return Type.VALUE_REFERENCE;
	}

	@Override
	public <T> TypedObjectNode[] evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {
		return xpathEvaluator.eval(obj, this);
	}

	@Override
	public String toString() {
		return toString("");
	}

	@Override
	public String toString(String indent) {
		return indent + "-PropertyName ('" + text + "')\n";
	}

	@Override
	public Expression[] getParams() {
		return new Expression[0];
	}

	// TODO hashcode/equals unfortunately seem not to work on the xpath object
	// at least using the text is a better bet than using object identity
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof ValueReference)) {
			return false;
		}
		return text.equals(((ValueReference) other).text);
	}

	@Override
	public int hashCode() {
		return text.hashCode();
	}

}
