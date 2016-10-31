/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.documentation;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointDocumentationAware;
import com.intellij.lang.jsgraphql.ide.injection.JSGraphQLLanguageInjectionUtil;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceClient;
import com.intellij.lang.jsgraphql.languageservice.api.TokenDocumentationResponse;
import com.intellij.lang.jsgraphql.languageservice.api.TypeDocumentationResponse;
import com.intellij.lang.jsgraphql.psi.JSGraphQLAttributePsiElement;
import com.intellij.lang.jsgraphql.psi.JSGraphQLFragmentDefinitionPsiElement;
import com.intellij.lang.jsgraphql.psi.JSGraphQLNamedPropertyPsiElement;
import com.intellij.lang.jsgraphql.psi.JSGraphQLNamedPsiElement;
import com.intellij.lang.jsgraphql.psi.JSGraphQLNamedTypePsiElement;
import com.intellij.lang.jsgraphql.schema.ide.project.JSGraphQLSchemaLanguageProjectService;
import com.intellij.lang.jsgraphql.schema.psi.JSGraphQLSchemaFile;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.GuiUtils;
import com.intellij.util.containers.ContainerUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class JSGraphQLDocumentationProvider extends DocumentationProviderEx {

    public final static String GRAPHQL_DOC_PREFIX = "GraphQL";
    public final static String GRAPHQL_DOC_TYPE = "Type";

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if(isDocumentationSupported(element)) {
            return createQuickNavigateDocumentation(resolveDocumentationElement(element, originalElement), false);
        }
        return null;
    }

    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if(isDocumentationSupported(element)) {
            return createQuickNavigateDocumentation(resolveDocumentationElement(element, originalElement), true);
        }
        return null;
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        if(link.startsWith(GRAPHQL_DOC_PREFIX)) {
            return new JSGraphQLDocumentationPsiElement(context, link);
        }
        return super.getDocumentationElementForLink(psiManager, link, context);
    }

    private boolean isDocumentationSupported(PsiElement element) {
        final PsiFile file = element.getContainingFile();
        if(file instanceof JSGraphQLSchemaFile) {
            return JSGraphQLSchemaLanguageProjectService.isProjectSchemaFile(file.getVirtualFile());
        }
        return true;
    }

    // ensures that the built-in schema info that points to the schema file are sent to the doc methods as the originalElement
    private PsiElement resolveDocumentationElement(PsiElement element, PsiElement originalElement) {
        if(element instanceof JSGraphQLSchemaFile) {
            if(originalElement instanceof JSGraphQLNamedPsiElement) {
                return originalElement;
            } else if(originalElement.getParent() instanceof JSGraphQLNamedPsiElement) {
                // the doc can be invoked on the leaf node, so move up to the parent which contains the type/field name
                return originalElement.getParent();
            }
        }
        return element;
    }

    @Nullable
    private String createQuickNavigateDocumentation(PsiElement element, boolean fullDocumentation) {
        if(!isDocumentationSupported(element)) {
            return null;
        }
        if(element instanceof JSGraphQLDocumentationPsiElement) {
            JSGraphQLDocumentationPsiElement docElement = (JSGraphQLDocumentationPsiElement)element;
            return getTypeDocumentation(docElement);
        }

        if(element instanceof JSGraphQLNamedPsiElement) {

            final Project project = element.getProject();

            if(element instanceof JSGraphQLNamedPropertyPsiElement) {
                final JSGraphQLNamedPropertyPsiElement propertyPsiElement = (JSGraphQLNamedPropertyPsiElement) element;
                String typeName = JSGraphQLSchemaLanguageProjectService.getService(project).getTypeName(propertyPsiElement);
                if(typeName == null) {
                    // in structure view we don't get the schema psi element, so try to find it using the reference
                    final PsiReference reference = propertyPsiElement.getReference();
                    if(reference != null) {
                        final PsiElement schemaReference = reference.resolve();
                        if(schemaReference instanceof JSGraphQLNamedPropertyPsiElement) {
                            typeName = JSGraphQLSchemaLanguageProjectService.getService(project).getTypeName((JSGraphQLNamedPropertyPsiElement) schemaReference);
                        } else if(schemaReference instanceof JSGraphQLSchemaFile) {
                            // field belongs to a built in type which isn't shown in the schema file
                            // hence our reference to the file -- see JSGraphQLSchemaLanguageProjectService.getReference()
                            final String buffer = element.getContainingFile().getText();
                            final LogicalPosition pos = getTokenPos(buffer, element);
                            final String environment = JSGraphQLLanguageInjectionUtil.getEnvironment(element.getContainingFile());
                            final TokenDocumentationResponse tokenDocumentation = JSGraphQLNodeLanguageServiceClient.getTokenDocumentation(
                                    buffer,
                                    pos.line,
                                    pos.column,
                                    project,
                                    environment
                            );
                            if(tokenDocumentation != null) {
                                String doc = "";
                                if (tokenDocumentation.getDescription() != null) {
                                    if(tokenDocumentation.getType() != null && !JSGraphQLSchemaLanguageProjectService.SCALAR_TYPES.contains(tokenDocumentation.getType())) {
                                        doc += "<div style=\"margin-bottom: 4px\">" + tokenDocumentation.getDescription() + "</div>";
                                    }
                                }
                                doc += "<code>" + element.getText() + ": " + getTypeHyperLink(tokenDocumentation.getType()) + "</code>";
                                return getDocTemplate(fullDocumentation).replace("${body}", doc);
                            }
                        }
                    }
                }
                if(typeName != null) {
                    final TokenDocumentationResponse fieldDocumentation = JSGraphQLNodeLanguageServiceClient.getFieldDocumentation(typeName, propertyPsiElement.getName(), project);
                    if (fieldDocumentation != null) {
                        String doc = "";
                        if (fieldDocumentation.getDescription() != null) {
                            doc += "<div style=\"margin-bottom: 4px\">" + StringEscapeUtils.escapeHtml(fieldDocumentation.getDescription()) + "</div>";
                        }
                        String typeNameOrLink = fullDocumentation ? getTypeHyperLink(typeName) : typeName;
                        doc += "<code>" + typeNameOrLink + " <b>" + element.getText() + "</b>: " + getTypeHyperLink(fieldDocumentation.getType()) + "</code>";
                        return getDocTemplate(fullDocumentation).replace("${body}", doc);
                    }
                }
            } else if(element instanceof JSGraphQLNamedTypePsiElement) {
                if (((JSGraphQLNamedTypePsiElement) element).isDefinition()) {
                    if (element.getParent() instanceof JSGraphQLFragmentDefinitionPsiElement) {
                        // the named type represents the name of a fragment definition,
                        // so return doc along the lines of 'fragment MyFrag on SomeType'
                        final StringBuilder doc = new StringBuilder("<code>fragment ");
                        doc.append("<b>").append(element.getText()).append("</b>");
                        final JSGraphQLNamedTypePsiElement fragmentType = PsiTreeUtil.getNextSiblingOfType(element, JSGraphQLNamedTypePsiElement.class);
                        if(fragmentType != null) {
                            doc.append(" on ").append(getTypeHyperLink(fragmentType.getName()));
                        }
                        doc.append("</code>");
                        return getDocTemplate(fullDocumentation).replace("${body}", doc);
                    }
                }
                if(fullDocumentation) {
                    final PsiManager psiManager = PsiManager.getInstance(project);
                    final String link = GRAPHQL_DOC_PREFIX + "/" + GRAPHQL_DOC_TYPE + "/" + element.getText();
                    final PsiElement documentationElement = getDocumentationElementForLink(psiManager, link, element);
                    if(documentationElement instanceof JSGraphQLDocumentationPsiElement) {
                        return getTypeDocumentation((JSGraphQLDocumentationPsiElement) documentationElement);
                    }
                }
                TypeDocumentationResponse typeDocumentation = JSGraphQLNodeLanguageServiceClient.getTypeDocumentation(
                        element.getText(),
                        project
                );
                if(typeDocumentation != null) {
                    String doc = "";
                    if (typeDocumentation.description != null) {
                        doc += "<div style=\"margin-bottom: 4px\">" + StringEscapeUtils.escapeHtml(typeDocumentation.description) + "</div>";
                    }
                    doc += "<code><b>" + element.getText() + "</b>";
                    if(!ContainerUtil.isEmpty(typeDocumentation.interfaces)) {
                        doc += ": ";
                        for (int i = 0; i < typeDocumentation.interfaces.size(); i++) {
                            if(i > 0)  {
                                doc += ", ";
                            }
                            doc += getTypeHyperLink(typeDocumentation.interfaces.get(i));
                        }
                    }
                    doc += "</code>";
                    return getDocTemplate(fullDocumentation).replace("${body}", doc);
                }
            } else if(element instanceof JSGraphQLAttributePsiElement) {
                String doc = "";
                PsiElement prevLeaf = PsiTreeUtil.prevLeaf(element);
                String documentation = "";
                while (prevLeaf instanceof PsiWhiteSpace || prevLeaf instanceof PsiComment) {
                    documentation = StringUtils.removeStart(prevLeaf.getText(), "# ") + documentation;
                    prevLeaf = PsiTreeUtil.prevLeaf(prevLeaf);
                }
                documentation = documentation.trim();
                if(StringUtils.isNotBlank(documentation)) {
                    doc += "<div style=\"margin-bottom: 4px\">" + StringEscapeUtils.escapeHtml(documentation) + "</div>";
                }
                doc += "<code>"  + element.getText();
                PsiElement nextLeaf = PsiTreeUtil.nextLeaf(element);
                // include the attribute type (stopping at newline, ",", and ")")
                while(nextLeaf != null && !nextLeaf.getText().contains("\n") && !nextLeaf.getText().contains(",") && !nextLeaf.getText().contains(")")) {
                    doc += nextLeaf.getText();
                    nextLeaf = PsiTreeUtil.nextLeaf(nextLeaf);
                }
                doc += "</code>";
                return getDocTemplate(fullDocumentation).replace("${body}", doc);
            }

        } else if(element instanceof JSGraphQLEndpointDocumentationAware) {
            final JSGraphQLEndpointDocumentationAware documentationAware = (JSGraphQLEndpointDocumentationAware) element;
            final String documentation = documentationAware.getDocumentation(fullDocumentation);
            String doc = "";
            if(documentation != null) {
                doc += "<div style=\"margin-bottom: 4px\">" + StringEscapeUtils.escapeHtml(documentation) + "</div>";
            }
            doc += "<code>" + documentationAware.getDeclaration() + "</code>";
            return getDocTemplate(fullDocumentation).replace("${body}", doc);
        }

        return null;
    }

    private LogicalPosition getTokenPos(String buffer, PsiElement element) {
        int line = 0;
        int column = 0;
        int targetPos = element.getTextOffset();
        int pos = 0;
        while(pos < targetPos) {
            char c = buffer.charAt(pos);
            switch (c) {
                case '\n':
                    line++;
                    column = 0;
                    break;
                default:
                    column++;
            }
            pos++;
        }
        return new LogicalPosition(line, column);
    }

    private String getDocTemplate(boolean fullDocumentation) {
        String doc = "<body style=\"margin: 0\">";
        if (fullDocumentation) {
            doc += createIndex(null);
        }
        doc += "<div style=\"margin: 4px 8px 4px 8px;\">${body}</div></body>";
        return doc;
    }

    private String getTypeDocumentation(JSGraphQLDocumentationPsiElement docElement) {

        final EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        final Color borderColor = globalScheme.getDefaultForeground();
        //final TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(JSGraphQLSyntaxHighlighter.PROPERTY).clone();

        final StringBuilder sb = new StringBuilder();
        TypeDocumentationResponse typeDocumentation = JSGraphQLNodeLanguageServiceClient.getTypeDocumentation(docElement.getType(), docElement.getProject());
        if(typeDocumentation != null) {
            sb.append("<html style=\"margin: 0;\"><body style=\"margin: 0;\">");
            sb.append(createIndex(borderColor));
            sb.append("<div style=\"margin: 4px 8px 4px 8px;\"");
            sb.append("<h1 style=\"margin: 0 0 8px 0; font-size: 200%\">").append(docElement.getType()).append("</h1>");
            if(typeDocumentation.description != null) {
                sb.append("<div>").append(StringEscapeUtils.escapeHtml(typeDocumentation.description)).append("</div><br>");
            }
            if(typeDocumentation.implementations != null && !typeDocumentation.implementations.isEmpty()) {
                sb.append(getSection(borderColor, "IMPLEMENTATIONS"));
                for (String implementation : typeDocumentation.implementations) {
                    sb.append("<div><code>").append(getTypeHyperLink(implementation)).append("</code></div>");
                }
                sb.append("<br><br>");
            }
            if(typeDocumentation.interfaces != null && !typeDocumentation.interfaces.isEmpty()) {
                sb.append(getSection(borderColor, "IMPLEMENTS"));
                for (String intf : typeDocumentation.interfaces) {
                    sb.append("<div><code>").append(getTypeHyperLink(intf)).append("</code></div>");
                }
                sb.append("<br><br>");
            }
            if(typeDocumentation.fields != null && !typeDocumentation.fields.isEmpty()) {
                sb.append(getSection(borderColor, "FIELDS"));
                for (TypeDocumentationResponse.Field field : typeDocumentation.fields) {
                    sb.append("<div style=\"margin-bottom: 4px\">- <code><b>").append(field.name).append("</b></code>");
                    if(field.args != null && !field.args.isEmpty()) {
                        sb.append("<code>(");
                        List<String> fieldArgs = Lists.newArrayListWithCapacity(field.args.size());
                        for (TypeDocumentationResponse.FieldArgument arg : field.args) {
                            fieldArgs.add(arg.name + ": " + getTypeHyperLink(arg.type));
                        }
                        sb.append(StringUtils.join(fieldArgs, ", "));
                        sb.append(")");
                    }
                    sb.append(": ").append(getTypeHyperLink(field.type));
                    if(field.description != null) {
                        sb.append("<div style=\"margin-left: 8px; margin-top: 4px; margin-bottom: 4px;\">");
                        sb.append(StringEscapeUtils.escapeHtml(field.description)).append("</div>");
                    }
                    sb.append("</code></div>");
                }
                sb.append("<br><br>");
            }
            sb.append("</div>");
            sb.append("</html></body>");
            return sb.toString();
        }
        return null;
    }

    @NotNull
    private String createIndex(Color borderColor) {
        if(borderColor == null) {
            final EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
            borderColor = globalScheme.getDefaultForeground();
        }
        return "<div style=\"border-bottom: 1px outset "+ GuiUtils.colorToHex(borderColor)+"; padding: 0 2px 8px 0; margin-bottom: 16px; text-align: right;\">Index: "+ getTypeHyperLink("Query", "Queries") + " - "+ getTypeHyperLink("Mutation", "Mutations")+"</div>";
    }

    @NotNull
    private String getSection(Color borderColor, String label) {
        return "<div style=\"margin-bottom: 8px; border-bottom: 1px solid; padding-bottom: 4px; "+ GuiUtils.colorToHex(borderColor)+";\">"+label+"</div>";
    }

    private String getTypeHyperLink(String type) {
        return getTypeHyperLink(type, null);
    }

    private String getTypeHyperLink(String type, String text) {
        // TODO: Investigate the use of colors like https://upsource.jetbrains.com/idea-community/file/1731d054af4ca27aa827c03929e27eeb0e6a8366/plugins%2Fproperties%2Fsrc%2Fcom%2Fintellij%2Flang%2Fproperties%2FPropertiesDocumentationProvider.java
        if(type == null) return "";
        final String typeName = type.replaceAll("[\\[\\]!]", "");
        final StringBuilder sb = new StringBuilder()
                .append(type.startsWith("[")?"[":"")
                .append("<a style=\"\" href=\"")
                .append(getTypeLink(typeName))
                .append("\">")
                .append(text == null ? typeName : text).append("</a>")
                .append(type.contains("!")?"!":"")
                .append(type.endsWith("]")?"]":"")
                .append(type.endsWith("]!")?"]!":"")
                ;
        return sb.toString();
    }

    private String getTypeLink(String typeName) {
        return new StringBuilder(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL)
                .append(GRAPHQL_DOC_PREFIX).append("/")
                .append(GRAPHQL_DOC_TYPE).append("/")
                .append(typeName).toString();
    }

}

