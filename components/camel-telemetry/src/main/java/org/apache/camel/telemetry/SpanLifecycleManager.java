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
package org.apache.camel.telemetry;

/**
 * This interface is used to manage the lifecycle of a Span.
 */
public interface SpanLifecycleManager {

    Span create(String spanName, Span parent, SpanContextPropagationExtractor extractor);

    void activate(Span span);

    void deactivate(Span span);

    void close(Span span);

    void inject(Span span, SpanContextPropagationInjector injector);

}
