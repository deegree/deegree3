//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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

import static org.deegree.metadata.ebrim.model.SlotMapper.EOTYPE.ACQUPLATFORM;
import static org.deegree.metadata.ebrim.model.SlotMapper.EOTYPE.ARCHIVINGINFO;
import static org.deegree.metadata.ebrim.model.SlotMapper.EOTYPE.BROWSEINFO;
import static org.deegree.metadata.ebrim.model.SlotMapper.EOTYPE.DATALAYER;
import static org.deegree.metadata.ebrim.model.SlotMapper.EOTYPE.MASKINFO;
import static org.deegree.metadata.ebrim.model.SlotMapper.EOTYPE.PRODUCT;
import static org.deegree.metadata.ebrim.model.SlotMapper.EOTYPE.PRODUCTINFO;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.ebrim.model.Association;
import org.deegree.metadata.ebrim.model.Classification;
import org.deegree.metadata.ebrim.model.ClassificationNode;
import org.deegree.metadata.ebrim.model.ExtrinsicObject;
import org.deegree.metadata.ebrim.model.RegistryObject;
import org.deegree.metadata.ebrim.model.RegistryPackage;
import org.deegree.metadata.ebrim.model.SlotMapper.EOTYPE;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class RegistryPackageRecord extends RegistryObjectRecord {

    public RegistryPackageRecord( XMLStreamReader xmlReader ) {
        super( xmlReader );
    }

    public RegistryPackageRecord( OMElement asOMElement ) {
        super( asOMElement );
    }

    @Override
    public RegistryPackage getParsedRecord() {
        RegistryObject ro = super.getParsedRecord();

        OMElement record = adapter.getRootElement();

        List<ExtrinsicObject> extObjects = new ArrayList<ExtrinsicObject>();
        OMElement eoElement = adapter.getElement( record, getEOPath( PRODUCT ) );
        if ( eoElement != null ) {
            extObjects.add( parseExtrinsicObject( eoElement ) );
        }
        List<OMElement> browseInfos = adapter.getElements( record, getEOPath( BROWSEINFO ) );
        for ( OMElement bi : browseInfos ) {
            extObjects.add( parseExtrinsicObject( bi ) );
        }
        List<OMElement> aquPlatForms = adapter.getElements( record, getEOPath( ACQUPLATFORM ) );
        for ( OMElement apf : aquPlatForms ) {
            extObjects.add( parseExtrinsicObject( apf ) );
        }
        List<OMElement> maskInfos = adapter.getElements( record, getEOPath( MASKINFO ) );
        for ( OMElement mi : maskInfos ) {
            extObjects.add( parseExtrinsicObject( mi ) );
        }
        List<OMElement> archivingInfos = adapter.getElements( record, getEOPath( ARCHIVINGINFO ) );
        for ( OMElement ai : archivingInfos ) {
            extObjects.add( parseExtrinsicObject( ai ) );
        }

        List<OMElement> dataLayers = adapter.getElements( record, getEOPath( DATALAYER ) );
        for ( OMElement di : dataLayers ) {
            extObjects.add( parseExtrinsicObject( di ) );
        }

        List<OMElement> productInfos = adapter.getElements( record, getEOPath( PRODUCTINFO ) );
        for ( OMElement pi : productInfos ) {
            extObjects.add( parseExtrinsicObject( pi ) );
        }

        List<Association> associations = parseAssociations( record );
        List<Classification> classifications = parseClassifications( record );
        List<ClassificationNode> classificationNodes = parseClassficationNodes( record );

        return new RegistryPackage( ro, extObjects, associations, classifications, classificationNodes );
    }

    /**
     * @param eoElement
     * @param product
     * @return
     */
    private ExtrinsicObject parseExtrinsicObject( OMElement eoElement ) {
        ExtrinsicObjectRecord eoRecord = new ExtrinsicObjectRecord( eoElement );
        return eoRecord.getParsedRecord();
    }

    private List<Association> parseAssociations( OMElement record ) {
        List<OMElement> associationElements = adapter.getElements( record,
                                                                   new XPath(
                                                                              "./rim:RegistryObjectList/rim:Association",
                                                                              ns ) );

        List<Association> associations = new ArrayList<Association>();
        for ( OMElement associationElem : associationElements ) {
            AssociationRecord associationRecord = new AssociationRecord( associationElem );
            associations.add( associationRecord.getParsedRecord() );
        }
        return associations;
    }

    private List<Classification> parseClassifications( OMElement record ) {
        List<OMElement> classificationElems = adapter.getElements( record,
                                                                   new XPath(
                                                                              "./rim:RegistryObjectList/rim:Classification",
                                                                              ns ) );
        List<Classification> classifications = new ArrayList<Classification>();
        for ( OMElement classificationElem : classificationElems ) {
            ClassificationRecord classRecord = new ClassificationRecord( classificationElem );
            classifications.add( classRecord.getParsedRecord() );
        }
        return classifications;
    }

    private List<ClassificationNode> parseClassficationNodes( OMElement record ) {
        // ALL classifictionNodes are parsed -> they can be childs of ClassificationNode or ClassificationScheme (...)
        List<OMElement> classNodeElems = adapter.getElements( record,
                                                              new XPath(
                                                                         "./rim:RegistryObjectList//rim:ClassificationNode",
                                                                         ns ) );
        List<ClassificationNode> classNodes = new ArrayList<ClassificationNode>();
        for ( OMElement classNodeElem : classNodeElems ) {
            ClassificationNodeRecord classNodeRecord = new ClassificationNodeRecord( classNodeElem );
            classNodes.add( classNodeRecord.getParsedRecord() );
        }
        return classNodes;
    }

    private XPath getEOPath( EOTYPE type ) {
        return new XPath( "./rim:RegistryObjectList/rim:ExtrinsicObject[@objectType='" + type.getType() + "']", ns );
    }

}
