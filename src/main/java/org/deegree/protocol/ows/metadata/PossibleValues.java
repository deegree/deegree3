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
 * The <code></code> class TODO add class documentation here.
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

    public List<String> getValue() {
        if ( value == null ) {
            value = new ArrayList<String>();
        }
        return value;
    }

    public List<Range> getRange() {
        if ( range == null ) {
            range = new ArrayList<Range>();
        }
        return range;
    }

    public void setAnyValue() {
        anyvalue = true;
    }

    public boolean getAnyValue() {
        return anyvalue;
    }

    public void setNoValue() {
        novalue = true;
    }

    public boolean getNoValue() {
        return novalue;
    }

    public void setReferenceName( String referenceName ) {
        this.referenceName = referenceName;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceURL( String referenceURL ) {
        this.referenceURL = referenceURL;
    }

    public String getReferenceURL() {
        return referenceURL;
    }

}
