/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.schema.GraphQLOutputType;

import java.util.List;

@PublicApi
public class FieldWiringEnvironment extends WiringEnvironment {

    private final FieldDefinition fieldDefinition;
    private final TypeDefinition parentType;
    private final GraphQLOutputType fieldType;
    private final List<GraphQLDirective> directives;

    FieldWiringEnvironment(TypeDefinitionRegistry registry, TypeDefinition parentType, FieldDefinition fieldDefinition, GraphQLOutputType fieldType, List<GraphQLDirective> directives) {
        super(registry);
        this.fieldDefinition = fieldDefinition;
        this.parentType = parentType;
        this.fieldType = fieldType;
        this.directives = directives;
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public TypeDefinition getParentType() {
        return parentType;
    }

    public GraphQLOutputType getFieldType() {
        return fieldType;
    }

    public List<GraphQLDirective> getDirectives() {
        return directives;
    }
}
