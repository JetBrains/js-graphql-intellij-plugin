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
package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInterfaceImplementedInspection;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.ImplementingTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

@Internal
public class InterfaceFieldArgumentNotOptionalError extends BaseError {
    public InterfaceFieldArgumentNotOptionalError(String typeOfType,
                                                  ImplementingTypeDefinition typeDefinition,
                                                  InterfaceTypeDefinition interfaceTypeDef,
                                                  FieldDefinition objectFieldDef,
                                                  String objectArgStr,
                                                  FieldDefinition interfaceFieldDef) {
        super(typeDefinition, format("The %s type '%s' field '%s' defines an additional non-optional argument '%s' which is not allowed because field is also defined in interface '%s'.",
            typeOfType, typeDefinition.getName(), objectFieldDef.getName(), objectArgStr, interfaceTypeDef.getName()));
        addReferences(interfaceFieldDef);
    }

    @Override
    public @Nullable Class<? extends GraphQLInspection> getInspectionClass() {
        return GraphQLInterfaceImplementedInspection.class;
    }
}