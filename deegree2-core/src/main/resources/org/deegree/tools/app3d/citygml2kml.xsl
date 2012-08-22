<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" 
xmlns:wfs="http://www.opengis.net/wfs" 
xmlns="http://earth.google.com/kml/2.0"
xmlns:xAL="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="citygml2kml_geometry.xsl"></xsl:include>
	<xsl:include href="citygml2kml_building.xsl"></xsl:include>

	<xsl:template match="wfs:FeatureCollection | citygml:CityModel">
		<kml>
			<Document>
				<Style id="defaultPolygonStyle">
					<PolyStyle>
						<color>e5ff0000</color>
						<outline>0</outline>
					</PolyStyle>
				</Style>
				<xsl:if test="gml:name">
				  	<name>
				  		<xsl:value-of select="gml:name"></xsl:value-of>
				  	</name>
			  	</xsl:if>
			  	<xsl:if test="gml:description">
				  	<description>
				  		<xsl:value-of select="gml:description"></xsl:value-of>
				  	</description>
			  	</xsl:if>
			  	<LookAt xmlns:java="java" xmlns:geometryutil="org.deegree.framework.xml.GeometryUtils">
					<longitude>
						<xsl:value-of select="geometryutil:getCentroidX( gml:boundedBy/child::*[1], 'EPSG:4326' )"/>
					</longitude>
					<latitude>
						<xsl:value-of select="geometryutil:getCentroidY( gml:boundedBy/child::*[1], 'EPSG:4326' )"/>
					</latitude>
				</LookAt>
				<xsl:for-each select="citygml:cityObjectMember | gml:featureMember">
					<xsl:apply-templates select="."></xsl:apply-templates>
				</xsl:for-each>
			</Document>
		</kml>
	</xsl:template>
	
	<xsl:template match="citygml:Building | citygml:BuildingPart">
		<Folder>
		    <xsl:if test="gml:name">
			  	<name>
			  		<xsl:value-of select="gml:name"></xsl:value-of>
			  	</name>
		  	</xsl:if>
		  	<description>
		  		Building/BuildingPart:
		  		<xsl:if test="boolean( gml:description )">
			  		<xsl:value-of select="gml:description"></xsl:value-of>
		  		</xsl:if>
		  		<xsl:if test="boolean( citygml:class )">
		  			class: <xsl:value-of select="citygml:class"/>
		  		</xsl:if>
		  		<xsl:for-each select="citygml:function">
		  			function: <xsl:value-of select="."/>
		  		</xsl:for-each>
		  		<xsl:for-each select="citygml:usage">
		  			usage: <xsl:value-of select="."/>
		  		</xsl:for-each>
		  		<xsl:if test="boolean( citygml:yearOfConstruction )">
		  			yearOfConstruction: <xsl:value-of select="citygml:yearOfConstruction"/>
		  		</xsl:if>
		  		<xsl:if test="boolean( citygml:roofType )">
		  			roofType: <xsl:value-of select="citygml:roofType"/>
		  		</xsl:if>
		  		<xsl:if test="boolean( citygml:measuredHeigth )">
		  			measuredHeigth: <xsl:value-of select="citygml:measuredHeigth"/>
		  		</xsl:if>
		  		<xsl:if test="boolean( citygml:storeysAboveGround )">
		  			storeysAboveGround: <xsl:value-of select="citygml:storeysAboveGround"/>
		  		</xsl:if>
		  		<xsl:if test="boolean( citygml:storeysBelowGround )">
		  			storeysBelowGround: <xsl:value-of select="citygml:storeysBelowGround"/>
		  		</xsl:if>
		  		<xsl:if test="boolean( citygml:storeyHeightsAboveGround )">
		  			storeyHeightsAboveGround: <xsl:value-of select="citygml:storeyHeightsAboveGround"/>
		  		</xsl:if>
		  		<xsl:if test="boolean( citygml:storeyHeightsBelowGround )">
		  			storeyHeightsBelowGround: <xsl:value-of select="citygml:storeyHeightsBelowGround"/>
		  		</xsl:if>
		  		<xsl:if test="boolean( address ) ">
		  			<xsl:if test="boolean( citygml:address//xAL:Locality[./@Type = 'Town']/xAL:PostalCode/xAL:PostalCodeNumber )">
						Postal Code: <xsl:value-of select="citygml:address//xAL:Locality[./@Type = 'Town']/xAL:PostalCode/xAL:PostalCodeNumber"></xsl:value-of>
					</xsl:if>
					<xsl:if test="boolean( citygml:address//xAL:Locality[./@Type = 'Town']/xAL:LocalityName )">
						City: <xsl:value-of select="citygml:address//xAL:Locality[./@Type = 'Town']/xAL:LocalityName"></xsl:value-of>
					</xsl:if>
			  		<xsl:if test="boolean( citygml:address//xAL:Locality[./@Type = 'Town']/xAL:Thoroughfare[./@Type = 'Street']/xAL:ThoroughfareName )">
						Street:  <xsl:value-of select="citygml:address//xAL:Locality[./@Type = 'Town']/xAL:Thoroughfare[./@Type = 'Street']/xAL:ThoroughfareName"></xsl:value-of>
							       <xsl:value-of select="citygml:address//xAL:Locality[./@Type = 'Town']/xAL:Thoroughfare[./@Type = 'Street']/xAL:ThoroughfareNumber"></xsl:value-of>
					</xsl:if>					
					<xsl:if test="boolean( citygml:address//xAL:Country/xAL:CountryName )">
						Country: <xsl:value-of select="citygml:address/citygml:Address/citygml:xalAddress/xAL:AddressDetails/xAL:Country/xAL:CountryName"></xsl:value-of>
					</xsl:if>
		  		</xsl:if>
		  	</description>
			<xsl:call-template name="BUILDING"/>
		</Folder>
	</xsl:template>

</xsl:stylesheet>
