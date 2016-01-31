/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class JSGraphQLIcons {

    public static class Logos {
        public static final Icon GraphQL = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/graphql.png");
        public static final Icon Relay = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/relay.png");
    }

    public static class Files {
        public static final Icon GraphQL = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/graphql-file.png");
        public static final Icon GraphQLSchema = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/graphql-schema-file.png");
    }

    public static class UI {
        public static final Icon GraphQLNode = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/graphql-node.png");
        public static final Icon GraphQLVariables = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/variable.png");
    }

    public static class Schema {
        public static final Icon Field = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/field.png");
        public static final Icon Scalar = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/scalar.png");
        public static final Icon Enum = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/enum.png");
        public static final Icon Type = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/type.png");
        public static final Icon Interface = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/interface.png");
        public static final Icon Query = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/query.png");
        public static final Icon Attribute = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/attribute.png");
        public static final Icon Subscription = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/subscription.png");
        public static final Icon Mutation = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/mutation.png");
        public static final Icon Fragment = JSGraphQLIcons.load("/com/intellij/lang/jsgraphql/icons/fragment.png");
    }

    private static Icon load(String path) {
        return IconLoader.getIcon(path, JSGraphQLIcons.class);
    }

}
