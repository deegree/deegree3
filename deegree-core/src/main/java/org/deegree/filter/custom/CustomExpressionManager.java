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
package org.deegree.filter.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for retrieving {@link CustomExpressionProvider} and {@link FunctionProvider} instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CustomExpressionManager {

    private static final Logger LOG = LoggerFactory.getLogger( CustomExpressionManager.class );

    private static ServiceLoader<CustomExpressionProvider> customExpressionLoader = ServiceLoader.load( CustomExpressionProvider.class );

    private static ServiceLoader<FunctionProvider> customFunctionLoader = ServiceLoader.load( FunctionProvider.class );

    private static Map<QName, CustomExpressionProvider> elNameToExpression;

    private static Map<String, FunctionProvider> nameToFunction;

    /**
     * Returns all available {@link CustomExpressionProvider}s.
     * 
     * @return all available functions, keys: name, value: CustomExpression
     */
    public static synchronized Map<QName, CustomExpressionProvider> getCustomExpressions() {
        if ( elNameToExpression == null ) {
            elNameToExpression = new HashMap<QName, CustomExpressionProvider>();
            try {
                for ( CustomExpressionProvider expr : customExpressionLoader ) {
                    LOG.debug( "Expression: " + expr + ", element name: " + expr.getElementName() );
                    if ( elNameToExpression.containsKey( expr.getElementName() ) ) {
                        LOG.error( "Multiple CustomExpression instances for element name: '" + expr.getElementName()
                                   + "' on classpath -- omitting '" + expr.getClass().getName() + "'." );
                        continue;
                    }
                    elNameToExpression.put( expr.getElementName(), expr );
                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            }
        }
        return elNameToExpression;
    }

    /**
     * Returns all available {@link FunctionProvider}s. Multiple functions with the same case ignored name are not
     * allowed.
     * 
     * @return all available functions, keys: name, value: CustomFunction
     */
    public static synchronized Map<String, FunctionProvider> getCustomFunctions() {
        if ( nameToFunction == null ) {
            nameToFunction = new HashMap<String, FunctionProvider>();
            try {
                for ( FunctionProvider function : customFunctionLoader ) {
                    LOG.debug( "Function: " + function + ", name: " + function.getName() );
                    String name = function.getName().toLowerCase();
                    if ( nameToFunction.containsKey( name ) ) {
                        LOG.error( "Multiple CustomFunction instances for name: '" + name
                                   + "' on classpath -- omitting '" + function.getClass().getName() + "'." );
                        continue;
                    }
                    nameToFunction.put( name, function );
                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            }
        }
        return nameToFunction;
    }

    /**
     * Returns the {@link CustomExpressionProvider} for the given element name.
     * 
     * @param elName
     *            name of the element, must not be <code>null</code>
     * @return custom expression instance, or <code>null</code> if there is no expression with this name
     */
    public static CustomExpressionProvider getExpression( QName elName ) {
        return getCustomExpressions().get( elName );
    }

    /**
     * Returns the {@link FunctionProvider} for the given name.
     * 
     * @param name
     *            name of the function, must not be <code>null</code>
     * @return custom function instance, or <code>null</code> if there is no function with this name
     */
    public static FunctionProvider getFunction( String name ) {
        return getCustomFunctions().get( name.toLowerCase() );
    }
}