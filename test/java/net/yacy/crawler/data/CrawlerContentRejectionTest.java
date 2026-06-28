package net.yacy.crawler.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.junit.Test;

import net.yacy.cora.document.id.DigestURL;
import net.yacy.document.Document;

public class CrawlerContentRejectionTest {

    @Test
    public void testRulesMatchCaseInsensitiveText() throws Exception {
        final CrawlerContentRejection rejection = new CrawlerContentRejection(tempDirectory());
        rejection.addRule("casino bonus");

        assertEquals("casino bonus", rejection.firstMatchingRule(document(
                "Useful title",
                "Useful description",
                "This parked page is now a CASINO BONUS advertisement.")));
    }

    @Test
    public void testRulesMatchDescription() throws Exception {
        final CrawlerContentRejection rejection = new CrawlerContentRejection(tempDirectory());
        rejection.addRule("domain is for sale");

        assertEquals("domain is for sale", rejection.firstMatchingRule(document(
                "Example",
                "This domain is for sale",
                "")));
    }

    @Test
    public void testNoMatchReturnsNull() throws Exception {
        final CrawlerContentRejection rejection = new CrawlerContentRejection(tempDirectory());
        rejection.addRule("casino bonus");

        assertNull(rejection.firstMatchingRule(document(
                "Research article",
                "Useful article summary",
                "This document contains normal content.")));
    }

    private static File tempDirectory() {
        final File directory = new File(System.getProperty("java.io.tmpdir"),
                "CrawlerContentRejectionTest-" + System.nanoTime());
        directory.mkdirs();
        directory.deleteOnExit();
        return directory;
    }

    private static Document document(final String title, final String description, final String text) throws MalformedURLException {
        return new Document(
                new DigestURL("https://example.org/page.html"),
                "text/html",
                "UTF-8",
                null,
                Collections.emptySet(),
                new String[0],
                new ArrayList<String>(Collections.singletonList(title)),
                "",
                "",
                new String[0],
                new ArrayList<String>(description.length() == 0 ? Collections.emptyList() : Collections.singletonList(description)),
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
