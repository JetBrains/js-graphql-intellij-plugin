/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.google.common.collect.Maps;
import com.intellij.lang.jsgraphql.JSGraphQLLanguage;
import com.intellij.lang.jsgraphql.languageservice.api.Token;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class JSGraphQLElementType extends IElementType {

    public final static String SELECTION_SET_KIND = "SelectionSet";
    public final static String DOCUMENT_KIND = "Document";
    public final static String PROPERTY_KIND = "Property";
    public final static String ATOM_KIND = "Atom";
    public final static String TEMPLATE_FRAGMENT_KIND = "TemplateFragment";

    private final static Map<String, JSGraphQLElementType> knownElementTypes = Maps.newConcurrentMap();

    private String kind;

    private JSGraphQLElementType(@NotNull @NonNls String kindOrType) {
        super(kindOrType, JSGraphQLLanguage.INSTANCE);
        this.kind = kindOrType;
    }


    public static JSGraphQLElementType create(@NotNull @NonNls Token token) {
        final String kindOrType = Optional.ofNullable(token.getKind()).orElse(token.getType());
        return create(kindOrType);
    }

    public static JSGraphQLElementType create(String kind) {
        return knownElementTypes.computeIfAbsent(kind, s -> new JSGraphQLElementType(kind));
    }

    public String getKind() {
        return kind;
    }
}
