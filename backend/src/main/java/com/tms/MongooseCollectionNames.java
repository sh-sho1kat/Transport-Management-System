package com.tms;

import java.util.*;
import java.util.regex.Pattern;

/** Mongoose 8 collection naming compatibility. Rules adapted from Mongoose (MIT); see THIRD_PARTY_NOTICES.md. */
final class MongooseCollectionNames {
    private MongooseCollectionNames() {}
    private record Rule(Pattern pattern, String replacement) {}
    private static Rule rule(String regex, String replacement) {
        return new Rule(Pattern.compile(regex), replacement);
    }
    private static final List<Rule> RULES = List.of(
        rule("human$", "humans"),
        rule("(m)an$", "$1en"),
        rule("(pe)rson$", "$1ople"),
        rule("(child)$", "$1ren"),
        rule("^(ox)$", "$1en"),
        rule("(ax|test)is$", "$1es"),
        rule("(octop|vir)us$", "$1i"),
        rule("(alias|status)$", "$1es"),
        rule("(bu)s$", "$1ses"),
        rule("(buffal|tomat|potat)o$", "$1oes"),
        rule("([ti])um$", "$1a"),
        rule("sis$", "ses"),
        rule("(?:([^f])fe|([lr])f)$", "$1$2ves"),
        rule("(hive)$", "$1s"),
        rule("([^aeiouy]|qu)y$", "$1ies"),
        rule("(x|ch|ss|sh)$", "$1es"),
        rule("(matr|vert|ind)ix|ex$", "$1ices"),
        rule("([m|l])ouse$", "$1ice"),
        rule("(kn|w|l)ife$", "$1ives"),
        rule("(quiz)$", "$1zes"),
        rule("^goose$", "geese"),
        rule("s$", "s"),
        rule("([^a-z])$", "$1"),
        rule("$", "s")
    );
    private static final Set<String> UNCOUNTABLE = Set.of("advice", "energy", "excretion", "digestion", "cooperation", "health", "justice", "labour", "machinery", "equipment", "information", "pollution", "sewage", "paper", "money", "species", "series", "rain", "rice", "fish", "sheep", "moose", "deer", "news", "expertise", "status", "media");
    static String forTrip(String tripId) {
        String name = tripId.toLowerCase(Locale.ROOT);
        if (UNCOUNTABLE.contains(name)) return name;
        for (Rule rule : RULES) {
            var matcher = rule.pattern.matcher(name);
            if (matcher.find()) return matcher.replaceFirst(rule.replacement);
        }
        return name;
    }
}
