/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.execution.nextgen.result;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.util.NodeAdapter;
import com.intellij.lang.jsgraphql.types.util.NodeLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.Assert.assertTrue;

@Internal
public class ResultNodeAdapter implements NodeAdapter<ExecutionResultNode> {

    public static final ResultNodeAdapter RESULT_NODE_ADAPTER = new ResultNodeAdapter();

    private ResultNodeAdapter() {

    }

    @Override
    public Map<String, List<ExecutionResultNode>> getNamedChildren(ExecutionResultNode parentNode) {
        return Collections.singletonMap(null, parentNode.getChildren());
    }

    @Override
    public ExecutionResultNode withNewChildren(ExecutionResultNode parentNode, Map<String, List<ExecutionResultNode>> newChildren) {
        assertTrue(newChildren.size() == 1);
        List<ExecutionResultNode> childrenList = newChildren.get(null);
        assertNotNull(childrenList);
        return parentNode.withNewChildren(childrenList);
    }

    @Override
    public ExecutionResultNode removeChild(ExecutionResultNode parentNode, NodeLocation location) {
        int index = location.getIndex();
        List<ExecutionResultNode> childrenList = new ArrayList<>(parentNode.getChildren());
        assertTrue(index >= 0 && index < childrenList.size(), () -> "The remove index MUST be within the range of the children");
        childrenList.remove(index);
        return parentNode.withNewChildren(childrenList);
    }
}
