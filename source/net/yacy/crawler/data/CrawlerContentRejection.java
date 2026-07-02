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

    private final File file;
    private final File poisonPillFile;
    private volatile long lastModified;
    private volatile long poisonPillLastModified;
    private volatile List<String> rules;
    private volatile List<String> poisonPills;

    public CrawlerContentRejection(final File listsPath) {
        this.file = new File(listsPath, FILENAME);
        this.poisonPillFile = new File(listsPath, POISON_PILL_FILENAME);
        this.lastModified = Long.MIN_VALUE;
        this.poisonPillLastModified = Long.MIN_VALUE;
        this.rules = new ArrayList<>();
        this.poisonPills = new ArrayList<>();
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

    public synchronized void deleteRules(final Collection<String> rulesToDelete) throws IOException {
        deleteRules(this.file, rulesToDelete, getRules(), false);
    }

    public synchronized void deletePoisonPills(final Collection<String> rulesToDelete) throws IOException {
        deleteRules(this.poisonPillFile, rulesToDelete, getPoisonPills(), true);
    }

    private synchronized void deleteRules(final File targetFile, final Collection<String> rulesToDelete,
            final List<String> currentRules, final boolean poisonPills) throws IOException {
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
        setRules(targetFile, updatedRules, poisonPills);
    }

    public synchronized void setRules(final Collection<String> newRules) throws IOException {
        setRules(this.file, newRules, false);
    }

    public synchronized void setPoisonPills(final Collection<String> newRules) throws IOException {
        setRules(this.poisonPillFile, newRules, true);
    }

    private synchronized void setRules(final File targetFile, final Collection<String> newRules, final boolean poisonPills) throws IOException {
        final List<String> normalizedRules = normalizeRules(newRules);
        final File parent = targetFile.getParentFile();
        if (parent != null) parent.mkdirs();

        try (BufferedWriter writer = Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8)) {
            for (final String rule : normalizedRules) {
                writer.write(rule);
                writer.newLine();
            }
        }

        if (poisonPills) {
            this.poisonPills = normalizedRules;
            this.poisonPillLastModified = targetFile.lastModified();
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

    public String firstMatchingRule(final Document document) {
        return firstMatchingRule(document, false);
    }

    public String firstMatchingPoisonPill(final Document document) {
        return firstMatchingRule(document, true);
    }

    private String firstMatchingRule(final Document document, final boolean poisonPills) {
        if (document == null) return null;

        final List<String> currentRules = poisonPills ? getPoisonPills() : getRules();
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

    public String firstMatchingRule(final byte[] source, final String charsetName) {
        return firstMatchingRule(source, charsetName, false);
    }

    public String firstMatchingPoisonPill(final byte[] source, final String charsetName) {
        return firstMatchingRule(source, charsetName, true);
    }

    private String firstMatchingRule(final byte[] source, final String charsetName, final boolean poisonPills) {
        if (source == null || source.length == 0) return null;

        final List<String> currentRules = poisonPills ? getPoisonPills() : getRules();
        if (currentRules.isEmpty()) return null;

        final String haystack = clean(new String(source, charset(charsetName))).toLowerCase(Locale.ROOT);
        if (haystack.length() == 0) return null;

        for (final String rule : currentRules) {
            if (haystack.contains(rule.toLowerCase(Locale.ROOT))) return rule;
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

    private static String normalize(final String rule) {
        return rule == null ? "" : rule.replaceAll("\\s+", " ").trim();
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
}
