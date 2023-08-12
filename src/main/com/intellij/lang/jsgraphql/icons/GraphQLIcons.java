/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.icons;

import com.intellij.icons.AllIcons;
import com.intellij.lang.jsgraphql.GraphqlIcons;
import com.intellij.ui.LayeredIcon;

import javax.swing.*;

public final class GraphQLIcons {
  public static final Icon FILE = GraphqlIcons.Graphql;

  public static final class Logos {
    public static final Icon GraphQL = FILE;
    public static final Icon Relay = GraphqlIcons.Relay;
    public static final Icon Apollo = GraphqlIcons.Apollo;
  }

  public static final class Files {
    public static final Icon GraphQL = FILE;
    public static final Icon GraphQLConfig = GraphqlIcons.GraphqlConfig;
    public static final Icon GraphQLSchema = GraphqlIcons.GraphqlSchema;
    public static final Icon GraphQLScratch = LayeredIcon.layeredIcon(() -> new Icon[]{GraphQL, AllIcons.Actions.Scratch});
  }

  public static final class UI {
    public static final Icon GraphQLToolWindow = GraphqlIcons.GraphqlToolWindow;
    public static final Icon GraphQLVariables = GraphqlIcons.Variable;
    public static final Icon GraphQLNode = GraphqlIcons.GraphqlNode;
  }

  public static final class Schema {
    public static final Icon Field = GraphqlIcons.Field;
    public static final Icon Scalar = GraphqlIcons.Scalar;
    public static final Icon Enum = GraphqlIcons.Enum;
    public static final Icon Type = GraphqlIcons.Type;
    public static final Icon Interface = GraphqlIcons.Interface;
    public static final Icon Query = GraphqlIcons.Query;
    public static final Icon Attribute = GraphqlIcons.Attribute;
    public static final Icon Subscription = GraphqlIcons.Subscription;
    public static final Icon Mutation = GraphqlIcons.Mutation;
    public static final Icon Fragment = GraphqlIcons.Fragment;
  }
}
