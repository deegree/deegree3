<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:app="http://www.deegree.org/app"
	xmlns:csw="http://www.opengis.net/cat/csw"
	xmlns:gml="http://www.opengis.net/gml"
	xmlns:ogc="http://www.opengis.net/ogc"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gts="http://www.isotc211.org/2005/gts"  	
	xmlns:wfs="http://www.opengis.net/wfs"  
    xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:deegreewfs="http://www.deegree.org/wfs"
	xmlns:java="java"
	xmlns:mapping="org.deegree.ogcwebservices.csw.iso_profile.Mapping2_0_2_blob" version="1.0">

	<xsl:variable name="map" select="mapping:new( )"/>	
	
	<xsl:param name="NSP">a:a</xsl:param>
	
	<xsl:output encoding="UTF-8" indent="yes" method="xml" version="1.0" />
	<!-- ======================================================== -->
	<xsl:include href="iso19115_transaction_blob.xsl" />
	<xsl:include href="iso19119_transaction_blob.xsl" />
	<!-- ======================================================== -->
	<xsl:template match="csw:Transaction">
		<wfs:Transaction xmlns:xlink="http://www.w3.org/1999/xlink"
			service="WFS" version="1.1.0">
			<xsl:apply-templates select="csw:Insert" />
			<xsl:apply-templates select="csw:Update" />
			<xsl:apply-templates select="csw:Delete" />
		</wfs:Transaction>
	</xsl:template>
	<xsl:template match="csw:Insert">
		<wfs:Insert idgen="GenerateNew">
			<xsl:variable name="hierarchyLevel" select="gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
			<xsl:choose>
				<xsl:when test="boolean( $hierarchyLevel = 'service' )">
					<xsl:call-template name="SERVICEMETADATA"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="METADATA"/>			
				</xsl:otherwise>
			</xsl:choose>
		</wfs:Insert>
	</xsl:template>
	<xsl:template match="csw:Update">
		<wfs:Update typeName="app:CQP_Main">
		<xsl:variable name="hierarchyLevel" select="gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
			<xsl:choose>
				<xsl:when test="boolean( $hierarchyLevel = 'service' )">
					<xsl:call-template name="SERVICEMETADATA"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="METADATA"/>			
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="csw:Constraint/ogc:Filter" />
		</wfs:Update>
	</xsl:template>
	<xsl:template match="csw:Delete">
		<wfs:Delete typeName="app:CQP_Main">
			<xsl:apply-templates select="csw:Constraint/ogc:Filter" />
		</wfs:Delete>
	</xsl:template>

	
	<!-- =========================================================== 
		FILTER
		===============================================================-->
	<xsl:template match="csw:Constraint/ogc:Filter">
		<ogc:Filter>
			<xsl:apply-templates select="ogc:And" />
			<xsl:apply-templates select="ogc:Or" />
			<xsl:apply-templates select="ogc:Not" />
			<xsl:if
				test="local-name(./child::*[1]) != 'And' and local-name(./child::*[1])!='Or' and local-name(./child::*[1])!='Not'">
				<xsl:for-each select="./child::*">
					<xsl:call-template name="copyProperty" />
				</xsl:for-each>
			</xsl:if>
		</ogc:Filter>
	</xsl:template>
	<xsl:template match="ogc:And | ogc:Or | ogc:Not">
		<xsl:copy>
			<xsl:apply-templates select="ogc:And" />
			<xsl:apply-templates select="ogc:Or" />
			<xsl:apply-templates select="ogc:Not" />
			<xsl:for-each select="./child::*">
				<xsl:if
					test="local-name(.) != 'And' and local-name(.)!='Or' and local-name(.)!='Not'">
					<xsl:call-template name="copyProperty" />
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template name="copyProperty">
		<xsl:copy>
			<xsl:if test="local-name(.) = 'PropertyIsLike'">
				<xsl:attribute name="wildCard">
					<xsl:value-of select="./@wildCard" />
				</xsl:attribute>
				<xsl:attribute name="singleChar">
					<xsl:value-of select="./@singleChar" />
				</xsl:attribute>
				<xsl:attribute name="escapeChar">
					<xsl:value-of select="./@escape" />
					<xsl:value-of select="./@escapeChar" />
				</xsl:attribute>
			</xsl:if>
			<ogc:PropertyName>
				<xsl:apply-templates select="ogc:PropertyName" />
			</ogc:PropertyName>
			<xsl:for-each select="./child::*">
				<xsl:if test="local-name(.) != 'PropertyName' ">
					<xsl:copy-of select="." />
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="ogc:PropertyName | csw:ElementName">
		<!-- mapping property name value -->
		<xsl:value-of select="mapping:mapPropertyValue( $map, ., $NSP )" />
	</xsl:template>
	
	
</xsl:stylesheet>
