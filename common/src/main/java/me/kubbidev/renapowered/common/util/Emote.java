package me.kubbidev.renapowered.common.util;

import lombok.Getter;

public enum Emote {
    // animated emotes,

    // static emotes,
    RED_TRIANGLE(  "red_triangle",   "1256401129470820503", false),
    GREEN_TRIANGLE("green_triangle", "1256401158935674991", false),
    EQUAL(         "equal",          "1256401174173843518", false),
    EMPTY(         "empty",          "1256401190401474600", false),

    // online status
    ONLINE(        "online",         "1256401217370853386", false),
    IDLE(          "idle",           "1256401228854857758", false),
    DO_NOT_DISTURB("dnb",            "1256401247599071263", false),
    INVISIBLE(     "invisible",      "1256401272089739264", false),
    OFFLINE(       "invisible",      "1256401272089739264", false),

    // default emotes
    WHITE_CHECK_MARK(   "white_check_mark"),
    WARNING(            "warning"),
    ACCESS_DENIED(      "no_entry_sign"),
    RED_CROSS(          "x"),
    LOCK(               "lock"),
    GREY_EXCLAMATION(   "grey_exclamation"),
    RED_EXCLAMATION(    "exclamation"),
    QUESTION(           "question"),
    RED_FLAG(           "triangular_flag_on_post"),
    INFO(               "information_source"),
    MAGNIFYING_GLASS(   "mag"),
    STOPWATCH(          "stopwatch"),
    GEAR(               "gear"),
    HOURGLASS(          "hourglass_flowing_sand"),
    PURSE(              "purse"),
    MONEY_BAG(          "moneybag"),
    BANK(               "bank"),
    DICE(               "game_die"),
    TICKET(             "tickets"),
    SPADES(             "spades"),
    CLUBS(              "clubs"),
    HEARTS(             "hearts"),
    DIAMONDS(           "diamonds"),
    THERMOMETER(        "thermometer"),
    CLOUD(              "cloud"),
    WIND(               "wind_blowing_face"),
    RAIN(               "cloud_rain"),
    DROPLET(            "droplet"),
    MUSICAL_NOTE(       "musical_note"),
    TRACK_NEXT(         "track_next"),
    PLAY(               "arrow_forward"),
    PAUSE(              "pause_button"),
    REPEAT(             "repeat"),
    SOUND(              "sound"),
    MUTE(               "mute"),
    STOP_BUTTON(        "stop_button"),
    THUMBSDOWN(         "thumbsdown"),
    SPEECH(             "speech_balloon"),
    CLAP(               "clap"),
    TRIANGULAR_RULER(   "triangular_ruler"),
    SCISSORS(           "scissors"),
    GEM(                "gem"),
    LEAF(               "leaves"),
    BALLOT_BOX(         "ballot_box"),
    BUG(                "bug"),
    ROCKET(             "rocket"),
    BROKEN_HEART(       "broken_heart"),
    ID(                 "id"),
    CROWN(              "crown"),
    MAP(                "map"),
    BIRTHDAY(           "birthday"),
    BUSTS_IN_SILHOUETTE("busts_in_silhouette"),
    MICROPHONE(         "microphone2"),
    KEYBOARD(           "keyboard"),
    SPEECH_BALLOON(     "speech_balloon"),
    DATE(               "date"),
    BUST_IN_SILHOUETTE( "bust_in_silhouette"),
    MILITARY_MEDAL(     "military_medal"),
    ROBOT(              "robot"),
    SATELLITE(          "satellite"),
    SCREWDRIVER(        "screwdriver"),
    APPLE(              "apple"),
    CHERRIES(           "cherries"),
    BELL(               "bell"),
    GIFT(               "gift"),
    LOUD_SOUND(         "loud_sound");

    @Getter
    private final String id;
    private final String emoteName;

    @Getter
    private final boolean custom;
    private final boolean animated;

    // default discord emotes constructor
    Emote(String id) {
        this.emoteName = id;
        this.id = id;
        this.animated = false;
        this.custom = false;
    }

    Emote(String emoteName, String id, boolean animated) {
        this.emoteName = emoteName;
        this.id = id;
        this.animated = animated;
        this.custom = true;
    }

    /**
     * Gets the formatted string of the emote.
     * <br>Note: the emote needs to be a custom provided by a guild in order to display correctly.
     *
     * @return the formatted emote
     * @see Emote#getEmoji()
     */
    public String getEmote() {
        return String.format(
                "<%s:%s:%s>",
                this.animated ? "a" :"",
                this.emoteName,
                this.id
        );
    }

    /**
     * Gets the discord default emoji format of this emote.
     * <br>Note: the emote needs to be provided by default by discord in order to display correctly.
     *
     * @return the formatted emoji
     * @see Emote#getEmote()
     */
    public String getEmoji() {
        return String.format(
                ":%s:",
                this.id
        );
    }

    public StringBuilder append(String content) {
        StringBuilder builder = new StringBuilder(toString());
        builder.append(' ');
        builder.append(content);

        return builder;
    }

    @Override
    public String toString() {
        return isCustom() ? getEmote() : getEmoji();
    }
}
