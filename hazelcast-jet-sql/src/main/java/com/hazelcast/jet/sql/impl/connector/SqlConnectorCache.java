/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.sql.impl.connector;

import com.hazelcast.core.HazelcastException;
import com.hazelcast.internal.util.ServiceLoader;
import com.hazelcast.jet.sql.SqlConnector;
import com.hazelcast.spi.impl.NodeEngine;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class SqlConnectorCache {

    private static final String FACTORY_ID = "com.hazelcast.sql.Connectors";

    private final Map<String, SqlConnector> connectors = new HashMap<>();

    public SqlConnectorCache(NodeEngine nodeEngine) {
        try {
            ServiceLoader.iterator(SqlConnector.class, FACTORY_ID, nodeEngine.getConfigClassLoader())
                         .forEachRemaining(this::addConnector);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addConnector(SqlConnector connector) {
        if (connectors.putIfAbsent(connector.typeName().toUpperCase(Locale.ENGLISH), connector) != null) {
            throw new HazelcastException("Duplicate connector: " + connector.typeName());
        }
    }

    public SqlConnector forType(String type) {
        type = type.toUpperCase(Locale.ENGLISH);
        return Objects.requireNonNull(connectors.get(type), "Unknown type: " + type);
    }
}
