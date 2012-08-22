rem This file is part of deegree, for copyright\license information, please visit http:\\www.deegree.org\license.
rem This script uses postgis tool 'shp2pgsql' to transfer shape data into postgis database table

rem SCRIPT BEGIN
shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID024_StateBoundary sgid024_stateboundary | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID100_CountyBoundaries_edited SGID100_CountyBoundaries_edited | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID500_ZipCodes sgid500_zipcodes | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID500_Contours500Ft sgid500_contours500ft | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID500_Contours1000Ft sgid500_contours1000ft | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID500_Contours2500Ft sgid500_contours2500ft | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID500_EnergyResourcesPoly sgid500_energyresourcespoly | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID024_Springs sgid024_springs | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID100_LakesDLG100 sgid100_lakesdlg100 | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID100_Airports sgid100_airports | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID100_RailroadsDLG100 sgid100_railroadsdlg100 | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID100_RailroadsTIGER1990 sgid100_railroadstiger1990 | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID100_RoadsDLG100 sgid100_roadsdlg100 | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID500_RoadsDLG500 sgid500_roadsdlg500 | psql -d deegreetest -U deegreetest
rem shp2pgsql -d -i -I -s 26912 ..\..\data\utah\vector\SGID500_DominantVegetation sgid500_dominantvegetation | psql -d deegreetest -U deegreetest

rem SCRIPT END