/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.scripting.thymeleaf.it.tests;

import javax.inject.Inject;
import javax.script.ScriptEngineFactory;

import org.apache.sling.api.servlets.ServletResolver;
import org.apache.sling.auth.core.AuthenticationSupport;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.resource.presence.ResourcePresence;
import org.apache.sling.scripting.thymeleaf.it.app.Activator;
import org.apache.sling.testing.paxexam.TestSupport;
import org.jetbrains.annotations.NotNull;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.Constants;
import org.osgi.service.http.HttpService;
import org.thymeleaf.ITemplateEngine;

import static org.apache.sling.testing.paxexam.SlingOptions.slingI18n;
import static org.apache.sling.testing.paxexam.SlingOptions.slingInstallerProviderJcr;
import static org.apache.sling.testing.paxexam.SlingOptions.slingModels;
import static org.apache.sling.testing.paxexam.SlingOptions.slingQuickstartOakTar;
import static org.apache.sling.testing.paxexam.SlingOptions.slingResourcePresence;
import static org.apache.sling.testing.paxexam.SlingOptions.slingScripting;
import static org.apache.sling.testing.paxexam.SlingOptions.slingScriptingJsp;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.factoryConfiguration;

public abstract class ThymeleafTestSupport extends TestSupport {

    @Inject
    protected ServletResolver servletResolver;

    @Inject
    protected SlingRequestProcessor slingRequestProcessor;

    @Inject
    protected AuthenticationSupport authenticationSupport;

    @Inject
    protected HttpService httpService;

    @Inject
    @Filter(value = "(names=thymeleaf)")
    protected ScriptEngineFactory scriptEngineFactory;

    @Inject
    protected ITemplateEngine templateEngine;

    @Inject
    @Filter(value = "(path=/apps/thymeleaf)")
    private ResourcePresence resourcePresence;

    @Configuration
    public Option[] configuration() {
        return options(
            baseConfiguration(),
            quickstart(),
            // Sling Scripting Thymeleaf
            testBundle("bundle.filename"),
            mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.thymeleaf").versionAsInProject(),
            mavenBundle().groupId("org.attoparser").artifactId("attoparser").versionAsInProject(),
            mavenBundle().groupId("org.unbescape").artifactId("unbescape").versionAsInProject(),
            mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.ognl").versionAsInProject(),
            mavenBundle().groupId("org.javassist").artifactId("javassist").versionAsInProject(),
            // testing
            slingResourcePresence(),
            factoryConfiguration("org.apache.sling.jcr.repoinit.RepositoryInitializer")
                .put("scripts", new String[]{"create path (sling:OrderedFolder) /content/thymeleaf\nset ACL for everyone\nallow jcr:read on /content/thymeleaf\nend"})
                .asOption(),
            factoryConfiguration("org.apache.sling.resource.presence.internal.ResourcePresenter")
                .put("path", "/apps/thymeleaf")
                .asOption(),
            mavenBundle().groupId("org.jsoup").artifactId("jsoup").versionAsInProject()
        );
    }

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(@NotNull final TestProbeBuilder testProbeBuilder) {
        testProbeBuilder.setHeader(Constants.BUNDLE_ACTIVATOR, Activator.class.getName());
        testProbeBuilder.setHeader(Constants.BUNDLE_SYMBOLICNAME, "org.apache.sling.scripting.thymeleaf.it.app");
        testProbeBuilder.setHeader(Constants.EXPORT_PACKAGE, "org.apache.sling.scripting.thymeleaf.it.app");
        testProbeBuilder.setHeader("Sling-Model-Packages", "org.apache.sling.scripting.thymeleaf.it.app");
        testProbeBuilder.setHeader("Sling-Initial-Content", "initial-content");
        return testProbeBuilder;
    }

    protected Option quickstart() {
        final int httpPort = findFreePort();
        final String workingDirectory = workingDirectory();
        return composite(
            slingQuickstartOakTar(workingDirectory, httpPort),
            slingI18n(),
            slingModels(),
            slingInstallerProviderJcr(),
            slingScripting(),
            slingScriptingJsp()
        );
    }

}
