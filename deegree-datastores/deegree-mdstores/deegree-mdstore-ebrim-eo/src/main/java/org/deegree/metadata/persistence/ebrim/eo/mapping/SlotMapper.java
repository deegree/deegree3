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
package org.deegree.metadata.persistence.ebrim.eo.mapping;

import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping.SlotType._date;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping.SlotType._double;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping.SlotType._geom;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping.SlotType._int;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping.SlotType._multiple;
import static org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping.SlotType._string;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.metadata.ebrim.ExtrinsicObject;
import org.deegree.metadata.ebrim.RIMType;
import org.deegree.metadata.persistence.ebrim.eo.mapping.SlotMapping.SlotType;

/**
 * Defines the slots and types for all supported {@link ExtrinsicObject}s and their
 * mapping to the relational model.
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class SlotMapper {

	public enum MainColumnName {

		internalId, data, id

	}

	public enum Table {

		idxtb_association, idxtb_classification, idxtb_extrinsicobject, idxtb_registrypackage, idxtb_classificationNode

	}

	public static final String SLOTURN = "urn:ogc:def:ebRIM-Slot:OGC-06-131:";

	private static final List<SlotMapping> productSlots = new ArrayList<SlotMapping>();

	private static final List<SlotMapping> productInfoSlots = new ArrayList<SlotMapping>();

	private static final List<SlotMapping> browseInfoSlots = new ArrayList<SlotMapping>();

	private static final List<SlotMapping> maskInfoSlots = new ArrayList<SlotMapping>();

	private static final List<SlotMapping> dataLayerSlots = new ArrayList<SlotMapping>();

	private static final List<SlotMapping> acqSlots = new ArrayList<SlotMapping>();

	private static final List<SlotMapping> archivingInfoSlots = new ArrayList<SlotMapping>();

	private static final Map<String, SlotMapping> slotNameToSlot = new HashMap<String, SlotMapping>();

	static {
		addSlot(acqSlots, "instrumentShortName", "ap_instShortName", _string);
		addSlot(acqSlots, "platformOrbitType", "ap_platformOrbitType", _string);
		addSlot(acqSlots, "platformSerialIdentifier", "ap_platformSerialId", _string);
		addSlot(acqSlots, "sensorOperationalMode", "ap_sensorOpMode", _string);
		addSlot(acqSlots, "sensorResolution", "ap_sensorResolution", _double);
		addSlot(acqSlots, "sensorType", "ap_sensorType", _string);
		addSlot(acqSlots, "swathIdentifier", "ap_swathId", _string);
		addSlot(archivingInfoSlots, "archivingDate", "ai_archivingDate", _date);
		addSlot(archivingInfoSlots, "archivingIdentifier", "ai_archivingIdentifier", _string);
		addSlot(browseInfoSlots, "subType", "bi_subType", _string);
		addSlot(dataLayerSlots, "highestLocation", "dl_highestLocation", _double);
		addSlot(dataLayerSlots, "lowestLocation", "dl_lowestLocation", _double);
		addSlot(maskInfoSlots, "format", "mi_format", _string);
		addSlot(productInfoSlots, "size", "pi_size", _string);
		addSlot(productSlots, "acquisitionDate", "ep_acquisitionDate", _date);
		addSlot(productSlots, "acquisitionStation", "ep_acquisitionStation", _string);
		addSlot(productSlots, "acquisitionSubType", "ep_acquisitionSubType", _string);
		addSlot(productSlots, "acquisitionType", "ep_acquisitionType", _string);
		addSlot(productSlots, "acrossTrackIncidenceAngle", "ep_acrossTrackIncAngle", _double);
		addSlot(productSlots, "alongTrackIncidenceAngle", "ep_alongTrackIncAngle", _double);
		addSlot(productSlots, "ascendingNodeDate", "ep_ascendingNodeDate", _date);
		addSlot(productSlots, "ascendingNodeLongitude", "ep_ascNdLong", _double);
		addSlot(productSlots, "beginPosition", "ep_beginPosition", _date);
		addSlot(productSlots, "centerOf", "ep_centerOf", _geom);
		addSlot(productSlots, "cloudCoverPercentage", "ep_cloudCoverPerc", _double);
		addSlot(productSlots, "completionTimeFromAscendingNode", "ep_compTimeAscNd", _double);
		addSlot(productSlots, "doi", "ep_doi", _string);
		addSlot(productSlots, "endPosition", "ep_endPosition", _date);
		addSlot(productSlots, "illuminationAzimuthAngle", "ep_illumAzimuthAngle", _double);
		addSlot(productSlots, "illuminationElevationAngle", "ep_illumElevationAngle", _double);
		addSlot(productSlots, "imageQualityDegradation", "ep_imgQualityDeg", _double);
		addSlot(productSlots, "imageQualityDegradationQuotationMode", "ep_imgQualityDegQuotMd", _string);
		addSlot(productSlots, "incidenceAngle", "ep_incidenceAngle", _double);
		addSlot(productSlots, "lastOrbitNumber", "ep_lastOrbitNumber", _int);
		addSlot(productSlots, "multiExtentOf", "ep_multiExtentOf", _geom);
		addSlot(productSlots, "orbitDirection", "ep_orbitDirection", _string);
		addSlot(productSlots, "orbitDuration", "ep_orbitDuration", _double);
		addSlot(productSlots, "orbitNumber", "ep_orbitNumber", _int);
		addSlot(productSlots, "parentIdentifier", "ep_parentIdentifier", _string);
		addSlot(productSlots, "pitch", "ep_pitch", _double);
		addSlot(productSlots, "productType", "ep_productType", _string);
		addSlot(productSlots, "roll", "ep_roll", _double);
		addSlot(productSlots, "snowCoverPercentage", "ep_snowCoverPerc", _double);
		addSlot(productSlots, "startTimeFromAscendingNode", "ep_startTimeAscNd", _double);
		addSlot(productSlots, "status", "ep_status", _string);
		addSlot(productSlots, "vendorSpecificAttributes", "ep_vendorSpecAttr", _multiple);
		addSlot(productSlots, "vendorSpecificValues", "ep_vendorSpecVal", _multiple);
		addSlot(productSlots, "wrsLatitudeGrid", "ep_wrsLatitudeGrid", _string);
		addSlot(productSlots, "wrsLongitudeGrid", "ep_wrsLongitudeGrid", _string);
		addSlot(productSlots, "yaw", "ep_yaw", _double);
	}

	private static void addSlot(List<SlotMapping> slotList, String name, String column, SlotType type) {
		SlotMapping slot = new SlotMapping(name, column, type);
		slotList.add(slot);
		slotNameToSlot.put(SLOTURN + name, slot);
	}

	public enum EOTYPE {

		PRODUCT("urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOProduct", productSlots),
		//
		ACQUPLATFORM("urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOAcquisitionPlatform", acqSlots),
		//
		MASKINFO("urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOMaskInformation", maskInfoSlots),
		//
		ARCHIVINGINFO("urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOArchivingInformation", archivingInfoSlots),
		//
		PRODUCTINFO("urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOProductInformation", productInfoSlots),
		//
		DATALAYER("urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EODataLayer", dataLayerSlots),
		//
		BROWSEINFO("urn:x-ogc:specification:csw-ebrim:ObjectType:EO:EOBrowseInformation", browseInfoSlots);

		private final String type;

		private final List<SlotMapping> slots;

		EOTYPE(String type, List<SlotMapping> slots) {
			this.type = type;
			this.slots = slots;
		}

		public String getType() {
			return type;
		}

		public List<SlotMapping> getSlots() {
			return slots;
		}

		public static EOTYPE valueOfType(String type) {
			for (EOTYPE et : EOTYPE.values()) {
				if (et.getType().equals(type)) {
					return et;
				}
			}
			return null;
		}

	}

	public static List<String> getSlots(EOTYPE type, SlotType slotType) {
		List<String> list = new ArrayList<String>();
		for (SlotMapping slot : type.getSlots()) {
			if (slot.getType() == slotType)
				list.add(slot.getName());
		}
		return list;
	}

	public static SlotMapping getSlot(String slotName) {
		return slotNameToSlot.get(slotName);
	}

	public static Table getTable(RIMType type) {
		switch (type) {
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
			case ClassificationNode: {
				return Table.idxtb_classificationNode;
			}
		}
		return null;
	}

}