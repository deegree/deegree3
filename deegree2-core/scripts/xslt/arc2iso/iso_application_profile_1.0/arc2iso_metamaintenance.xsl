<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:gmd="http://www.isotc211.org/2005/gmd" 
 xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="METAMAINTENANCE">
		<gmd:metadataMaintenance>
			<gmd:MD_MaintenanceInformation>
				<gmd:maintenanceAndUpdateFrequency>
					<gmd:MD_MaintenanceFrequencyCode codeList="MD_MaintenanceFrequencyCode">
						<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getMaintenanceCode( idinfo/status/update )"/></xsl:attribute>
					</gmd:MD_MaintenanceFrequencyCode>
				</gmd:maintenanceAndUpdateFrequency>
			</gmd:MD_MaintenanceInformation>
		</gmd:metadataMaintenance>
	</xsl:template>
</xsl:stylesheet>
