/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.engine;

import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.cache.AlwaysValidCacheEntryValidity;
import org.thymeleaf.cache.ICache;
import org.thymeleaf.cache.ICacheEntryValidity;
import org.thymeleaf.cache.ICacheManager;
import org.thymeleaf.cache.NonCacheableCacheEntryValidity;
import org.thymeleaf.cache.TemplateCacheKey;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.preprocessor.IPreProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateparser.ITemplateParser;
import org.thymeleaf.templateparser.markup.HTMLTemplateParser;
import org.thymeleaf.templateparser.markup.XMLTemplateParser;
import org.thymeleaf.templateparser.raw.RawTemplateParser;
import org.thymeleaf.templateparser.text.CSSTemplateParser;
import org.thymeleaf.templateparser.text.JavaScriptTemplateParser;
import org.thymeleaf.templateparser.text.TextTemplateParser;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.util.LoggingUtils;
import org.thymeleaf.util.Validate;


/**
 *
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 3.0.0
 *
 */
public final class TemplateManager {

    private static final Logger logger = LoggerFactory.getLogger(TemplateManager.class);

    private static final int DEFAULT_PARSER_POOL_SIZE = 40;
    private static final int DEFAULT_PARSER_BLOCK_SIZE = 2048;

    private final IEngineConfiguration configuration;

    private final ITemplateParser htmlParser;
    private final ITemplateParser xmlParser;
    private final ITemplateParser textParser;
    private final ITemplateParser javascriptParser;
    private final ITemplateParser cssParser;
    private final ITemplateParser rawParser;


    private final ICache<TemplateCacheKey,TemplateModel> templateCache; // might be null! (= no cache)




    /**
     * <p>
     *   This constructor should only be called directly for <strong>testing purposes</strong>.
     * </p>
     *
     * @param configuration the engine configuration
     */
    public TemplateManager(final IEngineConfiguration configuration) {
        
        super();

        Validate.notNull(configuration, "Configuration cannot be null");

        this.configuration = configuration;

        final ICacheManager cacheManager = this.configuration.getCacheManager();

        if (cacheManager == null) {
            this.templateCache = null;
        } else {
            this.templateCache = cacheManager.getTemplateCache();
        }

        final boolean standardDialectPresent = this.configuration.isStandardDialectPresent();
        final String standardDialectPrefix = this.configuration.getStandardDialectPrefix();

        // TODO Make these parser implementations configurable: one parser per template mode, then make default implementations extensible/configurable (e.g. AttoParser config)
        this.htmlParser = new HTMLTemplateParser(DEFAULT_PARSER_POOL_SIZE,DEFAULT_PARSER_BLOCK_SIZE);
        this.xmlParser = new XMLTemplateParser(DEFAULT_PARSER_POOL_SIZE, DEFAULT_PARSER_BLOCK_SIZE);
        this.textParser = new TextTemplateParser(DEFAULT_PARSER_POOL_SIZE, DEFAULT_PARSER_BLOCK_SIZE, standardDialectPresent, standardDialectPrefix);
        this.javascriptParser = new JavaScriptTemplateParser(DEFAULT_PARSER_POOL_SIZE, DEFAULT_PARSER_BLOCK_SIZE, standardDialectPresent, standardDialectPrefix);
        this.cssParser = new CSSTemplateParser(DEFAULT_PARSER_POOL_SIZE, DEFAULT_PARSER_BLOCK_SIZE, standardDialectPresent, standardDialectPrefix);
        this.rawParser = new RawTemplateParser(DEFAULT_PARSER_POOL_SIZE, DEFAULT_PARSER_BLOCK_SIZE);

    }
    

    
    
    
    /**
     * <p>
     *   Clears the template cache.
     * </p>
     */
    public void clearCaches() {
        if (this.templateCache != null) {
            this.templateCache.clear();
        }
    }

    
    /**
     * <p>
     *   Clears any existing entries for template of the specified
     *   name at the template cache.
     * </p>
     * 
     * @param template the name of the template whose entries have to be cleared.
     */
    public void clearCachesFor(final String template) {
        Validate.notNull(template, "Cannot specify null template");
        if (this.templateCache != null) {
            final Set<TemplateCacheKey> keysToBeRemoved = new HashSet<TemplateCacheKey>(4);
            final Set<TemplateCacheKey> templateCacheKeys = this.templateCache.keySet();
            // We are iterating twice and creating a temporary set just in case the 'keySet' Set is still connected
            // to the original cache store and we provoke ConcurrentModificationExceptions when removing entries
            for (final TemplateCacheKey templateCacheKey : templateCacheKeys) {
                final String ownerTemplate = templateCacheKey.getOwnerTemplate();
                if (ownerTemplate != null) {
                    // It's not a standalone template, so we are interested on the owner template
                    if (ownerTemplate.equals(template)) {
                        keysToBeRemoved.add(templateCacheKey);
                    }
                } else {
                    if (templateCacheKey.getTemplate().equals(template)) {
                        keysToBeRemoved.add(templateCacheKey);
                    }
                }
            }
            for (final TemplateCacheKey keyToBeRemoved : keysToBeRemoved) {
                this.templateCache.clearKey(keyToBeRemoved);
            }
        }
    }






    /*
     * -------------
     * PARSE methods
     * -------------
     *
     * Parse methods will create 'template models' that are basically collections of events in the form of an
     * immutable IModel implementation.
     */


    public TemplateModel parseStandalone(
            final ITemplateContext context, final String template, final Set<String> templateSelectors,
            final TemplateMode templateMode, final boolean useCache) {

        Validate.notNull(context, "Context cannot be null");
        Validate.notNull(template, "Template cannot be null");
        // templateSelectors CAN be null if we are going to render the entire template
        // templateMode CAN be null if we are going to use the mode specified by the template resolver
        // templateResolutionAttributes CAN be null


        final String ownerTemplate = context.getTemplateData().getTemplate();
        final Map<String,Object> templateResolutionAttributes = context.getTemplateResolutionAttributes();

        final Set<String> cleanTemplateSelectors;
        if (templateSelectors != null && !templateSelectors.isEmpty()) {
            Validate.containsNoEmpties(
                    templateSelectors, "If specified, the Template Selector set cannot contain any nulls or empties");
            if (templateSelectors.size() == 1) {
                cleanTemplateSelectors = Collections.singleton(templateSelectors.iterator().next());
            } else {
                // We will be using a TreeSet because we want the selectors to be ORDERED, so that comparison at the
                // equals(...) method works alright
                cleanTemplateSelectors = Collections.unmodifiableSet(new TreeSet<String>(templateSelectors));
            }
        } else {
            cleanTemplateSelectors = null;
        }


        final TemplateCacheKey cacheKey =
                useCache?
                        new TemplateCacheKey(
                                ownerTemplate,
                                template, cleanTemplateSelectors,
                                0, 0,
                                templateMode,
                                templateResolutionAttributes)
                        : null;

        /*
         * First look at the cache - it might be already cached
         */
        if (useCache && this.templateCache != null) {
            final TemplateModel cached =  this.templateCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }


        /*
         * Resolve the template
         */
        final TemplateResolution templateResolution =
                resolveTemplate(this.configuration, context, ownerTemplate, template, templateResolutionAttributes);


        /*
         * Build the TemplateData object
         */
        final TemplateData templateData =
                buildTemplateData(templateResolution, template, cleanTemplateSelectors, templateMode, useCache);


        /*
         * Build the TemplateModel that we will end up returning
         */
        final TemplateModel templateModel = new TemplateModel(this.configuration, templateData);


        /*
         *  Create the Template Handler that will be in charge of building the TemplateModel
         */
        final ModelBuilderTemplateHandler builderHandler = new ModelBuilderTemplateHandler(templateModel.getInternalModel());


        /*
         * PROCESS THE TEMPLATE
         */
        final ITemplateParser parser = getParserForTemplateMode(templateData.getTemplateMode());
        parser.parseStandalone(
                this.configuration,
                ownerTemplate, template, cleanTemplateSelectors, templateData.getTemplateResource(),
                templateData.getTemplateMode(), builderHandler);


        /*
         * Cache the template if it is cacheable
         */
        if (useCache && this.templateCache != null) {
            if (templateResolution.getValidity().isCacheable()) {
                this.templateCache.put(cacheKey, templateModel);
            }
        }

        return templateModel;



    }




    public TemplateModel parseString(
            final TemplateData ownerTemplateData, final String template,
            final int lineOffset, final int colOffset,
            final TemplateMode templateMode,
            final boolean useCache) {

        Validate.notNull(ownerTemplateData, "Owner template cannot be null");
        Validate.notNull(template, "Template cannot be null");
        // NOTE selectors cannot be specified when parsing a nested template
        // templateMode CAN be null (if we are using the owner's)

        final String ownerTemplate = ownerTemplateData.getTemplate();

        final TemplateMode definitiveTemplateMode =
                (templateMode != null? templateMode : ownerTemplateData.getTemplateMode());


        final TemplateCacheKey cacheKey =
                useCache?
                        new TemplateCacheKey(
                                ownerTemplate,
                                template, null,
                                lineOffset, colOffset,
                                definitiveTemplateMode,
                                null) // template resolution attributes do not affect string fragments: no resolution!
                        : null;

        /*
         * First look at the cache - it might be already cached
         */
        if (useCache && this.templateCache != null) {
            final TemplateModel cached =  this.templateCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }


        /*
         * Compute the cache validity. In order for a String fragment to be cacheable, we will have to have
         * specified the 'useCache' parameter as true, and the owner template must be cacheable
         */
        final ICacheEntryValidity cacheValidity =
                (useCache && ownerTemplateData.getValidity().isCacheable()?
                        AlwaysValidCacheEntryValidity.INSTANCE : NonCacheableCacheEntryValidity.INSTANCE);


        /*
         * Build the TemplateData
         *
         * NOTE how, by default, we are using the owner's TemplateData. And even if the template mode changes
         * and we need to create a new TemplateData object, we will keep the original name and resource.
         * This is because we want the elements inside the fragment to me reported as belonging to the
         * container template, not to the fragment String considered as a fragment in its own (which
         * wouldn't make sense)
         */
        final TemplateData templateData =
                (templateMode == null?
                        // No change in Template Mode -> simply use the owner's template data
                        ownerTemplateData :
                        // Template Mode changed -> new TemplateData, very similar but different template mode
                        new TemplateData(
                                ownerTemplateData.getTemplate(), ownerTemplateData.getTemplateSelectors(),
                                ownerTemplateData.getTemplateResource(), templateMode, cacheValidity));


        /*
         * Build the TemplateModel
         *
         * NOTE how we are using the owner's TemplateData and not a new one created for this fragment, because
         * we want the elements inside the fragment to me reported as belonging to the container template,
         * not to the fragment String considered as a fragment in its own (which wouldn't make sense)
         */
        final TemplateModel parsedTemplate = new TemplateModel(this.configuration, templateData);


        /*
         *  Create the Template Handler that will be in charge of building the TemplateModel
         */
        final ModelBuilderTemplateHandler builderHandler = new ModelBuilderTemplateHandler(parsedTemplate.getInternalModel());


        /*
         * PROCESS THE TEMPLATE
         */
        final ITemplateParser parser = getParserForTemplateMode(templateData.getTemplateMode());
        // NO RESOURCE is sent to the parser, in this case. We simply pass the String template
        parser.parseString(this.configuration, ownerTemplate, template, lineOffset, colOffset, definitiveTemplateMode, builderHandler);


        /*
         * Cache the template if it is cacheable
         */
        if (useCache && this.templateCache != null) {
            if (cacheValidity.isCacheable()) {
                this.templateCache.put(cacheKey, parsedTemplate);
            }
        }
        
        return parsedTemplate;
        
    }






    /*
     * ---------------
     * PROCESS methods
     * ---------------
     *
     * Processing means executing a template that has already been parsed into a TemplateModel object
     */


    public void process(
            final TemplateModel template,
            final ITemplateContext context,
            final Writer writer) {

        Validate.isTrue(
                this.configuration == template.getConfiguration(),
                "Specified template was built by a different Template Engine instance");

        /*
         * Create the context instance that corresponds to this execution of the template engine
         */
        final IEngineContext engineContext =
                EngineContextManager.prepareEngineContext(this.configuration, template.getTemplateData(), context.getTemplateResolutionAttributes(), context);

        /*
         * Create the handler chain to process the data
         */
        final ITemplateHandler processingHandlerChain = createTemplateProcessingHandlerChain(engineContext, writer);

        /*
         *  Process the template
         */
        template.getInternalModel().process(processingHandlerChain);


        /*
         * Dispose the engine context now that processing has been done
         */
        EngineContextManager.disposeEngineContext(engineContext);

    }






    /*
     * -------------------------
     * PARSE-AND-PROCESS methods
     * -------------------------
     *
     * These methods perform the whole cycle of a template's processing: resolving, parsing and processing.
     * This is only meant to be called from the TemplateEngine
     */


    public void parseAndProcess(
            final TemplateSpec templateSpec,
            final IContext context,
            final Writer writer) {

        Validate.notNull(templateSpec, "Template Specification cannot be null");
        Validate.notNull(context, "Context cannot be null");
        Validate.notNull(writer, "Writer cannot be null");


        // TemplateSpec will already have validated its contents, so need to do it here (template selectors,
        // resolution attributes, etc.)

        final String template = templateSpec.getTemplate();
        final Set<String> templateSelectors = templateSpec.getTemplateSelectors();
        final TemplateMode templateMode = templateSpec.getTemplateMode();
        final Map<String, Object> templateResolutionAttributes = templateSpec.getTemplateResolutionAttributes();

        final TemplateCacheKey cacheKey =
                    new TemplateCacheKey(
                            null, // ownerTemplate
                            template, templateSelectors,
                            0, 0, // lineOffset, colOffset
                            templateMode,
                            templateResolutionAttributes);


        /*
         * First look at the cache - it might be already cached
         */
        if (this.templateCache != null) {

            final TemplateModel cached =  this.templateCache.get(cacheKey);

            if (cached != null) {

                final IEngineContext engineContext =
                        EngineContextManager.prepareEngineContext(this.configuration, cached.getTemplateData(), templateResolutionAttributes, context);

                final ITemplateHandler processingHandlerChain = createTemplateProcessingHandlerChain(engineContext, writer);

                cached.getInternalModel().process(processingHandlerChain);

                EngineContextManager.disposeEngineContext(engineContext);

                return;

            }

        }


        /*
         * Resolve the template
         */
        final TemplateResolution templateResolution =
                resolveTemplate(this.configuration, context, null, template, templateResolutionAttributes);


        /*
         * Build the TemplateData object
         */
        final TemplateData templateData =
                buildTemplateData(templateResolution, template, templateSelectors, templateMode, true);


        /*
         * Prepare the context instance that corresponds to this execution of the template engine
         */
        final IEngineContext engineContext =
                EngineContextManager.prepareEngineContext(this.configuration, templateData, templateResolutionAttributes, context);


        /*
         * Create the handler chain to process the data
         */
        final ITemplateHandler processingHandlerChain = createTemplateProcessingHandlerChain(engineContext, writer);


        /*
         * Obtain the parser
         */
        final ITemplateParser parser = getParserForTemplateMode(engineContext.getTemplateMode());


        /*
         * If the resolved template is cacheable, so we will first read it as an object, cache it, and then process it
         */
        if (templateResolution.getValidity().isCacheable() && this.templateCache != null) {

            // Build the TemplateModel
            final TemplateModel templateModel = new TemplateModel(this.configuration, templateData);

            // Create the handler chain to create the Template object
            final ModelBuilderTemplateHandler builderHandler = new ModelBuilderTemplateHandler(templateModel.getInternalModel());

            // Process the cached template itself
            parser.parseStandalone(
                    this.configuration,
                    null, template, templateSelectors, templateData.getTemplateResource(),
                    engineContext.getTemplateMode(), builderHandler);

            // Put the new template into cache
            this.templateCache.put(cacheKey, templateModel);

            // Process the read (+cached) template itself
            templateModel.getInternalModel().process(processingHandlerChain);

        } else {

            //  Process the template, which is not cacheable (so no worry about caching)
            parser.parseStandalone(
                    this.configuration,
                    null, template, templateSelectors, templateData.getTemplateResource(),
                    engineContext.getTemplateMode(), processingHandlerChain);

        }


        /*
         * Dispose the engine context now that processing has been done
         */
        EngineContextManager.disposeEngineContext(engineContext);


    }






    private static TemplateResolution resolveTemplate(
            final IEngineConfiguration configuration,
            final IContext context,
            final String ownerTemplate,
            final String template,
            final Map<String, Object> templateResolutionAttributes) {

        // Note that the MARKUP SELECTORS that might be used for a executing or inserting a template
        // are not specified to the template resolver. The reason is markup selectors are applied by the parser,
        // not the template resolvers, and allowing the resolver to take any decisions based on markup selectors
        // (like e.g. omitting some output from the resource) could harm the correctness of the selection operation
        // performed by the parser.

        for (final ITemplateResolver templateResolver : configuration.getTemplateResolvers()) {

            final TemplateResolution templateResolution =
                    templateResolver.resolveTemplate(configuration, context, ownerTemplate, template, templateResolutionAttributes);
            if (templateResolution != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "[THYMELEAF][{}] Template resolver match! Resolver \"{}\" will resolve template \"{}\"",
                            new Object[] {TemplateEngine.threadIndex(), templateResolver.getName(), LoggingUtils.loggifyTemplateName(template)});
                }
                return templateResolution;
            }

            if (logger.isTraceEnabled()) {
                    logger.trace(
                            "[THYMELEAF][{}] Skipping template resolver \"{}\" for template \"{}\"",
                            new Object[] {TemplateEngine.threadIndex(), templateResolver.getName(), LoggingUtils.loggifyTemplateName(template)});
            }

        }

        throw new TemplateInputException(
                "Error resolving template \"" + LoggingUtils.loggifyTemplateName(template) + "\", " +
                "template might not exist or might not be accessible by " +
                "any of the configured Template Resolvers");

    }




    private static TemplateData buildTemplateData(
            final TemplateResolution templateResolution,
            final String template,
            final Set<String> templateSelectors,
            final TemplateMode templateMode,
            final boolean useCache) {

        final TemplateMode definitiveTemplateMode =
                (templateMode == null ? templateResolution.getTemplateMode() : templateMode);

        final ICacheEntryValidity definitiveCacheEntryValidity =
                (useCache? templateResolution.getValidity() : NonCacheableCacheEntryValidity.INSTANCE);

        return new TemplateData(
                template, templateSelectors, templateResolution.getTemplateResource(), definitiveTemplateMode, definitiveCacheEntryValidity);


    }




    private ITemplateParser getParserForTemplateMode(final TemplateMode templateMode) {
        switch (templateMode) {
            case HTML:       return this.htmlParser;
            case XML:        return this.xmlParser;
            case TEXT:       return this.textParser;
            case JAVASCRIPT: return this.javascriptParser;
            case CSS:        return this.cssParser;
            case RAW:        return this.rawParser;
            default:
                throw new IllegalArgumentException("No parser exists for template mode: " + templateMode);
        }
    }





    private static ITemplateHandler createTemplateProcessingHandlerChain(
            final IEngineContext context,
            final Writer writer) {

        final IEngineConfiguration configuration = context.getConfiguration();

        /*
         * Declare the pair of pointers that will allow us to build the chain of template handlers
         */
        ITemplateHandler firstHandler = null;
        ITemplateHandler lastHandler = null;

        /*
         * First type of handlers to be added: pre-processors (if any)
         */
        final Set<IPreProcessor> preProcessors = configuration.getPreProcessors(context.getTemplateMode());
        if (preProcessors != null) {
            for (final IPreProcessor preProcessor : preProcessors) {
                final Class<? extends ITemplateHandler> preProcessorClass = preProcessor.getHandlerClass();
                final ITemplateHandler preProcessorHandler;
                try {
                    preProcessorHandler = preProcessorClass.newInstance();
                } catch (final Exception e) {
                    // This should never happen - class was already checked during configuration to contain a zero-arg constructor
                    throw new TemplateProcessingException(
                            "An exception happened during the creation of a new instance of pre-processor " + preProcessorClass.getClass().getName(), e);
                }
                // Initialize the pre-processor
                preProcessorHandler.setContext(context);
                if (firstHandler == null) {
                    firstHandler = preProcessorHandler;
                    lastHandler = preProcessorHandler;
                } else {
                    lastHandler.setNext(preProcessorHandler);
                    lastHandler = preProcessorHandler;
                }
            }
        }


        /*
         * Initialize and add to the chain te Processor Handler itself, the central piece of the chain
         */
        final ProcessorTemplateHandler processorHandler = new ProcessorTemplateHandler();
        processorHandler.setContext(context);
        if (firstHandler == null) {
            firstHandler = processorHandler;
            lastHandler = processorHandler;
        } else {
            lastHandler.setNext(processorHandler);
            lastHandler = processorHandler;
        }


        /*
         * After the Processor Handler, we now must add the post-processors (if any)
         */
        final Set<IPostProcessor> postProcessors = configuration.getPostProcessors(context.getTemplateMode());
        if (postProcessors != null) {
            for (final IPostProcessor postProcessor : postProcessors) {
                final Class<? extends ITemplateHandler> postProcessorClass = postProcessor.getHandlerClass();
                final ITemplateHandler postProcessorHandler;
                try {
                    postProcessorHandler = postProcessorClass.newInstance();
                } catch (final Exception e) {
                    // This should never happen - class was already checked during configuration to contain a zero-arg constructor
                    throw new TemplateProcessingException(
                            "An exception happened during the creation of a new instance of post-processor " + postProcessorClass.getClass().getName(), e);
                }
                // Initialize the pre-processor
                postProcessorHandler.setContext(context);
                if (firstHandler == null) {
                    firstHandler = postProcessorHandler;
                    lastHandler = postProcessorHandler;
                } else {
                    lastHandler.setNext(postProcessorHandler);
                    lastHandler = postProcessorHandler;
                }
            }
        }


        /*
         * Last step: the OUTPUT HANDLER
         */
        final OutputTemplateHandler outputHandler = new OutputTemplateHandler(writer);
        outputHandler.setContext(context);
        if (firstHandler == null) {
            firstHandler = outputHandler;
        } else {
            lastHandler.setNext(outputHandler);
        }

        return firstHandler;

    }




}
