package co.tinode.tinodesdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;

import co.tinode.tinodesdk.Tinode;

/**
 * Common type of the `private` field of {meta}: holds structured
 * data, such as comment and archival status.
 */
public class PrivateType extends HashMap<String,Object> {
    public PrivateType() {
        super();
    }

    @JsonIgnore
    private Object getValue(String name) {
        Object value = get(name);
        if (Tinode.isNull(value)) {
            value = null;
        }
        return value;
    }

    @JsonIgnore
    public String getComment() {
        try {
            return (String) getValue("comment");
        } catch (ClassCastException ignored) {}
        return null;
    }

    @JsonIgnore
    public void setComment(String comment) {
        put("comment", comment != null && comment.length() > 0 ? comment : Tinode.NULL_VALUE);
    }

    @JsonIgnore
    public Boolean isArchived() {
        try {
            return (Boolean) getValue("arch");
        } catch (ClassCastException ignored) {}
        return Boolean.FALSE;
    }

    @JsonIgnore
    public void setArchived(boolean arch) {
        put("arch", arch ? true : Tinode.NULL_VALUE);
    }

    @JsonIgnore
    public Integer getTop() {
        Integer data = 0;
        Object top = get("top");
        if (top != null) {
            if (top instanceof Integer) {
                data = (Integer) top;
            } else {
                data = Integer.parseInt(top.toString());
            }
        } else {
        }

        return data;
    }

    @JsonIgnore
    public Integer setTop(Integer top) {
        return (Integer) put("top", top);
    }
}
