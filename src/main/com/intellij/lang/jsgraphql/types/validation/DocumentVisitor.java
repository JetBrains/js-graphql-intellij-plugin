package com.intellij.lang.jsgraphql.types.validation;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Node;

import java.util.List;

@Internal
public interface DocumentVisitor {

    void enter(Node node, List<Node> path);

    void leave(Node node, List<Node> path);
}
