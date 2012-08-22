<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0"
xmlns:wfs="http://www.opengis.net/wfs"
 xmlns:xAL="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0"
 exclude-result-prefixes="wfs fo xsl">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

	<xsl:include href="gml3TOcitygml_building.xsl"/>
	<xsl:include href="gml3TOcitygml_trafficarea.xsl"/>	
	<xsl:include href="gml3TOcitygml_road.xsl"/>
	<xsl:include href="gml3TOcitygml_railway.xsl"/>	
	<xsl:include href="gml3TOcitygml_cityfurniture.xsl"/>		
	<xsl:include href="gml3TOcitygml_outterBuildingInstallation.xsl"/>
	<xsl:include href="gml3TOcitygml_boundedby.xsl"/>
	
	<xsl:template match="wfs:FeatureCollection">
		<wfs:FeatureCollection>
			<xsl:choose>
				<xsl:when test="boolean( ./@numberOfFeatures )">
					<xsl:attribute name="numberOfFeatures"><xsl:value-of select="./@numberOfFeatures"></xsl:value-of></xsl:attribute>
				</xsl:when>				
				<xsl:otherwise>
					<xsl:attribute name="numberOfFeatures"><xsl:value-of select="count( gml:featureMember )"></xsl:value-of></xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="boolean( ./@timeStamp )">
				<xsl:attribute name="timeStamp"><xsl:value-of select="./@timeStamp"></xsl:value-of></xsl:attribute>
			</xsl:if>
			<xsl:if test="boolean( ./@lockId )">
				<xsl:attribute name="lockId"><xsl:value-of select="./@lockId"></xsl:value-of></xsl:attribute>
			</xsl:if>
				
			<xsl:copy-of select="gml:boundedBy"/>
			<xsl:for-each select="gml:featureMember">
				<gml:featureMember>
					<xsl:if test="citygml:Feature/citygml:type = 'Building' " >
						<xsl:call-template name="BUILDING"></xsl:call-template>
					</xsl:if>
					<xsl:if test="boolean( citygml:Feature/citygml:road )" >
						<xsl:call-template name="ROAD"></xsl:call-template>
					</xsl:if>
					<xsl:if test="boolean( citygml:Feature/citygml:railway )" >
						<xsl:call-template name="RAILWAY"></xsl:call-template>
					</xsl:if>
					<xsl:if test="boolean( citygml:Feature/citygml:cityFurniture )" >
						<xsl:call-template name="CITYFURNITURE"></xsl:call-template>
					</xsl:if>
				</gml:featureMember>
			</xsl:for-each>
		</wfs:FeatureCollection>
	</xsl:template>
	
	<xsl:template match="citygml:Feature">
		<xsl:if test="boolean( citygml:featureName )">
			<gml:name>
				<xsl:value-of select="citygml:featureName"></xsl:value-of>
			</gml:name>
		</xsl:if>
		<xsl:copy-of select="gml:boundedBy"/>
		<xsl:if test="boolean( citygml:creation )">
			<citygml:creationDate>
				<xsl:value-of select="substring-before( citygml:creation, 'T' )"/>
			</citygml:creationDate>
		</xsl:if>
		<xsl:if test="boolean( citygml:deletion )">
			<citygml:terminationDate>
				<xsl:value-of select="substring-before( citygml:deletion, 'T' )"/>
			</citygml:terminationDate>
		</xsl:if>
		
		<xsl:apply-templates select="citygml:externalReference/citygml:Externalref"/>
		<xsl:apply-templates select="citygml:dateAttribute/citygml:Date_Attribute"/>
		<xsl:apply-templates select="citygml:doubleAttribute/citygml:Float_Attribute"/>
		<xsl:apply-templates select="citygml:intAttribute/citygml:Int_Attribute"/>
		<xsl:apply-templates select="citygml:stringAttribute/citygml:Text_Attribute"/>
		<xsl:apply-templates select="citygml:uriAttribute/citygml:URI_Attribute"/>
		
		<xsl:for-each select="citygml:function">
			<citygml:function>
				<xsl:value-of select="."></xsl:value-of>
			</citygml:function>
		</xsl:for-each>			
		<xsl:for-each select="citygml:usage">
			<citygml:usage>
				<xsl:value-of select="."></xsl:value-of>
			</citygml:usage>
		</xsl:for-each>
		
		<xsl:for-each select="citygml:linkedFeature/citygml:LINK_FEAT_FEAT/citygml:feature">
			<xsl:if test="../citygml:role = 'generalizesTo' and boolean( citygml:Feature/citygml:building ) ">
				<citygml:generalizesTo>
					<xsl:call-template name="BUILDING"></xsl:call-template>
				</citygml:generalizesTo>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="citygml:Address">
		<citygml:Address>
			<citygml:xalAddress>
				<xAL:AddressDetails>
					<xAL:Country>
						<xAL:CountryName>	
							<xsl:value-of select="citygml:country"></xsl:value-of>
						</xAL:CountryName>
						<xsl:choose>
							<xsl:when test="boolean( citygml:citydistrict )">
								<xAL:AdministrativeArea>
									<xAL:AdministrativeAreaName>
										<xsl:value-of select="citygml:citydistrict"></xsl:value-of>
									</xAL:AdministrativeAreaName>
									<xsl:call-template name="LOCALITY"></xsl:call-template>
								</xAL:AdministrativeArea>
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="LOCALITY"></xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
					</xAL:Country>
				</xAL:AddressDetails>
			</citygml:xalAddress>
		</citygml:Address>
	</xsl:template>
	
	<xsl:template name="LOCALITY">
		<xAL:Locality Type="Town">
			<xAL:LocalityName>
				<xsl:value-of select="citygml:city"></xsl:value-of>
			</xAL:LocalityName>
			<xAL:Thoroughfare Type="Street">
				<xAL:ThoroughfareNumber>
					<xsl:value-of select="citygml:code"></xsl:value-of>
				</xAL:ThoroughfareNumber>
				<xAL:ThoroughfareName>
					<xsl:value-of select="citygml:street"></xsl:value-of>
				</xAL:ThoroughfareName>
			</xAL:Thoroughfare>
			<xAL:PostalCode>
				<xAL:PostalCodeNumber>
					<xsl:value-of select="citygml:postalcode"></xsl:value-of>
				</xAL:PostalCodeNumber>
			</xAL:PostalCode>
		</xAL:Locality>
	</xsl:template>
		
	<xsl:template match="citygml:Externalref">
		<citygml:externalReference>
			<xsl:if test="citygml:informationsystem">
				<citygml:informationSystem>
					<xsl:value-of select="citygml:informationsystem"></xsl:value-of>
				</citygml:informationSystem>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="boolean( citygml:name )">
					<citygml:externalObject>
						<citygml:name>
							<xsl:value-of select="citygml:name"></xsl:value-of>
						</citygml:name>
					</citygml:externalObject>
				</xsl:when>
				<xsl:otherwise>
					<citygml:externalObject>
						<citygml:uri>
							<xsl:value-of select="citygml:uri"></xsl:value-of>
						</citygml:uri>
					</citygml:externalObject>
				</xsl:otherwise>
			</xsl:choose>
		</citygml:externalReference>
	</xsl:template>
	
	<xsl:template match="citygml:Date_Attribute">
		<citygml:dateAttribute>
			<xsl:attribute name="name"><xsl:value-of select="citygml:name"></xsl:value-of></xsl:attribute>
			<citygml:value>
				<xsl:value-of select="substring-before( citygml:value, 'T' )"></xsl:value-of>
			</citygml:value>
		</citygml:dateAttribute>
	</xsl:template>
	
	<xsl:template match="citygml:Float_Attribute">
		<citygml:doubleAttribute>
			<xsl:attribute name="name"><xsl:value-of select="citygml:name"></xsl:value-of></xsl:attribute>
			<citygml:value>
				<xsl:value-of select="citygml:value"></xsl:value-of>
			</citygml:value>
		</citygml:doubleAttribute>
	</xsl:template>
	
	<xsl:template match="citygml:Int_Attribute">
		<citygml:intAttribute>
			<xsl:attribute name="name"><xsl:value-of select="citygml:name"></xsl:value-of></xsl:attribute>
			<citygml:value>
				<xsl:value-of select="citygml:value"></xsl:value-of>
			</citygml:value>
		</citygml:intAttribute>
	</xsl:template>
	
	<xsl:template match="citygml:Text_Attribute">
		<citygml:stringAttribute>
			<xsl:attribute name="name"><xsl:value-of select="citygml:name"></xsl:value-of></xsl:attribute>
			<citygml:value>
				<xsl:value-of select="citygml:value"></xsl:value-of>
			</citygml:value>
		</citygml:stringAttribute>
	</xsl:template>
	
	<xsl:template match="citygml:URI_Attribute">
		<citygml:uriAttribute>
			<xsl:attribute name="name"><xsl:value-of select="citygml:name"></xsl:value-of></xsl:attribute>
			<citygml:value>
				<xsl:value-of select="citygml:value"></xsl:value-of>
			</citygml:value>
		</citygml:uriAttribute>
	</xsl:template>
	
	<xsl:template match="citygml:Polygon">
		<xsl:choose>
			<xsl:when test="boolean( citygml:material ) or boolean( citygml:texture )">
				<citygml:TexturedSurface>
					<xsl:choose>
						<xsl:when test="boolean(citygml:orientation)">
							<xsl:attribute name="orientation">
								<xsl:value-of select="citygml:orientation"></xsl:value-of>
							</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="orientation">+</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
					<gml:baseSurface>
						<xsl:copy-of select="citygml:geom/*"></xsl:copy-of>
					</gml:baseSurface>
					<xsl:apply-templates select="citygml:material/citygml:Material"></xsl:apply-templates>
					<xsl:apply-templates select="citygml:texture/citygml:Texture"></xsl:apply-templates>
				</citygml:TexturedSurface>	
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="citygml:geom/*"></xsl:copy-of>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="citygml:Material">
		<citygml:appearance>
			<citygml:Material>
				<xsl:if test="boolean(citygml:shininess)">
					<citygml:shininess>
						<xsl:value-of select="citygml:shininess"></xsl:value-of>
					</citygml:shininess>
				</xsl:if>
				<xsl:if test="boolean(citygml:transparency)">
					<citygml:transparency>
						<xsl:value-of select="citygml:transparency"></xsl:value-of>
					</citygml:transparency>
				</xsl:if>
				<xsl:if test="boolean(citygml:ambientintensity)">
					<citygml:ambientIntensity>
						<xsl:value-of select="citygml:ambientintensity"></xsl:value-of>
					</citygml:ambientIntensity>
				</xsl:if>
				<xsl:if test="boolean(citygml:specularcolor)">
					<citygml:specularColor>
						<xsl:value-of select="citygml:specularcolor"></xsl:value-of>
					</citygml:specularColor>
				</xsl:if>
				<xsl:if test="boolean(citygml:diffusecolor)">
					<citygml:diffuseColor>
						<xsl:value-of select="citygml:diffusecolor"></xsl:value-of>
					</citygml:diffuseColor>
				</xsl:if>
				<xsl:if test="boolean(citygml:emissivecolor)">
					<citygml:emissiveColor>
						<xsl:value-of select="citygml:emissivecolor"></xsl:value-of>
					</citygml:emissiveColor>
				</xsl:if>
			</citygml:Material>
		</citygml:appearance>
	</xsl:template>
	
	<xsl:template match="citygml:Texture">
		<citygml:appearance>
			<citygml:SimpleTexture>
				<citygml:textureMap>
					<xsl:value-of select="citygml:texturemap"></xsl:value-of>
				</citygml:textureMap>
				<citygml:textureCoordinates>
					<xsl:value-of select="citygml:texturecoordinates"></xsl:value-of>
				</citygml:textureCoordinates>
				<citygml:textureType>
					<xsl:value-of select="citygml:texturetype"></xsl:value-of>
				</citygml:textureType>
				<citygml:repeat>
					<xsl:value-of select="citygml:repeat"></xsl:value-of>
				</citygml:repeat>
			</citygml:SimpleTexture>
		</citygml:appearance>
	</xsl:template>
	
	<xsl:template match="citygml:Curve">
		<xsl:copy-of select="citygml:geom/gml:Curve"></xsl:copy-of>
	</xsl:template>
	
</xsl:stylesheet>
