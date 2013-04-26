package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.ElementId;

import java.util.HashMap;
import java.util.Map;

public class StreamContentOrder {
    private static final Map<ElementId, Map<String, Integer>> tagSortIdOrder =
            new HashMap<ElementId, Map<String, Integer>>();

    public StreamContentOrder() {
    }

    public void put(ElementId tagId, String[] sortIds) {
        Map<String, Integer> sortIdOrder = tagSortIdOrder.get(tagId);
        if (sortIdOrder == null) {
            sortIdOrder = new HashMap<String, Integer>();
            tagSortIdOrder.put(tagId, sortIdOrder);
        }

        for (int i = 0; i < sortIds.length; ++i) {
            sortIdOrder.put(sortIds[i], i);
        }
    }

    public Map<String, Integer> getSortIdOrder(ElementId tagId) {
        return tagSortIdOrder.get(tagId);
    }

    public int getSortOrder(ElementId tagId, String sortId) {
        if (sortId == null)
            return Integer.MAX_VALUE;
        
        final Map<String, Integer> sortIdOrder = getSortIdOrder(tagId);
        final Integer sortOrder = sortIdOrder != null ? sortIdOrder.get(sortId) : null;
        return sortOrder != null ? sortOrder : Integer.MAX_VALUE;
    }
}
