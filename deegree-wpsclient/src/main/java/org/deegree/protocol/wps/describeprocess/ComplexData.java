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
 * represents the Element ComplexData section of the DescribeProcess response document of the WPS 1.0 specification
 * 
 * @author <a href="walenciak@uni-heidelberg">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class ComplexData {

    private Format defaulT;

    private List<Format> supportedList = new ArrayList<Format>();

    private String maximumMegaBytes;

    /**
     * 
     * @return Format of ComplexData
     */
    public Format getDefaulT() {
        return defaulT;
    }

    /**
     * 
     * @param Format
     *            of ComplexData
     */
    public void setDefaulT( Format defaulT ) {
        this.defaulT = defaulT;
    }

    /**
     * 
     * @return List of supported formats of ComplexData
     */
    public List<Format> getSupported() {
        return supportedList;
    }

    /**
     * 
     * @param supported
     *            Format of ComplexData
     */
    public void addSupported( Format supported ) {
        supportedList.add( supported );
    }

    /**
     * 
     * @return maximum size of ComplexData
     */
    public String getMaximumMegaBytes() {
        return maximumMegaBytes;
    }

    /**
     * 
     * @param maximumMegaBytes
     *            of ComplexData
     */
    public void setMaximumMegaBytes( String maximumMegaBytes ) {
        this.maximumMegaBytes = maximumMegaBytes;
    }

}
