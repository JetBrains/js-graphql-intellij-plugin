package com.intellij.lang.jsgraphql.types.language;

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.List;

@PublicApi
public interface NodeDirectivesBuilder extends NodeBuilder {

    NodeDirectivesBuilder directives(List<Directive> directives);

    NodeDirectivesBuilder directive(Directive directive);


}
