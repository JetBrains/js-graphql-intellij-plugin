package com.intellij.lang.jsgraphql.types.language;


import com.intellij.lang.jsgraphql.types.PublicApi;

@PublicApi
public interface SelectionSetContainer<T extends Node> extends Node<T> {
    SelectionSet getSelectionSet();
}
