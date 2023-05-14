/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.metadata.iso.types;

import java.util.List;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class Constraint {

	private List<String> limitations;

	private List<String> accessConstraints;

	private List<String> otherConstraints;

	private String classification;

	public Constraint() {
	}

	/**
	 * @param useLimitations
	 * @param accessConstraints
	 * @param otherConstraints
	 * @param classification
	 */
	public Constraint(List<String> limitations, List<String> accessConstraints, List<String> otherConstraints,
			String classification) {
		super();
		this.limitations = limitations;
		this.accessConstraints = accessConstraints;
		this.otherConstraints = otherConstraints;
		this.classification = classification;
	}

	public List<String> getLimitations() {
		return limitations;
	}

	public List<String> getAccessConstraints() {
		return accessConstraints;
	}

	public List<String> getOtherConstraints() {
		return otherConstraints;
	}

	public String getClassification() {
		return classification;
	}

	public void setLimitations(List<String> limitations) {
		this.limitations = limitations;
	}

	public void setAccessConstraints(List<String> accessConstraints) {
		this.accessConstraints = accessConstraints;
	}

	public void setOtherConstraints(List<String> otherConstraints) {
		this.otherConstraints = otherConstraints;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

}
