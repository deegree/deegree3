package org.deegree.gml.schema;

import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_EMPTY;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_MIXED;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_SIMPLE;
import static org.apache.xerces.xs.XSConstants.ELEMENT_DECLARATION;
import static org.apache.xerces.xs.XSConstants.MODEL_GROUP;
import static org.apache.xerces.xs.XSConstants.WILDCARD;
import static org.apache.xerces.xs.XSModelGroup.COMPOSITOR_ALL;
import static org.apache.xerces.xs.XSModelGroup.COMPOSITOR_CHOICE;
import static org.apache.xerces.xs.XSTypeDefinition.SIMPLE_TYPE;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.tom.gml.GMLObjectCategory;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.GenericGMLObjectType;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.feature.types.property.ArrayPropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GmlObjectTypeFactory {

	private static final Logger LOG = LoggerFactory.getLogger(GmlObjectTypeFactory.class);

	private final GMLSchemaInfoSet gmlSchema;

	private final Map<String, String> nsToPrefix;

	private int prefixIndex = 0;

	private final Map<QName, PropertyType> propNameToGlobalDecl = new HashMap<QName, PropertyType>();

	GmlObjectTypeFactory(final GMLSchemaInfoSet gmlSchema, final Map<String, String> nsToPrefix) {
		this.gmlSchema = gmlSchema;
		this.nsToPrefix = nsToPrefix;
	}

	GMLObjectType build(final XSElementDeclaration elDecl) {
		final QName elName = createQName(elDecl.getNamespace(), elDecl.getName());
		final GMLObjectCategory category = gmlSchema.getObjectCategory(elName);
		LOG.debug("Building object type declaration: '" + elName + "'");
		if (elDecl.getTypeDefinition().getType() == SIMPLE_TYPE) {
			final String msg = "Schema type of element '" + elName
					+ "' is simple, but object elements must have a complex type.";
			throw new IllegalArgumentException(msg);
		}
		final List<PropertyType> pts = new ArrayList<PropertyType>();
		final XSComplexTypeDefinition typeDef = (XSComplexTypeDefinition) elDecl.getTypeDefinition();
		switch (typeDef.getContentType()) {
			case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
				final XSParticle particle = typeDef.getParticle();
				final int minOccurs = particle.getMinOccurs();
				final int maxOccurs = particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs();
				final XSTerm term = particle.getTerm();
				switch (term.getType()) {
					case MODEL_GROUP: {
						addPropertyTypes(pts, (XSModelGroup) term, minOccurs, maxOccurs);
						break;
					}
					case ELEMENT_DECLARATION: {
						pts.add(buildPropertyType((XSElementDeclaration) term, minOccurs, maxOccurs));
						break;
					}
					case WILDCARD: {
						final String msg = "Object element '" + elName + "' uses wildcard in type model.";
						throw new IllegalArgumentException(msg);
					}
					default: {
						final String msg = "Internal error. Unhandled term type: " + term.getName();
						throw new RuntimeException(msg);
					}
				}
				break;
			}
			case CONTENTTYPE_EMPTY: {
				LOG.debug("Empty GML object type declaration.");
				break;
			}
			case CONTENTTYPE_MIXED: {
				final String msg = "GML object element '" + elName + "' uses mixed content in type model.";
				throw new IllegalArgumentException(msg);
			}
			case CONTENTTYPE_SIMPLE: {
				final String msg = "GML object element '" + elName + "' uses simple content in type model.";
				throw new IllegalArgumentException(msg);
			}
			default: {
				final String msg = "Internal error. Unhandled ContentType: " + typeDef.getContentType();
				throw new RuntimeException(msg);
			}
		}
		return new GenericGMLObjectType(category, elName, pts, elDecl.getAbstract());
	}

	private void addPropertyTypes(List<PropertyType> pts, XSModelGroup modelGroup, int parentMinOccurs,
			int parentMaxOccurs) {
		switch (modelGroup.getCompositor()) {
			case COMPOSITOR_ALL: {
				LOG.debug("Encountered 'All' compositor in object type model -- treating as sequence.");
				break;
			}
			case COMPOSITOR_CHOICE: {
				LOG.debug("Encountered 'Choice' compositor in object type model -- treating as sequence.");
				break;
			}
		}
		XSObjectList sequence = modelGroup.getParticles();
		for (int i = 0; i < sequence.getLength(); i++) {
			XSParticle particle = (XSParticle) sequence.item(i);
			int minOccurs = getCombinedOccurs(parentMinOccurs, particle.getMinOccurs());
			int maxOccurs = getCombinedOccurs(parentMaxOccurs,
					particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs());

			switch (particle.getTerm().getType()) {
				case ELEMENT_DECLARATION: {
					XSElementDeclaration elementDecl = (XSElementDeclaration) particle.getTerm();
					PropertyType pt = buildPropertyType(elementDecl, minOccurs, maxOccurs);
					if (pt != null) {
						pts.add(pt);
					}
					break;
				}
				case MODEL_GROUP: {
					addPropertyTypes(pts, (XSModelGroup) particle.getTerm(), minOccurs, maxOccurs);
					break;
				}
				case WILDCARD: {
					String msg = "Broken GML application schema: Encountered wildcard in feature type definition.";
					throw new IllegalArgumentException(msg);
				}
				default: {
					String msg = "Internal error. Unhandled term type: " + particle.getTerm().getName();
					throw new RuntimeException(msg);
				}
			}
		}
	}

	private PropertyType buildPropertyType(XSElementDeclaration elementDecl, int minOccurs, int maxOccurs) {
		PropertyType pt = null;
		final QName ptName = createQName(elementDecl.getNamespace(), elementDecl.getName());
		LOG.trace("*** Found property declaration: '" + elementDecl.getName() + "'.");
		// parse substitutable property declarations (e.g. genericProperty in CityGML)
		List<PropertyType> ptSubstitutions = new ArrayList<PropertyType>();
		XSObjectList list = gmlSchema.getXSModel().getSubstitutionGroup(elementDecl);
		if (list != null) {
			for (int i = 0; i < list.getLength(); i++) {
				XSElementDeclaration substitution = (XSElementDeclaration) list.item(i);
				QName declName = new QName(substitution.getNamespace(), substitution.getName());
				PropertyType globalDecl = propNameToGlobalDecl.get(declName);
				if (globalDecl == null) {
					globalDecl = buildPropertyType(substitution, minOccurs, maxOccurs);
					propNameToGlobalDecl.put(declName, globalDecl);
				}
				ptSubstitutions.add(globalDecl);
			}
		}
		XSTypeDefinition typeDef = elementDecl.getTypeDefinition();
		switch (typeDef.getTypeCategory()) {
			case XSTypeDefinition.SIMPLE_TYPE: {
				pt = new SimplePropertyType(ptName, minOccurs, maxOccurs, getPrimitiveType((XSSimpleType) typeDef),
						elementDecl, ptSubstitutions, (XSSimpleTypeDefinition) typeDef);
				((SimplePropertyType) pt).setCodeList(getCodeListId(elementDecl));
				break;
			}
			case XSTypeDefinition.COMPLEX_TYPE: {
				pt = buildPropertyType(elementDecl, (XSComplexTypeDefinition) typeDef, minOccurs, maxOccurs,
						ptSubstitutions);
				break;
			}
		}
		return pt;
	}

	private PropertyType buildPropertyType(XSElementDeclaration elementDecl, XSComplexTypeDefinition typeDef,
			int minOccurs, int maxOccurs, List<PropertyType> ptSubstitutions) {
		PropertyType pt = null;
		QName ptName = createQName(elementDecl.getNamespace(), elementDecl.getName());
		LOG.trace("- Property definition '" + ptName + "' uses a complex type for content definition.");
		// check for well known GML property declarations first
		if (typeDef.getName() != null) {
			QName typeName = createQName(typeDef.getNamespace(), typeDef.getName());
			if (typeName.equals(new QName(gmlSchema.getVersion().getNamespace(), "BoundingShapeType"))) {
				LOG.trace("Identified an EnvelopePropertyType.");
				pt = new EnvelopePropertyType(ptName, minOccurs, maxOccurs, elementDecl, ptSubstitutions);
			}
			else if (typeName.equals(new QName(gmlSchema.getVersion().getNamespace(), "FeatureArrayPropertyType"))) {
				LOG.trace("Identified a FeatureArrayPropertyType");
				pt = new ArrayPropertyType(ptName, minOccurs, maxOccurs, elementDecl, ptSubstitutions);
			}
		}
		// no success -> check if it is a GML object property declaration (feature,
		// geometry, ...)
		if (pt == null) {
			pt = gmlSchema.getGMLPropertyDecl(elementDecl, ptName, minOccurs, maxOccurs, ptSubstitutions);
		}
		// no success -> build custom property declaration
		if (pt == null) {
			pt = new CustomPropertyType(ptName, minOccurs, maxOccurs, elementDecl, ptSubstitutions);
		}
		return pt;
	}

	/**
	 * Combines the minOccurs/maxOccurs information from a parent model group with the
	 * current one.
	 * <p>
	 * This is necessary to cope with GML application schemas that don't follow good
	 * practices (minOccurs/maxOccurs should only be set on the property element
	 * declaration).
	 * </p>
	 * @param parentOccurs occurence information of the parent model group, -1 means
	 * unbounded
	 * @param occurs occurence information of the current particle, -1 means unbounded
	 * @return combined occurence information, -1 means unbounded
	 */
	private int getCombinedOccurs(int parentOccurs, int occurs) {
		if (parentOccurs == -1 || occurs == -1) {
			return -1;
		}
		return parentOccurs * occurs;
	}

	private QName createQName(String namespace, String localPart) {
		String prefix = nsToPrefix.get(namespace);
		if (prefix == null) {
			prefix = generatePrefix();
			nsToPrefix.put(namespace, prefix);
		}
		return new QName(namespace, localPart, prefix);
	}

	private String generatePrefix() {
		return "app" + prefixIndex++;
	}

	private String getCodeListId(final XSElementDeclaration elementDecl) {
		String codeListId = null;
		// handle adv schemas (referenced code list id inside annotation element)
		XSObjectList annotations = elementDecl.getAnnotations();
		if (annotations.getLength() > 0) {
			XSAnnotation annotation = (XSAnnotation) annotations.item(0);
			String s = annotation.getAnnotationString();
			XMLAdapter adapter = new XMLAdapter(new StringReader(s));
			NamespaceBindings nsContext = new NamespaceBindings();
			nsContext.addNamespace("xs", CommonNamespaces.XSNS);
			nsContext.addNamespace("adv", "http://www.adv-online.de/nas");
			codeListId = adapter.getNodeAsString(adapter.getRootElement(),
					new XPath("xs:appinfo/adv:referenzierteCodeList/text()", nsContext), null);
			if (codeListId != null) {
				codeListId = codeListId.trim();
			}
		}
		return codeListId;
	}

	private BaseType getPrimitiveType(final XSSimpleType typeDef) {
		final BaseType pt = BaseType.valueOf(typeDef);
		LOG.trace("Mapped '" + typeDef.getName() + "' (base type: '" + typeDef.getBaseType() + "') -> '" + pt + "'");
		return pt;
	}

}
