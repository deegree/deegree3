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
package org.deegree.tools.feature.gml;

import java.net.MalformedURLException;
import java.util.List;

public class GMLSchemaComparator {

	private GMLSchemaAnalyzer schema1;

	private GMLSchemaAnalyzer schema2;

	private GMLSchemaComparator(GMLSchemaAnalyzer schema1, GMLSchemaAnalyzer schema2) {
		this.schema1 = schema1;
		this.schema2 = schema2;
	}

	public void compareFeatureContentModel(String featureName1, String featureName2) {

		// System.out.println( "\n\nFeatureType '" + featureName1 + "' (2006) -> '" +
		// featureName2 + "' (2008)\n" );
		//
		// System.out.println ("Properties in IMRO 2006:");
		// List<PropertyDeclaration> properties1 = schema1.getProperties( featureName1 );
		// List<PropertyDeclaration> properties2 = schema2.getProperties( featureName2 );
		//
		// for ( PropertyDeclaration declaration1 : properties1 ) {
		// PropertyDeclaration declaration2 = findSameProperty( declaration1, properties2
		// );
		// if ( declaration2 != null ) {
		// System.out.print( " - " + declaration1.name.getLocalPart() + ": match" );
		// if ( !declaration1.minOccurs.equals( declaration2.minOccurs )) {
		// System.out.print( " [minOccurs differs (" + declaration1.minOccurs + " != " +
		// declaration2.minOccurs
		// + ")]" );
		// }
		// if ( !declaration1.maxOccurs.equals( declaration2.maxOccurs )) {
		// System.out.print( " [maxOccurs differs (" + declaration1.maxOccurs + " != " +
		// declaration2.maxOccurs
		// + ")]" );
		// }
		// if ( !declaration1.typeName.getLocalPart().equals(
		// declaration2.typeName.getLocalPart())) {
		// System.out.print( " [type differs (" + declaration1.typeName.getLocalPart() + "
		// != " + declaration2.typeName.getLocalPart()
		// + ")]" );
		// }
		// System.out.println ();
		// } else {
		// System.out.println( " - " + declaration1.name.getLocalPart() + ": no match [" +
		// declaration1.toString() + "]" );
		// }
		// }
		//
		// System.out.println ("\nProperties in IMRO 2008 with no match in IMRO 2006:");
		// for ( PropertyDeclaration declaration : properties2 ) {
		// if (findSameProperty( declaration, properties1 ) == null) {
		// System.out.println( " - " + declaration.name.getLocalPart() + "[" +
		// declaration.toString() + "]" );
		// }
		// }
	}

	public PropertyDeclaration findSameProperty(PropertyDeclaration declaration, List<PropertyDeclaration> properties) {
		for (PropertyDeclaration property : properties) {
			if (declaration.name.getLocalPart().equals(property.name.getLocalPart())) {
				return property;
			}
		}
		return null;
	}

	/**
	 * @param args
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws ClassCastException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws ClassCastException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, MalformedURLException {
		//
		// System.setProperty( DOMImplementationRegistry.PROPERTY,
		// "org.apache.xerces.dom.DOMXSImplementationSourceImpl" );
		// DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
		// XSImplementation impl = (XSImplementation) registry.getDOMImplementation(
		// "XS-Loader" );
		// XSLoader schemaLoader = impl.createXSLoader( null );
		//
		// if ( args.length != 2 ) {
		// System.out.println( "Usage: GMLSchemaComparator <schemafile1> <schemafile2>" );
		// System.exit( 0 );
		// }
		//
		// File file = new File( args[0] );
		// System.out.println( "Loading schema 1: '" + file + "'" );
		// XSModel schema = schemaLoader.loadURI( file.toURI().toURL().toString() );
		// GMLSchemaAnalyzer analyzer1 = new GMLSchemaAnalyzer( schema,
		// schema.getNamespaceItems().item( 0 ).getSchemaNamespace() );
		//
		// file = new File( args[1] );
		// System.out.println( "Loading schema 2: '" + file + "'" );
		// schema = schemaLoader.loadURI( file.toURI().toURL().toString() );
		// GMLSchemaAnalyzer analyzer2 = new GMLSchemaAnalyzer( schema,
		// schema.getNamespaceItems().item( 0 ).getSchemaNamespace() );
		//
		// GMLSchemaComparator comparator = new GMLSchemaComparator( analyzer1, analyzer2
		// );
		//
		// SortedSet<String> featureDeclarations1 = analyzer1.getFeatureDeclarations();
		// SortedSet<String> featureDeclarations2 = analyzer2.getFeatureDeclarations();
		//
		// SortedSet<String> newFeatureDeclarations = featureDeclarations2;
		// newFeatureDeclarations.retainAll( featureDeclarations1 );
		// System.out.println( newFeatureDeclarations.size() + " feature declarations with
		// same name:" );
		// for ( String string : newFeatureDeclarations ) {
		// comparator.compareFeatureContentModel( string, string );
		// }
		//
		// comparator.compareFeatureContentModel( "GemeentelijkComplex",
		// "Structuurvisiecomplex_G" );
		// comparator.compareFeatureContentModel( "ProvinciaalComplex",
		// "Structuurvisiecomplex_P" );
		// comparator.compareFeatureContentModel( "NationaalComplex",
		// "Structuurvisiecomplex_R" );
		// comparator.compareFeatureContentModel( "GemeentelijkGebied",
		// "Structuurvisiegebied_G" );
		// comparator.compareFeatureContentModel( "ProvinciaalGebied",
		// "Structuurvisiegebied_P" );
		// comparator.compareFeatureContentModel( "NationaalGebied",
		// "Structuurvisiegebied_R" );
		//
		// comparator.compareFeatureContentModel( "StructuurvisieGebied",
		// "Structuurvisieplangebied_G" );
		// comparator.compareFeatureContentModel( "ProvinciaalPlangebied",
		// "Structuurvisieplangebied_P" );
		// comparator.compareFeatureContentModel( "NationaalPlangebied",
		// "Structuurvisieplangebied_R" );
	}

}
