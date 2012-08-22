<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO"
 xmlns:gmd="http://www.isotc211.org/2005/gmd" 
 xmlns:gco="http://www.isotc211.org/2005/gco" 
 xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xsl java arc2iso">

	<xsl:import href="arc2iso_dataidentification.xsl"></xsl:import>
	<xsl:import href="arc2iso_pointofcontact.xsl"></xsl:import>
	<xsl:import href="arc2iso_dataqualityinfo.xsl"></xsl:import>
	<xsl:import href="arc2iso_spatialrepinfo.xsl"></xsl:import>
	<xsl:import href="arc2iso_referencesysteminfo.xsl"></xsl:import>
	<xsl:import href="arc2iso_distributioninfo.xsl"></xsl:import>
	<xsl:import href="arc2iso_metadataconstraint.xsl"></xsl:import>
	<xsl:import href="arc2iso_metamaintenance.xsl"></xsl:import>
	<xsl:import href="arc2iso_citation.xsl"></xsl:import>
	
	<xsl:template match="metadata">
		<gmd:MD_Metadata  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.isotc211.org/2005/gmd D:\java\projekte\iso19115\CSW202_APISO_100_FINAL\csw202_apiso100\iso\19139\20060504\gmd\gmd.xsd">
			<xsl:if test="Esri/MetaID">
				<gmd:fileIdentifier>
					<gco:CharacterString>
						<xsl:value-of select="Esri/MetaID"/>
					</gco:CharacterString>
				</gmd:fileIdentifier>
			</xsl:if>
			<gmd:language>
                <gmd:LanguageCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#LanguageCode" codeListValue="metainfo/langmeta"/>
            </gmd:language>
			<gmd:characterSet>
				<gmd:MD_CharacterSetCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#LanguageCode">
					<xsl:attribute name="codeListValue">
						<xsl:value-of select="arc2iso:getCharacterSetCode( mdChar/CharSetCd/@value )"></xsl:value-of>
					</xsl:attribute>
				</gmd:MD_CharacterSetCode>
			</gmd:characterSet>			
			<gmd:hierarchyLevel>
				<gmd:MD_ScopeCode codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ScopeCode" codeListValue="dataset"/>
			</gmd:hierarchyLevel>
			<gmd:hierarchyLevelName>
				<gco:CharacterString>dataset</gco:CharacterString>
			</gmd:hierarchyLevelName>
			
			<!-- ESRI + ISO: Mandatory -->
			<gmd:contact>			
				<xsl:apply-templates select="metainfo/metc/cntinfo"></xsl:apply-templates>
			</gmd:contact>
			
			<gmd:dateStamp>
				<gco:DateTime>
					<xsl:value-of select="concat( arc2iso:getISODate( metainfo/metd ), 'T00:00:00' )"/>
				</gco:DateTime>
			</gmd:dateStamp>
			
			<gmd:metadataStandardName>
                <gco:CharacterString xmlns:gco="http://www.isotc211.org/2005/gco">ISO 19115 Geographic Information - Metadata</gco:CharacterString>
		    </gmd:metadataStandardName>
		    <gmd:metadataStandardVersion>
		        <gco:CharacterString xmlns:gco="http://www.isotc211.org/2005/gco">2003/Cor.1:2006</gco:CharacterString>
		    </gmd:metadataStandardVersion>		
		    
		    <!-- ESRI + ISO:Optional -->
			<xsl:if test="spdoinfo/ptvctinf/esriterm/efeageom">
				<xsl:call-template name="SPATIALREPINFO"/>
			</xsl:if>
			
			<!-- ESRI + ISO:Optional (Test in template)-->
			<xsl:call-template name="REFERENCESYSTEMINFO"/>
			
			<!-- ESRI + ISO: Mandatory -->			
			<xsl:call-template name="DATAIDENTIFICATION"/>

			
			<!-- ESRI + ISO:Optional -->
			<xsl:if test="metainfo/metac | metainfo/metuc">
				<xsl:call-template name="METACONSTRAINT"/>
			</xsl:if>			
			
			<!-- ESRI + ISO: Optional -->
			<xsl:if test="dataqual">
				<xsl:call-template name="DATAQUALITYINFO"/>
			</xsl:if>
			
			<!-- ESRI: Mandatory; ISO:Optional -->
			<xsl:if test="boolean( idinfo/status/update )">
				<xsl:call-template name="METAMAINTENANCE"/>
			</xsl:if>
		
			<!-- ESRI + ISO:Optional -->
			<xsl:if test="distinfo/stdorder/digform/digtinfo/formname">
				<xsl:call-template name="DISTRIBUTIONINFO"/>
			</xsl:if>

			<!-- ESRI + ISO:Optional 
			<xsl:if test="metainfo/metextns/onlink">
				<xsl:call-template name="METAEXTENSIONINFO"/>
			</xsl:if>
			-->
			 
		</gmd:MD_Metadata>
	</xsl:template>
	
</xsl:stylesheet>
