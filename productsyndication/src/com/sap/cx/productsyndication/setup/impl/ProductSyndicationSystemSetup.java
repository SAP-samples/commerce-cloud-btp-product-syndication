package com.sap.cx.productsyndication.setup.impl;

import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import java.util.ArrayList;
import java.util.List;

import com.sap.cx.productsyndication.constants.ProductsyndicationConstants;

/**
 * System setup class for Product Syndication.
 */
@SystemSetup(extension = ProductsyndicationConstants.EXTENSIONNAME)
public class ProductSyndicationSystemSetup extends AbstractSystemSetup {

    /**
     * Provides the initialization parameters for this extension.
     *
     * @return The list of options
     */
    @Override
    @SystemSetupParameterMethod
    public List<SystemSetupParameter> getInitializationOptions() {
        final var params = new ArrayList<SystemSetupParameter>();
        params.add(createBooleanSystemSetupParameter(ProductsyndicationConstants.CLEANUP_EXISTING_DATA,
                "Clean Existing Data Before Importing", false));
        params.add(
                createBooleanSystemSetupParameter(ProductsyndicationConstants.IMPORT_SAMPLE_DATA, "Import Sample Data", false));
        return params;
    }

    /**
     * Creates project data.
     *
     * @param context The setup context
     */
    @SystemSetup(type = Type.PROJECT, process = Process.ALL)
    public void createProjectData(final SystemSetupContext context) {
        if (getBooleanSystemSetupParameter(context, ProductsyndicationConstants.CLEANUP_EXISTING_DATA)) {
            importImpexFile(context, ProductSyndicationSystemSetup.getSampledataFilePath("cleanup.impex"), true);
        }

        if (getBooleanSystemSetupParameter(context, ProductsyndicationConstants.IMPORT_SAMPLE_DATA)) {
            importImpexFile(context, ProductSyndicationSystemSetup.getSampledataFilePath("genericproduct.impex"), true);
            importImpexFile(context, ProductSyndicationSystemSetup.getSampledataFilePath("genericproduct-outbound.impex"), true);

            importImpexFile(context, ProductSyndicationSystemSetup.getSampledataFilePath("variantproduct.impex"), true);
            importImpexFile(context, ProductSyndicationSystemSetup.getSampledataFilePath("variantproduct-outbound.impex"), true);

            importImpexFile(context, ProductSyndicationSystemSetup.getSampledataFilePath("multidimensionalproduct.impex"), true);
            importImpexFile(context,
                    ProductSyndicationSystemSetup.getSampledataFilePath("multidimensionalproduct-outbound.impex"), true);

            importImpexFile(context, ProductSyndicationSystemSetup.getSampledataFilePath("electronicsproduct.impex"), true);

            importImpexFile(context, ProductSyndicationSystemSetup.getSampledataFilePath("supplemental-prices.impex"), true);
            importImpexFile(context, ProductSyndicationSystemSetup.getSampledataFilePath("supplemental-prices-outbound.impex"), true);
        }
    }

    private static String getSampledataFilePath(final String filename) {
        return "/%s/import/sampledata/%s".formatted(ProductsyndicationConstants.EXTENSIONNAME, filename);
    }
}
