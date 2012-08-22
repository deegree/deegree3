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
package org.deegree.portal.standard.gazetteer;

import java.util.Map;

import org.deegree.datatypes.QualifiedName;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class HierarchyNode {

    private QualifiedName featureType;

    private String name;

    private boolean freeSearch;

    private HierarchyNode childNode;

    private Map<String, String> properties;

    private boolean stricMode = true;

    private boolean matchCase = true;

    /**
     * 
     * @param featureType
     * @param properties
     * @param name
     * @param freeSearch
     */
    public HierarchyNode( QualifiedName featureType, Map<String, String> properties, String name, boolean freeSearch ) {
        this.featureType = featureType;
        this.name = name;
        this.freeSearch = freeSearch;
        this.properties = properties;
    }

    /**
     * 
     * @param featureType
     * @param properties
     * @param name
     * @param freeSearch
     * @param stricMode
     * @param matchCase
     */
    public HierarchyNode( QualifiedName featureType, Map<String, String> properties, String name, boolean freeSearch,
                          boolean stricMode, boolean matchCase ) {
        this( featureType, properties, name, freeSearch );
        this.stricMode = stricMode;
        this.matchCase = matchCase;
    }

    /**
     * @return the featureType
     */
    public QualifiedName getFeatureType() {
        return featureType;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the childNode
     */
    public HierarchyNode getChildNode() {
        return childNode;
    }

    /**
     * @param childNode
     *            the childNode to set
     */
    public void setChildNode( HierarchyNode childNode ) {
        this.childNode = childNode;
    }

    /**
     * 
     * @return true if type supports a free text search; false if just selection from a list is supported
     */
    public boolean supportFreeSearch() {
        return freeSearch;
    }

    /**
     * 
     * @return property names assigned to a well known gazetteer properties.
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * @return the stricMode
     */
    public boolean isStricMode() {
        return stricMode;
    }

    /**
     * @return the matchCase
     */
    public boolean isMatchCase() {
        return matchCase;
    }

}
