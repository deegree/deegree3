//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General private License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General private License for more
 details.
 You should have received a copy of the GNU Lesser General private License
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
package org.deegree.metadata.ebrim.model;

import java.util.List;

import org.apache.axiom.om.OMElement;

/**
 * Main entry point for an ebRim EO Record
 * 
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class RegistryPackage extends RegistryObject {

    private final List<ExtrinsicObject> extrinsicObjects;

    private final List<Association> associations;

    private final List<Classification> classifications;

    private final List<ClassificationNode> classificationNodes;

    public RegistryPackage( RegistryObject ro, List<ExtrinsicObject> extObjects, List<Association> associations,
                            List<Classification> classifications, List<ClassificationNode> classificationNodes ) {
        super( ro );
        this.extrinsicObjects = extObjects;
        this.associations = associations;
        this.classifications = classifications;
        this.classificationNodes = classificationNodes;
    }

    public RegistryPackage( String id, String name, String description, String externalId,
                            List<ExtrinsicObject> extObjects, List<Association> associations,
                            List<Classification> classifications, List<ClassificationNode> classificationNodes,
                            OMElement element ) {
        super( id, null, null, null, name, description, null, externalId, null, element );
        this.extrinsicObjects = extObjects;
        this.associations = associations;
        this.classifications = classifications;
        this.classificationNodes = classificationNodes;
    }

    /**
     * @return the extrinsicObjects
     */
    public List<ExtrinsicObject> getExtrinsicObjects() {
        return extrinsicObjects;
    }

    /**
     * @return the associations
     */
    public List<Association> getAssociations() {
        return associations;
    }

    /**
     * @return the classifications
     */
    public List<Classification> getClassifications() {
        return classifications;
    }

    /**
     * @return
     */
    public List<ClassificationNode> getClassificationNodes() {
        return classificationNodes;
    }

}
