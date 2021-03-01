package com.intellij.lang.jsgraphql.types.scalar;

import com.intellij.lang.jsgraphql.types.Internal;

@Internal
class CoercingUtil {
    static boolean isNumberIsh(Object input) {
        return input instanceof Number || input instanceof String;
    }

    static String typeName(Object input) {
        if (input == null) {
            return "null";
        }

        return input.getClass().getSimpleName();
    }
}
