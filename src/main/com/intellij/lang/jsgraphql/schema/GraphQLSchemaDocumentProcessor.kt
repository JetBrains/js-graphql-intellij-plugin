package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.schema.builder.GraphQLCompositeRegistry;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.psi.PsiFile;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQLSchemaDocumentProcessor implements Processor<PsiFile> {
    private final GraphQLCompositeRegistry myCompositeRegistry = new GraphQLCompositeRegistry();
    private boolean isProcessedGraphQL;

    public @NotNull GraphQLCompositeRegistry getCompositeRegistry() {
        return myCompositeRegistry;
    }

    public boolean isProcessed() {
        return isProcessedGraphQL;
    }

    @Override
    public boolean process(@Nullable PsiFile psiFile) {
        if (!(psiFile instanceof GraphQLFile)) {
            return true;
        }

        isProcessedGraphQL = true;
        Document document = ((GraphQLFile) psiFile).getDocument();
        myCompositeRegistry.addFromDocument(document);
        return true;
    }
}
