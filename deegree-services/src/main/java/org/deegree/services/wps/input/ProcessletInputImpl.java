//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps.input;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.geometry.Envelope;
import org.deegree.services.jaxb.wps.ProcessletInputDefinition;
import org.deegree.services.wps.Processlet;

/**
 * An input parameter of a {@link Processlet} execution.
 * <p>
 * A {@link ProcessletInputImpl} instance is always one of the following:
 * <ul>
 * <li>{@link LiteralInputImpl}: Literal data of a simple quantity (e.g., one number) with optional UOM (unit-of-measure)
 * information.</li>
 * <li>{@link BoundingBoxInputImpl}: A spatial {@link Envelope}.</li>
 * <li>{@link ComplexInputImpl}: A complex parameter (e.g. a data structure encoded in XML or a raw stream).</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public abstract class ProcessletInputImpl implements ProcessletInput {

    /** Corresponding input argument definition from process description. */
    protected ProcessletInputDefinition definition;

    private LanguageString title;

    private LanguageString summary;

    /**
     * Creates a new {@link ProcessletInputImpl} instance.
     *
     * @param definition
     *            corresponding input definition from process description
     * @param title
     *            optional title supplied with the input parameter, may be null
     * @param summary
     *            optional narrative description supplied with the input parameter, may be null
     */
    protected ProcessletInputImpl( ProcessletInputDefinition definition, LanguageString title, LanguageString summary ) {
        this.definition = definition;
        this.title = title;
        this.summary = summary;
    }

    /**
     * Returns the type information for this input argument.
     *
     * @return the type information
     */
    public ProcessletInputDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns the identifier or name of the input parameter as defined in the process description.
     *
     * @return the identifier of the input parameter
     */
    public CodeType getIdentifier() {
        return new CodeType( definition.getIdentifier().getValue(), definition.getIdentifier().getCodeSpace() );
    }

    /**
     * Returns the title that has been supplied with the input parameter, normally available for display to a human.
     *
     * @return the title provided with the input, may be null
     */
    public LanguageString getTitle() {
        return title;
    }

    /**
     * Returns the narrative description that has been supplied with the input parameter, normally available for display
     * to a human.
     *
     * @return the abstract provided with the input, may be null
     */
    public LanguageString getAbstract() {
        return summary;
    }

    @Override
    public String toString() {
        return "Input parameter, identifier='" + getIdentifier() + "', title='" + title + "', abstract='" + summary
               + "'";
    }
}
