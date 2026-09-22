package net.yacy.htroot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;
import org.jsoup.Jsoup;

import net.yacy.server.http.TemplateEngine;
import net.yacy.server.serverObjects;

public class BlacklistTestPageTest {
    private String render(final serverObjects prop) throws Exception {
        final String template = new String(Files.readAllBytes(Paths.get("htroot/BlacklistTest_p.html")),
                StandardCharsets.UTF_8).replaceAll("#%[^%]+%#", "");
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        TemplateEngine.writeTemplate("BlacklistTest_p.html",
                new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)), out, prop);
        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    public void rendersAllRulesAndCombinedPurposesInTheActualTemplate() throws Exception {
        final serverObjects prop = new serverObjects();
        prop.put("testlist", 1);
        final Map<String, Set<String>> rules = new TreeMap<>();
        rules.put("*.example.org/.*", new TreeSet<>(Arrays.asList("Crawling", "Search")));
        rules.put("example\\.org/news/.*", Collections.singleton("DHT"));
        BlacklistTest_p.putMatchingRules(prop, rules);
        assertEquals("2", prop.get("testlist_matchdetails_rows"));
        final String html = render(prop);
        assertTrue(html.contains("Matching rules (2)"));
        assertTrue(html.contains("<code>*.example.org/.*</code>"));
        assertEquals("example\\.org/news/.*", Jsoup.parse(html).select("tbody code").get(1).text());
        assertTrue(html.contains("Crawling, Search"));
        assertFalse(html.contains("No matching active rules."));
    }

    @Test
    public void escapesRuleTextInsteadOfRenderingMarkup() throws Exception {
        final serverObjects prop = new serverObjects();
        prop.put("testlist", 1);
        BlacklistTest_p.putMatchingRules(prop, Collections.singletonMap("example.org/<script>&\".*",
                Collections.singleton("Search")));
        final String html = render(prop);
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("&amp;"));
        assertTrue(html.contains("&quot;"));
    }

    @Test
    public void preservesEncodedPathsPlusSignsAndBackslashes() throws Exception {
        final serverObjects prop = new serverObjects();
        prop.put("testlist", 1);
        final String rule = "example\\.org/caf%C3%A9\\?q=a+b&x=1";
        BlacklistTest_p.putMatchingRules(prop, Collections.singletonMap(rule, Collections.singleton("Search")));
        assertEquals(rule, Jsoup.parse(render(prop)).select("tbody code").first().text());
    }

    @Test
    public void distinguishesCacheOnlyBlockFromActiveMatch() throws Exception {
        final serverObjects prop = new serverObjects();
        prop.put("testlist", 1);
        prop.put("testlist_cachedonly", 1);
        prop.putHTML("testlist_cachedonly_types", "Crawling, Search");
        BlacklistTest_p.putMatchingRules(prop, Collections.emptyMap());
        final String html = render(prop);
        assertTrue(html.contains("No matching active rules."));
        assertTrue(html.contains("The block for Crawling, Search is cached; no current rule matched."));
        assertFalse(html.contains("Matching rules ("));
    }

    @Test
    public void initialPageAndInvalidUrlDoNotShowMatchResults() throws Exception {
        final serverObjects initial = BlacklistTest_p.respond(null, null, null);
        assertEquals("http://", initial.get("url"));
        assertFalse(render(initial).contains("Matching rules ("));
        final serverObjects post = new serverObjects();
        post.put("testList", "Test");
        post.put("testurl", "http://[");
        final serverObjects invalid = BlacklistTest_p.respond(null, post, null);
        assertEquals("2", invalid.get("testlist"));
        assertTrue(render(invalid).contains("The tested URL was not valid."));
        assertFalse(render(invalid).contains("Matching rules ("));
    }
}
