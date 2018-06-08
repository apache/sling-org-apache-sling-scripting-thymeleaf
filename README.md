[<img src="http://sling.apache.org/res/logos/sling.png"/>](http://sling.apache.org)

 [![Build Status](https://builds.apache.org/buildStatus/icon?job=sling-org-apache-sling-scripting-thymeleaf-1.8)](https://builds.apache.org/view/S-Z/view/Sling/job/sling-org-apache-sling-scripting-thymeleaf-1.8) [![Test Status](https://img.shields.io/jenkins/t/https/builds.apache.org/view/S-Z/view/Sling/job/sling-org-apache-sling-scripting-thymeleaf-1.8.svg)](https://builds.apache.org/view/S-Z/view/Sling/job/sling-org-apache-sling-scripting-thymeleaf-1.8/test_results_analyzer/) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.scripting.thymeleaf/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.scripting.thymeleaf%22) [![JavaDocs](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.scripting.thymeleaf.svg)](https://www.javadoc.io/doc/org.apache.sling/org.apache.sling.scripting.thymeleaf) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0) [![scripting](https://sling.apache.org/badges/group-scripting.svg)](https://github.com/apache/sling-aggregator/blob/master/docs/groups/scripting.md)&#32;[![contrib](http://sling.apache.org/badges/status-contrib.svg)](https://github.com/apache/sling-aggregator/blob/master/docs/status/contrib.md)

# Apache Sling Scripting Thymeleaf

This module is part of the [Apache Sling](https://sling.apache.org) project.

scripting engine for [_Thymeleaf_](http://www.thymeleaf.org) templates

## Features

* Supporting all of Thymeleaf's extension points: [_TemplateResolver_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/templateresolver/ITemplateResolver.html)﻿s, [_MessageResolver_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/messageresolver/IMessageResolver.html)﻿s, [_Dialect_﻿](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/dialect/IDialect.html)s, [_LinkBuilder_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/linkbuilder/ILinkBuilder.html)﻿s, [_DecoupledTemplateLogicResolver_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/templateparser/markup/decoupled/IDecoupledTemplateLogicResolver.html), [_CacheManager_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/cache/ICacheManager.html) and [_EngineContextFactory_](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/context/IEngineContext.html)
* `SlingResourceTemplateResolver` customizable through `TemplateModeProvider`﻿
* `ResourceBundleMessageResolver` backed by `ResourceBundleProvider` from [Sling i18n](https://sling.apache.org/documentation/bundles/internationalization-support-i18n.html) customizable through optional `AbsentMessageRepresentationProvider`﻿
* `PatternTemplateModeProvider` supporting [`Pattern`](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html) configurations for all [template modes](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/templatemode/TemplateMode.html) (`HTML`, `XML`, `TEXT`, `JAVASCRIPT`, `CSS` and `RAW`)
* `SlingDialect`
* Thymeleaf's [`TemplateEngine`](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/ITemplateEngine.html) registered as OSGi Service ([`ITemplateEngine`](http://www.thymeleaf.org/apidocs/thymeleaf/3.0.0.RELEASE/org/thymeleaf/ITemplateEngine.html)) for direct use

## Installation

For running Sling Scripting Thymeleaf with Sling's Launchpad some dependencies need to be resolved. This can be achieved by installing the following bundles:

    mvn:org.attoparser/attoparser/2.0.2.RELEASE
    mvn:org.unbescape/unbescape/1.1.4.RELEASE
    mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.ognl/3.2_1
    mvn:org.javassist/javassist/3.20.0-GA

There is a feature for [Karaf](https://github.com/apache/sling/tree/trunk/contrib/launchpad/karaf):

    karaf@root()> feature:install sling-scripting-thymeleaf

**Note:** Sling Scripting Thymeleaf requires an implementation of OSGi Declarative Services 1.3 (e.g. [Apache Felix Service Component Runtime](http://felix.apache.org/documentation/subprojects/apache-felix-service-component-runtime.html) 2.0.0 or greater)

## Relevant Thymeleaf issues

* [Create OSGi bundle](https://github.com/thymeleaf/thymeleaf/issues/32)
* [keep (custom) IContext accessible](https://github.com/thymeleaf/thymeleaf/issues/388)
* [Make IContext available in ITemplateResolver](https://github.com/thymeleaf/thymeleaf/issues/514)
