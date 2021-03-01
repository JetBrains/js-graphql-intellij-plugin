package com.intellij.lang.jsgraphql.types.language;

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.List;
import java.util.Map;

@PublicApi
public interface NodeBuilder {

    NodeBuilder sourceLocation(SourceLocation sourceLocation);

    NodeBuilder comments(List<Comment> comments);

    NodeBuilder ignoredChars(IgnoredChars ignoredChars);

    NodeBuilder additionalData(Map<String, String> additionalData);

    NodeBuilder additionalData(String key, String value);

}
