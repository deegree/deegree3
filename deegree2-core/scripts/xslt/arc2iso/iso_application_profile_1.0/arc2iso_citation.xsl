<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:gmd="http://www.isotc211.org/2005/gmd" 
 xmlns:gco="http://www.isotc211.org/2005/gco"
 xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
		
	<xsl:template match="citeinfo">
		<gmd:CI_Citation>
		
			<gmd:title>
				<gco:CharacterString>
					<xsl:value-of select="title"/>
				</gco:CharacterString>
			</gmd:title>
			
			<gmd:date>
				<gmd:CI_Date>
					<gmd:date>
						<gco:DateTime>
							<xsl:value-of select="concat(arc2iso:getISODate( pubdate ), 'T00:00:00' )"/>
						</gco:DateTime>
					</gmd:date>
					<gmd:dateType><gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="publication"/></gmd:dateType>
				</gmd:CI_Date>
			</gmd:date>
		
			<xsl:if test="edition">
				<gmd:edition>
					<gco:CharacterString>
						<xsl:value-of select="edition"/>
					</gco:CharacterString>
				</gmd:edition>
			</xsl:if>
		
			<xsl:if test="serinfo">
				<gmd:series>
					<gmd:CI_Series>
						<gmd:name>
							<gco:CharacterString>
								<xsl:value-of select="serinfo/sername"/>
							</gco:CharacterString>
						</gmd:name>
						<gmd:issueIdentification>
							<gco:CharacterString>
								<xsl:value-of select="serinfo/issue"/>
							</gco:CharacterString>
						</gmd:issueIdentification>
					</gmd:CI_Series>
				</gmd:series>
			</xsl:if>
		
			<xsl:if test="othercit">
				<gmd:otherCitationDetails>
					<gco:CharacterString>
						<xsl:value-of select="othercit"/>
					</gco:CharacterString>
				</gmd:otherCitationDetails>		
			</xsl:if>
			
		</gmd:CI_Citation>
	</xsl:template>
	
</xsl:stylesheet>
