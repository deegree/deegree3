CREATE TABLE feature_types (
    id smallint PRIMARY KEY,
    qname text NOT NULL
);

COMMENT ON TABLE feature_types IS 'Ids and bboxes of concrete feature types';

SELECT ADDGEOMETRYCOLUMN('public', 'feature_types','bbox','4258','GEOMETRY',2);
ALTER TABLE feature_types ADD CONSTRAINT feature_types_check_bbox CHECK (isvalid(bbox));
/* (no spatial index needed, as envelope is only used for keeping track of feature type extents) */
INSERT INTO feature_types (id,qname) VALUES (0,'{urn:x-inspire:specification:gmlas:Addresses:3.0}Address');
INSERT INTO feature_types (id,qname) VALUES (1,'{urn:x-inspire:specification:gmlas:Addresses:3.0}AddressAreaName');
INSERT INTO feature_types (id,qname) VALUES (2,'{urn:x-inspire:specification:gmlas:Addresses:3.0}AdminUnitName');
INSERT INTO feature_types (id,qname) VALUES (3,'{urn:x-inspire:specification:gmlas:Addresses:3.0}PostalDescriptor');
INSERT INTO feature_types (id,qname) VALUES (4,'{urn:x-inspire:specification:gmlas:Addresses:3.0}ThoroughfareName');
INSERT INTO feature_types (id,qname) VALUES (5,'{urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0}AdministrativeBoundary');
INSERT INTO feature_types (id,qname) VALUES (6,'{urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0}AdministrativeUnit');
INSERT INTO feature_types (id,qname) VALUES (7,'{urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0}Condominium');
INSERT INTO feature_types (id,qname) VALUES (8,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}AerodromeArea');
INSERT INTO feature_types (id,qname) VALUES (9,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}AerodromeCategory');
INSERT INTO feature_types (id,qname) VALUES (10,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}AerodromeNode');
INSERT INTO feature_types (id,qname) VALUES (11,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}AerodromeType');
INSERT INTO feature_types (id,qname) VALUES (12,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}AirLinkSequence');
INSERT INTO feature_types (id,qname) VALUES (13,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}AirRoute');
INSERT INTO feature_types (id,qname) VALUES (14,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}AirRouteLink');
INSERT INTO feature_types (id,qname) VALUES (15,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}AirspaceArea');
INSERT INTO feature_types (id,qname) VALUES (16,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}ApronArea');
INSERT INTO feature_types (id,qname) VALUES (17,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}ConditionOfAirFacility');
INSERT INTO feature_types (id,qname) VALUES (18,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}DesignatedPoint');
INSERT INTO feature_types (id,qname) VALUES (19,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}ElementLength');
INSERT INTO feature_types (id,qname) VALUES (20,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}ElementWidth');
INSERT INTO feature_types (id,qname) VALUES (21,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}FieldElevation');
INSERT INTO feature_types (id,qname) VALUES (22,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}InstrumentApproachProcedure');
INSERT INTO feature_types (id,qname) VALUES (23,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}LowerAltitudeLimit');
INSERT INTO feature_types (id,qname) VALUES (24,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}Navaid');
INSERT INTO feature_types (id,qname) VALUES (25,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}ProcedureLink');
INSERT INTO feature_types (id,qname) VALUES (26,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}RunwayArea');
INSERT INTO feature_types (id,qname) VALUES (27,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}RunwayCentrelinePoint');
INSERT INTO feature_types (id,qname) VALUES (28,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}StandardInstrumentArrival');
INSERT INTO feature_types (id,qname) VALUES (29,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}StandardInstrumentDeparture');
INSERT INTO feature_types (id,qname) VALUES (30,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}SurfaceComposition');
INSERT INTO feature_types (id,qname) VALUES (31,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}TaxiwayArea');
INSERT INTO feature_types (id,qname) VALUES (32,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}TouchDownLiftOff');
INSERT INTO feature_types (id,qname) VALUES (33,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}UpperAltitudeLimit');
INSERT INTO feature_types (id,qname) VALUES (34,'{urn:x-inspire:specification:gmlas:AirTransportNetwork:3.0}UseRestriction');
INSERT INTO feature_types (id,qname) VALUES (35,'{urn:x-inspire:specification:gmlas:BiogeographicalRegions:0.0}Bio-GeographicalRegion');
INSERT INTO feature_types (id,qname) VALUES (36,'{urn:x-inspire:specification:gmlas:Buildings:0.0}Building');
INSERT INTO feature_types (id,qname) VALUES (37,'{urn:x-inspire:specification:gmlas:Buildings:0.0}ControlTower');
INSERT INTO feature_types (id,qname) VALUES (38,'{urn:x-inspire:specification:gmlas:CableTransportNetwork:3.0}CablewayLink');
INSERT INTO feature_types (id,qname) VALUES (39,'{urn:x-inspire:specification:gmlas:CableTransportNetwork:3.0}CablewayLinkSequence');
INSERT INTO feature_types (id,qname) VALUES (40,'{urn:x-inspire:specification:gmlas:CableTransportNetwork:3.0}CablewayLinkSet');
INSERT INTO feature_types (id,qname) VALUES (41,'{urn:x-inspire:specification:gmlas:CableTransportNetwork:3.0}CablewayNode');
INSERT INTO feature_types (id,qname) VALUES (42,'{urn:x-inspire:specification:gmlas:CadastralParcels:3.0}BasicPropertyUnit');
INSERT INTO feature_types (id,qname) VALUES (43,'{urn:x-inspire:specification:gmlas:CadastralParcels:3.0}CadastralBoundary');
INSERT INTO feature_types (id,qname) VALUES (44,'{urn:x-inspire:specification:gmlas:CadastralParcels:3.0}CadastralParcel');
INSERT INTO feature_types (id,qname) VALUES (45,'{urn:x-inspire:specification:gmlas:CadastralParcels:3.0}CadastralZoning');
INSERT INTO feature_types (id,qname) VALUES (46,'{urn:x-inspire:specification:gmlas:CommonTransportElements:3.0}AccessRestriction');
INSERT INTO feature_types (id,qname) VALUES (47,'{urn:x-inspire:specification:gmlas:CommonTransportElements:3.0}ConditionOfFacility');
INSERT INTO feature_types (id,qname) VALUES (48,'{urn:x-inspire:specification:gmlas:CommonTransportElements:3.0}MaintenanceAuthority');
INSERT INTO feature_types (id,qname) VALUES (49,'{urn:x-inspire:specification:gmlas:CommonTransportElements:3.0}MarkerPost');
INSERT INTO feature_types (id,qname) VALUES (50,'{urn:x-inspire:specification:gmlas:CommonTransportElements:3.0}OwnerAuthority');
INSERT INTO feature_types (id,qname) VALUES (51,'{urn:x-inspire:specification:gmlas:CommonTransportElements:3.0}RestrictionForVehicles');
INSERT INTO feature_types (id,qname) VALUES (52,'{urn:x-inspire:specification:gmlas:CommonTransportElements:3.0}TrafficFlowDirection');
INSERT INTO feature_types (id,qname) VALUES (53,'{urn:x-inspire:specification:gmlas:CommonTransportElements:3.0}TransportNetwork');
INSERT INTO feature_types (id,qname) VALUES (54,'{urn:x-inspire:specification:gmlas:CommonTransportElements:3.0}VerticalPosition');
INSERT INTO feature_types (id,qname) VALUES (55,'{urn:x-inspire:specification:gmlas:EnergyResources:0.0}HydroPowerPlant');
INSERT INTO feature_types (id,qname) VALUES (56,'{urn:x-inspire:specification:gmlas:Gazetteer:3.2}Gazetteer');
INSERT INTO feature_types (id,qname) VALUES (57,'{urn:x-inspire:specification:gmlas:Gazetteer:3.2}LocationType');
INSERT INTO feature_types (id,qname) VALUES (58,'{urn:x-inspire:specification:gmlas:GeographicalNames:3.0}NamedPlace');
INSERT INTO feature_types (id,qname) VALUES (59,'{urn:x-inspire:specification:gmlas:Geology:0.0}SpringOrSeep');
INSERT INTO feature_types (id,qname) VALUES (60,'{urn:x-inspire:specification:gmlas:Geology:0.0}VanishingPoint');
INSERT INTO feature_types (id,qname) VALUES (61,'{urn:x-inspire:specification:gmlas:HabitatsAndBiotopes:0.0}Habitat');
INSERT INTO feature_types (id,qname) VALUES (62,'{urn:x-inspire:specification:gmlas:HydroNetwork:3.0}HydroNode');
INSERT INTO feature_types (id,qname) VALUES (63,'{urn:x-inspire:specification:gmlas:HydroNetwork:3.0}WatercourseLink');
INSERT INTO feature_types (id,qname) VALUES (64,'{urn:x-inspire:specification:gmlas:HydroNetwork:3.0}WatercourseLinkSequence');
INSERT INTO feature_types (id,qname) VALUES (65,'{urn:x-inspire:specification:gmlas:HydroNetwork:3.0}WatercourseSeparatedCrossing');
INSERT INTO feature_types (id,qname) VALUES (66,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}Crossing');
INSERT INTO feature_types (id,qname) VALUES (67,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}DamOrWeir');
INSERT INTO feature_types (id,qname) VALUES (68,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}DrainageBasin');
INSERT INTO feature_types (id,qname) VALUES (69,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}Falls');
INSERT INTO feature_types (id,qname) VALUES (70,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}Ford');
INSERT INTO feature_types (id,qname) VALUES (71,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}LandWaterBoundary');
INSERT INTO feature_types (id,qname) VALUES (72,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}Lock');
INSERT INTO feature_types (id,qname) VALUES (73,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}Rapids');
INSERT INTO feature_types (id,qname) VALUES (74,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}RiverBasin');
INSERT INTO feature_types (id,qname) VALUES (75,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}ShorelineConstruction');
INSERT INTO feature_types (id,qname) VALUES (76,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}Sluice');
INSERT INTO feature_types (id,qname) VALUES (77,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}StandingWater');
INSERT INTO feature_types (id,qname) VALUES (78,'{urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0}Watercourse');
INSERT INTO feature_types (id,qname) VALUES (79,'{urn:x-inspire:specification:gmlas:LandCover:0.0}GlacierSnowfield');
INSERT INTO feature_types (id,qname) VALUES (80,'{urn:x-inspire:specification:gmlas:LandCover:0.0}Shore');
INSERT INTO feature_types (id,qname) VALUES (81,'{urn:x-inspire:specification:gmlas:LandCover:0.0}Wetland');
INSERT INTO feature_types (id,qname) VALUES (82,'{urn:x-inspire:specification:gmlas:NaturalRiskZones:0.0}Embankment');
INSERT INTO feature_types (id,qname) VALUES (83,'{urn:x-inspire:specification:gmlas:NaturalRiskZones:0.0}InundatedLand');
INSERT INTO feature_types (id,qname) VALUES (84,'{urn:x-inspire:specification:gmlas:Network:3.2}CrossReference');
INSERT INTO feature_types (id,qname) VALUES (85,'{urn:x-inspire:specification:gmlas:Network:3.2}GradeSeparatedCrossing');
INSERT INTO feature_types (id,qname) VALUES (86,'{urn:x-inspire:specification:gmlas:Network:3.2}Network');
INSERT INTO feature_types (id,qname) VALUES (87,'{urn:x-inspire:specification:gmlas:Network:3.2}NetworkConnection');
INSERT INTO feature_types (id,qname) VALUES (88,'{urn:x-inspire:specification:gmlas:ProtectedSites:3.0}ProtectedSite');
INSERT INTO feature_types (id,qname) VALUES (89,'{urn:x-inspire:specification:gmlas:ProtectedSitesFull:3.0}ProtectedSite');
INSERT INTO feature_types (id,qname) VALUES (90,'{urn:x-inspire:specification:gmlas:ProtectedSitesFull:3.0}ResponsibleAgency');
INSERT INTO feature_types (id,qname) VALUES (91,'{urn:x-inspire:specification:gmlas:ProtectedSitesNatura2000:3.0}ProtectedSite');
INSERT INTO feature_types (id,qname) VALUES (92,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}DesignSpeed');
INSERT INTO feature_types (id,qname) VALUES (93,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}NominalTrackGauge');
INSERT INTO feature_types (id,qname) VALUES (94,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}NumberOfTracks');
INSERT INTO feature_types (id,qname) VALUES (95,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayArea');
INSERT INTO feature_types (id,qname) VALUES (96,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayElectrification');
INSERT INTO feature_types (id,qname) VALUES (97,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayLine');
INSERT INTO feature_types (id,qname) VALUES (98,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayLink');
INSERT INTO feature_types (id,qname) VALUES (99,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayLinkSequence');
INSERT INTO feature_types (id,qname) VALUES (100,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayNode');
INSERT INTO feature_types (id,qname) VALUES (101,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayStationArea');
INSERT INTO feature_types (id,qname) VALUES (102,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayStationCode');
INSERT INTO feature_types (id,qname) VALUES (103,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayStationNode');
INSERT INTO feature_types (id,qname) VALUES (104,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayType');
INSERT INTO feature_types (id,qname) VALUES (105,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayUse');
INSERT INTO feature_types (id,qname) VALUES (106,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayYardArea');
INSERT INTO feature_types (id,qname) VALUES (107,'{urn:x-inspire:specification:gmlas:RailwayTransportNetwork:3.0}RailwayYardNode');
INSERT INTO feature_types (id,qname) VALUES (108,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}ERoad');
INSERT INTO feature_types (id,qname) VALUES (109,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}FormOfWay');
INSERT INTO feature_types (id,qname) VALUES (110,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}FunctionalRoadClass');
INSERT INTO feature_types (id,qname) VALUES (111,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}NumberOfLanes');
INSERT INTO feature_types (id,qname) VALUES (112,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}Road');
INSERT INTO feature_types (id,qname) VALUES (113,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}RoadArea');
INSERT INTO feature_types (id,qname) VALUES (114,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}RoadLink');
INSERT INTO feature_types (id,qname) VALUES (115,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}RoadLinkSequence');
INSERT INTO feature_types (id,qname) VALUES (116,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}RoadName');
INSERT INTO feature_types (id,qname) VALUES (117,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}RoadNode');
INSERT INTO feature_types (id,qname) VALUES (118,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}RoadServiceArea');
INSERT INTO feature_types (id,qname) VALUES (119,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}RoadServiceType');
INSERT INTO feature_types (id,qname) VALUES (120,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}RoadSurfaceCategory');
INSERT INTO feature_types (id,qname) VALUES (121,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}RoadWidth');
INSERT INTO feature_types (id,qname) VALUES (122,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}SpeedLimit');
INSERT INTO feature_types (id,qname) VALUES (123,'{urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0}VehicleTrafficArea');
INSERT INTO feature_types (id,qname) VALUES (124,'{urn:x-inspire:specification:gmlas:SeaRegions:0.0}OceanRegion');
INSERT INTO feature_types (id,qname) VALUES (125,'{urn:x-inspire:specification:gmlas:SpeciesDistribution:0.0}SpeciesDistribution');
INSERT INTO feature_types (id,qname) VALUES (126,'{urn:x-inspire:specification:gmlas:StatisticalUnits:0.0}NUTSRegion');
INSERT INTO feature_types (id,qname) VALUES (127,'{urn:x-inspire:specification:gmlas:UtilityAndGovernmentalServices:0.0}Pipe');
INSERT INTO feature_types (id,qname) VALUES (128,'{urn:x-inspire:specification:gmlas:UtilityAndGovernmentalServices:0.0}PumpingStation');
INSERT INTO feature_types (id,qname) VALUES (129,'{urn:x-inspire:specification:gmlas:WaterFrameworkDirective:0.0}WFDCoastalWater');
INSERT INTO feature_types (id,qname) VALUES (130,'{urn:x-inspire:specification:gmlas:WaterFrameworkDirective:0.0}WFDGroundWaterBody');
INSERT INTO feature_types (id,qname) VALUES (131,'{urn:x-inspire:specification:gmlas:WaterFrameworkDirective:0.0}WFDLake');
INSERT INTO feature_types (id,qname) VALUES (132,'{urn:x-inspire:specification:gmlas:WaterFrameworkDirective:0.0}WFDRiver');
INSERT INTO feature_types (id,qname) VALUES (133,'{urn:x-inspire:specification:gmlas:WaterFrameworkDirective:0.0}WFDTransitionalWater');
INSERT INTO feature_types (id,qname) VALUES (134,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}Beacon');
INSERT INTO feature_types (id,qname) VALUES (135,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}Buoy');
INSERT INTO feature_types (id,qname) VALUES (136,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}CEMTClass');
INSERT INTO feature_types (id,qname) VALUES (137,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}ConditionOfWaterFacility');
INSERT INTO feature_types (id,qname) VALUES (138,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}FairwayArea');
INSERT INTO feature_types (id,qname) VALUES (139,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}FerryCrossing');
INSERT INTO feature_types (id,qname) VALUES (140,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}FerryUse');
INSERT INTO feature_types (id,qname) VALUES (141,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}InlandWaterway');
INSERT INTO feature_types (id,qname) VALUES (142,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}MarineWaterway');
INSERT INTO feature_types (id,qname) VALUES (143,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}PortArea');
INSERT INTO feature_types (id,qname) VALUES (144,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}PortNode');
INSERT INTO feature_types (id,qname) VALUES (145,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}RestrictionForWaterVehicles');
INSERT INTO feature_types (id,qname) VALUES (146,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}TrafficSeparationSchemeCrossing');
INSERT INTO feature_types (id,qname) VALUES (147,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}TrafficSeparationSchemeLane');
INSERT INTO feature_types (id,qname) VALUES (148,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}TrafficSeparationSchemeRoundabout');
INSERT INTO feature_types (id,qname) VALUES (149,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}TrafficSeparationSchemeSeparator');
INSERT INTO feature_types (id,qname) VALUES (150,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}WaterLinkSequence');
INSERT INTO feature_types (id,qname) VALUES (151,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}WaterTrafficFlowDirection');
INSERT INTO feature_types (id,qname) VALUES (152,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}WaterwayLink');
INSERT INTO feature_types (id,qname) VALUES (153,'{urn:x-inspire:specification:gmlas:WaterTransportNetwork:3.0}WaterwayNode');

CREATE TABLE gml_objects (
    id SERIAL PRIMARY KEY,
    gml_id text UNIQUE NOT NULL,
    gml_description text,
    ft_type smallint REFERENCES feature_types,
    binary_object bytea
);
COMMENT ON TABLE gml_objects IS 'All objects (features and geometries)';
SELECT ADDGEOMETRYCOLUMN('public', 'gml_objects','gml_bounded_by','-1','GEOMETRY',2);
ALTER TABLE gml_objects ADD CONSTRAINT gml_objects_geochk CHECK (isvalid(gml_bounded_by));
CREATE INDEX gml_objects_sidx ON gml_objects USING GIST ( gml_bounded_by GIST_GEOMETRY_OPS );

CREATE TABLE gml_names (
    gml_object_id integer REFERENCES GML_OBJECTS,
    name text NOT NULL,
    codespace text,
    prop_idx smallint NOT NULL
);
