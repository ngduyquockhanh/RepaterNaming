package org.ndqk;
import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepeaterNaming implements BurpExtension {
    private MontoyaApi api;
    private  JCheckBox showMethodCheckBox = new JCheckBox("Show HTTP method");
    private JCheckBox regexPathCheckBox = new JCheckBox("Regex path");
    private JCheckBox showReponseStatusCheckBox = new JCheckBox("Show HTTP response status");

    private  JTextField inputField = new JTextField(20);
    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("RepeaterNaming");
        JLabel label = new JLabel("Config Name Tab (use regex)");

        // Create layout
        JPanel panel = new JPanel();
        panel.add(label);
        panel.add(showMethodCheckBox);
        panel.add(regexPathCheckBox);
        panel.add(inputField);
        panel.add(showReponseStatusCheckBox);

        api.userInterface().registerSuiteTab("RepeaterNaming", panel);
        api.userInterface().registerContextMenuItemsProvider(new MyContextMenu());

    }

    class MyContextMenu implements ContextMenuItemsProvider {

        public MyContextMenu() {
        }

        @Override
        public List<Component> provideMenuItems(ContextMenuEvent event) {
            ArrayList<Component> menus = new ArrayList<>();
            JMenuItem TestJMenuItem = new JMenuItem("Send To Repeater");
            TestJMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<HttpRequestResponse> httpRequestResponses = event.selectedRequestResponses();
                    httpRequestResponses.forEach(httpRequestResponse -> {
                        String tabName = httpRequestResponse.request().path();
                        if (regexPathCheckBox.isSelected()){
                            String regexPattern = inputField.getText().trim();
                            Pattern pattern = Pattern.compile(regexPattern);
                            Matcher matcher = pattern.matcher(httpRequestResponse.request().path());
                            if (matcher.find()){
                                tabName = matcher.replaceFirst("");
                            }
                        }
                        if (showMethodCheckBox.isSelected()){
                            tabName = httpRequestResponse.request().method() + " " + tabName;
                        }

                        if (showReponseStatusCheckBox.isSelected()){
                            tabName = tabName + " : " +httpRequestResponse.response().statusCode();
                        }

                        api.repeater().sendToRepeater(httpRequestResponse.request(), tabName);
                    });
                }
            });
            menus.add(TestJMenuItem);
            return menus;
        }
    }
}