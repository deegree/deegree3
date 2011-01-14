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
package org.deegree.protocol.ows.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>PossibleValues</code> bean encapsulates the corresponding GetCapabilities response metadata element.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class PossibleValues {

    private List<String> value;

    private List<Range> range;

    private boolean anyvalue;

    private boolean novalue;

    private String referenceName;

    private String referenceURL;

    /**
     * @return value, may be empty but never <code>null</code>
     */
    public List<String> getValue() {
        if ( value == null ) {
            value = new ArrayList<String>();
        }
        return value;
    }

    /**
     * @return range, may be empty but never <code>null</code>
     */
    public List<Range> getRange() {
        if ( range == null ) {
            range = new ArrayList<Range>();
        }
        return range;
    }

    /**
     * 
     */
    public void setAnyValue() {
        anyvalue = true;
    }

    /**
     * @return anyvalue
     */
    public boolean getAnyValue() {
        return anyvalue;
    }

    /**
     * 
     */
    public void setNoValue() {
        novalue = true;
    }

    /**
     * @return novalue
     */
    public boolean getNoValue() {
        return novalue;
    }

    /**
     * @param referenceName
     */
    public void setReferenceName( String referenceName ) {
        this.referenceName = referenceName;
    }

    /**
     * @return referenceName, may be <code>null</code>.
     */
    public String getReferenceName() {
        return referenceName;
    }

    /**
     * @param referenceURL
     */
    public void setReferenceURL( String referenceURL ) {
        this.referenceURL = referenceURL;
    }

    /**
     * @return referenceURL, may be <code>null</code>.
     */
    public String getReferenceURL() {
        return referenceURL;
    }

}
