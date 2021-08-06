package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GraphQLExternalTypeDefinitionsProvider {
    private final static Logger LOG = Logger.getInstance(GraphQLExternalTypeDefinitionsProvider.class);

    private static final String BUILT_IN_SCHEMA = "BUILT_IN";
    private static final String RELAY = "RELAY";

    private final Project myProject;
    private final Map<String, PsiFile> myDefinitionFiles = new ConcurrentHashMap<>();

    public GraphQLExternalTypeDefinitionsProvider(@NotNull Project project) {
        myProject = project;
    }

    public static GraphQLExternalTypeDefinitionsProvider getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLExternalTypeDefinitionsProvider.class);
    }

    /**
     * Gets the built-in Schema that all endpoints support, including the introspection types, fields, directives and default scalars.
     */
    @NotNull
    public PsiFile getBuiltInSchema() {
        return getPsiFileFromResources(
            "graphql specification schema.graphql",
            "GraphQL Specification Schema",
            BUILT_IN_SCHEMA
        );
    }

    /**
     * Gets the built-in Relay Modern Directives schema
     */
    @NotNull
    public PsiFile getRelayModernDirectivesSchema() {
        return getPsiFileFromResources(
            "relay modern directives schema.graphql",
            "Relay Modern Directives Schema",
            RELAY
        );
    }

    @NotNull
    private PsiFile getPsiFileFromResources(@NotNull String resourceName,
                                            @NotNull String displayName,
                                            @NotNull String sourceType) {
        return myDefinitionFiles.computeIfAbsent(sourceType, s -> {
            final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(myProject);
            String specSchemaText = "";
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("META-INF/" + resourceName)) {
                if (inputStream != null) {
                    specSchemaText = new String(IOUtils.toByteArray(inputStream));
                }
            } catch (IOException e) {
                LOG.error("Unable to load schema", e);
                Notifications.Bus.notify(new Notification(
                    GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                    "Unable to load " + displayName,
                    GraphQLNotificationUtil.formatExceptionMessage(e),
                    NotificationType.ERROR
                ));
            }
            PsiFile psiFile = psiFileFactory.createFileFromText(displayName, GraphQLLanguage.INSTANCE, specSchemaText);
            try {
                psiFile.getVirtualFile().setWritable(false);
            } catch (IOException e) {
                LOG.info(e);
            }
            return psiFile;
        });
    }
}
