package me.kubbidev.renapowered.common.storage.implementation.mongodb;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entry;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entity;

import java.lang.reflect.Field;
import java.util.*;

public final class MongoEntity {
    private final List<MongoColumn> columnList = new ArrayList<>();

    @Getter
    private String collectionName;

    public MongoEntity(Class<?> tableType) {
        processEntity(tableType);
        processEntries(tableType);
    }

    public List<MongoColumn> getColumnList() {
        return ImmutableList.copyOf(this.columnList);
    }

    private void processEntity(Class<?> tableType) {
        Entity annotation = tableType.getAnnotation(Entity.class);
        if (annotation == null) {
            throw new NoSuchElementException("The class: " + tableType.getSimpleName()
                    + " does not extend the @Entity annotation");
        }
        this.collectionName = annotation.name();
    }

    private void processEntries(Class<?> tableType) {
        for (Field field : tableType.getDeclaredFields()) {
            var annotation = field.getAnnotation(Entry.class);
            if (annotation == null) {
                continue;
            }
            if (!field.trySetAccessible()) {
                continue;
            }
            MongoColumn column = new MongoColumn(this, field, annotation.name());
            this.columnList.add(column);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof MongoEntity other))
            return false;

        return this.collectionName.equals(other.collectionName);
    }

    @Override
    public int hashCode() {
        return this.collectionName.hashCode();
    }

    @Override
    public String toString() {
        return "StoredEntity(name=" + this.collectionName + ")";
    }
}
