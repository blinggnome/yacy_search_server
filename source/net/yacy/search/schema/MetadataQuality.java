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
}
