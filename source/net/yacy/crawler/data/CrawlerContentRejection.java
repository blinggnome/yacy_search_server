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
import java.nio.charset.StandardCharsets;
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

    private final File file;
    private volatile long lastModified;
    private volatile List<String> rules;

    public CrawlerContentRejection(final File listsPath) {
        this.file = new File(listsPath, FILENAME);
        this.lastModified = Long.MIN_VALUE;
        this.rules = new ArrayList<>();
    }

    public synchronized void addRule(final String rule) throws IOException {
        final List<String> updatedRules = new ArrayList<>(getRules());
        updatedRules.add(rule);
        setRules(updatedRules);
    }

    public synchronized void deleteRules(final Collection<String> rulesToDelete) throws IOException {
        if (rulesToDelete == null || rulesToDelete.isEmpty()) return;

        final Map<String, String> normalizedRulesToDelete = new LinkedHashMap<>();
        for (final String rule : rulesToDelete) {
            final String normalized = normalize(rule);
            if (normalized.length() > 0) normalizedRulesToDelete.put(normalized.toLowerCase(Locale.ROOT), normalized);
        }
        if (normalizedRulesToDelete.isEmpty()) return;

        final List<String> updatedRules = new ArrayList<>();
        for (final String rule : getRules()) {
            if (!normalizedRulesToDelete.containsKey(rule.toLowerCase(Locale.ROOT))) updatedRules.add(rule);
        }
        setRules(updatedRules);
    }

    public synchronized void setRules(final Collection<String> newRules) throws IOException {
        final List<String> normalizedRules = normalizeRules(newRules);
        final File parent = this.file.getParentFile();
        if (parent != null) parent.mkdirs();

        try (BufferedWriter writer = Files.newBufferedWriter(this.file.toPath(), StandardCharsets.UTF_8)) {
            for (final String rule : normalizedRules) {
                writer.write(rule);
                writer.newLine();
            }
        }

        this.rules = normalizedRules;
        this.lastModified = this.file.lastModified();
    }

    public List<String> getRules() {
        reloadIfNeeded();
        return new ArrayList<>(this.rules);
    }

    public String firstMatchingRule(final Document document) {
        if (document == null) return null;

        final List<String> currentRules = getRules();
        if (currentRules.isEmpty()) return null;

        final String haystack = new StringBuilder()
                .append(clean(document.dc_title())).append('\n')
                .append(clean(first(document.dc_description()))).append('\n')
                .append(clean(document.getTextString()))
                .toString()
                .toLowerCase(Locale.ROOT);

        if (haystack.length() == 0) return null;

        for (final String rule : currentRules) {
            if (haystack.contains(rule.toLowerCase(Locale.ROOT))) return rule;
        }
        return null;
    }

    private void reloadIfNeeded() {
        final long fileLastModified = this.file.exists() ? this.file.lastModified() : 0L;
        if (fileLastModified == this.lastModified) return;

        synchronized (this) {
            final long synchronizedLastModified = this.file.exists() ? this.file.lastModified() : 0L;
            if (synchronizedLastModified == this.lastModified) return;

            this.rules = loadRules();
            this.lastModified = synchronizedLastModified;
        }
    }

    private List<String> loadRules() {
        if (!this.file.exists()) return new ArrayList<>();

        final List<String> loadedRules = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(this.file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) loadedRules.add(line);
        } catch (final IOException e) {
            return new ArrayList<>(this.rules);
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

    private static String normalize(final String rule) {
        return rule == null ? "" : rule.replaceAll("\\s+", " ").trim();
    }

    private static String clean(final String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private static String first(final String[] values) {
        return values == null || values.length == 0 ? "" : values[0];
    }
}
