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

public class GraphQLIcons {

    public static final Icon FILE = GraphQLIcons.load("/icons/graphqlFile.svg");

    public static class Logos {
        public static final Icon GraphQL = GraphQLIcons.load("/icons/graphql.svg");
        public static final Icon Relay = GraphQLIcons.load("/icons/relay.svg");
        public static final Icon Apollo = GraphQLIcons.load("/icons/apollo.svg");
        public static final Icon Lokka = GraphQLIcons.load("/icons/lokka.svg");
    }

    public static class Files {
        public static final Icon GraphQL = GraphQLIcons.load("/icons/graphqlFile.svg");
        public static final Icon GraphQLSchema = GraphQLIcons.load("/icons/graphqlSchema.svg");
    }

    public static class UI {
        public static final Icon GraphQLToolWindow = GraphQLIcons.load("/icons/graphqlToolWindow.svg");
        public static final Icon GraphQLVariables = GraphQLIcons.load("/icons/variable.svg");
        public static final Icon GraphQLNode = GraphQLIcons.load("/icons/graphqlNode.svg");
    }

    public static class Schema {
        public static final Icon Field = GraphQLIcons.load("/icons/field.svg");
        public static final Icon Scalar = GraphQLIcons.load("/icons/scalar.svg");
        public static final Icon Enum = GraphQLIcons.load("/icons/enum.svg");
        public static final Icon Type = GraphQLIcons.load("/icons/type.svg");
        public static final Icon Interface = GraphQLIcons.load("/icons/interface.svg");
        public static final Icon Query = GraphQLIcons.load("/icons/query.svg");
        public static final Icon Attribute = GraphQLIcons.load("/icons/attribute.svg");
        public static final Icon Subscription = GraphQLIcons.load("/icons/subscription.svg");
        public static final Icon Mutation = GraphQLIcons.load("/icons/mutation.svg");
        public static final Icon Fragment = GraphQLIcons.load("/icons/fragment.svg");
    }

    private static Icon load(String path) {
        return IconLoader.getIcon(path, GraphQLIcons.class);
    }

}
