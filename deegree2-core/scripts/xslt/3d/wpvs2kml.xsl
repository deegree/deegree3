<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:gml="http://www.opengis.net/gml" 
xmlns:wpvs="http://www.deegree.org/app"
xmlns:wfs="http://www.opengis.net/wfs" 
xmlns:geometryutil="org.deegree.framework.xml.GeometryUtils"
xmlns:citygmlutil="org.deegree.framework.xml.CityGMLUtils"
xmlns="http://earth.google.com/kml/2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="SRCCRS"><xsl:value-of select="//@srsName"/></xsl:param>
	<xsl:param name="TARGETCRS">EPSG:4326</xsl:param>

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
				<xsl:call-template name="FEATURE"></xsl:call-template>
			</Document>
		</kml>
	</xsl:template>
	
	<xsl:template name="FEATURE">
		<xsl:for-each select="gml:featureMember/wpvs:WPVS">
			<Folder>
				<name>
					<xsl:value-of select="concat( 'root feature:', wpvs:fk_feature)"></xsl:value-of>
				</name>
				<Placemark>
					<xsl:apply-templates select=".//gml:Polygon | .//gml:Surface"></xsl:apply-templates>
				</Placemark>
			</Folder>
		</xsl:for-each>		
	</xsl:template>
	
	<xsl:template  match="gml:Polygon | gml:Surface">
		<Style>
			<PolyStyle>
				<color>
					<xsl:value-of select="citygmlutil:getColor( ../..//wpvs:diffusecolor,  ../..//wpvs:transparency )"></xsl:value-of>
				</color>
				<outline>0</outline>
			</PolyStyle>
		</Style>
		<Polygon>
			<altitudeMode>absolute</altitudeMode>
			<outerBoundaryIs>
				<LinearRing>
					<coordinates xmlns:java="java" xmlns:geometryutil="org.deegree.framework.xml.GeometryUtils">
						<xsl:value-of select="geometryutil:getPolygonOuterRing( . , $SRCCRS, $TARGETCRS )"></xsl:value-of>
					</coordinates>
				</LinearRing>
			</outerBoundaryIs>
			<xsl:for-each select=".//gml:interior | .//gml:innerBoundaryIs">
				<innerBoundaryIs>
					<LinearRing>
						<coordinates xmlns:java="java" xmlns:geometryutil="org.deegree.framework.xml.GeometryUtils">
							<xsl:value-of select="geometryutil:getPolygonInnerRing( .. , position(), $SRCCRS, $TARGETCRS )"></xsl:value-of>
							<xsl:value-of select="geometryutil:getPolygonInnerRing( ../.. , position(), $SRCCRS, $TARGETCRS )"></xsl:value-of>
							<xsl:value-of select="geometryutil:getPolygonInnerRing( ../../.. , position(), $SRCCRS, $TARGETCRS )"></xsl:value-of>
						</coordinates>
					</LinearRing>
				</innerBoundaryIs>
			</xsl:for-each>
		</Polygon>
	</xsl:template>
	
</xsl:stylesheet>
