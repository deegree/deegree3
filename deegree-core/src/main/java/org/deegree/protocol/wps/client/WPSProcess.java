//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.protocol.wps.client;

/**
 * Encapsulates the information of one process offered by a WPS and allows to execute it.
 * 
 * @see WPSClient
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSProcess {

    private String id;

    private String title;

    private String abstr;

    /**
     * Returns the process identifier.
     * 
     * @return identifier of the process
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the process title.
     * 
     * @return title of the process
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the process abstract.
     * 
     * @return abstract of the process
     */
    public String getAbstract() {
        return abstr;
    }

    // TODO the tricky part: find a concept for representing input and output parameters?

    public Object execute (Object inputs) {
        return null;
    }
    
    public Object getInputParamDeclarations() {
        return null;
    }

    public Object getOutputParamDeclarations() {
        return null;
    }
}
