// DeadDomains_p.java

package net.yacy.htroot;

import net.yacy.cora.protocol.RequestHeader;
import net.yacy.search.Switchboard;
import net.yacy.server.serverObjects;
import net.yacy.server.serverSwitch;

public class DeadDomains_p {

    public static serverObjects respond(@SuppressWarnings("unused") final RequestHeader header, final serverObjects post, final serverSwitch env) {
        final Switchboard sb = (Switchboard) env;
        final serverObjects prop = new serverObjects();

        prop.put("status", "0");
        if (post != null && post.containsKey("save")) {
            final boolean enabled = post.containsKey("autoCleanup");
            sb.setConfig(Switchboard.CRAWLER_DEAD_DOMAIN_AUTO_CLEANUP, enabled);
            prop.put("status", "1");
            prop.put("status_success", "1");
            prop.putHTML("status_message", enabled
                    ? "Dead-domain automatic cleanup enabled."
                    : "Dead-domain automatic cleanup disabled.");
        }

        prop.put("autoCleanup", sb.getConfigBool(Switchboard.CRAWLER_DEAD_DOMAIN_AUTO_CLEANUP, false) ? "1" : "0");
        return prop;
    }
}
