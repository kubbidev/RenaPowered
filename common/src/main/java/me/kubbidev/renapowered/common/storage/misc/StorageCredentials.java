package me.kubbidev.renapowered.common.storage.misc;

import java.util.Objects;

public record StorageCredentials(String address, String database, String username, String password) {
    @Override
    public String address() {
        return Objects.requireNonNull(this.address, "address");
    }

    @Override
    public String database() {
        return Objects.requireNonNull(this.database, "database");
    }

    @Override
    public String username() {
        return Objects.requireNonNull(this.username, "username");
    }

    @Override
    public String password() {
        return Objects.requireNonNull(this.password, "password");
    }
}