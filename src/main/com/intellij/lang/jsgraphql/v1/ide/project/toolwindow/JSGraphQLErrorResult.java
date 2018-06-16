/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.project.toolwindow;

/**
 * Represents an error in the GraphQL tool window error tree.
 */
public class JSGraphQLErrorResult {

    final int myColumn;
    final int myLine;
    final String myErrorText;
    final String myCategory;
    final String myFileAbsoluteSystemDependPath;

    public JSGraphQLErrorResult(String errorText, String fileAbsolutePath, String category, int line, int column) {
        myCategory = category;
        myColumn = column - 1;
        myLine = line - 1;
        myErrorText = errorText;
        myFileAbsoluteSystemDependPath = fileAbsolutePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JSGraphQLErrorResult that = (JSGraphQLErrorResult) o;
        if (myColumn != that.myColumn) return false;
        if (myLine != that.myLine) return false;
        if (myErrorText != null ? !myErrorText.equals(that.myErrorText) : that.myErrorText != null) return false;
        return myCategory != null ? myCategory.equals(that.myCategory) : that.myCategory == null && (myFileAbsoluteSystemDependPath != null ? myFileAbsoluteSystemDependPath.equals(that.myFileAbsoluteSystemDependPath) : that.myFileAbsoluteSystemDependPath == null);
    }

    public int hashCode() {
        int result = myColumn;
        result = 31 * result + myLine;
        result = 31 * result + (myErrorText != null ? myErrorText.hashCode() : 0);
        result = 31 * result + (myCategory != null ? myCategory.hashCode() : 0);
        result = 31 * result + (myFileAbsoluteSystemDependPath != null ? myFileAbsoluteSystemDependPath.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "JSGraphQLErrorResult{myColumn=" + myColumn + ", myLine=" + myLine + ", myErrorText=\'" + myErrorText + '\'' + ", myCategory=\'" + myCategory + '\'' + ", myFileAbsoluteSystemDependPath=\'" + myFileAbsoluteSystemDependPath + '\'' + '}';
    }
}
