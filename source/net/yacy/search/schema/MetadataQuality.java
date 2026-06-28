/**
 *  MetadataQuality
 *  Copyright 2026 by YaCy contributors
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 */

package net.yacy.search.schema;

import java.util.Collection;
import java.util.Locale;

import org.apache.solr.common.SolrDocument;

import net.yacy.document.Document;

public final class MetadataQuality {

    private static final String GENERIC_YOUTUBE_TITLE = "- YouTube";
    private static final String GENERIC_YOUTUBE_DESCRIPTION = "Enjoy the videos and music you love, upload original content, and share it all with friends, family, and the world on YouTube.";

    private MetadataQuality() {
    }

    public static int score(final Document document) {
        if (document == null) return 0;

        int score = 0;
        score += titleScore(document.dc_title(), document.dc_source().toNormalform(true));
        score += descriptionScore(first(document.dc_description()));
        score += authorScore(document.dc_creator());
        return score;
    }

    public static int score(final SolrDocument document) {
        if (document == null) return 0;

        int score = 0;
        score += titleScore(first(document.getFieldValue(CollectionSchema.title.getSolrFieldName())),
                first(document.getFieldValue(CollectionSchema.sku.getSolrFieldName())));
        score += descriptionScore(first(document.getFieldValue(CollectionSchema.description_txt.getSolrFieldName())));
        score += authorScore(first(document.getFieldValue(CollectionSchema.author.getSolrFieldName())));
        return score;
    }

    public static boolean isPoor(final SolrDocument document) {
        return score(document) < 4;
    }

    public static boolean existingIsBetter(final SolrDocument existingDocument, final Document newDocument) {
        return score(existingDocument) > score(newDocument);
    }

    public static boolean isZeroContentStub(final Document document) {
        if (document == null) return false;

        final String url = document.dc_source() == null ? "" : document.dc_source().toNormalform(true);
        final String title = document.dc_title();
        final boolean hasUsableTitle = titleScore(title, url) > 0 && !titleMatchesUrlSlug(title, document);
        final boolean hasUsableDescription = descriptionScore(first(document.dc_description())) > 0;
        final boolean hasBodyText = clean(document.getTextString()).length() > 0;

        return !hasUsableTitle && !hasUsableDescription && !hasBodyText;
    }

    public static boolean isErrorPage(final Document document) {
        if (document == null) return false;

        final String title = clean(document.dc_title()).toLowerCase(Locale.ROOT);
        final String description = clean(first(document.dc_description())).toLowerCase(Locale.ROOT);
        final String body = clean(document.getTextString()).toLowerCase(Locale.ROOT);

        if (!titleIndicatesErrorPage(title)) return false;
        return textIndicatesMissingPage(description) || textIndicatesMissingPage(body);
    }

    private static int titleScore(final String title, final String url) {
        final String cleanTitle = clean(title);
        if (cleanTitle.length() == 0 || GENERIC_YOUTUBE_TITLE.equals(cleanTitle)) return 0;
        if (url != null && cleanTitle.equalsIgnoreCase(url)) return 0;
        if (cleanTitle.length() < 8) return 1;
        return 3;
    }

    private static int descriptionScore(final String description) {
        final String cleanDescription = clean(description);
        if (cleanDescription.length() == 0 || GENERIC_YOUTUBE_DESCRIPTION.equals(cleanDescription)) return 0;
        if (cleanDescription.length() < 40) return 1;
        return 3;
    }

    private static int authorScore(final String author) {
        return clean(author).length() == 0 ? 0 : 1;
    }

    private static String first(final Object value) {
        if (value == null) return "";
        if (value instanceof Collection<?>) {
            final Collection<?> values = (Collection<?>) value;
            if (values.isEmpty()) return "";
            final Object first = values.iterator().next();
            return first == null ? "" : first.toString();
        }
        return value.toString();
    }

    private static String first(final String[] values) {
        return values == null || values.length == 0 ? "" : values[0];
    }

    private static String clean(final String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private static boolean titleIndicatesErrorPage(final String title) {
        if (title.length() == 0) return false;
        return "404".equals(title)
                || title.startsWith("404 ")
                || title.startsWith("404 -")
                || title.startsWith("404 |")
                || title.startsWith("error 404")
                || title.contains("404 not found")
                || title.contains("page not found");
    }

    private static boolean textIndicatesMissingPage(final String text) {
        if (text.length() == 0) return false;
        return text.contains("page not found")
                || text.contains("404 not found")
                || text.contains("content you're looking for")
                || text.contains("content you’re looking for")
                || text.contains("page you are looking for")
                || text.contains("page has either been moved")
                || text.contains("removed from our server")
                || text.contains("go to homepage");
    }

    private static boolean titleMatchesUrlSlug(final String title, final Document document) {
        final String cleanTitle = clean(title);
        if (cleanTitle.length() == 0 || document == null || document.dc_source() == null) return false;
        return cleanTitle.equalsIgnoreCase(titleFromUrlSlug(document));
    }

    private static String titleFromUrlSlug(final Document document) {
        String filename = document.dc_source().getFileName();
        if (filename.length() == 0) {
            final String path = document.dc_source().getPath();
            if (path.length() <= 1) return "";
            final String normalizedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
            final int slash = normalizedPath.lastIndexOf('/');
            filename = slash >= 0 ? normalizedPath.substring(slash + 1) : normalizedPath;
        }
        if (filename.length() == 0 || filename.indexOf('.') >= 0) return "";

        final String[] parts = filename.split("[-_]+");
        final StringBuilder title = new StringBuilder(filename.length());
        for (final String part : parts) {
            if (part.length() == 0) continue;
            if (title.length() > 0) title.append(' ');
            title.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) title.append(part.substring(1));
        }
        return clean(title.toString());
    }
}
