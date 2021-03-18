package com.intellij.lang.jsgraphql.types.language;


import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

@PublicApi
public final class SourceLocation implements Serializable {

    private final int line;
    private final int column;
    private final String sourceName;
    private final @Nullable PsiElement myElement;
    private final @Nullable CachedValue<Location> myPsiBasedLocation;

    public SourceLocation(int line, int column) {
        this(line, column, null);
    }

    public SourceLocation(int line, int column, @Nullable String sourceName) {
        this(line, column, sourceName, null);
    }

    public SourceLocation(@NotNull PsiElement element) {
        this(-1, -1, null, element);
    }

    private SourceLocation(int line, int column, String sourceName, @Nullable PsiElement element) {
        this.line = line;
        this.column = column;
        this.sourceName = sourceName;

        myElement = element;
        myPsiBasedLocation = myElement != null ?
            CachedValuesManager.getManager(myElement.getProject()).createCachedValue(this::computeLocation) : null;
    }

    public int getLine() {
        return isPsiBased() ? Objects.requireNonNull(myPsiBasedLocation).getValue().line : line;
    }

    public int getColumn() {
        return isPsiBased() ? Objects.requireNonNull(myPsiBasedLocation).getValue().column : column;
    }

    public String getSourceName() {
        return isPsiBased() ? Objects.requireNonNull(myPsiBasedLocation).getValue().sourceName : sourceName;
    }

    public boolean isPsiBased() {
        return myElement != null;
    }

    public @Nullable PsiElement getElement() {
        return myElement;
    }

    private @NotNull CachedValueProvider.Result<Location> computeLocation() {
        Objects.requireNonNull(myElement);

        Location defaultLocation = new Location(-1, -1, "Unknown");
        PsiFile containingFile = myElement.getContainingFile();
        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(myElement.getProject());
        PsiFile topLevelFile = injectedLanguageManager.getTopLevelFile(myElement);
        if (topLevelFile == null) return CachedValueProvider.Result.create(defaultLocation, ModificationTracker.NEVER_CHANGED);
        com.intellij.openapi.editor.Document document = PsiDocumentManager.getInstance(myElement.getProject()).getDocument(topLevelFile);
        if (document == null) return CachedValueProvider.Result.create(defaultLocation, ModificationTracker.NEVER_CHANGED);

        VirtualFile virtualFile = topLevelFile.getVirtualFile();
        int offset = injectedLanguageManager.injectedToHost(myElement, myElement.getTextOffset());
        int lineNumber = document.getLineNumber(offset);
        int column = offset - document.getLineStartOffset(lineNumber);
        Location location = new Location(lineNumber + 1, column + 1, virtualFile != null ? virtualFile.getPath() : null);
        return CachedValueProvider.Result.create(location, containingFile, topLevelFile, document);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceLocation that = (SourceLocation) o;

        if (getLine() != that.getLine()) return false;
        if (getColumn() != that.getColumn()) return false;
        return Objects.equals(getSourceName(), that.getSourceName());
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Integer.hashCode(getLine());
        result = 31 * result + Integer.hashCode(getColumn());
        result = 31 * result + Objects.hashCode(getSourceName());
        return result;
    }

    @Override
    public String toString() {
        return "SourceLocation{" +
            "line=" + getLine() +
            ", column=" + getColumn() +
            (getSourceName() != null ? ", sourceName=" + getSourceName() : "") +
            '}';
    }

    private static final class Location {
        private final int line;
        private final int column;
        private final String sourceName;

        Location(int line, int column, @Nullable String sourceName) {
            this.line = line;
            this.column = column;
            this.sourceName = sourceName;
        }
    }
}
