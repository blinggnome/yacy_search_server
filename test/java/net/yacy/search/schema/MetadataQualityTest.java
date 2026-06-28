package net.yacy.search.schema;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.apache.solr.common.SolrDocument;
import org.junit.Test;

import net.yacy.cora.document.id.DigestURL;
import net.yacy.document.Document;

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

    @Test
    public void testZeroContentSlugTitleIsStub() throws MalformedURLException {
        final Document document = document(
                "https://documents.example/en/privacy-policy",
                Collections.singletonList("Privacy Policy"),
                Collections.emptyList(),
                "");

        assertTrue(MetadataQuality.isZeroContentStub(document));
    }

    @Test
    public void testDescriptionKeepsMetadataOnlyDocument() throws MalformedURLException {
        final Document document = document(
                "https://documents.example/en/privacy-policy",
                Collections.singletonList("Privacy Policy"),
                Collections.singletonList("Privacy policy details for the example service."),
                "");

        assertFalse(MetadataQuality.isZeroContentStub(document));
    }

    @Test
    public void testBodyTextKeepsDocument() throws MalformedURLException {
        final Document document = document(
                "https://www.fifa.com/clubworldcup/news/newsid=1972809/index.html",
                Collections.emptyList(),
                Collections.emptyList(),
                "This document contains actual extracted body text.");

        assertFalse(MetadataQuality.isZeroContentStub(document));
    }

    private static Document document(
            final String url,
            final java.util.List<String> titles,
            final java.util.List<String> descriptions,
            final String text) throws MalformedURLException {
        return new Document(
                new DigestURL(url),
                "text/html",
                "UTF-8",
                null,
                Collections.emptySet(),
                new String[0],
                new ArrayList<String>(titles),
                "",
                "",
                new String[0],
                new ArrayList<String>(descriptions),
                0.0d,
                0.0d,
                text,
                Collections.emptyList(),
                new LinkedHashMap<DigestURL, String>(),
                new LinkedHashMap<>(),
                false,
                null);
    }
}
