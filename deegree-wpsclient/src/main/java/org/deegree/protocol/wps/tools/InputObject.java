//$HeadURL: https://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.protocol.wps.tools;

/**
 * 
 * InputObject is used to assign one input. Identifier and InputObject are mandatory. Input will be set on
 * "as Reference" as default
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class InputObject {

    private String identifier;

    private Object inputObject;

    private boolean asReference;

    private String schema;

    private String body;

    private String encoding;

    private String method;

    private String mimeType;

    private String reference;

    private String bodyReferenceHref;

    /**
     * 
     * @param identifier
     * 
     * @param input
     * 
     * @param asReference
     * 
     */
    public InputObject( String identifier, Object input, boolean asReference ) {
        this.identifier = identifier;
        this.inputObject = input;
        this.asReference = asReference;
    }

    /**
     * 
     * @param identifier
     * 
     * @param input
     * 
     */
    public InputObject( String identifier, Object input ) {
        this.identifier = identifier;
        this.inputObject = input;
        this.asReference = true;

    }

    /**
     * 
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * 
     * @param identifier
     */
    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    /**
     * 
     * @return input
     */
    public Object getInput() {
        return inputObject;
    }

    /**
     * 
     * @param input
     */
    public void setInput( String input ) {
        this.inputObject = input;
    }

    /**
     * 
     * @return asReference
     */

    public boolean isAsReference() {
        return asReference;
    }

    /**
     * 
     * @param asReference
     */
    public void setAsReference( boolean asReference ) {
        this.asReference = asReference;
    }

    /**
     * 
     * @return schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 
     * @param schema
     */
    public void setSchema( String schema ) {
        this.schema = schema;
    }

    /**
     * 
     * @return body
     */
    public String getBody() {
        return body;
    }

    /**
     * 
     * @param body
     */
    public void setBody( String body ) {
        this.body = body;
    }

    /**
     * 
     * @return encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * 
     * @param encoding
     */
    public void setEncoding( String encoding ) {
        this.encoding = encoding;
    }

    /**
     * 
     * @return method
     */
    public String getMethod() {
        return method;
    }

    /**
     * 
     * @param method
     */
    public void setMethod( String method ) {
        this.method = method;
    }

    /**
     * 
     * @return mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 
     * @param mimeType
     */
    public void setMimeType( String mimeType ) {
        this.mimeType = mimeType;
    }

    /**
     * 
     * @return reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * 
     * @param reference
     */
    public void setReference( String reference ) {
        this.reference = reference;
    }

    /**
     * 
     * @return bodyReferenceHref
     */
    public String getBodyReferenceHref() {
        return bodyReferenceHref;
    }

    /**
     * 
     * @param bodyReferenceHref
     */
    public void setBodyReferenceHref( String bodyReferenceHref ) {
        this.bodyReferenceHref = bodyReferenceHref;
    }

}
