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
package org.deegree.metadata.ebrim.model;

import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._date;
import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._double;
import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._geom;
import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._int;
import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._multiple;
import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._string;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.filter.sql.UnmappableException;
import org.deegree.metadata.ebrim.RegistryObjectType;
import org.deegree.metadata.ebrim.model.Slot.SLOTTYPE;
import org.jaxen.expr.NameStep;

import com.sun.org.apache.xml.internal.dtm.Axis;

/**
 * Defines the slots and types for all supported ebRim ExtrinsicObjects and their mapping to the relational model.
 * 
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class SlotMapper {

    public enum MainColumnName {
        internalId, data, id
    }

    public enum Table {
        idxtb_association, idxtb_classification, idxtb_extrinsicobject, idxtb_registrypackage, idxtb_classificationNode
    }

    public static final String SLOTURN = "urn:ogc:def:ebRIM-Slot:OGC-06-131:";

    private static final List<Slot> productSlots = new ArrayList<Slot>();

    private static final List<Slot> productInfoSlots = new ArrayList<Slot>();

    private static final List<Slot> browseInfoSlots = new ArrayList<Slot>();

    private static final List<Slot> maskInfoSlots = new ArrayList<Slot>();

    private static final List<Slot> dataLayerSlots = new ArrayList<Slot>();

    private static final List<Slot> acqSlots = new ArrayList<Slot>();

    private static final List<Slot> archinvingInfoSlots = new ArrayList<Slot>();

    private static final Map<String, Slot> slotNameToSlot = new HashMap<String, Slot>();

    static {
        addSlot( productSlots, "acquisitionType", "ep_acquisitiontype", _string );
        addSlot( productSlots, "acquisitionStation", "ep_acquisitionstation", _string );
        addSlot( productSlots, "acquisitionSubType", "ep_acquisitionsubtype", _string );
        addSlot( productSlots, "doi", "ep_doi", _string );
        addSlot( productSlots, "imageQualityDegradationQuotationMode", "ep_imgqualitydegquotmd", _string );
        addSlot( productSlots, "orbitDirection", "ep_orbitdirection", _string );
        addSlot( productSlots, "parentIdentifier", "ep_parentidentifier", _string );
        addSlot( productSlots, "productType", "ep_producttype", _string );
        addSlot( productSlots, "status", "ep_status", _string );
        addSlot( productSlots, "wrsLongitudeGrid", "ep_wrslongitudegrid", _string );
        addSlot( productSlots, "wrsLatitudeGrid", "ep_wrslatitudegrid", _string );
        addSlot( productSlots, "vendorSpecificValues", "ep_vendorspecval", _multiple );
        addSlot( productSlots, "vendorSpecificAttributes", "ep_vendorspecattr", _multiple );
        addSlot( productSlots, "lastOrbitNumber", "ep_lastorbitnumber", _int );
        addSlot( productSlots, "orbitNumber", "ep_orbitnumber", _int );
        addSlot( productSlots, "endPosition", "ep_endposition", _date );
        addSlot( productSlots, "beginPosition", "ep_beginposition", _date );
        addSlot( productSlots, "acquisitionDate", "ep_acquisitiondate", _date );
        addSlot( productSlots, "ascendingNodeDate", "ep_ascendingnodedate", _date );
        addSlot( productSlots, "startTimeFromAscendingNode", "ep_starttimeascnd", _double );
        addSlot( productSlots, "completionTimeFromAscendingNode", "ep_comptimeascnd", _double );
        addSlot( productSlots, "ascendingNodeLongitude", "ep_ascndlong", _double );
        addSlot( productSlots, "orbitDuration", "ep_orbitduration", _double );
        addSlot( productSlots, "incidenceAngle", "ep_incidenceangle", _double );
        addSlot( productSlots, "acrossTrackIncidenceAngle", "ep_acrosstrackincangle", _double );
        addSlot( productSlots, "alongTrackIncidenceAngle", "ep_alongtrackincangle", _double );
        addSlot( productSlots, "imageQualityDegradation", "ep_imgqualitydeg", _double );
        addSlot( productSlots, "pitch", "ep_pitch", _double );
        addSlot( productSlots, "roll", "ep_roll", _double );
        addSlot( productSlots, "yaw", "ep_yaw", _double );
        addSlot( productSlots, "multiExtentOf", "ep_multiExtentOf", _geom );
        addSlot( productSlots, "centerOf", "ep_centerOf", _geom );

        addSlot( acqSlots, "instrumentShortName", "ap_instShortName", _string );
        addSlot( acqSlots, "platformOrbitType", "ap_platformOrbitType", _string );
        addSlot( acqSlots, "platformSerialIdentifier", "ap_platformSerialId", _string );
        addSlot( acqSlots, "sensorOperationalMode", "ap_sensorOpMode", _string );
        addSlot( acqSlots, "sensorType", "ap_sensorType", _string );
        addSlot( acqSlots, "swathIdentifier", "ap_swathId", _string );
        addSlot( acqSlots, "sensorResolution", "ap_sensoreResolution", _double );

        addSlot( maskInfoSlots, "format", "mi_format", _string );

        addSlot( productInfoSlots, "size", "pi_size", _string );

        addSlot( archinvingInfoSlots, "archivingIdentifier", "ai_archivingIdentifier", _string );
        addSlot( archinvingInfoSlots, "archivingDate", "ai_archivingDate", _date );

        addSlot( dataLayerSlots, "highestLocation", "dl_highestLocation", _double );
        addSlot( dataLayerSlots, "lowestLocation", "dl_lowestLocation", _double );

        addSlot( browseInfoSlots, "subType", "bi_subType", _string );
    }

    private static void addSlot( List<Slot> slotList, String name, String column, SLOTTYPE type ) {
        Slot slot = new Slot( name, column, type );
        slotList.add( slot );
        slotNameToSlot.put( SLOTURN + name, slot );
    }

    public enum EOTYPE {

        PRODUCT( "urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOProduct", productSlots ),
        //
        ACQUPLATFORM( "urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOAcquisitionPlatform", acqSlots ),
        //
        MASKINFO( "urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOMaskInformation", maskInfoSlots ),
        //
        ARCHIVINGINFO( "urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOArchivingInformation", archinvingInfoSlots ),
        //
        PRODUCTINFO( "urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOProductInformation", productInfoSlots ),
        //
        DATALAYER( "urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EODataLayer", dataLayerSlots ),
        //
        BROWSEINFO( "urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOBrowseInformation", browseInfoSlots );

        private String type;

        private List<Slot> slots;

        EOTYPE( String type, List<Slot> slots ) {
            this.type = type;
            this.slots = slots;
        }

        public String getType() {
            return type;
        }

        public List<Slot> getSlots() {
            return slots;
        }

        public static EOTYPE valueOfType( String type ) {
            for ( EOTYPE et : EOTYPE.values() ) {
                if ( et.getType().equals( type ) ) {
                    return et;
                }
            }
            return null;
        }
    }

    public static List<String> getSlots( EOTYPE type, SLOTTYPE slotType ) {
        List<String> list = new ArrayList<String>();
        for ( Slot slot : type.getSlots() ) {
            if ( slot.getType() == slotType )
                list.add( slot.getName() );
        }
        return list;
    }

    public static Slot getSlot( String slotName ) {
        return slotNameToSlot.get( slotName );
    }

    public static Table getTable( RegistryObjectType type ) {
        switch ( type ) {
        case RegistryPackage: {
            return Table.idxtb_registrypackage;
        }
        case Association: {
            return Table.idxtb_association;
        }
        case ExtrinsicObject: {
            return Table.idxtb_extrinsicobject;
        }
        case Classification: {
            return Table.idxtb_classification;
        }
        }
        return null;
    }

    public static String getColumn( RegistryObjectType type, List<NameStep> steps )
                            throws UnmappableException {
        switch ( type ) {
        case RegistryPackage: {
            return getRegistryPackageColumn( steps );
        }
        case Association: {
            return getAssociationColumn( steps );
        }
        case ExtrinsicObject: {
            return getExtrinsicObjectColumn( steps );
        }
        case Classification: {
            return getClassificationColumn( steps );
        }
        }
        throw new UnmappableException( "No mapping for registry objects of type '" + type + "' possible." );
    }

    private static String getRegistryPackageColumn( List<NameStep> steps )
                            throws UnmappableException {
        if ( steps.size() == 1 ) {
            if ( steps.get( 0 ).getAxis() == Axis.ATTRIBUTE ) {
                if ( "id".equals( steps.get( 0 ).getLocalName() ) ) {
                    return "regpackid";
                }
            }
        }
        return null;
    }

    private static String getClassificationColumn( List<NameStep> steps )
                            throws UnmappableException {
        // TODO Auto-generated method stub
        return null;
    }

    private static String getExtrinsicObjectColumn( List<NameStep> steps )
                            throws UnmappableException {
        // TODO Auto-generated method stub
        return null;
    }

    private static String getAssociationColumn( List<NameStep> steps )
                            throws UnmappableException {
        // TODO Auto-generated method stub
        return null;
    }
}
