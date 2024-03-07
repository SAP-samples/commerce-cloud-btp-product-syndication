package com.sap.cx.productsyndication.outboundservices.batch;

import com.google.common.base.Preconditions;
import de.hybris.platform.odata2services.odata.impl.ODataBatchParsingException;
import de.hybris.platform.outboundservices.facade.OutboundBatchRequestPartDTO;
import de.hybris.platform.outboundservices.service.MultiPartRequestGenerator;
import de.hybris.platform.outboundservices.service.OutboundMultiPartRequestConsolidator;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

/**
 * Re-implementation of the platform {@link de.hybris.platform.outboundservices.batch.DefaultBatchRequestGenerator} to
 * add a fixed UTF-8 charset to the content type header.
 */
public class ProductSyndicationBatchRequestGenerator implements MultiPartRequestGenerator {

    private static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE;
    private static final String CONTENT_TYPE_TEMPLATE = "multipart/mixed; boundary=%s; charset=utf-8";
    private static final String BODY_BOUNDARY_PREFIX = "batch_";

    private final OutboundMultiPartRequestConsolidator consolidator;

    /**
     * Constructor.
     *
     * @param requestConsolidator The request consolidator
     */
    public ProductSyndicationBatchRequestGenerator(
            final @NotNull OutboundMultiPartRequestConsolidator requestConsolidator) {
        Preconditions.checkArgument(requestConsolidator != null, "Multi-Part request consolidator must be provided");
        this.consolidator = requestConsolidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpEntity<String> generate(final List<OutboundBatchRequestPartDTO> requestDTOs) {
        final String batchBoundary = generateBoundary();
        final HttpHeaders headers = generateHttpHeaderForBatchRequest(requestDTOs, batchBoundary);
        final String body = getConsolidator().consolidate(requestDTOs, batchBoundary);
        return new HttpEntity<>(body, headers);
    }

    protected String generateBoundary() {
        return BODY_BOUNDARY_PREFIX + UUID.randomUUID();
    }

    protected HttpHeaders generateHttpHeaderForBatchRequest(final List<OutboundBatchRequestPartDTO> requestDTOs,
            final String boundary) {
        final HttpHeaders newHeaders = new HttpHeaders();
        newHeaders.addAll(getFirstDTO(requestDTOs).getHttpEntity().getHeaders());
        newHeaders.set(CONTENT_TYPE, CONTENT_TYPE_TEMPLATE.formatted(boundary));
        return newHeaders;
    }

    protected OutboundBatchRequestPartDTO getFirstDTO(final List<OutboundBatchRequestPartDTO> requestDTOs) {
        return requestDTOs.stream().findFirst().orElseThrow(ODataBatchParsingException::new);
    }

    protected OutboundMultiPartRequestConsolidator getConsolidator() {
        return consolidator;
    }
}
