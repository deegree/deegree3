<message:AIXMBasicMessage xmlns:message="http://www.aixm.aero/schema/5.1/message" xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml/3.2"
  xmlns:aixm="http://www.aixm.aero/schema/5.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.aixm.aero/schema/5.1/message ../appschemas/aixm/message/AIXM_BasicMessage.xsd" gml:id="uniqueid">
  <gml:boundedBy>
    <gml:Envelope srsName="urn:ogc:def:crs:EPSG:4326">
      <gml:lowerCorner>-32.31632162667984 52.36833333333333</gml:lowerCorner>
      <gml:upperCorner>-32.546517283367514 52.29638464616918</gml:upperCorner>
    </gml:Envelope>
  </gml:boundedBy>
  <message:hasMember>
    <aixm:AirportHeliport gml:id="EADH">
      <gml:identifier codeSpace="http://www.aixm.aero/schema/5.1/example">dd062d88-3e64-4a5d-bebd-89476db9ebea</gml:identifier>
      <aixm:timeSlice>
        <aixm:AirportHeliportTimeSlice gml:id="ahts1EADH">
          <gml:validTime>
            <gml:TimePeriod gml:id="vtnull0">
              <gml:beginPosition>2009-01-01T00:00:00.000</gml:beginPosition>
              <gml:endPosition indeterminatePosition="unknown" />
            </gml:TimePeriod>
          </gml:validTime>
          <aixm:interpretation>BASELINE</aixm:interpretation>
          <aixm:sequenceNumber>1</aixm:sequenceNumber>
          <aixm:correctionNumber>0</aixm:correctionNumber>
          <aixm:featureLifetime>
            <gml:TimePeriod gml:id="ltnull0">
              <gml:beginPosition>2009-01-01T00:00:00.000</gml:beginPosition>
              <gml:endPosition indeterminatePosition="unknown" />
            </gml:TimePeriod>
          </aixm:featureLifetime>
          <aixm:designator>EADH</aixm:designator>
          <aixm:name>DONLON/DOWNTOWN HELIPORT</aixm:name>
          <aixm:magneticVariation>-3</aixm:magneticVariation>
          <aixm:dateMagneticVariation>1990</aixm:dateMagneticVariation>
          <aixm:magneticVariationChange>0.03</aixm:magneticVariationChange>
          <aixm:responsibleOrganisation>
            <aixm:AirportHeliportResponsibilityOrganisation gml:id="A-a72cfd3a">
              <aixm:role>OPERATE</aixm:role>
            </aixm:AirportHeliportResponsibilityOrganisation>
          </aixm:responsibleOrganisation>
          <aixm:ARP>
            <aixm:ElevatedPoint srsDimension="2" gml:id="elpoint1EADH">
              <gml:metaDataProperty>
                <gml:GenericMetaData>Example for metadata: Ce point ne pas une GML point, c'est une AIXM point.</gml:GenericMetaData>
              </gml:metaDataProperty>
              <gml:description>This is just for testing the parsing of standard GML properties.</gml:description>
              <gml:descriptionReference xlink:href="http://www.aixm.org/whatever" />
              <gml:identifier codeSpace="urn:blabla:bla">XYZ</gml:identifier>
              <gml:name>Point P1</gml:name>
              <gml:name>P1</gml:name>
              <gml:pos srsDimension="2">-32.035 52.288888888888884</gml:pos>
              <aixm:horizontalAccuracy uom="M">1.0</aixm:horizontalAccuracy>
              <aixm:annotation xsi:nil="true" nilReason="inapplicable" />
              <aixm:elevation uom="M">18.0</aixm:elevation>
              <aixm:geoidUndulation uom="M">3.22</aixm:geoidUndulation>
              <aixm:verticalDatum>NAVD88</aixm:verticalDatum>
              <aixm:verticalAccuracy uom="M">2.0</aixm:verticalAccuracy>
            </aixm:ElevatedPoint>
          </aixm:ARP>
        </aixm:AirportHeliportTimeSlice>
      </aixm:timeSlice>
    </aixm:AirportHeliport>
  </message:hasMember>
  <message:hasMember>
    <aixm:AirportHeliport gml:id="EADD">
      <gml:identifier codeSpace="http://www.aixm.aero/schema/5.1/example">1b54b2d6-a5ff-4e57-94c2-f4047a381c64</gml:identifier>
      <aixm:timeSlice>
        <aixm:AirportHeliportTimeSlice gml:id="ahts1EADD">
          <gml:validTime>
            <gml:TimePeriod gml:id="vtEADH1">
              <gml:beginPosition>2009-01-01T00:00:00.000</gml:beginPosition>
              <gml:endPosition indeterminatePosition="unknown" />
            </gml:TimePeriod>
          </gml:validTime>
          <aixm:interpretation>BASELINE</aixm:interpretation>
          <aixm:sequenceNumber>1</aixm:sequenceNumber>
          <aixm:correctionNumber>0</aixm:correctionNumber>
          <aixm:featureLifetime>
            <gml:TimePeriod gml:id="ltEADH1">
              <gml:beginPosition>2009-01-01T00:00:00.000</gml:beginPosition>
              <gml:endPosition indeterminatePosition="unknown" />
            </gml:TimePeriod>
          </aixm:featureLifetime>
          <aixm:designator>EADD</aixm:designator>
          <aixm:name>DONLON</aixm:name>
          <aixm:magneticVariation>3</aixm:magneticVariation>
          <aixm:dateMagneticVariation>1990</aixm:dateMagneticVariation>
          <aixm:magneticVariationChange>0.03</aixm:magneticVariationChange>
          <aixm:responsibleOrganisation>
            <aixm:AirportHeliportResponsibilityOrganisation gml:id="A-bf72b7b4">
              <aixm:role>OPERATE</aixm:role>
            </aixm:AirportHeliportResponsibilityOrganisation>
          </aixm:responsibleOrganisation>
          <aixm:ARP>
            <aixm:ElevatedPoint srsDimension="2" gml:id="elpoint1EADD">
              <gml:pos srsDimension="3">-31.949444444444445 52.388333333333335 </gml:pos>
              <aixm:elevation uom="M">30.0</aixm:elevation>
            </aixm:ElevatedPoint>
          </aixm:ARP>
          <aixm:availability>
            <aixm:AirportHeliportAvailability gml:id="A-eef5b53b">
              <aixm:timeInterval>
                <aixm:Timesheet gml:id="T-c5fdb917">
                  <aixm:timeReference>UTC</aixm:timeReference>
                  <aixm:day>ANY</aixm:day>
                  <aixm:startTime>07:00</aixm:startTime>
                  <aixm:endTime>22:00</aixm:endTime>
                </aixm:Timesheet>
              </aixm:timeInterval>
              <aixm:operationalStatus>NORMAL</aixm:operationalStatus>
            </aixm:AirportHeliportAvailability>
          </aixm:availability>
          <aixm:availability>
            <aixm:AirportHeliportAvailability gml:id="A-ff34d2b9">
              <aixm:timeInterval>
                <aixm:Timesheet gml:id="T-e921a1ce">
                  <aixm:timeReference>UTC</aixm:timeReference>
                  <aixm:day>ANY</aixm:day>
                  <aixm:startTime>06:00</aixm:startTime>
                  <aixm:endTime>07:00</aixm:endTime>
                </aixm:Timesheet>
              </aixm:timeInterval>
              <aixm:timeInterval>
                <aixm:Timesheet gml:id="T-d9e420be">
                  <aixm:timeReference>UTC</aixm:timeReference>
                  <aixm:day>ANY</aixm:day>
                  <aixm:startTime>22:00</aixm:startTime>
                  <aixm:endTime>23:00</aixm:endTime>
                </aixm:Timesheet>
              </aixm:timeInterval>
              <aixm:operationalStatus>LIMITED</aixm:operationalStatus>
              <aixm:usage>
                <aixm:AirportHeliportUsage gml:id="A-d38f0d0f">
                  <aixm:type>FORBID</aixm:type>
                  <aixm:selection>
                    <aixm:ConditionCombination gml:id="C-ad9e4d95">
                      <aixm:logicalOperator>NONE</aixm:logicalOperator>
                      <aixm:aircraft>
                        <aixm:AircraftCharacteristic gml:id="A-a10bc8d4">
                          <aixm:type>HELICOPTER</aixm:type>
                        </aixm:AircraftCharacteristic>
                      </aixm:aircraft>
                    </aixm:ConditionCombination>
                  </aixm:selection>
                  <aixm:operation>ALL</aixm:operation>
                </aixm:AirportHeliportUsage>
              </aixm:usage>
            </aixm:AirportHeliportAvailability>
          </aixm:availability>
          <aixm:availability>
            <aixm:AirportHeliportAvailability gml:id="A-e9a4bdb0">
              <aixm:timeInterval>
                <aixm:Timesheet gml:id="T-a56ba915">
                  <aixm:timeReference>UTC</aixm:timeReference>
                  <aixm:day>ANY</aixm:day>
                  <aixm:startTime>23:00</aixm:startTime>
                  <aixm:endTime>06:00</aixm:endTime>
                </aixm:Timesheet>
              </aixm:timeInterval>
              <aixm:operationalStatus>CLOSED</aixm:operationalStatus>
            </aixm:AirportHeliportAvailability>
          </aixm:availability>
        </aixm:AirportHeliportTimeSlice>
      </aixm:timeSlice>
    </aixm:AirportHeliport>
  </message:hasMember>
</message:AIXMBasicMessage>