<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:iso19115="http://schemas.opengis.net/iso19115full" 
xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="METAMAINTENANCE">
		<iso19115:metadataMaintenance>
			<smXML:MD_MaintenanceInformation>
				<smXML:maintenanceAndUpdateFrequency>
					<smXML:MD_MaintenanceFrequencyCode codeList="MD_MaintenanceFrequencyCode">
						<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getMaintenanceCode( idinfo/status/update )"/></xsl:attribute>
					</smXML:MD_MaintenanceFrequencyCode>
				</smXML:maintenanceAndUpdateFrequency>
			</smXML:MD_MaintenanceInformation>
		</iso19115:metadataMaintenance>
	</xsl:template>
</xsl:stylesheet>
