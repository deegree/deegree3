<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:app="http://www.deegree.org/app"
  xmlns:csw="http://www.opengis.net/cat/csw"
  xmlns:gml="http://www.opengis.net/gml"
  xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
  xmlns:wfs="http://www.opengis.net/wfs" 
  xmlns:wrs="http://www.opengis.net/cat/wrs"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:minMaxExtract="org.deegree.framework.xml.MinMaxExtractor"
  exclude-result-prefixes="minMaxExtract wfs app xsl"
  >
  <xsl:output method="xml" indent="yes"/>

  <!-- Handy function to insert a default value into an xslt script -->
  <xsl:template name="choose_default">
    <xsl:param name="value"/>
    <xsl:param name="default"/>
    <xsl:choose>
      <xsl:when test="$value != '' ">
        <xsl:value-of select="$value"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$default"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ###############################################################
       # Mapping all property types of the app:RegistryObject, 
       # which has following structure:
       # - creating the inherited tns:Identifiable attributes and elements
       # - then creating the inherited rim:RegistryObject attributes and elements
       # - finally creating the rim:RegistryObject specific nested elements 
       #   and attributes.
       ########################################################-->

  <!-- #################################################################
       # the inherited tns:Identifiable Object attributes and elements #
       #################################################################-->
  
  <xsl:template match="app:iduri">
    <xsl:attribute name="id">
      <xsl:value-of select="normalize-space(.)"/>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="app:home">
    <xsl:attribute name="home">
      <xsl:value-of select="normalize-space(.)"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="app:slots/app:Slot">
    <rim:Slot>
      <xsl:attribute name="name">
        <xsl:value-of select="normalize-space( app:name )"/>
      </xsl:attribute>
      <xsl:attribute name="slotType">
        <xsl:value-of select="normalize-space( app:slotType )"/>
      </xsl:attribute>
      <wrs:ValueList>
        <xsl:apply-templates select="app:values/app:SlotValues"/>
      </wrs:ValueList>
    </rim:Slot>
  </xsl:template>

  <xsl:template match="app:values/app:SlotValues">
    <xsl:choose>
      <xsl:when test="app:stringValue != ''">
        <rim:Value>
          <xsl:value-of select="normalize-space( app:stringValue )"/>    
        </rim:Value>
      </xsl:when>
      <xsl:otherwise>
        <wrs:AnyValue>
        <!-- so it is a Geometry, we the given geometry will be returned as an Envelope, 
             which is a project specific definition.
             -->
        <xsl:choose>
          <xsl:when test="app:geometry != ''">
            <gml:Envelope srsName='EPSG:4326'>
              <!-- copy all attributes of the first element beneath the app:geometry element 
                   (probably just the srsName) -->
              <xsl:copy-of select="app:geometry/*[1]/@*"/>
              <gml:pos srsDimension='2'>
                <xsl:value-of select="minMaxExtract:getMinAsArray( app:geometry/*[1])"/>
              </gml:pos>
              <gml:pos srsDimension='2'>
                <xsl:value-of select="minMaxExtract:getMaxAsArray( app:geometry/*[1])"/>
              </gml:pos>
            </gml:Envelope>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message>
              <xsl:text>
                GML_TO_EBRIM.xsl: neither an app:stringValue nor an app:geometry element was 
                found in the slot, this cannot be.
              </xsl:text>
            </xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </wrs:AnyValue>
      </xsl:otherwise>
    </xsl:choose>      
  </xsl:template>

  <!-- #############################################################
       # the inherited rim:Registry Object attributes and elements #
       #############################################################-->
  <xsl:template match="app:liduri">
    <xsl:attribute name="lid">
      <xsl:value-of select="normalize-space(.)"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="app:objectType">
    <xsl:attribute name="objectType">
      <xsl:value-of select="normalize-space(.)"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="app:status">
    <xsl:attribute name="status">
      <xsl:value-of select="normalize-space(.)"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="app:LocalizedString">
    <rim:LocalizedString>
      <xsl:attribute name="charset">
        <xsl:value-of select="normalize-space( app:charset )"/>
      </xsl:attribute>
      <xsl:attribute name="lang">
        <xsl:value-of select="normalize-space( app:lang )"/>
      </xsl:attribute>
      <xsl:attribute name="value">
        <xsl:value-of select="normalize-space( app:value )"/>
      </xsl:attribute>
    </rim:LocalizedString>    
  </xsl:template>
  
  <!-- the app:name == rim:LocalizedString is an optional element of the rim:RegistryObject -->
  <xsl:template match="app:Name">
    <rim:Name>
      <xsl:apply-templates select="app:localizedString"/>
    </rim:Name>
  </xsl:template>

  <xsl:template match="app:Description">
    <rim:Description>
      <xsl:apply-templates select="app:localizedString"/>
    </rim:Description>
  </xsl:template>

  <xsl:template match="app:versionInfo/app:VersionInfo">
    <rim:VersionInfo>
      <xsl:attribute name="versionName">
        <xsl:call-template name="choose_default">
          <xsl:with-param name="value" >
            <xsl:value-of select="normalize-space(app:versionName)"/>
          </xsl:with-param>
          <xsl:with-param name="default">1.1</xsl:with-param>
        </xsl:call-template>        
      </xsl:attribute>
      <xsl:if test="app:comment != '' " >
        <xsl:attribute name="comment">
          <xsl:value-of select="normalize-space(app:comment)"/>
        </xsl:attribute>
      </xsl:if>      
    </rim:VersionInfo>
  </xsl:template>

  <!-- #################################################################
       # Create methods to find all inherited attributes, elements and # 
       # object specific nested elements.                              #
       #                                                               #
       # note: all app: elemenents are located at the root of the      #
       # app:registryObject and must therefore be called with the      #
       # anscestor of the context node                                 #
       #################################################################-->

  <!-- a template which inserts all the base attributes into a given app:RegistryObject-->
  <xsl:template name="create_rim_registry_object_attributes">
    <!--for brief, summary and full -->
    <!-- rim:Identifiable attributes -->
    <xsl:apply-templates select="../../app:iduri"/>
    
    <!-- rim:RegistryObject attributes -->
    <xsl:apply-templates select="../../app:liduri"/>
    <xsl:apply-templates select="../../app:objectType"/>
    <xsl:apply-templates select="../../app:status"/>

    <!-- defined only for full -->
    <xsl:if test="$ELEMENT_SET = 'full'">
      <xsl:apply-templates select="../../app:home"/>
    </xsl:if>
  </xsl:template>


  <!-- a template which inserts all the base elements into a given app:RegistryObject-->
  <xsl:template name="create_rim_registry_object_elements">
    <!--for brief, summary and full -->
    <xsl:apply-templates select="../../app:versionInfo/app:VersionInfo" />

    <xsl:if test="$ELEMENT_SET = 'full' or $ELEMENT_SET='summary' ">
      <!-- rim:Identifiable elements -->
      <xsl:apply-templates select="../../app:slots/app:Slot"/>
      
      <!-- rim:RegistryObject elements and attributes -->
      <xsl:apply-templates select="../../app:name/app:Name" />
      <xsl:apply-templates select="../../app:description/app:Description" />
    </xsl:if>
  </xsl:template>
  
  <!-- a template which inserts all the nested elements into a given app:RegistryObject -->
  <xsl:template name="create_nested_elements">
    <xsl:if test="$ELEMENT_SET = 'full'">
      <xsl:apply-templates select="../../app:linkedRegistryObject/app:LINK_RegObj_RegObj/app:registryObject/app:RegistryObject"/>      
    </xsl:if>
  </xsl:template>


  <!-- A convenience template which calls all three above templates -->
  <xsl:template name="create_rim_registry_object_base">
    <!-- first add all the inherited attributes to the given rim:RegistryObject -->
    <xsl:call-template name="create_rim_registry_object_attributes"/>
    
    <!-- then add all the inherited elements to the given rim:RegistryObject -->
    <xsl:call-template name="create_rim_registry_object_elements"/>
    
    <!-- finally add all the nested (object specific) elements to the given rim:RegistryObject -->
    <xsl:call-template name="create_nested_elements"/>
  </xsl:template>
  
  <!-- #############################################################
       # Mapping the app:RegistryObject FeatureType                #
       #                                                           #
       # beneath every gml:featureMember lies an app:RegistryObject# 
       # Therefore this template is the basis of the xslt mapping. #
       #############################################################-->  
  <xsl:template match="app:RegistryObject|app:linkedRegistryObject/app:LINK_RegObj_RegObj/app:registryObject/app:RegistryObject">
    <xsl:if test="app:type = 'RegistryObject'">
      <rim:RegistryObject>
        <!-- a template which inserts all the base attributes into a given app:RegistryObject-->
        <!--for brief, summary and full -->
        <!-- rim:Identifiable attributes -->
        <xsl:apply-templates select="app:iduri"/>
        
        <!-- rim:RegistryObject attributes -->
        <xsl:apply-templates select="app:liduri"/>
        <xsl:apply-templates select="app:objectType"/>
        <xsl:apply-templates select="app:status"/>
        
        <!-- defined only for full -->
        <xsl:if test="$ELEMENT_SET = 'full'">
          <xsl:apply-templates select="app:home"/>
        </xsl:if>
        <!-- a template which inserts all the base elements into a given app:RegistryObject-->
        <!--for brief, summary and full -->
        <xsl:apply-templates select="app:versionInfo/app:VersionInfo" />
        
        <xsl:if test="$ELEMENT_SET = 'full' or $ELEMENT_SET='summary' ">
          <!-- rim:Identifiable elements -->
          <xsl:apply-templates select="app:slots/app:Slot"/>
          
          <!-- rim:RegistryObject elements and attributes -->
          <xsl:apply-templates select="app:name/app:Name" />
          <xsl:apply-templates select="app:description/app:Description" />
        </xsl:if>
        <xsl:if test="$ELEMENT_SET = 'full'">
          <xsl:apply-templates select="app:linkedRegistryObject/app:LINK_RegObj_RegObj/app:registryObject/app:RegistryObject"/>      
        </xsl:if>
      </rim:RegistryObject>
    </xsl:if>
    <xsl:apply-templates select="app:association/app:Association"/>
    <xsl:apply-templates select="app:auditableEvent/app:AuditableEvent"/>
    <xsl:apply-templates select="app:classification/app:Classification"/>
    <xsl:apply-templates select="app:classificationNode/app:ClassificationNode"/>
    <xsl:apply-templates select="app:classificationScheme/app:ClassificationScheme"/>
    <xsl:apply-templates select="app:externalIdentifier/app:ExternalIdentifier"/>
    <xsl:apply-templates select="app:extrinsicObject/app:ExtrinsicObject"/>
    <xsl:apply-templates select="app:organization/app:Organization"/>
    <xsl:apply-templates select="app:person/app:Person"/>
    <xsl:apply-templates select="app:registryPackage/app:RegistryPackage"/>
  </xsl:template>

  <!-- ######################################
       # mapping the rim:Association Object #
       ###################################### -->
  <xsl:template match="app:association/app:Association">
    <rim:Association>
      <xsl:if test="$ELEMENT_SET = 'full'">
        <xsl:attribute name="associationType">
          <xsl:value-of select="normalize-space(app:associationType)"/>
        </xsl:attribute>
        <xsl:attribute name="sourceObject">
          <xsl:value-of select="normalize-space(app:sourceObject)"/>
        </xsl:attribute>
        <xsl:attribute name="targetObject">
          <xsl:value-of select="normalize-space(app:targetObject)"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:call-template name="create_rim_registry_object_base"/>
    </rim:Association>
  </xsl:template>


  <!-- #########################################
       # mapping the rim:AuditableEvent Object #
       ######################################### -->
  <xsl:template match="app:auditableEvent/app:AuditableEvent">
    <rim:AuditableEvent>
      <xsl:if test="$ELEMENT_SET = 'full'">
        <xsl:attribute name="eventType">
          <xsl:value-of select="normalize-space(app:eventType)"/>
        </xsl:attribute>
        <xsl:attribute name="timestamp">
          <xsl:value-of select="normalize-space(app:timestamp)"/>
        </xsl:attribute>
        <xsl:attribute name="user">
          <xsl:value-of select="normalize-space(app:username)"/>
        </xsl:attribute>
        <xsl:attribute name="requestId">
          <xsl:value-of select="normalize-space(app:requestId)"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:call-template name="create_rim_registry_object_base"/>
      <xsl:if test="$ELEMENT_SET = 'full'">
        <rim:affectedObjects>
          <xsl:apply-templates select="app:affectedObjects/app:ObjectRef"/>
        </rim:affectedObjects>
      </xsl:if>
    </rim:AuditableEvent>
  </xsl:template>
  
  <xsl:template match="app:affectedObjects/app:ObjectRef">
    <rim:ObjectRef>
      <xsl:apply-templates select="app:iduri"/>
      <xsl:apply-templates select="app:home"/>
      <xsl:apply-templates select="app:slots/app:Slot"/>
      <xsl:apply-templates select="app:createReplica"/>
    </rim:ObjectRef>
  </xsl:template>

  <xsl:template match="app:createReplica">
    <xsl:attribute name="createReplica">
      <xsl:value-of select="normalize-space(.)"/>
    </xsl:attribute>
  </xsl:template>

  <!-- #########################################
       # mapping the rim:Classification Object #
       ######################################### -->
  <xsl:template match="app:classification/app:Classification">
    <rim:Classification>
      <xsl:if test="$ELEMENT_SET = 'full'">
        <!--mandatory-->
        <xsl:attribute name="classifiedObject">
          <xsl:value-of select="normalize-space( app:classificationObject )"/>
        </xsl:attribute>
        <!--optional attributes -->
        <xsl:if test="app:classificationScheme != ''">
          <xsl:attribute name="classificationScheme">
            <xsl:value-of select="normalize-space( app:classificationScheme )"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="app:classificationNode != ''">
          <xsl:attribute name="classificationNode">
            <xsl:value-of select="normalize-space( app:classificationNode )"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="app:nodeRepresentation != ''">
          <xsl:attribute name="nodeRepresentation">
            <xsl:value-of select="normalize-space( app:nodeRepresentation )"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:if>
      <xsl:call-template name="create_rim_registry_object_base"/>
    </rim:Classification>
  </xsl:template>


  <!-- #############################################
       # mapping the rim:ClassificationNode Object #
       ############################################# -->
  <xsl:template match="app:classificationNode/app:ClassificationNode">
    <rim:ClassificationNode>
      <xsl:if test="$ELEMENT_SET = 'full'">
        <!--optional attributes -->
        <xsl:if test="app:parent != ''">
          <xsl:attribute name="parent">
            <xsl:value-of select="normalize-space( app:parent )"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="app:code != ''">
          <xsl:attribute name="code">
            <xsl:value-of select="normalize-space( app:code )"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="app:path != ''">
          <xsl:attribute name="path">
            <xsl:value-of select="normalize-space( app:path )"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:if>
      <xsl:call-template name="create_rim_registry_object_base"/>
    </rim:ClassificationNode>
  </xsl:template>


  <!-- ###############################################
       # mapping the rim:ClassificationScheme Object #
       ############################################### -->
  <xsl:template match="app:classificationScheme/app:ClassificationScheme">
    <rim:ClassificationScheme>
      <xsl:if test="$ELEMENT_SET = 'full'">
        <xsl:attribute name="isInternal">
          <xsl:value-of select="normalize-space( app:isInternal )"/>
        </xsl:attribute>
        <xsl:attribute name="nodeType">
          <xsl:value-of select="normalize-space( app:nodeType )"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:call-template name="create_rim_registry_object_base"/>
    </rim:ClassificationScheme>
  </xsl:template>

  <!-- #############################################
       # mapping the rim:ExternalIdentifier Object #
       ############################################# -->
  <xsl:template match="app:externalIdentifier/app:ExternalIdentifier">
    <rim:ExternalIdentifier>
      <xsl:if test="$ELEMENT_SET = 'full'">
        <xsl:attribute name="registryObject">
          <xsl:value-of select="normalize-space(app:registryObject)"/>
        </xsl:attribute>
        <xsl:attribute name="identificationScheme">
          <xsl:value-of select="normalize-space(app:identificationScheme)"/>
        </xsl:attribute>
        <xsl:attribute name="value">
          <xsl:value-of select="normalize-space(app:value)"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:call-template name="create_rim_registry_object_base"/>
    </rim:ExternalIdentifier>
  </xsl:template>


  <!-- ##########################################
       # mapping the rim:ExtrinsicObject Object #
       ########################################## -->
  <xsl:template match="app:extrinsicObject/app:ExtrinsicObject">
    <rim:ExtrinsicObject>
      <xsl:if test="$ELEMENT_SET = 'full'">
        <xsl:attribute name="mimeType">
          <xsl:call-template name="choose_default">
            <xsl:with-param name="value" >
              <xsl:value-of select="normalize-space(app:mimeType)"/>
            </xsl:with-param>
            <xsl:with-param name="default">application/octet-stream</xsl:with-param>
          </xsl:call-template>        
        </xsl:attribute>
        <xsl:attribute name="isOpaque">
          <xsl:call-template name="choose_default">
            <xsl:with-param name="value" >
              <xsl:value-of select="normalize-space(app:isOpaque)"/>
            </xsl:with-param>
            <xsl:with-param name="default">false</xsl:with-param>
          </xsl:call-template>        
        </xsl:attribute>
      </xsl:if>
      
      <xsl:call-template name="create_rim_registry_object_base"/>
      <!-- adding the optional ContentVersionInfo element, because it's attribute 
           versionName default to 1.1 it is always present in a rim:ExtrinsicObject -->
      <xsl:if test="$ELEMENT_SET = 'full'">
        <rim:ContentVersionInfo>
          <xsl:attribute name="versionName">
            <xsl:call-template name="choose_default">
              <xsl:with-param name="value" >
                <xsl:value-of select="normalize-space(app:cntInfoVersionName)"/>
              </xsl:with-param>
              <xsl:with-param name="default">1.1</xsl:with-param>
            </xsl:call-template>        
          </xsl:attribute>
          <xsl:if test="app:cntInfoVersionComment != ''">
            <xsl:attribute name="comment">
              <xsl:value-of select="normalize-space( app:cntInfoVersionComment )"/>
            </xsl:attribute>
          </xsl:if>
        </rim:ContentVersionInfo>
      </xsl:if>
    </rim:ExtrinsicObject>
  </xsl:template>

  <!-- ##########################################
       # mapping the rim:Organization Object    #
       ########################################## -->

  <xsl:template match="app:address/app:Address">
    <rim:Address>
      <xsl:if test="app:city != ''">
        <xsl:attribute name="city">
          <xsl:value-of select="normalize-space( app:city )"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="app:country != ''">
        <xsl:attribute name="country">
          <xsl:value-of select="normalize-space( app:country )"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="app:postalCode != ''">
        <xsl:attribute name="postalCode">
          <xsl:value-of select="normalize-space( app:postalCode )"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="app:stateOrProvince != ''">
        <xsl:attribute name="stateOrProvince">
          <xsl:value-of select="normalize-space( app:stateOrProvince )"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="app:street != ''">
        <xsl:attribute name="street">
          <xsl:value-of select="normalize-space( app:street )"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="app:streetNumber != ''">
        <xsl:attribute name="streetNumber">
          <xsl:value-of select="normalize-space( app:streetNumber )"/>
        </xsl:attribute>
      </xsl:if>
    </rim:Address>
  </xsl:template>

  <xsl:template name="create_telephone_number">
    <xsl:if test="app:areaCode != '' or app:countryCode !='' or app:extension != '' or app:number != '' or app:phoneType != ''">
      <rim:TelephoneNumber>
        <xsl:if test="app:areaCode != ''">
          <xsl:attribute name="areaCode">
            <xsl:value-of select="normalize-space( app:areaCode )"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="app:countryCode != ''">
          <xsl:attribute name="countryCode">
            <xsl:value-of select="normalize-space( app:countryCode )"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="app:extension != ''">
          <xsl:attribute name="extension">
            <xsl:value-of select="normalize-space( app:extension )"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="app:number != ''">
          <xsl:attribute name="number">
            <xsl:value-of select="normalize-space( app:number )"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="app:phoneType != ''">
          <xsl:attribute name="phoneType">
            <xsl:value-of select="normalize-space( app:phoneType )"/>
          </xsl:attribute>
        </xsl:if>
      </rim:TelephoneNumber>
    </xsl:if>
  </xsl:template>

  <xsl:template name="create_email_address">
    <xsl:if test="app:emailAddress != '' or app:emailAddressType !=''">
      <rim:EmailAddress>
        <xsl:if test="app:emailAddress != ''">
          <xsl:attribute name="address">
            <xsl:value-of select="normalize-space( app:emailAddress )"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="app:emailAddressType != ''">
          <xsl:attribute name="type">
            <xsl:value-of select="normalize-space( app:emailAddressType )"/>
          </xsl:attribute>
        </xsl:if>
      </rim:EmailAddress>
    </xsl:if>
  </xsl:template>

  <xsl:template match="app:organization/app:Organization">
    <rim:Organization>
      <xsl:apply-templates select="app:address/app:Address" />
      <xsl:call-template name="create_telephone_number"/>
      <xsl:call-template name="create_email_address"/>
    </rim:Organization>
  </xsl:template>

  <!-- ##########################################
       # mapping the rim:Person Object          #
       ########################################## -->
  <xsl:template match="app:Person/app:Person">
    <rim:Person>
      <xsl:apply-templates select="app:address/app:Address" />
      <xsl:if test="app:firstName != '' or app:lastName != '' or app:middleName != ''">
        <rim:PersonName>
          <xsl:if test="app:firstName != ''">
            <xsl:attribute name="firstName">
              <xsl:value-of select="normalize-space( app:firstName )"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="app:middleName != ''">
            <xsl:attribute name="middleName">
              <xsl:value-of select="normalize-space( app:middleName )"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="app:lastName != ''">
            <xsl:attribute name="lastName">
              <xsl:value-of select="normalize-space( app:lastName )"/>
            </xsl:attribute>
          </xsl:if>
        </rim:PersonName>
      </xsl:if>
      <xsl:call-template name="create_telephone_number"/>
      <xsl:call-template name="create_email_address"/>
    </rim:Person>
  </xsl:template>

  <!-- ##########################################
       # mapping the rim:RegistryPackage Object #
       ########################################## -->
  <xsl:template match="app:registryPackage/app:RegistryPackage">
    <rim:RegistryPackage>
      <xsl:call-template name="create_rim_registry_object_attributes"/>
      <xsl:call-template name="create_rim_registry_object_elements"/>
      <xsl:if test="$ELEMENT_SET = 'full'">
        <!-- only create an rim:RegistryObjectList if this registrypackage has 
             one or nested elements -->
        <xsl:if test="app:linkedRegistryObject/app:LINK_RegObj_RegObj/app:registryObject/app:RegistryObject/app:iduri != ''">
          <rim:RegistryObjectList>
            <xsl:call-template name="create_nested_elements"/>
          </rim:RegistryObjectList>
        </xsl:if>
      </xsl:if>
    </rim:RegistryPackage>
  </xsl:template>
 
</xsl:stylesheet>
