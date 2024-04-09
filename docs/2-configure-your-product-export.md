# Configure Your Product Export

Once the initial configuration is completed, it is required to configure the product and what fields should be sent to Product Syndication.

# Supported Commerce Product Types

Commerce Cloud provides a set of built-in product types in the platform and allows you to create your own.

Built-in ones with optional custom attributes is a preferred option of extending solution, as that allow to use Product Export configuration as-is.

Documentation covers product types listed below:

 - [ProductModel](https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/8c33030986691014b683de344b46b559.html?locale=en-US&amp;q=variant%20product) - used for mapping generic products. It is used for items from an electronics store</p></li>
 - [GenericVariantProductModel](https://help.sap.com/docs/SAP_COMMERCE/4c33bf189ab9409e84e589295c36d96e/8b998f6186691014af3993f8f8924835.html?locale=en-US&amp;q=variant%20product) - used for mapping generic variant products. It is used in b2b and partially in electronics stores. Allows to build multi-dimensional variants
 - [ApparelSizeVariantProductModel](https://help.sap.com/docs/SAP_COMMERCE/4c33bf189ab9409e84e589295c36d96e/8af06a1b8669101483c085453d75849e.html?locale=en-US&amp;q=variant%20product) - used for mapping variant products. It is used as the final variant in apparel store samples and is related to tree structure: base product → style variant → size variant

## What if I Have Custom Product Types?

If your implementation uses custom types, that is still fine, we will support your solution. It may take a bit of effort from your side to customize the sample configuration, but it is recommended to use it as a boilerplate.

Your custom product type for sure extends one of the Commerce Cloud Integration Object root types mentioned at the beginning of the section:

 - ProductModel
 - GenericVariantProductModel
 - ApparelSizeVariantProductModel

Having `ProductModel` as a root type will return all items of classes which extend that type.

If you have modelled a custom product type, you have extended e.g. `ProductModel`:

![Extending ProductModel](./imgs/productmodel-extend.png ':size=200px')

In that case, you would need to keep exporting the root product type to `ProductModel`, as it is in provided configuration.

If you would like to use custom attributes on `MyProductModel`, you need to customize the configuration and use [Virtual Attributes](https://help.sap.com/docs/SAP_COMMERCE/50c996852b32456c96d3161a95544cdb/de99ef65ca85473d92520c6eab4d30b7.html?locale=en-US) ([micro video](https://microlearning.opensap.com/media/Working+with+Virtual+Attributes+in+SAP+Commerce+Cloud/1_yla1aml9)). You can find an example of how to do that later.

# Initial Product Definition

For defining the Product payload to be exported, we have provided the [Integration Objects](https://help.sap.com/docs/SAP_COMMERCE/50c996852b32456c96d3161a95544cdb/0e9911b94c8d4120965a9afeccdc663e.html?locale=en-US) configuration. Integration Objects provides the possibility to create, configure and modify endpoint definition in the runtime. It supports Open Data Protocol (OData) v2, which is an open protocol to create and consume queryable and interoperable REST APIs. We recommend reviewing [SAP Help: Integration API Module](https://help.sap.com/docs/SAP_COMMERCE/50c996852b32456c96d3161a95544cdb/a369beb4497b44d08eb7f548147cae1d.html?locale=en-US) page.

Product Syndication expects that products sent from Commerce will have a list of attributes:

| Attribute name | Required | Validation Rule | Description |
| ---------------|----------|-----------------|-------------|
| code | Yes | up to 50 characters | Unique product id which can be searched by the user |
| name | Yes | up to 150 characters | Product name
| description | Yes | up to 5000 characters | Product description |
| url | Yes | | Product link to website (relative to web shop base URL) |
| imageUrl | Yes | | Main product image link (relative to image base URL) |
| availability | Yes | `in_stock`, `out_of_stock` | Product availability in your webshop. Text values aligned with [Google availability](https://support.google.com/merchants/answer/6324448?hl=en&ref_topic=6324338&sjid=6437206415583587348-EU) attribute |
| brand | Yes | up to 70 characters | Product brand, e.g., Samsung for mobile phone |
| ean | Yes | up to 50 digits | Product GTIN id, also known as EAN or barcode id (if that is missing, mpn attribute must be sent) |
| mpn | No | up to 70 characters | Custom, internal product ID. When a webshop offers e.g., homemade products, which are not registered in GTIN.
| condition | No | `new`, `refurbished`, `used` | Product condition status. The default value if missing is "new". Values are aligned with the [Google condition](https://support.google.com/merchants/answer/6324469?hl=en&ref_topic=6324338&sjid=6437206415583587348-EU) attribute. |
| bundle | No | `true`, `false` | if a given product is sold as a bundle, that flag must be set to "true" (default value: `false`) |
| availabilityDate | No | date | the value set for a currently not-available product with the known availability date|
| expirationDate | No | date | the value set for products with a discounted price, or defined expiration date |
| europe1Prices | Yes | List<PriceRow> | List of product prices from Commerce Cloud. Product Syndication will pick a valid price for the required currency

> [!ATTENTION]
> **Product Required Attributes**
>
> In the table above some attributes are marked as required. These fields have to be filled in before sending to Product Syndication, otherwise, these products will be automatically rejected by internal validation.
>
> These fields are required by Google Shopping and will not be sent as that violates their API requirements.

# Variant Product Attributes

Google supports product variants in Google Search. Product Syndication consumes and maps all standard Commerce Cloud variant types.

Google accepts multiple variant attributes which allow to identify of particular products, attributes which must be sent for each variant item:

| Attribute name | Required | Validation Rule | Description |
| ---------------|----------|-----------------|-------------|
 | itemGroupId | Yes | | Grouping ID. The same value should be sent for all variants of a given product |
 | color | No | up to 100 characters | The colour value is the main differentiator of variant products. There is no defined colour palette, so it gives freedom of choice |
 | gender | No | `male`, `female`, `unisex` | Gender is main the differentiator of variant products. The list is limited to defined by [gender Google values](https://support.google.com/merchants/answer/6324479?hl=en&ref_topic=6324338&sjid=6437206415583587348-EU). Should be set for apparel products. |
 | age | No | `newborn`, `infant`, `toddler`, `kids`, `adult` | Age is grouped by ranges, limited to [age_group](https://support.google.com/merchants/answer/6324463?hl=en&ref_topic=6324338&sjid=6437206415583587348-EU) Google values. Should be set for apparel products. |
 | size | No | up to 100 characters | Product size. Should be set for apparel products. There is no limited list of sizes. |

> [!ATTENTION]
> **Minimal Product Variant Configuration**<br>
> Product variants must have defined at least 2 additional attributes:
> - `itemGroupId` - it is required for variants and allows Google to group variants into one offering. Usually, that value is set to the base product code.
> - at least one optional variant attribute from the list above

# Classification Attributes as Input for Google Product Details

Commerce Cloud stores a sub-set of product information in classification attributes. If you would like to export some data to Google Shopping, it is possible to do that and store it under [Product detail](https://support.google.com/merchants/answer/9218260?hl=en&ref_topic=6324338&sjid=6437206415583587348-EU#zippy=%2Claptops%2Cfurniture) information.

Product details are key: value mapping. It is possible to provide additionally a prefix to the key. Google calls it a section_name.

The final format of produced product detail will look like one below:

```
Memory:Flash:64GB
```

That string is built of Google-specific sections:

 - "Memory" is set by `section_name`,
 - "Flash" is set by `attribute_name`,
 - "64GB" is set by `attribute_value`

Google accept any product detail name. It only requires meeting length limits for each of them:

| Attribute name | Required | Validation Rule | Description |
| ---------------|----------|-----------------|-------------|
| section_name | No | up to 140 characters |
| attribute_name | Yes | up to 140 characters |
| attribute_value | Yes | up to 1000 characters | That attribute is filled in from classification attribute mapping.

Product detail `attribute_value` is taken directly from the classification catalogue. There is no possibility to add any further conversion/decoration to the value.

To send these values, it is required to create a new attribute and bind it with the selected classification attribute.

The attribute name has to follow the pattern:

```regex
// That is Regular Expression Pattern
clss(_[a-zA-Z0-9]*)(_[a-zA-Z0-9]*)?
```

Attribute name has to follow a few rules:

 - the attribute name starts with the prefix: "clss"
 - the optional section starts with the underscore "_" and the name is in camel case, e.g. "_Memory", "_Display", "_LensSystem"
 - the mandatory attribute name starts with the underscore "_" and the name is camel case, e.g. "_Flash", "_Size", "_ApertureRange"
 - camel-case names will be split by space, e.g.
    - "_LensSystem" will be converted to: "Lens System"
    - "_ApertureRange" will be converted to "Aperture Range"

The following sample value from configuration, where the classification attribute has been declared as clss_LensSystem_ApertureRange should be parsed as:

 - (clss) = classification attribute for product detail
 - (_LensSystem) =  set section = "Lens System"
 - (_ApertureRange) = set attribute_name = "Aperture Range"

that will be presented in Google payload:

```
Lens System:Aperture Range:3.5 - 4.6
```

Following these rules for our initial example: `Memory:Flash:64GB`, we would need to create an attribute with the name: `clss_Memory_Flash`.

Regarding classification attribute assignment we do recommend looking at the sample classification configuration in _electronicsproduct.impex_ file.

For our example, we have assigned `clss_Memory_Flash` to one classification attribute:

```impex
INSERT_UPDATE IntegrationObjectItemClassificationAttribute; integrationObjectItem(integrationObject(code), code)[unique = true]; attributeName[unique = true]  ; classAttributeAssignment(classificationClass(catalogVersion(catalog(id), version), code), classificationAttribute(systemVersion(catalog(id), version), code)); returnIntegrationObjectItem(integrationObject(code), code)
                                                          ; GenericProduct:Product                                             ; clss_Memory_Flash             ; ElectronicsClassification:1.0:42:ElectronicsClassification:1.0:Internal memory, 6                                                                            ;
```

> [!ATTENTION]
> **Product Price Mapping**
>
> The process is exporting all product prices from Commerce Cloud. The price sent to Google has to have currency [applicable to the target country](https://support.google.com/merchants/answer/160637?hl=en&ref_topic=9216868&sjid=16103813391362173586-EU), which is why filtering is done on the Product Syndication side.

