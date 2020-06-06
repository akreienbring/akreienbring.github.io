package de.binformed.platform.model;

public interface BInformedModel {

    // Types
    public static final String NAMESPACE_BINFORMED_CONTENT_MODEL  = "http://www.b-informed.com/model/content/1.0";
    public static final String TYPE_BI_DOC = "document";
    public static final String TYPE_BI_WHITEPAPER = "whitepaper";

    // Aspects
    public static final String ASPECT_BI_WEBABLE = "webable";
    public static final String ASPECT_BI_PRODUCT_RELATED = "productRelated";

    // Properties
    public static final String PROP_PRODUCT = "product";
    public static final String PROP_VERSION = "version";
    public static final String PROP_PUBLISHED = "published";
    public static final String PROP_IS_ACTIVE = "isActive";
    public static final String PROP_EXTERNALID = "externalID";

    // Associations
    public static final String ASSN_RELATED_DOCUMENTS = "relatedDocuments";
}