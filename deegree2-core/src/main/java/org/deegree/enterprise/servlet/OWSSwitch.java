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
package org.deegree.enterprise.servlet;

import static java.lang.Double.parseDouble;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.util.KVP2Map.toMap;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.framework.log.ILogger;

/**
 * <code>OWSSwitch</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OWSSwitch extends HttpServlet {

    static final ILogger LOG = getLogger( OWSSwitch.class );

    private static final long serialVersionUID = 5831555086588516559L;

    private TreeMap<String, Rule> rulesOrder = new TreeMap<String, Rule>();

    private HashMap<Rule, String> addresses = new HashMap<Rule, String>();

    private String defaultAddress;

    @Override
    public void init( ServletConfig conf )
                            throws ServletException {
        Enumeration<?> enu = conf.getInitParameterNames();

        while ( enu.hasMoreElements() ) {
            String name = (String) enu.nextElement();
            String val = conf.getInitParameter( name );

            if ( name.toLowerCase().startsWith( "rule" ) ) {
                String[] ss = val.split( "\\s+" );
                Rule rule = new Rule( ss[0].toUpperCase(), ss[1], ss[2] );
                if ( ss[3].length() == 0 ) {
                    LOG.logWarning( "You configured an empty address, in the configuration line " + val
                                    + ". Is this intended?" );
                }
                addresses.put( rule, ss[3] );
                rulesOrder.put( name, rule );
            }
            if ( name.equalsIgnoreCase( "defaultaddress" ) ) {
                defaultAddress = val;
            }
        }

        if ( defaultAddress == null ) {
            LOG.logError( "You have to specify a default address." );
            throw new ServletException( "You have to specify a default address." );
        }
        if ( addresses.isEmpty() ) {
            LOG.logError( "You have to specify at least one rule." );
            throw new ServletException( "You have to specify at least one rule." );
        }
        LOG.logInfo( "OWSSwitch servlet initialized successfully." );
        LOG.logDebug( "Default address is '" + defaultAddress + "'" );
        if ( LOG.isDebug() ) {
            for ( Rule key : rulesOrder.values() ) {
                LOG.logDebug( "Rule '" + key + "' -> '" + addresses.get( key ) + "'" );
            }
        }
    }

    @Override
    public void service( ServletRequest req, ServletResponse res ) {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        Map<String, String> map = toMap( request );

        for ( Rule rule : rulesOrder.values() ) {
            if ( rule.eval( map ) ) {
                String loc = addresses.get( rule )
                             + ( request.getQueryString() == null ? "" : ( "?" + request.getQueryString() ) );
                if ( LOG.isDebug() ) {
                    LOG.logDebug( "Redirecting to address :'" + loc + "'" );
                }
                response.setHeader( "Location", loc );
                response.setStatus( 302 );
                return;
            }
        }

        String loc = defaultAddress + ( request.getQueryString() == null ? "" : ( "?" + request.getQueryString() ) );
        if ( LOG.isDebug() ) {
            LOG.logDebug( "Redirecting to default address :'" + loc + "'" );
        }
        response.setHeader( "Location", loc );
        response.setStatus( 302 );
    }

    static class Rule {
        String parameter, operation, value;

        double dvalue;

        Rule( String p, String o, String v ) throws ServletException {
            parameter = p;
            operation = o;
            value = v;

            if ( o.equals( "<" ) || o.equals( ">" ) || o.equals( "<=" ) || o.equals( ">=" ) ) {
                dvalue = parseDouble( v );
            } else {
                if ( !o.equals( "=" ) ) {
                    LOG.logError( "The operator '" + o + "' is not implemented." );
                    throw new ServletException( "The operator '" + o + "' is not implemented." );
                }
            }
        }

        boolean eval( Map<String, String> map ) {
            String val = map.get( parameter );
            if ( val == null ) {
                return false;
            }
            if ( operation.equals( "=" ) ) {
                return val.equals( value );
            }

            double v = parseDouble( val );

            if ( operation.equals( "<" ) ) {
                return v < dvalue;
            }
            if ( operation.equals( ">" ) ) {
                return v > dvalue;
            }
            if ( operation.equals( ">=" ) ) {
                return v >= dvalue;
            }
            if ( operation.equals( "<=" ) ) {
                return v <= dvalue;
            }
            return false;
        }

        @Override
        public String toString() {
            return parameter + " " + operation + " " + value;
        }
    }

}
