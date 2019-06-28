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

package com.haulmont.cuba.gui.components.validation;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.datatypes.DatatypeRegistry;
import com.haulmont.cuba.core.global.BeanLocator;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.ValidationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * GroovyScript validator runs a custom Groovy script. If the script returns {@code false},
 * then {@link ValidationException} is thrown.
 * <p>
 * For error message it uses Groovy string and it is possible to use '$value' key for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *   &lt;bean id="cuba_GroovyScriptValidator" class="com.haulmont.cuba.gui.components.validation.GroovyScriptValidator" scope="prototype"/&gt;
 *   </pre>
 * Use {@link BeanLocator} when creating the validator programmatically.
 *
 * @param <T> any Object
 */
@Component(GroovyScriptValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroovyScriptValidator<T> extends AbstractValidator<T> {

    public static final String NAME = "cuba_GroovyScriptValidator";

    @Inject
    protected Scripting scripting;

    protected String validatorGroovyScript;

    @Inject
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Inject
    protected void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Inject
    protected void setUserSessionSource(UserSessionSource userSessionSource) {
        this.userSessionSource = userSessionSource;
    }

    /**
     * Constructor with default error message.
     *
     * @param validatorGroovyScript groovy script with {V} macro
     */
    public GroovyScriptValidator(String validatorGroovyScript) {
        this.validatorGroovyScript = validatorGroovyScript;
    }

    /**
     * Constructor with custom error message. This message can contain '$value' key for formatted output.
     * <p>
     * Example: "Value '$value' is incorrect".
     *
     * @param validatorGroovyScript groovy script with {V} macro
     * @param message error message
     */
    public GroovyScriptValidator(String validatorGroovyScript, String message) {
        this.validatorGroovyScript = validatorGroovyScript;
        this.message = message;
    }

    @Override
    public void accept(T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        Map<String, Object> context = new HashMap<>();
        context.put("__value__", value);

        Object scriptResult = scripting.evaluateGroovy(validatorGroovyScript.replace("{V}", "__value__"), context);

        if (Boolean.FALSE.equals(scriptResult)) {
            fireValidationException(value);
        }
    }

    protected String getDefaultMessage() {
        return messages.getMainMessage("validation.constraints.customGroovyScript");
    }

    protected void fireValidationException(T value) {
        String message = getMessage();

        String formattedValue = formatValue(value);

        String formattedMessage = getTemplateErrorMessage(
                message == null ? getDefaultMessage() : message,
                ParamsMap.of("value", formattedValue));

        throw new ValidationException(formattedMessage);
    }
}
