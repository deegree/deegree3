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
package org.deegree.metadata.ebrim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XPath;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
public class RegistryPackage extends RegistryObject {

	private Map<String, List<ExtrinsicObject>> extrinsicObjects = new HashMap<String, List<ExtrinsicObject>>();

	private List<Association> associations;

	private List<Classification> classifications;

	private List<ClassificationNode> classificationNodes;

	public RegistryPackage(XMLStreamReader xmlReader) {
		super(xmlReader);
	}

	public RegistryPackage(OMElement asOMElement) {
		super(asOMElement);
	}

	private XPath getEOPath(String type) {
		return new XPath("./rim:RegistryObjectList/rim:ExtrinsicObject[@objectType='" + type + "']", ns);
	}

	public List<ExtrinsicObject> getExtrinsicObjects(String type) {
		if (!extrinsicObjects.containsKey(type)) {
			List<ExtrinsicObject> eos = new ArrayList<ExtrinsicObject>();
			List<OMElement> eoElems = adapter.getElements(adapter.getRootElement(), getEOPath(type));
			for (OMElement eoElem : eoElems) {
				eos.add(new ExtrinsicObject(eoElem));
			}
			extrinsicObjects.put(type, eos);
		}

		return extrinsicObjects.get(type);
	}

	/**
	 * @return the associations
	 */
	public List<Association> getAssociations() {
		if (associations == null) {
			List<OMElement> associationElements = adapter.getElements(adapter.getRootElement(),
					new XPath("./rim:RegistryObjectList/rim:Association", ns));

			associations = new ArrayList<Association>();
			for (OMElement associationElem : associationElements) {
				associations.add(new Association(associationElem));
			}
		}
		return associations;
	}

	/**
	 * @return the classifications
	 */
	public List<Classification> getClassifications() {
		if (classifications == null) {
			List<OMElement> classificationElems = adapter.getElements(adapter.getRootElement(),
					new XPath("./rim:RegistryObjectList/rim:Classification", ns));
			classifications = new ArrayList<Classification>();
			for (OMElement classificationElem : classificationElems) {
				classifications.add(new Classification(classificationElem));
			}

		}
		return classifications;
	}

	/**
	 * @return
	 */
	public List<ClassificationNode> getClassificationNodes() {
		if (classificationNodes == null) {
			// ALL classifictionNodes are parsed -> they can be childs of
			// ClassificationNode or ClassificationScheme
			// (...)
			List<OMElement> classNodeElems = adapter.getElements(adapter.getRootElement(),
					new XPath("./rim:RegistryObjectList//rim:ClassificationNode", ns));
			classificationNodes = new ArrayList<ClassificationNode>();
			for (OMElement classNodeElem : classNodeElems) {
				classificationNodes.add(new ClassificationNode(classNodeElem));
			}
		}
		return classificationNodes;
	}

}
