package me.kubbidev.renapowered.common.command.util;

import java.util.List;

/**
 * Tokenizes command input into distinct "argument" tokens.
 *
 * <p>Splits on whitespace, except when surrounded by quotes.</p>
 */
public enum ArgumentTokenizer {

    EXECUTE {
        @Override
        public List<String> tokenizeInput(String args) {
            return new QuotedStringTokenizer(args).tokenize(true);
        }
    },
    TAB_COMPLETE {
        @Override
        public List<String> tokenizeInput(String args) {
            return new QuotedStringTokenizer(args).tokenize(false);
        }
    };

    public abstract List<String> tokenizeInput(String args);

}