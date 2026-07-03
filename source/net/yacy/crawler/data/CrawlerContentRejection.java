/**
 *  CrawlerContentRejection
 *  Copyright 2026 by YaCy contributors
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 */

package net.yacy.crawler.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.yacy.document.Document;

public class CrawlerContentRejection {

    public static final String FILENAME = "crawler-content-rejection.txt";
    public static final String POISON_PILL_FILENAME = "crawler-content-poison-pills.txt";
    public static final String WHITELIST_FILENAME = "crawler-content-whitelist.txt";

    private static final int EXCERPT_RADIUS = 80;

    private final File file;
    private final File poisonPillFile;
    private final File whitelistFile;
    private volatile long lastModified;
    private volatile long poisonPillLastModified;
    private volatile long whitelistLastModified;
    private volatile List<String> rules;
    private volatile List<String> poisonPills;
    private volatile List<String> whitelist;

    public CrawlerContentRejection(final File listsPath) {
        this.file = new File(listsPath, FILENAME);
        this.poisonPillFile = new File(listsPath, POISON_PILL_FILENAME);
        this.whitelistFile = new File(listsPath, WHITELIST_FILENAME);
        this.lastModified = Long.MIN_VALUE;
        this.poisonPillLastModified = Long.MIN_VALUE;
        this.whitelistLastModified = Long.MIN_VALUE;
        this.rules = new ArrayList<>();
        this.poisonPills = new ArrayList<>();
        this.whitelist = new ArrayList<>();
    }

    public synchronized void addRule(final String rule) throws IOException {
        final List<String> updatedRules = new ArrayList<>(getRules());
        updatedRules.add(rule);
        setRules(updatedRules);
    }

    public synchronized void addPoisonPill(final String rule) throws IOException {
        final List<String> updatedRules = new ArrayList<>(getPoisonPills());
        updatedRules.add(rule);
        setPoisonPills(updatedRules);
    }

    public synchronized void addWhitelistEntry(final String entry) throws IOException {
        final List<String> updatedEntries = new ArrayList<>(getWhitelist());
        updatedEntries.add(entry);
        setWhitelist(updatedEntries);
    }

    public synchronized void deleteRules(final Collection<String> rulesToDelete) throws IOException {
        deleteRules(this.file, rulesToDelete, getRules(), RuleFile.SOFT_RULES);
    }

    public synchronized void deletePoisonPills(final Collection<String> rulesToDelete) throws IOException {
        deleteRules(this.poisonPillFile, rulesToDelete, getPoisonPills(), RuleFile.POISON_PILLS);
    }

    public synchronized void deleteWhitelistEntries(final Collection<String> entriesToDelete) throws IOException {
        deleteRules(this.whitelistFile, entriesToDelete, getWhitelist(), RuleFile.WHITELIST);
    }

    private synchronized void deleteRules(final File targetFile, final Collection<String> rulesToDelete,
            final List<String> currentRules, final RuleFile ruleFile) throws IOException {
        if (rulesToDelete == null || rulesToDelete.isEmpty()) return;

        final Map<String, String> normalizedRulesToDelete = new LinkedHashMap<>();
        for (final String rule : rulesToDelete) {
            final String normalized = normalize(rule);
            if (normalized.length() > 0) normalizedRulesToDelete.put(normalized.toLowerCase(Locale.ROOT), normalized);
        }
        if (normalizedRulesToDelete.isEmpty()) return;

        final List<String> updatedRules = new ArrayList<>();
        for (final String rule : currentRules) {
            if (!normalizedRulesToDelete.containsKey(rule.toLowerCase(Locale.ROOT))) updatedRules.add(rule);
        }
        setRules(targetFile, updatedRules, ruleFile);
    }

    public synchronized void setRules(final Collection<String> newRules) throws IOException {
        setRules(this.file, newRules, RuleFile.SOFT_RULES);
    }

    public synchronized void setPoisonPills(final Collection<String> newRules) throws IOException {
        setRules(this.poisonPillFile, newRules, RuleFile.POISON_PILLS);
    }

    public synchronized void setWhitelist(final Collection<String> newEntries) throws IOException {
        setRules(this.whitelistFile, normalizeWhitelist(newEntries), RuleFile.WHITELIST);
    }

    private synchronized void setRules(final File targetFile, final Collection<String> newRules, final RuleFile ruleFile) throws IOException {
        final List<String> normalizedRules = normalizeRules(newRules);
        final File parent = targetFile.getParentFile();
        if (parent != null) parent.mkdirs();

        try (BufferedWriter writer = Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8)) {
            for (final String rule : normalizedRules) {
                writer.write(rule);
                writer.newLine();
            }
        }

        if (ruleFile == RuleFile.POISON_PILLS) {
            this.poisonPills = normalizedRules;
            this.poisonPillLastModified = targetFile.lastModified();
        } else if (ruleFile == RuleFile.WHITELIST) {
            this.whitelist = normalizedRules;
            this.whitelistLastModified = targetFile.lastModified();
        } else {
            this.rules = normalizedRules;
            this.lastModified = targetFile.lastModified();
        }
    }

    public List<String> getRules() {
        reloadRulesIfNeeded();
        return new ArrayList<>(this.rules);
    }

    public List<String> getPoisonPills() {
        reloadPoisonPillsIfNeeded();
        return new ArrayList<>(this.poisonPills);
    }

    public List<String> getWhitelist() {
        reloadWhitelistIfNeeded();
        return new ArrayList<>(this.whitelist);
    }

    public boolean isWhitelisted(final String host) {
        final String normalizedHost = normalizeHost(host);
        if (normalizedHost.length() == 0) return false;
        for (final String entry : getWhitelist()) {
            if (normalizedHost.equals(entry) || normalizedHost.endsWith('.' + entry)) return true;
        }
        return false;
    }

    public String firstMatchingRule(final Document document) {
        final RuleMatch match = firstRuleMatch(document, false);
        return match == null ? null : match.rule;
    }

    public String firstMatchingPoisonPill(final Document document) {
        final RuleMatch match = firstRuleMatch(document, true);
        return match == null ? null : match.rule;
    }

    public RuleMatch firstRuleMatch(final Document document) {
        return firstRuleMatch(document, false);
    }

    public RuleMatch firstPoisonPillMatch(final Document document) {
        return firstRuleMatch(document, true);
    }

    private RuleMatch firstRuleMatch(final Document document, final boolean poisonPills) {
        if (document == null) return null;

        final List<String> currentRules = poisonPills ? getPoisonPills() : getRules();
        if (currentRules.isEmpty()) return null;

        RuleMatch match = firstMatchInText(currentRules, "title", clean(document.dc_title()));
        if (match != null) return match;
        match = firstMatchInText(currentRules, "description", clean(first(document.dc_description())));
        if (match != null) return match;
        return firstMatchInText(currentRules, "body", clean(document.getTextString()));
    }

    public String firstMatchingRule(final byte[] source, final String charsetName) {
        final RuleMatch match = firstRuleMatch(source, charsetName, false);
        return match == null ? null : match.rule;
    }

    public String firstMatchingPoisonPill(final byte[] source, final String charsetName) {
        final RuleMatch match = firstRuleMatch(source, charsetName, true);
        return match == null ? null : match.rule;
    }

    public RuleMatch firstRuleMatch(final byte[] source, final String charsetName) {
        return firstRuleMatch(source, charsetName, false);
    }

    public RuleMatch firstPoisonPillMatch(final byte[] source, final String charsetName) {
        return firstRuleMatch(source, charsetName, true);
    }

    private RuleMatch firstRuleMatch(final byte[] source, final String charsetName, final boolean poisonPills) {
        if (source == null || source.length == 0) return null;

        final List<String> currentRules = poisonPills ? getPoisonPills() : getRules();
        if (currentRules.isEmpty()) return null;

        return firstMatchInText(currentRules, "raw source", clean(new String(source, charset(charsetName))));
    }

    private static RuleMatch firstMatchInText(final List<String> currentRules, final String location, final String text) {
        if (text == null || text.length() == 0) return null;
        final String haystack = text.toLowerCase(Locale.ROOT);
        for (final String rule : currentRules) {
            final int offset = haystack.indexOf(rule.toLowerCase(Locale.ROOT));
            if (offset >= 0) return new RuleMatch(rule, location, excerpt(text, offset, rule.length()));
        }
        return null;
    }

    private void reloadRulesIfNeeded() {
        final long fileLastModified = this.file.exists() ? this.file.lastModified() : 0L;
        if (fileLastModified == this.lastModified) return;

        synchronized (this) {
            final long synchronizedLastModified = this.file.exists() ? this.file.lastModified() : 0L;
            if (synchronizedLastModified == this.lastModified) return;

            this.rules = loadRules();
            this.lastModified = synchronizedLastModified;
        }
    }

    private void reloadPoisonPillsIfNeeded() {
        final long fileLastModified = this.poisonPillFile.exists() ? this.poisonPillFile.lastModified() : 0L;
        if (fileLastModified == this.poisonPillLastModified) return;

        synchronized (this) {
            final long synchronizedLastModified = this.poisonPillFile.exists() ? this.poisonPillFile.lastModified() : 0L;
            if (synchronizedLastModified == this.poisonPillLastModified) return;

            this.poisonPills = loadRules(this.poisonPillFile, this.poisonPills);
            this.poisonPillLastModified = synchronizedLastModified;
        }
    }

    private void reloadWhitelistIfNeeded() {
        final long fileLastModified = this.whitelistFile.exists() ? this.whitelistFile.lastModified() : 0L;
        if (fileLastModified == this.whitelistLastModified) return;

        synchronized (this) {
            final long synchronizedLastModified = this.whitelistFile.exists() ? this.whitelistFile.lastModified() : 0L;
            if (synchronizedLastModified == this.whitelistLastModified) return;

            this.whitelist = loadRules(this.whitelistFile, this.whitelist);
            this.whitelistLastModified = synchronizedLastModified;
        }
    }

    private List<String> loadRules() {
        return loadRules(this.file, this.rules);
    }

    private static List<String> loadRules(final File file, final List<String> fallbackRules) {
        if (!file.exists()) return new ArrayList<>();

        final List<String> loadedRules = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) loadedRules.add(line);
        } catch (final IOException e) {
            return new ArrayList<>(fallbackRules);
        }
        return normalizeRules(loadedRules);
    }

    private static List<String> normalizeRules(final Collection<String> rules) {
        final Map<String, String> normalizedRules = new LinkedHashMap<>();
        if (rules != null) {
            for (final String rule : rules) {
                final String normalized = normalize(rule);
                if (normalized.length() == 0 || normalized.startsWith("#")) continue;
                normalizedRules.put(normalized.toLowerCase(Locale.ROOT), normalized);
            }
        }
        return new ArrayList<>(normalizedRules.values());
    }

    private static List<String> normalizeWhitelist(final Collection<String> entries) {
        final Map<String, String> normalizedEntries = new LinkedHashMap<>();
        if (entries != null) {
            for (final String entry : entries) {
                final String normalized = normalizeHost(entry);
                if (normalized.length() == 0 || normalized.startsWith("#")) continue;
                normalizedEntries.put(normalized, normalized);
            }
        }
        return new ArrayList<>(normalizedEntries.values());
    }

    private static String normalize(final String rule) {
        return rule == null ? "" : rule.replaceAll("\\s+", " ").trim();
    }

    private static String normalizeHost(final String host) {
        if (host == null) return "";
        String normalized = host.trim().toLowerCase(Locale.ROOT);
        final int schemeIndex = normalized.indexOf("://");
        if (schemeIndex >= 0) normalized = normalized.substring(schemeIndex + 3);
        final int slashIndex = normalized.indexOf('/');
        if (slashIndex >= 0) normalized = normalized.substring(0, slashIndex);
        final int portIndex = normalized.indexOf(':');
        if (portIndex >= 0) normalized = normalized.substring(0, portIndex);
        while (normalized.startsWith("*.")) normalized = normalized.substring(2);
        while (normalized.startsWith(".")) normalized = normalized.substring(1);
        while (normalized.endsWith(".")) normalized = normalized.substring(0, normalized.length() - 1);
        return normalized.replaceAll("\\s+", "");
    }

    private static String excerpt(final String text, final int offset, final int length) {
        final int start = Math.max(0, offset - EXCERPT_RADIUS);
        final int end = Math.min(text.length(), offset + Math.max(length, 0) + EXCERPT_RADIUS);
        final String prefix = start > 0 ? "..." : "";
        final String suffix = end < text.length() ? "..." : "";
        return prefix + text.substring(start, end).replaceAll("\\s+", " ").trim() + suffix;
    }

    private static String clean(final String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private static Charset charset(final String charsetName) {
        if (charsetName != null && charsetName.trim().length() > 0) {
            try {
                return Charset.forName(charsetName.trim());
            } catch (final IllegalCharsetNameException | UnsupportedCharsetException e) {
            }
        }
        return StandardCharsets.UTF_8;
    }

    private static String first(final String[] values) {
        return values == null || values.length == 0 ? "" : values[0];
    }

    private enum RuleFile {
        SOFT_RULES,
        POISON_PILLS,
        WHITELIST
    }

    public static final class RuleMatch {
        public final String rule;
        public final String location;
        public final String excerpt;

        private RuleMatch(final String rule, final String location, final String excerpt) {
            this.rule = rule;
            this.location = location;
            this.excerpt = excerpt;
        }

        public String shortSummary() {
            return "rule '" + this.rule + "' matched in " + this.location;
        }

        public String summary() {
            return shortSummary()
                    + (this.excerpt.length() == 0 ? "" : ": \"" + this.excerpt + "\"");
        }
    }
}
