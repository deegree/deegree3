--// inhaltliche indizes
CREATE INDEX idx_citation ON ci_citation(title, identifier, creationdate, revisiondate, publicationdate);
CREATE INDEX idx_publicationdate ON ci_citation(publicationdate);
CREATE INDEX idx_alternatetitle ON alternatetitle(alternatetitle);
CREATE INDEX idx_keyword ON keyword(keyword);
CREATE INDEX idx_identification ON md_identification(abstract, purpose);
CREATE INDEX idx_spatialreptypecode ON md_spatialreptypecode(codelistvalue);
CREATE INDEX idx_geogrbbox ON ex_geogrbbox(crs);
CREATE INDEX idx_format ON md_format(name);
CREATE INDEX idx_metadata ON md_metadata(language, parentidentifier, fk_hlevelname);
CREATE INDEX idx_respparty ON public.ci_respparty(organisationname);
CREATE INDEX idx_legalconstraints ON public.md_legalconstraints(defined);
CREATE INDEX idx_keywordtypecode ON public.md_keywordtypecode(codelistvalue);
CREATE INDEX idx_topiccategorycode ON public.md_topiccategorycode(category);
CREATE INDEX idx_geoobjtypecode ON public.md_geoobjtypecode(codelistvalue);
CREATE INDEX idx_temporalextent ON public.ex_temporalextent(begin_, end_);
CREATE INDEX idx_fileidentifier ON public.fileidentifier(fileidentifier);

--// indizes auf FKs
CREATE INDEX idx_fk_alternatetitle ON public.alternatetitle(fk_citation);
CREATE INDEX idx_fk_address ON ci_address(fk_deliverypoint, fk_email);
CREATE INDEX idx_fk_citation ON ci_citation(fk_fileidentifier, fk_presfromcode);
CREATE INDEX idx_fk_contact ON ci_contact(fk_address, fk_onlineresource);
CREATE INDEX idx_fk_onlineresource ON ci_onlineresource(fk_function);
CREATE INDEX idx_fk_respparty ON ci_respparty(fk_role, fk_contact);
CREATE INDEX idx_fk_series ON ci_series(fk_citation);
CREATE INDEX idx_fk_serviceidentification ON csw_serviceidentification(FK_LegalConst, FK_CouplingType,FK_SecConst);
CREATE INDEX idx_fk_dataquality ON dq_dataquality(fk_metadata);
CREATE INDEX idx_fk_element ON dq_element(fk_dataquality);
CREATE INDEX idx_fk_boundingpolygon ON ex_boundingpolygon(fk_dataident);
CREATE INDEX idx_fk_geographicdescription	ON ex_geographicdescription(geographicidentifiercode);
CREATE INDEX idx_fk_geogrbbox ON ex_geogrbbox(fk_owner);
CREATE INDEX idx_fk_temporalextent ON ex_temporalextent(fk_dataident);
CREATE INDEX idx_fk_verticalextent ON ex_verticalextent(fk_dataident);
CREATE INDEX idx_fk_facsimile ON facsimile(fk_contact);
CREATE INDEX idx_fk_featuretypes ON featuretypes(fk_featcatdesc);
CREATE INDEX idx_fk_citation_respparty ON jt_citation_respparty(fk_citation, fk_respparty);
CREATE INDEX idx_fk_dataident_spatialreptype ON jt_dataident_spatialreptype(fk_dataident, fk_spatialreptype);
CREATE INDEX idx_fk_dataident_topiccat ON jt_dataident_topiccat(fk_dataident, fk_topiccategory);
CREATE INDEX idx_fk_ident_keywords ON jt_ident_keywords(fk_ident, fk_keywords);
CREATE INDEX idx_fk_ident_mainten ON jt_ident_mainten(FK_Identification, FK_Maintenance);
CREATE INDEX idx_fk_ident_respparty ON jt_ident_respparty(fk_identification, fk_responsibleparty);
CREATE INDEX idx_fk_ident_usage ON jt_ident_usage(fk_ident, fk_usage);
CREATE INDEX idx_fk_keywords_keyword ON jt_keywords_keyword(fk_keywords, fk_keyword);
CREATE INDEX idx_fk_legalconst_accessconst ON jt_legalconst_accessconst(fk_legalconst, fk_restrictcode);
CREATE INDEX idx_fk_legalconst_useconst ON jt_legalconst_useconst(fk_legalconst, fk_restrictcode);
CREATE INDEX idx_fk_metadata_refsys ON jt_metadata_refsys(fk_metadata, fk_refsys);
CREATE INDEX idx_fk_metadata_respparty ON jt_metadata_respparty(fk_metadata, fk_respparty);
CREATE INDEX idx_fk_operation_dcp ON jt_operation_dcp(fk_operation, fk_dcp);
CREATE INDEX idx_fk_operation_name ON jt_operation_name(fk_operation, fk_name);
CREATE INDEX idx_fk_operation_operateson ON jt_operation_operateson(fk_operation, fk_operateson);
CREATE INDEX idx_fk_opmeta_onlineres ON jt_opmeta_onlineres(fk_operation, fk_onlineresource);
CREATE INDEX idx_fk_browsegraphic ON md_browsegraphic(fk_ident);
CREATE INDEX idx_fk_dataidentification ON md_dataidentification(fk_characterset);
CREATE INDEX idx_fk_digtransferopt ON md_digtransferopt(fk_distribution,fk_OnlineResource);
CREATE INDEX idx_fk_distributor ON md_distributor(fk_responsibleparty);
CREATE INDEX idx_fk_featcatdesc ON md_featcatdesc(fk_citation);
CREATE INDEX idx_fk_identification ON md_identification(fk_progress, fk_citation);
CREATE INDEX idx_fk_keywords ON md_keywords(fk_thesaurus, fk_type);
CREATE INDEX idx_fk_maintenanceinformation ON md_maintenanceinformation(FK_MainFreq, fk_scope);
CREATE INDEX idx_fk_metadata ON md_metadata(fk_fileidentifier, fk_characterset, fk_hlevelcode, fk_hlevelname);
CREATE INDEX idx_fk_portrayalcatref ON md_portrayalcatref(fk_citation);
CREATE INDEX idx_fk_resolution ON md_resolution(fk_dataident);
CREATE INDEX idx_fk_standorderproc ON md_standorderproc(fk_distributor);
CREATE INDEX idx_fk_usage ON md_usage(fk_usercontactinfo);
CREATE INDEX idx_fk_vectorspatialrep ON md_vectorspatialreprenstation(fk_topolevelcode, fk_geoobjtypecode, fk_metadata);
CREATE INDEX idx_fk_operateson ON operateson(fk_serviceidentification, fk_dataidentification);
CREATE INDEX idx_fk_otherconstraints ON otherconstraints(fk_legalconstraints);
CREATE INDEX idx_fk_rsidentifier ON rs_identifier(fk_authority);
CREATE INDEX idx_fk_serviceversion ON serviceversion(fk_serviceident);
CREATE INDEX idx_fk_operationmetadata ON sv_operationmetadata(fk_serviceident);
CREATE INDEX idx_fk_parameter ON sv_parameter(fk_operation);
CREATE INDEX idx_fk_voice ON voice(fk_contact);

--// räumliche indizes
CREATE INDEX spx_geogrbbox ON ex_geogrbbox USING GIST ( geom GIST_GEOMETRY_OPS );
CREATE INDEX spx_boundingpolygon ON ex_boundingpolygon USING GIST ( geom GIST_GEOMETRY_OPS );












