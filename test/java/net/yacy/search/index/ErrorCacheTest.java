package net.yacy.search.index;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ErrorCacheTest {

    @Test
    public void testHttpStatusRemovesExistingDocument() {
        assertFalse(ErrorCache.httpStatusRemovesExistingDocument(-1));
        assertFalse(ErrorCache.httpStatusRemovesExistingDocument(200));
        assertFalse(ErrorCache.httpStatusRemovesExistingDocument(301));
        assertTrue(ErrorCache.httpStatusRemovesExistingDocument(400));
        assertTrue(ErrorCache.httpStatusRemovesExistingDocument(403));
        assertTrue(ErrorCache.httpStatusRemovesExistingDocument(404));
        assertTrue(ErrorCache.httpStatusRemovesExistingDocument(500));
    }
}
