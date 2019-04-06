/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.scopes;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.DelegatingGlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Conditional version of delegating global search scope.
 * Allows GraphQL settings to control whether a scope contains anything.
 */
public class ConditionalGlobalSearchScope extends DelegatingGlobalSearchScope {

    private final Supplier<Boolean> isEnabled;

    public ConditionalGlobalSearchScope(@NotNull GlobalSearchScope baseScope, Supplier<Boolean> isEnabled) {
        super(baseScope);
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
        if(!isEnabled.get()) {
            return false;
        }
        return super.contains(file);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConditionalGlobalSearchScope that = (ConditionalGlobalSearchScope) o;
        return Objects.equals(isEnabled, that.isEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isEnabled);
    }
}
