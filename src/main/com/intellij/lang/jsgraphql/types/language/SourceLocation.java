package com.intellij.lang.jsgraphql.types.language;


import com.intellij.lang.jsgraphql.types.PublicApi;

import java.io.Serializable;
import java.util.Objects;

@PublicApi
public class SourceLocation implements Serializable {

    private final int line;
    private final int column;
    private final String sourceName;

    public SourceLocation(int line, int column) {
        this(line, column, null);
    }

    public SourceLocation(int line, int column, String sourceName) {
        this.line = line;
        this.column = column;
        this.sourceName = sourceName;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getSourceName() {
        return sourceName;
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
}
