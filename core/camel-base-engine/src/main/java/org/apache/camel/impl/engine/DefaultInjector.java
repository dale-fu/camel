/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl.engine;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.spi.CamelBeanPostProcessor;
import org.apache.camel.spi.Injector;
import org.apache.camel.support.ObjectHelper;
import org.apache.camel.support.PluginHelper;

/**
 * A default implementation of {@link Injector} which just uses reflection to instantiate new objects using their zero
 * argument constructor, and then performing bean post processing using {@link CamelBeanPostProcessor}.
 */
public class DefaultInjector implements Injector {

    // use the reflection injector
    private final CamelContext camelContext;
    private final CamelBeanPostProcessor postProcessor;

    public DefaultInjector(CamelContext context) {
        this.camelContext = context;
        this.postProcessor = PluginHelper.getBeanPostProcessor(camelContext);
    }

    @Override
    public <T> T newInstance(Class<T> type) {
        return newInstance(type, true);
    }

    @Override
    public <T> T newInstance(Class<T> type, String factoryMethod) {
        return newInstance(type, null, factoryMethod);
    }

    @Override
    public <T> T newInstance(Class<T> type, Class<?> factoryClass, String factoryMethod) {
        Class<?> target = factoryClass != null ? factoryClass : type;
        T answer = null;
        try {
            // lookup factory method
            Method fm = target.getMethod(factoryMethod);
            if (Modifier.isStatic(fm.getModifiers()) && Modifier.isPublic(fm.getModifiers())
                    && fm.getReturnType() != Void.class) {
                Object obj = fm.invoke(null);
                answer = type.cast(obj);
            }
            // inject camel context if needed
            CamelContextAware.trySetCamelContext(answer, camelContext);
        } catch (Exception e) {
            throw new RuntimeCamelException("Error invoking factory method: " + factoryMethod + " on class: " + target, e);
        }
        return answer;
    }

    @Override
    public <T> T newInstance(Class<T> type, boolean postProcessBean) {
        T answer = ObjectHelper.newInstance(type);
        // inject camel context if needed
        CamelContextAware.trySetCamelContext(answer, camelContext);
        if (postProcessBean) {
            try {
                postProcessor.postProcessBeforeInitialization(answer, answer.getClass().getName());
                postProcessor.postProcessAfterInitialization(answer, answer.getClass().getName());
            } catch (Exception e) {
                throw new RuntimeCamelException("Error during post processing of bean: " + answer, e);
            }
        }
        return answer;
    }

    @Override
    public boolean supportsAutoWiring() {
        return false;
    }
}
