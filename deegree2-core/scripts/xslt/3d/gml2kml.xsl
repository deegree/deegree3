<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" 
xmlns:wfs="http://www.opengis.net/wfs" 
xmlns="http://earth.google.com/kml/2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="gml2kml_style.xsl"></xsl:include>
	<xsl:include href="gml2kml_geometry.xsl"></xsl:include>
	<xsl:include href="gml2kml_building.xsl"></xsl:include>

	<xsl:template match="wfs:FeatureCollection">
		<kml>
			<Document>
				<Style id="defaultPolygonStyle">
					<PolyStyle>
						<color>e5aaaaaa</color>
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
				<xsl:for-each select="gml:featureMember">
					<xsl:apply-templates select="."></xsl:apply-templates>
				</xsl:for-each>
			</Document>
		</kml>
	</xsl:template>
	
	<xsl:template match="citygml:Feature">
		<Folder>
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
			<xsl:apply-templates select="citygml:building"/>
		</Folder>
	</xsl:template>

</xsl:stylesheet>
