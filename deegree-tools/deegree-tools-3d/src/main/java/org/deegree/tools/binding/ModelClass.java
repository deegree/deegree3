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
package org.deegree.tools.binding;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.StringPair;
import org.deegree.commons.utils.StringUtils;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public abstract class ModelClass {

	public final static String SP = "    ";

	public final static String SP2 = SP + SP;

	public final static String SP3 = SP2 + SP;

	public final static String SP4 = SP3 + SP;

	private String className;

	private String packageName;

	private HashMap<String, Field> fields;

	private boolean isInterface;

	private String packageDir;

	private final boolean isAbstract;

	public ModelClass(QName name, String rootPackage, String packageDir, String defaultPackage, boolean isInterface,
			boolean isAbstract) {

		this.className = createBetterClassName(name.getLocalPart());

		if (defaultPackage == null) {
			String lastUriPart = CityGMLImporterGenerator.getPackageName(name.getNamespaceURI());
			this.packageName = rootPackage + lastUriPart.replace('/', '.');
			this.packageDir = packageDir + lastUriPart;
		}
		else {
			this.packageName = rootPackage + defaultPackage;
			this.packageDir = packageDir + defaultPackage + "/";
		}
		if (packageName.endsWith(".")) {
			packageName = packageName.substring(0, packageName.length() - 1);
		}
		fields = new HashMap<String, Field>();
		this.isInterface = isInterface;
		this.isAbstract = isAbstract;
	}

	/**
	 * @param string
	 * @param string2
	 * @param b
	 */
	public ModelClass(String className, String packageName, boolean isInterface) {
		this.packageName = packageName;
		this.className = className;
		this.isInterface = isInterface;
		this.isAbstract = true;
	}

	public abstract List<String> getImports(Map<QName, FeatureClass> featClasses);

	public String getClassQName() {
		return getPackageName() + "." + className;
	}

	/**
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return
	 */
	public String getPackageDir() {
		return packageDir;
	}

	public String getClassLocation() {
		return getPackageDir() + className + ".java";
	}

	/**
	 *
	 */
	protected void addField(Field field) {
		this.fields.put(field.getFieldName(), field);
	}

	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof ModelClass) {
			final ModelClass that = (ModelClass) other;
			return getClassQName().equals(that.getClassQName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		long result = 32452843;
		result = result * 37 + this.getClassQName().hashCode();
		return (int) (result >>> 32) ^ (int) result;
	}

	@Override
	public String toString() {
		return getClassQName();
	}

	/**
	 * @return the isInterface
	 */
	public boolean isInterface() {
		return isInterface;
	}

	/**
	 * @return
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	/**
	 * @param fieldName to test.
	 * @return true if a field with the given name was already defined.
	 */
	public boolean hasField(String fieldName) {
		return fields.containsKey(fieldName);

	}

	/**
	 * @param out
	 * @throws IOException
	 */
	public void writeHeader(Writer out) throws IOException {
		out.write("/*----------------------------------------------------------------------------\n");
		out.write("This file is part of deegree, http://deegree.org/\n");
		out.write("Copyright (C) 2001-2009 by:\n");
		out.write("Department of Geography, University of Bonn\n");
		out.write("and\n");
		out.write("lat/lon GmbH\n");
		out.write("\n");
		out.write("This library is free software; you can redistribute it and/or modify it under\n");
		out.write("the terms of the GNU Lesser General Public License as published by the Free\n");
		out.write("Software Foundation; either version 2.1 of the License, or (at your option)\n");
		out.write("any later version.\n");
		out.write("This library is distributed in the hope that it will be useful, but WITHOUT\n");
		out.write("ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\n");
		out.write("FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more\n");
		out.write("details.\n");
		out.write("You should have received a copy of the GNU Lesser General Public License\n");
		out.write("along with this library; if not, write to the Free Software Foundation, Inc.,\n");
		out.write("59 Temple Place, Suite 330, Boston, MA 02111-1307 USA\n");
		out.write("\n");
		out.write("Contact information:\n");
		out.write("\n");
		out.write("lat/lon GmbH\n");
		out.write("Aennchenstr. 19, 53177 Bonn\n");
		out.write("Germany\n");
		out.write("http://lat-lon.de/\n");
		out.write("\n");
		out.write("Department of Geography, University of Bonn\n");
		out.write("Prof. Dr. Klaus Greve\n");
		out.write("Postfach 1147, 53001 Bonn\n");
		out.write("Germany\n");
		out.write("http://www.geographie.uni-bonn.de/deegree/\n");
		out.write("\n");
		out.write("e-mail: info@deegree.org\n");
		out.write("----------------------------------------------------------------------------*/\n\n");

	}

	/**
	 * @param out
	 * @throws IOException
	 */

	public void writePackage(Writer out) throws IOException {
		out.write("package " + getPackageName() + ";\n");
	}

	/**
	 * @param out
	 * @param featClasses
	 * @throws IOException
	 */
	public void writeImports(BufferedWriter out, Map<QName, FeatureClass> featClasses) throws IOException {
		List<String> imports = getImports(featClasses);
		if (imports != null && !imports.isEmpty()) {
			Collections.sort(imports);
			for (String im : imports) {
				out.write("import " + im + ";\n");
			}
		}
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	public void writeClassDoc(Writer out) throws IOException {
		out.write("/**\n");
		out.write(" * Automatically created class from a feature schema.\n");
		out.write(" */\n");
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	public void writeClassStart(Writer out) throws IOException {
		StringBuilder sb = new StringBuilder("public ");
		if (isAbstract) {
			sb.append("abstract ");
		}
		if (isInterface()) {
			sb.append("interface");
		}
		else {
			sb.append("class ");
		}
		sb.append(className);
		sb.append(" {\n");
		out.write(sb.toString());
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	public void writeFields(Writer out) throws IOException {
		Iterator<String> it = fields.keySet().iterator();
		while (it.hasNext()) {
			String fieldName = it.next();
			Field field = fields.get(fieldName);
			writeField(out, field);
		}
	}

	protected static void writeField(Writer out, Field field) throws IOException {
		out.write(SP);
		out.write(field.getModifier().toString());
		if (field.isStatic()) {
			out.write(" static");
		}
		if (field.isFinal()) {
			out.write(" final");
		}
		out.write(" ");
		if (field.isList()) {
			out.write("List<");
			out.write(field.getTypeName());
			out.write("> ");
			out.write(field.getFieldName());

		}
		else {
			out.write(field.getTypeName());
			out.write(" ");
			out.write(field.getFieldName());
		}
		if (field.getValue() != null) {
			out.write(" = " + field.getValue());
		}
		else {
			if (field.isList()) {
				out.write(" = new ArrayList<");
				out.write(field.getTypeName());
				out.write(">()");
			}
		}

		out.write(";\n");
	}

	/**
	 * @param out
	 */
	public void writeMethods(Writer out, HashMap<QName, FeatureClass> featClasses) throws IOException {
		generateGettersSetters(out);
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	protected void generateGettersSetters(Writer out) throws IOException {
		Iterator<String> it = fields.keySet().iterator();
		while (it.hasNext()) {
			String fieldName = it.next();
			Field field = fields.get(fieldName);
			generateGetter(out, field);
			generateSetter(out, field);
		}
	}

	protected static void openMethod(Writer out, String access, String returnType, String name, List<StringPair> params,
			List<String> thro, boolean override) throws IOException {
		out.write(SP);
		if (override) {
			out.write("@Override\n");
			out.write(SP);
		}
		out.write(access);
		if (returnType != null) {
			out.write(" ");
			out.write(returnType);
		}
		out.write(" ");
		out.write(name);
		out.write("( ");
		if (params != null && !params.isEmpty()) {
			Iterator<StringPair> it = params.iterator();
			while (it.hasNext()) {
				StringPair pv = it.next();
				out.write(pv.first);
				out.write(" ");
				out.write(pv.second);
				if (it.hasNext()) {
					out.write(", ");
				}
				else {
					out.write(" ");
				}
			}
		}
		else {
			out.write(" ");
		}
		out.write(") ");
		if (thro != null && !thro.isEmpty()) {
			Iterator<String> it = thro.iterator();
			out.write("throws ");
			while (it.hasNext()) {
				out.write(it.next());
				if (it.hasNext()) {
					out.write(", ");
				}
			}
		}
		out.write("{\n");
	}

	protected static void closeMethod(Writer out) throws IOException {
		out.write(SP);
		out.write("}\n");
		out.write("\n");
	}

	/**
	 * @param sb
	 * @param fieldType
	 * @param fieldName
	 * @throws IOException
	 */
	protected void generateSetter(Writer out, Field field) throws IOException {
		List<StringPair> params = new LinkedList<StringPair>();
		String nFN = createBetterClassName(field.getFieldName());
		params.add(new StringPair(field.getTypeName(), "new" + nFN));
		if (field.hasSubstitutions()) {
			params.add(new StringPair("QName", "propertyName"));
		}
		List<String> doc = new ArrayList<String>();
		doc.add("Set a new value for " + field.getTypeName());
		doc.add("@param new" + nFN + " the new value for " + field.getTypeName());
		if (field.hasSubstitutions()) {
			doc.add("@param propertyName name of the property for which the value is valid for.");
		}
		generateMethodDoc(out, doc);
		openMethod(out, "public", "void", "set" + nFN, params, null, false);
		out.write(SP2);
		out.write("// add to global property map.\n");
		out.write(SP2);
		if (field.isList()) {
			out.write("this.");
			out.write(field.getFieldName());
			out.write(".add( ");
			out.write("new");
			out.write(nFN);
			out.write(" );\n");
		}
		else {
			out.write("this.");
			out.write(field.getFieldName());
			out.write(" = ");
			out.write("new");
			out.write(nFN);
			out.write(";\n");
		}
		closeMethod(out);

	}

	protected void generateGetter(Writer out, Field field) throws IOException {
		List<String> doc = new ArrayList<String>();
		if (field.isList()) {
			doc.add("Gets a copy of the internal list. Modifications to the list will not be visible to the instance.");
		}
		doc.add("@return the " + field.getFieldName() + " from this instance.");
		generateMethodDoc(out, doc);
		openPublic(out, field.isList() ? "List<" + field.getTypeName() + ">" : field.getTypeName(),
				"get" + createBetterClassName(field.getFieldName()));
		out.write(SP2);
		if (field.isList()) {
			out.write("return new ArrayList<");
			out.write(field.getTypeName());
			out.write(">( ");
			out.write(field.getFieldName());
			out.write(" );\n");
		}
		else {
			out.append("return ").append(field.getFieldName()).append(";\n");
		}
		closeMethod(out);
	}

	protected static void generateMethodDoc(Writer out, List<String> lines) throws IOException {

		out.write(SP);
		out.write("/**\n");
		if (lines != null) {
			for (String line : lines) {
				out.write(SP);
				out.write(" * ");
				out.write(line);
				out.write("\n");
			}
		}
		out.write(SP);
		out.write(" */\n");
	}

	protected static void openPublic(Writer out, String returnType, String name) throws IOException {
		openMethod(out, "public", returnType, name, null, null, false);
	}

	/**
	 * Removes underscores and sets the first Character to an Uppercase.
	 * @param someValue
	 * @return the new class name
	 */
	public static String createBetterClassName(String someValue) {
		if (StringUtils.isSet(someValue)) {
			String t = someValue.replace("_", "");
			String result = modifyKeyword(t);
			StringBuilder sb = new StringBuilder(result);
			sb.replace(0, 1, t.substring(0, 1).toUpperCase());
			return sb.toString();
		}
		return someValue;
	}

	/**
	 * Removes underscores and sets the first Character to a lowercase.
	 * @param someValue
	 * @return the new class name
	 */
	public static String createBetterMethodName(String someValue) {
		if (StringUtils.isSet(someValue)) {
			String t = someValue.replace("_", "");
			String result = modifyKeyword(t);
			StringBuilder sb = new StringBuilder(result);
			sb.replace(0, 1, t.substring(0, 1).toLowerCase());
			return sb.toString();
		}
		return someValue;
	}

	protected String createFieldName(String propName) {
		String result = modifyKeyword(propName);
		int i = 2;
		while (fields.containsKey(result)) {
			result = propName + (i++);
		}
		return result;
	}

	private static String modifyKeyword(String propName) {
		if ("abstract".equals(propName)) {
			return propName + "_";
		}
		else if ("continue".equals(propName)) {
			return propName + "_";
		}
		else if ("for".equals(propName)) {
			return propName + "_";
		}
		else if ("new".equals(propName)) {
			return propName + "_";
		}
		else if ("switch".equals(propName)) {
			return propName + "_";
		}
		else if ("assert".equals(propName)) {
			return propName + "_";
		}
		else if ("default".equals(propName)) {
			return propName + "_";
		}
		else if ("goto".equals(propName)) {
			return propName + "_";
		}
		else if ("package".equals(propName)) {
			return propName + "_";
		}
		else if ("synchronized".equals(propName)) {
			return propName + "_";
		}
		else if ("boolean".equals(propName)) {
			return propName + "_";
		}
		else if ("do".equals(propName)) {
			return propName + "_";
		}
		else if ("if".equals(propName)) {
			return propName + "_";
		}
		else if ("private".equals(propName)) {
			return propName + "_";
		}
		else if ("this".equals(propName)) {
			return propName + "_";
		}
		else if ("break".equals(propName)) {
			return propName + "_";
		}
		else if ("double".equals(propName)) {
			return propName + "_";
		}
		else if ("implements".equals(propName)) {
			return propName + "_";
		}
		else if ("protected".equals(propName)) {
			return propName + "_";
		}
		else if ("throw".equals(propName)) {
			return propName + "_";
		}
		else if ("byte".equals(propName)) {
			return propName + "_";
		}
		else if ("else".equals(propName)) {
			return propName + "_";
		}
		else if ("import".equals(propName)) {
			return propName + "_";
		}
		else if ("public".equals(propName)) {
			return propName + "_";
		}
		else if ("throws".equals(propName)) {
			return propName + "_";
		}
		else if ("case".equals(propName)) {
			return propName + "_";
		}
		else if ("enum".equals(propName)) {
			return propName + "_";
		}
		else if ("instanceof".equals(propName)) {
			return propName + "_";
		}
		else if ("return".equals(propName)) {
			return propName + "_";
		}
		else if ("transient".equals(propName)) {
			return propName + "_";
		}
		else if ("catch".equals(propName)) {
			return propName + "_";
		}
		else if ("extends".equals(propName)) {
			return propName + "_";
		}
		else if ("int".equals(propName)) {
			return propName + "_";
		}
		else if ("short".equals(propName)) {
			return propName + "_";
		}
		else if ("try".equals(propName)) {
			return propName + "_";
		}
		else if ("char".equals(propName)) {
			return propName + "_";
		}
		else if ("final".equals(propName)) {
			return propName + "_";
		}
		else if ("interface".equals(propName)) {
			return propName + "_";
		}
		else if ("static".equals(propName)) {
			return propName + "_";
		}
		else if ("void".equals(propName)) {
			return propName + "_";
		}
		else if ("class".equals(propName)) {
			return propName + "_";
		}
		else if ("finally".equals(propName)) {
			return propName + "_";
		}
		else if ("long".equals(propName)) {
			return propName + "_";
		}
		else if ("strictfp".equals(propName)) {
			return propName + "_";
		}
		else if ("volatile".equals(propName)) {
			return propName + "_";
		}
		else if ("const".equals(propName)) {
			return propName + "_";
		}
		else if ("float".equals(propName)) {
			return propName + "_";
		}
		else if ("native".equals(propName)) {
			return propName + "_";
		}
		else if ("super".equals(propName)) {
			return propName + "_";
		}
		else if ("while".equals(propName)) {
			return propName + "_";
		}
		return propName;

	}

	public static String map(String key, String val) {
		return "Map<" + key + "," + val + ">";
	}

	public static String linkedHashMap(String key, String val, boolean createNew) {
		if (createNew) {
			return "new LinkedHashMap<" + key + "," + val + ">()";
		}
		return "LinkedHashMap<" + key + "," + val + ">";
	}

	public static String hashMap(String key, String val, boolean createNew) {
		if (createNew) {
			return "new HashMap<" + key + "," + val + ">()";
		}
		return "HashMap<" + key + "," + val + ">";
	}

	public static String list(String key) {
		return "List<" + key + ">";
	}

	public static String arlist(String key) {
		return arlist(key, false, -1);
	}

	public static String arlist(String key, boolean createNew, int size) {
		if (createNew) {
			String s = "";
			if (size > 0) {
				s = Integer.toString(size);
			}
			return "new ArrayList<" + key + ">(" + s + ")";
		}
		return "ArrayList<" + key + ">";
	}

	public static String oif(boolean and, String... args) {
		StringBuilder sb = new StringBuilder();
		sb.append("if ( ");
		final String cond = and ? " && " : " || ";
		for (int i = 0; i < args.length; ++i) {
			sb.append(args[i].trim());
			if ((i + 1) < args.length) {
				sb.append(cond);
			}
		}
		sb.append(" ) {\n");
		return sb.toString();
	}

	public static String newMap(String key, String val, String varName) {
		StringBuilder result = new StringBuilder(map(key, val));
		result.append(varName).append(" = ");
		result.append(hashMap(key, val, true));
		result.append(";\n");
		return result.toString();
	}

	public static String newList(String type, String varName) {
		StringBuilder result = new StringBuilder(list(type));
		result.append(varName).append(" = ");
		result.append(arlist(type, true, -1));
		result.append(";\n");
		return result.toString();
	}

	public static String newSet(String type, String varName) {
		StringBuilder result = new StringBuilder(set(type));
		result.append(varName).append(" = ");
		result.append(hashSet(type, true));
		result.append(";\n");
		return result.toString();
	}

	/**
	 * @param type
	 * @param b
	 * @return
	 */
	public static String hashSet(String type, boolean createNew) {
		if (createNew) {
			return "new HashSet<" + type + ">()";
		}
		return "HashSet<" + type + ">";
	}

	/**
	 * @param type
	 * @return
	 */
	public static String set(String type) {
		return "Set<" + type + ">";
	}

}
