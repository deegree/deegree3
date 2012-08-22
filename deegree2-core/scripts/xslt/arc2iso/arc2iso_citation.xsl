<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:iso19115="http://schemas.opengis.net/iso19115full" 
xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
		
	<xsl:template match="citeinfo">
		<smXML:CI_Citation>
		
			<smXML:title>
				<smXML:CharacterString>
					<xsl:value-of select="title"/>
				</smXML:CharacterString>
			</smXML:title>
			
			<smXML:date>
				<smXML:CI_Date>
					<smXML:date>
						<smXML:DateTime>
							<xsl:value-of select="concat(arc2iso:getISODate( pubdate ), 'T00:00:00' )"/>
						</smXML:DateTime>
					</smXML:date>
					<smXML:dateType><smXML:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="publication"/></smXML:dateType>
				</smXML:CI_Date>
			</smXML:date>
		
			<xsl:if test="edition">
				<smXML:edition>
					<smXML:CharacterString>
						<xsl:value-of select="edition"/>
					</smXML:CharacterString>
				</smXML:edition>
			</xsl:if>
		
			<xsl:if test="serinfo">
				<smXML:series>
					<smXML:CI_Series>
						<smXML:name>
							<smXML:CharacterString>
								<xsl:value-of select="serinfo/sername"/>
							</smXML:CharacterString>
						</smXML:name>
						<smXML:issueIdentification>
							<smXML:CharacterString>
								<xsl:value-of select="serinfo/issue"/>
							</smXML:CharacterString>
						</smXML:issueIdentification>
					</smXML:CI_Series>
				</smXML:series>
			</xsl:if>
		
			<xsl:if test="othercit">
				<smXML:otherCitationDetails>
					<smXML:CharacterString>
						<xsl:value-of select="othercit"/>
					</smXML:CharacterString>
				</smXML:otherCitationDetails>		
			</xsl:if>
			
		</smXML:CI_Citation>
	</xsl:template>
	
</xsl:stylesheet>
