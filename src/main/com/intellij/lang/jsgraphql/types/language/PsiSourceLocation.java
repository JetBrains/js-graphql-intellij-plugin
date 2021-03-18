package com.intellij.lang.jsgraphql.types.language;

import com.google.common.collect.Lists;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PsiSourceLocation extends SourceLocation {
    private final PsiElement myElement;

    public PsiSourceLocation(@NotNull PsiElement element) {
        super(1, 1);
        myElement = element;
    }

    private @NotNull SourceLocation getSourceLocation() {
        return CachedValuesManager.getCachedValue(myElement, () -> {
            SourceLocation defaultLocation = new SourceLocation(1, 1, "Unknown");
            PsiFile containingFile = myElement.getContainingFile();
            InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(myElement.getProject());
            PsiFile topLevelFile = injectedLanguageManager.getTopLevelFile(myElement);
            if (topLevelFile == null) return CachedValueProvider.Result.create(defaultLocation, ModificationTracker.EVER_CHANGED);

            VirtualFile virtualFile = topLevelFile.getVirtualFile();
            Collection<Object> dependencies = Lists.newArrayList(containingFile, topLevelFile);
            if (virtualFile != null) {
                dependencies.add(virtualFile);
            }

            com.intellij.openapi.editor.Document document = PsiDocumentManager.getInstance(myElement.getProject()).getDocument(topLevelFile);
            if (document == null) return CachedValueProvider.Result.create(defaultLocation, dependencies);

            int offset = injectedLanguageManager.injectedToHost(myElement, myElement.getTextOffset());
            int lineNumber = document.getLineNumber(offset);
            int column = offset - document.getLineStartOffset(lineNumber);

            SourceLocation location = new SourceLocation(lineNumber + 1, column + 1, virtualFile != null ? virtualFile.getPath() : null);
            return CachedValueProvider.Result.create(location, dependencies);
        });
    }

    @Override
    public int getLine() {
        return getSourceLocation().getLine();
    }

    @Override
    public int getColumn() {
        return getSourceLocation().getColumn();
    }

    @Override
    public String getSourceName() {
        return getSourceLocation().getSourceName();
    }
}
