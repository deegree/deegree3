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

import static org.deegree.client.mdeditor.configuration.Utils.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.deegree.client.mdeditor.model.INPUT_TYPE;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class BeanWriter {

    private BufferedWriter writer;

    private String name;

    BeanWriter( String id ) throws IOException {
        this.name = createBeanName( id );
        writer = new BufferedWriter( new FileWriter(
                                                     "/home/lyn/workspace/deegree-mdeditor/src/main/java/org/deegree/client/mdeditor/form/gui/"
                                                                             + name + ".java" ) );

    }

    public String getFullBeanName() {
        return "org.deegree.client.mdeditor.form.gui." + name;
    }

    public void startWriting()
                            throws IOException, XMLStreamException {
        writer.write( "package org.deegree.client.mdeditor.form.gui;\n" );

        writer.write( "import javax.faces.bean.ManagedBean;\n" );
        writer.write( "import javax.faces.bean.SessionScoped;\n" );

        writer.write( "@ManagedBean\n" );
        writer.write( "@SessionScoped\n" );
        writer.write( "public class " + name + " {\n" );

    }

    public void finishWriting()
                            throws IOException, XMLStreamException {
        writer.write( "}" );
        writer.close();
    }

    /**
     * @param id
     * @param visible
     * @param helpAsLink
     * @param help
     * @param inputType
     * @param value
     * @throws IOException
     * @throws XMLStreamException
     */
    public void addInputField( String id, boolean visible, boolean required, String help, boolean helpAsLink,
                               INPUT_TYPE inputType, String defaultValue )
                            throws IOException, XMLStreamException {
        String value = defaultValue;
        writeVisibility( id, visible );
        writeRequired( id, required );
        writeHelp( id, helpAsLink, help );

        switch ( inputType ) {
        case TEXT:
        case STRING:
            value = "\"" + value + "\"";
            writeGetterSetter( "String", id, value );
            break;
        case INT:
            writeGetterSetter( "int", id, value );
            break;
        case DOUBLE:
            writeGetterSetter( "Double", id, value );
            break;
        case TIMESTAMP:
            value = "new Date(" + value + ")";
            writeGetterSetter( "Date", id, value );
            break;
        default:
            break;
        }

    }

    /**
     * @param id
     * @param visible
     * @param label
     * @param selectType
     * @param helpAsLink
     * @param help
     * @throws IOException
     * @throws XMLStreamException
     */
    public void addSelectField( String id, boolean visible, boolean required, String selectType, String help,
                                boolean helpAsLink )
                            throws IOException, XMLStreamException {
        writeVisibility( id, visible );
        writeRequired( id, required );
        writeHelp( id, helpAsLink, help );
        if ( "one".equals( selectType ) ) {
            writeGetterSetter( "Object", id, null );
        } else if ( "many".equals( selectType ) ) {
            // TODO
        }
    }

    private void writeHelp( String id, boolean helpAsLink, String help )
                            throws IOException {
        String prop = getHelpProp( id );
        writer.write( "private boolean " + prop + " = " + helpAsLink + ";\n" );
        writer.write( "public boolean " + getHelpMethode( id ) + "(){\n " );
        writer.write( "return " + prop + ";\n" );
        writer.write( "}\n" );

        String ucId = beginUpperCaseId( id );
        writer.write( "private String help" + ucId + " = \"" + help + "\";\n" );

        writer.write( "public String getHelp" + ucId + "(){\n " );
        writer.write( "return help" + ucId + ";\n" );
        writer.write( "}\n" );

    }

    private void writeRequired( String id, boolean required )
                            throws IOException {
        String prop = getRequiredProp( id );
        writer.write( "private boolean " + prop + " = " + required + ";\n" );
        writer.write( "public boolean " + getRequiredMethode( id ) + "(){\n " );
        writer.write( "return " + prop + ";\n" );
        writer.write( "}\n" );
    }

    private void writeGetterSetter( String type, String id, String defaultValue )
                            throws IOException {
        String lcId = beginLowerCaseId( id );
        String ucId = beginUpperCaseId( id );

        String var = "private " + type + " " + lcId;
        if ( defaultValue != null && defaultValue.length() > 0 ) {
            var += " = " + defaultValue + ";\n";
        } else {
            var += ";\n";
        }

        writer.write( var );

        writer.write( "public " + type + " get" + ucId + "(){\n " );
        writer.write( "return " + lcId + ";\n" );
        writer.write( "}\n" );

        writer.write( "public void set" + ucId + "( " + type + " id){\n " );
        writer.write( "this. " + lcId + " = id;\n" );
        writer.write( "}\n" );
    }

    private void writeVisibility( String id, boolean visible )
                            throws IOException {
        String prop = getVisibilityProp( id );

        writer.write( "private boolean " + prop + " = " + visible + ";\n" );
        writer.write( "public boolean " + getVisibilityMethode( id ) + "(){\n " );
        writer.write( "return " + prop + ";\n" );
        writer.write( "}\n" );
    }

}
