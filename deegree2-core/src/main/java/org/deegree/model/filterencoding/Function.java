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
package org.deegree.model.filterencoding;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.feature.Feature;
import org.w3c.dom.Element;

/**
 * Encapsulates the information of a <code>Function</code>element as defined in the Expression
 * DTD.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class Function extends Expression {

    private static final ILogger LOG = LoggerFactory.getLogger( Function.class );

    /** The Function's name (as specified in it's name attribute). */
    protected String name;

    /** The Function's arguments. */
    protected List<Expression> args;

    private static Map<String, Class<?>> functions;

    static {
        if ( Function.functions == null ) {
            Function.initialize();
        }
    }

    private static void initialize() {

        functions = new HashMap<String, Class<?>>();

        try {
            // initialize mappings with mappings from "functions.properties" file in this package
            InputStream is = Function.class.getResourceAsStream( "function.properties" );
            Properties props = new Properties();
            try {
                props.load( is );
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
            }
            Iterator<?> iter = props.keySet().iterator();
            while ( iter.hasNext() ) {
                String key = (String) iter.next();
                String className = props.getProperty( key );
                functions.put( key, Class.forName( className ) );
            }

            // override mappings with mappings from "functions.properties" file in root package
            is = Function.class.getResourceAsStream( "/function.properties" );
            if ( is != null ) {
                props = new Properties();
                props.load( is );
                iter = props.keySet().iterator();
                while ( iter.hasNext() ) {
                    String key = (String) iter.next();
                    String className = props.getProperty( key );
                    functions.put( key, Class.forName( className ) );
                }
            }

            for ( String functionName : functions.keySet() ) {
                LOG.logDebug( "Filter function '" + functionName + "' is handled by class '"
                              + functions.get( functionName ).getName() + "'." );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    protected Function() {
        // protected constructor?
    }

    /**
     * Constructs a new Function.
     *
     * @param name
     * @param args
     */
    public Function( String name, List<Expression> args ) {
        this();
        this.id = ExpressionDefines.FUNCTION;
        this.name = name;
        this.args = args;
    }

    /**
     *
     * @param args
     */
    public void setArguments( List<Expression> args ) {
        this.args = args;
    }

    /**
     * Given a DOM-fragment, a corresponding Expression-object is built. This method recursively
     * calls other buildFromDOM () - methods to validate the structure of the DOM-fragment.
     *
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     */
    public static Expression buildFromDOM( Element element )
                            throws FilterConstructionException {

        // check if root element's name equals 'Function'
        if ( !element.getLocalName().toLowerCase().equals( "function" ) ) {
            throw new FilterConstructionException( Messages.getMessage( "FILTER_WRONG_ROOTELEMENT" ) );
        }

        // determine the name of the Function
        String name = element.getAttribute( "name" );
        if ( name == null ) {
            throw new FilterConstructionException( Messages.getMessage( "FILTER_MISSING_NAME" ) );
        }

        // determine the arguments of the Function
        ElementList children = XMLTools.getChildElements( element );
        if ( children.getLength() < 1 ) {
            throw new FilterConstructionException( Messages.getMessage( "FILTER_MISSING_ELEMENT", name ) );
        }

        ArrayList<Expression> args = new ArrayList<Expression>( children.getLength() );
        for ( int i = 0; i < children.getLength(); i++ ) {
            args.add( Expression.buildFromDOM( children.item( i ) ) );
        }

        Class<?> function = Function.functions.get( name );
        if ( function == null ) {
            throw new FilterConstructionException( Messages.getMessage( "FILTER_UNKNOWN_FUNCTION", name ) );
        }
        Function func;
        try {
            func = (Function) function.newInstance();
        } catch ( InstantiationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new FilterConstructionException( e.getMessage() );
        } catch ( IllegalAccessException e ) {
            LOG.logError( e.getMessage(), e );
            throw new FilterConstructionException( e.getMessage() );
        }
        func.setName( name );
        func.setArguments( args );

        return func;
    }

    /**
     * Returns the Function's name.
     *
     * @return functions name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see org.deegree.model.filterencoding.Function#getName()
     *
     * @param name
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * returns the arguments of the function
     *
     * @return arguments of the function
     */
    public List<Expression> getArguments() {
        return this.args;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.model.filterencoding.Expression#getExpressionId()
     */
    @Override
    public int getExpressionId() {
        return ExpressionDefines.FUNCTION;
    }

    /**
     * Returns the <tt>Function</tt>'s value (to be used in the evaluation of a complexer
     * <tt>Expression</tt>).
     *
     * @param feature
     *            that determines the concrete values of <tt>PropertyNames</tt> found in the
     *            expression
     * @return the resulting value
     */
    @Override
    public abstract Object evaluate( Feature feature )
                            throws FilterEvaluationException;

    /**
     * Produces an indented XML representation of this object.
     *
     * @return xml representation
     */
    @Override
    public StringBuffer toXML() {
        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<ogc:Function name=\"" ).append( this.name ).append( "\">" );
        for ( int i = 0; i < this.args.size(); i++ ) {
            sb.append( this.args.get( i ).toXML() );
        }
        sb.append( "</ogc:Function>" );
        return sb;
    }
}
