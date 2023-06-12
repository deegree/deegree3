/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.metadata.iso;

import static org.deegree.commons.xml.CommonNamespaces.OWS_NS;
import static org.deegree.protocol.csw.CSWConstants.APISO_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.DC_NS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.CRSUtils;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.GeometryFactory;
import org.deegree.metadata.iso.parsing.QueryableProperties;
import org.deegree.metadata.iso.types.BoundingBox;
import org.deegree.metadata.iso.types.CRS;
import org.deegree.metadata.iso.types.Constraint;
import org.deegree.metadata.iso.types.Format;
import org.deegree.metadata.iso.types.Keyword;
import org.deegree.metadata.iso.types.OperatesOnData;

/**
 * {@link XPathEvaluator} implementation to evaluate {@link ISORecord}s
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class ISORecordEvaluator implements XPathEvaluator<ISORecord> {

	private static final List<QName> QP_TITLE = new ArrayList<QName>();

	private static final List<QName> QP_ABSTRACT = new ArrayList<QName>();

	private static final List<QName> QP_BBOX = new ArrayList<QName>();

	private static final List<QName> QP_TYPE = new ArrayList<QName>();

	private static final List<QName> QP_FORMAT = new ArrayList<QName>();

	private static final List<QName> QP_SUBJECT = new ArrayList<QName>();

	private static final List<QName> QP_ANYTEXT = new ArrayList<QName>();

	private static final List<QName> QP_IDENTIFIER = new ArrayList<QName>();

	private static final List<QName> QP_MODIFIED = new ArrayList<QName>();

	private static final List<QName> QP_CRS = new ArrayList<QName>();

	private static final List<QName> QP_LANGUAGE = new ArrayList<QName>();

	private static final QName QP_REVISION = new QName(APISO_NS, "RevisionDate");

	private static final QName QP_CREATION = new QName(APISO_NS, "CreationDate");

	private static final QName QP_ALTERNATETITLE = new QName(APISO_NS, "AlternateTitle");

	private static final QName QP_PUBLICATIONDATE = new QName(APISO_NS, "PublicationDate");

	private static final QName QP_ORGANISATIONNAME = new QName(APISO_NS, "OrganisationName");

	private static final QName QP_HASSECURITYCONSTRAINTS = new QName(APISO_NS, "HasSecurityConstraints");

	private static final QName QP_RESOURCEIDENTIFIER = new QName(APISO_NS, "ResourceIdentifier");

	private static final QName QP_PARENTIDENTIFIER = new QName(APISO_NS, "ParentIdentifier");

	private static final QName QP_KEYWORDTYPE = new QName(APISO_NS, "KeywordType");

	private static final QName QP_TOPICCATEGORY = new QName(APISO_NS, "TopicCategory");

	private static final QName QP_RESOURCELANGUAGE = new QName(APISO_NS, "ResourceLanguage");

	private static final QName QP_GEOGRAPHICDESCRIPTIONCODE = new QName(APISO_NS, "GeographicDescriptionCode");

	private static final QName QP_DENOMINATOR = new QName(APISO_NS, "Denominator");

	private static final QName QP_DISTANCEVALUE = new QName(APISO_NS, "DistanceValue");

	private static final QName QP_DISTANCEUOM = new QName(APISO_NS, "DistanceUOM");

	private static final QName QP_TEMPEXTENT_BEGIN = new QName(APISO_NS, "TempExtent_begin");

	private static final QName QP_TEMPEXTENT_END = new QName(APISO_NS, "TempExtent_end");

	private static final QName QP_SERVICETYPE = new QName(APISO_NS, "ServiceType");

	private static final QName QP_SERVICETYPEVERSION = new QName(APISO_NS, "ServiceTypeVersion");

	private static final QName QP_OPERATION = new QName(APISO_NS, "Operation");

	private static final QName QP_OPERATESON = new QName(APISO_NS, "OperatesOn");

	private static final QName QP_OPERATESONIDENTIFIER = new QName(APISO_NS, "OperatesOnIdentifier");

	private static final QName QP_OPERATESONNAME = new QName(APISO_NS, "OperatesOnName");

	private static final QName QP_COUPLINGTYPE = new QName(APISO_NS, "CouplingType");

	private static final QName QP_DEGREE = new QName(APISO_NS, "Degree");

	private static final QName QP_ACCESSCONSTRAINTS = new QName(APISO_NS, "AccessConstraints");

	private static final QName QP_OTHERCONSTRAINTS = new QName(APISO_NS, "OtherConstraints");

	private static final QName QP_CLASSIFICATION = new QName(APISO_NS, "Classification");

	private static final QName QP_CONDITIONAPPLYINGTOACCESSANDUSE = new QName(APISO_NS,
			"ConditionApplyingToAccessAndUse");

	private static final QName QP_LINEAGE = new QName(APISO_NS, "Lineage");

	private static final QName QP_SPECIFICATIONTITLE = new QName(APISO_NS, "SpecificationTitle");

	private static final QName QP_SPECIFICATIONDATETYPE = new QName(APISO_NS, "SpecificationDateType");

	private static final QName QP_SPECIFICATIONDATE = new QName(APISO_NS, "SpecificationDate");

	private static final QName QP_RESPONSIBLEPARTYROLE = new QName(APISO_NS, "ResponsiblePartyRole");

	static {
		QP_SUBJECT.add(new QName(APISO_NS, "Subject"));
		QP_SUBJECT.add(new QName(APISO_NS, "subject"));
		QP_SUBJECT.add(new QName(DC_NS, "Subject"));
		QP_SUBJECT.add(new QName("Subject"));
		QP_SUBJECT.add(new QName(CSW_202_NS, "Subject"));

		QP_TITLE.add(new QName(APISO_NS, "Title"));
		QP_TITLE.add(new QName(APISO_NS, "title"));
		QP_TITLE.add(new QName(DC_NS, "Title"));
		QP_TITLE.add(new QName("Title"));
		QP_TITLE.add(new QName(CSW_202_NS, "Title"));

		QP_ABSTRACT.add(new QName(APISO_NS, "Abstract"));
		QP_ABSTRACT.add(new QName(APISO_NS, "abstract"));
		QP_ABSTRACT.add(new QName(DC_NS, "Abstract"));
		QP_ABSTRACT.add(new QName("Abstract"));
		QP_ABSTRACT.add(new QName(CSW_202_NS, "Abstract"));

		QP_BBOX.add(new QName(APISO_NS, "BoundingBox"));
		QP_BBOX.add(new QName(APISO_NS, "boundingBox"));
		QP_BBOX.add(new QName(OWS_NS, "BoundingBox"));
		QP_BBOX.add(new QName(OWS_NS, "boundingBox"));
		QP_BBOX.add(new QName(DC_NS, "BoundingBox"));
		QP_BBOX.add(new QName("BoundingBox"));
		QP_BBOX.add(new QName(CSW_202_NS, "BoundingBox"));
		QP_BBOX.add(new QName(DC_NS, "coverage"));

		QP_TYPE.add(new QName(APISO_NS, "Type"));
		QP_TYPE.add(new QName(APISO_NS, "type"));
		QP_TYPE.add(new QName(DC_NS, "Type"));
		QP_TYPE.add(new QName("Type"));
		QP_TYPE.add(new QName(CSW_202_NS, "Type"));

		QP_FORMAT.add(new QName(APISO_NS, "Format"));
		QP_FORMAT.add(new QName(APISO_NS, "format"));
		QP_FORMAT.add(new QName(DC_NS, "Format"));
		QP_FORMAT.add(new QName("Format"));
		QP_FORMAT.add(new QName(CSW_202_NS, "Format"));

		QP_ANYTEXT.add(new QName(APISO_NS, "AnyText"));
		QP_ANYTEXT.add(new QName(APISO_NS, "anyText"));
		QP_ANYTEXT.add(new QName(DC_NS, "AnyText"));
		QP_ANYTEXT.add(new QName("AnyText"));
		QP_ANYTEXT.add(new QName(CSW_202_NS, "AnyText"));

		QP_IDENTIFIER.add(new QName(APISO_NS, "Identifier"));
		QP_IDENTIFIER.add(new QName(APISO_NS, "identifier"));
		QP_IDENTIFIER.add(new QName(DC_NS, "Identifier"));
		QP_IDENTIFIER.add(new QName("Identifier"));
		QP_IDENTIFIER.add(new QName(CSW_202_NS, "Identifier"));

		QP_MODIFIED.add(new QName(APISO_NS, "Modified"));
		QP_MODIFIED.add(new QName(APISO_NS, "modified"));
		QP_MODIFIED.add(new QName(DC_NS, "Modified"));
		QP_MODIFIED.add(new QName("Modified"));
		QP_MODIFIED.add(new QName(CSW_202_NS, "Modified"));

		QP_CRS.add(new QName(APISO_NS, "CRS"));
		QP_CRS.add(new QName(DC_NS, "CRS"));
		QP_CRS.add(new QName("CRS"));

		QP_LANGUAGE.add(new QName(APISO_NS, "Language"));
		QP_LANGUAGE.add(new QName(APISO_NS, "language"));
	}

	@Override
	public TypedObjectNode[] eval(ISORecord context, ValueReference valueRef) throws FilterEvaluationException {
		QueryableProperties qp = context.getParsedElement().getQueryableProperties();
		if (isQueryable(QP_TITLE, valueRef)) {
			return getResult(context.getTitle());
		}
		else if (isQueryable(QP_ABSTRACT, valueRef)) {
			return getResult(context.getAbstract());
		}
		else if (isQueryable(QP_BBOX, valueRef)) {
			return getResultBBox(qp.getBoundingBox());
		}
		else if (isQueryable(QP_TYPE, valueRef)) {
			return getResult(context.getType());
		}
		else if (isQueryable(QP_FORMAT, valueRef)) {
			return getResultFormat(qp.getFormat());
		}
		else if (isQueryable(QP_SUBJECT, valueRef)) {
			return getResultKeywords(qp.getKeywords());
		}
		else if (isQueryable(QP_ANYTEXT, valueRef)) {
			return getResult(qp.getAnyText());
		}
		else if (isQueryable(QP_IDENTIFIER, valueRef)) {
			return getResult(qp.getIdentifier());
		}
		else if (isQueryable(QP_MODIFIED, valueRef)) {
			return getResult(qp.getModified());
		}
		else if (isQueryable(QP_CRS, valueRef)) {
			return getResultCrs(qp.getCrs());
		}
		else if (isQueryable(QP_LANGUAGE, valueRef)) {
			return getResult(qp.getLanguage());
		}
		else if (isQueryable(QP_REVISION, valueRef)) {
			return getResult(qp.getRevisionDate());
		}
		else if (isQueryable(QP_CREATION, valueRef)) {
			return getResult(qp.getCreationDate());
		}
		else if (isQueryable(QP_ALTERNATETITLE, valueRef)) {
			return getResult(qp.getAlternateTitle());
		}
		else if (isQueryable(QP_PUBLICATIONDATE, valueRef)) {
			return getResult(qp.getPublicationDate());
		}
		else if (isQueryable(QP_ORGANISATIONNAME, valueRef)) {
			return getResult(qp.getOrganisationName());
		}
		else if (isQueryable(QP_HASSECURITYCONSTRAINTS, valueRef)) {
			return getResult(qp.isHasSecurityConstraints());
		}
		else if (isQueryable(QP_RESOURCEIDENTIFIER, valueRef)) {
			return getResult(qp.getResourceIdentifier());
		}
		else if (isQueryable(QP_PARENTIDENTIFIER, valueRef)) {
			return getResult(qp.getParentIdentifier());
		}
		else if (isQueryable(QP_KEYWORDTYPE, valueRef)) {
			return getResultKeywordsType(qp.getKeywords());
		}
		else if (isQueryable(QP_TOPICCATEGORY, valueRef)) {
			return getResult(qp.getTopicCategory());
		}
		else if (isQueryable(QP_RESOURCELANGUAGE, valueRef)) {
			return getResult(qp.getResourceLanguage());
		}
		else if (isQueryable(QP_GEOGRAPHICDESCRIPTIONCODE, valueRef)) {
			return getResult(qp.getGeographicDescriptionCode_service());
		}
		else if (isQueryable(QP_DENOMINATOR, valueRef)) {
			return getResult(qp.getDenominator());
		}
		else if (isQueryable(QP_DISTANCEVALUE, valueRef)) {
			return getResult(qp.getDistanceValue());
		}
		else if (isQueryable(QP_DISTANCEUOM, valueRef)) {
			return getResult(qp.getDistanceUOM());
		}
		else if (isQueryable(QP_TEMPEXTENT_BEGIN, valueRef)) {
			return getResult(qp.getTemporalExtentBegin());
		}
		else if (isQueryable(QP_TEMPEXTENT_END, valueRef)) {
			return getResult(qp.getTemporalExtentEnd());
		}
		else if (isQueryable(QP_SERVICETYPE, valueRef)) {
			return getResult(qp.getServiceType());
		}
		else if (isQueryable(QP_SERVICETYPEVERSION, valueRef)) {
			return getResult(qp.getServiceTypeVersion());
		}
		else if (isQueryable(QP_OPERATION, valueRef)) {
			return getResult(qp.getOperation());
		}
		else if (isQueryable(QP_OPERATESON, valueRef)) {
			return getResultOperatesOn(qp.getOperatesOnData());
		}
		else if (isQueryable(QP_OPERATESONIDENTIFIER, valueRef)) {
			return getResultOperatesOnIdentifier(qp.getOperatesOnData());
		}
		else if (isQueryable(QP_OPERATESONNAME, valueRef)) {
			return getResultOperatesOnName(qp.getOperatesOnData());
		}
		else if (isQueryable(QP_COUPLINGTYPE, valueRef)) {
			return getResult(qp.getCouplingType());
		}
		else if (isQueryable(QP_DEGREE, valueRef)) {
			return getResult(qp.isDegree());
		}
		else if (isQueryable(QP_ACCESSCONSTRAINTS, valueRef)) {
			return getResultAccessConstraints(qp.getConstraints());
		}
		else if (isQueryable(QP_OTHERCONSTRAINTS, valueRef)) {
			return getResultOtherConstraints(qp.getConstraints());
		}
		else if (isQueryable(QP_CLASSIFICATION, valueRef)) {
			return getResultClassification(qp.getConstraints());
		}
		else if (isQueryable(QP_CONDITIONAPPLYINGTOACCESSANDUSE, valueRef)) {
			return getResultCondition(qp.getConstraints());
		}
		else if (isQueryable(QP_LINEAGE, valueRef)) {
			return getResult(qp.getLineages());
		}
		else if (isQueryable(QP_SPECIFICATIONTITLE, valueRef)) {
			return getResult(qp.getSpecificationTitle());
		}
		else if (isQueryable(QP_SPECIFICATIONDATE, valueRef)) {
			return getResult(qp.getSpecificationDate());
		}
		else if (isQueryable(QP_SPECIFICATIONDATETYPE, valueRef)) {
			return getResult(qp.getSpecificationDateType());
		}
		else if (isQueryable(QP_RESPONSIBLEPARTYROLE, valueRef)) {
			return getResult(qp.getRespPartyRole());
		}
		throw new FilterEvaluationException("Could not map " + valueRef.toString());
	}

	private TypedObjectNode[] getResultCondition(List<Constraint> constraints) {
		if (constraints == null)
			return new TypedObjectNode[0];
		List<TypedObjectNode> result = new ArrayList<TypedObjectNode>();
		for (Constraint constraint : constraints) {
			for (String limitation : constraint.getLimitations()) {
				result.add(new PrimitiveValue(limitation));
			}
		}
		return result.toArray(new TypedObjectNode[result.size()]);
	}

	private TypedObjectNode[] getResultClassification(List<Constraint> constraints) {
		if (constraints == null)
			return new TypedObjectNode[0];
		List<TypedObjectNode> result = new ArrayList<TypedObjectNode>();
		for (Constraint constraint : constraints) {
			if (constraint.getClassification() != null) {
				result.add(new PrimitiveValue(constraint.getClassification()));
			}
		}
		return result.toArray(new TypedObjectNode[result.size()]);
	}

	private TypedObjectNode[] getResultOtherConstraints(List<Constraint> constraints) {
		if (constraints == null)
			return new TypedObjectNode[0];
		List<TypedObjectNode> result = new ArrayList<TypedObjectNode>();
		for (Constraint constraint : constraints) {
			for (String other : constraint.getOtherConstraints()) {
				result.add(new PrimitiveValue(other));
			}

		}
		return result.toArray(new TypedObjectNode[result.size()]);
	}

	private TypedObjectNode[] getResultAccessConstraints(List<Constraint> constraints) {
		if (constraints == null)
			return new TypedObjectNode[0];
		List<TypedObjectNode> result = new ArrayList<TypedObjectNode>();
		for (Constraint constraint : constraints) {
			for (String access : constraint.getAccessConstraints()) {
				result.add(new PrimitiveValue(access));
			}

		}
		return result.toArray(new TypedObjectNode[result.size()]);
	}

	private TypedObjectNode[] getResultKeywordsType(List<Keyword> keywords) {
		if (keywords == null)
			return new TypedObjectNode[0];
		TypedObjectNode[] result = new TypedObjectNode[keywords.size()];
		int i = 0;
		for (Keyword keyword : keywords) {
			if (keyword.getKeywordType() != null) {
				result[i++] = new PrimitiveValue(keyword.getKeywordType());
			}
		}
		return result;
	}

	private TypedObjectNode[] getResultKeywords(List<Keyword> keywords) {
		if (keywords == null)
			return new TypedObjectNode[0];
		List<TypedObjectNode> result = new ArrayList<TypedObjectNode>();
		for (Keyword keywordList : keywords) {
			for (String keyword : keywordList.getKeywords()) {
				result.add(new PrimitiveValue(keyword));
			}
		}
		return result.toArray(new TypedObjectNode[result.size()]);
	}

	private TypedObjectNode[] getResultOperatesOnName(List<OperatesOnData> operatesOnDatas) {
		if (operatesOnDatas == null)
			return new TypedObjectNode[0];
		TypedObjectNode[] result = new TypedObjectNode[operatesOnDatas.size()];
		int i = 0;
		for (OperatesOnData operatesOnData : operatesOnDatas) {
			if (operatesOnData.getOperatesOnName() != null) {
				result[i++] = new PrimitiveValue(operatesOnData.getOperatesOnName());
			}
		}
		return result;
	}

	private TypedObjectNode[] getResultOperatesOnIdentifier(List<OperatesOnData> operatesOnDatas) {
		if (operatesOnDatas == null)
			return new TypedObjectNode[0];
		TypedObjectNode[] result = new TypedObjectNode[operatesOnDatas.size()];
		int i = 0;
		for (OperatesOnData operatesOnData : operatesOnDatas) {
			if (operatesOnData.getOperatesOnIdentifier() != null) {
				result[i++] = new PrimitiveValue(operatesOnData.getOperatesOnIdentifier());
			}
		}
		return result;
	}

	private TypedObjectNode[] getResultOperatesOn(List<OperatesOnData> operatesOnDatas) {
		if (operatesOnDatas == null)
			return new TypedObjectNode[0];
		TypedObjectNode[] result = new TypedObjectNode[operatesOnDatas.size()];
		int i = 0;
		for (OperatesOnData operatesOnData : operatesOnDatas) {
			if (operatesOnData.getOperatesOnId() != null) {
				result[i++] = new PrimitiveValue(operatesOnData.getOperatesOnId());
			}
		}
		return result;
	}

	private TypedObjectNode[] getResultFormat(List<Format> formats) {
		if (formats == null)
			return new TypedObjectNode[0];
		TypedObjectNode[] result = new TypedObjectNode[formats.size()];
		int i = 0;
		for (Format format : formats) {
			if (format.getName() != null) {
				result[i++] = new PrimitiveValue(format.getName());
			}
		}
		return result;
	}

	private TypedObjectNode[] getResultBBox(List<BoundingBox> bbox) {
		if (bbox == null || bbox.isEmpty())
			return new TypedObjectNode[0];
		double west = bbox.get(0).getWestBoundLongitude();
		double east = bbox.get(0).getEastBoundLongitude();
		double south = bbox.get(0).getSouthBoundLatitude();
		double north = bbox.get(0).getNorthBoundLatitude();
		for (BoundingBox b : bbox) {
			west = Math.min(west, b.getWestBoundLongitude());
			east = Math.max(east, b.getEastBoundLongitude());
			south = Math.min(south, b.getSouthBoundLatitude());
			north = Math.max(north, b.getNorthBoundLatitude());
		}
		GeometryFactory gf = new GeometryFactory();
		return new TypedObjectNode[] { gf.createEnvelope(west, south, east, north, CRSUtils.EPSG_4326) };
	}

	private TypedObjectNode[] getResultCrs(List<CRS> crs) {
		if (crs == null)
			return new TypedObjectNode[0];
		TypedObjectNode[] result = new TypedObjectNode[crs.size()];
		int i = 0;
		for (CRS c : crs) {
			if (c.getCrsId() != null) {
				result[i++] = new PrimitiveValue(c.getCrsId());
			}
		}
		return result;
	}

	private TypedObjectNode[] getResult(List<String> values) {
		if (values == null)
			return new TypedObjectNode[0];
		TypedObjectNode[] result = new TypedObjectNode[values.size()];
		int i = 0;
		for (String value : values) {
			result[i++] = new PrimitiveValue(value);
		}
		return result;
	}

	private TypedObjectNode[] getResult(String[] values) {
		if (values == null)
			return new TypedObjectNode[0];
		TypedObjectNode[] result = new TypedObjectNode[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = new PrimitiveValue(values[i]);
		}
		return result;
	}

	private TypedObjectNode[] getResult(float value) {
		return getSimpleResult(value);
	}

	private TypedObjectNode[] getResult(int value) {
		return getSimpleResult(value);
	}

	private TypedObjectNode[] getResult(boolean value) {
		return getSimpleResult(value);
	}

	private TypedObjectNode[] getResult(Date value) {
		return getSimpleResult(value);
	}

	private TypedObjectNode[] getResult(String value) {
		return getSimpleResult(value);
	}

	private TypedObjectNode[] getSimpleResult(Object value) {
		if (value == null) {
			return new TypedObjectNode[0];
		}
		return new TypedObjectNode[] { new PrimitiveValue(value) };
	}

	private boolean isQueryable(List<QName> queryable, ValueReference valueRef) {
		return queryable.contains(valueRef.getAsQName());
	}

	private boolean isQueryable(QName queryable, ValueReference valueRef) {
		return queryable.equals(valueRef.getAsQName());
	}

	@Override
	public String getId(ISORecord context) {
		return context.getIdentifier();
	}

}
