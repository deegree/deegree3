<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:iso19115="http://schemas.opengis.net/iso19115full" 
 xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">

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
		<iso19115:MD_Metadata>
			<xsl:if test="Esri/MetaID">
				<iso19115:fileIdentifier>
					<smXML:CharacterString>
						<xsl:value-of select="Esri/MetaID"/>
					</smXML:CharacterString>
				</iso19115:fileIdentifier>
			</xsl:if>
			<iso19115:language>
				<smXML:CharacterString>
					<xsl:value-of select="metainfo/langmeta"/>
				</smXML:CharacterString>
			</iso19115:language>
			<iso19115:characterSet>
				<smXML:MD_CharacterSetCode codeList="MD_CharacterSetCode">
					<xsl:attribute name="codeListValue">
						<xsl:value-of select="arc2iso:getCharacterSetCode( mdChar/CharSetCd/@value )"></xsl:value-of>
					</xsl:attribute>
				</smXML:MD_CharacterSetCode>
			</iso19115:characterSet>			
			<iso19115:hierarchyLevel>
				<smXML:MD_ScopeCode codeList="MD_ScopeCode" codeListValue="dataset"/>
			</iso19115:hierarchyLevel>
			<iso19115:hierarchyLevelName>
				<smXML:CharacterString>dataset</smXML:CharacterString>
			</iso19115:hierarchyLevelName>
			
			<!-- ESRI + ISO: Mandatory -->
			<iso19115:contact>			
				<xsl:apply-templates select="metainfo/metc/cntinfo"></xsl:apply-templates>
			</iso19115:contact>
			
			<iso19115:dateStamp>
				<smXML:DateTime>
					<xsl:value-of select="concat( arc2iso:getISODate( metainfo/metd ), 'T00:00:00' )"/>
				</smXML:DateTime>
			</iso19115:dateStamp>
			
			<iso19115:metadataStandardName>
				<smXML:CharacterString>ISO 19115 Geographic Information Metadata</smXML:CharacterString>
			</iso19115:metadataStandardName>
			
			<iso19115:metadataStandardVersion>
				<smXML:CharacterString>DIS</smXML:CharacterString>
			</iso19115:metadataStandardVersion>			
			
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
			<xsl:if test="spdoinfo/ptvctinf/esriterm/efeageom">
				<xsl:call-template name="SPATIALREPINFO"/>
			</xsl:if>
			
			<!-- ESRI + ISO:Optional (Test in template)-->
			<xsl:call-template name="REFERENCESYSTEMINFO"/>

			<!-- ESRI + ISO:Optional -->
			<xsl:if test="distinfo/stdorder/digform/digtinfo/formname">
				<xsl:call-template name="DISTRIBUTIONINFO"/>
			</xsl:if>

			<!-- ESRI + ISO:Optional 
			<xsl:if test="metainfo/metextns/onlink">
				<xsl:call-template name="METAEXTENSIONINFO"/>
			</xsl:if>
			-->
			
			
			 
		</iso19115:MD_Metadata>
	</xsl:template>
	
</xsl:stylesheet>
