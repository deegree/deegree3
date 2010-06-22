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
package org.deegree.protocol.wps.execute;

/**
 * 
 * Represents the DataInput section of the Execute chapter of the WPS specification 1.0
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class DataInputExecute {

    private String identifier;

    private String title;

    private String abstraCt;

    private InputFormChoiceExecute inputFormChoice;

    /**
     *  
     * @return identifier of DataInput
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     *  
     * @param title of DataInput
     */
    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    /**
     *  
     * @return title of DataInput
     */
    public String getTitle() {
        return title;
    }

    /**
     *  
     * @param identifier of DataInput
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     *  
     * @return abstract of DataInput
     */
    public String getAbstraCt() {
        return abstraCt;
    }

    /**
     *  
     * @param abstract of DataInput
     */
    public void setAbstraCt( String abstraCt ) {
        this.abstraCt = abstraCt;
    }

    /**
     *  
     * @return inputFormChoice of DataInput
     */
    public InputFormChoiceExecute getInputFormChoice() {
        return inputFormChoice;
    }

    /**
     *  
     * @param inputFormChoice of DataInput
     */
    public void setInputFormChoice( InputFormChoiceExecute inputFormChoice ) {
        this.inputFormChoice = inputFormChoice;
    }

}
