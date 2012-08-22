<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:wfs="http://www.opengis.net/wfs" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gml="http://www.opengis.net/gml" 
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:app="http://www.deegree.org/app"  
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco" 
	xmlns:gts="http://www.isotc211.org/2005/gts" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:java="java"
	xmlns:minmax="org.deegree.framework.xml.MinMaxExtractor" 
	exclude-result-prefixes="xsl app java minmax deegreewfs wfs" >
	
	<xsl:param name="REQUEST_ID" />
	<xsl:param name="SEARCH_STATUS" />
	<xsl:param name="TIMESTAMP" />
	<xsl:param name="ELEMENT_SET" />
	<xsl:param name="RECORD_SCHEMA" />
	<xsl:param name="RECORDS_MATCHED" />
	<xsl:param name="RECORDS_RETURNED" />
	<xsl:param name="NEXT_RECORD" />
	<xsl:param name="REQUEST_NAME" />	
	
	<xsl:template match="Collection" xmlns:deegreewfs="http://www.deegree.org/wfs">
		<xsl:choose>
			<xsl:when test="$REQUEST_NAME = 'GetRecordById'">
				<csw:GetRecordByIdResponse xmlns:csw="http://www.opengis.net/cat/csw" version="2.0.0">
					<xsl:for-each select="gmd:MD_Metadata">
						<xsl:apply-templates select=".">
							<xsl:with-param name="HLEVEL">
								<xsl:value-of
									select="./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue" />
							</xsl:with-param>
						</xsl:apply-templates>
					</xsl:for-each>
				</csw:GetRecordByIdResponse>
			</xsl:when>
			<xsl:otherwise>
				<csw:GetRecordsResponse xmlns:csw="http://www.opengis.net/cat/csw" version="2.0.0">
					<csw:RequestId>
						<xsl:value-of select="$REQUEST_ID" />
					</csw:RequestId>
					<csw:SearchStatus>
						<xsl:attribute name="status">
							<xsl:value-of select="$SEARCH_STATUS" />
						</xsl:attribute>
						<xsl:attribute name="timestamp">
							<xsl:value-of select="$TIMESTAMP" />
						</xsl:attribute>
					</csw:SearchStatus>
					<csw:SearchResults>
						<xsl:attribute name="requestId">
							<xsl:value-of select="$REQUEST_ID" />
						</xsl:attribute>
						<!--				<xsl:attribute name="elementSet"><xsl:value-of select="$ELEMENT_SET"/></xsl:attribute>-->
						<xsl:attribute name="recordSchema">
							<xsl:value-of select="$RECORD_SCHEMA" />
						</xsl:attribute>
						<xsl:attribute name="numberOfRecordsMatched">
							<xsl:value-of select="$RECORDS_MATCHED" />
						</xsl:attribute>
						<xsl:attribute name="numberOfRecordsReturned">
							<xsl:value-of select="$RECORDS_RETURNED" />
						</xsl:attribute>
						<xsl:attribute name="nextRecord">
							<xsl:value-of select="$NEXT_RECORD" />
						</xsl:attribute>
						<xsl:for-each select="gmd:MD_Metadata">
							<xsl:apply-templates select=".">
								<xsl:with-param name="HLEVEL">
									<xsl:value-of select="./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue" />
								</xsl:with-param>
							</xsl:apply-templates>
						</xsl:for-each>
					</csw:SearchResults>
				</csw:GetRecordsResponse>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- =================================================
		template for MD_Metadata
		====================================================-->
	<xsl:template match="gmd:MD_Metadata">	
		<xsl:param name="HLEVEL" />
		<xsl:if test="$ELEMENT_SET = 'brief'">
			<xsl:call-template name="BRIEF" />
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'summary'">
			<xsl:call-template name="SUMMARY" />
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'full'">
			<xsl:call-template name="FULL" />
		</xsl:if>		
	</xsl:template>
	
	
	<!-- ========================================================  
        result set brief
    ===========================================================  -->
    <xsl:template name="BRIEF">
        <gmd:MD_Metadata>
          <xsl:copy-of select="gmd:fileIdentifier"/>
          <xsl:copy-of select="gmd:hierarchyLevel"/>
          <xsl:copy-of select="gmd:contact"/>
          <xsl:copy-of select="gmd:dateStamp"/>
          <xsl:copy-of select="gmd:identificationInfo"/>
        </gmd:MD_Metadata>
    </xsl:template>
    
    <!-- ========================================================   
        result set summary
    ===========================================================  -->
    <xsl:template name="SUMMARY">
        <gmd:MD_Metadata>
          <xsl:copy-of select="gmd:fileIdentifier"/>
          <xsl:copy-of select="gmd:language"/>
          <xsl:copy-of select="gmd:characterSet"/>
          <xsl:copy-of select="gmd:parentidentifier"/>
          <xsl:copy-of select="gmd:hierarchyLevelName"/>
          <xsl:copy-of select="gmd:hierarchyLevel"/>
          <xsl:copy-of select="gmd:contact"/>
          <xsl:copy-of select="gmd:dateStamp"/>
          <xsl:copy-of select="gmd:metadataStandardName"/>
          <xsl:copy-of select="gmd:metadataStandardVersion"/>
          <xsl:copy-of select="gmd:referenceSystemInfo"/>
          <xsl:copy-of select="gmd:identificationInfo"/>
          <xsl:copy-of select="gmd:distributionInfo"/>
          <xsl:copy-of select="gmd:dataQualityInfo"/>
          <xsl:copy-of select="gmd:metadataConstraints"/>
        </gmd:MD_Metadata>  
    </xsl:template> 

    <!-- ========================================================   
        result set full 
    ===========================================================  -->
    <xsl:template name="FULL">
        <xsl:copy-of select="."></xsl:copy-of>
    </xsl:template>
	
</xsl:stylesheet>
