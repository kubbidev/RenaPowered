package me.kubbidev.renapowered.common.storage.implementation.mongodb;

import java.lang.reflect.Field;
import java.util.Objects;

public record MongoColumn(MongoEntity entity, Field field, String name) {

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof MongoColumn other))
            return false;

        return this.entity.equals(other.entity) &&
                this.name.equals(other.name);
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
