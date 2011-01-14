<?xml version="1.0" encoding="UTF-8"?>
<base:SpatialDataSet gml:id="NL.KAD.BAG" xmlns:base="urn:x-inspire:specification:gmlas:BaseTypes:3.2"
  xmlns:ad="urn:x-inspire:specification:gmlas:Addresses:3.0" xmlns:gn="urn:x-inspire:specification:gmlas:GeographicalNames:3.0"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml/3.2">
  <gml:boundedBy>
    <gml:Envelope srsName="EPSG:4258">
      <gml:lowerCorner>5.234514684466063 52.68961794947077</gml:lowerCorner>
      <gml:upperCorner>5.2540758176641 52.711751467641456</gml:upperCorner>
    </gml:Envelope>
  </gml:boundedBy>
  <base:identifier>
    <base:Identifier>
      <base:localId>0</base:localId>
      <base:namespace>NL.KAD.BAG</base:namespace>
    </base:Identifier>
  </base:identifier>
  <base:metadata xsi:nil="true" />
  <base:member>
    <ad:Address gml:id="NL.KAD.BAG.0532200000000003">
      <ad:inspireId>
        <base:Identifier>
          <base:localId>0532200000000003</base:localId>
          <base:namespace>NL.KAD.BAG</base:namespace>
        </base:Identifier>
      </ad:inspireId>
      <ad:position>
        <ad:GeographicPosition>
          <ad:geometry>
            <gml:Point srsName="EPSG:4258" gml:id="NL.KAD.BAG.0532200000000003_P">
              <gml:pos>5.246345245309804 52.68961794947077</gml:pos>
            </gml:Point>
          </ad:geometry>
          <ad:specification>entrance</ad:specification>
          <ad:method>byOtherParty</ad:method>
          <ad:default>true</ad:default>
        </ad:GeographicPosition>
      </ad:position>
      <ad:locator>
        <ad:AddressLocator>
          <ad:designator xmlns:gml="http://www.opengis.net/gml">
            <ad:LocatorDesignator>
              <ad:designator>1</ad:designator>
              <ad:type>2</ad:type>
            </ad:LocatorDesignator>
          </ad:designator>
          <ad:level>unitLevel</ad:level>
        </ad:AddressLocator>
      </ad:locator>
      <ad:validFrom xmlns:gml="http://www.opengis.net/gml">2009-01-05T23:00:00.000</ad:validFrom>
      <ad:validTo xmlns:gml="http://www.opengis.net/gml">2299-12-30T23:00:00.000</ad:validTo>
      <ad:beginLifespanVersion xsi:nil="true" nilReason="UNKNOWN" />
      <ad:endLifespanVersion xsi:nil="true" nilReason="UNKNOWN" />
      <ad:component xlink:href="#NL.KAD.AA.1102" />
      <ad:component xlink:href="#NL.KAD.PD.1611ZP" />
      <ad:component xlink:href="#NL.KAD.TN.0532300000000077" />
    </ad:Address>
  </base:member>
  <base:member>
    <ad:AddressAreaName gml:id="NL.KAD.AA.1102">
      <ad:beginLifespanVersion xsi:nil="true" nilReason="UNKNOWN" />
      <ad:endLifespanVersion xsi:nil="true" nilReason="UNKNOWN" />

      <ad:validFrom xsi:nil="true" nilReason="UNKNOWN" />
      <ad:validTo xsi:nil="true" nilReason="UNKNOWN" />
      <ad:name>
        <gn:GeographicalName>
          <gn:language>nld</gn:language>
          <gn:nativeness>Endonym</gn:nativeness>
          <gn:nameStatus>Official</gn:nameStatus>
          <gn:sourceOfName>Het Kadaster, Nederland</gn:sourceOfName>
          <gn:pronunciation>
            <gn:PronunciationOfName />
          </gn:pronunciation>
          <gn:spelling>
            <gn:SpellingOfName>
              <gn:text>Bovenkarspel</gn:text>
              <gn:script>Latn</gn:script>
            </gn:SpellingOfName>
          </gn:spelling>
        </gn:GeographicalName>
      </ad:name>
      <ad:namedPlace xsi:nil="true" nilReason="UNKNOWN" />
    </ad:AddressAreaName>
  </base:member>
  <base:member>
    <ad:PostalDescriptor gml:id="NL.KAD.PD.1611ZP">
      <ad:beginLifespanVersion xsi:nil="true" nilReason="UNKNOWN" />
      <ad:endLifespanVersion xsi:nil="true" nilReason="UNKNOWN" />
      <ad:status />
      <ad:validFrom xsi:nil="true" nilReason="UNKNOWN" />
      <ad:validTo xsi:nil="true" nilReason="UNKNOWN" />
      <ad:postCode>1611ZP</ad:postCode>
    </ad:PostalDescriptor>
  </base:member>
  <base:member>
    <ad:ThoroughfareName gml:id="NL.KAD.TN.0532300000000077">
      <ad:beginLifespanVersion xsi:nil="true" nilReason="UNKNOWN" />
      <ad:endLifespanVersion xsi:nil="true" nilReason="UNKNOWN" />
      <ad:validFrom xsi:nil="true" nilReason="UNKNOWN" />
      <ad:validTo xsi:nil="true" nilReason="UNKNOWN" />
      <ad:name>
        <gn:GeographicalName>
          <gn:language>nld</gn:language>
          <gn:nativeness>Endonym</gn:nativeness>
          <gn:nameStatus>Official</gn:nameStatus>
          <gn:sourceOfName>Het Kadaster, Nederland</gn:sourceOfName>
          <gn:pronunciation>
            <gn:PronunciationOfName />
          </gn:pronunciation>
          <gn:spelling>
            <gn:SpellingOfName>
              <gn:text>Hugo de Grootsingel</gn:text>
              <gn:script>Latn</gn:script>
            </gn:SpellingOfName>
          </gn:spelling>
        </gn:GeographicalName>
      </ad:name>
    </ad:ThoroughfareName>
  </base:member>
</base:SpatialDataSet>