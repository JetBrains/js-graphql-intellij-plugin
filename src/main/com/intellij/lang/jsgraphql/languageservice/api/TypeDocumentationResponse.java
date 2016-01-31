/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

import com.google.common.collect.Lists;

import java.util.List;

public class TypeDocumentationResponse {

    public String type;
    public String description;
    public List<String> interfaces = Lists.newArrayListWithExpectedSize(0);
    public List<String> implementations = Lists.newArrayListWithExpectedSize(0);
    public List<Field> fields = Lists.newArrayList();

    @Override
    public String toString() {
        return "TypeDocumentationResponse{" +
                "type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", interfaces=" + interfaces +
                ", implementations=" + implementations +
                ", fields=" + fields +
                '}';
    }

    public static class Field {
        public String name;
        public List<FieldArgument> args = Lists.newArrayListWithExpectedSize(0);
        public String type;
        public String description;

        @Override
        public String toString() {
            return "Field{" +
                    "name='" + name + '\'' +
                    ", args=" + args +
                    ", type='" + type + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    public static class FieldArgument {
        public String name;
        public String type;
        public String description;

        @Override
        public String toString() {
            return "FieldArgument{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

}
