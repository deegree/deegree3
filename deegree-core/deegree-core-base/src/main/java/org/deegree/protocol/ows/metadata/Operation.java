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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.Pair;

/**
 * Encapsulates the metadata on a single operation of an OGC web service (as reported in the capabilities document).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Operation {

    private final String name;

    private final List<URL> getUrls = new ArrayList<URL>();

    private final List<URL> postUrls = new ArrayList<URL>();

    private final List<DCP> dcp;

    private final List<Domain> parameter;

    private final List<Domain> constraint;

    private final List<Pair<URL, URL>> metadata;

    public Operation( String name, List<DCP> dcps, List<Domain> params, List<Domain> constraints,
                      List<Pair<URL, URL>> metadata ) {

        this.name = name;
        this.dcp = dcps;
        this.parameter = params;
        this.constraint = constraints;
        this.metadata = metadata;

        for ( DCP dcp : dcps ) {
            for ( Pair<URL, List<Domain>> urls : dcp.getGetURLs() ) {
                getUrls.add( urls.first );
            }
            for ( Pair<URL, List<Domain>> urls : dcp.getGetURLs() ) {
                postUrls.add( urls.first );
            }
        }
    }

    /**
     * Returns the operation name.
     * 
     * @return the operation name, never <code>null</code>
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the endpoint {@link URL}s for this operation (HTTP-GET).
     * 
     * @return endpoint URLs, can be empty, but never <code>null</code>
     */
    public List<URL> getGetUrls() {
        return getUrls;
    }

    /**
     * Returns the endpoint {@link URL}s for this operation (HTTP-POST).
     * 
     * @return endpoint URLs, can be empty, but never <code>null</code>
     */
    public List<URL> getPostUrls() {
        return postUrls;
    }

    /**
     * @return dcp, never <code>null</code>.
     */
    public List<DCP> getDCPs() {
        return dcp;
    }

    /**
     * @return parameter, never <code>null</code>.
     */
    public List<Domain> getParameter() {
        return parameter;
    }

    /**
     * @return constraint, never <code>null</code>
     */
    public List<Domain> getConstraint() {
        return constraint;
    }

    /**
     * @return metadata, never <code>null</code>
     */
    public List<Pair<URL, URL>> getMetadata() {
        return metadata;
    }
}
