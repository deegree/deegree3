//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.client.mdeditor.util;

import java.util.Comparator;

import org.deegree.commons.utils.StringPair;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class StringPairCompararator implements Comparator<StringPair> {

    private boolean sortFirst = true;

    public StringPairCompararator() {
    }

    public StringPairCompararator( boolean sortFirst ) {
        this.sortFirst = sortFirst;
    }

    @Override
    public int compare( StringPair o1, StringPair o2 ) {
        String v1 = getCompareValue( o1 );
        String v2 = getCompareValue( o2 );
        if ( v1 != null && v2 != null ) {
            return v1.compareToIgnoreCase( v2 );
        } else if ( v1 != null && v2 == null ) {
            return -1;
        } else if ( v2 != null && v1 == null ) {
            return 1;
        }
        return 0;
    }

    private String getCompareValue( StringPair sp ) {
        if ( sp != null ) {
            if ( sortFirst ) {
                return sp.first;
            }
            return sp.second;
        }
        return null;
    }

}
