//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.filter.expression.custom;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.Expression;

/**
 * Implementations of this class provide {@link Expression}s with custom XML encoding (i.e. they use a non-standard
 * element substitutable for the standard <code>ogc:expression</code> element).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface CustomExpressionProvider extends Expression {

    /**
     * Returns the element name used for encoding this expression.
     * 
     * @return the element name of the expression, never <code>null</code>
     */
    public QName getElementName();

    /**
     * Returns the object representation for the given <code>ogc:Function</code> element event (Filter Encoding 1.0.0)
     * that the cursor of the given {@link XMLStreamReader} points at.
     * <p>
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:function&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/ogc:function&gt;)</li>
     * </ul>
     * </p>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:Function&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/ogc:Function&gt;) afterwards
     * @return corresponding {@link Expression} object
     * @throws XMLParsingException
     *             if the element is not a valid "ogc:Function" element
     * @throws XMLStreamException
     */
    public CustomExpressionProvider parse100( XMLStreamReader xmlStream )
                            throws XMLStreamException;

    /**
     * Returns the object representation for the given <code>ogc:Function</code> element event (Filter Encoding 1.1.0)
     * that the cursor of the given {@link XMLStreamReader} points at.
     * <p>
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:function&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/ogc:function&gt;)</li>
     * </ul>
     * </p>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:Function&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/ogc:Function&gt;) afterwards
     * @return corresponding {@link Expression} object
     * @throws XMLParsingException
     *             if the element is not a valid "ogc:Function" element
     * @throws XMLStreamException
     */
    public CustomExpressionProvider parse110( XMLStreamReader xmlStream )
                            throws XMLStreamException;

    /**
     * Returns the object representation for the given <code>ogc:Function</code> element event (Filter Encoding 2.0.0)
     * that the cursor of the given {@link XMLStreamReader} points at.
     * <p>
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:function&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/ogc:function&gt;)</li>
     * </ul>
     * </p>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:Function&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/ogc:Function&gt;) afterwards
     * @return corresponding {@link Expression} object
     * @throws XMLParsingException
     *             if the element is not a valid "ogc:Function" element
     * @throws XMLStreamException
     */
    public CustomExpressionProvider parse200( XMLStreamReader xmlStream )
                            throws XMLStreamException;
}