package me.kubbidev.renapowered.common.storage.implementation.mongodb;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entry;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Entity;
import me.kubbidev.renapowered.common.storage.implementation.mongodb.annotation.Id;

import java.lang.reflect.Field;
import java.util.*;

public final class MongoEntity {
    private final List<MongoEntry> entries = new ArrayList<>();

    @Getter
    private String collectionName;
    private String id = "_id";

    public MongoEntity(Class<?> tableType) {
        processEntity(tableType);
        processEntries(tableType);
    }

    public List<MongoEntry> getEntries() {
        return ImmutableList.copyOf(this.entries);
    }

    public String getId() {
        return this.id.equalsIgnoreCase("id") ? '_' + this.id : this.id;
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
            var id = field.getAnnotation(Id.class);
            if (id != null) {
                this.id = id.name();
            }

            var annotation = field.getAnnotation(Entry.class);
            if (annotation == null) {
                continue;
            }
            if (!field.trySetAccessible()) {
                continue;
            }
            MongoEntry entry = new MongoEntry(this, field, annotation.name());
            this.entries.add(entry);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof MongoEntity other))
            return false;

        return Objects.equals(this.collectionName, other.collectionName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.collectionName);
    }

    @Override
    public String toString() {
        return "StoredEntity(name=" + this.collectionName + ")";
    }
}
