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

/**
 * 
 * Represents the OutputDescription section of the DescribeProcess response document of the WPS specification 1.0
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class OutputDescription {

    private String identifier;

    private String title;

    private String abstraCt;

    private OutputFormChoice outputFormChoice;

    /**
     *  
     * @return identifier of output element
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     *  
     * @param identifier of output element
     */
    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    /**
     *  
     * @return title of output element
     */
    public String getTitle() {
        return title;
    }

    /**
     *  
     * @param title of output element
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     *  
     * @return abstract of output element
     */
    public String getAbstraCt() {
        return abstraCt;
    }

    /**
     *  
     * @param abstract of output element
     */
    public void setAbstraCt( String abstraCt ) {
        this.abstraCt = abstraCt;
    }

    /**
     *  
     * @return outputformchoice of output element
     */
    public OutputFormChoice getOutputFormChoice() {
        return outputFormChoice;
    }

    /**
     *  
     * @param outputformchoice of output element
     */
    public void setOutputFormChoice( OutputFormChoice outputFormChoice ) {
        this.outputFormChoice = outputFormChoice;
    }

}
