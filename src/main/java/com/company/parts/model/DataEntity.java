package com.company.parts.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * @author Orefin
 *         Date: 24.11.18.
 *         Time: 23:41
 *         Class for table metadata description
 */
public class DataEntity {
    private String tableName;
    private Map<String, FieldMetadata> fields;

    public DataEntity(@NotNull String tableName, @NotNull Map<String, FieldMetadata> fields) {
        this.tableName = tableName;
        this.fields = fields;
    }

    public String getTableName() {
        return tableName;
    }

    public Map<String, FieldMetadata> getFields() {
        return Collections.unmodifiableMap(fields);
    }
}
