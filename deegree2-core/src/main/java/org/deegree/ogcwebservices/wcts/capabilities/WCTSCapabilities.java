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

package org.deegree.ogcwebservices.wcts.capabilities;

import org.deegree.owscommon_1_1_0.OWSCommonCapabilities;
import org.deegree.owscommon_1_1_0.OperationsMetadata;
import org.deegree.owscommon_1_1_0.ServiceIdentification;
import org.deegree.owscommon_1_1_0.ServiceProvider;

/**
 * The <code>WCTSCapabilities</code> encapsulates the capabilities document of the wcts 0.4.0, using the ows 1.1.0.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class WCTSCapabilities extends OWSCommonCapabilities {

    /**
     *
     */
    private static final long serialVersionUID = 8281912303859111001L;

    private final Content contents;

    /**
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param contents
     */
    public WCTSCapabilities( String version, String updateSequence, ServiceIdentification serviceIdentification,
                             ServiceProvider serviceProvider, OperationsMetadata operationsMetadata, Content contents ) {
        super( version, updateSequence, serviceIdentification, serviceProvider, operationsMetadata );
        this.contents = contents;
    }

    /**
     * @param other to copy from.
     */
    public WCTSCapabilities( WCTSCapabilities other ) {
        this( other.getVersion(),
              other.getUpdateSequence(),
              other.getServiceIdentification(),
              other.getServiceProvider(),
              other.getOperationsMetadata(),
              other.getContents() );
    }

    /**
     * @return the contents.
     */
    public final Content getContents() {
        return contents;
    }

}
