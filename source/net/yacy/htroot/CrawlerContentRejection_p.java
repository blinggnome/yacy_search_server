// CrawlerContentRejection_p.java

package net.yacy.htroot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.yacy.document.parser.html.CharacterCoding;
import net.yacy.cora.protocol.RequestHeader;
import net.yacy.crawler.data.CrawlerContentRejection;
import net.yacy.data.ListManager;
import net.yacy.search.Switchboard;
import net.yacy.server.serverObjects;
import net.yacy.server.serverSwitch;

public class CrawlerContentRejection_p {

    public static serverObjects respond(@SuppressWarnings("unused") final RequestHeader header, final serverObjects post, final serverSwitch env) {
        final Switchboard sb = (Switchboard) env;
        final serverObjects prop = new serverObjects();
        final CrawlerContentRejection rejectionRules = sb.crawlerContentRejection == null
                ? new CrawlerContentRejection(ListManager.listsPath)
                : sb.crawlerContentRejection;

        prop.put("status", "0");

        if (post != null) {
            try {
                if (post.containsKey("addRule")) {
                    rejectionRules.addRule(post.get("newRule", ""));
                    prop.put("status", "1");
                    prop.put("status_success", "1");
                    prop.putHTML("status_message", "Crawler content rejection rule added.");
                } else if (post.containsKey("deleteRules")) {
                    rejectionRules.deleteRules(Arrays.asList(post.getAll("selectedRule.*")));
                    prop.put("status", "1");
                    prop.put("status_success", "1");
                    prop.putHTML("status_message", "Selected crawler content rejection rules deleted.");
                } else if (post.containsKey("saveRules")) {
                    rejectionRules.setRules(Arrays.asList(post.get("rulesText", "").split("\\r?\\n")));
                    prop.put("status", "1");
                    prop.put("status_success", "1");
                    prop.putHTML("status_message", "Crawler content rejection rules saved.");
                }
            } catch (final IOException e) {
                prop.put("status", "1");
                prop.put("status_success", "0");
                prop.putHTML("status_message", "Crawler content rejection rules could not be saved: " + e.getMessage());
            }
        }

        final List<String> rules = rejectionRules.getRules();
        prop.putNum("count", rules.size());
        prop.put("hasRules", rules.isEmpty() ? "0" : "1");

        final StringBuilder rulesText = new StringBuilder();
        for (int i = 0; i < rules.size(); i++) {
            final String rule = rules.get(i);
            prop.put("hasRules_ruleList_" + i + "_count", i);
            prop.put("hasRules_ruleList_" + i + "_rule", CharacterCoding.unicode2html(rule, true));
            prop.put("hasRules_ruleList_" + i + "_dark", (i & 1) == 0 ? "1" : "0");
            rulesText.append(rule).append('\n');
        }
        prop.put("hasRules_ruleList", rules.size());
        prop.put("rulesText", CharacterCoding.unicode2html(rulesText.toString(), true));

        return prop;
    }
}
