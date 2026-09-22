// BlacklistTest_p.java
// -----------------------
// part of YaCy
// (C) by Michael Peter Christen; mc@yacy.net
// first published on http://www.anomic.de
// Frankfurt, Germany, 2004
//
// This File is contributed by Alexander Schier
//
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

// You must compile this file with
// javac -classpath .:../classes Blacklist_p.java
// if the shell's current path is HTROOT

package net.yacy.htroot;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.yacy.cora.document.id.DigestURL;
import net.yacy.cora.protocol.RequestHeader;
import net.yacy.repository.Blacklist;
import net.yacy.repository.Blacklist.BlacklistType;
import net.yacy.search.Switchboard;
import net.yacy.server.serverObjects;
import net.yacy.server.serverSwitch;

public class BlacklistTest_p {

    public static serverObjects respond(@SuppressWarnings("unused") final RequestHeader header, final serverObjects post, @SuppressWarnings("unused") final serverSwitch env) {

        final serverObjects prop = new serverObjects();
        prop.putHTML("blacklistEngine", Blacklist.getEngineInfo());

        // do all post operations
        if(post != null && post.containsKey("testList")) {
            prop.put("testlist", "1");
            String urlstring = post.get("testurl", "");
            if (!urlstring.startsWith("http://") &&
                    !urlstring.startsWith("https://") &&
                    !urlstring.startsWith("ftp://") &&
                    !urlstring.startsWith("smb://") &&
                    !urlstring.startsWith("file://")) urlstring = "http://" + urlstring;
            DigestURL testurl = null;
            try {
                testurl = new DigestURL(urlstring);
            } catch (final MalformedURLException e) {
            	testurl = null;
            }
            if(testurl != null) {
                prop.putHTML("url",testurl.toNormalform(false));
                prop.putHTML("testlist_url",testurl.toNormalform(false));
                boolean isblocked = false;
                final Map<String, Set<String>> matchingRules = new TreeMap<>();
                final Set<String> cachedOnly = new TreeSet<>();
                for (final BlacklistType type : BlacklistType.values()) {
                    final boolean listed = Switchboard.urlBlacklist.isListed(type, testurl);
                    final Set<String> rules = Switchboard.urlBlacklist.getMatchingRules(type, testurl);
                    final String purpose = type == BlacklistType.CRAWLER ? "Crawling"
                            : type == BlacklistType.DHT ? "DHT"
                            : type.name().charAt(0) + type.toString().substring(1);
                    if (listed) {
                        prop.put("testlist_listedin" + type.toString(), "1");
                        isblocked = true;
                        if (rules.isEmpty()) {
                            cachedOnly.add(purpose);
                        }
                    }
                    for (final String rule : rules) {
                        matchingRules.computeIfAbsent(rule, key -> new TreeSet<>()).add(purpose);
                    }
                }
                putMatchingRules(prop, matchingRules);
                prop.put("testlist_cachedonly", cachedOnly.isEmpty() ? 0 : 1);
                prop.putHTML("testlist_cachedonly_types", String.join(", ", cachedOnly));

                if (!isblocked) {
                    prop.put("testlist_isnotblocked", "1");
                }
            }
            else {
                prop.putHTML("url",urlstring);
                prop.put("testlist", "2");
            }
        } else {
            prop.putHTML("url", "http://");
        }
        return prop;
    }

    static void putMatchingRules(final serverObjects prop, final Map<String, Set<String>> rules) {
        prop.put("testlist_matchdetails", rules.isEmpty() ? 0 : 1);
        prop.put("testlist_matchdetails_count", rules.size());
        int row = 0;
        for (final Map.Entry<String, Set<String>> entry : rules.entrySet()) {
            final String prefix = "testlist_matchdetails_rows_" + row + "_";
            prop.putUrlEncodedHTML(prefix + "rule", entry.getKey());
            prop.putHTML(prefix + "types", String.join(", ", entry.getValue()));
            row++;
        }
        prop.put("testlist_matchdetails_rows", row);
    }

}
