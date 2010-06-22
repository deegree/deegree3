//$HeadURL: svn+ssh://georg@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.protocol.wps.execute;

/**
 * 
 * Represents the OutputDefinition section of the Execute chapter of the WPS specification 1.0
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class OutputDefinition {

    private String mimeType;

    private String encoding;

    private String schema;

    private String uom;

    private boolean asReference;

    private String identifier;

    private String title;

    private String abstraCt;

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
     * @return uom
     */
    public String getUom() {
        return uom;
    }

    /**
     *  
     * @param uom
     */
    public void setUom( String uom ) {
        this.uom = uom;
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
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     *  
     * @param title
     */
    public void setTitle( String title ) {
        this.title = title;
    }
    
    /**
     *  
     * @return abstract
     */
    public String getAbstraCt() {
        return abstraCt;
    }

    /**
     *  
     * @param abstract
     */
    public void setAbstraCt( String abstraCt ) {
        this.abstraCt = abstraCt;
    }

}
