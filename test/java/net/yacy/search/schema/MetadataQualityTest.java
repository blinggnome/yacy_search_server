package net.yacy.search.schema;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.solr.common.SolrDocument;
import org.junit.Test;

public class MetadataQualityTest {

    @Test
    public void testGenericYouTubeMetadataIsPoor() {
        final SolrDocument document = new SolrDocument();
        document.setField(CollectionSchema.sku.getSolrFieldName(), "https://www.youtube.com/watch?v=example");
        document.setField(CollectionSchema.title.getSolrFieldName(), "- YouTube");
        document.setField(CollectionSchema.description_txt.getSolrFieldName(),
                "Enjoy the videos and music you love, upload original content, and share it all with friends, family, and the world on YouTube.");

        assertTrue(MetadataQuality.isPoor(document));
    }

    @Test
    public void testUsefulMetadataIsNotPoor() {
        final SolrDocument document = new SolrDocument();
        document.setField(CollectionSchema.sku.getSolrFieldName(), "https://www.youtube.com/watch?v=example");
        document.setField(CollectionSchema.title.getSolrFieldName(), "A useful and specific video title");
        document.setField(CollectionSchema.description_txt.getSolrFieldName(),
                "YouTube video by Example Channel: A useful and specific video title");
        document.setField(CollectionSchema.author.getSolrFieldName(), "Example Channel");

        assertFalse(MetadataQuality.isPoor(document));
    }
}
