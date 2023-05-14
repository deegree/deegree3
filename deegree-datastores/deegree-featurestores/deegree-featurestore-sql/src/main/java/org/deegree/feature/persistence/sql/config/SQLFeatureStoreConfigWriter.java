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
package org.deegree.feature.persistence.sql.config;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.id.SequenceIDGenerator;
import org.deegree.feature.persistence.sql.id.UUIDGenerator;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.GMLVersion;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates configuration documents for the {@link PostGISFeatureStore} from
 * {@link MappedAppSchema} instances.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class SQLFeatureStoreConfigWriter {

	private static Logger LOG = LoggerFactory.getLogger(SQLFeatureStoreConfigWriter.class);

	private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/sql";

	private static final String SCHEMA_LOCATION = "http://www.deegree.org/datasource/feature/sql https://schemas.deegree.org/core/3.5/datasource/feature/sql/sql.xsd";

	private final MappedAppSchema schema;

	private final List<QName> propertiesWithPrimitiveHref;

	/**
	 * Creates a new {@link SQLFeatureStoreConfigWriter} instance.
	 * @param schema the mapped application schema to export, must not be
	 * <code>null</code>
	 */
	public SQLFeatureStoreConfigWriter(MappedAppSchema schema) {
		this(schema, null);
	}

	/**
	 * Creates a new {@link SQLFeatureStoreConfigWriter} instance.
	 * @param schema the mapped application schema to export, must not be
	 * <code>null</code>
	 * @param propertiesWithPrimitiveHref list of properties which are written with
	 * primitive instead of feature mapping, is applied to all properties of type
	 * {@link FeatureMapping}, may be <code>null</code>
	 */
	public SQLFeatureStoreConfigWriter(MappedAppSchema schema, List<QName> propertiesWithPrimitiveHref) {
		this.schema = schema;
		this.propertiesWithPrimitiveHref = propertiesWithPrimitiveHref;
	}

	/**
	 * Exports the configuration document.
	 * @param writer
	 * @param connId
	 * @param schemaURLs
	 * @throws XMLStreamException
	 */
	public void writeConfig(XMLStreamWriter writer, String connId, List<String> schemaURLs) throws XMLStreamException {

		writer.writeStartElement("SQLFeatureStore");
		writer.writeNamespace(DEFAULT_NS_PREFIX, CONFIG_NS);
		writer.writeNamespace("xsi", XSINS);
		writer.writeAttribute(XSINS, "schemaLocation", SCHEMA_LOCATION);
		int i = 1;
		for (String ns : schema.getGMLSchema().getAppNamespaces()) {
			String prefix = schema.getGMLSchema().getNamespacePrefixes().get(ns);
			if (prefix != null && !prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
				writer.writeNamespace(prefix, ns);
			}
			else {
				writer.writeNamespace("app" + (i++), ns);
			}
		}

		writer.writeNamespace("xlink", XLNNS);
		GMLVersion version = schema.getGMLSchema().getVersion();
		writer.writeNamespace("gml", version.getNamespace());

		// writer.writeStartElement( CONFIG_NS, "StorageCRS" );
		// writer.writeCharacters( storageCrs );
		// writer.writeEndElement();

		writer.writeStartElement(CONFIG_NS, "JDBCConnId");
		writer.writeCharacters(connId);
		writer.writeEndElement();

		writer.writeStartElement(CONFIG_NS, "StorageCRS");
		writer.writeAttribute("srid", schema.getGeometryParams().getSrid());
		// TODO
		writer.writeAttribute("dim", "2D");
		writer.writeCharacters(schema.getGeometryParams().getCrs().getAlias());
		writer.writeEndElement();

		for (String schemaUrl : schemaURLs) {
			writer.writeStartElement(CONFIG_NS, "GMLSchema");
			writer.writeCharacters(schemaUrl);
			writer.writeEndElement();
		}

		if (schema.getBlobMapping() != null) {
			writeBlobMapping(writer, schema.getBlobMapping());
		}

		List<FeatureType> fts = schema.getFeatureTypes(null, false, false);
		SortedSet<String> ftNames = new TreeSet<String>();
		for (FeatureType ft : fts) {
			ftNames.add(ft.getName().toString());
		}

		for (String qName : ftNames) {
			QName ftName = QName.valueOf(qName);
			FeatureType ft = schema.getFeatureType(ftName);
			if (schema.getFtMapping(ft.getName()) != null) {
				writeFeatureTypeMapping(writer, ft);
			}
		}

		writer.writeEndElement();
	}

	private void writeBlobMapping(XMLStreamWriter writer, BlobMapping blobMapping) throws XMLStreamException {
		writer.writeStartElement(CONFIG_NS, "BLOBMapping");
		writer.writeEndElement();
	}

	private void writeFeatureTypeMapping(XMLStreamWriter writer, FeatureType ft) throws XMLStreamException {

		LOG.debug("Feature type '" + ft.getName() + "'");
		FeatureTypeMapping ftMapping = schema.getFtMapping(ft.getName());

		writer.writeStartElement(CONFIG_NS, "FeatureTypeMapping");
		writer.writeAttribute("name", getName(ft.getName()));
		writer.writeAttribute("table", ftMapping.getFtTable().toString());

		FIDMapping fidMapping = ftMapping.getFidMapping();
		writer.writeStartElement(CONFIG_NS, "FIDMapping");
		if (fidMapping.getPrefix() != null && !fidMapping.getPrefix().isEmpty()) {
			writer.writeAttribute("prefix", fidMapping.getPrefix());
		}
		for (Pair<SQLIdentifier, BaseType> column : fidMapping.getColumns()) {
			writer.writeStartElement(CONFIG_NS, "Column");
			writer.writeAttribute("name", column.getFirst().toString());
			writer.writeAttribute("type", column.getSecond().getXSTypeName());
			writer.writeEndElement();
		}
		IDGenerator generator = fidMapping.getIdGenerator();
		if (generator instanceof AutoIDGenerator) {
			writer.writeEmptyElement(CONFIG_NS, "AutoIdGenerator");
		}
		else if (generator instanceof SequenceIDGenerator) {
			writer.writeEmptyElement(CONFIG_NS, "SequenceIDGenerator");
		}
		else if (generator instanceof UUIDGenerator) {
			writer.writeEmptyElement(CONFIG_NS, "UUIDGenerator");
		}
		writer.writeEndElement();

		for (Mapping particle : ftMapping.getMappings()) {
			writeMapping(writer, particle, false);
		}

		writer.writeEndElement();
	}

	private void writeMapping(XMLStreamWriter writer, Mapping particle, boolean isHrefPrimitive)
			throws XMLStreamException {

		if (particle instanceof PrimitiveMapping) {
			PrimitiveMapping pm = (PrimitiveMapping) particle;
			writer.writeStartElement(CONFIG_NS, "Primitive");
			writer.writeAttribute("path", particle.getPath().getAsText());
			MappingExpression mapping = pm.getMapping();
			if (mapping instanceof DBField) {
				writer.writeAttribute("mapping", ((DBField) mapping).getColumn());
				switch (pm.getType().getBaseType()) {
					case DATE_TIME:
						writer.writeAttribute("type", "dateTime");
						break;
					case DATE:
						writer.writeAttribute("type", "date");
						break;
					case TIME:
						writer.writeAttribute("type", "time");
						break;
				}
			}
			else {
				writer.writeAttribute("mapping", mapping.toString());
			}
			if (particle.getJoinedTable() != null) {
				writeJoinedTable(writer, particle.getJoinedTable().get(0));
			}
			writer.writeEndElement();
		}
		else if (particle instanceof GeometryMapping) {
			GeometryMapping gm = (GeometryMapping) particle;
			writer.writeStartElement(CONFIG_NS, "Geometry");
			writer.writeAttribute("path", particle.getPath().getAsText());
			writer.writeAttribute("mapping", gm.getMapping().toString());
			if (particle.getJoinedTable() != null) {
				writeJoinedTable(writer, particle.getJoinedTable().get(0));
			}
			writer.writeEndElement();
		}
		else if (particle instanceof FeatureMapping) {
			FeatureMapping gm = (FeatureMapping) particle;
			if (gm.getHrefMapping() != null && isHrefPrimitive) {
				writer.writeStartElement(CONFIG_NS, "Primitive");
				writer.writeAttribute("path", "@xlink:href");
				writer.writeAttribute("mapping", gm.getHrefMapping().toString());
				writer.writeEndElement();
			}
			else {
				writer.writeStartElement(CONFIG_NS, "Feature");
				writer.writeAttribute("path", particle.getPath().getAsText());
				if (particle.getJoinedTable() != null && !particle.getJoinedTable().isEmpty()) {
					writeJoinedTable(writer, particle.getJoinedTable().get(0));
				}
				if (gm.getHrefMapping() != null) {
					writer.writeStartElement(CONFIG_NS, "Href");
					writer.writeAttribute("mapping", gm.getHrefMapping().toString());
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
		}
		else if (particle instanceof CompoundMapping) {
			writer.writeStartElement(CONFIG_NS, "Complex");
			writer.writeAttribute("path", particle.getPath().getAsText());
			if (particle.getJoinedTable() != null) {
				writeJoinedTable(writer, particle.getJoinedTable().get(0));
			}
			CompoundMapping compound = (CompoundMapping) particle;
			for (Mapping childMapping : compound.getParticles()) {
				boolean isChildHrefPrimitive = isHrefPrimitive(compound);
				writeMapping(writer, childMapping, isChildHrefPrimitive);
			}
			writer.writeEndElement();
		}
		else {
			LOG.warn("Unhandled mapping particle " + particle.getClass().getName());
		}
	}

	private void writeJoinedTable(XMLStreamWriter writer, TableJoin jc) throws XMLStreamException {
		writer.writeStartElement(CONFIG_NS, "Join");
		if (jc.getToTable() != null) {
			writer.writeAttribute("table", jc.getToTable().toString());
		}
		writer.writeAttribute("fromColumns", StringUtils.concat(jc.getFromColumns(), ","));
		writer.writeAttribute("toColumns", StringUtils.concat(jc.getToColumns(), ","));
		if (jc.getOrderColumns() != null && !jc.getOrderColumns().isEmpty()) {
			writer.writeAttribute("orderColumns", StringUtils.concat(jc.getOrderColumns(), ","));
		}
		if (jc.isNumberedOrder()) {
			writer.writeAttribute("numbered", "true");
		}
		writer.writeEndElement();
	}

	private String getName(QName name) {
		if (name.getNamespaceURI() != null && !name.getNamespaceURI().equals("")) {
			String prefix = schema.getGMLSchema().getNamespacePrefixes().get(name.getNamespaceURI());
			return prefix + ":" + name.getLocalPart();
		}
		return name.getLocalPart();
	}

	private boolean isHrefPrimitive(CompoundMapping compound) {
		return propertiesWithPrimitiveHref != null
				&& propertiesWithPrimitiveHref.contains(compound.getPath().getAsQName());
	}

}