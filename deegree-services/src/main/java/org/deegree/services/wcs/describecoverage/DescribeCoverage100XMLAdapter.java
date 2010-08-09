//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.services.wcs.describecoverage;

import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XPath;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.wcs.WCSRequest100XMLAdapter;

/**
 * This is an xml adapter for DescribeCoverage requests after the WCS 1.0.0 spec.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class DescribeCoverage100XMLAdapter extends WCSRequest100XMLAdapter {

    /**
     * @param rootElement
     */
    public DescribeCoverage100XMLAdapter( OMElement rootElement ) {
        this.setRootElement( rootElement );
    }

    /**
     * @return the parsed request
     * @throws OWSException
     */
    public DescribeCoverage parse()
                            throws OWSException {

        Version version = checkVersion( rootElement );

        List<OMElement> coverageElems = getElements( rootElement, new XPath( "/wcs:DescribeCoverage/wcs:Coverage",
                                                                             wcsNSContext ) );
        List<String> coverages = new ArrayList<String>( coverageElems.size() );
        for ( OMElement elem : coverageElems ) {
            coverages.add( elem.getText() );
        }
        return new DescribeCoverage( version, coverages );
    }

}
