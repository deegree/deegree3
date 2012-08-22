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
package org.deegree.enterprise.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.w3c.dom.Node;

/**
 * This class respectively its method {@link #mapPropertyValue(Node)} can be used by XSLT scripts to map a node value
 * (key) to another, corresponding value (value). The mappings are taken from the properties file
 * <code>org.deegree.enterprise.servlet.wfsrequestmapping.properties</code>. If no matching value for a key is defined
 * in the properties file, the returned <code>String</code> is null.
 * <p>
 * The node reference passed to this method must point to an element that contains a single text node, e.g.
 * &lt;PropertyName&gt;/MyProperty/value&lt;/PropertyName&gt;
 * </p>
 * <p>
 * If a special behavior is needed by a deegree WFS instance and/or you do not want to edit the default properties and
 * use your own one you should write a class that extends this or it as pattern.
 *
 * @see #mapPropertyValue(Node)
 * @see java.util.Properties
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSRequestMapping {

    private static ILogger LOG = LoggerFactory.getLogger( WFSRequestMapping.class );

    /** TODO why is this here and ununused? */
    protected static String propertiesFile = "wfsrequestmapping.properties";

    private static Properties mapping = null;

    private static Properties prefixToNs = new Properties();

    private static Properties nsToPrefix = new Properties();

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
    static {
        mapping = new Properties();
        try {
            URL url = WFSRequestMapping.class.getResource( "/wfsrequestmapping.properties" );
            InputStream is = null;
            if ( url != null ) {
                is = url.openStream();
            } else {
                is = WFSRequestMapping.class.getResourceAsStream( "wfsrequestmapping.properties" );
            }
            InputStreamReader isr = new InputStreamReader( is );
            BufferedReader br = new BufferedReader( isr );
            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                if ( !line.trim().startsWith( "#" ) ) {
                    String[] tmp = StringTools.toArray( line, "=", false );
                    if ( tmp != null && tmp[0] != null && tmp[1] != null ) {
                        if ( tmp[0].startsWith( "$namespace." ) ) {
                            String pre = tmp[0].substring( tmp[0].indexOf( '.' ) + 1, tmp[0].length() );
                            prefixToNs.put( pre, tmp[1] );
                            nsToPrefix.put( tmp[1], pre );
                            try {
                                nsc.addNamespace( pre, new URI( tmp[1] ) );
                            } catch ( URISyntaxException e ) {
                                e.printStackTrace();
                            }
                        } else {
                            mapping.put( tmp[0], tmp[1] );
                        }
                    }
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * This method can be used by XSLT scripts to map a node value (key) to another, corresponding value (value). The
     * mappings are taken from the properties file
     * <code>org.deegree.enterprise.servlet.wfsrequestmapping.properties</code>. If no matching value for a key is
     * defined in the properties file, the returned <code>String</code> is null.
     * <p>
     * The node reference passed to this method must point to an element that contains a single text node, e.g.
     * &lt;PropertyName&gt;/MyProperty/value&lt;/PropertyName&gt;
     * </p>
     *
     * @param node
     *            node that will be mapped
     * @return mapping for the node as an XPath, null if no mapping is defined
     */
    public static String mapPropertyValue( Node node ) {

        String nde = null;
        String key = null;
        try {
            nde = XMLTools.getNodeAsString( node, ".", nsc, null );
            if ( nde.startsWith( "/" ) ) {
                key = '.' + nde;
            } else if ( nde.startsWith( "." ) ) {
                key = nde;
            } else {
                key = "./" + nde;
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        if ( mapping.getProperty( key ) != null ) {
            nde = mapping.getProperty( key );
        }
        LOG.logDebug( "mapped property: " + nde );
        return nde;
    }

    /**
     * This method can be used by XSLT scripts to map a node value (key) to another, corresponding value (value). The
     * mappings are taken from the properties file
     * <code>org.deegree.enterprise.servlet.wfsrequestmapping.properties</code>. If no matching value for a key is
     * defined in the properties file, the returned <code>String</code> is null.
     * <p>
     * The node reference passed to this method must point to an element that contains a single text node, e.g.
     * &lt;PropertyName&gt;/MyProperty/value&lt;/PropertyName&gt;
     * </p>
     *
     * @param node
     *            node that will be mapped
     * @param typeName
     *            feature type name
     * @return mapping for the node as an XPath, null if no mapping is defined
     */
    public static String mapPropertyValue( Node node, String typeName ) {
        String s = null;
        try {
            s = XMLTools.getNodeAsString( node, ".", nsc, null );
            if ( s.startsWith( "/" ) ) {
                s = s.substring( 1, s.length() );
            } else if ( s.startsWith( "." ) ) {
                s = s.substring( 2, s.length() );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        if ( s.indexOf( "SI_Gazetteer" ) > -1 ) {
            s = ( new StringBuilder( "./" ) ).append( s ).toString();
        } else if ( s.indexOf( typeName ) < 0 ) {
            s = ( new StringBuilder( String.valueOf( typeName ) ) ).append( '/' ).append( s ).toString();
        }
        try {
            PropertyPath pp = transformToPropertyPath( s, nsc, null );
            s = pp.getAsString();
        } catch ( InvalidParameterValueException e ) {
            e.printStackTrace();
        }
        LOG.logDebug( ( new StringBuilder( "mapped property: " ) ).append( mapping.getProperty( s ) ).toString() );
        String st = mapping.getProperty( s );
        if ( st == null ) {
            st = s;
        }
        return st;
    }

    /**
     * This method can be used by XSLT scripts to map a node value (key) to another, corresponding value (value). The
     * mappings are taken from the properties file
     * <code>org.deegree.enterprise.servlet.wfsrequestmapping.properties</code>. If no matching value for a key is
     * defined in the properties file, the returned <code>String</code> is null.
     * <p>
     * The node reference passed to this method must point to an element that contains a single text node, e.g.
     * &lt;PropertyName&gt;/MyProperty/value&lt;/PropertyName&gt;
     * </p>
     * 
     * @param node
     *            node that will be mapped
     * @param typeName
     *            feature type name
     * @param nsp
     * @return mapping for the node as an XPath, null if no mapping is defined
     */
    public static String mapPropertyValue( Node node, String typeName, String nsp ) {
        String s = null;
        try {
            s = XMLTools.getNodeAsString( node, ".", nsc, null );
            if ( s.startsWith( "/" ) ) {
                s = s.substring( 1, s.length() );
            } else if ( s.startsWith( "." ) ) {
                s = s.substring( 2, s.length() );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        if ( s.indexOf( "SI_Gazetteer" ) > -1 ) {
            s = ( new StringBuilder( "./" ) ).append( s ).toString();
        } else if ( s.indexOf( typeName ) < 0 ) {
            s = ( new StringBuilder( String.valueOf( typeName ) ) ).append( '/' ).append( s ).toString();
        }
        try {
            PropertyPath pp = transformToPropertyPath( s, nsc, createNsp( nsp ) );
            s = pp.getAsString();
        } catch ( InvalidParameterValueException e ) {
            e.printStackTrace();
        }
        LOG.logDebug( ( new StringBuilder( "mapped property: " ) ).append( mapping.getProperty( s ) ).toString() );
        String st = mapping.getProperty( s );
        if ( st == null ) {
            st = s;
        }
        return st;
    }

    private static Properties createNsp( String nspDec ) {
        Properties nsp = new Properties();
        String[] tmp = StringTools.toArray( nspDec, ";", false );
        for ( int i = 0; i < tmp.length; i++ ) {
            int p = tmp[i].indexOf( ':' );
            String pre = tmp[i].substring( 0, p );
            String val = tmp[i].substring( p + 1 );
            nsp.put( pre, val );
        }
        return nsp;
    }

    private static PropertyPath transformToPropertyPath( String propName, NamespaceContext nsContext, Properties nsp )
                            throws InvalidParameterValueException {
        String steps[] = propName.split( "/" );
        List<PropertyPathStep> propertyPathSteps = new ArrayList<PropertyPathStep>( steps.length );
        for ( int i = 0; i < steps.length; i++ ) {
            PropertyPathStep propertyStep = null;
            QualifiedName propertyName = null;
            String step = steps[i];
            boolean isAttribute = false;
            boolean isIndexed = false;
            int selectedIndex = -1;
            if ( step.startsWith( "@" ) ) {
                if ( i != steps.length - 1 ) {
                    StringBuilder msg = new StringBuilder( "PropertyName '" );
                    msg.append( propName ).append( "' is illegal: the attribute specifier may only " );
                    msg.append( "be used for the final step." );
                    throw new InvalidParameterValueException( msg.toString() );
                }
                step = step.substring( 1 );
                isAttribute = true;
            }
            if ( step.endsWith( "]" ) ) {
                if ( isAttribute ) {
                    StringBuilder msg = new StringBuilder( "PropertyName '" );
                    msg.append( propName ).append( "' is illegal: if the attribute specifier ('@') is used, " );
                    msg.append( "index selection ('[...']) is not possible." );
                    throw new InvalidParameterValueException( msg.toString() );
                }
                int bracketPos = step.indexOf( '[' );
                if ( bracketPos < 0 ) {
                    StringBuilder msg = new StringBuilder( "PropertyName '" );
                    msg.append( propName ).append( "' is illegal. No opening brackets found for step '" );
                    msg.append( step ).append( "'." );
                    throw new InvalidParameterValueException( msg.toString() );
                }
                try {
                    selectedIndex = Integer.parseInt( step.substring( bracketPos + 1, step.length() - 1 ) );
                } catch ( NumberFormatException e ) {
                    LOG.logError( e.getMessage(), e );
                    StringBuilder msg = new StringBuilder( "PropertyName '" );
                    msg.append( propName ).append( "' is illegal. Specified index '" );
                    msg.append( step.substring( bracketPos + 1, step.length() - 1 ) ).append( "' is not a number." );
                    throw new InvalidParameterValueException( msg.toString() );
                }
                step = step.substring( 0, bracketPos );
                isIndexed = true;
            }
            int colonPos = step.indexOf( ':' );
            String prefix = "";
            String localName = step;
            if ( colonPos > 0 ) {
                prefix = step.substring( 0, colonPos );
                localName = step.substring( colonPos + 1 );
            }
            URI nsURI = null;
            if ( nsp == null ) {
                nsURI = nsContext.getURI( prefix );
            } else {
                try {
                    nsURI = new URI( nsp.getProperty( prefix ) );
                    prefix = nsToPrefix.getProperty( nsURI.toString() );                    
                } catch ( URISyntaxException e ) {
                    throw new InvalidParameterValueException( e.getMessage(), e );
                }
            }
            if ( nsURI == null && prefix.length() > 0 ) {
                String msg = ( new StringBuilder( "PropertyName '" ) ).append( propName ).append(
                                                                                                  "' uses an unbound namespace prefix: " ).append(
                                                                                                                                                   prefix ).toString();
                throw new InvalidParameterValueException( msg );
            }
            propertyName = new QualifiedName( prefix, localName, nsURI );
            if ( isAttribute ) {
                propertyStep = PropertyPathFactory.createAttributePropertyPathStep( propertyName );
            } else if ( isIndexed ) {
                propertyStep = PropertyPathFactory.createPropertyPathStep( propertyName, selectedIndex );
            } else {
                propertyStep = PropertyPathFactory.createPropertyPathStep( propertyName );
            }
            propertyPathSteps.add( propertyStep );
        }

        return PropertyPathFactory.createPropertyPath( propertyPathSteps );
    }
}
