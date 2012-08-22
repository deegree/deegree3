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
package org.deegree.portal.standard.admin.model;

import java.util.ArrayList;
import java.util.List;

/**
 * bean mapping a GUI area using fields that can be mapped to extJs tree node JSON objects
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExtJsAreaBean {
    private String text;

    private String id;

    private String cls = "folder";

    private boolean leaf = false;

    @SuppressWarnings("unchecked")
    private List children = new ArrayList();

    /**
     * @return the leaf
     */
    public boolean isLeaf() {
        return leaf;
    }

    /**
     * @param leaf
     *            the leaf to set
     */
    public void setLeaf( boolean leaf ) {
        this.leaf = leaf;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText( String text ) {
        this.text = text;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId( String id ) {
        this.id = id;
    }

    /**
     * @return the children
     */
    @SuppressWarnings("unchecked")
    public List getChildren() {
        return children;
    }

    /**
     * @param children
     *            the children to set
     */
    @SuppressWarnings("unchecked")
    public void setChildren( List children ) {
        this.children = children;
    }

    /**
     * @return the cls
     */
    public String getCls() {
        return cls;
    }

    /**
     * @param cls
     *            the cls to set
     */
    public void setCls( String cls ) {
        this.cls = cls;
    }

}
