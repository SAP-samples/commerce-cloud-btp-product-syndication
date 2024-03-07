package com.sap.cx.productsyndication.outboundsync.activator.impl;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.integrationservices.exception.IntegrationAttributeException;
import de.hybris.platform.integrationservices.exception.IntegrationAttributeProcessingException;
import de.hybris.platform.integrationservices.service.ItemModelSearchService;
import de.hybris.platform.integrationservices.util.Log;
import de.hybris.platform.outboundservices.enums.OutboundSource;
import de.hybris.platform.outboundservices.facade.OutboundServiceFacade;
import de.hybris.platform.outboundservices.facade.SyncParameters;
import de.hybris.platform.outboundservices.service.OutboundMultiPartResponseParser;
import de.hybris.platform.outboundsync.activator.impl.DefaultOutboundSyncService;
import de.hybris.platform.outboundsync.dto.OutboundItemDTOGroup;
import de.hybris.platform.outboundsync.exceptions.BatchResponseNotFoundException;
import de.hybris.platform.outboundsync.job.OutboundItemFactory;
import de.hybris.platform.outboundsync.job.impl.OutboundSyncJobRegister;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

/**
 * Extends the {@link DefaultOutboundSyncService} to provide better error handling and logging.
 */
public class ProductSyndicationOutboundSyncService extends DefaultOutboundSyncService {

    private static final Logger LOG = Log.getLogger(ProductSyndicationOutboundSyncService.class);
    private final OutboundMultiPartResponseParser batchResponseParser;

    private static final String CONTENT_ID = "Content-ID";

    /**
     * Constructor.
     *
     * @param itemModelSearchService The item model search service
     * @param jobRegister            The job register
     * @param outboundItemFactory    The outbound item factory
     * @param batchResponseParser    The batch response parser
     * @param outboundServiceFacade  The outbound service facade
     */
    public ProductSyndicationOutboundSyncService(
            final ItemModelSearchService itemModelSearchService,
            final OutboundSyncJobRegister jobRegister,
            final OutboundItemFactory outboundItemFactory,
            final OutboundMultiPartResponseParser batchResponseParser,
            final OutboundServiceFacade outboundServiceFacade) {
        super(itemModelSearchService, outboundItemFactory, jobRegister, outboundServiceFacade, batchResponseParser);
        this.batchResponseParser = batchResponseParser;
    }

    @Override
    public void syncBatch(final Collection<OutboundItemDTOGroup> outboundItemDTOGroups) {
        if (outboundItemDTOGroups == null || outboundItemDTOGroups.isEmpty()) {
            LOG.error("The collection of items to be synced is empty.");
            return;
        }
        syncInternal(
                outboundItemDTOGroups.stream().findFirst().orElseThrow().getCronJobPk(),
                outboundItemDTOGroups,
                () -> synchronizeItem(outboundItemDTOGroups)
        );

    }

    @Override
    protected void handleResponse(final ResponseEntity<Map> responseEntity, final OutboundItemDTOGroup group) {
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            handleSuccessfulSync(group);
        } else {
            LOG.error("Error for item PK={}: {}", group.getRootItemPk(), responseEntity.getBody());
            handleError(group);
        }
    }

    private void synchronizeItem(final Collection<OutboundItemDTOGroup> outboundItemDTOGroups) {
        final List<SyncParameters> syncParametersList = createSyncParameters(outboundItemDTOGroups).stream().toList();
        if (syncParametersList.isEmpty()) {
            LOG.debug("No root items found for this synchronization.");
            return;
        }
        try {
            final ResponseEntity<String> response = getOutboundServiceFacade().sendBatch(syncParametersList);
            handleBatchResponse(response, outboundItemDTOGroups);
        } catch (final RuntimeException e) {
            handleBatchError(e, outboundItemDTOGroups);
        }
    }

    private List<SyncParameters> createSyncParameters(final Collection<OutboundItemDTOGroup> outboundItemDTOGroups) {
        final var syncParams = new ArrayList<SyncParameters>();
        outboundItemDTOGroups.forEach(dtoGroup ->
                findRootItemModel(dtoGroup).ifPresent(item -> syncParams.add(convertToSyncParameters(dtoGroup, item)))
        );
        return syncParams;
    }

    private void handleBatchResponse(final ResponseEntity<String> response,
            final Collection<OutboundItemDTOGroup> groups) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("response headers: {}", response.getHeaders());
		}
        final List<ResponseEntity<Map>> responseParts = getBatchResponseParser().parseMultiPartResponse(response);
        groups.forEach(g -> findResponseAndHandle(g, responseParts));
    }

    private void handleBatchError(final Throwable throwable, final Collection<OutboundItemDTOGroup> outboundItemDTOGroups) {
        LOG.error("Failed to send batch request for synchronization", throwable);
        if (throwable instanceof BadRequest badRequest) {
            LOG.error("Error details: {}", badRequest.getResponseBodyAsString());
        } else {
            LOG.error("Error details: {}", throwable.getMessage());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Error stacktrace", throwable);
        }
        if (throwable instanceof IntegrationAttributeProcessingException) {
            outboundItemDTOGroups.forEach(this::handleError);
        } else if (throwable instanceof IntegrationAttributeException) {
            publishSystemErrorEvent(
                    outboundItemDTOGroups.stream().findFirst().orElseThrow().getCronJobPk(),
                    outboundItemDTOGroups
            );
        } else {
            outboundItemDTOGroups.forEach(this::handleError);
        }
    }

    private Optional<ItemModel> findRootItemModel(final OutboundItemDTOGroup outboundItemDTOGroup) {
        final Long rootItemPk = outboundItemDTOGroup.getRootItemPk();
        LOG.debug("Synchronizing changes in item with PK={}", rootItemPk);

        final Optional<ItemModel> item = findItemByPk(PK.fromLong(rootItemPk));
        if (item.isEmpty()) {
            LOG.debug("Cannot find item with PK={}", rootItemPk);
        }
        return item;
    }

    private OutboundMultiPartResponseParser getBatchResponseParser() {
        return batchResponseParser;
    }

    private SyncParameters convertToSyncParameters(final OutboundItemDTOGroup dtoGroup, final ItemModel itemModel) {
        return SyncParameters.syncParametersBuilder()
                .withItem(itemModel)
                .withIntegrationObjectCode(dtoGroup.getIntegrationObjectCode())
                .withDestinationId(dtoGroup.getDestinationId())
                .withChangeId(dtoGroup.getChangeId())
                .withSource(OutboundSource.OUTBOUNDSYNC)
                .build();
    }

    private void findResponseAndHandle(final OutboundItemDTOGroup g, final List<ResponseEntity<Map>> responseParts) {
        responseParts.stream()
                .filter(getResponsePartFilterForGroup(g))
                .findFirst()
                .ifPresentOrElse(
                        r -> handleResponse(r, g),
                        () -> handleError(new BatchResponseNotFoundException(g), g)
                );
    }


    private void handleError(final Throwable throwable, final OutboundItemDTOGroup outboundItemDTOGroup) {
        LOG.error("Failed to send item with PK={}", outboundItemDTOGroup.getRootItemPk(), throwable);
        if (throwable instanceof IntegrationAttributeProcessingException) {
            handleError(outboundItemDTOGroup);
        } else if (throwable instanceof IntegrationAttributeException) {
            publishSystemErrorEvent(outboundItemDTOGroup.getCronJobPk(), outboundItemDTOGroup);
        } else {
            handleError(outboundItemDTOGroup);
        }
    }

    private static Predicate<ResponseEntity<Map>> getResponsePartFilterForGroup(final OutboundItemDTOGroup group) {
        return r -> {
            final var contentId = r.getHeaders().get(CONTENT_ID);
            return contentId != null
                    && !contentId.isEmpty()
                    && StringUtils.equals(group.getChangeId(), contentId.get(0));
        };
    }
}
