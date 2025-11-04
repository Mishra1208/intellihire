package com.intellihire.backend.model;

public record SearchRequest(String q, String location, Boolean remote, Integer page, Integer pageSize) {
    public String qSafe() { return q == null ? "" : q; }
    public String locSafe() { return location == null ? "" : location; }
    public boolean remoteSafe() { return remote != null && remote; }
    public int pageSafe() { return (page == null || page < 1) ? 1 : page; }
    public int sizeSafe() { return (pageSize == null || pageSize < 1 || pageSize > 50) ? 20 : pageSize; }
}
