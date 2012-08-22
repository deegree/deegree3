<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:app="http://www.deegree.org/app" 
xmlns:gml="http://www.opengis.net/gml" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" 
xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xlink="http://www.w3.org/1999/xlink"
xmlns:java="java"
xmlns:toString="org.deegree.framework.xml.XMLTools">
	<xsl:template name="SERVICEMETADATA">
		<app:CQP_Main>
			<xsl:variable name="cqpKeywords" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword"/>
			<xsl:if test="boolean( $cqpKeywords != '' )">
				<app:subject>
					<xsl:for-each select="$cqpKeywords">
						<xsl:value-of select="concat( '|', gco:CharacterString, '|' )"/>
					</xsl:for-each>
				</app:subject>
			</xsl:if>
			<app:title>
				<xsl:value-of select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
			</app:title>
			<app:abstract>
				<xsl:value-of select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract/gco:CharacterString"/>
			</app:abstract>			
			<app:identifier>
				<xsl:value-of select="gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString"/>
			</app:identifier>
			<app:modified>
				<xsl:value-of select="gmd:MD_Metadata/gmd:dateStamp/gco:DateTime"/>
				<xsl:value-of select="gmd:MD_Metadata/gmd:dateStamp/gco:Date"/>
			</app:modified>
			<app:type>
				<xsl:value-of select="gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
			</app:type>
			<xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:date">
				<xsl:if test="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'revision'">
					<app:revisionDate>
						<xsl:value-of select="gmd:CI_Date/gmd:date/gco:Date"/>
						<xsl:value-of select="gmd:CI_Date/gmd:date/gco:DateTime"/>
					</app:revisionDate>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:date">
				<xsl:if test="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'creation'">
					<app:creationDate>
						<xsl:value-of select="gmd:CI_Date/gmd:date/gco:Date"/>
						<xsl:value-of select="gmd:CI_Date/gmd:date/gco:DateTime"/>
					</app:creationDate>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:date">
				<xsl:if test="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication'">
					<app:publicationDate>
						<xsl:value-of select="gmd:CI_Date/gmd:date/gco:Date"/>
						<xsl:value-of select="gmd:CI_Date/gmd:date/gco:DateTime"/>
					</app:publicationDate>
				</xsl:if>
			</xsl:for-each>
			<xsl:variable name="qcpAlternateTitle" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle"/>
			<xsl:if test="boolean( $qcpAlternateTitle != '' )">
				<app:alternateTitle>
					<xsl:for-each select="$qcpAlternateTitle">
						<xsl:value-of select="concat( '|', gco:CharacterString, '|' )"/>
					</xsl:for-each>
				</app:alternateTitle>
			</xsl:if>
			<xsl:variable name="qcpResourceId" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code"/>
			<xsl:if test="boolean( $qcpResourceId != '' )">
				<app:resourceIdentifier>
					<xsl:for-each select="$qcpResourceId">
						<xsl:value-of select="concat( '|', gco:CharacterString, '|' )"/>
					</xsl:for-each>
				</app:resourceIdentifier>
			</xsl:if>
			<xsl:variable name="cqpDescCode" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code"/>
			<xsl:if test="boolean( $cqpDescCode != '' )">
				<app:geographicDescripionCode>
					<xsl:for-each select="$cqpDescCode">
						<xsl:value-of select="concat( '|', gco:CharacterString, '|' )"/>
					</xsl:for-each>
				</app:geographicDescripionCode>
			</xsl:if>
			<app:serviceType>
				<xsl:value-of select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType"/>
			</app:serviceType>
			<xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
				<xsl:variable name="minx" select="gmd:westBoundLongitude/gco:Decimal"/>
				<xsl:variable name="maxx" select="gmd:eastBoundLongitude/gco:Decimal"/>
				<xsl:variable name="miny" select="gmd:southBoundLatitude/gco:Decimal"/>
				<xsl:variable name="maxy" select="gmd:northBoundLatitude/gco:Decimal"/>
				<app:bbox>
					<app:CQP_BBOX>
						<app:geom>
							<gml:Polygon srsName="EPSG:4326">
								<gml:outerBoundaryIs>
									<gml:LinearRing>
										<gml:coordinates cs="," decimal="." ts=" ">
											<xsl:value-of select="concat( $minx, ',', $miny, ' ', $minx, ',', $maxy, ' ', $maxx, ',', $maxy, ' ', $maxx, ',', $miny, ' ',$minx, ',', $miny)"/>
										</gml:coordinates>
									</gml:LinearRing>
								</gml:outerBoundaryIs>
							</gml:Polygon>
						</app:geom>
					</app:CQP_BBOX>
				</app:bbox>
			</xsl:for-each>
			<xsl:variable name="cqpParentID" select="gmd:MD_Metadata/gmd:parentIdentifier"/>
			<xsl:if test="boolean( $cqpParentID != '' )">
				<app:parentIdentifier>
					<xsl:value-of select="$cqpParentID"/>
				</app:parentIdentifier>
			</xsl:if>
			<xsl:if test="boolean( gmd:MD_Metadata/gmd:language/gco:CharacterString != '' )">
				<app:language>
					<xsl:value-of select="gmd:MD_Metadata/gmd:language/gco:CharacterString"/>
				</app:language>
			</xsl:if>
			<xsl:if test="boolean( gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue != '' )">
				<app:language>
					<xsl:value-of select="gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>
				</app:language>
			</xsl:if>
			<xsl:variable name="cqpLineage" select="gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString"/>
			<xsl:if test="boolean( $cqpLineage != '' )">
				<app:lineage>
					<xsl:value-of select="$cqpLineage"/>
				</app:lineage>
			</xsl:if>
			<!-- conditionApplyingToAccessAndUse  -->
			<xsl:variable name="cqpLimit" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString"/>
			<xsl:variable name="cqpLimitLegal" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gco:CharacterString"/>
			<xsl:variable name="cqpLimitSecurity" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:useLimitation/gco:CharacterString"/>
			<xsl:if test="boolean( $cqpLimit != '' ) or boolean( $cqpLimitLegal != '' ) or boolean( $cqpLimitSecurity != '' )">
				<app:condAppToAccAndUse>
					<xsl:for-each select="$cqpLimit">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
					<xsl:for-each select="$cqpLimitLegal">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
					<xsl:for-each select="$cqpLimitSecurity">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
				</app:condAppToAccAndUse>
			</xsl:if>
			<xsl:variable name="cqpAcessConstraints" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue"/>
			<xsl:if test="boolean( $cqpAcessConstraints != '' )">
				<app:accessConstraints>
					<xsl:for-each select="$cqpAcessConstraints">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
				</app:accessConstraints>
			</xsl:if>
			<xsl:variable name="cqpOtherConstraints" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString"/>
			<xsl:if test="boolean( $cqpOtherConstraints != '' )">
				<app:otherConstraints>
					<xsl:for-each select="$cqpOtherConstraints">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
				</app:otherConstraints>
			</xsl:if>
			<xsl:variable name="cqpClassification" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:classification/gmd:MD_ClassificationCode/@codeListValue"/>
			<xsl:if test="boolean( $cqpClassification != '' )">
				<app:classification>
					<xsl:for-each select="$cqpClassification">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
				</app:classification>
			</xsl:if>
			<xsl:variable name="cqpCouplingType" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:couplingType/srv:SV_CouplingType/@codeListValue"/>
			<xsl:if test="boolean( $cqpCouplingType != '' )">
				<app:couplingType>
					<xsl:for-each select="$cqpCouplingType">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
				</app:couplingType>
			</xsl:if>
			<xsl:variable name="cqpOperation" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:operationName/gco:CharacterString"/>
			<xsl:if test="boolean( cqpOperation != '' )">
				<app:operation>
					<xsl:for-each select="cqpOperation">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
				</app:operation>
			</xsl:if>
			<xsl:variable name="cqpOperatesOn" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn/@uuidref"/>
			<xsl:if test="boolean( $cqpOperatesOn != '' )">
				<app:operatesOn>
					<xsl:for-each select="$cqpOperatesOn">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
				</app:operatesOn>
			</xsl:if>
			<xsl:variable name="cqpOperatesOnIdentifier" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:coupledResource/srv:SV_CoupledResource/srv:identifier/gco:CharacterString"/>
			<xsl:if test="boolean( $cqpOperatesOnIdentifier != '' )">
				<app:operatesOnIdentifier>
					<xsl:for-each select="$cqpOperatesOnIdentifier">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
				</app:operatesOnIdentifier>
			</xsl:if>
			<xsl:variable name="cqpOperatesOnName" select="gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:coupledResource/srv:SV_CoupledResource/srv:operationName/gco:CharacterString"/>
			<xsl:if test="boolean( $cqpOperatesOnName != '' )">
				<app:operatesOnName>
					<xsl:for-each select="$cqpOperatesOnName">
						<xsl:value-of select="concat( '|', ., '|' )"/>
					</xsl:for-each>
				</app:operatesOnName>
			</xsl:if>
			<xsl:variable name="cqpDomainConsistency" select="gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult"/>
			<xsl:for-each select="$cqpDomainConsistency">
				<app:domainConsistency>
					<app:CQP_DomainConsistency>
						<xsl:variable name="specificationTitle" select="gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
						<xsl:if test="boolean( $specificationTitle != '' )">
							<app:specificationTitle>
								<xsl:value-of select="$specificationTitle"/>
							</app:specificationTitle>
						</xsl:if>
						<xsl:variable name="degree" select="gmd:pass/gco:Boolean"/>
						<xsl:if test="boolean( $degree != '' )">
							<app:degree>
								<xsl:value-of select="$degree"/>
							</app:degree>
						</xsl:if>
						<xsl:for-each select="gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date">
							<app:specificationDate>
								<app:CQP_SpecificationDate>
									<app:dateStamp>
										<xsl:value-of select="gmd:date/gco:Date"/>
										<xsl:value-of select="gmd:date/gco:DateTime"/>
									</app:dateStamp>
									<app:datetype>
										<xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/>
									</app:datetype>
								</app:CQP_SpecificationDate>
							</app:specificationDate>
						</xsl:for-each>
					</app:CQP_DomainConsistency>
				</app:domainConsistency>
			</xsl:for-each>
			<app:metadataset>
				<xsl:value-of select="toString:escape(  gmd:MD_Metadata )" />
			</app:metadataset>
		</app:CQP_Main>
	</xsl:template>
</xsl:stylesheet>
