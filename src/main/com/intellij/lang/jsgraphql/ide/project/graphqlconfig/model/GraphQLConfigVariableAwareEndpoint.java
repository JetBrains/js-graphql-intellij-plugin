/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model;

import com.google.common.collect.Maps;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A GraphQL Config Endpoint with ${env:name-of-env-variables} expanded
 */
public class GraphQLConfigVariableAwareEndpoint {

    Function<String, String> GET_ENV_VAR = System::getProperty;

    private final static Pattern ENV_PATTERN = Pattern.compile("\\$\\{(?<var>[^}]*)}");

    private final GraphQLConfigEndpoint endpoint;
    private final Project project;

    public GraphQLConfigVariableAwareEndpoint(GraphQLConfigEndpoint endpoint, Project project) {
        this.endpoint = endpoint;
        this.project = project;
    }

    public String getUrl() {
        if (endpoint.url != null) {
            return expandVariables(endpoint.url);
        }
        return null;
    }

    public Map<String, Object> getHeaders() {
        if (endpoint.headers != null) {
            return expandVariables(Maps.newLinkedHashMap(endpoint.headers));
        }
        return null;
    }

    public static boolean containsVariable(String rawValue) {
        return ENV_PATTERN.matcher(rawValue).find();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> expandVariables(Map<String, Object> map) {
        map.keySet().forEach(key -> {
            Object value = map.get(key);
            if (value instanceof String) {
                map.put(key, expandVariables((String) value));
            } else if (value instanceof Map) {
                map.put(key, expandVariables(Maps.newLinkedHashMap((Map<String, Object>) value)));
            }
        });
        return map;
    }

    public String expandVariables(String rawValue) {

        Matcher matcher = ENV_PATTERN.matcher(rawValue);

        StringBuffer sb = new StringBuffer(rawValue.length());
        while (matcher.find()) {
            String var = matcher.group(1);
            String[] parts = var.split(":");
            if (parts.length != 2) {
                Notifications.Bus.notify(new Notification("GraphQL", "Unexpected env variable format", "Expected variable source and variable value separated by a colon, e.g. env:myvar, got: " + var, NotificationType.ERROR));
                continue;
            }
            final String varSource = parts[0];
            final String varName = parts[1];
            if (!"env".equals(varSource)) {
                Notifications.Bus.notify(new Notification("GraphQL", "Unsupported variable source", "Supported variables sources are 'env', but got: " + varSource, NotificationType.ERROR));
                continue;
            }
            String varValue = GET_ENV_VAR.apply(varName);
            if (varValue == null || varValue.trim().isEmpty()) {
                final VariableDialog dialog = new VariableDialog(project, varName);
                if (dialog.showAndGet()) {
                    varValue = dialog.getValue();
                } else {
                    continue;
                }
            }
            matcher.appendReplacement(sb, varValue);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    static class VariableDialog extends DialogWrapper {

        private static Map<String, String> previousVariables = Maps.newHashMap();

        private final String variableName;
        private JBTextField textField;

        VariableDialog(@NotNull Project project, String variableName) {
            super(project);
            this.variableName = variableName;
            setTitle("Enter Missing GraphQL \"" + variableName + "\" Environment Variable");
            init();
        }

        @Nullable
        @Override
        public JComponent getPreferredFocusedComponent() {
            return this.textField;
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            textField = new JBTextField();
            textField.setText(previousVariables.getOrDefault(variableName, ""));
            myPreferredFocusedComponent = textField;
            JBLabel hint = new JBLabel("<html><b>Hint</b>: Specify environment variables using <code>-DvarName=varValue</code> on the IDE command line.<div style=\"margin-top: 6\">This dialog stores the entered value until the IDE is restarted.</div></html>");
            return FormBuilder.createFormBuilder().addLabeledComponent(variableName, textField).addComponent(hint).getPanel();
        }

        @Override
        protected void doOKAction() {
            previousVariables.put(variableName, textField.getText());
            super.doOKAction();
        }

        public String getValue() {
            return textField.getText();
        }
    }

}
