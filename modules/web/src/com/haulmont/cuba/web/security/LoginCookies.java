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

package com.haulmont.cuba.web.security;

import com.haulmont.bali.util.URLEncodeUtils;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.security.app.UserManagementService;
import com.haulmont.cuba.security.auth.RememberMeCredentials;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.Connection;
import com.haulmont.cuba.web.WebConfig;
import com.haulmont.cuba.web.sys.VaadinSessionScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.haulmont.cuba.web.App.*;

/**
 * Allow to save/clear cookies for "remember me" and do login using cookies.
 */
@Component(LoginCookies.NAME)
@Scope(VaadinSessionScope.NAME)
public class LoginCookies {
    public static final String NAME = "cuba_LoginCookies";

    protected WebConfig webConfig;

    protected GlobalConfig globalConfig;

    protected UserManagementService userManagementService;

    protected App app;

    protected Connection connection;

    @Inject
    protected void setWebConfig(WebConfig webConfig) {
        this.webConfig = webConfig;
    }

    @Inject
    protected void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    @Inject
    protected void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @Inject
    protected void setApp(App app) {
        this.app = app;
    }

    @Inject
    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Sets "remember me" cookies.
     *
     * @param login login to save.
     */
    public void setRememberMeCookies(String login) {
        if (connection.isAuthenticated() && webConfig.getRememberMeEnabled()) {
            int rememberMeExpiration = globalConfig.getRememberMeExpirationTimeoutSec();

            app.addCookie(COOKIE_REMEMBER_ME, Boolean.TRUE.toString(), rememberMeExpiration);

            String encodedLogin = URLEncodeUtils.encodeUtf8(login);
            app.addCookie(COOKIE_LOGIN, StringEscapeUtils.escapeJava(encodedLogin), rememberMeExpiration);

            UserSession session = connection.getSession();
            if (session == null) {
                throw new IllegalStateException("Unable to get session after login");
            }
            User user = session.getUser();
            String rememberMeToken = userManagementService.generateRememberMeToken(user.getId());
            app.addCookie(COOKIE_PASSWORD, rememberMeToken, rememberMeExpiration);
        } else {
            resetRememberCookies();
        }
    }

    /**
     * Clears cookies.
     */
    public void resetRememberCookies() {
        app.removeCookie(COOKIE_REMEMBER_ME);
        app.removeCookie(COOKIE_LOGIN);
        app.removeCookie(COOKIE_PASSWORD);
    }
}
