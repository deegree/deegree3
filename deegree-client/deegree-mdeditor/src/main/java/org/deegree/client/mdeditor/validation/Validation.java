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
package org.deegree.client.mdeditor.validation;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.deegree.client.mdeditor.gui.GuiUtils;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.InputFormField;
import org.deegree.client.mdeditor.model.VALIDATION_TYPE;
import org.slf4j.Logger;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class Validation {

    private static final Logger LOG = getLogger( Validation.class );

    public static List<String> validateFormFields( FacesContext fc, FormGroup formGroup ) {
        LOG.debug( "validate form fields of group with id " + formGroup.getId() );
        List<String> msgs = new ArrayList<String>();
        for ( FormElement fe : formGroup.getFormElements() ) {
            if ( fe instanceof FormGroup ) {
                msgs.addAll( validateFormFields( fc, (FormGroup) fe ) );
            } else {
                FormField ff = (FormField) fe;
                List<VALIDATION_TYPE> validationMap = ff.validate();
                addValidationMsg( fc, msgs, validationMap, ff );
            }
        }
        return msgs;
    }

    private static void addValidationMsg( FacesContext fc, List<String> msgs, List<VALIDATION_TYPE> validationErrors,
                                          FormField ff ) {
        for ( VALIDATION_TYPE key : validationErrors ) {
            String label = GuiUtils.getResourceTextOrKey( fc, "confLabels", ff.getLabel() );
            if ( ff instanceof InputFormField && ( (InputFormField) ff ).getValidation() != null ) {
                org.deegree.client.mdeditor.model.Validation v = ( (InputFormField) ff ).getValidation();
                switch ( key ) {
                case RANGE:
                    msgs.add( GuiUtils.getResourceText( fc, "mdLabels", "invalid_" + key, label, v.getMinValue(),
                                                        v.getMaxValue() ) );
                    break;
                case MIN:
                    msgs.add( GuiUtils.getResourceText( fc, "mdLabels", "invalid_" + key, label, v.getMaxValue() ) );
                    break;
                case MAX:
                    msgs.add( GuiUtils.getResourceText( fc, "mdLabels", "invalid_" + key, label, v.getMinValue() ) );
                    break;
                case LENGTH:
                    msgs.add( GuiUtils.getResourceText( fc, "mdLabels", "invalid_" + key, label, v.getLength() ) );
                    break;
                default:
                    msgs.add( GuiUtils.getResourceText( fc, "mdLabels", "invalid_" + key, label ) );
                    break;
                }
            } else {
                msgs.add( GuiUtils.getResourceText( fc, "mdLabels", "invalid_" + key, label ) );
            }
        }
    }

    public static void validateAgainstSchema() {
        // TODO
    }

    public static String validateAgainstExternalUnit( String validationURL, File exportedDataset )
                            throws Exception {
        LOG.debug( "validate dataset against external URL: " + validationURL );
        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost( validationURL );
        httpPost.addHeader( "Accept", "text/html" );

        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart( "dataFile", new FileBody( exportedDataset ) );
        httpPost.setEntity( reqEntity );

        HttpResponse response = httpclient.execute( httpPost );
        return parseServiceResponse( response );
    }

    private static String parseServiceResponse( HttpResponse response )
                            throws Exception {
        LOG.debug( "HTTP response code " + response.getStatusLine().getStatusCode() );
        if ( response.getStatusLine().getStatusCode() != 200 ) {
            return null;
        }
        InputStream is = ( (BasicHttpResponse) response ).getEntity().getContent();
        InputStreamReader isr = new InputStreamReader( is );

        StringBuffer sb = new StringBuffer( 10000 );
        int c = 0;
        while ( ( c = isr.read() ) > -1 ) {
            sb.append( (char) c );
        }
        isr.close();

        String s = sb.toString();

        s = "<html><head></head><body>" + s + "</body></html>";
        BufferedReader br = new BufferedReader( new StringReader( s ) );

        HTMLEditorKit htmlKit = new HTMLEditorKit();
        HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
        HTMLEditorKit.Parser parser = new ParserDelegator();
        HTMLEditorKit.ParserCallback callback = htmlDoc.getReader( 0 );
        parser.parse( br, callback, true );

        // Parse
        ElementIterator iterator = new ElementIterator( htmlDoc );
        Element element;
        StringBuffer text = new StringBuffer();
        while ( ( element = iterator.next() ) != null ) {
            AttributeSet attributes = element.getAttributes();
            Object name = attributes.getAttribute( StyleConstants.NameAttribute );
            if ( ( name instanceof HTML.Tag ) && ( ( name == HTML.Tag.IMPLIED ) ) ) {
                // Build up content text as it may be within multiple elements
                int count = element.getElementCount();
                for ( int i = 0; i < count; i++ ) {
                    Element child = element.getElement( i );
                    AttributeSet childAttributes = child.getAttributes();
                    if ( childAttributes.getAttribute( StyleConstants.NameAttribute ) == HTML.Tag.CONTENT ) {
                        int startOffset = child.getStartOffset();
                        int endOffset = child.getEndOffset();
                        int length = endOffset - startOffset;
                        text.append( htmlDoc.getText( startOffset, length ) );
                    }
                }
            }
        }
        return text.toString();

    }

}
