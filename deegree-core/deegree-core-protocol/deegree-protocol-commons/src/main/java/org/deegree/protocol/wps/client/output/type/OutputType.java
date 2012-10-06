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
package org.deegree.protocol.wps.client.output.type;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;

/**
 * Abstract base class for definitions of process output parameters.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class OutputType {

    private final CodeType id;

    private final LanguageString inputTitle;

    private final LanguageString inputAbstract;

    /**
     * Convenvience enum type for discriminating the different subclasses of {@link OutputType}.
     * 
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum Type {
        /** Instance is a {@link LiteralOutputType}. */
        LITERAL,
        /** Instance is a {@link BBoxOutputType}. */
        BBOX,
        /** Instance is a {@link ComplexOutputType}. */
        COMPLEX
    }
    
    /**
     * Creates a new {@link OutputType} instance.
     * 
     * @param id
     *            parameter identifier, must not be <code>null</code>
     * @param inputTitle
     *            parameter title, must not be <code>null</code>
     * @param inputAbstract
     *            abstract for the parameter, can be <code>null</code>
     */
    protected OutputType( CodeType id, LanguageString inputTitle, LanguageString inputAbstract ) {
        this.id = id;
        this.inputTitle = inputTitle;
        this.inputAbstract = inputAbstract;
    }

    /**
     * Returns the concrete output type of this instance.
     * 
     * @return the concrete output type, never <code>null</code> 
     */
    public abstract Type getType ();
    
    /**
     * Returns the parameter identifier.
     * 
     * @return the parameter identifier, never <code>null</code>
     */
    public CodeType getId() {
        return id;
    }

    /**
     * Returns the parameter title.
     * 
     * @return the parameter title, never <code>null</code>
     */
    public LanguageString getTitle() {
        return inputTitle;
    }

    /**
     * Returns the abstract for the parameter.
     * 
     * @return the abstract for the parameter, can be <code>null</code>
     */
    public LanguageString getAbstract() {
        return inputAbstract;
    }
}
