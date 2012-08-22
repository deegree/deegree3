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

package org.deegree.owscommon_1_1_0;

import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.util.Pair;

/**
 * <code>BasicIdentification</code> is a bean representation of a basicIdentification type, which can be used for
 * identifying and describing a set of data. Defined in ows 1.1.0.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class BasicIdentification extends DescriptionBase {

    private final Pair<String, String> identifier;

    private final List<Metadata> metadatas;

    /**
     * @param keywords
     * @param abstracts
     * @param title
     * @param identifier
     *            a &lt; value, codeSpace &gt; pair
     * @param metadatas
     *            a list of metadatas
     */
    public BasicIdentification( List<String> title, List<String> abstracts, List<Keywords> keywords,
                                Pair<String, String> identifier, List<Metadata> metadatas ) {
        super( title, abstracts, keywords );
        this.identifier = identifier;
        if ( metadatas == null ) {
            this.metadatas = new ArrayList<Metadata>();
        } else {
            this.metadatas = metadatas;
        }
    }

    /**
     * @return the identifier &lt; value, codeSpace &gt; pair, may be <code>null</code>
     */
    public final Pair<String, String> getIdentifier() {
        return identifier;
    }

    /**
     * @return the metadatas may be empty but never <code>null</code>
     */
    public final List<Metadata> getMetadatas() {
        return metadatas;
    }

}
