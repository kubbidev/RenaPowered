package me.kubbidev.renapowered.common.storage.implementation.mongodb;

import java.lang.reflect.Field;
import java.util.Objects;

public record MongoEntry(MongoEntity entity, Field field, String name) {

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof MongoEntry other))
            return false;

        return Objects.equals(this.entity, other.entity)
                && Objects.equals(this.name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entity, this.name);
    }

    @Override
    public String toString() {
        return "StoredColumn(name=" + this.name + ")";
    }
}
