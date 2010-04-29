//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.mdeditor.configuration;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class BeanListWriter {

    private static Map<String, List<String>> beans = new HashMap<String, List<String>>();

    public void addField( String beanName, String propName ) {
        if ( beans.containsKey( beanName ) ) {
            beans.get( beanName ).add( propName );
        } else {
            List<String> propNames = new ArrayList<String>();
            propNames.add( propName );
            beans.put( beanName, propNames );
        }
    }

    public void write()
                            throws FileNotFoundException, XMLStreamException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        FileOutputStream fos = new FileOutputStream(
                                                     "/home/lyn/workspace/deegree-mdeditor/src/main/java/org/deegree/client/mdeditor/form/beans.xml" );
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter( fos );
        writer.writeStartDocument();
        writer.writeStartElement( "BeanList" );

        for ( String key : beans.keySet() ) {
            writer.writeStartElement( "Bean" );
            writer.writeStartElement( "name" );
            // TODO: package!
            writer.writeCharacters( Utils.beginLowerCaseId( key ) );
            writer.writeEndElement();
            for ( String prop : beans.get( key ) ) {
                writer.writeStartElement( "property" );
                writer.writeCharacters( prop );
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.close();

    }

}
