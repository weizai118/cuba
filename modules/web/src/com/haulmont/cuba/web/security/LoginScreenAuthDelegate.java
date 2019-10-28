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

import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.security.auth.AbstractClientCredentials;
import com.haulmont.cuba.security.auth.Credentials;
import com.haulmont.cuba.security.auth.LoginPasswordCredentials;
import com.haulmont.cuba.security.global.InternalAuthenticationException;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.Connection;
import com.haulmont.cuba.web.app.login.LoginScreen;
import com.haulmont.cuba.web.app.loginwindow.AppLoginWindow;
import com.haulmont.cuba.web.sys.VaadinSessionScope;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Is intended to use from {@link LoginScreen}, {@link AppLoginWindow} and provides performing log in.
 */
@Component(LoginScreenAuthDelegate.NAME)
@Scope(VaadinSessionScope.NAME)
public class LoginScreenAuthDelegate {
    public static final String NAME = "cuba_LoginScreenAuthDelegate";

    protected App app;
    protected Connection connection;

    protected GlobalConfig globalConfig;

    @Inject
    protected void setApp(App app) {
        this.app = app;
    }

    @Inject
    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Inject
    protected void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    /**
     * Performs log in.
     *
     * @param login                  user login
     * @param password               user password
     * @param selectedLocale         selected locale
     * @param isLocalesSelectVisible is locales select visible
     * @throws InternalAuthenticationException
     * @throws LoginException
     * @throws Exception
     */
    public void doLogin(String login, String password, Locale selectedLocale, boolean isLocalesSelectVisible)
            throws InternalAuthenticationException, LoginException, Exception {
        password = password != null ? password : "";

        app.setLocale(selectedLocale);

        doLogin(new LoginPasswordCredentials(login, password, selectedLocale), isLocalesSelectVisible);

        // locale could be set on the server
        if (connection.getSession() != null) {
            Locale loggedInLocale = connection.getSession().getLocale();

            if (globalConfig.getLocaleSelectVisible()) {
                app.addCookie(App.COOKIE_LOCALE, loggedInLocale.toLanguageTag());
            }
        }
    }

    /**
     * Performs log in with credentials.
     *
     * @param credentials            user credentials
     * @param isLocalesSelectVisible is locales select visible
     * @throws LoginException
     */
    public void doLogin(Credentials credentials, boolean isLocalesSelectVisible) throws LoginException {
        if (credentials instanceof AbstractClientCredentials) {
            ((AbstractClientCredentials) credentials).setOverrideLocale(isLocalesSelectVisible);
        }
        connection.login(credentials);
    }

    /**
     * Contains user's auth information. Is used in login screens for saving state and initializing auth value.
     */
    public static class AuthInfo {

        protected final String login;
        protected final String password;
        protected final Boolean rememberMe;

        public AuthInfo(String login, String password, Boolean rememberMe) {
            this.login = login;
            this.password = password;
            this.rememberMe = rememberMe;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public Boolean getRememberMe() {
            return rememberMe;
        }
    }
}
