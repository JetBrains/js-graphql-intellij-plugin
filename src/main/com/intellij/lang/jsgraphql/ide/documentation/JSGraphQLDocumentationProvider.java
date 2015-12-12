/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.documentation;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.ide.injection.JSGraphQLLanguageInjectionUtil;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceClient;
import com.intellij.lang.jsgraphql.languageservice.api.TokenDocumentationResponse;
import com.intellij.lang.jsgraphql.languageservice.api.TypeDocumentationResponse;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.ui.GuiUtils;
import com.intellij.util.containers.ContainerUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class JSGraphQLDocumentationProvider extends DocumentationProviderEx {

    public final static String GRAPHQL_DOC_PREFIX = "GraphQL";
    public final static String GRAPHQL_DOC_TYPE = "Type";

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return createQuickNavigateDocumentation(element, false);
    }

    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        return createQuickNavigateDocumentation(element, true);
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        if(link.startsWith(GRAPHQL_DOC_PREFIX)) {
            return new JSGraphQLDocumentationPsiElement(context, link);
        }
        return super.getDocumentationElementForLink(psiManager, link, context);
    }

    @Nullable
    private String createQuickNavigateDocumentation(PsiElement element, boolean fullDocumentation) {
        if(element instanceof JSGraphQLDocumentationPsiElement) {
            JSGraphQLDocumentationPsiElement docElement = (JSGraphQLDocumentationPsiElement)element;
            return getTypeDocumentation(docElement);
        }

        final IElementType elementType = element.getNode().getElementType();
        if(elementType == JSGraphQLTokenTypes.PROPERTY || elementType == JSGraphQLTokenTypes.ATOM) {
            final Editor editor = resolveEditor(element);
            if(editor != null) {
                final String buffer = element.getContainingFile().getText();
                final LogicalPosition pos = getTokenPos(buffer, element);
                if(elementType == JSGraphQLTokenTypes.PROPERTY) {
                    final boolean relay = JSGraphQLLanguageInjectionUtil.isRelayInjection(element.getContainingFile());
                    final TokenDocumentationResponse tokenDocumentation = JSGraphQLNodeLanguageServiceClient.getTokenDocumentation(
                            buffer,
                            pos.line,
                            pos.column,
                            editor.getProject(),
                            relay
                    );
                    if (tokenDocumentation != null) {
                        String doc = "";
                        if (tokenDocumentation.getDescription() != null) {
                            doc += "<div style=\"margin-bottom: 4px\">" + tokenDocumentation.getDescription() + "</div>";
                        }
                        doc += "<code>" + element.getText() + ": " + getTypeHyperLink(tokenDocumentation.getType()) + "</code>";
                        return getDocTemplate(fullDocumentation).replace("${body}", doc);
                    }
                } else if(elementType == JSGraphQLTokenTypes.ATOM) {
                    if(fullDocumentation && editor.getProject() != null) {
                        final PsiManager psiManager = PsiManager.getInstance(editor.getProject());
                        final String link = GRAPHQL_DOC_PREFIX + "/" + GRAPHQL_DOC_TYPE + "/" + element.getText();
                        final PsiElement documentationElement = getDocumentationElementForLink(psiManager, link, element);
                        if(documentationElement instanceof JSGraphQLDocumentationPsiElement) {
                            return getTypeDocumentation((JSGraphQLDocumentationPsiElement) documentationElement);
                        }
                    }
                    TypeDocumentationResponse typeDocumentation = JSGraphQLNodeLanguageServiceClient.getTypeDocumentation(
                            element.getText(),
                            editor.getProject()
                    );
                    if(typeDocumentation != null) {
                        String doc = "";
                        if (typeDocumentation.description != null) {
                            doc += "<div style=\"margin-bottom: 4px\">" + typeDocumentation.description + "</div>";
                        }
                        doc += "<code>" + element.getText();
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
                }
            }
        }

        return null;
    }

    private String getDocTemplate(boolean fullDocumentation) {
        String doc = "<body style=\"margin: 0\">";
        if (fullDocumentation) {
            doc += createIndex(null);
        }
        doc += "<div style=\"margin: 4px 8px 4px 8px;\">${body}</div></body>";
        return doc;
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

    private String getTypeDocumentation(JSGraphQLDocumentationPsiElement docElement) {

        final EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        final Color borderColor = globalScheme.getDefaultForeground();
        //final TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(JSGraphQLSyntaxHighlighter.PROPERTY).clone();

        final Editor editor = resolveEditor(docElement);
        final StringBuilder sb = new StringBuilder();
        TypeDocumentationResponse typeDocumentation = JSGraphQLNodeLanguageServiceClient.getTypeDocumentation(docElement.getType(), editor.getProject());
        if(typeDocumentation != null) {
            sb.append("<html style=\"margin: 0;\"><body style=\"margin: 0;\">");
            sb.append(createIndex(borderColor));
            sb.append("<div style=\"margin: 4px 8px 4px 8px;\"");
            sb.append("<h1 style=\"margin: 0 0 8px 0; font-size: 200%\">").append(docElement.getType()).append("</h1>");
            if(typeDocumentation.description != null) {
                sb.append("<div>").append(typeDocumentation.description).append("</div><br>");
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
                        sb.append("<div style=\"margin-left: 8px; margin-top: 4px; margin-bottom: 4px;\">").append(field.description).append("</div>");
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

    private Editor resolveEditor(final PsiElement element) {
        Ref<Editor> editorRef = new Ref<>();
        Runnable runnable = () -> editorRef.set(PsiEditorUtil.Service.getInstance().findEditorByPsiElement(element));
        if(EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
                EventQueue.invokeAndWait(runnable);
            } catch (InterruptedException | InvocationTargetException e) {
                return null;
            }
        }
        return editorRef.get();
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

