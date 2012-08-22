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

package org.deegree.portal.context;

/**
 * Helper class to collect metadata about a WMC. It substitutes some of the metadata available in a
 * WMC and should be used for providing faster access to theses metadata.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class WMCMetadataSurrogate {

    private final String filename;

    private final String author;

    private final String title;

    private final String description;

    private final String[] keywords;

    /**
     * Creates a new <code>WMCMetadataSurrogate</code>.
     *
     * @param filename
     * @param author
     * @param title
     * @param description
     * @param keywords
     */
    public WMCMetadataSurrogate( String filename, String author, String title, String description, String[] keywords ) {
        this.filename = filename;
        this.author = author;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
    }

    /**
     * Creates a new <code>WMCMetadataSurrogate</code> using the filename and the
     * <code>ViewContext</code> vc.
     *
     * @param filename
     * @param vc
     *            the ViewContext used to initialize the title, the description and the keywords.
     *            Cannot be null.
     * @return a new <code>WMCMetadataSurrogate</code>
     */
    public static final WMCMetadataSurrogate createFromWMC( String filename, ViewContext vc ) {
        if ( vc == null ) {
            throw new IllegalArgumentException( "ViewContext cannot be null." );
        }

        General g = vc.getGeneral();

        return new WMCMetadataSurrogate( filename, g.getContactInformation().getIndividualName()[0], g.getTitle(),
                                         g.getAbstract(), g.getKeywords() );
    }

    /**
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return keywords
     */
    public String[] getKeywords() {
        return keywords;
    }

    /**
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return author
     */
    public String getAuthor() {
        return author;
    }

}
