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
package org.deegree.portal.cataloguemanager.control;

import java.io.Serializable;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SearchResultInfo implements Serializable {

    private static final long serialVersionUID = -9038184179180791169L;

    private int numberOfRecordsReturned;

    private int numberOfRecordsMatched;

    private String cswURL;

    private String cswName;
    

    /**
     * @return the numberOfRecordsReturned
     */
    public int getNumberOfRecordsReturned() {
        return numberOfRecordsReturned;
    }

    /**
     * @param numberOfRecordsReturned
     *            the numberOfRecordsReturned to set
     */
    public void setNumberOfRecordsReturned( int numberOfRecordsReturned ) {
        this.numberOfRecordsReturned = numberOfRecordsReturned;
    }

    /**
     * @return the numberOfRecordsMatched
     */
    public int getNumberOfRecordsMatched() {
        return numberOfRecordsMatched;
    }

    /**
     * @param numberOfRecordsMatched
     *            the numberOfRecordsMatched to set
     */
    public void setNumberOfRecordsMatched( int numberOfRecordsMatched ) {
        this.numberOfRecordsMatched = numberOfRecordsMatched;
    }

    /**
     * @return the cswURL
     */
    public String getCswURL() {
        return cswURL;
    }

    /**
     * @param cswURL
     *            the cswURL to set
     */
    public void setCswURL( String cswURL ) {
        this.cswURL = cswURL;
    }

    /**
     * @return the cswName
     */
    public String getCswName() {
        return cswName;
    }

    /**
     * @param cswName
     *            the cswName to set
     */
    public void setCswName( String cswName ) {
        this.cswName = cswName;
    }

}
