<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:citygml="http://www.citygml.org/citygml/1/0/0" 
xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc" 
xmlns:gml="http://www.opengis.net/gml" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:java="java" xmlns:mapping="de.latlon.citygml.Mapping" 
xmlns:local="de.latlon.citygml.XSLTHelper" exclude-result-prefixes="wfs fo xsl ogc local mapping">
	<!-- ====================================================================== -->
	<xsl:include href="./citygmlTOgml3.xsl"/>
	<!-- ====================================================================== -->
	<xsl:param name="REMOVEPREFIX">true</xsl:param>
	<xsl:template match="/">
		<xsl:apply-templates select="wfs:GetFeature"/>
		<xsl:apply-templates select="wfs:Transaction"/>
	</xsl:template>
	<xsl:template match="wfs:GetFeature">
		<wfs:GetFeature>
			<xsl:copy-of select="./@*"/>
			<xsl:apply-templates select="wfs:Query"/>
		</wfs:GetFeature>
	</xsl:template>
	<xsl:template match="wfs:Query">
		<wfs:Query typeName="citygml:Feature">
			<xsl:choose>
				<xsl:when test="boolean( wfs:PropertyName )">
					<xsl:for-each select="wfs:PropertyName">
						<wfs:PropertyName>
							<xsl:value-of select="mapping:mapPropertyValue( ., ../@typeName )"/>
						</wfs:PropertyName>
					</xsl:for-each>
					<wfs:PropertyName>citygml:type</wfs:PropertyName>
				</xsl:when>
				<xsl:otherwise>
					<wfs:PropertyName>citygml:id</wfs:PropertyName>
					<wfs:PropertyName>citygml:type</wfs:PropertyName>
					<wfs:PropertyName>citygml:creation</wfs:PropertyName>
					<wfs:PropertyName>citygml:revision</wfs:PropertyName>
					<wfs:PropertyName>citygml:deletion</wfs:PropertyName>
					<!--
					<wfs:PropertyName>citygml:lod1tis</wfs:PropertyName>
					<wfs:PropertyName>citygml:lod2tis</wfs:PropertyName>
					<wfs:PropertyName>citygml:lod3tis</wfs:PropertyName>
					<wfs:PropertyName>citygml:lod4tis</wfs:PropertyName>
					-->
					<wfs:PropertyName>citygml:envelope</wfs:PropertyName>
					<wfs:PropertyName>citygml:featureName</wfs:PropertyName>
					<wfs:PropertyName>citygml:externalReference</wfs:PropertyName>
					<wfs:PropertyName>citygml:function</wfs:PropertyName>
					<wfs:PropertyName>citygml:usage</wfs:PropertyName>
					<wfs:PropertyName>citygml:linkedFeature</wfs:PropertyName>
					<wfs:PropertyName>citygml:linkedGeometry</wfs:PropertyName>
					<wfs:PropertyName>citygml:stringAttribute</wfs:PropertyName>
					<xsl:choose>
						<xsl:when test="substring-after(./@typeName, ':' ) = 'Building' ">							
							<wfs:PropertyName>citygml:building</wfs:PropertyName>
							<wfs:PropertyName>citygml:address</wfs:PropertyName>
						</xsl:when>
						<xsl:when test="substring-after(./@typeName, ':' ) = 'Road' ">							
							<wfs:PropertyName>citygml:road</wfs:PropertyName>
						</xsl:when>
						<xsl:when test="substring-after(./@typeName, ':' ) = 'Railway' ">							
							<wfs:PropertyName>citygml:railway</wfs:PropertyName>
						</xsl:when>
						<xsl:when test="substring-after(./@typeName, ':' ) = 'CityFurniture' ">							
							<wfs:PropertyName>citygml:cityFurniture</wfs:PropertyName>
						</xsl:when>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
			<!--
			<xsl:apply-templates select="wfs:PropertyName"/>
			-->
			<xsl:choose>
				<xsl:when test="boolean( ogc:Filter )">
					<xsl:apply-templates select="ogc:Filter"/>
				</xsl:when>
				<xsl:otherwise>
					<ogc:Filter>
						<ogc:PropertyIsEqualTo>
							<ogc:PropertyName>citygml:type</ogc:PropertyName>
							<ogc:Literal>
								<xsl:value-of select="local:getLocalName(./@typeName)"/>
							</ogc:Literal>
						</ogc:PropertyIsEqualTo>
					</ogc:Filter>
				</xsl:otherwise>
			</xsl:choose>
		</wfs:Query>
	</xsl:template>
	<xsl:template match="ogc:Filter">
		<ogc:Filter>
			<ogc:And>
				<ogc:PropertyIsEqualTo>
					<ogc:PropertyName>citygml:type</ogc:PropertyName>
					<ogc:Literal>
						<xsl:value-of select="local:getLocalName(../@typeName)"/>
					</ogc:Literal>
				</ogc:PropertyIsEqualTo>
				<xsl:choose>
					<xsl:when test="count( ogc:FeatureId ) = 1 or count( ogc:GmlObjectId ) = 1">
						<xsl:apply-templates select="ogc:FeatureId | ogc:GmlObjectId"/>
					</xsl:when>
					<xsl:when test="count( ogc:FeatureId ) > 1 or count( ogc:GmlObjectId ) > 1">
						<ogc:Or>
							<xsl:apply-templates select="ogc:FeatureId | ogc:GmlObjectId"/>
						</ogc:Or>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="node()|@*"/>
					</xsl:otherwise>
				</xsl:choose>
			</ogc:And>
		</ogc:Filter>
	</xsl:template>
	<xsl:template match="ogc:FeatureId | ogc:GmlObjectId">
		<xsl:variable name="TMP">
			<xsl:value-of select="./@fid"/>
			<xsl:value-of select="./@gml:id"/>
		</xsl:variable>
		<xsl:variable name="ID">
			<xsl:choose>
				<xsl:when test="$REMOVEPREFIX = 'true'">
					<xsl:value-of select="substring-after( $TMP, '_' ) "/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$TMP"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<ogc:PropertyIsEqualTo>
			<ogc:PropertyName>citygml:id</ogc:PropertyName>
			<ogc:Literal>
				<xsl:value-of select="$ID"/>
			</ogc:Literal>
		</ogc:PropertyIsEqualTo>
	</xsl:template>
	<xsl:template match="node()|@*">
		<xsl:variable name="TMP">
			<xsl:value-of select="local-name(.) "/>
		</xsl:variable>
		<xsl:copy>
			<xsl:choose>
				<xsl:when test="$TMP = 'PropertyName' ">
					<xsl:if test="local-name(.) = 'PropertyName'">
						<xsl:value-of select="mapping:mapPropertyValue( ., //@typeName )"/>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="node()|@*"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="wfs:Transaction">
		<wfs:Transaction version="1.1.0" service="WFS" xmlns:citygml="http://www.citygml.org/citygml/1/0/0" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:wfs="http://www.opengis.net/wfs">
			<xsl:apply-templates select="wfs:Insert"/>
			<xsl:apply-templates select="wfs:Update"/>
			<xsl:apply-templates select="wfs:Delete"/>
		</wfs:Transaction>
	</xsl:template>
	<xsl:template match="wfs:Insert">
		<xsl:copy>
			<xsl:if test="./@handle">
				<xsl:attribute name="handle"><xsl:value-of select="./@handle"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="./@idgen">
				<xsl:attribute name="idgen"><xsl:value-of select="./@idgen"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each select="./child::*">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="wfs:Update">
		<wfs:Update typeName="citygml:Feature">
			<xsl:if test="./@handle">
				<xsl:attribute name="handle"><xsl:value-of select="./@handle"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each select="./child::*">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
		</wfs:Update>
	</xsl:template>
	<xsl:template match="wfs:Delete">
		<wfs:Delete typeName="citygml:Feature">
			<xsl:if test="./@handle">
				<xsl:attribute name="handle"><xsl:value-of select="./@handle"/></xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="ogc:Filter"/>
		</wfs:Delete>
	</xsl:template>
	<xsl:template match="wfs:Property">
		<wfs:Property>
			<wfs:Name>
				<xsl:value-of select="mapping:mapPropertyValue( ./wfs:Name, //@typeName )"/>
			</wfs:Name>
			<xsl:choose>
				<xsl:when test="wfs:Value = 'false' ">
					<wfs:Value>0</wfs:Value>
				</xsl:when>
				<xsl:when test="wfs:Value = 'true' ">
					<wfs:Value>1</wfs:Value>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="wfs:Value"/>
				</xsl:otherwise>
			</xsl:choose>
		</wfs:Property>
	</xsl:template>
</xsl:stylesheet>
