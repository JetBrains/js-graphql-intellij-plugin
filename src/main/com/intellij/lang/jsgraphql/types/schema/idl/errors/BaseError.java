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

import com.intellij.lang.jsgraphql.types.*;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("rawtypes")
@Internal
public class BaseError extends GraphQLException implements GraphQLError {
    protected static final SourceLocation EMPTY = new SourceLocation(-1, -1);

    private final Node node;
    private final List<Node> myReferences = new ArrayList<>();

    public BaseError(@Nullable Node node, @Nullable String msg) {
        super(StringUtil.trimEnd(StringUtil.notNullize(msg), "."));
        this.node = node;
    }

    public static String lineCol(Node node) {
        SourceLocation sourceLocation = node.getSourceLocation() == null ? EMPTY : node.getSourceLocation();
        return String.format("[@%d:%d]", sourceLocation.getLine(), sourceLocation.getColumn());
    }

    @Override
    public List<SourceLocation> getLocations() {
        return node == null || node.getSourceLocation() == null ?
            Collections.singletonList(EMPTY) :
            Collections.singletonList(node.getSourceLocation());
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.ValidationError;
    }

    @Override
    public String toString() {
        return getMessage();
    }


    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return GraphqlErrorHelper.equals(this, o);
    }

    @Override
    public int hashCode() {
        return GraphqlErrorHelper.hashCode(this);
    }

    @Override
    public @Nullable Node getNode() {
        return node;
    }

    protected void addReferences(Node @Nullable ... references) {
        if (references != null) {
            ContainerUtil.addAllNotNull(myReferences, references);
        }
    }

    public @NotNull List<Node> getReferences() {
        return myReferences;
    }
}
