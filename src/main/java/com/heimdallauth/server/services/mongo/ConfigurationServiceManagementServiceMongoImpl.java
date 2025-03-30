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
import org.checkerframework.checker.units.qual.A;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.heimdallauth.server.utils.mapper.DocumentMapperRepository.DOCUMENT_MAPPER;

@Repository
@ConditionalOnProperty(name = {"spring.data.mongodb.uri", "spring.data.mongodb.host"})
public class ConfigurationServiceManagementServiceMongoImpl implements ConfigurationSetManagementService, EmailSuppressionManagementService {
    private final MongoTemplate mongoTemplate;
    private static final ModelMapper MAPPER = DOCUMENT_MAPPER;

    private static final String COLLECTION_CONFIGURATION_SETS = "configuration_sets";
    private static final String COLLECTION_SUPPRESSION_LIST = "suppression_list";
    private static final String CONFIGURATION_SET_SUPPRESSION_LIST_MAPPING = "configuration_set_suppression_list_mapping";

    @Autowired
    public ConfigurationServiceManagementServiceMongoImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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
        if(!force || isConfigurationSetExists(createConfigurationSetPayload.getConfigurationSetName(), tenantId)){
            throw new ConfigurationSetAlreadyExists("Configuration set already exists");
        }
        if(createConfigurationSetPayload.getEmailSuppressionEntries() != null && !createConfigurationSetPayload.getEmailSuppressionEntries().isEmpty()){
            List<UUID> suppressionIdsNotPresent = this.getSuppressionIdsNotPresent(createConfigurationSetPayload.getEmailSuppressionEntries());
            if(!suppressionIdsNotPresent.isEmpty()){
                throw new SuppressionListNotFound("Suppression list not found");
            }
        }
        UUID configurationSetId = UUID.randomUUID();
        ConfigurationSetMasterDocument configurationSetMasterDocument = ConfigurationSetMasterDocument.builder()
                .configurationId(configurationSetId)
                .tenantId(tenantId)
                .configurationSetName(createConfigurationSetPayload.getConfigurationSetName())
                .configurationSetDescription(createConfigurationSetPayload.getConfigurationSetDescription())
                .suppressionListIds(createConfigurationSetPayload.getEmailSuppressionEntries())
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

    @Override
    public ConfigurationSetModel getConfigurationSetById(UUID configurationSetId) throws ConfigurationSetNotFound {
        ConfigurationSetAggregationModel aggregationResult = getConfigurationSetMasterDocumentById(configurationSetId);
        if(aggregationResult != null){
            return MAPPER.map(aggregationResult, ConfigurationSetModel.class);
        }
        return null;
    }
    private ConfigurationSetAggregationModel getConfigurationSetMasterDocumentById(UUID configurationSetId) throws ConfigurationSetNotFound {
        Aggregation configurationSetAggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("id").is(configurationSetId)),
                Aggregation.lookup(COLLECTION_SUPPRESSION_LIST, "suppressionListIds", "id", "suppressionEntries")
        );
       return this.mongoTemplate.aggregate(configurationSetAggregation, COLLECTION_CONFIGURATION_SETS, ConfigurationSetAggregationModel.class).getMappedResults().getFirst();
    }

    @Override
    public ConfigurationSetModel getConfigurationSetByNameAndTenantId(String configurationSetName, String tenantId) throws ConfigurationSetNotFound {
        return null;
    }

    @Override
    public ConfigurationSetModel updateConfigurationSetMasterData(String configurationSetId, String configurationSetName, String configurationSetDescription) throws ConfigurationSetNotFound {
        return null;
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
        Query suppressionEntrySearchQuery = Query.query(Criteria.where("id").in(suppressionEntryId));
        List<SuppressionEntryModel> matchedSuppressionEntries = this.mongoTemplate.find(suppressionEntrySearchQuery, SuppressionEntryDocument.class, COLLECTION_SUPPRESSION_LIST)
                .stream().map(ConfigurationServiceManagementServiceMongoImpl::mapSuppressionEntryDocumentToModel).toList();
        if(matchedSuppressionEntries.isEmpty()){
            throw new SuppressionListNotFound("No suppression list found");
        }
        return matchedSuppressionEntries;
    }

    @Override
    public SuppressionEntryModel createSuppressionEntry(CreateSuppressionEntryDTO createSuppressionEntryPayload) {
        return null;
    }

    @Override
    public SuppressionEntryModel getSuppressionEntryByConfigurationSetId(String configurationSetId) throws SuppressionListNotFound {
        return null;
    }

    @Override
    public void deleteSuppressionEntryById(String suppressionEntryId)  {
        Query suppressionCollectionSearchQuery = Query.query(Criteria.where("id").is(suppressionEntryId));

    }

    /**
     * Map a SuppressionEntryDocument to a SuppressionEntryModel.
     *
     * @param suppressionEntryDocument The SuppressionEntryDocument to map.
     * @return The mapped SuppressionEntryModel.
     */
    private static SuppressionEntryModel mapSuppressionEntryDocumentToModel(SuppressionEntryDocument suppressionEntryDocument) {
        return MAPPER.map(suppressionEntryDocument, SuppressionEntryModel.class);
    }
    /**
     * Get the list of suppression IDs that are not present in the database.
     *
     * @param idsToValidate The list of suppression IDs to validate.
     * @return A list of UUIDs representing the suppression IDs that are not present in the database.
     */
    private List<UUID> getSuppressionIdsNotPresent(List<UUID> idsToValidate){
        List<UUID> idsMatchedInDB = this.getAllSuppressionEntriesById(idsToValidate).stream().map(SuppressionEntryModel::getSuppressionEntryId).toList();
        return idsToValidate.stream().filter(id -> !idsMatchedInDB.contains(id)).toList();
    }
}
