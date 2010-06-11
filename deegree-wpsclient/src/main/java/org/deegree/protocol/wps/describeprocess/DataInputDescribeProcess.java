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
package org.deegree.protocol.wps.describeprocess;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Represents the DataInput section of the DescribeProcess response document of the WPS specification 1.0
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class DataInputDescribeProcess {

    private String identifier;

    private String title;

    private String abstraCt;

    private String minOccurs;

    private String maxOccurs;

    private String metadata;// ows:Metadata

    private InputFormChoiceDescribeProcess inputFormChoice;

    private List<Object> valueList = new ArrayList<Object>();
 
    /**
     *  
     * @return identifier of Input
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     *  
     * @param identifier of Input
     */
    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    /**
     *  
     * @return title of Input
     */
    public String getTitle() {
        return title;
    }

    /**
     *  
     * @param title of Input
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     *  
     * @return abstract of Input
     */
    public String getAbstraCt() {
        return abstraCt;
    }
    
    /**
     *  
     * @param abstract of Input
     */
    public void setAbstraCt( String abstraCt ) {
        this.abstraCt = abstraCt;
    }

    /**
     *  
     * @return minOccurs of Input
     */
    public String getMinOccurs() {
        return minOccurs;
    }
    
    /**
     *  
     * @param minOccurs of Input
     */
    public void setMinOccurs( String minOccurs ) {
        this.minOccurs = minOccurs;
    }

    /**
     *  
     * @return maxOccurs of Input
     */
    public String getMaxOccurs() {
        return maxOccurs;
    }
    
    /**
     *  
     * @param maxOccurs of Input
     */
    public void setMaxOccurs( String maxOccurs ) {
        this.maxOccurs = maxOccurs;
    }
    
    /**
     *  
     * @return metadata of Input
     */
    public String getMetadata() {
        return metadata;
    }
    
    /**
     *  
     * @param metadata of Input
     */
    public void setMetadata( String metadata ) {
        this.metadata = metadata;
    }

    /**
     *  
     * @return InputFormChoice of Input
     */
    public InputFormChoiceDescribeProcess getInputFormChoice() {
        return inputFormChoice;
    }

    /**
     *  
     * @param InputFormChoice of Input
     */
    public void setInputFormChoice( InputFormChoiceDescribeProcess inputFormChoice ) {
        this.inputFormChoice = inputFormChoice;
    }

    /**
     *  
     * @return allowedValue of Input
     */
    public List<Object> getValueList() {
        return valueList;
    }

    /**
     *  
     * @param allowedValue of Input
     */
    public void addValue( Object value ) {
        valueList.add( value );
    }

}
