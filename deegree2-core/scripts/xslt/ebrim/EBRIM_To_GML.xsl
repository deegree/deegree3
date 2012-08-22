<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format" 
  xmlns:app="http://www.deegree.org/app"
  xmlns:gml="http://www.opengis.net/gml"
  xmlns:ogc="http://www.opengis.net/ogc" 
  xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
  xmlns:wfs="http://www.opengis.net/wfs"
  xmlns:wrs="http://www.opengis.net/cat/wrs"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:deegreecsw="http://www.deegree.org/csw"
  xmlns:geomConv="org.deegree.framework.xml.GeometryUtils"
  >

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

  <!-- 
       ***************************************
       * Creating the localizedString object *
       ***************************************
       -->
  <xsl:template match="rim:LocalizedString">
    <app:localizedString>
      <app:LocalizedString>
        <app:charset>
          <xsl:call-template name="choose_default">
            <xsl:with-param name="value" ><xsl:value-of select="@charset"/></xsl:with-param>
            <xsl:with-param name="default">UTF-8</xsl:with-param>
          </xsl:call-template>
        </app:charset>
        <app:value><xsl:value-of select="@value"/></app:value>
        <app:lang>
          <xsl:call-template name="choose_default">
            <xsl:with-param name="value" ><xsl:value-of select="@xml:lang"/></xsl:with-param>
            <xsl:with-param name="default">en</xsl:with-param>
          </xsl:call-template>
        </app:lang>    
      </app:LocalizedString>
    </app:localizedString>
  </xsl:template>

  <xsl:template match="rim:Name">
    <!--xsl:message>creating <xsl:value-of select="local-name(.)"/> object</xsl:message-->
    <app:name>
      <app:Name>
        <xsl:apply-templates select="*" />
      </app:Name>
    </app:name>
  </xsl:template>

  <xsl:template match="rim:Description">
    <!--xsl:message>creating <xsl:value-of select="local-name(.)"/> object</xsl:message-->
    <app:description>
      <app:Description>
        <xsl:apply-templates select="*" />
      </app:Description>
    </app:description>
  </xsl:template>

  <!--
       ***********************************************************
       * Creating the slot objects (inherited from identifiable) *
       ***********************************************************
       -->
  <xsl:template match="rim:ValueList|wrs:ValueList">
    <xsl:for-each select="*">
      <xsl:sort />  
      <app:values>
        <app:SlotValues>
          <xsl:choose>
            <xsl:when test="name(.) = 'rim:Value'">
              <app:stringValue><xsl:value-of select="."/></app:stringValue>
            </xsl:when>
            <xsl:otherwise>
              <app:geometry>
                <xsl:choose>
                  <!-- gml:Envelope is no geometry !!!! what ever -->
                  <xsl:when test="name(child::*[1]) = 'gml:Envelope'">
                    <gml:Surface>
                      <gml:patches>
                        <gml:PolygonPatch>
                          <gml:exterior>
                            <gml:LinearRing>
                              <gml:posList><xsl:value-of select="geomConv:getPolygonCoordinatesFromEnvelope( child::*[1])"/></gml:posList>
                            </gml:LinearRing>
                          </gml:exterior>
                        </gml:PolygonPatch>
                      </gml:patches>
                    </gml:Surface>
                  </xsl:when>                  
                  <xsl:otherwise>
                    <xsl:copy-of select="*"/>
                  </xsl:otherwise>
                </xsl:choose>
              </app:geometry>
            </xsl:otherwise>
          </xsl:choose>
        </app:SlotValues>
      </app:values>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="rim:Slot">
    <app:slots>
      <app:Slot>
        <app:name><xsl:value-of select="@name"/></app:name>
        <xsl:if test="@slotType != '' ">
          <app:slotType><xsl:value-of select="@slotType"/></app:slotType>          
        </xsl:if>
        <xsl:apply-templates select="rim:ValueList | wrs:ValueList" />
      </app:Slot>
    </app:slots>
  </xsl:template>

  <!-- 
       ************************************************
       * creating the simple elements from attributes *
       ************************************************
       -->

  <xsl:template match="@id">
    <app:iduri><xsl:value-of select="."/></app:iduri>
  </xsl:template>

  <!-- inherited from rim:identifiable, optional -->
  <xsl:template match="@home">
    <app:home><xsl:value-of select="."/></app:home>
  </xsl:template>

  <xsl:template match="@objectType">
    <app:objectType><xsl:value-of select="."/></app:objectType>
  </xsl:template>

  <!--
       **************************
       * The versioninfo object *
       **************************
       -->
  <xsl:template match="rim:VersionInfo">
    <app:versionInfo>
      <app:VersionInfo>
        <app:versionName>
          <xsl:call-template name="choose_default">
            <xsl:with-param name="value" ><xsl:value-of select="@versionName"/></xsl:with-param>
            <xsl:with-param name="default">1.1</xsl:with-param>
          </xsl:call-template>
        </app:versionName>
        <xsl:if test="@comment != '' ">
          <app:comment>
            <xsl:value-of select="@comment" />
          </app:comment>
        </xsl:if>
      </app:VersionInfo>
    </app:versionInfo>
  </xsl:template>

  <!--
       **************************************************
       * All values coming from the inherited registry  *
       * object are processed in the following template * 
       **************************************************
       -->
  <xsl:template name="registry_object_type">
    <xsl:apply-templates select="rim:Classification"/>
    <xsl:apply-templates select="rim:ExternalIdentifier"/>
    <xsl:apply-templates select="rim:Name"/>
    <xsl:apply-templates select="rim:Description"/>

    <!-- for the wfs scheme the slots are beneath the
         app:RegistryObject, inherited from rim:identifiable -->
    <xsl:apply-templates select="rim:Slot"/>

    <app:type><xsl:value-of select="local-name(.)"/></app:type>
    <!-- inherited from rim:identifiable: required-->

    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates select="@home"/>
    <app:liduri>
      <xsl:call-template name="choose_default">
        <xsl:with-param name="value" ><xsl:value-of select="@lid"/></xsl:with-param>
        <xsl:with-param name="default"><xsl:value-of select="@id"/></xsl:with-param>
      </xsl:call-template>
    </app:liduri>
    <xsl:apply-templates select="@objectType"/>
    <app:status>
      <xsl:call-template name="choose_default">
        <xsl:with-param name="value" ><xsl:value-of select="@status"/></xsl:with-param>
        <xsl:with-param name="default">valid</xsl:with-param>
      </xsl:call-template>
    </app:status>
    <xsl:apply-templates select="rim:VersionInfo"/>
  </xsl:template>


  <!--
       ******************************
       * Association Object parsing *
       ******************************
       -->  
  <xsl:template match="rim:Association">
    <!--xsl:message>creating rim:Association</xsl:message-->
    <app:RegistryObject>
      <xsl:call-template name="registry_object_type" />
      <app:association>
        <app:Association>
          <app:associationType>
            <xsl:value-of select="@associationType"/>
          </app:associationType>
          <app:sourceObject>
            <xsl:value-of select="@sourceObject"/>
          </app:sourceObject>
          <app:targetObject>
            <xsl:value-of select="@targetObject"/>
          </app:targetObject>
        </app:Association>
      </app:association>
    </app:RegistryObject>
  </xsl:template>


  <!--
       ******************************
       * AuditableEvent Object parsing *
       ******************************
       -->  
  <xsl:template match="rim:ObjectRef">
    <app:affectedObjects>
      <app:ObjectRef>
        <app:iduri><xsl:value-of select="@id"/></app:iduri>
        <xsl:apply-templates select="@home"/>
        <app:createReplica>
          <xsl:call-template name="choose_default">
            <xsl:with-param name="value" ><xsl:value-of select="@createReplica"/></xsl:with-param>
            <xsl:with-param name="default">false</xsl:with-param>
          </xsl:call-template>
        </app:createReplica>
        <xsl:apply-templates select="rim:Slot"/>
      </app:ObjectRef>
    </app:affectedObjects>
  </xsl:template>

  <xsl:template match="rim:AuditableEvent">
    <app:RegistryObject>
      <xsl:call-template name="registry_object_type" />
      <app:auditableEvent>
        <app:AuditableEvent>
          <app:eventType><xsl:value-of select="@eventType"/></app:eventType>
          <app:timestamp><xsl:value-of select="@timestamp"/></app:timestamp>
          <app:username><xsl:value-of select="@user"/></app:username>
          <app:requestId><xsl:value-of select="@requestId"/></app:requestId>
          <xsl:apply-templates select="rim:affectedObjects/rim:ObjectRef"/>
        </app:AuditableEvent>
      </app:auditableEvent>
    </app:RegistryObject>
  </xsl:template>

  <!--
       *************************************
       * Classification Object parsing *
       *************************************
       -->  

  <xsl:template name="classificationCreator">
    <app:RegistryObject>
      <xsl:call-template name="registry_object_type" />
      <app:classification>
        <app:Classification>
          <!-- optional rim attribute classificationScheme -->
          <xsl:if test="@classificationScheme != '' ">
            <app:classificationScheme>
              <xsl:value-of select="@classificationScheme" />
            </app:classificationScheme>
          </xsl:if>          
          <!-- required rim attribute classified -->
          <app:classificationObject>
            <xsl:value-of select="@classifiedObject" />
          </app:classificationObject>
          <!-- optional rim attribute classificationNode -->
          <xsl:if test="@classificationNode != '' ">
            <app:classificationNode>
              <xsl:value-of select="@classificationNode" />
            </app:classificationNode>
          </xsl:if>          
          <!-- optional rim attribute nodeRepresentation -->
          <xsl:if test="@nodeRepresentation != '' ">
            <app:nodeRepresentation>
              <xsl:value-of select="@nodeRepresentation" />
            </app:nodeRepresentation>
          </xsl:if>          
        </app:Classification>
      </app:classification>
    </app:RegistryObject>
  </xsl:template>


  <xsl:template match="rim:Classification">
      <!-- xsl:message>
        <xsl:text>Localname of parent = </xsl:text><xsl:value-of select="local-name( parent::* )"/>
      </xsl:message-->
    <xsl:choose>
      <!-- simulate the jointables if the namespaces of the parent equals
           the namespace of the current node, else the namespace must be 
           different e.g. csw:insert != rim:Classification -->

      <xsl:when test="namespace-uri( parent::* ) = namespace-uri( . ) and (local-name( parent::* ) != 'RegistryObjectList')">
        <app:linkedRegistryObject>
          <app:LINK_RegObj_RegObj>
            <app:registryObject>
              <xsl:call-template name="classificationCreator"/>
            </app:registryObject>
            <app:type><xsl:value-of select="local-name(.)"/></app:type>
          </app:LINK_RegObj_RegObj>
        </app:linkedRegistryObject>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="classificationCreator" /> 
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
       *************************************
       * ClassificationNode Object parsing *
       *************************************
       -->  

  <xsl:template name="classificationNodeCreator">
    <app:RegistryObject>
      <xsl:apply-templates select="rim:ClassificationNode"/>
      <xsl:call-template name="registry_object_type" /> 
      <!--allthough the attributes are optional, it is preferable to generate the empty Features -->
      <app:classificationNode>
        <app:ClassificationNode>
          <xsl:if test="@parent | @code | @path != '' ">
            <xsl:if test="@parent != '' ">
              <app:parent>
                <xsl:value-of select="@parent" />
              </app:parent>
            </xsl:if>          
            <xsl:if test="@code != '' ">
              <app:code>
                <xsl:value-of select="@code" />
              </app:code>
            </xsl:if>          
            <xsl:if test="@path != '' ">
              <app:path>
                <xsl:value-of select="@path" />
              </app:path>
            </xsl:if>          
          </xsl:if>
        </app:ClassificationNode>
      </app:classificationNode>
    </app:RegistryObject>
  </xsl:template>
  

  <xsl:template match="rim:ClassificationNode">
      <!-- xsl:message><xsl:text>Localname of parent = </xsl:text><xsl:value-of select="local-name( parent::* )"/></xsl:message-->
    <xsl:choose>
      <!-- simulate the jointables if the namespaces of the parent equals
           the namespace of the current node, else the namespace must be 
           different e.g. csw:insert != rim:ClassificationNode -->
      <xsl:when test="namespace-uri( parent::* ) = namespace-uri( . ) and (local-name( parent::* ) != 'RegistryObjectList')">
        <app:linkedRegistryObject>
          <app:LINK_RegObj_RegObj>
            <app:registryObject>
              <xsl:call-template name="classificationNodeCreator"/>
            </app:registryObject>
            <app:type><xsl:value-of select="local-name(.)"/></app:type>
          </app:LINK_RegObj_RegObj>
        </app:linkedRegistryObject>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="classificationNodeCreator" /> 
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
       ***************************************
       * ClassificationScheme Object parsing *
       ***************************************
       -->  

  <xsl:template match="rim:ClassificationScheme">
    <app:RegistryObject>
      <xsl:apply-templates select="rim:ClassificationNode"/>
      <xsl:call-template name="registry_object_type" />
      <app:classificationScheme>
        <app:ClassificationScheme>
          <app:isInternal>
            <xsl:value-of select="@isInternal" />
          </app:isInternal>
          <app:nodeType>
            <xsl:value-of select="@nodeType" />
          </app:nodeType>
        </app:ClassificationScheme>
      </app:classificationScheme>
    </app:RegistryObject>
  </xsl:template>


  <!--
       *************************************
       * ExternalIdentifier Object parsing *
       *************************************
       -->
  <xsl:template match="rim:ExternalIdentifier">
    <app:RegistryObject>
      <xsl:call-template name="registry_object_type" />
      <app:externalIdentifier>
        <app:ExternalIdentifier>
          <app:registryObject>
            <xsl:value-of select="@registryObject"/>
          </app:registryObject>
          <app:identificationScheme>
            <xsl:value-of select="@identificationScheme"/>
          </app:identificationScheme>
          <app:value>
            <xsl:value-of select="@value"/>
          </app:value>
        </app:ExternalIdentifier>
      </app:externalIdentifier>
    </app:RegistryObject>
  </xsl:template>


  <!--
       ****************************
       * Extrinsic Object parsing *
       ****************************
       -->
  <xsl:template match="rim:ContentVersionInfo">
    <app:cntInfoVersionName>
      <xsl:call-template name="choose_default">
        <xsl:with-param name="value" ><xsl:value-of select="@versionName"/></xsl:with-param>
        <xsl:with-param name="default">1.1</xsl:with-param>
      </xsl:call-template>
    </app:cntInfoVersionName>
    <xsl:if test="@comment != '' ">
      <app:cntInfoVersionComment>
        <xsl:value-of select="@comment" />
      </app:cntInfoVersionComment>
    </xsl:if>          
  </xsl:template>

  <xsl:template match="deegreecsw:DescribedObject">
    <app:object>
      <xsl:value-of select="."/>
    </app:object>
  </xsl:template>

  <!-- This template matches any given rim:extrinsicobject to an app:ExtrinsicObject -->
  <xsl:template match="rim:ExtrinsicObject">
    <!--xsl:message>creating rim:ExtrinsicObject</xsl:message-->
    <app:RegistryObject>
      <xsl:call-template name="registry_object_type" />
      <app:extrinsicObject>
        <app:ExtrinsicObject>
          <app:mimeType>
            <xsl:call-template name="choose_default">
              <xsl:with-param name="value" ><xsl:value-of select="@mimeType"/></xsl:with-param>
              <xsl:with-param name="default">application/octet-stream</xsl:with-param>
            </xsl:call-template>
          </app:mimeType>
          <app:isOpaque>
            <xsl:call-template name="choose_default">
              <xsl:with-param name="value" ><xsl:value-of select="@isOpaque"/></xsl:with-param>
              <xsl:with-param name="default">false</xsl:with-param>
            </xsl:call-template>
          </app:isOpaque>
          <xsl:apply-templates select="rim:ContentVersionInfo" />
          <xsl:apply-templates select="deegreecsw:DescribedObject"/>
        </app:ExtrinsicObject>
      </app:extrinsicObject>
    </app:RegistryObject>
  </xsl:template>

  <!--
       **********************************
       * Organization Object parsing    *
       **********************************
       -->
  <!-- following templates, Address, PersonName, TelephoneNumber 
       and EmailAddress are used both in Organization and Person -->
  <xsl:template match="rim:Address">
    <app:address>
      <app:Address>
        <xsl:if test="@city != ''">
          <app:city>
            <xsl:value-of select="normalize-space( @city )"/>
          </app:city>
        </xsl:if>
        <xsl:if test="@country!= ''">
          <app:country>
            <xsl:value-of select="normalize-space( @country )"/>
          </app:country>
        </xsl:if>
        <xsl:if test="@postalCode != ''">
          <app:postalCode>
            <xsl:value-of select="normalize-space( @postalCode )"/>
          </app:postalCode>
        </xsl:if>
        <xsl:if test="@stateOrProvince != ''">
          <app:stateOrProvince>
            <xsl:value-of select="normalize-space( @stateOrProvince )"/>
          </app:stateOrProvince>
        </xsl:if>
        <xsl:if test="@street != ''">
          <app:street>
            <xsl:value-of select="normalize-space( @street )"/>
          </app:street>
        </xsl:if>
        <xsl:if test="@streetNumber != ''">
          <app:streetNumber>
            <xsl:value-of select="normalize-space( @streetNumber )"/>
          </app:streetNumber>
        </xsl:if>
      </app:Address>
    </app:address>
  </xsl:template>

  <xsl:template match="rim:PersonName">
    <xsl:if test="@firstName != ''">
      <app:firstName>
        <xsl:value-of select="normalize-space( @firstName )"/>
      </app:firstName>
    </xsl:if>
    <xsl:if test="@middleName != ''">
      <app:middleName>
        <xsl:value-of select="normalize-space( @middleName )"/>
      </app:middleName>
    </xsl:if>
    <xsl:if test="@lastName != ''">
      <app:lastName>
        <xsl:value-of select="normalize-space( @lastName )"/>
      </app:lastName>
    </xsl:if>
  </xsl:template>

  <xsl:template match="rim:TelephoneNumber">
    <xsl:if test="@areaCode != ''">
      <app:areaCode>
        <xsl:value-of select="normalize-space( @areaCode )"/>
      </app:areaCode>
    </xsl:if>
    <xsl:if test="@countryCode != ''">
      <app:countryCode>
        <xsl:value-of select="normalize-space( @countryCode )"/>
      </app:countryCode>
    </xsl:if>
    <xsl:if test="@extension != ''">
      <app:extension>
        <xsl:value-of select="normalize-space( @extension )"/>
      </app:extension>
    </xsl:if>
    <xsl:if test="@number != ''">
      <app:number>
        <xsl:value-of select="normalize-space( @number )"/>
      </app:number>
    </xsl:if>
    <xsl:if test="@phoneType != ''">
      <app:phoneType>
        <xsl:value-of select="normalize-space( @phoneType )"/>
      </app:phoneType>
    </xsl:if>
  </xsl:template>

  <xsl:template match="rim:EmailAddress">
    <xsl:if test="@address != ''">
      <app:emailAddress>
        <xsl:value-of select="normalize-space( @address )"/>
      </app:emailAddress>
    </xsl:if>
    <xsl:if test="@type != ''">
      <app:emailAddressType>
        <xsl:value-of select="normalize-space( @type )"/>
      </app:emailAddressType>
    </xsl:if>
  </xsl:template>

  
  <xsl:template match="rim:Organization">
    <app:RegistryObject>
      <xsl:call-template name="registry_object_type" />
      <app:organization>
        <app:Organization>
          <xsl:apply-templates select="rim:Address"/>
          <xsl:apply-templates select="rim:TelephoneNumber"/>
          <xsl:apply-templates select="rim:EmailAddress"/>
          <!-- currently not supported
               <app:parent>
                 <xsl:value-of select="@parent" />
               </app:parent>
               <xsl:if test="@primaryContact != ''">
                 <app:primaryContact>
                   <xsl:value-of select="@primaryContact" />
                 </app:primaryContact>
               </xsl:if>
               -->
        </app:Organization>
      </app:organization>
    </app:RegistryObject>
  </xsl:template>

  <!--
       **********************************
       * Person Object parsing          *
       **********************************
       -->
  <xsl:template match="rim:Person">
    <app:RegistryObject>
      <xsl:call-template name="registry_object_type" />
      <app:person>
        <app:Person>
          <xsl:apply-templates select="rim:Address"/>
          <xsl:apply-templates select="rim:PersonName"/>
          <xsl:apply-templates select="rim:TelephoneNumber"/>
          <xsl:apply-templates select="rim:EmailAddress"/>
        </app:Person>
      </app:person>      
    </app:RegistryObject>
  </xsl:template>

  <!--
       **********************************
       * RegistryPackage Object parsing *
       **********************************
       -->
  <xsl:template match="rim:RegistryObjectList">
    <!-- xsl:message>
      <xsl:text>In the registryObjectList</xsl:text>
    </xsl:message-->
    <xsl:for-each select="*">
      <app:linkedRegistryObject>
        <app:LINK_RegObj_RegObj>
          <app:registryObject>
            <xsl:apply-templates select="."/>
          </app:registryObject>
          <app:type><xsl:value-of select="local-name(.)"/></app:type>
        </app:LINK_RegObj_RegObj>
      </app:linkedRegistryObject>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="rim:RegistryPackage">
    <app:RegistryObject>
      <xsl:apply-templates select="rim:RegistryObjectList" />
      <xsl:call-template name="registry_object_type" />
      <app:registryPackage>
        <app:RegistryPackage>
          <!-- inherited from identifiable -->
          <!--app:id><xsl:value-of select="@id"/></app:id-->
        </app:RegistryPackage>
      </app:registryPackage>
    </app:RegistryObject>
  </xsl:template>  


  <!---*************************
       the rim:RegistryObject
       *************************-->
  <xsl:template match="rim:RegistryObject">
    <app:RegistryObject>
      <xsl:call-template name="registry_object_type" />
    </app:RegistryObject>
  </xsl:template>  

</xsl:stylesheet>
