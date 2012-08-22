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

package org.deegree.security;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class AuthenticationDocument extends XMLFragment {

    private static final long serialVersionUID = 9122355584178027980L;

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

    /**
     *
     *
     */
    public AuthenticationDocument() {
        super();
    }

    /**
     *
     * @param doc
     * @param systemId
     * @throws MalformedURLException
     */
    public AuthenticationDocument( Document doc, String systemId ) throws MalformedURLException {
        super( doc, systemId );
    }

    /**
     *
     * @param element
     */
    public AuthenticationDocument( Element element ) {
        super( element );
    }

    /**
     *
     * @param file
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     */
    public AuthenticationDocument( File file ) throws MalformedURLException, IOException, SAXException {
        super( file );
    }

    /**
     *
     * @param elementName
     */
    public AuthenticationDocument( QualifiedName elementName ) {
        super( elementName );
    }

    /**
     *
     * @param reader
     * @param systemId
     * @throws SAXException
     * @throws IOException
     */
    public AuthenticationDocument( Reader reader, String systemId ) throws SAXException, IOException {
        super( reader, systemId );
    }

    /**
     *
     * @param url
     * @throws IOException
     * @throws SAXException
     */
    public AuthenticationDocument( URL url ) throws IOException, SAXException {
        super( url );
    }

    /**
     * parses the authentications document and returns the content as an instance of
     * {@link Authentications}
     *
     * @return new Authentications object
     * @throws XMLParsingException
     */
    public Authentications createAuthentications()
                            throws XMLParsingException {

        List<Node> methodNodes = XMLTools.getNodes( getRootElement(), "//Method", nsc );
        List<AbstractAuthentication> authentications = new ArrayList<AbstractAuthentication>();
        for ( Node node : methodNodes ) {
            String name = XMLTools.getRequiredNodeAsString( node, "./@name", nsc );
            String className = XMLTools.getRequiredNodeAsString( node, "./class/text()", nsc );

            // parameter type for map of init-params and authentication name
            Class<?>[] cl = new Class[2];
            cl[0] = String.class;
            cl[1] = Map.class;

            // set parameter to submit to the constructor
            Object[] o = new Object[2];
            o[0] = name;
            o[1] = createInitParams( node );

            try {
                Class<?> clzz = Class.forName( className );
                Constructor<?> con = clzz.getConstructor( cl );
                authentications.add( (AbstractAuthentication) con.newInstance( o ) );
            } catch ( Exception e ) {
                throw new InvalidParameterValueException( e.getMessage(), "class", className );
            }
        }
        return new Authentications( authentications );
    }

    /**
     *
     * @param methodNode
     * @return
     * @throws XMLParsingException
     */
    private Map<String, String> createInitParams( Node methodNode )
                            throws XMLParsingException {
        List<Node> nodes = XMLTools.getNodes( methodNode, "./init-param", nsc );
        Map<String, String> map = new HashMap<String, String>();
        for ( Node node : nodes ) {
            String name = XMLTools.getRequiredNodeAsString( node, "./name/text()", nsc );
            String value = XMLTools.getRequiredNodeAsString( node, "./value/text()", nsc );
            map.put( name, value );
        }
        return map;
    }

}
