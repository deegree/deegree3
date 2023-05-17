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
package org.deegree.feature.persistence.sql.config;

import static java.lang.Boolean.TRUE;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.feature.persistence.sql.blob.BlobCodec.Compression.NONE;
import static org.deegree.feature.persistence.sql.jaxb.NullEscalationType.AUTO;
import static org.deegree.feature.persistence.sql.jaxb.NullEscalationType.FALSE;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.sql.BBoxTableMapping;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.sqldialect.SortCriterion;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.jaxb.AbstractParticleJAXB;
import org.deegree.feature.persistence.sql.jaxb.ComplexParticleJAXB;
import org.deegree.feature.persistence.sql.jaxb.FIDMappingJAXB;
import org.deegree.feature.persistence.sql.jaxb.FIDMappingJAXB.ColumnJAXB;
import org.deegree.feature.persistence.sql.jaxb.FeatureParticleJAXB;
import org.deegree.feature.persistence.sql.jaxb.FeatureTypeMappingJAXB;
import org.deegree.feature.persistence.sql.jaxb.GeometryParticleJAXB;
import org.deegree.feature.persistence.sql.jaxb.NullEscalationType;
import org.deegree.feature.persistence.sql.jaxb.OrderByJAXB;
import org.deegree.feature.persistence.sql.jaxb.PrimitiveParticleJAXB;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB.BLOBMapping;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB.NamespaceHint;
import org.deegree.feature.persistence.sql.jaxb.StorageCRS;
import org.deegree.feature.persistence.sql.mapper.XPathSchemaWalker;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.ObjectPropertyType;
import org.deegree.filter.expression.ValueReference;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.deegree.sqldialect.filter.MappingExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates {@link MappedAppSchema} instances from JAXB {@link BLOBMapping} and JAXB
 * {@link FeatureTypeMapping} instances.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class MappedSchemaBuilderGML extends AbstractMappedSchemaBuilder {

	private static Logger LOG = LoggerFactory.getLogger(MappedSchemaBuilderGML.class);

	private static final String FEATURE_TYPE_TABLE = "feature_types";

	private static final String GML_OBJECTS_TABLE = "gml_objects";

	private final AppSchema gmlSchema;

	private final NamespaceBindings nsBindings = new NamespaceBindings();

	private BlobMapping blobMapping;

	private BBoxTableMapping bboxMapping;

	private final Map<QName, org.deegree.feature.persistence.sql.FeatureTypeMapping> ftNameToMapping = new HashMap<QName, org.deegree.feature.persistence.sql.FeatureTypeMapping>();

	private final GeometryStorageParams geometryParams;

	private final XPathSchemaWalker schemaWalker;

	private final boolean deleteCascadingByDB;

	public MappedSchemaBuilderGML(String configURL, List<String> gmlSchemas, StorageCRS storageCRS,
			List<NamespaceHint> nsHints, BLOBMapping blobConf, List<FeatureTypeMappingJAXB> ftMappingConfs,
			boolean deleteCascadingByDB) throws FeatureStoreException {

		gmlSchema = buildGMLSchema(configURL, gmlSchemas);

		CRSRef crs = CRSManager.getCRSRef(storageCRS.getValue());
		CoordinateDimension dim = crs.getDimension() == 3 ? DIM_3 : DIM_2;
		geometryParams = new GeometryStorageParams(crs, storageCRS.getSrid(), dim);

		// add namespace bindings
		addNamespaceBindings(configURL, gmlSchema, nsHints);

		schemaWalker = new XPathSchemaWalker(gmlSchema, nsBindings);
		if (blobConf != null) {
			Pair<BlobMapping, BBoxTableMapping> pair = buildBlobMapping(blobConf,
					gmlSchema.getGMLSchema().getVersion());
			blobMapping = pair.first;
			bboxMapping = pair.second;
		}
		if (ftMappingConfs != null) {
			for (FeatureTypeMappingJAXB ftMappingConf : ftMappingConfs) {
				org.deegree.feature.persistence.sql.FeatureTypeMapping ftMapping = buildFtMapping(ftMappingConf);
				ftNameToMapping.put(ftMapping.getFeatureType(), ftMapping);
			}
		}
		this.deleteCascadingByDB = deleteCascadingByDB;
	}

	private void addNamespaceBindings(String componentLocation, AppSchema gmlSchema, List<NamespaceHint> userHints) {

		// explicit namespace hints
		for (NamespaceHint userHint : userHints) {
			String nsUri = userHint.getNamespaceURI();
			String prefix = userHint.getPrefix();
			String oldPrefix = nsBindings.getPrefix(nsUri);
			if (oldPrefix != null && !oldPrefix.equals(prefix)) {
				LOG.warn("Multiple prefices for namespace '" + nsUri + "': " + prefix + " / " + oldPrefix);
			}
			else {
				nsBindings.addNamespace(prefix, nsUri);
			}
		}

		// Namespace bindings from config file
		InputStream is = null;
		try {
			LOG.debug("Scanning config file '" + componentLocation + "' for namespace bindings");
			is = new URL(componentLocation).openStream();
			XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
			while (xmlStream.next() != END_DOCUMENT) {
				if (xmlStream.isStartElement()) {
					for (int i = 0; i < xmlStream.getNamespaceCount(); i++) {
						String prefix = xmlStream.getNamespacePrefix(i);
						if (prefix != null && !prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
							String nsUri = xmlStream.getNamespaceURI(i);
							String oldPrefix = nsBindings.getPrefix(nsUri);
							if (oldPrefix != null && !oldPrefix.equals(prefix)) {
								LOG.debug("Multiple prefices for namespace '" + nsUri + "': " + prefix + " / "
										+ oldPrefix);
							}
							else {
								nsBindings.addNamespace(prefix, nsUri);
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			LOG.error("Error determining namespaces from config file '" + componentLocation + "': " + e.getMessage());
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		// app namespaces from GML schema
		Map<String, String> schemaNSBindings = gmlSchema.getNamespaceBindings();
		for (String prefix : schemaNSBindings.keySet()) {
			String nsUri = schemaNSBindings.get(prefix);
			String oldPrefix = nsBindings.getPrefix(nsUri);
			if (oldPrefix != null && !oldPrefix.equals(prefix)) {
				LOG.warn("Multiple prefices for namespace '" + nsUri + "': " + prefix + " / " + oldPrefix);
			}
			else {
				nsBindings.addNamespace(prefix, nsUri);
			}
		}
		nsBindings.addNamespace("xsi", XSINS);

		// general namespace bindings used in XML schema
		schemaNSBindings = gmlSchema.getGMLSchema().getNamespacePrefixes();
		for (String prefix : schemaNSBindings.keySet()) {
			String nsUri = schemaNSBindings.get(prefix);
			String oldPrefix = nsBindings.getPrefix(nsUri);
			if (oldPrefix != null && !oldPrefix.equals(prefix)) {
				LOG.warn("Multiple prefices for namespace '" + nsUri + "': " + prefix + " / " + oldPrefix);
			}
			else {
				nsBindings.addNamespace(prefix, nsUri);
			}
		}
	}

	/**
	 * Returns the {@link MappedAppSchema} derived from GML application schemas /
	 * configuration.
	 * @return mapped application schema, never <code>null</code>
	 */
	@Override
	public MappedAppSchema getMappedSchema() {
		FeatureType[] fts = gmlSchema.getFeatureTypes();
		org.deegree.feature.persistence.sql.FeatureTypeMapping[] ftMappings = ftNameToMapping.values()
			.toArray(new org.deegree.feature.persistence.sql.FeatureTypeMapping[ftNameToMapping.size()]);
		Map<FeatureType, FeatureType> ftToSuperFt = gmlSchema.getFtToSuperFt();
		Map<String, String> prefixToNs = new HashMap<String, String>();
		Iterator<String> prefixIter = nsBindings.getPrefixes();
		while (prefixIter.hasNext()) {
			String prefix = prefixIter.next();
			prefixToNs.put(prefix, nsBindings.getNamespaceURI(prefix));
		}
		GMLSchemaInfoSet xsModel = gmlSchema.getGMLSchema();
		return new MappedAppSchema(fts, ftToSuperFt, prefixToNs, xsModel, ftMappings, bboxMapping, blobMapping,
				geometryParams, deleteCascadingByDB, null, gmlSchema.getGmlObjectTypes(),
				gmlSchema.getGeometryToSuperType());
	}

	private AppSchema buildGMLSchema(String configURL, List<String> gmlSchemas) throws FeatureStoreException {

		LOG.debug("Building application schema from GML schema files.");
		AppSchema appSchema = null;
		try {
			XMLAdapter resolver = new XMLAdapter();
			resolver.setSystemId(configURL);

			String[] schemaURLs = new String[gmlSchemas.size()];
			int i = 0;
			for (String gmlSchema : gmlSchemas) {
				schemaURLs[i++] = resolver.resolve(gmlSchema.trim()).toString();
			}

			GMLAppSchemaReader decoder = null;
			if (schemaURLs.length == 1 && schemaURLs[0].startsWith("file:")) {
				File file = new File(new URL(schemaURLs[0]).toURI());
				decoder = new GMLAppSchemaReader(null, null, file);
			}
			else {
				decoder = new GMLAppSchemaReader(null, null, schemaURLs);
			}
			appSchema = decoder.extractAppSchema();
		}
		catch (Throwable t) {
			t.printStackTrace();
			String msg = "Error building GML application schema: " + t.getMessage();
			throw new FeatureStoreException(msg);
		}
		LOG.debug("GML version: " + appSchema.getGMLSchema().getVersion());
		return appSchema;
	}

	private Pair<BlobMapping, BBoxTableMapping> buildBlobMapping(BLOBMapping blobMappingConf, GMLVersion gmlVersion) {
		String ftTable = blobMappingConf.getFeatureTypeTable() == null ? FEATURE_TYPE_TABLE
				: blobMappingConf.getFeatureTypeTable();
		BBoxTableMapping bboxMapping = new BBoxTableMapping(ftTable, geometryParams.getCrs());
		String blobTable = blobMappingConf.getBlobTable() == null ? GML_OBJECTS_TABLE : blobMappingConf.getBlobTable();
		BlobMapping blobMapping = new BlobMapping(blobTable, geometryParams.getCrs(), new BlobCodec(gmlVersion, NONE));
		return new Pair<BlobMapping, BBoxTableMapping>(blobMapping, bboxMapping);
	}

	private FeatureTypeMapping buildFtMapping(FeatureTypeMappingJAXB ftMappingConf) throws FeatureStoreException {

		QName ftName = ftMappingConf.getName();
		TableName ftTable = new TableName(ftMappingConf.getTable());
		FIDMapping fidMapping = buildFIDMapping(ftTable, ftName, ftMappingConf.getFIDMapping());
		List<Mapping> particleMappings = new ArrayList<Mapping>();
		XSElementDeclaration elDecl = gmlSchema.getGMLSchema().getElementDecl(ftName);
		for (JAXBElement<? extends AbstractParticleJAXB> particle : ftMappingConf.getAbstractParticle()) {
			particleMappings
				.add(buildMapping(ftTable, new Pair<XSElementDeclaration, Boolean>(elDecl, TRUE), particle.getValue()));
		}
		List<SortCriterion> sortCriteria = createSortCriteria(ftMappingConf, ftTable);
		return new FeatureTypeMapping(ftName, ftTable, fidMapping, particleMappings, sortCriteria);
	}

	private FIDMapping buildFIDMapping(TableName table, QName ftName, FIDMappingJAXB config)
			throws FeatureStoreException {

		String prefix = config != null ? config.getPrefix() : null;
		if (prefix == null) {
			prefix = ftName.getPrefix().toUpperCase() + "_" + ftName.getLocalPart().toUpperCase() + "_";
		}

		List<Pair<SQLIdentifier, BaseType>> columns = new ArrayList<Pair<SQLIdentifier, BaseType>>();
		if (config != null && config.getColumn() != null) {
			for (ColumnJAXB configColumn : config.getColumn()) {
				String column = configColumn.getName();
				BaseType pt = null;
				if (configColumn.getType() != null) {
					pt = getPrimitiveType(configColumn.getType());
				}
				columns.add(new Pair<SQLIdentifier, BaseType>(new SQLIdentifier(column), pt));
			}
		}

		IDGenerator generator = buildGenerator(config == null ? null : config.getAbstractIDGenerator());
		if (!(generator instanceof AutoIDGenerator)) {
			if (columns.isEmpty()) {
				throw new FeatureStoreException("No FIDMapping column for table '" + table
						+ "' specified. This is only possible for AutoIDGenerator.");
			}
		}
		return new FIDMapping(prefix, "_", columns, generator);
	}

	private Mapping buildMapping(TableName currentTable, Pair<XSElementDeclaration, Boolean> elDecl,
			AbstractParticleJAXB value) {
		LOG.debug("Building mapping for path '{}' on element '{}'", value.getPath(), elDecl);
		if (value instanceof PrimitiveParticleJAXB) {
			return buildMapping(currentTable, elDecl, (PrimitiveParticleJAXB) value);
		}
		if (value instanceof GeometryParticleJAXB) {
			return buildMapping(currentTable, elDecl, (GeometryParticleJAXB) value);
		}
		if (value instanceof FeatureParticleJAXB) {
			return buildMapping(currentTable, elDecl, (FeatureParticleJAXB) value);
		}
		if (value instanceof ComplexParticleJAXB) {
			return buildMapping(currentTable, elDecl, (ComplexParticleJAXB) value);
		}
		throw new RuntimeException(
				"Internal error. Unhandled particle mapping JAXB bean '" + value.getClass().getName() + "'.");
	}

	private Mapping buildMapping(TableName currentTable, Pair<XSElementDeclaration, Boolean> elDecl,
			PrimitiveParticleJAXB config) {

		ValueReference path = new ValueReference(config.getPath(), nsBindings);
		Pair<PrimitiveType, Boolean> pt = null;
		try {
			pt = schemaWalker.getTargetType(elDecl, path);
		}
		catch (RuntimeException e) {
			throw new RuntimeException("Error in mapping of table '" + currentTable + "': " + e);
		}

		if (config.getType() != null) {
			PrimitiveType forcedType = new PrimitiveType(getPrimitiveType(config.getType()));
			LOG.debug(
					"Overriding schema-derived primitive type '" + pt.getFirst() + "'. Forcing '" + forcedType + "'.");
			pt.first = forcedType;
		}

		MappingExpression me = parseMappingExpression(config.getMapping());
		List<TableJoin> joinedTable = buildJoinTable(currentTable, config.getJoin());
		LOG.debug("Targeted primitive type: " + pt);
		boolean escalateVoid = determineParticleVoidability(pt.second, config.getNullEscalation());
		return new PrimitiveMapping(path, escalateVoid, me, pt.first, joinedTable, config.getCustomConverter());
	}

	private GeometryMapping buildMapping(TableName currentTable, Pair<XSElementDeclaration, Boolean> elDecl,
			GeometryParticleJAXB config) {
		ValueReference path = new ValueReference(config.getPath(), nsBindings);
		MappingExpression me = parseMappingExpression(config.getMapping());
		elDecl = schemaWalker.getTargetElement(elDecl, path);
		QName ptName = new QName(elDecl.first.getNamespace(), elDecl.getFirst().getName());
		ObjectPropertyType pt = gmlSchema.getGMLSchema().getGMLPropertyDecl(elDecl.first, ptName, 1, 1, null);
		GeometryType type = GeometryType.GEOMETRY;
		if (pt instanceof GeometryPropertyType) {
			type = ((GeometryPropertyType) pt).getGeometryType();
		}
		boolean escalateVoid = determineParticleVoidability(elDecl.second, config.getNullEscalation());
		List<TableJoin> joinedTable = buildJoinTable(currentTable, config.getJoin());
		return new GeometryMapping(path, escalateVoid, me, type, geometryParams, joinedTable,
				config.getCustomConverter());
	}

	private FeatureMapping buildMapping(TableName currentTable, Pair<XSElementDeclaration, Boolean> elDecl,
			FeatureParticleJAXB config) {
		ValueReference path = new ValueReference(config.getPath(), nsBindings);
		MappingExpression hrefMe = null;
		if (config.getHref() != null) {
			hrefMe = parseMappingExpression(config.getHref().getMapping());
		}
		elDecl = schemaWalker.getTargetElement(elDecl, path);
		QName ptName = new QName(elDecl.first.getNamespace(), elDecl.first.getName());
		// TODO rework this
		FeaturePropertyType pt = (FeaturePropertyType) gmlSchema.getGMLSchema()
			.getGMLPropertyDecl(elDecl.first, ptName, 0, 1, null);
		boolean escalateVoid = determineParticleVoidability(elDecl.second, config.getNullEscalation());
		List<TableJoin> joinedTable = buildJoinTable(currentTable, config.getJoin());
		return new FeatureMapping(path, escalateVoid, hrefMe, pt.getFTName(), joinedTable, config.getCustomConverter());
	}

	private CompoundMapping buildMapping(TableName currentTable, Pair<XSElementDeclaration, Boolean> elDecl,
			ComplexParticleJAXB config) {
		ValueReference path = new ValueReference(config.getPath(), nsBindings);
		elDecl = schemaWalker.getTargetElement(elDecl, path);
		boolean escalateVoid = determineParticleVoidability(elDecl.second, config.getNullEscalation());
		List<TableJoin> joinedTable = buildJoinTable(currentTable, config.getJoin());
		if (joinedTable != null) {
			currentTable = joinedTable.get(joinedTable.size() - 1).getToTable();
		}

		List<JAXBElement<? extends AbstractParticleJAXB>> children = config.getAbstractParticle();
		List<Mapping> particles = new ArrayList<Mapping>(children.size());
		for (JAXBElement<? extends AbstractParticleJAXB> child : children) {
			Mapping particle = buildMapping(currentTable, elDecl, child.getValue());
			if (particle != null) {
				particles.add(particle);
			}
		}

		return new CompoundMapping(path, escalateVoid, particles, joinedTable, elDecl.first,
				config.getCustomConverter());
	}

	private boolean determineParticleVoidability(boolean fromSchema, NullEscalationType config) {
		if (config == null || config == AUTO) {
			return fromSchema;
		}
		if (config == FALSE) {
			return true;
		}
		return false;
	}

}
