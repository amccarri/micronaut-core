/*
 * Copyright 2017-2018 original authors
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

package io.micronaut.dbmigration.flyway;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Collection;

/**
 * Synchronous listener for {@link StartupEvent} to run flyway operations.
 *
 * @author Iván López
 * @since 1.1
 */
@Requires(classes = Flyway.class)
@Requires(property = "flyway.enabled", notEquals = "false")
@Singleton
class FlywayStartupEventListener extends AbstractFlyway {
    private static final Logger LOG = LoggerFactory.getLogger(FlywayStartupEventListener.class);

    /**
     * @param flywayConfigurationProperties Collection of Flyway Configurations
     */
    public FlywayStartupEventListener(Collection<FlywayConfigurationProperties> flywayConfigurationProperties) {
        super(flywayConfigurationProperties);
    }

    /**
     * Runs Flyway for the datasource where there is a flyway configuration available.
     *
     * @param event Server startup event
     */
    @EventListener
    public void onStartup(StartupEvent event) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Executing flyway event listener");
        }
        run(false);
    }
}
