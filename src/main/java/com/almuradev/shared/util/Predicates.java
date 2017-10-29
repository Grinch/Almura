/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.util;

import com.google.common.base.Predicate;

public class Predicates {
    public static final class IntegerFilterPredicate implements Predicate<String> {

        @Override
        public boolean apply(String input) {
            final int rawIndex;
            try {
                rawIndex = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                return false;
            }
            return rawIndex >= 0 && rawIndex < Integer.MAX_VALUE;
        }
    }

    public static final class StringLengthPredicate implements Predicate<String> {

        private final int min, max;

        public StringLengthPredicate(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean apply(String input) {
            return input.length() >= min && input.length() <= max;
        }
    }
}