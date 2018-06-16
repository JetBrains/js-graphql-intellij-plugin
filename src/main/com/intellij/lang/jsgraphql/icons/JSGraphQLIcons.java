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
        public static final Icon GraphQL = JSGraphQLIcons.load("/icons/graphql.png");
        public static final Icon Relay = JSGraphQLIcons.load("/icons/relay.png");
        public static final Icon Apollo = JSGraphQLIcons.load("/icons/apollo.png");
        public static final Icon Lokka = JSGraphQLIcons.load("/icons/lokka.png");
    }

    public static class Files {
        public static final Icon GraphQL = JSGraphQLIcons.load("/icons/graphql-file.png");
        public static final Icon GraphQLSchema = JSGraphQLIcons.load("/icons/graphql-schema-file.png");
    }

    public static class UI {
        public static final Icon GraphQLNode = JSGraphQLIcons.load("/icons/graphql-node.png");
        public static final Icon GraphQLVariables = JSGraphQLIcons.load("/icons/variable.png");
    }

    public static class Schema {
        public static final Icon Field = JSGraphQLIcons.load("/icons/field.png");
        public static final Icon Scalar = JSGraphQLIcons.load("/icons/scalar.png");
        public static final Icon Enum = JSGraphQLIcons.load("/icons/enum.png");
        public static final Icon Type = JSGraphQLIcons.load("/icons/type.png");
        public static final Icon Interface = JSGraphQLIcons.load("/icons/interface.png");
        public static final Icon Query = JSGraphQLIcons.load("/icons/query.png");
        public static final Icon Attribute = JSGraphQLIcons.load("/icons/attribute.png");
        public static final Icon Subscription = JSGraphQLIcons.load("/icons/subscription.png");
        public static final Icon Mutation = JSGraphQLIcons.load("/icons/mutation.png");
        public static final Icon Fragment = JSGraphQLIcons.load("/icons/fragment.png");
    }

    private static Icon load(String path) {
        return IconLoader.getIcon(path, JSGraphQLIcons.class);
    }

}
