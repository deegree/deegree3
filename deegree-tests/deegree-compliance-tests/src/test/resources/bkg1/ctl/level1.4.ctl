<?xml version="1.0" encoding="UTF-8"?>
<package
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://www.occamlab.com/ctl"
   xmlns:ctl="http://www.occamlab.com/ctl"
   xmlns:parsers="http://www.occamlab.com/te/parsers"
   xmlns:p="http://teamengine.sourceforge.net/parsers"
   xmlns:saxon="http://saxon.sf.net/"
   xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:csw2="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:iso19115="http://schemas.opengis.net/iso19115full"
   xmlns:ows="http://www.opengis.net/ows"
   xmlns:ogc="http://www.opengis.net/ogc"
   xmlns:gmd="http://www.isotc211.org/2005/gmd"
   xmlns:srv="http://www.isotc211.org/2005/srv"
   xmlns:gco="http://www.isotc211.org/2005/gco"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:dct="http://purl.org/dc/terms/"
   xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
   xmlns:xlink="http://www.w3.org/1999/xlink"
   xmlns:xi="http://www.w3.org/2001/XInclude">

  <test name="csw:level1.4">
    <param name="csw.GetCapabilities.document"/>
    <assertion>Run tests for level 1.4 compliance.</assertion>
    <code>

      <xsl:variable name="csw.GetCapabilities.get.url">
        <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
      </xsl:variable>

      <xsl:variable name="csw.GetRecordById.get.url">
        <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
      </xsl:variable>

      <xsl:variable name="csw.GetRecords.post.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.GetRecords.soap.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.GetRecordById.post.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.GetRecordById.soap.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.DescribeRecord.post.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.DescribeRecord.soap.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <call-test name="csw:CorrectnessOfResult.GetRecords-tightly-coupledServicesFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectnessOfResult.GetRecords-tightly-coupledOperatesOnFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectnessOfResult.GetRecords-ViewServicesFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectnessOfResult.GetRecords-ServicesOpOnFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectnessOfResult.GetRecords-ServiceProvidesData">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectnessOfResult.GetRecords-ViewServiceProvidesData">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectnessOfResult.GetRecords-ViewServiceProvidesDataWithOp">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:CorrectnessOfResult.GetRecords-DataProvidedByViewService">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectnessOfResult.GetRecords-tightly-coupledServicesFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for services which are tightly-coupled.
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 2 ‘brief’ metadata entries (although 3 are found) returned in the http://www.isotc211.org/2005/gmd format
      3. the number of records matched within the response is 3.
      4. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="2" maxRecords="3">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">brief</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>service</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:CouplingType</ogc:PropertyName>
                            <ogc:Literal>tight</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)=2)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response/soap:Envelope/soap:Body/csw2:GetRecordsResponse/csw2:SearchResults/@numberOfRecordsMatched=3)">
        <message>FAILURE: the third assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectnessOfResult.GetRecords-tightly-coupledOperatesOnFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for services which are tightly-coupled and operate on a specified dataset..
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 2 ‘summary’ metadata entries returned in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">summary</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>service</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:CouplingType</ogc:PropertyName>
                            <ogc:Literal>tight</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:OperatesOn</ogc:PropertyName>
                            <ogc:Literal>_3E06228D-0676-1C4F-33B4-722163F876C3</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;1)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectnessOfResult.GetRecords-ViewServicesFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for View Service (WMS)  metadata records which boundingBox satisfies a specific spatial filter (s. query below) and which title is like “Topographische Karte” or which includes a keyword of value “Topographische Karte”.
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 1 ‘brief’ metadata entry returned in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">brief</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>service</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:ServiceType</ogc:PropertyName>
                            <ogc:Literal>view</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:BBOX>
                            <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>
                            <gml:Envelope>
                              <gml:lowerCorner>7.30 49.30</gml:lowerCorner>
                              <gml:upperCorner>10.70 51.70</gml:upperCorner>
                            </gml:Envelope>
                          </ogc:BBOX>
                          <ogc:Or>
                            <ogc:PropertyIsEqualTo>
                              <ogc:PropertyName>apiso:subject</ogc:PropertyName>
                              <ogc:Literal>Topographische Karte</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLike escapeChar="\" singleChar="?" wildCard="*">
                              <ogc:PropertyName>apiso:title</ogc:PropertyName>
                              <ogc:Literal>*Topographische Karte*</ogc:Literal>
                            </ogc:PropertyIsLike>
                          </ogc:Or>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectnessOfResult.GetRecords-ServicesOpOnFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for View- and Download-Services which operate on a specific dataset.
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 2 ‘full’ metadata entries returned in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">full</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>service</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:Or>
                            <ogc:And>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>apiso:ServiceType</ogc:PropertyName>
                                <ogc:Literal>view</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>apiso:OperatesOn</ogc:PropertyName>
                                <ogc:Literal>_3E06228D-0676-1C4F-33B4-722163F876C3</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                            </ogc:And>
                            <ogc:And>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>apiso:ServiceType</ogc:PropertyName>
                                <ogc:Literal>download</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>apiso:OperatesOn</ogc:PropertyName>
                                <ogc:Literal>_3E06228D-0676-1C4F-33B4-722163F876C3</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                            </ogc:And>
                          </ogc:Or>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;1)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectnessOfResult.GetRecords-ServiceProvidesData">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      First, search for Data (“Wasserschutzgebiete”) in a specific area.
      The response to the first GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 1 ‘brief’ metadata entry referencing the dataset defined in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
      Second, try to find a service providing the data. Therefore the ressourceIdentifier must be selected by the CTL script from the dataset metadata found by the query in step 1 and be copied into the comparedTo value of the operatesOn queryable in the query of step 2.
      The response to the second GetRecords request must satisfy the applicable assertions:
      4. the filter request is understood by the server and no exception concerning the request is thrown
      5. the response includes 2 ‘full’ metadata entries referencing the view service and the download service (providing the data) in http://www.isotc211.org/2005/gmd format
      6. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         c.validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         d.validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">summary</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:BBOX>
                            <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>
                            <gml:Envelope>
                              <gml:lowerCorner>8.09 49.90</gml:lowerCorner>
                              <gml:upperCorner>8.20 50.10</gml:upperCorner>
                            </gml:Envelope>
                          </ogc:BBOX>
                          <ogc:PropertyIsLike escapeChar="\" singleChar="?" wildCard="*">
                            <ogc:PropertyName>apiso:subject</ogc:PropertyName>
                            <ogc:Literal>*Wasserschutzgebiet*</ogc:Literal>
                          </ogc:PropertyIsLike>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

      <message>Requesting datasets which operate on ID <xsl:value-of select="$response//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString" /></message>

      <xsl:variable name="response2">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">full</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>service</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:OperatesOn</ogc:PropertyName>
                            <ogc:Literal><xsl:value-of select="$response//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString" /></ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response2/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response2//gmd:MD_Metadata)&gt;1)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response2//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response2//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response2/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectnessOfResult.GetRecords-ViewServiceProvidesData">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      First, search for Data (“Wasserschutzgebiete”) in a specific area.
      The response to the first GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 1 ‘brief’ metadata entry referencing the dataset defined in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
      Second, try to find a view service providing the data referenced within the data metadata item (found in step one). Therefore the ressourceIdentifier must be selected by the CTL script from the dataset metadata found by the query in step 1 and be copied into the comparedTo value of the operatesOn queryable in the query of step 2.
      The response to the second GetRecords request must satisfy the applicable assertions:
      4. the filter request is understood by the server and no exception concerning the request is thrown
      5. the response includes 1 ‘full’ metadata entry referencing the view service (providing the data) in http://www.isotc211.org/2005/gmd format
      6. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         c. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         d. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">summary</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:BBOX>
                            <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>
                            <gml:Envelope>
                              <gml:lowerCorner>8.09 49.90</gml:lowerCorner>
                              <gml:upperCorner>8.20 50.10</gml:upperCorner>
                            </gml:Envelope>
                          </ogc:BBOX>
                          <ogc:PropertyIsLike escapeChar="\" singleChar="?" wildCard="*">
                            <ogc:PropertyName>apiso:subject</ogc:PropertyName>
                            <ogc:Literal>*Wasserschutzgebiet*</ogc:Literal>
                          </ogc:PropertyIsLike>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

      <message>Requesting datasets which operate on ID <xsl:value-of select="$response//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString" /></message>

      <xsl:variable name="response2">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
		<GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
		  <Query typeNames="gmd:MD_Metadata">
		    <ElementSetName typeNames="">full</ElementSetName>
		    <Constraint version="1.1.0">
		      <ogc:Filter>
			<ogc:And>
			  <ogc:PropertyIsEqualTo>
			    <ogc:PropertyName>apiso:type</ogc:PropertyName>
			    <ogc:Literal>service</ogc:Literal>
			  </ogc:PropertyIsEqualTo>
			  <ogc:PropertyIsEqualTo>
			    <ogc:PropertyName>apiso:ServiceType</ogc:PropertyName>
			    <ogc:Literal>view</ogc:Literal>
			  </ogc:PropertyIsEqualTo>
			  <ogc:PropertyIsEqualTo>
			    <ogc:PropertyName>apiso:OperatesOn</ogc:PropertyName>
			    <ogc:Literal><xsl:value-of select="$response//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString" /></ogc:Literal>
			  </ogc:PropertyIsEqualTo>
			</ogc:And>
		      </ogc:Filter>
		    </Constraint>
		  </Query>
		</GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response2/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response2//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response2//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response2//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response2/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectnessOfResult.GetRecords-ViewServiceProvidesDataWithOp">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      First, search for Data (“Wasserschutzgebiete”) in a specific area.
      The response to the first GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 1 ‘brief’ metadata entry referencing the dataset defined in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
      Second, try to find a view service providing the data referenced within the data metadata item (found in step one) via a GetMap-operation. Therefore the ressourceIdentifier must be selected by the CTL script from the dataset metadata found by the query in step 1 and be copied into the comparedTo value of the operatesOn queryable in the query of step 2.
      The response to the second GetRecords request must satisfy the applicable assertions:
      4. the filter request is understood by the server and no exception concerning the request is thrown
      5. the response includes 1 ‘full’ metadata entry referencing the view service (providing the data) in http://www.isotc211.org/2005/gmd format
      6. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         c. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         d. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">summary</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:BBOX>
                            <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>
                            <gml:Envelope>
                              <gml:lowerCorner>8.09 49.90</gml:lowerCorner>
                              <gml:upperCorner>8.20 50.10</gml:upperCorner>
                            </gml:Envelope>
                          </ogc:BBOX>
                          <ogc:PropertyIsLike escapeChar="\" singleChar="?" wildCard="*">
                            <ogc:PropertyName>apiso:subject</ogc:PropertyName>
                            <ogc:Literal>*Wasserschutzgebiet*</ogc:Literal>
                          </ogc:PropertyIsLike>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

      <message>Requesting datasets which operate on ID <xsl:value-of select="$response//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString" /></message>

      <xsl:variable name="response2">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">full</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>service</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:ServiceType</ogc:PropertyName>
                            <ogc:Literal>view</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:OperatesOnIdentifier</ogc:PropertyName>
                            <ogc:Literal><xsl:value-of select="$response//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString" /></ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:OperatesOnName</ogc:PropertyName>
                            <ogc:Literal>GetMap</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response2/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response2//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response2//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response2//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response2/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectnessOfResult.GetRecords-DataProvidedByViewService">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      First try to find a view service providing two specific dataset instances which are already known by their ressourceIdentifier.
      The response to this GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 1 ‘full’ metadata entry referencing the view service (providing the data) in http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd

      In a second step try to find the full metadata of the two data items referenced by their ressourceIdentifier.
      Therefore the values of the operatesOn properties of the service metadata item found in step 1 must be copied into the comparedTo values of the ResourceIdentifier queryables in the query of step 2.
      The response to this second GetRecords request must satisfy the applicable assertions:
      4. the filter request is understood by the server and no exception concerning the request is thrown
      5. the response includes 2 ‘full’ metadata entries referencing the datasets defined in the http://www.isotc211.org/2005/gmd format
      6. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         c. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         d. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">full</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>service</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:ServiceType</ogc:PropertyName>
                            <ogc:Literal>view</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:OperatesOn</ogc:PropertyName>
                            <ogc:Literal>_3E06228D-0676-1C4F-33B4-722163F876C3</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:OperatesOn</ogc:PropertyName>
                            <ogc:Literal>_7A48288E-1288-8C2A-65C1-985623C777D2</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

      <message>Requesting dataset operating on <xsl:value-of select="$response//srv:operatesOn[1]/@uuidref" /> and <xsl:value-of select="$response//srv:operatesOn[2]/@uuidref" /></message>

      <xsl:variable name="response2">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">full</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:Or>
                            <ogc:PropertyIsEqualTo>
                              <ogc:PropertyName>apiso:ResourceIdentifier</ogc:PropertyName>
                              <ogc:Literal><xsl:value-of select="$response//srv:operatesOn[1]/@uuidref" /></ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                              <ogc:PropertyName>apiso:ResourceIdentifier</ogc:PropertyName>
                            <ogc:Literal><xsl:value-of select="$response//srv:operatesOn[2]/@uuidref" /></ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                          </ogc:Or>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response2/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response2//gmd:MD_Metadata)&gt;1)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response2//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response2//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response2/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

</package>
