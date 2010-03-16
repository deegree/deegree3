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
package org.deegree.feature.xpath;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.gml.GMLVersion;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;

/**
 * <a href="http://jaxen.codehaus.org/">Jaxen</a> XPath implementation for {@link Feature} objects.
 * <p>
 * This is the entry point for matching an XPath expression against a {@link Feature}. Let <code>fc</code> be a
 * {@link FeatureCollection} that you want to match against. Create a compiled XPath object, then match it against one
 * or more context nodes using the {@link #selectNodes(Object)} method, as in the following example (which features the
 * infamous Philosoper feature type):
 * </p>
 * 
 * <pre>
 * XPath xpath = new FeatureXPath( &quot;gml:featureMember/app:Philosopher/app:friend/app:Philosopher//app:name&quot; );
 * xpath.setNamespaceContext( nsContext );
 * List&lt;?&gt; selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
 * </pre>
 * 
 * @see GMLObjectNode
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class FeatureXPath extends BaseXPath {

    private static final long serialVersionUID = 2352279998281119079L;

    /**
     * Create a new <code>FeatureXPath</code> from an XPath expression string.
     * 
     * @param xpathExpr
     *            the XPath expression
     * @param version
     *            determines the names and types of the standard GML properties, can be <code>null</code> (if no
     *            properties such as "gml:name" are used)
     * @throws JaxenException
     *             if there is a syntax error in the expression
     */
    public FeatureXPath( String xpathExpr, GMLVersion version ) throws JaxenException {
        super( xpathExpr, new FeatureNavigator( null, version ) );
    }

    /**
     * Create a new <code>FeatureXPath</code> from an XPath expression string.
     * 
     * @param xpathExpr
     *            the XPath expression
     * @param rootFeature
     *            root of the navigation hierarchy (document node)
     * @param version
     *            determines the names and types of the standard GML properties, can be <code>null</code> (if no
     *            properties such as "gml:name" are used)
     * @throws JaxenException
     *             if there is a syntax error in the expression
     */
    public FeatureXPath( String xpathExpr, Feature rootFeature, GMLVersion version ) throws JaxenException {
        super( xpathExpr, new FeatureNavigator( rootFeature, version ) );
    }
}
