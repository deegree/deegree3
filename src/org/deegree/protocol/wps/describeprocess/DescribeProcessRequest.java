//$Header: /deegreerepository/deegree/resources/eclipse/svn_classfile_header_template.xml,v 1.2 2007/03/06 09:44:09 bezema Exp $
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

package org.deegree.protocol.wps.describeprocess;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wps.WPSRequest;

/**
 * Represents a WPS <code>DescribeProcess</code> request.
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: padberg$
 *
 * @version $Revision$, $Date: 08.05.2008 14:22:05$
 */
public class DescribeProcessRequest extends WPSRequest {

    private List<CodeType> identifiers = new ArrayList<CodeType>();

    /**
     * Creates a new {@link DescribeProcessRequest} instance.
     *
     * @param version
     *            WPS protocol version
     * @param language
     *            RFC 4646 language code of the human-readable text
     * @param identifiers
     *            identifiers for the processes to be described
     */
    public DescribeProcessRequest( Version version, String language, List<CodeType> identifiers ) {
        super( version, language );
        this.identifiers = identifiers;
    }

    /**
     * Returns the identifiers of the processes to be described.
     *
     * @return the identifiers, contains at least one identifier (or 'ALL')
     */
    public List<CodeType> getIdentifiers() {
        return identifiers;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( " Request: DescribeProcess\n" + super.toString() + ", identifiers: [ " );
        for ( CodeType identifier : identifiers ) {
            sb.append( identifier );
            sb.append( " " );
        }
        sb.append( "]" );
        return sb.toString();
    }
}
