<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gco="http://www.isotc211.org/2005/gco" 
xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:srv="http://www.isotc211.org/2005/srv">

    <xsl:param name="TITLE">Title</xsl:param>
    <xsl:param name="ABSTRACT">Abstract</xsl:param>
    <xsl:param name="TOPICCATEGORY">Topic category</xsl:param>
    <xsl:param name="HIERARCHYLEVEL">Hierarchy level</xsl:param>    
    <xsl:param name="GEOGRDESC">Geogr. Description</xsl:param>    
    <xsl:param name="CREATIONDATE">Creation Date</xsl:param>    
    <xsl:param name="PUBLICATIONDATE">Publication Date</xsl:param>    
    <xsl:param name="REVISIONDATE">Revision Date</xsl:param>
    <xsl:param name="DISTONLINE">URL der Datenquelle</xsl:param>
    <xsl:param name="CONTACT">Contact</xsl:param>    
    <xsl:param name="NAME">Name</xsl:param>    
    <xsl:param name="ORGANISATION">Organisation</xsl:param>    
    <xsl:param name="ADDRESS">Address</xsl:param>    
    <xsl:param name="VOICE">Voice</xsl:param>    
    <xsl:param name="FAX">Facsimile</xsl:param>
    <xsl:param name="EMAIL">Email</xsl:param>
    <xsl:param name="URL">URL</xsl:param>
    <xsl:param name="CSW">-</xsl:param>      
    
    
	<xsl:output method="html"></xsl:output>

	<xsl:template match="csw:GetRecordsResponse">		
		<xsl:apply-templates select="csw:SearchResults/gmd:MD_Metadata"></xsl:apply-templates>		
	</xsl:template>
	 
	<xsl:template match="gmd:MD_Metadata">
		<xsl:variable name="ID">
			<xsl:value-of select="./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:identifier[1]/gmd:MD_Identifier/gmd:code/gco:CharacterString"/>
		</xsl:variable>
		<xsl:variable name="BBOX">
			<xsl:value-of select="concat( ./gmd:identificationInfo/*/*/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal, ' ',
							                          ./gmd:identificationInfo/*/*/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal,' ',
							                          ./gmd:identificationInfo/*/*/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal,' ',
							                          ./gmd:identificationInfo/*/*/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal)"/>
		</xsl:variable>

		<table width="650" cellpadding="2">
			<tr>
			    <td width="22"></td>
				<td valign="top" width="150"><xsl:value-of select="$TITLE"/></td>
				<td width="10"></td>
				<td valign="top">
					<xsl:value-of select="./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
				</td>
			</tr>			
			<tr>
			    <td width="22"></td>
				<td valign="top"><xsl:value-of select="$ABSTRACT"/></td>
				<td width="10"></td>
				<td valign="top">
					<xsl:value-of select="./gmd:identificationInfo/*/gmd:abstract/gco:CharacterString"/>
				</td>
			</tr>		
			<tr>
			     <td width="22"></td>
				<td valign="top"><xsl:value-of select="$TOPICCATEGORY"/></td>
				<td width="10"></td>
				<td valign="top">
					<xsl:value-of select="./gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode"/>
				</td>
			</tr>		
			<tr>
			     <td width="22"></td>
				<td valign="top"><xsl:value-of select="$HIERARCHYLEVEL"/></td>
				<td width="10"></td>
				<td valign="top">
					<xsl:value-of select="./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
				</td>
			</tr>
			<tr>
			     <td width="22"></td>
				<td valign="top"><xsl:value-of select="$GEOGRDESC"/></td>
				<td width="10"></td>
				<td valign="top">
					<xsl:value-of select="./gmd:identificationInfo/*/*/gmd:EX_Extent/gmd:description/gco:CharacterString"/>
				</td>
			</tr>		
			<tr>
			     <td width="22"></td>
				<td valign="top"><xsl:value-of select="$CREATIONDATE"/></td>
				<td width="10"></td>
				<td valign="top">
					<xsl:value-of select="./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date[../gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'creation' ]/gco:DateTime"/>
				</td>
			</tr>		
			<tr>
			     <td width="22"></td>
				<td valign="top"><xsl:value-of select="$PUBLICATIONDATE"/></td>
				<td width="10"></td>
				<td valign="top">
					<xsl:value-of select="./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date[../gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication' ]/gco:DateTime"/>
				</td>
			</tr>
			<tr>
			     <td width="22"></td>
				<td valign="top"><xsl:value-of select="$REVISIONDATE"/></td>
				<td width="10"></td>
				<td valign="top">
					<xsl:value-of select="./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date[../gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'revision' ]/gco:DateTime"/>
				</td>
			</tr>
            <tr>
                 <td width="22"></td>
                <td valign="top"><xsl:value-of select="$DISTONLINE"/></td>
                <td width="10"></td>
                <td valign="top">
                    <a target="_blank" style="font-style:italic">
                        <xsl:attribute name="href">
                            <xsl:value-of select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                        </xsl:attribute>
                        <xsl:value-of select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                    </a>
                </td>
            </tr>
			<tr>
			     <td width="22"></td>
				<td valign="top"><xsl:value-of select="$CONTACT"/></td>
				<td width="10"></td>
				<td valign="top">
					<table>
						<tr>
							<td valign="top" colspan="3"></td>							
						</tr>		
						<tr>
							<td valign="top"><xsl:value-of select="$NAME"/></td>
							<td width="10"></td>
							<td valign="top">
								<xsl:value-of select="./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString"/>
							</td>
						</tr>						
						<tr>
							<td valign="top"><xsl:value-of select="$ORGANISATION"/></td>
							<td width="10"></td>
							<td valign="top">
								<xsl:value-of select="./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
							</td>
						</tr>						
						<tr>
							<td valign="top"><xsl:value-of select="$ADDRESS"/></td>
							<td width="10"></td>
							<td valign="top">
								<xsl:value-of select="./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString"/>
								<br></br>
								<xsl:value-of select="concat( ./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString,
								                               ' ', ./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString )"/>
								<br></br>
								<xsl:value-of select="./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString"/>
							</td>
						</tr>	
						<tr>
							<td valign="top"><xsl:value-of select="$VOICE"/></td>
							<td width="10"></td>
							<td valign="top">
								<xsl:value-of select="./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString"/>
							</td>
						</tr>		
						<tr>
							<td valign="top"><xsl:value-of select="$FAX"/></td>
							<td width="10"></td>
							<td valign="top">
								<xsl:value-of select="./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString"/>
							</td>
						</tr>		
						<tr>
							<td valign="top"><xsl:value-of select="$EMAIL"/></td>
							<td width="10"></td>
							<td valign="top">
								<a style="font-style:italic"> 
									<xsl:attribute name="href" >								
										<xsl:value-of select="concat( 'mailto:',./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString )"/>
									</xsl:attribute>
									<xsl:value-of select="./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
								</a>
							</td>
						</tr>		
						<tr>
							<td valign="top"><xsl:value-of select="$URL"/></td>
							<td width="10"></td>
							<td valign="top">
								<a target="_blank" style="font-style:italic">
									<xsl:attribute name="href">
										<xsl:value-of select="./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
									</xsl:attribute>
									<xsl:value-of select="./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
								</a>
							</td>
						</tr>		
					</table>
				</td>
			</tr>	
			<tr>
				<td colspan="3" height="20"></td>
			</tr>
		</table>	
	</xsl:template>
	
</xsl:stylesheet>