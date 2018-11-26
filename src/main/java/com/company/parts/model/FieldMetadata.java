package com.company.parts.model;


import org.jetbrains.annotations.NotNull;

/**
 * @author Orefin
 *         Date: 24.11.18.
 *         Time: 23:32
 *         Field metadata description (field name and field type)
 */
public class FieldMetadata {
    private String name;
    private DataTypes type;

    public FieldMetadata(@NotNull String name, @NotNull DataTypes type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public DataTypes getType() {
        return type;
    }
}
