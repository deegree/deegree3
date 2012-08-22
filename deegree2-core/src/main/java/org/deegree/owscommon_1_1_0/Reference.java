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
 * <code>Reference</code> encapsulates the reference element of ows 1.1.0
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class Reference {

    private final String hrefAttribute;

    private final String roleAttribute;

    private final String typeAttribute;

    private final Pair<String, String> identifier;

    private final List<String> abstracts;

    private final String format;

    private final List<Metadata> metadatas;

    /**
     * @param hrefAttribute
     * @param roleAttribute
     * @param typeAttribute
     * @param identifier
     *            a &lt; value, codeSpace &gt; pair
     * @param abstracts
     * @param format
     * @param metadatas
     *            a list of metadatas
     */
    public Reference( String hrefAttribute, String roleAttribute, String typeAttribute,
                      Pair<String, String> identifier, List<String> abstracts, String format, List<Metadata> metadatas ) {
        this.hrefAttribute = hrefAttribute;
        this.roleAttribute = roleAttribute;
        this.typeAttribute = typeAttribute;
        this.identifier = identifier;
        if ( abstracts == null ) {
            abstracts = new ArrayList<String>();
        }
        this.abstracts = abstracts;
        this.format = format;
        if ( metadatas == null ) {
            this.metadatas = new ArrayList<Metadata>();
        } else {
            this.metadatas = metadatas;
        }
    }

    /**
     * @return the hrefAttribute.
     */
    public final String getHrefAttribute() {
        return hrefAttribute;
    }

    /**
     * @return the typeAttribute.
     */
    public final String getTypeAttribute() {
        return typeAttribute;
    }

    /**
     * @return the identifier &lt; value, codeSpace &gt; pair, may be <code>null</code>
     */
    public final Pair<String, String> getIdentifier() {
        return identifier;
    }

    /**
     * @return the abstracts, which may be empty but never <code>null</code>
     */
    public final List<String> getAbstracts() {
        return abstracts;
    }

    /**
     * @return the format.
     */
    public final String getFormat() {
        return format;
    }

    /**
     * @return the metadatas a list of metadatas may be empty but never <code>null</code>
     */
    public final List<Metadata> getMetadatas() {
        return metadatas;
    }

    /**
     * @return the roleAttribute.
     */
    public final String getRoleAttribute() {
        return roleAttribute;
    }

}
