package com.intellij.lang.jsgraphql.types.language;


import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * Represents a language node that has a name
 */
@PublicApi
public interface NamedNode<T extends NamedNode> extends Node<T> {

    /**
     * @return the name of this node
     */
    String getName();
}
