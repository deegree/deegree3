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
package org.deegree.client.core.component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.deegree.client.core.utils.MessageUtils;
import org.deegree.commons.xml.schema.SchemaValidator;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@FacesComponent(value = "HtmlInputConfigurationXML")
public class HtmlInputConfigurationXML extends HtmlInputTextarea {

    public HtmlInputConfigurationXML() {
        setRendererType( "org.deegree.InputConfigurationXML" );
    }

    private enum AdditionalProperties {
        schemaURLS
    }

    @Override
    public void setValue( Object value ) {
        System.out.println( "setValue " + value );
        if ( !( value instanceof InputStream ) ) {
            throw new FacesException(
                                      "value of HtmlInputConfigurationXML component must be from type org.deegree.commons.xml.XMLAdapter" );
        }
        super.setValue( value );
    }

    public void setSchemaURLS( String schemaURLS ) {
        getStateHelper().put( AdditionalProperties.schemaURLS, schemaURLS );
    }

    public String getSchemaURLS() {
        return (String) getStateHelper().eval( AdditionalProperties.schemaURLS, null );
    }

    @Override
    protected Object getConvertedValue( FacesContext context, Object value )
                            throws ConverterException {
        if ( value != null ) {
            return new ByteArrayInputStream( value.toString().getBytes() );
        }
        return null;
    }

    @Override
    protected void validateValue( FacesContext context, Object newValue ) {
        super.validateValue( context, newValue );
        InputStream xml = (InputStream) newValue;

        // StringBuilder sb = new StringBuilder();
        // String line;
        // try {
        // BufferedReader reader = new BufferedReader( new InputStreamReader( xml, "UTF-8" ) );
        // try {
        // while ( ( line = reader.readLine() ) != null ) {
        // sb.append( line ).append( "\n" );
        // }
        // } finally {
        // reader.close();
        // }
        // } catch ( IOException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // System.out.println( sb.toString() );
        System.out.println(getSchemaURLS());

        List<String> results = SchemaValidator.validate( xml, getSchemaURLS().split( "," ) );
        if ( results.size() > 0 ) {
            FacesMessage message = MessageUtils.getFacesMessage(
                                                                 null,
                                                                 FacesMessage.SEVERITY_ERROR,
                                                                 "org.deegree.client.core.component.HtmlInputConfiguration.VALIDATION_FAILED",
                                                                 results );
            context.addMessage( getClientId( context ), message );
            setValid( false );
        } else {
            setValid( true );
        }
    }
}