/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.metadata.ebrim;

/**
 * Enum for discriminating the registry object types of the ebRIM 3.0 information model.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public enum RIMType {

	/** TODO some details on semantics would be nice */
	AdhocQuery,
	/** TODO some details on semantics would be nice */
	Association,
	/** TODO some details on semantics would be nice */
	AuditableEvent,
	/** TODO some details on semantics would be nice */
	Classification,
	/** TODO some details on semantics would be nice */
	ClassificationNode,
	/** TODO some details on semantics would be nice */
	ClassificationScheme,
	/** TODO some details on semantics would be nice */
	ExternalIdentifier,
	/** TODO some details on semantics would be nice */
	ExternalLink,
	/** TODO some details on semantics would be nice */
	ExtrinsicObject,
	/** TODO some details on semantics would be nice */
	Federation,
	/** TODO some details on semantics would be nice */
	Organization,
	/** TODO some details on semantics would be nice */
	Person,
	/** TODO some details on semantics would be nice */
	RegistryObject,
	/** TODO some details on semantics would be nice */
	RegistryPackage,
	/** TODO some details on semantics would be nice */
	Service,
	/** TODO some details on semantics would be nice */
	ServiceBinding,
	/** TODO some details on semantics would be nice */
	SpecificationLink,
	/** TODO some details on semantics would be nice */
	User

}
