/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.feature.persistence.shape;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MILLISECOND;
import static org.deegree.commons.utils.EncodingGuesser.guess;
import static org.deegree.commons.utils.StringUtils.concat;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2_OR_3;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.persistence.shape.ShapeFeatureStoreProvider.Mapping;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.slf4j.Logger;

/**
 * <code>DBFReader</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class DBFReader {

	private static final Logger LOG = getLogger(DBFReader.class);

	private final int noOfRecords, recordLength, headerLength;

	private HashMap<String, Field> fields = new HashMap<String, Field>();

	private Map<String, PropertyType> properties = null;

	private LinkedList<String> fieldOrder = new LinkedList<String>();

	private final Charset encoding;

	private GenericFeatureType featureType;

	private final RandomAccessFile file;

	private FileChannel channel;

	// buffer is not thread-safe (needs to be duplicated for every thread)
	private final ByteBuffer sharedBuffer;

	/**
	 * Already reads/parses the header.
	 * @param in
	 * @param encoding
	 * @param ftName the name of the feature type, also used for namespace URI and
	 * namespace prefix of property declarations (must not be null)
	 * @param geomType the geometry type of the .shp
	 * @param mappings may be null, in which case the dbf names will be used
	 * @throws IOException
	 */
	public DBFReader(RandomAccessFile in, Charset encoding, QName ftName, GeometryType geomType, List<Mapping> mappings)
			throws IOException {
		this.encoding = encoding;
		this.file = in;
		channel = file.getChannel();
		sharedBuffer = channel.map(MapMode.READ_ONLY, 0, file.length());
		ByteBuffer buffer = sharedBuffer.asReadOnlyBuffer();
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		int version = getUnsigned(buffer);
		if (version < 3 || version > 5) {
			LOG.warn("DBase file is of unsupported version " + version + ". Trying to continue anyway...");
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace("Version number: " + version);
			int year = 1900 + getUnsigned(buffer);
			int month = getUnsigned(buffer);
			int day = getUnsigned(buffer);
			LOG.trace("Last modified: " + year + "/" + month + "/" + day);
		}
		else {
			skipBytes(buffer, 3);
		}

		noOfRecords = buffer.getInt();
		LOG.trace("Number of records: " + noOfRecords);

		headerLength = buffer.getShort();
		LOG.trace("Length of header: " + headerLength);

		recordLength = buffer.getShort();
		LOG.trace("Record length: " + recordLength);
		buffer.position(14);
		int dirty = getUnsigned(buffer);
		if (dirty == 1) {
			LOG.warn("DBase file is marked as 'transaction in progress'. Unexpected things may happen.");
		}
		int enc = getUnsigned(buffer);
		if (enc == 1) {
			LOG.warn("DBase file is marked as encrypted. This is unsupported, so you'll get garbage output.");
		}

		if (LOG.isTraceEnabled()) {
			buffer.position(29);
			LOG.trace("Language driver code is " + getUnsigned(buffer));
			skipBytes(buffer, 2);
		}
		else {
			buffer.position(32);
		}

		LinkedList<Byte> buf = new LinkedList<Byte>();
		LinkedList<PropertyType> types = new LinkedList<PropertyType>();

		String namespace = ftName.getNamespaceURI();
		String prefix = ftName.getPrefix();

		Map<String, Mapping> map = null;
		if (mappings != null) {
			map = new HashMap<String, Mapping>();
			properties = new HashMap<String, PropertyType>();
			for (Mapping m : mappings) {
				if (m.fieldname != null) {
					map.put(m.fieldname, m);
				}
			}
		}

		int read;
		while ((read = getUnsigned(buffer)) != 13) {
			while (read != 0 && buf.size() < 10) {
				buf.add((byte) read);
				read = getUnsigned(buffer);
			}

			skipBytes(buffer, 10 - buf.size());

			byte[] bs = new byte[buf.size()];
			for (int i = 0; i < bs.length; ++i) {
				bs[i] = buf.poll();
			}
			String name = getString(bs, encoding);

			char type = (char) getUnsigned(buffer);
			SimplePropertyType pt = null;

			skipBytes(buffer, 4);

			int fieldLength = getUnsigned(buffer);
			int fieldPrecision = getUnsigned(buffer);
			LOG.trace("Field length is " + fieldLength + ", type is " + type);

			// using the prefix here is vital for repairing of unqualified property names
			// in WFS...
			QName ptName;
			if (map != null && map.get(name) != null) {

				ptName = new QName(namespace, map.get(name).propname, prefix);
			}
			else {
				ptName = new QName(namespace, name, prefix);
			}
			switch (type) {
				case 'C':
					if (fieldPrecision > 0) {
						fieldLength += fieldPrecision << 8;
						LOG.trace("Field length is changed to " + fieldLength + " for text field.");
					}
					pt = new SimplePropertyType(ptName, 0, 1, BaseType.STRING, null, null);
					break;
				case 'N':
					pt = new SimplePropertyType(ptName, 0, 1, BaseType.DECIMAL, null, null);
					break;
				case 'L':
					pt = new SimplePropertyType(ptName, 0, 1, BaseType.BOOLEAN, null, null);
					break;
				case 'D':
					pt = new SimplePropertyType(ptName, 0, 1, BaseType.DATE, null, null);
					break;
				case 'F':
					pt = new SimplePropertyType(ptName, 0, 1, BaseType.DECIMAL, null, null);
					break;
				case 'T':
					LOG.warn(
							"Date/Time fields are not supported. Please send the file to the devs, so they can implement it.");
					break;
				case 'I':
					pt = new SimplePropertyType(ptName, 0, 1, BaseType.INTEGER, null, null);
					break;
				case '@':
					pt = new SimplePropertyType(ptName, 0, 1, BaseType.DATE_TIME, null, null);
					break;
				case 'O':
					LOG.warn(
							"Double fields are not supported. Please send the file to the devs, so they can implement it.");
					break;
				default:
					LOG.warn("Exotic field encountered: '" + type
							+ "'. Please send the file to the devs, so they can have a look.");
			}

			LOG.trace("Found field with name '" + name + "' and type "
					+ (pt != null ? pt.getPrimitiveType() : " no supported type."));

			fields.put(name, new Field(type, pt, fieldLength));
			fieldOrder.add(name);
			types.add(pt);
			if (map != null) {
				properties.put(name, pt);
			}

			skipBytes(buffer, 13);
			if (getUnsigned(buffer) == 1) {
				LOG.warn("Index found: index files are not supported by this implementation.");
			}
		}

		if (map != null) {
			createCustomMappedProperties(geomType, mappings, types, namespace, prefix);
		}
		else {
			types.add(new GeometryPropertyType(new QName(namespace, "geometry", prefix), 0, 1, null, null, geomType,
					DIM_2_OR_3, BOTH));
		}
		// TODO
		// properly determine the dimension from SHP type
		featureType = new GenericFeatureType(ftName, types, false);
	}

	private void createCustomMappedProperties(GeometryType geomType, List<Mapping> mappings,
			LinkedList<PropertyType> types, String namespace, String prefix) {
		types.clear();
		for (Mapping m : mappings) {
			if (m.fieldname == null) {
				types.add(new GeometryPropertyType(new QName(namespace, m.propname, prefix), 0, 1, null, null, geomType,
						DIM_2_OR_3, BOTH));
			}
			else {
				PropertyType pt = properties.get(m.fieldname);
				if (pt == null) {
					List<String> fieldNames = new ArrayList<String>(properties.keySet());
					String msg = "Error in mapping. DBF file does not have a field with name '" + m.fieldname
							+ "'. Defined fields are: " + concat(fieldNames, ", ");
					throw new IllegalArgumentException(msg);
				}
				types.add(pt);
			}
		}
	}

	private static String getString(byte[] bs, Charset encoding) throws UnsupportedEncodingException {
		if (encoding == null) {
			encoding = guess(bs);
		}
		if (encoding == null) {
			encoding = Charset.forName("UTF-8");
		}
		return new String(bs, encoding);
	}

	/**
	 * @param num zero based
	 * @return a map with the property types mapped to their value (which might be null)
	 * @throws IOException
	 */
	public HashMap<SimplePropertyType, Property> getEntry(int num) throws IOException {

		ByteBuffer buffer = sharedBuffer.asReadOnlyBuffer();
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		HashMap<SimplePropertyType, Property> map = new HashMap<SimplePropertyType, Property>();
		int pos = headerLength + num * recordLength;
		if (pos != buffer.position()) {
			buffer.position(pos);
			pos = headerLength + (num + 1) * recordLength;
		}
		if (getUnsigned(buffer) == 42) {
			LOG.warn("The record with number " + num + " is marked as deleted.");
		}

		for (String name : fieldOrder) {
			Field field = fields.get(name);

			Property property = null;

			byte[] bs = new byte[field.length];

			if (properties != null && !properties.containsKey(name)) {
				buffer.position(buffer.position() + field.length);
				continue;
			}

			switch (field.type) {
				case 'C': {
					buffer.get(bs);
					property = new SimpleProperty(field.propertyType, getString(bs, encoding).trim());
					break;
				}
				case 'N':
				case 'F': {
					buffer.get(bs);
					String str = getString(bs, encoding).trim();
					if (str.isEmpty() || str.startsWith("*")) {
						continue;
					}
					property = new SimpleProperty(field.propertyType, str);
					break;
				}
				case 'L': {
					char c = (char) getUnsigned(buffer);
					Boolean b = null;
					if (c == 'Y' || c == 'y' || c == 'T' || c == 't') {
						b = true;
					}
					if (c == 'N' || c == 'n' || c == 'F' || c == 'f') {
						b = false;
					}
					// TODO avoid string conversion
					property = new SimpleProperty(field.propertyType, "" + b);
					break;
				}
				case 'D': {
					buffer.get(bs);
					String val = new String(bs, 0, 4).trim();
					if (val.isEmpty()) {
						continue;
					}
					int year = Integer.valueOf(val);
					int month = Integer.valueOf(new String(bs, 4, 2));
					int day = Integer.valueOf(new String(bs, 6, 2));
					Calendar cal = new GregorianCalendar(year, month, day);
					PrimitiveType pt = field.propertyType.getPrimitiveType();
					PrimitiveValue pv = new PrimitiveValue(new Date(cal, true), pt);
					property = new SimpleProperty(field.propertyType, pv);
					break;
				}
				case 'I': {
					int ival = buffer.getInt();
					// TODO avoid string conversion
					property = new SimpleProperty(field.propertyType, "" + ival);
					break;
				}
				case '@': {
					int days = buffer.getInt();
					int millis = buffer.getInt();
					Calendar cal = new GregorianCalendar(-4713, 1, 1);
					cal.add(DAY_OF_MONTH, days); // it's lenient by default
					cal.add(MILLISECOND, millis);
					// TODO check this
					PrimitiveType pt = field.propertyType.getPrimitiveType();
					PrimitiveValue pv = new PrimitiveValue(new Date(cal, true), pt);
					property = new SimpleProperty(field.propertyType, pv);
					break;
				}
				case 'T':
				case 'O':
				default:
					LOG.trace("Skipping unsupported field " + field.propertyType.getName());
			}

			map.put(field.propertyType, property);
		}
		return map;
	}

	private int getUnsigned(ByteBuffer buffer) {
		return buffer.get() & 0xff;
	}

	/**
	 * Closes the underlying input stream.
	 * @throws IOException
	 */
	public void close() throws IOException {
		channel.close();
		file.close();
	}

	/**
	 * @return the property types of the contained fields
	 */
	public LinkedList<PropertyType> getFields() {
		LinkedList<PropertyType> list = new LinkedList<PropertyType>();

		for (String f : fieldOrder) {
			list.add(fields.get(f).propertyType);
		}

		return list;
	}

	static class Field {

		char type;

		SimplePropertyType propertyType;

		int length;

		Field(char c, SimplePropertyType pt, int l) {
			type = c;
			propertyType = pt;
			length = l;
		}

	}

	/**
	 * @return the feature type
	 */
	public GenericFeatureType getFeatureType() {
		return featureType;
	}

	private void skipBytes(ByteBuffer buffer, int bytes) {
		buffer.position(buffer.position() + bytes);
	}

	/**
	 * @return the total number of records in this dbf
	 */
	public int size() {
		return noOfRecords;
	}

}
