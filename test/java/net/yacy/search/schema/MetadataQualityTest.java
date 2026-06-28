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
    public void testGenericYouTubeWatchPageIsStub() throws MalformedURLException {
        final Document document = document(
                "https://www.youtube.com/watch?v=7Djp08YT0U8",
                Collections.singletonList("- YouTube"),
                Collections.singletonList("Enjoy the videos and music you love, upload original content, and share it all with friends, family, and the world on YouTube."),
                "- YouTube. \n\nAbout Press Copyright Contact us Creators Advertise Developers Terms Privacy Policy & Safety How YouTube works Test new features NFL Sunday Ticket \n\n(c) 2026 Google LLC.");

        assertTrue(MetadataQuality.isGenericYouTubeStub(document));
    }

    @Test
    public void testUsefulYouTubeWatchPageIsNotStub() throws MalformedURLException {
        final Document document = document(
                "https://www.youtube.com/watch?v=example",
                Collections.singletonList("A useful and specific video title"),
                Collections.singletonList("YouTube video by Example Channel: A useful and specific video title"),
                "");

        assertFalse(MetadataQuality.isGenericYouTubeStub(document));
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

    @Test
    public void testParsed404PageIsRejected() throws MalformedURLException {
        final Document document = document(
                "https://www.ct-tc.gc.ca/CasesAffaires/findCase-eng.asp",
                Collections.singletonList("Error 404 / Erreur 404"),
                Collections.emptyList(),
                "ERROR 404 Page not found! The page you are looking for has either been moved or maybe completely removed from our server.");

        assertTrue(MetadataQuality.isErrorPage(document));
    }

    @Test
    public void testGenericSiteDescriptionDoesNotSave404Page() throws MalformedURLException {
        final Document document = document(
                "https://www.zerohedge.com/users/nope-1004",
                Collections.singletonList("404 | ZeroHedge"),
                Collections.singletonList("ZeroHedge - On a long enough timeline, the survival rate for everyone drops to zero"),
                "We couldn't find the content you're looking for.");

        assertTrue(MetadataQuality.isErrorPage(document));
    }

    @Test
    public void test404MentionInRealContentIsNotRejected() throws MalformedURLException {
        final Document document = document(
                "https://example.org/articles/http-errors",
                Collections.singletonList("How to Fix 404 Errors"),
                Collections.singletonList("A technical guide to diagnosing missing page responses."),
                "This article explains why a website may return a 404 response and how to repair broken links.");

        assertFalse(MetadataQuality.isErrorPage(document));
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
