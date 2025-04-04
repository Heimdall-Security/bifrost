package com.heimdallauth.server.services.mongo;

import com.heimdallauth.server.documents.ConfigurationSetAggregationModel;
import com.heimdallauth.server.documents.ConfigurationSetMasterDocument;
import com.heimdallauth.server.documents.SuppressionEntryDocument;
import com.heimdallauth.server.dto.bifrost.CreateConfigurationSetDTO;
import com.heimdallauth.server.dto.bifrost.CreateSuppressionEntryDTO;
import com.heimdallauth.server.exceptions.ConfigurationSetAlreadyExists;
import com.heimdallauth.server.exceptions.ConfigurationSetNotFound;
import com.heimdallauth.server.exceptions.SuppressionListNotFound;
import com.heimdallauth.server.models.bifrost.ConfigurationSetModel;
import com.heimdallauth.server.models.bifrost.SuppressionEntryModel;
import com.heimdallauth.server.services.ConfigurationSetManagementService;
import com.heimdallauth.server.services.EmailSuppressionManagementService;
import com.heimdallauth.server.utils.mapper.ConfigurationMapper;
import com.heimdallauth.server.utils.mapper.SuppressionEntryMapper;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@EnableScheduling
@Slf4j
public class ConfigurationServiceManagementServiceMongoImpl implements ConfigurationSetManagementService, EmailSuppressionManagementService {
    private final MongoTemplate mongoTemplate;
    private final ConfigurationMapper configurationMapper;
    private final SuppressionEntryMapper suppressionEntryMapper;
    private static final String COLLECTION_CONFIGURATION_SETS = "configuration_sets";
    private static final String COLLECTION_SUPPRESSION_LIST = "suppression_list";

    public ConfigurationServiceManagementServiceMongoImpl(MongoTemplate mongoTemplate, ConfigurationMapper configurationMapper, SuppressionEntryMapper suppressionEntryMapper) {
        this.mongoTemplate = mongoTemplate;
        this.configurationMapper = configurationMapper;
        this.suppressionEntryMapper = suppressionEntryMapper;
    }

    /**
     * Create a new configuration set with the given payload.
     *
     * @param createConfigurationSetPayload The payload containing the configuration set details.
     * @param tenantId The ID of the tenant creating the configuration set.
     * @param force Whether to force creation even if a configuration set with the same name exists.
     * @return The created ConfigurationSetModel.
     * @throws ConfigurationSetAlreadyExists If a configuration set with the same name already exists and force is false.
     */
    @Override
    public ConfigurationSetModel createNewConfigurationSet(CreateConfigurationSetDTO createConfigurationSetPayload,UUID tenantId ,boolean force) throws ConfigurationSetAlreadyExists {
        if(force || isConfigurationSetExists(createConfigurationSetPayload.configurationSetName(), tenantId)){
            throw new ConfigurationSetAlreadyExists("Configuration set already exists");
        }
        if(createConfigurationSetPayload.suppressionEntryIds() != null && !createConfigurationSetPayload.suppressionEntryIds().isEmpty()){
            List<UUID> suppressionIdsNotPresent = this.getSuppressionIdsNotPresent(createConfigurationSetPayload.suppressionEntryIds());
            if(!suppressionIdsNotPresent.isEmpty()){
                log.error("Suppression entry ids not present, ids={}", suppressionIdsNotPresent);
            }
        }
        UUID configurationSetId = UUID.randomUUID();
        ConfigurationSetMasterDocument configurationSetMasterDocument = ConfigurationSetMasterDocument.builder()
                .configurationId(configurationSetId.toString())
                .tenantId(tenantId.toString())
                .configurationSetName(createConfigurationSetPayload.configurationSetName())
                .configurationSetDescription(createConfigurationSetPayload.configurationSetDescription())
                .suppressionListIds(createConfigurationSetPayload.suppressionEntryIds().stream().map(UUID::toString).collect(Collectors.toList()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        this.mongoTemplate.save(configurationSetMasterDocument, COLLECTION_CONFIGURATION_SETS);
        return this.getConfigurationSetById(configurationSetId);
    }
    /**
     * Check if a configuration set with the given name already exists for the specified tenant.
     *
     * @param configurationSetName The name of the configuration set to check.
     * @param tenantId The ID of the tenant to check against.
     * @return true if the configuration set exists, false otherwise.
     */
    private boolean isConfigurationSetExists(String configurationSetName, UUID tenantId){
        Query configurationSetSearchQueryForTenant = Query.query(Criteria.where("tenantId").is(tenantId).and("configurationSetName").is(configurationSetName));
        return this.mongoTemplate.exists(configurationSetSearchQueryForTenant, ConfigurationSetMasterDocument.class, COLLECTION_CONFIGURATION_SETS);
    }

    /**
     * Get a configuration set by its ID.
     *
     * @param configurationSetId The ID of the configuration set to retrieve.
     * @return The ConfigurationSetModel associated with the given ID.
     * @throws ConfigurationSetNotFound If no configuration set is found for the given ID.
     */
    @Override
    public ConfigurationSetModel getConfigurationSetById(UUID configurationSetId) throws ConfigurationSetNotFound {
        ConfigurationSetAggregationModel aggregationResult = getConfigurationSetMasterDocumentById(configurationSetId);
        if(aggregationResult != null){
            return configurationMapper.toConfigurationSetModel(aggregationResult);
        }
        return null;
    }

    @Override
    public List<ConfigurationSetModel> getConfigurationSetsForTenantId(UUID tenantId) {
        Query searchConfigurationSetForTenantQuery = Query.query(Criteria.where("tenantId").is(tenantId.toString()));
        List<ConfigurationSetMasterDocument> configurationSetDocuments = this.mongoTemplate.find(searchConfigurationSetForTenantQuery, ConfigurationSetMasterDocument.class, COLLECTION_CONFIGURATION_SETS);
        if(configurationSetDocuments.isEmpty()){
            return List.of();
        }else{
            return configurationSetDocuments.stream().map(configurationMapper::toConfigurationSetModel).toList();
        }
    }

    @Override
    public void deleteConfigurationSetById(UUID configurationSetId) {
        Query deleteConfigurationSetQuery = Query.query(Criteria.where("_id").is(configurationSetId.toString()));
        DeleteResult deleteResult = this.mongoTemplate.remove(deleteConfigurationSetQuery, COLLECTION_CONFIGURATION_SETS);
        if(deleteResult.getDeletedCount() > 0){
            log.debug("Deleted configuration set with ID: {}. Deleted count: {}", configurationSetId, deleteResult.getDeletedCount());
        }else{
            log.error("No instances matched, Nothing to delete. Deleted count: {}", 0);
        }
    }

    /**
     * Get a configuration set by its ID.
     *
     * @param configurationSetId The ID of the configuration set to retrieve.
     * @return The ConfigurationSetModel associated with the given ID.
     * @throws ConfigurationSetNotFound If no configuration set is found for the given ID.
     */
    private ConfigurationSetAggregationModel getConfigurationSetMasterDocumentById(UUID configurationSetId) throws ConfigurationSetNotFound {
        Aggregation configurationSetAggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(configurationSetId.toString())),
                Aggregation.lookup(COLLECTION_SUPPRESSION_LIST, "suppressionListIds", "_id", "suppressionEntries")
        );
       return this.mongoTemplate.aggregate(configurationSetAggregation, COLLECTION_CONFIGURATION_SETS, ConfigurationSetAggregationModel.class).getMappedResults().getFirst();
    }

    /**
     * Get a configuration set by its name and tenant ID.
     *
     * @param configurationSetName The name of the configuration set to retrieve.
     * @param tenantId The ID of the tenant to retrieve the configuration set for.
     * @return The ConfigurationSetModel associated with the given name and tenant ID.
     * @throws ConfigurationSetNotFound If no configuration set is found for the given name and tenant ID.
     */
    @Override
    public ConfigurationSetModel getConfigurationSetByNameAndTenantId(String configurationSetName, String tenantId) throws ConfigurationSetNotFound {
        Aggregation aggregationPipelineQuery = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("configurationSetName").is(configurationSetName).and("tenantId").is(tenantId)),
                Aggregation.lookup(COLLECTION_SUPPRESSION_LIST, "suppressionListIds", "id", "suppressionEntries")
        );
        ConfigurationSetAggregationModel aggregationModel = this.mongoTemplate.aggregate(aggregationPipelineQuery, COLLECTION_CONFIGURATION_SETS, ConfigurationSetAggregationModel.class).getMappedResults().getFirst();
        if(aggregationModel != null){
            return configurationMapper.toConfigurationSetModel(aggregationModel);
        }else{
            throw new ConfigurationSetNotFound("Configuration set not found");
        }
    }

    /**
     * Update the master data of a configuration set.
     *
     * @param configurationSetId The ID of the configuration set to update.
     * @param configurationSetName The new name for the configuration set.
     * @param configurationSetDescription The new description for the configuration set.
     * @return The updated ConfigurationSetModel.
     * @throws ConfigurationSetNotFound If the configuration set is not found.
     */
    @Override
    public ConfigurationSetModel updateConfigurationSetMasterData(String configurationSetId, String configurationSetName, String configurationSetDescription) throws ConfigurationSetNotFound {
        Query configurationSetSearchQuery = Query.query(Criteria.where("id").is(configurationSetId));
        Update updateSpec = Update.update("configurationSetDescription", configurationSetDescription);
        updateSpec.set("configurationSetName", configurationSetName);
        this.mongoTemplate.updateFirst(configurationSetSearchQuery, updateSpec, ConfigurationSetMasterDocument.class);
        ConfigurationSetAggregationModel aggregationResult = getConfigurationSetMasterDocumentById(UUID.fromString(configurationSetId));
        if(aggregationResult != null){
            return configurationMapper.toConfigurationSetModel(aggregationResult);
        }else{
            throw new ConfigurationSetNotFound("Configuration set not found");
        }
    }

    @Override
    public List<SuppressionEntryModel> getAllSuppressionEntries() {
        List<SuppressionEntryDocument> allEntriesDocuments  = this.mongoTemplate.findAll(SuppressionEntryDocument.class, COLLECTION_SUPPRESSION_LIST);
        return allEntriesDocuments.stream().map(this::mapSuppressionEntryDocumentToModel).toList();
    }

    /**
     * Get all suppression entries by their IDs.
     *
     * @param suppressionEntryId The list of suppression entry IDs to retrieve.
     * @return A list of SuppressionEntryModel objects matching the provided IDs.
     * @throws SuppressionListNotFound If no suppression entries are found for the given IDs.
     */
    @Override
    public List<SuppressionEntryModel> getAllSuppressionEntriesById(List<UUID> suppressionEntryId) throws SuppressionListNotFound {
        Query suppressionEntrySearchQuery = Query.query(Criteria.where("id").in(suppressionEntryId.stream().map(UUID::toString).toList()));
        List<SuppressionEntryModel> matchedSuppressionEntries = this.mongoTemplate.find(suppressionEntrySearchQuery, SuppressionEntryDocument.class, COLLECTION_SUPPRESSION_LIST)
                .stream().map(this::mapSuppressionEntryDocumentToModel).toList();
        if(matchedSuppressionEntries.isEmpty()){
            throw new SuppressionListNotFound("No suppression list found");
        }
        return matchedSuppressionEntries;
    }

    /**
     * Create a new suppression entry.
     *
     * @param createSuppressionEntryPayload The payload containing the suppression entry details.
     * @return The created SuppressionEntryModel.
     */
    @Override
    public SuppressionEntryModel createSuppressionEntry(CreateSuppressionEntryDTO createSuppressionEntryPayload) {
        SuppressionEntryDocument suppressionEntryDocument = SuppressionEntryDocument.builder()
                .id(UUID.randomUUID().toString())
                .entryType(createSuppressionEntryPayload.entryType())
                .value(createSuppressionEntryPayload.value())
                .reason(createSuppressionEntryPayload.reason())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        SuppressionEntryDocument createdSuppressionEntryDocument = this.mongoTemplate.save(suppressionEntryDocument, COLLECTION_SUPPRESSION_LIST);
        return suppressionEntryMapper.map(createdSuppressionEntryDocument);
    }

    /**
     * Get a suppression entry by its ID.
     *
     * @param suppressionEntryId The ID of the suppression entry to retrieve.
     * @return The SuppressionEntryModel associated with the given ID.
     * @throws SuppressionListNotFound If no suppression entry is found for the given ID.
     */
    @Override
    public SuppressionEntryModel getSuppressionEntryById(UUID suppressionEntryId) throws SuppressionListNotFound {
        Query suppressionEntrySearchQuery = Query.query(Criteria.where("_id").is(suppressionEntryId.toString()));
        Optional<SuppressionEntryDocument> suppressionEntryDocument = Optional.ofNullable(this.mongoTemplate.findOne(suppressionEntrySearchQuery, SuppressionEntryDocument.class, COLLECTION_SUPPRESSION_LIST));
        if(suppressionEntryDocument.isPresent()){
            return mapSuppressionEntryDocumentToModel(suppressionEntryDocument.get());
        }else{
            throw new SuppressionListNotFound("Suppression list not found");
        }
    }

    /**
     * Get suppression entries by configuration set ID.
     *
     * @param configurationSetId The ID of the configuration set to retrieve suppression entries for.
     * @return A list of SuppressionEntryModel objects associated with the given configuration set ID.
     * @throws SuppressionListNotFound If no suppression entries are found for the given configuration set ID.
     */
    @Override
    public List<SuppressionEntryModel> getSuppressionEntryByConfigurationSetId(UUID configurationSetId) throws SuppressionListNotFound {
        return List.of();
    }

    /**
     * Delete a suppression entry by its ID.
     *
     * @param suppressionEntryId The ID of the suppression entry to delete.
     */
    @Override
    public void deleteSuppressionEntryById(UUID suppressionEntryId)  {
        Query suppressionCollectionSearchQuery = Query.query(Criteria.where("id").is(suppressionEntryId.toString()));
        DeleteResult deleteResult = this.mongoTemplate.remove(suppressionCollectionSearchQuery, SuppressionEntryDocument.class, COLLECTION_SUPPRESSION_LIST);
        log.debug("Deleted suppression entry with ID: {}. Deleted count: {}", suppressionEntryId, deleteResult.getDeletedCount());
    }
    /**
     * Delete suppression entries by their IDs.
     *
     * @param suppressionEntryIds The list of suppression entry IDs to delete.
     */
    private void deleteSuppressionEntryByIds(List<String> suppressionEntryIds){
        Query suppressionEntrySearchQuery = Query.query(Criteria.where("id").in(suppressionEntryIds));
        this.mongoTemplate.remove(suppressionEntrySearchQuery, SuppressionEntryDocument.class, COLLECTION_SUPPRESSION_LIST);
        log.debug("Deleted suppression entries with IDs: {}", suppressionEntryIds);
    }

    /**
     * Map a SuppressionEntryDocument to a SuppressionEntryModel.
     *
     * @param suppressionEntryDocument The SuppressionEntryDocument to map.
     * @return The mapped SuppressionEntryModel.
     */
    private SuppressionEntryModel mapSuppressionEntryDocumentToModel(SuppressionEntryDocument suppressionEntryDocument) {
        return suppressionEntryMapper.map(suppressionEntryDocument);
    }
    /**
     * Get the list of suppression IDs that are not present in the database.
     *
     * @param idsToValidate The list of suppression IDs to validate.
     * @return A list of UUIDs representing the suppression IDs that are not present in the database.
     */
    private List<UUID> getSuppressionIdsNotPresent(List<UUID> idsToValidate){
        List<UUID> idsMatchedInDB = this.getAllSuppressionEntriesById(idsToValidate).stream().map(SuppressionEntryModel::suppressionEntryId).toList();
        return idsToValidate.stream().filter(id -> !idsMatchedInDB.contains(id)).toList();
    }
    /**
     * Trigger the unused suppression list cleanup.
     * This method is scheduled to run daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    void triggerDatabaseMaintenance() {
        log.debug("Database maintenance triggered");
       triggerUnusedSuppressionList();
    }
    /**
     * Trigger the unused suppression list cleanup.
     * This method is scheduled to run daily at midnight.
     */
    private void triggerUnusedSuppressionList(){
        Set<String> allSuppressionListIds = this.mongoTemplate.findAll(ConfigurationSetMasterDocument.class, COLLECTION_CONFIGURATION_SETS).stream().map(ConfigurationSetMasterDocument::getSuppressionListIds).flatMap(List::stream).collect(Collectors.toSet());
        Set<String> allSuppressionIds = this.mongoTemplate.findAll(SuppressionEntryDocument.class, COLLECTION_SUPPRESSION_LIST).stream().map(SuppressionEntryDocument::getId).collect(Collectors.toSet());
        Set<String> unusedSuppressionIds = allSuppressionIds.stream().filter(id -> !allSuppressionListIds.contains(id)).collect(Collectors.toSet());
        log.debug("Unused Suppression List: {}", unusedSuppressionIds);
        this.deleteSuppressionEntryByIds(unusedSuppressionIds.stream().toList());
    }

}
