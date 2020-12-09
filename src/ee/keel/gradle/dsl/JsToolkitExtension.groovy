package ee.keel.gradle.dsl

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input

import ee.keel.gradle.BasePlugin
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@CompileStatic
abstract class JsToolkitExtension extends JsToolkitModel
{
	private final static Logger logger = Logging.getLogger(JsToolkitExtension)

	@Input
	final Property<String> environment

	@Input
	final Property<EnvironmentExtension> currentEnvironment

	@Input
	final Property<String> name

	@Input
	final Property<String> version

	@Input
	final Property<String> librariesVersion

	@Input
	final Property<Boolean> preferParentTools

	@Input
	final DirectoryProperty toolsDirectory

	@Input
	final RegularFileProperty toolsLockfile

	@Input
	final Property<NodeConfig> node

	@Input
	final Property<PnpmConfig> pnpm

	@Input
	final Property<PackagesConfig> packages

	@Input
	final Property<WebpackConfig> webpack

	@Input
	final Property<BabelConfig> babel

	@Input
	final Property<SentryConfig> sentry

	@Input
	final NamedDomainObjectContainer<OutputConfig> outputs

	@Input
	final NamedDomainObjectContainer<LibraryConfig> libraries

	@Input
	final NamedDomainObjectContainer<ModuleConfig> modules

//	@Input
//	final NamedDomainObjectContainer<PatchConfig> patches

	@Input
	final NamedDomainObjectContainer<DistributionConfig> distributions

	@Input
	final NamedDomainObjectContainer<RuntimeConfig> runtimes

	JsToolkitExtension(Project project, BasePlugin plugin)
	{
		ObjectFactory of = getObjectFactory()
		ProviderFactory pf = getProviderFactory()

		name = of.property(String).convention(project.name)
		librariesVersion = of.property(String)
		preferParentTools = of.property(Boolean).convention(true)
		toolsDirectory = of.directoryProperty().convention(project.layout.projectDirectory.dir("tools"))
		toolsLockfile = of.fileProperty().convention(project.layout.projectDirectory.file("pnpm-lock-tools.yaml"))

		node = of.property(NodeConfig).convention(of.newInstance(NodeConfig, project))
		pnpm = of.property(PnpmConfig).convention(of.newInstance(PnpmConfig, project))
		packages = of.property(PackagesConfig).convention(of.newInstance(PackagesConfig, project))
		webpack = of.property(WebpackConfig).convention(of.newInstance(WebpackConfig, project))
		babel = of.property(BabelConfig).convention(of.newInstance(BabelConfig, project))
		sentry = of.property(SentryConfig).convention(of.newInstance(SentryConfig, project))

		outputs = objectFactory.domainObjectContainer(OutputConfig, { name -> of.newInstance(OutputConfig, name, project) })
		libraries = of.domainObjectContainer(LibraryConfig, { name -> of.newInstance(LibraryConfig, name, project) })
		modules = of.domainObjectContainer(ModuleConfig, { name -> of.newInstance(ModuleConfig, name, project) })
//		patches = of.domainObjectContainer(PatchConfig, { name -> of.newInstance(PatchConfig, name, project) })
		runtimes = of.domainObjectContainer(RuntimeConfig, { name -> of.newInstance(RuntimeConfig, name, project) })
		distributions = of.domainObjectContainer(DistributionConfig, { name -> of.newInstance(DistributionConfig, name, project) })

		environment = of.property(String).convention(project.provider {
			def proj = project

			while(proj)
			{
				if(proj.hasProperty("dev"))
				{
					return "development"
				}

				proj = proj.parent
			}

			return "production"
		})

		version = of.property(String).convention(project.provider {
			def proj = project

			while(proj)
			{
				def versionProperty = "${project.name}Version"

				if(proj.hasProperty(versionProperty))
				{
					logger.debug("Using version form property {}", versionProperty)

					return proj.getProperties().get(versionProperty) as String
				}

				if(proj.version && proj.version != "unspecified")
				{
					logger.debug("Using project {} version", project.name)

					return proj.version as String
				}

				proj = proj.parent
			}

			return "snapshot"
		})

		currentEnvironment = of.property(EnvironmentExtension)
		currentEnvironment.set(pf.provider({ plugin.envExt.getByName(environment.get()) }))
		currentEnvironment.disallowChanges()

		project.extensions.extraProperties.set("env", new EnvironmentHelper(currentEnvironment))

		if(project.hasProperty("libsVersion"))
		{
			librariesVersion.convention(project.properties.libsVersion as String)
		}
		else
		{
			librariesVersion.convention(version)
		}

		applyDefaults()
	}

	@CompileDynamic
	protected void applyDefaults()
	{
		logger.debug("Applying default tool versions")

		packages {
			version "@babel/core", "latest"
			version "@babel/plugin-proposal-class-properties", "latest"
			version "@babel/plugin-proposal-decorators", "latest"
			version "@babel/plugin-proposal-object-rest-spread", "latest"
			version "@babel/plugin-transform-runtime", "latest"
			version "@babel/plugin-syntax-dynamic-import", "latest"
			version "@babel/plugin-transform-regenerator", "latest"
			version "@babel/preset-env", "latest"
			version "@babel/preset-react", "latest"
			version "babel-loader", "latest"
			version "core-js", "latest"
			version "browserslist-useragent-regexp", "latest"
//			version "@babel/runtime-corejs3", "latest"
			version "@babel/runtime", "latest"
			version "@loadable/babel-plugin", "latest"

			version "postcss", "latest"
			version "postcss-cli", "siilike/postcss-cli"
			version "postcss-advanced-variables", "latest"
			version "postcss-css-variables", "siilike/postcss-css-variables"
			version "postcss-atroot", "latest"
			version "postcss-extend-rule", "latest"
			version "postcss-import", "latest"
			version "postcss-nested", "latest"
			version "postcss-nested-props", "latest"
			version "postcss-preset-env", "latest"
			version "postcss-property-lookup", "latest"
			version "postcss-scss", "latest"
			version "postcss-use", "latest"
			version "postcss-url", "latest"
			version "postcss-push", "latest"
			version "autoprefixer", "latest"
			version "cssnano", "latest"

			version "webpack", "^5"
			version "webpack-cli", "latest"
			version "terser", "latest"
			version "terser-webpack-plugin", "latest"
			version "webpack-babel-env-deps", "latest"
			version "enhanced-resolve", "latest"
			version "html-webpack-plugin", "latest"

			version "webpack-visualizer-plugin", "siilike/webpack-visualizer"
			version "madge", "latest"

			version "react-refresh", "latest"
			version "@pmmmwh/react-refresh-webpack-plugin", "latest"
 			version "webpack-plugin-serve", "siilike/webpack-plugin-serve"
			version "webpack-notifier", "latest"

			version "hjson", "latest"

			version "@sentry/cli", "latest"

			version "zeromq", "6.0.0-beta.6"
		}
	}

	def node(Closure c)
	{
		c.delegate = node.get()
		c.setResolveStrategy(Closure.DELEGATE_FIRST)
		c()
	}

	def pnpm(Closure c)
	{
		c.delegate = pnpm.get()
		c.setResolveStrategy(Closure.DELEGATE_FIRST)
		c()
	}

	def webpack(Closure c)
	{
		c.delegate = webpack.get()
		c.setResolveStrategy(Closure.DELEGATE_FIRST)
		c()
	}

	def babel(Closure c)
	{
		c.delegate = babel.get()
		c.setResolveStrategy(Closure.DELEGATE_FIRST)
		c()
	}

	def packages(Closure c)
	{
		c.delegate = packages.get()
		c.setResolveStrategy(Closure.DELEGATE_FIRST)
		c()
	}

	def sentry(Closure c)
	{
		c.delegate = sentry.get()
		c.setResolveStrategy(Closure.DELEGATE_FIRST)
		c()
	}
}
