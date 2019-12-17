/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.web.app.main;

import com.haulmont.cuba.core.global.ClientType;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Events;
import com.haulmont.cuba.core.global.FtsConfigHelper;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.Route;
import com.haulmont.cuba.gui.ScreenTools;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.dev.LayoutAnalyzerContextMenuProvider;
import com.haulmont.cuba.gui.components.mainwindow.*;
import com.haulmont.cuba.gui.events.UserRemovedEvent;
import com.haulmont.cuba.gui.events.UserSubstitutionsChangedEvent;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiControllerUtils;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.security.app.UserSettingService;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.WebConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import javax.annotation.Nullable;

import static com.haulmont.cuba.gui.ComponentsHelper.setStyleName;

/**
 * Base class for a controller of application Main screen.
 */
@Route(path = "main", root = true)
@UiDescriptor("main-screen.xml")
@UiController("main")
public class MainScreen extends Screen implements Window.HasWorkArea, Window.HasUserIndicator {

    public static final String SIDEMENU_COLLAPSED_STATE = "sidemenuCollapsed";
    public static final String SIDEMENU_COLLAPSED_STYLENAME = "collapsed";
    public static final String SIDEMENU_COLLAPSE_ICON = "«";
    public static final String SIDEMENU_EXPAND_ICON = "»";

    protected static final String APP_LOGO_IMAGE = "application.logoImage";

    public MainScreen() {
        addInitListener(this::initComponents);
    }

    protected void initComponents(@SuppressWarnings("unused") InitEvent e) {
        initLogoImage();
        initFtsField();
        initUserIndicator();
        initTitleBar();
        initMenu();
        initLayoutAnalyzerContextMenu();
    }

    protected void initUserIndicator() {
        UserIndicator userIndicator = getUserIndicator();
        if (userIndicator != null) {
            boolean authenticated = AppUI.getCurrent().hasAuthenticatedSession();
            userIndicator.setVisible(authenticated);
        }
    }

    protected void initLogoImage() {
        Image logoImage = getLogoImage();
        String logoImagePath = getBeanLocator().get(Messages.class)
                .getMainMessage(APP_LOGO_IMAGE);

        if (logoImage != null
                && StringUtils.isNotBlank(logoImagePath)
                && !APP_LOGO_IMAGE.equals(logoImagePath)) {
            logoImage.setSource(ThemeResource.class).setPath(logoImagePath);
        }
    }

    protected void initFtsField() {
        FtsField ftsField = getFtsField();
        if (ftsField != null && !FtsConfigHelper.getEnabled()) {
            ftsField.setVisible(false);
        }
    }

    protected void initLayoutAnalyzerContextMenu() {
        Image logoImage = getLogoImage();
        if (logoImage != null) {
            LayoutAnalyzerContextMenuProvider laContextMenuProvider =
                    getBeanLocator().get(LayoutAnalyzerContextMenuProvider.NAME);
            laContextMenuProvider.initContextMenu(getWindow(), logoImage);
        }
    }

    protected void initMenu() {
        Component menu = getAppMenu();
        if (menu == null) {
            menu = getSideMenu();
        }

        if (menu != null) {
            ((Component.Focusable) menu).focus();
        }

        initCollapsibleMenu();
        initCollapseMenuButton();
    }

    protected void initCollapsibleMenu() {
        Component sideMenuContainer = getWindow().getComponent("sideMenuContainer");
        if (sideMenuContainer instanceof CssLayout) {
            String menuCollapsedSetting = getBeanLocator().get(UserSettingService.class)
                    .loadSetting(ClientType.WEB, SIDEMENU_COLLAPSED_STATE);

            setSideMenuCollapsed(Boolean.parseBoolean(menuCollapsedSetting));
        }
    }

    protected void initCollapseMenuButton() {
        Button collapseMenuButton = getMenuCollapseButton();
        if (collapseMenuButton != null) {
            collapseMenuButton.addClickListener(event ->
                    setSideMenuCollapsed(!isMenuCollapsed()));
        }
    }

    protected void initTitleBar() {
        Configuration configuration = getBeanLocator().get(Configuration.class);
        if (configuration.getConfig(WebConfig.class).getUseInverseHeader()) {
            Component titleBar = getTitleBar();
            if (titleBar != null) {
                titleBar.setStyleName("c-app-menubar c-inverse-header");
            }
        }
    }

    @Order(Events.LOWEST_PLATFORM_PRECEDENCE - 100)
    @EventListener
    protected void onUserSubstitutionsChange(UserSubstitutionsChangedEvent event) {
        UserIndicator userIndicator = getUserIndicator();
        if (userIndicator != null) {
            userIndicator.refreshUserSubstitutions();
        }
    }

    @Order(Events.LOWEST_PLATFORM_PRECEDENCE - 100)
    @EventListener
    protected void onUserRemove(UserRemovedEvent event) {
        UserIndicator userIndicator = getUserIndicator();
        if (userIndicator != null) {
            userIndicator.refreshUserSubstitutions();
        }
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        Screens screens = UiControllerUtils.getScreenContext(this)
                .getScreens();
        getBeanLocator().get(ScreenTools.class)
                .openDefaultScreen(screens);
    }

    protected void setSideMenuCollapsed(boolean collapsed) {
        UserSettingService userSettings = getBeanLocator().get(UserSettingService.class);

        CssLayout sideMenuPanel = getSideMenuPanel();
        Button collapseMenuButton = getMenuCollapseButton();
        Component sideMenuContainer = getWindow().getComponent("sideMenuContainer");

        setStyleName(sideMenuPanel, SIDEMENU_COLLAPSED_STYLENAME, collapsed);
        setStyleName(sideMenuContainer, SIDEMENU_COLLAPSED_STYLENAME, collapsed);

        if (collapseMenuButton != null) {
            Messages messages = getBeanLocator().get(Messages.class);
            if (collapsed) {
                collapseMenuButton.setCaption(SIDEMENU_EXPAND_ICON);
                collapseMenuButton.setDescription(messages.getMainMessage("sideMenuExpand"));
            } else {
                collapseMenuButton.setCaption(SIDEMENU_COLLAPSE_ICON);
                collapseMenuButton.setDescription(messages.getMainMessage("sideMenuCollapse"));
            }
        }

        userSettings.saveSetting(ClientType.WEB, SIDEMENU_COLLAPSED_STATE, String.valueOf(collapsed));
    }

    protected boolean isMenuCollapsed() {
        CssLayout sideMenuPanel = getSideMenuPanel();
        return sideMenuPanel != null
                && sideMenuPanel.getStyleName().contains(SIDEMENU_COLLAPSED_STYLENAME);
    }

    @Nullable
    protected CssLayout getSideMenuPanel() {
        return (CssLayout) getWindow().getComponent("sideMenuPanel");
    }

    @Nullable
    protected Button getMenuCollapseButton() {
        return (Button) getWindow().getComponent("collapseMenuButton");
    }

    @Nullable
    @Override
    public AppWorkArea getWorkArea() {
        return (AppWorkArea) getWindow().getComponent("workArea");
    }

    @Nullable
    @Override
    public UserIndicator getUserIndicator() {
        return (UserIndicator) getWindow().getComponent("userIndicator");
    }

    @Nullable
    protected Image getLogoImage() {
        return (Image) getWindow().getComponent("logoImage");
    }

    @Nullable
    protected FtsField getFtsField() {
        return (FtsField) getWindow().getComponent("ftsField");
    }

    @Nullable
    protected AppMenu getAppMenu() {
        return (AppMenu) getWindow().getComponent("appMenu");
    }

    @Nullable
    protected SideMenu getSideMenu() {
        return (SideMenu) getWindow().getComponent("sideMenu");
    }

    @Nullable
    protected Component getTitleBar() {
        return getWindow().getComponent("titleBar");
    }

    @Nullable
    protected UserActionsButton getUserActionsButton() {
        return (UserActionsButton) getWindow().getComponent("userActionsButton");
    }
}
