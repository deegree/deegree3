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
package org.deegree.framework.trigger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class TriggerConfigurationDocument extends XMLFragment {

    private ILogger LOG = LoggerFactory.getLogger( TriggerConfigurationDocument.class );

    private static NamespaceContext nsc = new NamespaceContext();
    static {
        try {
            nsc.addNamespace( "dgTr", new URI( "http://www.deegree.org/trigger" ) );
        } catch ( URISyntaxException e ) {
            // should never happen
            e.printStackTrace();
        }
    }

    /**
     * default constructor
     */
    public TriggerConfigurationDocument() {

    }

    /**
     * initialized the class by assigning a XML file
     *
     * @param file
     * @throws IOException
     * @throws SAXException
     */
    public TriggerConfigurationDocument( File file ) throws IOException, SAXException {
        super( file.toURL() );
    }

    /**
     *
     * @return TriggerCapabilities
     * @throws XMLParsingException
     * @throws TriggerException
     */
    public TriggerCapabilities parseTriggerCapabilities()
                            throws XMLParsingException, TriggerException {

        List list = XMLTools.getNodes( getRootElement(), "dgTr:class", nsc );
        Map<String, TargetClass> targetClasses = new HashMap<String, TargetClass>( list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            TargetClass tc = parserTargetClass( (Element) list.get( i ) );
            targetClasses.put( tc.getName(), tc );
        }

        return new TriggerCapabilities( targetClasses );
    }

    /**
     *
     * @param element
     * @return TargetClass
     * @throws XMLParsingException
     * @throws TriggerException
     */
    private TargetClass parserTargetClass( Element element )
                            throws XMLParsingException, TriggerException {

        String clName = XMLTools.getRequiredNodeAsString( element, "dgTr:name/text()", nsc );

        List list = XMLTools.getNodes( element, "dgTr:method", nsc );
        Map<String, TargetMethod> targetMethods = new HashMap<String, TargetMethod>( list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            TargetMethod tm = parseTargetMethod( (Element) list.get( i ) );
            targetMethods.put( tm.getName(), tm );
        }

        return new TargetClass( clName, targetMethods );
    }

    /**
     *
     * @param element
     * @return TargetMethod
     * @throws XMLParsingException
     * @throws TriggerException
     */
    private TargetMethod parseTargetMethod( Element element )
                            throws XMLParsingException, TriggerException {

        String mName = XMLTools.getRequiredNodeAsString( element, "dgTr:name/text()", nsc );

        TriggerCapability preTrigger = null;
        TriggerCapability postTrigger = null;

        // it is possible that no trigger is assigned to a method
        // in this case the present of a method just indicates that
        // it that Triggers can be assigned to it
        Node node = XMLTools.getNode( element, "dgTr:preTrigger/dgTr:trigger", nsc );
        if ( node != null ) {
            preTrigger = parseTriggerCapability( (Element) node );
        }
        node = XMLTools.getNode( element, "dgTr:postTrigger/dgTr:trigger", nsc );
        if ( node != null ) {
            postTrigger = parseTriggerCapability( (Element) node );
        }

        return new TargetMethod( mName, preTrigger, postTrigger );
    }

    /**
     *
     * @param element
     * @return trigger capability
     * @throws XMLParsingException
     * @throws TriggerException
     */
    private TriggerCapability parseTriggerCapability( Element element )
                            throws XMLParsingException, TriggerException {

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            XMLFragment doc = new XMLFragment();
            doc.setRootElement( element );
            LOG.logDebug( "Incoming trigger configuration element:\n " + doc.getAsPrettyString() );
        }
        // a node (if not null) may represents a simple Trigger or a
        // TriggerChain (which is a Trigger too)
        String trName = XMLTools.getRequiredNodeAsString( element, "dgTr:name/text()", nsc );
        String clName = XMLTools.getRequiredNodeAsString( element, "dgTr:performingClass/text()", nsc );
        Class clss = null;
        try {
            clss = Class.forName( clName );
            LOG.logDebug( "Found class: " + clss );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( Messages.getMessage( "FRAMEWORK_UNKNOWN_TRIGGERCLASS", clName ) );
        }

        if ( !Trigger.class.isAssignableFrom( clss ) ) {
            // class read from the configuration must be an implementation
            // of org.deegree.framework.trigger.Trigger
            throw new TriggerException( Messages.getMessage( "FRAMEWORK_INVALID_TRIGGERCLASS", clName ) );
        }

        Map<String, Class> paramTypes = new HashMap<String, Class>();
        Map<String, Object> paramValues = new HashMap<String, Object>();
        List<String> paramNames = new ArrayList<String>();
        List initParams = XMLTools.getNodes( element, "dgTr:initParam", nsc );
        parseInitParams( paramTypes, paramValues, paramNames, initParams );

        // get nested Trigger capabilities if available
        List nested = XMLTools.getNodes( element, "dgTr:trigger/dgTr:trigger", nsc );
        List<TriggerCapability> nestedList = new ArrayList<TriggerCapability>( nested.size() );
        for ( int i = 0; i < nested.size(); i++ ) {
            nestedList.add( parseTriggerCapability( (Element) nested.get( i ) ) );
        }

        return new TriggerCapability( trName, clss, paramNames, paramTypes, paramValues, nestedList );

    }

    /**
     *
     * @param paramTypes
     * @param paramValues
     * @param paramNames
     * @param initParams
     * @throws XMLParsingException
     */
    private void parseInitParams( Map<String, Class> paramTypes, Map<String, Object> paramValues,
                                  List<String> paramNames, List initParams )
                            throws XMLParsingException {
        for ( int i = 0; i < initParams.size(); i++ ) {
            String name = XMLTools.getRequiredNodeAsString( (Node) initParams.get( i ), "dgTr:name/text()", nsc );
            paramNames.add( name );
            String tmp = XMLTools.getRequiredNodeAsString( (Node) initParams.get( i ), "dgTr:type/text()", nsc );
            Class cl = null;
            try {
                cl = Class.forName( tmp );
            } catch ( ClassNotFoundException e ) {
                LOG.logError( e.getMessage(), e );
                throw new XMLParsingException( Messages.getMessage( "FRAMEWORK_UNKNOWN_INITPARAMCLASS", tmp ) );
            }
            tmp = XMLTools.getRequiredNodeAsString( (Node) initParams.get( i ), "dgTr:value/text()", nsc );
            Object value = null;
            try {
                value = getValue( cl, tmp );
            } catch ( MalformedURLException e ) {
                LOG.logError( e.getMessage(), e );
                throw new XMLParsingException( Messages.getMessage( "FRAMEWORK_TRIGGER_INITPARAM_PARSING", tmp,
                                                                    cl.getName() ) );
            }
            paramTypes.put( name, cl );
            paramValues.put( name, value );
        }
    }

    /**
     *
     * @param type
     * @param tmp
     * @return parameter value
     * @throws MalformedURLException
     */
    private Object getValue( Class type, String tmp )
                            throws MalformedURLException {
        Object value = null;
        if ( type.equals( Integer.class ) ) {
            value = Integer.parseInt( tmp );
        } else if ( type.equals( Double.class ) ) {
            value = Double.parseDouble( tmp );
        } else if ( type.equals( Float.class ) ) {
            value = Float.parseFloat( tmp );
        } else if ( type.equals( URL.class ) ) {
            value = new URL( tmp );
        } else if ( type.equals( Date.class ) ) {
            value = TimeTools.createCalendar( tmp ).getTime();
        } else if ( type.equals( String.class ) ) {
            value = tmp;
        }
        return value;
    }

}
