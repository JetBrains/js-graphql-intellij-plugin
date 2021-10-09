package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model;

public class GraphQLConfigCertificate {
    public enum Encoding {PEM}

    public String path;
    public Encoding format = Encoding.PEM;
}
