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
package org.deegree.protocol.wps.client.process;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;

/**
 * Encapsulates all information on a WPS process that's available from the capabilities document.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ProcessInfo {

    private String version;

    private CodeType id;

    private LanguageString title;

    private LanguageString processAbstract;

    /**
     * Creates a new {@link ProcessInfo} instance.
     * 
     * @param id
     *            the identifier of the process, must not be <code>null</code>
     * @param title
     *            the title of the process, must not be <code>null</code>
     * @param processAbstract
     *            the abstract of the process, can be <code>null</code>
     * @param version
     *            the version of the process, must not be <code>null</code>
     */
    public ProcessInfo( CodeType id, LanguageString title, LanguageString processAbstract, String version ) {
        this.version = version;
        this.id = id;
        this.title = title;
        this.processAbstract = processAbstract;
    }

    /**
     * Returns the identifier of the process.
     * 
     * @return the identifier, never <code>null</code>
     */
    public CodeType getId() {
        return id;
    }

    /**
     * Returns the version of the process.
     * 
     * @return the version, never <code>null</code>
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the title of the process.
     * 
     * @return the title, never <code>null</code>
     */
    public LanguageString getTitle() {
        return title;
    }

    /**
     * Returns the abstract of the process.
     * 
     * @return the abstract, can be <code>null</code> (if the process description does not define an abstract)
     */
    public LanguageString getAbstract() {
        return processAbstract;
    }
}
