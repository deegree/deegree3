/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.protocol.wms.filter;

import static org.deegree.filter.function.ParameterType.STRING;
import static org.deegree.filter.function.ParameterType.ANYTYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.StringUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.Function;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.filter.function.ParameterType;
import org.deegree.geometry.Envelope;
import org.deegree.workspace.Workspace;

/**
 * 
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class EnvFunction implements FunctionProvider {

    private static final String NAME = "env";

    private static final List<ParameterType> INPUTS = new ArrayList<ParameterType>( 2 );

    static {
        INPUTS.add( STRING );
        INPUTS.add( ANYTYPE );
    }

    static final ThreadLocal<Map<String, Object>> env = new ThreadLocal<Map<String, Object>>();

    public static ThreadLocal<Map<String, Object>> getCurrentEnvValue() {
        return env;
    }

    @Override
    public void init( Workspace ws ) {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<ParameterType> getArgs() {
        return INPUTS;
    }

    @Override
    public ParameterType getReturnType() {
        return ANYTYPE;
    }

    @Override
    public Function create( List<Expression> params ) {
        return new Function( NAME, params ) {
            @Override
            public <T> TypedObjectNode[] evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                                    throws FilterEvaluationException {
                TypedObjectNode[] key = getParams()[0].evaluate( obj, xpathEvaluator );
                TypedObjectNode[] def = getParams()[1].evaluate( obj, xpathEvaluator );
                if ( key.length != 1 ) {
                    throw new FilterEvaluationException( "The " + NAME + " function's first argument must "
                                                         + "evaluate to exactly one value." );
                }
                if ( def.length != 1 ) {
                    throw new FilterEvaluationException( "The " + NAME + " function's second argument must "
                                                         + "evaluate to exactly one value." );
                }

                Map<String, Object> map = env.get();
                Object result = map != null ? map.get( key[0].toString() ) : null;

                if ( result == null ) {
                    result = def[0];
                }

                if ( result != null ) {
                    if ( result instanceof TypedObjectNode ) {
                        return new TypedObjectNode[] { (TypedObjectNode) result };
                    }
                    try {
                        return new TypedObjectNode[] { new PrimitiveValue( result ) };
                    } catch ( Exception ignored ) {
                        return new TypedObjectNode[] { new PrimitiveValue( result.toString(),
                                                                           new PrimitiveType( BaseType.STRING ) ) };
                    }
                } else {
                    return new TypedObjectNode[] {};
                }
            }
        };
    }

    public static Map<String, Object> parse( Map<String, String> map, Envelope box, ICRS crs, int width, int height,
                                             double scale ) {
        Map<String, Object> res = new HashMap<>();
        String env = map != null ? map.get( "ENV" ) : null;

        for ( String word : StringUtils.splitEscaped( env, ';', 0 ) ) {
            List<String> keyValue = StringUtils.splitEscaped( word, ':', 2 );
            String raw = keyValue.size() == 1 ? "true" : StringUtils.unescape( keyValue.get( 1 ) );

            // no further key based parsing is available at the moment
            res.put( keyValue.get( 0 ), raw );
        }

        res.put( "wms_bbox", box );
        res.put( "wms_crs", crs );
        res.put( "wms_srs", crs != null ? crs.getName() : null );
        res.put( "wms_width", width );
        res.put( "wms_height", height );
        res.put( "wms_scale_denominator", scale );

        return res;
    }
}
