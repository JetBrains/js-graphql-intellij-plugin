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
package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.PublicSpi;

/**
 * A value unboxer takes values that are wrapped in classes like {@link java.util.Optional} / {@link java.util.OptionalInt} etc..
 * and returns value from them.  You can provide your own implementation if you have your own specific
 * holder classes.
 */
@PublicSpi
public interface ValueUnboxer {

    /**
     * The default value unboxer handles JDK classes such as {@link java.util.Optional} and {@link java.util.OptionalInt} etc..
     */
    ValueUnboxer DEFAULT = new DefaultValueUnboxer();

    /**
     * Unboxes 'object' if it is boxed in an {@link java.util.Optional } like
     * type that this unboxer can handle. Otherwise returns its input
     * unmodified
     *
     * @param object to unbox
     * @return unboxed object, or original if cannot unbox
     */
    Object unbox(final Object object);
}
