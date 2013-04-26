package com.gettingmobile.text;

import static java.lang.Character.UnicodeBlock.*;

public final class Teaser {
    private static boolean isHierographic(char c) {
        final Character.UnicodeBlock block = java.lang.Character.UnicodeBlock.of(c);
        return CJK_UNIFIED_IDEOGRAPHS.equals(block) ||
                CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block) ||
                CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B.equals(block) ||
                HIRAGANA.equals(block) ||
                KATAKANA.equals(block);
    }

    private static boolean isWordCharacter(char c) {
        return Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == '_';
    }

    private static boolean isWordEnd(CharSequence text, int index) {
        final char currentChar = text.charAt(index);
        final char nextChar = (index + 1 < text.length()) ? text.charAt(index + 1) : ' ';

        return isWordCharacter(currentChar) &&
                (!isWordCharacter(nextChar) || isHierographic(nextChar) || isHierographic(currentChar));
    }

    private static int skipNextWord(String text, int startIndex) {
        final int length = text.length();
        int i = startIndex;
        while (i < length && !isWordEnd(text, i))
            ++i;
        return i < length ? i + 1 : length;
    }

    public static String tease(String text, int startChar, int maxWords) {
        int index = startChar;
        for (int words = 0; index < text.length() && words < maxWords; ++words)
            index = skipNextWord(text, index);
        return text.substring(startChar, index);
    }

    public static String tease(String text, int startChar, int maxWords, String ellipsis) {
        final String teaser = tease(text, startChar, maxWords);
        return (teaser.length() + startChar) < text.length() ? teaser + ellipsis : teaser;
    }
}
