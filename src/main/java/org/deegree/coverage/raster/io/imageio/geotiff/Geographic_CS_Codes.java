//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.coverage.raster.io.imageio.geotiff;

import java.util.HashMap;

/**
 * <code>Geographic_CS_Codes</code> defines the different codes defined in the GeoTIFF spec. see
 * http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.3.1 for a list of possible values.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class Geographic_CS_Codes {

    private static HashMap<String, Integer> geographic_cs_type_codes = null;

    private static HashMap<String, Integer> ellipsoid_only_GCS = null;

    private static HashMap<String, Integer> geodectic_datum_code = null;

    private static HashMap<String, Integer> ellipsoid_only_datum = null;

    private static HashMap<String, Integer> ellipsoid_codes = null;

    private static HashMap<String, Integer> prime_meridian_codes = null;

    /**
     * private constructor. static initializer
     */
    private Geographic_CS_Codes() {
        // private constructor. static initializer
    }

    /**
     * static initializer
     */
    static {
        geographic_cs_type_codes = new HashMap<String, Integer>( 300 );
        ellipsoid_only_GCS = new HashMap<String, Integer>( 300 );
        geodectic_datum_code = new HashMap<String, Integer>( 300 );
        ellipsoid_only_datum = new HashMap<String, Integer>( 300 );
        ellipsoid_codes = new HashMap<String, Integer>( 300 );
        prime_meridian_codes = new HashMap<String, Integer>( 300 );

        // Geographic CS Type Codes
        geographic_cs_type_codes.put( "Adindan", new Integer( 4201 ) );
        geographic_cs_type_codes.put( "AGD66", new Integer( 4202 ) );
        geographic_cs_type_codes.put( "AGD84", new Integer( 4203 ) );
        geographic_cs_type_codes.put( "Ain_el_Abd", new Integer( 4204 ) );
        geographic_cs_type_codes.put( "Ain el Abd", new Integer( 4204 ) );
        geographic_cs_type_codes.put( "AinelAbd", new Integer( 4204 ) );
        geographic_cs_type_codes.put( "Afgooye", new Integer( 4205 ) );
        geographic_cs_type_codes.put( "Agadez", new Integer( 4206 ) );
        geographic_cs_type_codes.put( "Lisbon", new Integer( 4207 ) );
        geographic_cs_type_codes.put( "Aratu", new Integer( 4208 ) );
        geographic_cs_type_codes.put( "Arc_1950", new Integer( 4209 ) );
        geographic_cs_type_codes.put( "Arc 1950", new Integer( 4209 ) );
        geographic_cs_type_codes.put( "Arc1950", new Integer( 4209 ) );
        geographic_cs_type_codes.put( "Arc_1960", new Integer( 4210 ) );
        geographic_cs_type_codes.put( "Arc 1960", new Integer( 4210 ) );
        geographic_cs_type_codes.put( "Arc1960", new Integer( 4210 ) );
        geographic_cs_type_codes.put( "Batavia", new Integer( 4211 ) );
        geographic_cs_type_codes.put( "Barbados", new Integer( 4212 ) );
        geographic_cs_type_codes.put( "Beduaram", new Integer( 4213 ) );
        geographic_cs_type_codes.put( "Beijing_1954", new Integer( 4214 ) );
        geographic_cs_type_codes.put( "Beijing 1954", new Integer( 4214 ) );
        geographic_cs_type_codes.put( "Beijing1954", new Integer( 4214 ) );
        geographic_cs_type_codes.put( "Belge_1950", new Integer( 4215 ) );
        geographic_cs_type_codes.put( "Belge 1950", new Integer( 4215 ) );
        geographic_cs_type_codes.put( "Belge1950", new Integer( 4215 ) );
        geographic_cs_type_codes.put( "Bermuda_1957", new Integer( 4216 ) );
        geographic_cs_type_codes.put( "Bermuda 1957", new Integer( 4216 ) );
        geographic_cs_type_codes.put( "Bermuda1957", new Integer( 4216 ) );
        geographic_cs_type_codes.put( "Bern_1898", new Integer( 4217 ) );
        geographic_cs_type_codes.put( "Bern 1898", new Integer( 4217 ) );
        geographic_cs_type_codes.put( "Bern1898", new Integer( 4217 ) );
        geographic_cs_type_codes.put( "Bogota", new Integer( 4218 ) );
        geographic_cs_type_codes.put( "Bukit_Rimpah", new Integer( 4219 ) );
        geographic_cs_type_codes.put( "Bukit Rimpah", new Integer( 4219 ) );
        geographic_cs_type_codes.put( "BukitRimpah", new Integer( 4219 ) );
        geographic_cs_type_codes.put( "Camacupa", new Integer( 4220 ) );
        geographic_cs_type_codes.put( "Campo_Inchauspe", new Integer( 4221 ) );
        geographic_cs_type_codes.put( "Campo Inchauspe", new Integer( 4221 ) );
        geographic_cs_type_codes.put( "CampoInchauspe", new Integer( 4221 ) );
        geographic_cs_type_codes.put( "Cape", new Integer( 4222 ) );
        geographic_cs_type_codes.put( "Carthage", new Integer( 4223 ) );
        geographic_cs_type_codes.put( "Chua", new Integer( 4224 ) );
        geographic_cs_type_codes.put( "Corrego_Alegre", new Integer( 4225 ) );
        geographic_cs_type_codes.put( "Corrego Alegre", new Integer( 4225 ) );
        geographic_cs_type_codes.put( "CorregoAlegre", new Integer( 4225 ) );
        geographic_cs_type_codes.put( "Cote_d_Ivoire", new Integer( 4226 ) );
        geographic_cs_type_codes.put( "Cote d Ivoire", new Integer( 4226 ) );
        geographic_cs_type_codes.put( "CotedIvoire", new Integer( 4226 ) );
        geographic_cs_type_codes.put( "Deir_ez_Zor", new Integer( 4227 ) );
        geographic_cs_type_codes.put( "Deir ez Zor", new Integer( 4227 ) );
        geographic_cs_type_codes.put( "DeirezZor", new Integer( 4227 ) );
        geographic_cs_type_codes.put( "Douala", new Integer( 4228 ) );
        geographic_cs_type_codes.put( "Egypt_1907", new Integer( 4229 ) );
        geographic_cs_type_codes.put( "Egypt 1907", new Integer( 4229 ) );
        geographic_cs_type_codes.put( "Egypt1907", new Integer( 4229 ) );
        geographic_cs_type_codes.put( "ED50", new Integer( 4230 ) );
        geographic_cs_type_codes.put( "ED87", new Integer( 4231 ) );
        geographic_cs_type_codes.put( "Fahud", new Integer( 4232 ) );
        geographic_cs_type_codes.put( "Gandajika_1970", new Integer( 4233 ) );
        geographic_cs_type_codes.put( "Gandajika 1970", new Integer( 4233 ) );
        geographic_cs_type_codes.put( "Gandajika1970", new Integer( 4233 ) );
        geographic_cs_type_codes.put( "Garoua", new Integer( 4234 ) );
        geographic_cs_type_codes.put( "Guyane_Francaise", new Integer( 4235 ) );
        geographic_cs_type_codes.put( "Guyane Francaise", new Integer( 4235 ) );
        geographic_cs_type_codes.put( "GuyaneFrancaise", new Integer( 4235 ) );
        geographic_cs_type_codes.put( "Hu_Tzu_Shan", new Integer( 4236 ) );
        geographic_cs_type_codes.put( "Hu Tzu Shan", new Integer( 4236 ) );
        geographic_cs_type_codes.put( "HuTzuShan", new Integer( 4236 ) );
        geographic_cs_type_codes.put( "HD72", new Integer( 4237 ) );
        geographic_cs_type_codes.put( "ID74", new Integer( 4238 ) );
        geographic_cs_type_codes.put( "Indian_1954", new Integer( 4239 ) );
        geographic_cs_type_codes.put( "Indian 1954", new Integer( 4239 ) );
        geographic_cs_type_codes.put( "Indian1954", new Integer( 4239 ) );
        geographic_cs_type_codes.put( "Indian_1975", new Integer( 4240 ) );
        geographic_cs_type_codes.put( "Indian 1975", new Integer( 4240 ) );
        geographic_cs_type_codes.put( "Indian1975", new Integer( 4240 ) );
        geographic_cs_type_codes.put( "Jamaica_1875", new Integer( 4241 ) );
        geographic_cs_type_codes.put( "Jamaica 1875", new Integer( 4241 ) );
        geographic_cs_type_codes.put( "Jamaica1875", new Integer( 4241 ) );
        geographic_cs_type_codes.put( "JAD69", new Integer( 4242 ) );
        geographic_cs_type_codes.put( "Kalianpur", new Integer( 4243 ) );
        geographic_cs_type_codes.put( "Kandawala", new Integer( 4244 ) );
        geographic_cs_type_codes.put( "Kertau", new Integer( 4245 ) );
        geographic_cs_type_codes.put( "KOC", new Integer( 4246 ) );
        geographic_cs_type_codes.put( "La_Canoa", new Integer( 4247 ) );
        geographic_cs_type_codes.put( "La Canoa", new Integer( 4247 ) );
        geographic_cs_type_codes.put( "LaCanoa", new Integer( 4247 ) );
        geographic_cs_type_codes.put( "PSAD56", new Integer( 4248 ) );
        geographic_cs_type_codes.put( "Lake", new Integer( 4249 ) );
        geographic_cs_type_codes.put( "Leigon", new Integer( 4250 ) );
        geographic_cs_type_codes.put( "Liberia_1964", new Integer( 4251 ) );
        geographic_cs_type_codes.put( "Liberia 1964", new Integer( 4251 ) );
        geographic_cs_type_codes.put( "Liberia1964", new Integer( 4251 ) );
        geographic_cs_type_codes.put( "Lome", new Integer( 4252 ) );
        geographic_cs_type_codes.put( "Luzon_1911", new Integer( 4253 ) );
        geographic_cs_type_codes.put( "Luzon 1911", new Integer( 4253 ) );
        geographic_cs_type_codes.put( "Luzon1911", new Integer( 4253 ) );
        geographic_cs_type_codes.put( "Hito_XVIII_1963", new Integer( 4254 ) );
        geographic_cs_type_codes.put( "Hito XVIII 1963", new Integer( 4254 ) );
        geographic_cs_type_codes.put( "HitoXVIII1963", new Integer( 4254 ) );
        geographic_cs_type_codes.put( "Herat_North", new Integer( 4255 ) );
        geographic_cs_type_codes.put( "Herat North", new Integer( 4255 ) );
        geographic_cs_type_codes.put( "HeratNorth", new Integer( 4255 ) );
        geographic_cs_type_codes.put( "Mahe_1971", new Integer( 4256 ) );
        geographic_cs_type_codes.put( "Mahe 1971", new Integer( 4256 ) );
        geographic_cs_type_codes.put( "Mahe1971", new Integer( 4256 ) );
        geographic_cs_type_codes.put( "Makassar", new Integer( 4257 ) );
        geographic_cs_type_codes.put( "EUREF89", new Integer( 4258 ) );
        geographic_cs_type_codes.put( "Malongo_1987", new Integer( 4259 ) );
        geographic_cs_type_codes.put( "Malongo 1987", new Integer( 4259 ) );
        geographic_cs_type_codes.put( "Malongo1987", new Integer( 4259 ) );
        geographic_cs_type_codes.put( "Manoca", new Integer( 4260 ) );
        geographic_cs_type_codes.put( "Merchich", new Integer( 4261 ) );
        geographic_cs_type_codes.put( "Massawa", new Integer( 4262 ) );
        geographic_cs_type_codes.put( "Minna", new Integer( 4263 ) );
        geographic_cs_type_codes.put( "Mhast", new Integer( 4264 ) );
        geographic_cs_type_codes.put( "Monte_Mario", new Integer( 4265 ) );
        geographic_cs_type_codes.put( "Monte Mario", new Integer( 4265 ) );
        geographic_cs_type_codes.put( "MonteMario", new Integer( 4265 ) );
        geographic_cs_type_codes.put( "M_poraloko", new Integer( 4266 ) );
        geographic_cs_type_codes.put( "M poraloko", new Integer( 4266 ) );
        geographic_cs_type_codes.put( "Mporaloko", new Integer( 4266 ) );
        geographic_cs_type_codes.put( "NAD27", new Integer( 4267 ) );
        geographic_cs_type_codes.put( "NAD_Michigan", new Integer( 4268 ) );
        geographic_cs_type_codes.put( "NAD Michigan", new Integer( 4268 ) );
        geographic_cs_type_codes.put( "NADMichigan", new Integer( 4268 ) );
        geographic_cs_type_codes.put( "NAD83", new Integer( 4269 ) );
        geographic_cs_type_codes.put( "Nahrwan_1967", new Integer( 4270 ) );
        geographic_cs_type_codes.put( "Nahrwan 1967", new Integer( 4270 ) );
        geographic_cs_type_codes.put( "Nahrwan1967", new Integer( 4270 ) );
        geographic_cs_type_codes.put( "Naparima_1972", new Integer( 4271 ) );
        geographic_cs_type_codes.put( "Naparima 1972", new Integer( 4271 ) );
        geographic_cs_type_codes.put( "Naparima1972", new Integer( 4271 ) );
        geographic_cs_type_codes.put( "GD49", new Integer( 4272 ) );
        geographic_cs_type_codes.put( "NGO_1948", new Integer( 4273 ) );
        geographic_cs_type_codes.put( "NGO 1948", new Integer( 4273 ) );
        geographic_cs_type_codes.put( "NGO1948", new Integer( 4273 ) );
        geographic_cs_type_codes.put( "Datum_73", new Integer( 4274 ) );
        geographic_cs_type_codes.put( "Datum 73", new Integer( 4274 ) );
        geographic_cs_type_codes.put( "Datum73", new Integer( 4274 ) );
        geographic_cs_type_codes.put( "NTF", new Integer( 4275 ) );
        geographic_cs_type_codes.put( "NSWC_9Z_2", new Integer( 4276 ) );
        geographic_cs_type_codes.put( "NSWC 9Z 2", new Integer( 4276 ) );
        geographic_cs_type_codes.put( "NSWC9Z2", new Integer( 4276 ) );
        geographic_cs_type_codes.put( "OSGB_1936", new Integer( 4277 ) );
        geographic_cs_type_codes.put( "OSGB 1936", new Integer( 4277 ) );
        geographic_cs_type_codes.put( "OSGB1936", new Integer( 4277 ) );
        geographic_cs_type_codes.put( "OSGB70", new Integer( 4278 ) );
        geographic_cs_type_codes.put( "OS_SN80", new Integer( 4279 ) );
        geographic_cs_type_codes.put( "OS SN80", new Integer( 4279 ) );
        geographic_cs_type_codes.put( "OSSN80", new Integer( 4279 ) );
        geographic_cs_type_codes.put( "Padang", new Integer( 4280 ) );
        geographic_cs_type_codes.put( "Palestine_1923", new Integer( 4281 ) );
        geographic_cs_type_codes.put( "Palestine 1923", new Integer( 4281 ) );
        geographic_cs_type_codes.put( "Palestine1923", new Integer( 4281 ) );
        geographic_cs_type_codes.put( "Pointe_Noire", new Integer( 4282 ) );
        geographic_cs_type_codes.put( "Pointe Noire", new Integer( 4282 ) );
        geographic_cs_type_codes.put( "PointeNoire", new Integer( 4282 ) );
        geographic_cs_type_codes.put( "GDA94", new Integer( 4283 ) );
        geographic_cs_type_codes.put( "Pulkovo_1942", new Integer( 4284 ) );
        geographic_cs_type_codes.put( "Pulkovo 1942", new Integer( 4284 ) );
        geographic_cs_type_codes.put( "Pulkovo1942", new Integer( 4284 ) );
        geographic_cs_type_codes.put( "Qatar", new Integer( 4285 ) );
        geographic_cs_type_codes.put( "Qatar_1948", new Integer( 4286 ) );
        geographic_cs_type_codes.put( "Qatar 1948", new Integer( 4286 ) );
        geographic_cs_type_codes.put( "Qatar1948", new Integer( 4286 ) );
        geographic_cs_type_codes.put( "Qornoq", new Integer( 4287 ) );
        geographic_cs_type_codes.put( "Loma_Quintana", new Integer( 4288 ) );
        geographic_cs_type_codes.put( "Loma Quintana", new Integer( 4288 ) );
        geographic_cs_type_codes.put( "LomaQuintana", new Integer( 4288 ) );
        geographic_cs_type_codes.put( "Amersfoort", new Integer( 4289 ) );
        geographic_cs_type_codes.put( "RT38", new Integer( 4290 ) );
        geographic_cs_type_codes.put( "SAD69", new Integer( 4291 ) );
        geographic_cs_type_codes.put( "Sapper_Hill_1943", new Integer( 4292 ) );
        geographic_cs_type_codes.put( "Sapper Hill 1943", new Integer( 4292 ) );
        geographic_cs_type_codes.put( "SapperHill1943", new Integer( 4292 ) );
        geographic_cs_type_codes.put( "Schwarzeck", new Integer( 4293 ) );
        geographic_cs_type_codes.put( "Segora", new Integer( 4294 ) );
        geographic_cs_type_codes.put( "Serindung", new Integer( 4295 ) );
        geographic_cs_type_codes.put( "Sudan", new Integer( 4296 ) );
        geographic_cs_type_codes.put( "Tananarive", new Integer( 4297 ) );
        geographic_cs_type_codes.put( "Timbalai_1948", new Integer( 4298 ) );
        geographic_cs_type_codes.put( "Timbalai 1948", new Integer( 4298 ) );
        geographic_cs_type_codes.put( "Timbalai1948", new Integer( 4298 ) );
        geographic_cs_type_codes.put( "TM65", new Integer( 4299 ) );
        geographic_cs_type_codes.put( "TM75", new Integer( 4300 ) );
        geographic_cs_type_codes.put( "Tokyo", new Integer( 4301 ) );
        geographic_cs_type_codes.put( "Trinidad_1903", new Integer( 4302 ) );
        geographic_cs_type_codes.put( "Trinidad 1903", new Integer( 4302 ) );
        geographic_cs_type_codes.put( "Trinidad1903", new Integer( 4302 ) );
        geographic_cs_type_codes.put( "TC_1948", new Integer( 4303 ) );
        geographic_cs_type_codes.put( "TC 1948", new Integer( 4303 ) );
        geographic_cs_type_codes.put( "TC1948", new Integer( 4303 ) );
        geographic_cs_type_codes.put( "Voirol_1875", new Integer( 4304 ) );
        geographic_cs_type_codes.put( "Voirol 1875", new Integer( 4304 ) );
        geographic_cs_type_codes.put( "Voirol1875", new Integer( 4304 ) );
        geographic_cs_type_codes.put( "Voirol_Unifie", new Integer( 4305 ) );
        geographic_cs_type_codes.put( "Voirol Unifie", new Integer( 4305 ) );
        geographic_cs_type_codes.put( "VoirolUnifie", new Integer( 4305 ) );
        geographic_cs_type_codes.put( "Bern_1938", new Integer( 4306 ) );
        geographic_cs_type_codes.put( "Bern 1938", new Integer( 4306 ) );
        geographic_cs_type_codes.put( "Bern1938", new Integer( 4306 ) );
        geographic_cs_type_codes.put( "Nord_Sahara_1959", new Integer( 4307 ) );
        geographic_cs_type_codes.put( "Nord Sahara 1959", new Integer( 4307 ) );
        geographic_cs_type_codes.put( "NordSahara1959", new Integer( 4307 ) );
        geographic_cs_type_codes.put( "Stockholm_1938", new Integer( 4308 ) );
        geographic_cs_type_codes.put( "Stockholm 1938", new Integer( 4308 ) );
        geographic_cs_type_codes.put( "Stockholm1938", new Integer( 4308 ) );
        geographic_cs_type_codes.put( "Yacare", new Integer( 4309 ) );
        geographic_cs_type_codes.put( "Yoff", new Integer( 4310 ) );
        geographic_cs_type_codes.put( "Zanderij", new Integer( 4311 ) );
        geographic_cs_type_codes.put( "MGI", new Integer( 4312 ) );
        geographic_cs_type_codes.put( "Belge_1972", new Integer( 4313 ) );
        geographic_cs_type_codes.put( "Belge 1972", new Integer( 4313 ) );
        geographic_cs_type_codes.put( "Belge1972", new Integer( 4313 ) );
        geographic_cs_type_codes.put( "DHDN", new Integer( 4314 ) );
        geographic_cs_type_codes.put( "Conakry_1905", new Integer( 4315 ) );
        geographic_cs_type_codes.put( "Conakry 1905", new Integer( 4315 ) );
        geographic_cs_type_codes.put( "Conakry1905", new Integer( 4315 ) );
        geographic_cs_type_codes.put( "WGS_72", new Integer( 4322 ) );
        geographic_cs_type_codes.put( "WGS 72", new Integer( 4322 ) );
        geographic_cs_type_codes.put( "WGS72", new Integer( 4322 ) );
        geographic_cs_type_codes.put( "WGS_72BE", new Integer( 4324 ) );
        geographic_cs_type_codes.put( "WGS 72BE", new Integer( 4324 ) );
        geographic_cs_type_codes.put( "WGS72BE", new Integer( 4324 ) );
        geographic_cs_type_codes.put( "WGS_84", new Integer( 4326 ) );
        geographic_cs_type_codes.put( "WGS 84", new Integer( 4326 ) );
        geographic_cs_type_codes.put( "WGS84", new Integer( 4326 ) );
        geographic_cs_type_codes.put( "Bern_1898_Bern", new Integer( 4801 ) );
        geographic_cs_type_codes.put( "Bern 1898 Bern", new Integer( 4801 ) );
        geographic_cs_type_codes.put( "Bern1898Bern", new Integer( 4801 ) );
        geographic_cs_type_codes.put( "Bogota_Bogota", new Integer( 4802 ) );
        geographic_cs_type_codes.put( "Bogota Bogota", new Integer( 4802 ) );
        geographic_cs_type_codes.put( "BogotaBogota", new Integer( 4802 ) );
        geographic_cs_type_codes.put( "Lisbon_Lisbon", new Integer( 4803 ) );
        geographic_cs_type_codes.put( "Lisbon Lisbon", new Integer( 4803 ) );
        geographic_cs_type_codes.put( "LisbonLisbon", new Integer( 4803 ) );
        geographic_cs_type_codes.put( "Makassar_Jakarta", new Integer( 4804 ) );
        geographic_cs_type_codes.put( "Makassar_Jakarta", new Integer( 4804 ) );
        geographic_cs_type_codes.put( "MakassarJakarta", new Integer( 4804 ) );
        geographic_cs_type_codes.put( "MGI_Ferro", new Integer( 4805 ) );
        geographic_cs_type_codes.put( "Monte_Mario_Rome", new Integer( 4806 ) );
        geographic_cs_type_codes.put( "Monte Mario Rome", new Integer( 4806 ) );
        geographic_cs_type_codes.put( "MonteMarioRome", new Integer( 4806 ) );
        geographic_cs_type_codes.put( "NTF_Paris", new Integer( 4807 ) );
        geographic_cs_type_codes.put( "NTF Paris", new Integer( 4807 ) );
        geographic_cs_type_codes.put( "NTFParis", new Integer( 4807 ) );
        geographic_cs_type_codes.put( "Padang_Jakarta", new Integer( 4808 ) );
        geographic_cs_type_codes.put( "Padang Jakarta", new Integer( 4808 ) );
        geographic_cs_type_codes.put( "PadangJakarta", new Integer( 4808 ) );
        geographic_cs_type_codes.put( "Belge_1950_Brussels", new Integer( 4809 ) );
        geographic_cs_type_codes.put( "Belge 1950 Brussels", new Integer( 4809 ) );
        geographic_cs_type_codes.put( "Belge1950Brussels", new Integer( 4809 ) );
        geographic_cs_type_codes.put( "Tananarive_Paris", new Integer( 4810 ) );
        geographic_cs_type_codes.put( "Tananarive Paris", new Integer( 4810 ) );
        geographic_cs_type_codes.put( "TananariveParis", new Integer( 4810 ) );
        geographic_cs_type_codes.put( "Voirol_1875_Paris", new Integer( 4811 ) );
        geographic_cs_type_codes.put( "Voirol 1875 Paris", new Integer( 4811 ) );
        geographic_cs_type_codes.put( "Voirol1875Paris", new Integer( 4811 ) );
        geographic_cs_type_codes.put( "Voirol_Unifie_Paris", new Integer( 4812 ) );
        geographic_cs_type_codes.put( "Voirol Unifie Paris", new Integer( 4812 ) );
        geographic_cs_type_codes.put( "VoirolUnifieParis", new Integer( 4812 ) );
        geographic_cs_type_codes.put( "Batavia_Jakarta", new Integer( 4813 ) );
        geographic_cs_type_codes.put( "Batavia Jakarta", new Integer( 4813 ) );
        geographic_cs_type_codes.put( "BataviaJakarta", new Integer( 4813 ) );
        geographic_cs_type_codes.put( "ATF_Paris", new Integer( 4901 ) );
        geographic_cs_type_codes.put( "ATF Paris", new Integer( 4901 ) );
        geographic_cs_type_codes.put( "ATFParis", new Integer( 4901 ) );
        geographic_cs_type_codes.put( "NDG_Paris", new Integer( 4902 ) );
        geographic_cs_type_codes.put( "NDG Paris", new Integer( 4902 ) );
        geographic_cs_type_codes.put( "NDGParis", new Integer( 4902 ) );

        // Ellipsoid-Only GCS:
        ellipsoid_only_GCS.put( "Airy1830", new Integer( 4001 ) );
        ellipsoid_only_GCS.put( "AiryModified1849", new Integer( 4002 ) );
        ellipsoid_only_GCS.put( "AustralianNationalSpheroid", new Integer( 4003 ) );
        ellipsoid_only_GCS.put( "Bessel1841", new Integer( 4004 ) );
        ellipsoid_only_GCS.put( "BesselModified", new Integer( 4005 ) );
        ellipsoid_only_GCS.put( "BesselNamibia", new Integer( 4006 ) );
        ellipsoid_only_GCS.put( "Clarke1858", new Integer( 4007 ) );
        ellipsoid_only_GCS.put( "Clarke1866", new Integer( 4008 ) );
        ellipsoid_only_GCS.put( "Clarke1866Michigan", new Integer( 4009 ) );
        ellipsoid_only_GCS.put( "Clarke1880_Benoit", new Integer( 4010 ) );
        ellipsoid_only_GCS.put( "Clarke1880 Benoit", new Integer( 4010 ) );
        ellipsoid_only_GCS.put( "Clarke1880Benoit", new Integer( 4010 ) );
        ellipsoid_only_GCS.put( "Clarke1880_IGN", new Integer( 4011 ) );
        ellipsoid_only_GCS.put( "Clarke1880 IGN", new Integer( 4011 ) );
        ellipsoid_only_GCS.put( "Clarke1880IGN", new Integer( 4011 ) );
        ellipsoid_only_GCS.put( "Clarke1880_RGS", new Integer( 4012 ) );
        ellipsoid_only_GCS.put( "Clarke1880 RGS", new Integer( 4012 ) );
        ellipsoid_only_GCS.put( "Clarke1880RGS", new Integer( 4012 ) );
        ellipsoid_only_GCS.put( "Clarke1880_Arc", new Integer( 4013 ) );
        ellipsoid_only_GCS.put( "Clarke1880 Arc", new Integer( 4013 ) );
        ellipsoid_only_GCS.put( "Clarke1880Arc", new Integer( 4013 ) );
        ellipsoid_only_GCS.put( "Clarke1880_SGA1922", new Integer( 4014 ) );
        ellipsoid_only_GCS.put( "Clarke1880 SGA1922", new Integer( 4014 ) );
        ellipsoid_only_GCS.put( "Clarke1880SGA1922", new Integer( 4014 ) );
        ellipsoid_only_GCS.put( "Everest1830_1937Adjustment", new Integer( 4015 ) );
        ellipsoid_only_GCS.put( "Everest1830 1937Adjustment", new Integer( 4015 ) );
        ellipsoid_only_GCS.put( "Everest18301937Adjustment", new Integer( 4015 ) );
        ellipsoid_only_GCS.put( "Everest1830_1967Definition", new Integer( 4016 ) );
        ellipsoid_only_GCS.put( "Everest1830 1967Definition", new Integer( 4016 ) );
        ellipsoid_only_GCS.put( "Everest18301967Definition", new Integer( 4016 ) );
        ellipsoid_only_GCS.put( "Everest1830_1975Definition", new Integer( 4017 ) );
        ellipsoid_only_GCS.put( "Everest1830 1975Definition", new Integer( 4017 ) );
        ellipsoid_only_GCS.put( "Everest18301975Definition", new Integer( 4017 ) );
        ellipsoid_only_GCS.put( "Everest1830Modified", new Integer( 4018 ) );
        ellipsoid_only_GCS.put( "GRS1980", new Integer( 4019 ) );
        ellipsoid_only_GCS.put( "Helmert1906", new Integer( 4020 ) );
        ellipsoid_only_GCS.put( "IndonesianNationalSpheroid", new Integer( 4021 ) );
        ellipsoid_only_GCS.put( "International1924", new Integer( 4022 ) );
        ellipsoid_only_GCS.put( "International1967", new Integer( 4023 ) );
        ellipsoid_only_GCS.put( "Krassowsky1940", new Integer( 4024 ) );
        ellipsoid_only_GCS.put( "NWL9D", new Integer( 4025 ) );
        ellipsoid_only_GCS.put( "NWL10D", new Integer( 4026 ) );
        ellipsoid_only_GCS.put( "Plessis1817", new Integer( 4027 ) );
        ellipsoid_only_GCS.put( "Struve1860", new Integer( 4028 ) );
        ellipsoid_only_GCS.put( "WarOffice", new Integer( 4029 ) );
        ellipsoid_only_GCS.put( "WGS84", new Integer( 4030 ) );
        ellipsoid_only_GCS.put( "GEM10C", new Integer( 4031 ) );
        ellipsoid_only_GCS.put( "OSU86F", new Integer( 4032 ) );
        ellipsoid_only_GCS.put( "OSU91A", new Integer( 4033 ) );
        ellipsoid_only_GCS.put( "Clarke1880", new Integer( 4034 ) );
        ellipsoid_only_GCS.put( "Sphere", new Integer( 4035 ) );

        // Geodetic Datum Codes
        geodectic_datum_code.put( "Adindan", new Integer( 6201 ) );
        geodectic_datum_code.put( "Australian_Geodetic_Datum_1966", new Integer( 6202 ) );
        geodectic_datum_code.put( "Australian Geodetic Datum 1966", new Integer( 6202 ) );
        geodectic_datum_code.put( "AustralianGeodeticDatum1966", new Integer( 6202 ) );
        geodectic_datum_code.put( "Australian_Geodetic_Datum_1984", new Integer( 6203 ) );
        geodectic_datum_code.put( "Australian Geodetic Datum 1984", new Integer( 6203 ) );
        geodectic_datum_code.put( "AustralianGeodeticDatum1984", new Integer( 6203 ) );
        geodectic_datum_code.put( "Ain_el_Abd_1970", new Integer( 6204 ) );
        geodectic_datum_code.put( "Ain el Abd 1970", new Integer( 6204 ) );
        geodectic_datum_code.put( "AinelAbd1970", new Integer( 6204 ) );
        geodectic_datum_code.put( "Afgooye", new Integer( 6205 ) );
        geodectic_datum_code.put( "Agadez", new Integer( 6206 ) );
        geodectic_datum_code.put( "Lisbon", new Integer( 6207 ) );
        geodectic_datum_code.put( "Aratu", new Integer( 6208 ) );
        geodectic_datum_code.put( "Arc_1950", new Integer( 6209 ) );
        geodectic_datum_code.put( "Arc1950", new Integer( 6209 ) );
        geodectic_datum_code.put( "Arc1950", new Integer( 6209 ) );
        geodectic_datum_code.put( "Arc_1960", new Integer( 6210 ) );
        geodectic_datum_code.put( "Arc 1960", new Integer( 6210 ) );
        geodectic_datum_code.put( "Arc1960", new Integer( 6210 ) );
        geodectic_datum_code.put( "Batavia", new Integer( 6211 ) );
        geodectic_datum_code.put( "Barbados", new Integer( 6212 ) );
        geodectic_datum_code.put( "Beduaram", new Integer( 6213 ) );
        geodectic_datum_code.put( "Beijing_1954", new Integer( 6214 ) );
        geodectic_datum_code.put( "Beijing 1954", new Integer( 6214 ) );
        geodectic_datum_code.put( "Beijing1954", new Integer( 6214 ) );
        geodectic_datum_code.put( "Reseau_National_Belge_1950", new Integer( 6215 ) );
        geodectic_datum_code.put( "Reseau National Belge 1950", new Integer( 6215 ) );
        geodectic_datum_code.put( "ReseauNationalBelge1950", new Integer( 6215 ) );
        geodectic_datum_code.put( "Bermuda_1957", new Integer( 6216 ) );
        geodectic_datum_code.put( "Bermuda 1957", new Integer( 6216 ) );
        geodectic_datum_code.put( "Bermuda1957", new Integer( 6216 ) );
        geodectic_datum_code.put( "Bern_1898", new Integer( 6217 ) );
        geodectic_datum_code.put( "Bern 1898", new Integer( 6217 ) );
        geodectic_datum_code.put( "Bern1898", new Integer( 6217 ) );
        geodectic_datum_code.put( "Bogota", new Integer( 6218 ) );
        geodectic_datum_code.put( "Bukit_Rimpah", new Integer( 6219 ) );
        geodectic_datum_code.put( "Bukit Rimpah", new Integer( 6219 ) );
        geodectic_datum_code.put( "BukitRimpah", new Integer( 6219 ) );
        geodectic_datum_code.put( "Camacupa", new Integer( 6220 ) );
        geodectic_datum_code.put( "Campo_Inchauspe", new Integer( 6221 ) );
        geodectic_datum_code.put( "Campo Inchauspe", new Integer( 6221 ) );
        geodectic_datum_code.put( "CampoInchauspe", new Integer( 6221 ) );
        geodectic_datum_code.put( "Cape", new Integer( 6222 ) );
        geodectic_datum_code.put( "Carthage", new Integer( 6223 ) );
        geodectic_datum_code.put( "Chua", new Integer( 6224 ) );
        geodectic_datum_code.put( "Corrego_Alegre", new Integer( 6225 ) );
        geodectic_datum_code.put( "Corrego Alegre", new Integer( 6225 ) );
        geodectic_datum_code.put( "CorregoAlegre", new Integer( 6225 ) );
        geodectic_datum_code.put( "Cote_d_Ivoire", new Integer( 6226 ) );
        geodectic_datum_code.put( "Cote d Ivoire", new Integer( 6226 ) );
        geodectic_datum_code.put( "CotedIvoire", new Integer( 6226 ) );
        geodectic_datum_code.put( "Deir_ez_Zor", new Integer( 6227 ) );
        geodectic_datum_code.put( "Deir ez Zor", new Integer( 6227 ) );
        geodectic_datum_code.put( "DeirezZor", new Integer( 6227 ) );
        geodectic_datum_code.put( "Douala", new Integer( 6228 ) );
        geodectic_datum_code.put( "Egypt_1907", new Integer( 6229 ) );
        geodectic_datum_code.put( "Egypt 1907", new Integer( 6229 ) );
        geodectic_datum_code.put( "Egypt1907", new Integer( 6229 ) );
        geodectic_datum_code.put( "European_Datum_1950", new Integer( 6230 ) );
        geodectic_datum_code.put( "European Datum 1950", new Integer( 6230 ) );
        geodectic_datum_code.put( "EuropeanDatum1950", new Integer( 6230 ) );
        geodectic_datum_code.put( "European_Datum_1987", new Integer( 6231 ) );
        geodectic_datum_code.put( "European Datum 1987", new Integer( 6231 ) );
        geodectic_datum_code.put( "EuropeanDatum1987", new Integer( 6231 ) );
        geodectic_datum_code.put( "Fahud", new Integer( 6232 ) );
        geodectic_datum_code.put( "Gandajika_1970", new Integer( 6233 ) );
        geodectic_datum_code.put( "Gandajika 1970", new Integer( 6233 ) );
        geodectic_datum_code.put( "Gandajika1970", new Integer( 6233 ) );
        geodectic_datum_code.put( "Garoua", new Integer( 6234 ) );
        geodectic_datum_code.put( "Guyane_Francaise", new Integer( 6235 ) );
        geodectic_datum_code.put( "Guyane Francaise", new Integer( 6235 ) );
        geodectic_datum_code.put( "GuyaneFrancaise", new Integer( 6235 ) );
        geodectic_datum_code.put( "Hu_Tzu_Shan", new Integer( 6236 ) );
        geodectic_datum_code.put( "Hu Tzu Shan", new Integer( 6236 ) );
        geodectic_datum_code.put( "HuTzuShan", new Integer( 6236 ) );
        geodectic_datum_code.put( "Hungarian_Datum_1972", new Integer( 6237 ) );
        geodectic_datum_code.put( "Hungarian Datum 1972", new Integer( 6237 ) );
        geodectic_datum_code.put( "HungarianDatum1972", new Integer( 6237 ) );
        geodectic_datum_code.put( "Indonesian_Datum_1974", new Integer( 6238 ) );
        geodectic_datum_code.put( "Indonesian Datum 1974", new Integer( 6238 ) );
        geodectic_datum_code.put( "IndonesianDatum1974", new Integer( 6238 ) );
        geodectic_datum_code.put( "Indian_1954", new Integer( 6239 ) );
        geodectic_datum_code.put( "Indian 1954", new Integer( 6239 ) );
        geodectic_datum_code.put( "Indian1954", new Integer( 6239 ) );
        geodectic_datum_code.put( "Indian_1975", new Integer( 6240 ) );
        geodectic_datum_code.put( "Indian 1975", new Integer( 6240 ) );
        geodectic_datum_code.put( "Indian1975", new Integer( 6240 ) );
        geodectic_datum_code.put( "Jamaica_1875", new Integer( 6241 ) );
        geodectic_datum_code.put( "Jamaica 1875", new Integer( 6241 ) );
        geodectic_datum_code.put( "Jamaica1875", new Integer( 6241 ) );
        geodectic_datum_code.put( "Jamaica_1969", new Integer( 6242 ) );
        geodectic_datum_code.put( "Jamaica 1969", new Integer( 6242 ) );
        geodectic_datum_code.put( "Jamaica1969", new Integer( 6242 ) );
        geodectic_datum_code.put( "Kalianpur", new Integer( 6243 ) );
        geodectic_datum_code.put( "Kandawala", new Integer( 6244 ) );
        geodectic_datum_code.put( "Kertau", new Integer( 6245 ) );
        geodectic_datum_code.put( "Kuwait_Oil_Company", new Integer( 6246 ) );
        geodectic_datum_code.put( "Kuwait Oil Company", new Integer( 6246 ) );
        geodectic_datum_code.put( "KuwaitOilCompany", new Integer( 6246 ) );
        geodectic_datum_code.put( "La_Canoa", new Integer( 6247 ) );
        geodectic_datum_code.put( "La Canoa", new Integer( 6247 ) );
        geodectic_datum_code.put( "LaCanoa", new Integer( 6247 ) );
        geodectic_datum_code.put( "Provisional_S_American_Datum_1956", new Integer( 6248 ) );
        geodectic_datum_code.put( "Provisional S American Datum 1956", new Integer( 6248 ) );
        geodectic_datum_code.put( "ProvisionalSAmericanDatum1956", new Integer( 6248 ) );
        geodectic_datum_code.put( "Lake", new Integer( 6249 ) );
        geodectic_datum_code.put( "Leigon", new Integer( 6250 ) );
        geodectic_datum_code.put( "Liberia_1964", new Integer( 6251 ) );
        geodectic_datum_code.put( "Liberia 1964", new Integer( 6251 ) );
        geodectic_datum_code.put( "Liberia1964", new Integer( 6251 ) );
        geodectic_datum_code.put( "Lome", new Integer( 6252 ) );
        geodectic_datum_code.put( "Luzon_1911", new Integer( 6253 ) );
        geodectic_datum_code.put( "Luzon 1911", new Integer( 6253 ) );
        geodectic_datum_code.put( "Luzon1911", new Integer( 6253 ) );
        geodectic_datum_code.put( "Hito_XVIII_1963", new Integer( 6254 ) );
        geodectic_datum_code.put( "Hito XVIII 1963", new Integer( 6254 ) );
        geodectic_datum_code.put( "HitoXVIII1963", new Integer( 6254 ) );
        geodectic_datum_code.put( "Herat_North", new Integer( 6255 ) );
        geodectic_datum_code.put( "Herat North", new Integer( 6255 ) );
        geodectic_datum_code.put( "HeratNorth", new Integer( 6255 ) );
        geodectic_datum_code.put( "Mahe_1971", new Integer( 6256 ) );
        geodectic_datum_code.put( "Mahe 1971", new Integer( 6256 ) );
        geodectic_datum_code.put( "Mahe1971", new Integer( 6256 ) );
        geodectic_datum_code.put( "Makassar", new Integer( 6257 ) );
        geodectic_datum_code.put( "European_Reference_System_1989", new Integer( 6258 ) );
        geodectic_datum_code.put( "European Reference System 1989", new Integer( 6258 ) );
        geodectic_datum_code.put( "EuropeanReferenceSystem1989", new Integer( 6258 ) );
        geodectic_datum_code.put( "Malongo_1987", new Integer( 6259 ) );
        geodectic_datum_code.put( "Malongo 1987", new Integer( 6259 ) );
        geodectic_datum_code.put( "Malongo1987", new Integer( 6259 ) );
        geodectic_datum_code.put( "Manoca", new Integer( 6260 ) );
        geodectic_datum_code.put( "Merchich", new Integer( 6261 ) );
        geodectic_datum_code.put( "Massawa", new Integer( 6262 ) );
        geodectic_datum_code.put( "Minna", new Integer( 6263 ) );
        geodectic_datum_code.put( "Mhast", new Integer( 6264 ) );
        geodectic_datum_code.put( "Monte_Mario", new Integer( 6265 ) );
        geodectic_datum_code.put( "Monte Mario", new Integer( 6265 ) );
        geodectic_datum_code.put( "MonteMario", new Integer( 6265 ) );
        geodectic_datum_code.put( "M_poraloko", new Integer( 6266 ) );
        geodectic_datum_code.put( "M poraloko", new Integer( 6266 ) );
        geodectic_datum_code.put( "Mporaloko", new Integer( 6266 ) );
        geodectic_datum_code.put( "North_American_Datum_1927", new Integer( 6267 ) );
        geodectic_datum_code.put( "North American Datum 1927", new Integer( 6267 ) );
        geodectic_datum_code.put( "NorthAmericanDatum1927", new Integer( 6267 ) );
        geodectic_datum_code.put( "NAD_Michigan", new Integer( 6268 ) );
        geodectic_datum_code.put( "NAD Michigan", new Integer( 6268 ) );
        geodectic_datum_code.put( "NADMichigan", new Integer( 6268 ) );
        geodectic_datum_code.put( "North_American_Datum_1983", new Integer( 6269 ) );
        geodectic_datum_code.put( "North American Datum 1983", new Integer( 6269 ) );
        geodectic_datum_code.put( "NorthAmericanDatum1983", new Integer( 6269 ) );
        geodectic_datum_code.put( "Nahrwan_1967", new Integer( 6270 ) );
        geodectic_datum_code.put( "Nahrwan 1967", new Integer( 6270 ) );
        geodectic_datum_code.put( "Nahrwan1967", new Integer( 6270 ) );
        geodectic_datum_code.put( "Naparima_1972", new Integer( 6271 ) );
        geodectic_datum_code.put( "Naparima 1972", new Integer( 6271 ) );
        geodectic_datum_code.put( "Naparima1972", new Integer( 6271 ) );
        geodectic_datum_code.put( "New_Zealand_Geodetic_Datum_1949", new Integer( 6272 ) );
        geodectic_datum_code.put( "New Zealand Geodetic Datum 1949", new Integer( 6272 ) );
        geodectic_datum_code.put( "NewZealandGeodeticDatum1949", new Integer( 6272 ) );
        geodectic_datum_code.put( "NGO_1948", new Integer( 6273 ) );
        geodectic_datum_code.put( "NGO 1948", new Integer( 6273 ) );
        geodectic_datum_code.put( "NGO1948", new Integer( 6273 ) );
        geodectic_datum_code.put( "Datum_73", new Integer( 6274 ) );
        geodectic_datum_code.put( "Datum 73", new Integer( 6274 ) );
        geodectic_datum_code.put( "Datum73", new Integer( 6274 ) );
        geodectic_datum_code.put( "Nouvelle_Triangulation_Francaise", new Integer( 6275 ) );
        geodectic_datum_code.put( "Nouvelle Triangulation Francaise", new Integer( 6275 ) );
        geodectic_datum_code.put( "NouvelleTriangulationFrancaise", new Integer( 6275 ) );
        geodectic_datum_code.put( "NSWC_9Z_2", new Integer( 6276 ) );
        geodectic_datum_code.put( "NSWC 9Z 2", new Integer( 6276 ) );
        geodectic_datum_code.put( "NSWC9Z2", new Integer( 6276 ) );
        geodectic_datum_code.put( "OSGB_1936", new Integer( 6277 ) );
        geodectic_datum_code.put( "OSGB 1936", new Integer( 6277 ) );
        geodectic_datum_code.put( "OSGB1936", new Integer( 6277 ) );
        geodectic_datum_code.put( "OSGB_1970_SN", new Integer( 6278 ) );
        geodectic_datum_code.put( "OSGB 1970 SN", new Integer( 6278 ) );
        geodectic_datum_code.put( "OSGB1970SN", new Integer( 6278 ) );
        geodectic_datum_code.put( "OS_SN_1980", new Integer( 6279 ) );
        geodectic_datum_code.put( "OS SN 1980", new Integer( 6279 ) );
        geodectic_datum_code.put( "OSSN1980", new Integer( 6279 ) );
        geodectic_datum_code.put( "Padang_1884", new Integer( 6280 ) );
        geodectic_datum_code.put( "Padang 1884", new Integer( 6280 ) );
        geodectic_datum_code.put( "Padang1884", new Integer( 6280 ) );
        geodectic_datum_code.put( "Palestine_1923", new Integer( 6281 ) );
        geodectic_datum_code.put( "Palestine 1923", new Integer( 6281 ) );
        geodectic_datum_code.put( "Palestine1923", new Integer( 6281 ) );
        geodectic_datum_code.put( "Pointe_Noire", new Integer( 6282 ) );
        geodectic_datum_code.put( "Pointe Noire", new Integer( 6282 ) );
        geodectic_datum_code.put( "PointeNoire", new Integer( 6282 ) );
        geodectic_datum_code.put( "Geocentric_Datum_of_Australia_1994", new Integer( 6283 ) );
        geodectic_datum_code.put( "Geocentric Datum of Australia 1994", new Integer( 6283 ) );
        geodectic_datum_code.put( "GeocentricDatumofAustralia1994", new Integer( 6283 ) );
        geodectic_datum_code.put( "Pulkovo_1942", new Integer( 6284 ) );
        geodectic_datum_code.put( "Pulkovo 1942", new Integer( 6284 ) );
        geodectic_datum_code.put( "Pulkovo1942", new Integer( 6284 ) );
        geodectic_datum_code.put( "Qatar", new Integer( 6285 ) );
        geodectic_datum_code.put( "Qatar_1948", new Integer( 6286 ) );
        geodectic_datum_code.put( "Qatar 1948", new Integer( 6286 ) );
        geodectic_datum_code.put( "Qatar1948", new Integer( 6286 ) );
        geodectic_datum_code.put( "Qornoq", new Integer( 6287 ) );
        geodectic_datum_code.put( "Loma_Quintana", new Integer( 6288 ) );
        geodectic_datum_code.put( "Loma Quintana", new Integer( 6288 ) );
        geodectic_datum_code.put( "LomaQuintana", new Integer( 6288 ) );
        geodectic_datum_code.put( "Amersfoort", new Integer( 6289 ) );
        geodectic_datum_code.put( "RT38", new Integer( 6290 ) );
        geodectic_datum_code.put( "South_American_Datum_1969", new Integer( 6291 ) );
        geodectic_datum_code.put( "South American Datum 1969", new Integer( 6291 ) );
        geodectic_datum_code.put( "SouthAmericanDatum1969", new Integer( 6291 ) );
        geodectic_datum_code.put( "Sapper_Hill_1943", new Integer( 6292 ) );
        geodectic_datum_code.put( "Sapper Hill 1943", new Integer( 6292 ) );
        geodectic_datum_code.put( "SapperHill1943", new Integer( 6292 ) );
        geodectic_datum_code.put( "Schwarzeck", new Integer( 6293 ) );
        geodectic_datum_code.put( "Segora", new Integer( 6294 ) );
        geodectic_datum_code.put( "Serindung", new Integer( 6295 ) );
        geodectic_datum_code.put( "Sudan", new Integer( 6296 ) );
        geodectic_datum_code.put( "Tananarive_1925", new Integer( 6297 ) );
        geodectic_datum_code.put( "Tananarive 1925", new Integer( 6297 ) );
        geodectic_datum_code.put( "Tananarive1925", new Integer( 6297 ) );
        geodectic_datum_code.put( "Timbalai_1948", new Integer( 6298 ) );
        geodectic_datum_code.put( "Timbalai 1948", new Integer( 6298 ) );
        geodectic_datum_code.put( "Timbalai1948", new Integer( 6298 ) );
        geodectic_datum_code.put( "TM65", new Integer( 6299 ) );
        geodectic_datum_code.put( "TM75", new Integer( 6300 ) );
        geodectic_datum_code.put( "Tokyo", new Integer( 6301 ) );
        geodectic_datum_code.put( "Trinidad_1903", new Integer( 6302 ) );
        geodectic_datum_code.put( "Trinidad 1903", new Integer( 6302 ) );
        geodectic_datum_code.put( "Trinidad1903", new Integer( 6302 ) );
        geodectic_datum_code.put( "Trucial_Coast_1948", new Integer( 6303 ) );
        geodectic_datum_code.put( "Trucial Coast 1948", new Integer( 6303 ) );
        geodectic_datum_code.put( "TrucialCoast1948", new Integer( 6303 ) );
        geodectic_datum_code.put( "Voirol_1875", new Integer( 6304 ) );
        geodectic_datum_code.put( "Voirol 1875", new Integer( 6304 ) );
        geodectic_datum_code.put( "Voirol1875", new Integer( 6304 ) );
        geodectic_datum_code.put( "Voirol_Unifie_1960", new Integer( 6305 ) );
        geodectic_datum_code.put( "Voirol Unifie 1960", new Integer( 6305 ) );
        geodectic_datum_code.put( "VoirolUnifie1960", new Integer( 6305 ) );
        geodectic_datum_code.put( "Bern_1938", new Integer( 6306 ) );
        geodectic_datum_code.put( "Bern 1938", new Integer( 6306 ) );
        geodectic_datum_code.put( "Bern1938", new Integer( 6306 ) );
        geodectic_datum_code.put( "Nord_Sahara_1959", new Integer( 6307 ) );
        geodectic_datum_code.put( "Nord Sahara 1959", new Integer( 6307 ) );
        geodectic_datum_code.put( "NordSahara1959", new Integer( 6307 ) );
        geodectic_datum_code.put( "Stockholm_1938", new Integer( 6308 ) );
        geodectic_datum_code.put( "Stockholm 1938", new Integer( 6308 ) );
        geodectic_datum_code.put( "Stockholm_1938", new Integer( 6308 ) );
        geodectic_datum_code.put( "Yacare", new Integer( 6309 ) );
        geodectic_datum_code.put( "Yoff", new Integer( 6310 ) );
        geodectic_datum_code.put( "Zanderij", new Integer( 6311 ) );
        geodectic_datum_code.put( "Militar_Geographische_Institut", new Integer( 6312 ) );
        geodectic_datum_code.put( "Militar Geographische Institut", new Integer( 6312 ) );
        geodectic_datum_code.put( "MilitarGeographischeInstitut", new Integer( 6312 ) );
        geodectic_datum_code.put( "Reseau_National_Belge_1972", new Integer( 6313 ) );
        geodectic_datum_code.put( "Reseau National Belge 1972", new Integer( 6313 ) );
        geodectic_datum_code.put( "ReseauNationalBelge1972", new Integer( 6313 ) );
        geodectic_datum_code.put( "Deutsche_Hauptdreiecksnetz", new Integer( 6314 ) );
        geodectic_datum_code.put( "Deutsche Hauptdreiecksnetz", new Integer( 6314 ) );
        geodectic_datum_code.put( "DeutscheHauptdreiecksnetz", new Integer( 6314 ) );
        geodectic_datum_code.put( "Conakry_1905", new Integer( 6315 ) );
        geodectic_datum_code.put( "Conakry 1905", new Integer( 6315 ) );
        geodectic_datum_code.put( "Conakry1905", new Integer( 6315 ) );
        geodectic_datum_code.put( "WGS72", new Integer( 6322 ) );
        geodectic_datum_code.put( "WGS72_Transit_Broadcast_Ephemeris", new Integer( 6324 ) );
        geodectic_datum_code.put( "WGS72 Transit Broadcast Ephemeris", new Integer( 6324 ) );
        geodectic_datum_code.put( "WGS72_TransitBroadcastEphemeris", new Integer( 6324 ) );
        geodectic_datum_code.put( "WGS84", new Integer( 6326 ) );
        geodectic_datum_code.put( "Ancienne_Triangulation_Francaise", new Integer( 6901 ) );
        geodectic_datum_code.put( "Ancienne Triangulation Francaise", new Integer( 6901 ) );
        geodectic_datum_code.put( "AncienneTriangulationFrancaise", new Integer( 6901 ) );
        geodectic_datum_code.put( "Nord_de_Guerre", new Integer( 6902 ) );
        geodectic_datum_code.put( "Nord de Guerre", new Integer( 6902 ) );
        geodectic_datum_code.put( "NorddeGuerre", new Integer( 6902 ) );

        // Ellipsoid-Only Datum
        ellipsoid_only_datum.put( "Airy1830", new Integer( 6001 ) );
        ellipsoid_only_datum.put( "AiryModified1849", new Integer( 6002 ) );
        ellipsoid_only_datum.put( "AustralianNationalSpheroid", new Integer( 6003 ) );
        ellipsoid_only_datum.put( "Bessel1841", new Integer( 6004 ) );
        ellipsoid_only_datum.put( "BesselModified", new Integer( 6005 ) );
        ellipsoid_only_datum.put( "BesselNamibia", new Integer( 6006 ) );
        ellipsoid_only_datum.put( "Clarke1858", new Integer( 6007 ) );
        ellipsoid_only_datum.put( "Clarke1866", new Integer( 6008 ) );
        ellipsoid_only_datum.put( "Clarke1866Michigan", new Integer( 6009 ) );
        ellipsoid_only_datum.put( "Clarke1880_Benoit", new Integer( 6010 ) );
        ellipsoid_only_datum.put( "Clarke1880 Benoit", new Integer( 6010 ) );
        ellipsoid_only_datum.put( "Clarke1880Benoit", new Integer( 6010 ) );
        ellipsoid_only_datum.put( "Clarke1880_IGN", new Integer( 6011 ) );
        ellipsoid_only_datum.put( "Clarke1880 IGN", new Integer( 6011 ) );
        ellipsoid_only_datum.put( "Clarke1880IGN", new Integer( 6011 ) );
        ellipsoid_only_datum.put( "Clarke1880_RGS", new Integer( 6012 ) );
        ellipsoid_only_datum.put( "Clarke1880 RGS", new Integer( 6012 ) );
        ellipsoid_only_datum.put( "Clarke1880RGS", new Integer( 6012 ) );
        ellipsoid_only_datum.put( "Clarke1880_Arc", new Integer( 6013 ) );
        ellipsoid_only_datum.put( "Clarke1880 Arc", new Integer( 6013 ) );
        ellipsoid_only_datum.put( "Clarke1880Arc", new Integer( 6013 ) );
        ellipsoid_only_datum.put( "Clarke1880_SGA1922", new Integer( 6014 ) );
        ellipsoid_only_datum.put( "Clarke1880 SGA1922", new Integer( 6014 ) );
        ellipsoid_only_datum.put( "Clarke1880SGA1922", new Integer( 6014 ) );
        ellipsoid_only_datum.put( "Everest1830_1937Adjustment", new Integer( 6015 ) );
        ellipsoid_only_datum.put( "Everest1830 1937Adjustment", new Integer( 6015 ) );
        ellipsoid_only_datum.put( "Everest18301937Adjustment", new Integer( 6015 ) );
        ellipsoid_only_datum.put( "Everest1830_1967Definition", new Integer( 6016 ) );
        ellipsoid_only_datum.put( "Everest1830 1967Definition", new Integer( 6016 ) );
        ellipsoid_only_datum.put( "Everest18301967Definition", new Integer( 6016 ) );
        ellipsoid_only_datum.put( "Everest1830_1975Definition", new Integer( 6017 ) );
        ellipsoid_only_datum.put( "Everest1830 1975Definition", new Integer( 6017 ) );
        ellipsoid_only_datum.put( "Everest18301975Definition", new Integer( 6017 ) );
        ellipsoid_only_datum.put( "Everest1830Modified", new Integer( 6018 ) );
        ellipsoid_only_datum.put( "GRS1980", new Integer( 6019 ) );
        ellipsoid_only_datum.put( "Helmert1906", new Integer( 6020 ) );
        ellipsoid_only_datum.put( "IndonesianNationalSpheroid", new Integer( 6021 ) );
        ellipsoid_only_datum.put( "International1924", new Integer( 6022 ) );
        ellipsoid_only_datum.put( "International1967", new Integer( 6023 ) );
        ellipsoid_only_datum.put( "Krassowsky1960", new Integer( 6024 ) );
        ellipsoid_only_datum.put( "NWL9D", new Integer( 6025 ) );
        ellipsoid_only_datum.put( "NWL10D", new Integer( 6026 ) );
        ellipsoid_only_datum.put( "Plessis1817", new Integer( 6027 ) );
        ellipsoid_only_datum.put( "Struve1860", new Integer( 6028 ) );
        ellipsoid_only_datum.put( "WarOffice", new Integer( 6029 ) );
        ellipsoid_only_datum.put( "WGS84", new Integer( 6030 ) );
        ellipsoid_only_datum.put( "GEM10C", new Integer( 6031 ) );
        ellipsoid_only_datum.put( "OSU86F", new Integer( 6032 ) );
        ellipsoid_only_datum.put( "OSU91A", new Integer( 6033 ) );
        ellipsoid_only_datum.put( "Clarke1880", new Integer( 6034 ) );
        ellipsoid_only_datum.put( "Sphere", new Integer( 6035 ) );

    }

    /**
     * 
     * @param code
     * @return true if prime CS type code available
     */
    public static boolean containsGeographicCSTypeCode( String code ) {
        return geographic_cs_type_codes.containsKey( code );

    }

    /**
     * checks, if Code is found and returns the appropriate key-value.
     * 
     * @param code
     * @return type code
     */
    public static int getGeogrpahicCSTypeCode( String code ) {
        int value = -1;
        if ( geographic_cs_type_codes.containsKey( code ) ) {
            value = Geographic_CS_Codes.geographic_cs_type_codes.get( code );
        }
        return value;
    }

    /**
     * 
     * @param code
     * @return true if prime ellipse GCS available
     */
    public static boolean contains_Ellipsoid_Only_GCS( String code ) {
        return ellipsoid_only_GCS.containsKey( code );
    }

    /**
     * 
     * @param code
     * @return true if prime geodectic datum available
     */
    public static boolean contains_Geodectic_Datum_Code( String code ) {
        return geodectic_datum_code.containsKey( code );
    }

    /**
     * 
     * @param code
     * @return true if prime ellipse datum available
     */
    public static boolean contains_Ellipsoid_Only_Datum( String code ) {
        return ellipsoid_only_datum.containsKey( code );
    }

    /**
     * 
     * @param code
     * @return true if prime ellipse available
     */
    public static boolean contains_Ellipsoid_Codes( String code ) {
        return ellipsoid_codes.containsKey( code );
    }

    /**
     * 
     * @param code
     * @return true if prime meridian available
     */
    public static boolean contains_Prime_Meridian_Codes( String code ) {
        return prime_meridian_codes.containsKey( code );
    }

}
