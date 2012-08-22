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
package org.deegree.portal.context;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MapModelEntry {

    private String identifier;

    private String title;

    private boolean hidden;

    protected List<String> selectedFor;

    protected LayerGroup parent;

    protected MapModel owner;

    /**
     * @param identifier
     * @param title
     * @param hidden
     * @param parent
     * @param owner
     */
    public MapModelEntry( String identifier, String title, boolean hidden, LayerGroup parent, MapModel owner ) {
        this.identifier = identifier;
        this.title = title;
        this.hidden = hidden;
        this.selectedFor = new ArrayList<String>();
        this.parent = parent;
        this.owner = owner;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * @return the hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * @param hidden
     *            the hidden to set
     */
    public void setHidden( boolean hidden ) {
        this.hidden = hidden;
    }

    /**
     * @return the selectedFor
     */
    public List<String> getSelectedFor() {
        return selectedFor;
    }

    /**
     * @param selectedFor
     *            the selectedFor to set
     */
    public void setSelectedFor( List<String> selectedFor ) {
        this.selectedFor = selectedFor;
    }

    /**
     * @param selectedFor
     *            the selectedFor to add
     */
    public void addSelectedFor( String selectedFor ) {
        this.selectedFor.add( selectedFor );
    }

    /**
     * @param selectedFor
     *            the selectedFor to remove
     */
    public void removeSelectedFor( String selectedFor ) {
        this.selectedFor.remove( selectedFor );
    }

    /**
     * @return the parent
     */
    public LayerGroup getParent() {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent( LayerGroup parent ) {
        this.parent = parent;
    }

    /**
     * @return the owner
     */
    public MapModel getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if ( obj instanceof MapModelEntry ) {
            return getIdentifier().equals( ( (MapModelEntry) obj ).getIdentifier() );
        }
        return false;
    }
}
