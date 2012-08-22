//$HeadURL$
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

package org.deegree.ogcwebservices.csw.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcbase.SortProperty;

/**
 * Main component of a <code>GetRecords</code> request. A <code>GetRecords</code> request may
 * consist of several <code>Query</code> elements.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class Query {

    private String elementSetName;

    private List<PropertyPath> elementNamesAsPropertyPaths;

    private Filter constraint;

    private SortProperty[] sortProperties;

    private List<QualifiedName> typeNames;

    private Map<String, QualifiedName> declaredTypeNameVariables;

    private List<QualifiedName> elementSetNameTypeNamesList;

    private Map<String, QualifiedName> elementSetNameVariables;

    /**
     * Creates a new Query instance.
     *
     * @param elementSetName
     * @param elementNames
     *            a String array containing the requested ElementName propertypaths. If not null,
     *            they will be converted to an ArrayList<PropertyPath>.
     * @param sortProperties
     * @param constraint
     * @param typeNames
     *            which will be transformed to a list of qualified names.
     */
    Query( String elementSetName, String[] elementNames, Filter constraint,
           SortProperty[] sortProperties, String[] typeNames ) {
        List<QualifiedName> list = new ArrayList<QualifiedName>(
                                                                 ( ( typeNames != null ) ? typeNames.length
                                                                                        : 0 ) );
        if ( typeNames != null ) {
            for ( String tName : typeNames ) {
                list.add( new QualifiedName( tName ) );
            }
        }
        this.elementSetName = elementSetName;
        this.elementSetNameTypeNamesList = new ArrayList<QualifiedName>();
        elementNamesAsPropertyPaths = new ArrayList<PropertyPath>();
        if ( elementNames != null ) {
            for ( String en : elementNames ) {
                elementNamesAsPropertyPaths.add( PropertyPathFactory.createPropertyPath( new QualifiedName(
                                                                                                            en ) ) );
            }
        }

        this.constraint = constraint;
        this.sortProperties = sortProperties;
        this.typeNames = list;
        this.elementSetNameVariables = new HashMap<String, QualifiedName>();
        this.declaredTypeNameVariables = new HashMap<String, QualifiedName>();
    }

    /**
     * @param elementSetName
     * @param elementSetNameTypeNames
     *            the typenames (not the variables) which should be returned inside a
     *            GetRecordsReponse
     * @param elementSetNameVariables
     *            the variables (and their mapping to the TypeName) which were requested.
     * @param elementNames
     *            a list of propertyPath of propertys a client is interested in.
     * @param constraint
     * @param sortProperties
     * @param typeNames
     *            list of QualifiedNames which were defined in the query element.
     * @param typeNameVariables
     *            the variables (strings starting with an $-sign) which were declared
     *            typeNameattribtue in the Query element.
     */
    public Query( String elementSetName, List<QualifiedName> elementSetNameTypeNames,
                  Map<String, QualifiedName> elementSetNameVariables,
                  List<PropertyPath> elementNames, Filter constraint,
                  SortProperty[] sortProperties, List<QualifiedName> typeNames,
                  Map<String, QualifiedName> typeNameVariables ) {
        this.elementSetName = elementSetName;
        this.elementSetNameTypeNamesList = elementSetNameTypeNames;
        this.elementSetNameVariables = elementSetNameVariables;
        this.elementNamesAsPropertyPaths = elementNames;
        this.constraint = constraint;
        this.sortProperties = sortProperties;
        this.typeNames = typeNames;
        this.declaredTypeNameVariables = typeNameVariables;
    }

    /**
     * Zero or one (Optional); If <tt>null</tt> then getElementNames may return a list of
     * requested elements. If both methods returns <tt>null</tt> the default action is to present
     * all metadata elements.
     * <p>
     * The ElementName parameter is used to specify one or more metadata record elements that the
     * query should present in the response to the a GetRecords operation. Well known sets of
     * element may be named, in which case the ElementSetName parameter may be used (e. g.brief,
     * summary or full).
     * <p>
     * If neither parameter is specified, then a CSW shall present all metadata record elements
     *
     * @return the textual value (brief, summary, full) of the elementSetName node or null if none
     *         was given.
     */
    public String getElementSetName() {
        return elementSetName;
    }

    

    /**
     * Zero or one (Optional); Default action is to execute an unconstrained query
     *
     * @return the Filter which was given in the query.
     */
    public Filter getContraint() {
        return this.constraint;
    }

    /**
     * Ordered list of names of metadata elements to use for sorting the response. Format of each
     * list item is metadata_elemen_ name:A indicating an ascending sort or metadata_ element_name:D
     * indicating descending sort
     * <p>
     * The result set may be sorted by specifying one or more metadata record elements upon which to
     * sort.
     * <p>
     *
     * @todo verify return type URI[] or String
     * @return an Array of properties for sorting the response.
     */
    public SortProperty[] getSortProperties() {
        return this.sortProperties;
    }

    /**
     * The typeName parameter specifies the record type name that defines a set of metadata record
     * element names which will be constrained in the predicate of the query. In addition, all or
     * some of the these names may be specified in the query to define which metadata record
     * elements the query should present in the response to the GetRecords operation.
     *
     * @return the type names of the query.
     * @deprecated this function actually creates an Array of Strings using the values returned from
     *             the {@link QualifiedName#getFormattedString()} method or <code>null</code> if
     *             no typenames were requested. It is more correct to use the values of the
     *             {@link #getTypeNamesAsList()} method
     */
    @Deprecated
    public String[] getTypeNames() {
        if ( typeNames == null ) {
            return null;
        }
        String[] tNames = new String[typeNames.size()];
        for ( int i = 0; i < typeNames.size(); ++i ) {
            tNames[i] = typeNames.get( i ).getFormattedString();
        }
        return tNames;
    }

    /**
     * The typeName parameter specifies the record type name that defines a set of metadata record
     * element names which will be constrained in the predicate of the query. In addition, all or
     * some of the these names may be specified in the query to define which metadata record
     * elements the query should present in the response to the GetRecords operation.
     *
     * @return the type names of the query.
     */
    public List<QualifiedName> getTypeNamesAsList() {
        return typeNames;
    }

    /**
     * @return the variables (with a leading $ (dollar_sign) as a String), declared with the
     *         typeNames (given as {@link QualifiedName} ).
     */
    public Map<String, QualifiedName> getDeclaredTypeNameVariables() {
        return declaredTypeNameVariables;
    }

    /**
     * @return the requested elementNames as a list of PropertyPaths.
     */
    public List<PropertyPath> getElementNamesAsPropertyPaths() {
        return elementNamesAsPropertyPaths;
    }

    /**
     * @return the variables which were requested in the ElementSetNames/@typeNames attribute and
     *         the mapping to their typenames.
     */
    public Map<String, QualifiedName> getElementSetNameVariables() {
        return elementSetNameVariables;
    }

    /**
     * @return the typenames which were requested in the ElementSetNames/@typeNames attribute.
     */
    public List<QualifiedName> getElementSetNameTypeNamesList() {
        return elementSetNameTypeNamesList;
    }

}
