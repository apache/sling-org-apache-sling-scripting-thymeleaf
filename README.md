[![Apache Sling](https://sling.apache.org/res/logos/sling.png)](https://sling.apache.org)

&#32;[![Build Status](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-scripting-thymeleaf/job/master/badge/icon)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-scripting-thymeleaf/job/master/)&#32;[![Test Status](https://img.shields.io/jenkins/tests.svg?jobUrl=https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-scripting-thymeleaf/job/master/)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-scripting-thymeleaf/job/master/test/?width=800&height=600)&#32;[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-scripting-thymeleaf&metric=coverage)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-scripting-thymeleaf)&#32;[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-scripting-thymeleaf&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-scripting-thymeleaf)&#32;[![JavaDoc](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.scripting.thymeleaf.svg)](https://www.javadoc.io/doc/org.apache.sling/org.apache.sling.scripting.thymeleaf)&#32;[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.scripting.thymeleaf/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.scripting.thymeleaf%22)&#32;[![Contrib](https://sling.apache.org/badges/status-contrib.svg)](https://github.com/apache/sling-aggregator/blob/master/docs/status/contrib.md)&#32;[![scripting](https://sling.apache.org/badges/group-scripting.svg)](https://github.com/apache/sling-aggregator/blob/master/docs/groups/scripting.md) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Apache Sling Scripting Thymeleaf

This module is part of the [Apache Sling](https://sling.apache.org) project.

This module provides a scripting engine for [Thymeleaf](http://www.thymeleaf.org) templates.

## Features

* Supporting all of Thymeleaf's extension points: [_TemplateResolver_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/templateresolver/ITemplateResolver.html)﻿s, [_MessageResolver_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/messageresolver/IMessageResolver.html)﻿s, [_Dialect_﻿](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/dialect/IDialect.html)s, [_LinkBuilder_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/linkbuilder/ILinkBuilder.html)﻿s, [_DecoupledTemplateLogicResolver_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/templateparser/markup/decoupled/IDecoupledTemplateLogicResolver.html), [_CacheManager_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/cache/ICacheManager.html) and [_EngineContextFactory_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/context/IEngineContext.html)
* `SlingResourceTemplateResolver` customizable through `TemplateModeProvider`﻿
* `ResourceBundleMessageResolver` backed by `ResourceBundleProvider` from [Sling i18n](https://sling.apache.org/documentation/bundles/internationalization-support-i18n.html) customizable through optional `AbsentMessageRepresentationProvider`﻿
* `PatternTemplateModeProvider` supporting [`Pattern`](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html) configurations for all [template modes](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/templatemode/TemplateMode.html) (`HTML`, `XML`, `TEXT`, `JAVASCRIPT`, `CSS` and `RAW`)
* `SlingDialect`
* Thymeleaf's [`TemplateEngine`](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/ITemplateEngine.html) registered as OSGi Service ([`ITemplateEngine`](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/ITemplateEngine.html)) for direct use

## Installation

For running Sling Scripting Thymeleaf with Sling Starter some dependencies need to be resolved. This can be achieved by installing the following bundles:

    mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.thymeleaf/3.0.9.RELEASE_1
    mvn:org.attoparser/attoparser/2.0.5.RELEASE
    mvn:org.unbescape/unbescape/1.1.6.RELEASE
    mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.ognl/3.2.1_1
    mvn:org.javassist/javassist/3.23.1-GA

There is a feature for [Karaf](https://github.com/apache/sling/tree/trunk/contrib/launchpad/karaf):

    karaf@root()> feature:install sling-scripting-thymeleaf

**Note:** Sling Scripting Thymeleaf requires an implementation of OSGi Declarative Services 1.3 (e.g. [Apache Felix Service Component Runtime](http://felix.apache.org/documentation/subprojects/apache-felix-service-component-runtime.html) 2.0.0 or greater)
