package com.intellij.lang.jsgraphql.ide.project.indexing;

import com.google.common.collect.ImmutableList;
import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.openapi.fileTypes.FileType;

import java.util.Collection;

public final class GraphQLIndexUtil {
    public static final int INDEX_BASE_VERSION = 1;

    public static final Collection<FileType> FILE_TYPES_WITH_IGNORED_SIZE_LIMIT =
        ImmutableList.of(GraphQLFileType.INSTANCE, JsonFileType.INSTANCE);
}
