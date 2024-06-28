package me.kubbidev.renapowered.common.dependencies;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.kubbidev.renapowered.common.dependencies.relocation.Relocation;
import me.kubbidev.renapowered.common.dependencies.relocation.RelocationHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * The dependencies used by RenaPowered.
 */
public enum Dependency {

    ASM(
            "org.ow2.asm",
            "asm",
            "9.1",
            "zaTeRV+rSP8Ly3xItGOUR9TehZp6/DCglKmG8JNr66I="
    ),
    ASM_COMMONS(
            "org.ow2.asm",
            "asm-commons",
            "9.1",
            "r8sm3B/BLAxKma2mcJCN2C4Y38SIyvXuklRplrRwwAw="
    ),
    JAR_RELOCATOR(
            "me.lucko",
            "jar-relocator",
            "1.7",
            "b30RhOF6kHiHl+O5suNLh/+eAr1iOFEFLXhwkHHDu4I="
    ),
    ADVENTURE(
            "me{}lucko",
            "adventure-api",
            "4.13.0",
            "gcmYlY1KDrzbnE9nB8rA44oBZKPSMvv9kNRefoEuud0=",
            Relocation.of("adventure", "net{}kyori{}adventure")
    ),
    CAFFEINE(
            "com{}github{}ben-manes{}caffeine",
            "caffeine",
            "2.9.0",
            "VFMotEO3XLbTHfRKfL3m36GlN72E/dzRFH9B5BJiX2o=",
            Relocation.of("caffeine", "com{}github{}benmanes{}caffeine")
    ),
    OKIO(
            "com{}squareup{}" + RelocationHelper.OKIO_STRING,
            RelocationHelper.OKIO_STRING,
            "1.17.5",
            "Gaf/SNhtPPRJf38lD78pX0MME6Uo3Vt7ID+CGAK4hq0=",
            Relocation.of(RelocationHelper.OKIO_STRING, RelocationHelper.OKIO_STRING)
    ),
    OKHTTP(
            "com{}squareup{}" + RelocationHelper.OKHTTP3_STRING,
            "okhttp",
            "3.14.9",
            "JXD6tVUVy/iB16TO70n8UVSQvAJwV+Zmd2ooMkZa7KA=",
            Relocation.of(RelocationHelper.OKHTTP3_STRING, RelocationHelper.OKHTTP3_STRING),
            Relocation.of(RelocationHelper.OKIO_STRING, RelocationHelper.OKIO_STRING)
    ),
    SLF4J_SIMPLE(
            "org.slf4j",
            "slf4j-simple",
            "1.7.30",
            "i5J5y/9rn4hZTvrjzwIDm2mVAw7sAj7UOSh0jEFnD+4="
    ),
    SLF4J_API(
            "org.slf4j",
            "slf4j-api",
            "1.7.30",
            "zboHlk0btAoHYUhcax6ML4/Z6x0ZxTkorA1/lRAQXFc="
    ),
    MONGODB_DRIVER_CORE(
            "org.mongodb",
            "mongodb-driver-core",
            "4.5.0",
            "awqoW0ImUcrCTA2d1rDCjDLEjLMCrOjKWIcC7E+zLGA=",
            Relocation.of("mongodb", "com{}mongodb"),
            Relocation.of("bson", "org{}bson")
    ),
    MONGODB_DRIVER_LEGACY(
            "org.mongodb",
            "mongodb-driver-legacy",
            "4.5.0",
            "77KZGIr3KZmzBpN69rGOLXmnlJIBCXRl/U4gEIdlFhY=",
            Relocation.of("mongodb", "com{}mongodb"),
            Relocation.of("bson", "org{}bson")
    ),
    MONGODB_DRIVER_SYNC(
            "org.mongodb",
            "mongodb-driver-sync",
            "4.5.0",
            "q9XDSGJjlo/Ek6jHoCbqWnaK/dghB8y9aDM0hCLiSvk=",
            Relocation.of("mongodb", "com{}mongodb"),
            Relocation.of("bson", "org{}bson")
    ),
    MONGODB_DRIVER_BSON(
            "org.mongodb",
            "bson",
            "4.5.0",
            "6CFyEzxbdeiBEXdDBmcgqWs5dvicgFkBLU3MlQUIqRA=",
            Relocation.of("mongodb", "com{}mongodb"),
            Relocation.of("bson", "org{}bson")
    ),
    CONFIGURATE_CORE(
            "org{}spongepowered",
            "configurate-core",
            "3.7.2",
            "XF2LzWLkSV0wyQRDt33I+gDlf3t2WzxH1h8JCZZgPp4=",
            Relocation.of("configurate", "ninja{}leaping{}configurate")
    ),
    CONFIGURATE_GSON(
            "org{}spongepowered",
            "configurate-gson",
            "3.7.2",
            "9S/mp3Ig9De7NNd6+2kX+L4R90bHnAosSNVbFjrl7sM=",
            Relocation.of("configurate", "ninja{}leaping{}configurate")
    ),
    CONFIGURATE_YAML(
            "org{}spongepowered",
            "configurate-yaml",
            "3.7.2",
            "OBfYn4nSMGZfVf2DoZhZq+G9TF1mODX/C5OOz/mkPmc=",
            Relocation.of("configurate", "ninja{}leaping{}configurate")
    ),
    SNAKEYAML(
            "org.yaml",
            "snakeyaml",
            "1.28",
            "NURqFCFDXUXkxqwN47U3hSfVzCRGwHGD4kRHcwzh//o=",
            Relocation.of("yaml", "org{}yaml{}snakeyaml")
    ),
    CONFIGURATE_HOCON(
            "org{}spongepowered",
            "configurate-hocon",
            "3.7.2",
            "GOORZlK1FKLzdIm7dKyyXtBdvk7Z89HARAd2H6NiWSY=",
            Relocation.of("configurate", "ninja{}leaping{}configurate"),
            Relocation.of("hocon", "com{}typesafe{}config")
    ),
    HOCON_CONFIG(
            "com{}typesafe",
            "config",
            "1.4.1",
            "TAqn4iPHXIhAxB/Bg9TNMRgUCh7lA+PgjOZu0nlMlI8=",
            Relocation.of("hocon", "com{}typesafe{}config")
    ),
    COLLECTIONS4(
            "org.apache.commons",
            "commons-collections4",
            "4.4",
            "Hfi5QwtcjtFD14FeQD4z71NxskAKrb6b2giDdi4IRtE=",
            Relocation.of("collections4", "org{}apache{}commons{}collections4")
    ),
    NEOVISIONARIES(
            "com{}neovisionaries",
            "nv-websocket-client",
            "2.14",
            "7tD7b1712xfQhwOfHoKc/oJzY7KGMmUlipbw7TIzE7c=",
            Relocation.of("neovisionaries", "com{}neovisionaries{}ws{}client")
    ),
    TROVE4J(
            "net.sf.trove4j",
            "trove4j",
            "3.0.3",
            "PIYWID1hoSp+NIfos088GYwrW6npDaDH6jLZnNSVgBI=",
            Relocation.of("trove4j", "gnu{}trove")
    ),
    JDA(
            "net{}dv8tion",
            "JDA",
            "5.0.0-beta.24",
            "Az4P6A3Iyos6psCRJFDK3igTNAmw1pXo8E/uhOcPazQ=",
            Relocation.of(RelocationHelper.OKHTTP3_STRING, RelocationHelper.OKHTTP3_STRING),
            Relocation.of(RelocationHelper.OKIO_STRING, RelocationHelper.OKIO_STRING),
            Relocation.of("collections4", "org{}apache{}commons{}collections4"),
            Relocation.of("neovisionaries", "com{}neovisionaries{}ws{}client"),
            Relocation.of("trove4j", "gnu{}trove"),
            Relocation.of("jda", "net{}dv8tion{}jda")
    );

    private final String mavenRepoPath;
    private final String version;

    @Getter
    private final byte[] checksum;

    @Getter
    private final List<Relocation> relocations;

    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

    Dependency(String groupId, String artifactId, String version, String checksum) {
        this(groupId, artifactId, version, checksum, new Relocation[0]);
    }

    Dependency(String groupId, String artifactId, String version, String checksum, Relocation... relocations) {
        this.mavenRepoPath = String.format(MAVEN_FORMAT,
                rewriteEscaping(groupId).replace(".", "/"),
                rewriteEscaping(artifactId),
                version,
                rewriteEscaping(artifactId),
                version
        );
        this.version = version;
        this.checksum = Base64.getDecoder().decode(checksum);
        this.relocations = ImmutableList.copyOf(relocations);
    }

    private static String rewriteEscaping(String s) {
        return s.replace("{}", ".");
    }

    public String getFileName(String classifier) {
        String name = name().toLowerCase(Locale.ROOT).replace('_', '-');
        String extra = classifier == null || classifier.isEmpty()
                ? ""
                : "-" + classifier;

        return name + "-" + this.version + extra + ".jar";
    }

    String getMavenRepoPath() {
        return this.mavenRepoPath;
    }

    public boolean checksumMatches(byte[] hash) {
        return Arrays.equals(this.checksum, hash);
    }

    /**
     * Creates a {@link MessageDigest} suitable for computing the checksums
     * of dependencies.
     *
     * @return the digest
     */
    public static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}