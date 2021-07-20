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
package com.intellij.lang.jsgraphql.types.execution.batched;

import com.intellij.lang.jsgraphql.types.Internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Internal
@Deprecated
public class MapOrList {


    private Map<String, Object> map;
    private List<Object> list;

    public static MapOrList createList(List<Object> list) {
        MapOrList mapOrList = new MapOrList();
        mapOrList.list = list;
        return mapOrList;
    }

    public static MapOrList createMap(Map<String, Object> map) {
        MapOrList mapOrList = new MapOrList();
        mapOrList.map = map;
        return mapOrList;
    }

    public MapOrList createAndPutMap(String key) {
        Map<String, Object> map = new LinkedHashMap<>();
        putOrAdd(key, map);
        return createMap(map);
    }

    public MapOrList createAndPutList(String key) {
        List<Object> resultList = new ArrayList<>();
        putOrAdd(key, resultList);
        return createList(resultList);
    }

    public void putOrAdd(String fieldName, Object value) {
        if (this.map != null) {
            this.map.put(fieldName, value);
        } else {
            this.list.add(value);
        }
    }


    public Object toObject() {
        if (this.map != null) {
            return this.map;
        } else {
            return this.list;
        }
    }
}
