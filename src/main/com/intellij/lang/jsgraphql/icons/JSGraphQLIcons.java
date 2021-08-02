/*
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
        public static final Icon GraphQL = JSGraphQLIcons.load("/icons/graphql.svg");
        public static final Icon Relay = JSGraphQLIcons.load("/icons/relay.svg");
        public static final Icon Apollo = JSGraphQLIcons.load("/icons/apollo.svg");
        public static final Icon Lokka = JSGraphQLIcons.load("/icons/lokka.svg");
    }

    public static class Files {
        public static final Icon GraphQL = JSGraphQLIcons.load("/icons/graphqlFile.svg");
        public static final Icon GraphQLSchema = JSGraphQLIcons.load("/icons/graphqlSchema.svg");
    }

    public static class UI {
        public static final Icon GraphQLToolwindow = JSGraphQLIcons.load("/icons/graphqlToolWindow.svg");
        public static final Icon GraphQLVariables = JSGraphQLIcons.load("/icons/variable.svg");
        public static final Icon GraphQLNode = JSGraphQLIcons.load("/icons/graphqlNode.svg");
    }

    public static class Schema {
        public static final Icon Field = JSGraphQLIcons.load("/icons/field.svg");
        public static final Icon Scalar = JSGraphQLIcons.load("/icons/scalar.svg");
        public static final Icon Enum = JSGraphQLIcons.load("/icons/enum.svg");
        public static final Icon Type = JSGraphQLIcons.load("/icons/type.svg");
        public static final Icon Interface = JSGraphQLIcons.load("/icons/interface.svg");
        public static final Icon Query = JSGraphQLIcons.load("/icons/query.svg");
        public static final Icon Attribute = JSGraphQLIcons.load("/icons/attribute.svg");
        public static final Icon Subscription = JSGraphQLIcons.load("/icons/subscription.svg");
        public static final Icon Mutation = JSGraphQLIcons.load("/icons/mutation.svg");
        public static final Icon Fragment = JSGraphQLIcons.load("/icons/fragment.svg");
    }

    private static Icon load(String path) {
        return IconLoader.getIcon(path, JSGraphQLIcons.class);
    }

}
