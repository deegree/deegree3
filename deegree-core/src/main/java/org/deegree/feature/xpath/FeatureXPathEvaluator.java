//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.xpath;

import static java.util.Collections.synchronizedMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.PropertyName;
import org.deegree.gml.GMLVersion;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

/**
 * {@link XPathEvaluator} for {@link Feature} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureXPathEvaluator implements XPathEvaluator<Feature> {

    private final GMLVersion version;

    private static Map<Feature, Map<PropertyName, TypedObjectNode[]>> EVAL_CACHE = null;

    /**
     * temporary hack to enable caching again
     */
    public static void enableCache() {
        EVAL_CACHE = synchronizedMap( new HashMap<Feature, Map<PropertyName, TypedObjectNode[]>>() );
    }

    /**
     * Creates a new {@link FeatureXPathEvaluator} instance.
     * 
     * @param version
     *            gml version (determines the names and types of GML standard properties), must not be <code>null</code>
     */
    public FeatureXPathEvaluator( GMLVersion version ) {
        this.version = version;
    }

    @Override
    public TypedObjectNode[] eval( Feature context, PropertyName propName )
                            throws FilterEvaluationException {

        // simple property with just a simple element step?
        QName simplePropName = propName.getAsQName();
        if ( simplePropName != null ) {
            return context.getProperties( simplePropName, version );
        }

        TypedObjectNode[] resultValues = null;
        try {
            synchronized ( context ) {
                if ( EVAL_CACHE != null ) {
                    Map<PropertyName, TypedObjectNode[]> map = EVAL_CACHE.get( context );
                    if ( map != null ) {
                        resultValues = map.get( propName );
                        if ( resultValues != null ) {
                            return resultValues;
                        }
                    }
                }
                XPath xpath = new FeatureXPath( propName.getPropertyName(), context, version );
                xpath.setNamespaceContext( propName.getNsContext() );
                List<?> selectedNodes;
                selectedNodes = xpath.selectNodes( new GMLObjectNode<Feature>( null, context, version ) );
                resultValues = new TypedObjectNode[selectedNodes.size()];
                int i = 0;
                for ( Object node : selectedNodes ) {
                    if ( node instanceof XPathNode<?> ) {
                        resultValues[i++] = ( (XPathNode<?>) node ).getValue();
                    } else if ( node instanceof String || node instanceof Double || node instanceof Boolean ) {
                        resultValues[i++] = new PrimitiveValue( node );
                    } else {
                        throw new RuntimeException( "Internal error. Encountered unexpected value of type '"
                                                    + node.getClass().getName() + "' (=" + node
                                                    + ") during XPath-evaluation." );
                    }
                }
                if ( EVAL_CACHE != null ) {
                    Map<PropertyName, TypedObjectNode[]> map = EVAL_CACHE.get( context );
                    if ( map == null ) {
                        map = synchronizedMap( new HashMap<PropertyName, TypedObjectNode[]>() );
                        EVAL_CACHE.put( context, map );
                    }
                    map.put( propName, resultValues );
                }
            }
        } catch ( JaxenException e ) {
            throw new FilterEvaluationException( e.getMessage() );
        }
        return resultValues;
    }

    @Override
    public String getId( Feature context ) {
        return context.getId();
    }
}