package ee.keel.gradle

import java.util.regex.Pattern

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskActionListener
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.file.copy.CopySpecWrapper
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip
import org.redline_rpm.header.Os

import com.netflix.gradle.plugins.packaging.SystemPackagingBasePlugin
import com.netflix.gradle.plugins.packaging.SystemPackagingTask

import ee.keel.gradle.dsl.AbstractOutputConfig
import ee.keel.gradle.dsl.CopyFromNamed
import ee.keel.gradle.dsl.DistributionConfig
import ee.keel.gradle.dsl.EnvironmentsExtension
import ee.keel.gradle.dsl.JsToolkitExtension
import ee.keel.gradle.dsl.LibraryConfig
import ee.keel.gradle.dsl.ModuleConfig
import ee.keel.gradle.dsl.OutputConfig
import ee.keel.gradle.dsl.RuntimeConfig
import ee.keel.gradle.task.Deb
import ee.keel.gradle.task.HJsonTask
import ee.keel.gradle.task.NodeTask
import ee.keel.gradle.task.PnpmInstallTask
import ee.keel.gradle.task.PostCssTask
import ee.keel.gradle.task.Rpm
import ee.keel.gradle.task.SentryTask
import ee.keel.gradle.task.SentryUploadTask
import ee.keel.gradle.task.WebpackTask
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
abstract class JavascriptPlugin implements Plugin<Project>
{
	private final static Logger logger = Logging.getLogger(JavascriptPlugin)

	public static final String PLUGIN_PROPERTY_NAME = "__jsToolkitPlugin"

	public static final Pattern RESERVED_KEYWORDS = ~/^(\..*|runtime)$/
	public static final Pattern MODULE_RESERVED_KEYWORDS = ~/^(css)$/

	protected Project project
	protected JsToolkitExtension ext
	protected EnvironmentsExtension envExt

	public JavascriptPlugin()
	{
	}

	public JsToolkitExtension getExt()
	{
		return Utils.getExt(project)
	}

	@Override
	public void apply(Project project)
	{
		this.project = project

		project.plugins.apply(SystemPackagingBasePlugin)
		project.plugins.apply(ToolsPlugin)

		project.extensions.extraProperties.set(PLUGIN_PROPERTY_NAME, this)

		createTasks(project)
	}

	protected void createTasks(Project project)
	{
		/******************************* VALIDATION *******************************/

		project.afterEvaluate {
			[
				getExt().modules,
				getExt().distributions,
				getExt().outputs,
				getExt().libraries,
//				getExt().patches,
				getExt().runtimes,
			].each { container ->
				container.names.each { name ->
					if(name =~ RESERVED_KEYWORDS)
					{
						throw new IllegalStateException("\"${name}\" is a reserved keyword")
					}
				}
			}

			getExt().modules.each { name ->
				if(name =~ MODULE_RESERVED_KEYWORDS)
				{
					throw new IllegalStateException("\"${name}\" is a reserved keyword")
				}
			}
		}

		/*********************** ARTIFACTS LIST **************************/

		def artifactsFile = project.file("${project.buildDir}/latest-artifacts")

		project.beforeEvaluate {
			artifactsFile.delete()
		}

		project.gradle.addListener(new TaskActionListener()
		{
			@Override
			void beforeActions(Task task)
			{
			}

			@Override
			void afterActions(Task task)
			{
				if(task instanceof AbstractArchiveTask)
				{
					def files = task.outputs.files.asFileTree.files

					logger.debug("Saving files as artifacts: {}", files)

					artifactsFile << files.collect { it }.join("\n") + "\n"
				}
			}
		})

		/*********************** CONTAINERS & CLEANUP **************************/

		def buildLibraries = project.tasks.register("buildLibraries")

		def buildModulesCss = project.tasks.register("buildModulesCss")

		def buildModulesJs = project.tasks.register("buildModulesJs")

		def distAll = project.tasks.register("distAll")

		def distAllDeb = project.tasks.register("distAllDeb")

		def distAllRpm = project.tasks.register("distAllRpm")

		def distAllTar = project.tasks.register("distAllTar")

		def distAllZip = project.tasks.register("distAllZip")

		def distRuntimes = project.tasks.register("distRuntimes")

		def distRuntimesDeb = project.tasks.register("distRuntimesDeb")

		def distRuntimesRpm = project.tasks.register("distRuntimesRpm")

		def distRuntimesTar = project.tasks.register("distRuntimesTar")

		def distRuntimesZip = project.tasks.register("distRuntimesZip")

		def buildRuntimes = project.tasks.register("buildRuntimes")

		def buildPresetRegexes = project.tasks.register("buildPresetRegexes")

		def buildModules = project.tasks.register("buildModules", { Task it ->
			it.dependsOn buildModulesCss, buildModulesJs
		})

		def buildFrontend = project.tasks.register("buildFrontend", { Task it ->
			it.dependsOn buildModules
		})

		project.tasks.named("build").configure {
			it.dependsOn buildFrontend
		}

		def sentry = project.tasks.register("sentry")

		def cleanDist = project.tasks.register("cleanDist", { Task it ->
			it.doLast {
				project.file("${project.buildDir}/dist").deleteDir()
				project.file("${project.buildDir}/debian").deleteDir()
				project.file("${project.buildDir}/tmp").deleteDir()
				project.file("${project.buildDir}/latest-artifacts").delete()
			}
		})

		def cleanLibs = project.tasks.register("cleanLibs", { Task t ->
			t.doLast {
				project.file("${project.buildDir}/lib").deleteDir()
			}
		})

		def cleanManifests = project.tasks.register("cleanManifests", { Task t ->
			t.doLast {
				project.file("${project.buildDir}/manifest").deleteDir()
			}
		})

		def cleanModules = project.tasks.register("cleanModules", { Task t ->
			t.doLast {
				project.file("${project.buildDir}/module").deleteDir()
			}
		})

		def cleanRecords = project.tasks.register("cleanRecords", { Task t ->
			t.doLast {
				project.file("${project.buildDir}/records").deleteDir()
			}
		})

		def cleanBabelCache = project.tasks.register("cleanBabelCache", { Task t ->
			t.doLast {
				project.file("${project.buildDir}/.babel").deleteDir()
			}
		})

		def cleanNodeModules = project.tasks.register("cleanNodeModules", { Task t ->
			t.doLast {
				project.file("${project.buildDir}/node_modules").deleteDir()
			}
		})

		def cleanPackageJson = project.tasks.register("cleanPackageJson", { Task t ->
			t.doLast {
				project.file("${project.buildDir}/package.json").delete()
				project.file("${project.buildDir}/pnpm-lock.yaml").delete()
			}
		})

		def cleanAll = project.tasks.register("cleanFrontend", { Task t ->
			t.dependsOn cleanDist, cleanLibs, cleanModules, cleanBabelCache, cleanNodeModules, cleanManifests, cleanRecords, cleanPackageJson
		})

		def underlayDirs = [
			project.layout.projectDirectory.dir("underlay"),
			project.layout.projectDirectory.dir("underlay-development"),
			project.layout.projectDirectory.dir("underlay-production"),
		].findAll { it.asFile.exists() }

		def libsDirs = [
			project.layout.projectDirectory.dir("libs"),
			project.layout.projectDirectory.dir("libs-development"),
			project.layout.projectDirectory.dir("libs-production"),
		].findAll { it.asFile.exists() }

		def inputDirs = underlayDirs + libsDirs

		/********************************** PACKAGES ************************************/

/*
		def copyPackageJson = project.tasks.register("copyPackageJson", Copy, { Copy it ->
			it.from project.projectDir
			it.include "package.json"
			it.include "pnpm-lock.yaml"
			it.into project.buildDir
		})
*/

		def convertPackageJson = project.tasks.register("convertPackageJson", HJsonTask, { HJsonTask t ->
			def input = project.file("package.hjson")
			t.enabled = input.exists()
			t.input.set(input)
			t.output.set(project.file("package.json"))
		});

		def copyPackageJson = project.tasks.register("copyPackageJson", { Task t ->
			t.dependsOn convertPackageJson

			def files = [ "package.json", "pnpm-lock.yaml" ]

			files.each { f ->
				t.inputs.files "${project.projectDir}/${f}"
				t.outputs.file "${project.buildDir}/${f}"
			}

			t.doLast {
				files.each { f ->
					def input = new File("${project.projectDir}/${f}")
					def output = new File("${project.buildDir}/${f}")

					if(input.exists())
					{
						output.bytes = input.bytes
					}
				}
			}
		})

		def installDependencies = project.tasks.register("installDependencies", PnpmInstallTask, { PnpmInstallTask t ->
			t.dependsOn copyPackageJson
			t.dependsOn ToolsPlugin.ENSURE_BUILD_TOOLS_TASK

			t.setWorkingDir(project.buildDir)
		})

		// FIXME gradle bug
		project.afterEvaluate {

		/********************************** REGEXES ************************************/

		((Map<String, String>) ext.babel.get().presets.get()).each { name, preset ->
			def regexTask = project.tasks.register("buildPreset${name.capitalize()}Regex", NodeTask, { NodeTask t ->
				t.dependsOn installDependencies

				def outRegex = new File(project.buildDir, "regex/${name}.regex")

				t.outputs.file outRegex

				t.args "${getExt().toolsDirectory.asFile.get().absolutePath}/node_modules/browserslist-useragent-regexp/lib/cli.js"
				t.args "--allowHigherVersions"
				t.args preset

				t.doFirst {
					t.setStandardOutput(new FileOutputStream(outRegex))
				}

				t
			})

			buildPresetRegexes.configure {
				it.dependsOn regexTask
			}
		}

		/********************************** LIBRARIES ************************************/

		ext.libraries.all { LibraryConfig conf ->
			String library = conf.name
			def lcap = library.capitalize()
			def libDir = project.layout.buildDirectory.dir("lib/${library}")

			conf.babel.get().config.convention(configProvider("babel", library))
			conf.babel.get().presets.convention(getExt().babel.map { it.presets.get() })
			conf.outputs.convention(project.provider { getExt().outputs })

			cleanLibs.configure {
				it.doLast {
					libDir.get().asFile.deleteDir()
				}
			}

			def buildLibrary = project.tasks.register("buildLibrary${lcap}", { Task t ->
				t
			})

			def writeLibraryVersion = project.tasks.register("writeLibrary${lcap}Version", { Task t ->
				def versionFile = libDir.get().file(".version").asFile

				t.outputs.file versionFile

				t.doLast {
					versionFile.text = getExt().librariesVersion.get()
				}
			})

			buildLibraries.configure {
				it.dependsOn buildLibrary
			}

			conf.outputs.get().all { OutputConfig out ->
				def ocap = out.name.capitalize()

				((Collection<String>) out.presets.get()).each { String preset ->
					def webpackTask = project.tasks.register("buildLibrary${lcap}${ocap}${preset.capitalize()}", WebpackTask, { WebpackTask t ->
						t.dependsOn installDependencies
						t.finalizedBy writeLibraryVersion

						libsDirs.each {
							t.inputs.dir it
						}

						t.inputs.files getExt().webpack.map { it.inputs }
						t.outputs.files t.manifestDirectory.file("manifest-${library}-${out.name}-${preset}.json")

						t.env.set(out.name)
						t.module.set(library)
						t.browsersListEnv.set(preset)
						t.minify.set(out.minify)
						t.config.value(configProvider("webpack", library, true))
						t.babelConfig.value(configProvider("babel", library, true, true))
						t.outputDirectory.set(libDir.map { it.dir(out.name) })

						t.entries.set(project.provider {
							def a = conf.includes.get().includes

							if(!a.isEmpty())
							{
								return a as List
							}

							Map b = (Map) new JsonSlurper().parse(project.file("${project.buildDir}/package.json"))

							if(b.containsKey("dependencies"))
							{
								return ((Map) b.dependencies).keySet() as List
							}

							return []
						})

						t.environment "VERSION", getExt().librariesVersion.get()
						t.environment "SENTRY_RELEASE", getExt().sentry.map { it.release.get() }.get()

						t
					})

					writeLibraryVersion.configure {
						it.outputs.upToDateWhen { !webpackTask.get().didWork }
						it
					}

					buildLibrary.configure {
						it.dependsOn webpackTask
					}
				}
			}
		}

		/********************************** MODULES ************************************/

		ext.modules.all { ModuleConfig conf ->
			String module = conf.name

			conf.babel.get().config.convention(configProvider("babel", module))
			conf.babel.get().presets.convention(getExt().babel.map { it.presets.get() })
			conf.outputs.convention(project.provider { getExt().outputs })

			def mcap = module.capitalize()
			def moduleDir = project.layout.buildDirectory.dir("module/${module}")
			def cssInputDir = project.layout.projectDirectory.dir("css/${module}")
			def cssTmpFile = moduleDir.map { it.file(".${module}.css") }
			def cssOutputFile = moduleDir.map { it.file("css/${module}.css") }
			def jsInputDir = project.layout.projectDirectory.dir("js/${module}")

			cleanModules.configure {
				it.doLast {
					moduleDir.get().asFile.deleteDir()
				}
			}

			def buildModule = project.tasks.register("buildModule${mcap}", { Task t ->
				//
			})

			def writeModuleVersion = project.tasks.register("writeModule${mcap}Version", { Task t ->
				def versionFile = moduleDir.map { it.file(".version") }

				t.inputs.property "version", getExt().version
				t.outputs.file versionFile

				t.doLast {
					versionFile.get().asFile.text = getExt().librariesVersion.get()
				}
			})

			def prepareModule = project.tasks.register("prepareModule${mcap}", { Task t ->
				t.dependsOn installDependencies
				t.finalizedBy writeModuleVersion

				((Collection<String>) conf.libraries.get()).each { String l ->
					t.dependsOn "buildLibrary${l.capitalize()}"

					t.doLast {
						def versionFile = moduleDir.map { it.file(".version-${l}") }

						versionFile.get().asFile.text = project.layout.buildDirectory.file("lib/${l}/.version").get().asFile.text
					}
				}

				t.inputs.dir cssInputDir
				t.inputs.property "css", conf.css

				t.outputs.files cssTmpFile

				t.doFirst {
					moduleDir.get().asFile.mkdirs()

					Map<String, Object> css = conf.css.get()

					def files = project.fileTree(cssInputDir).files.collect { it.name }
					files.addAll(css.keySet())

					def sorted = files.sort { a, b ->
						def n1 = (a =~ /^\d+/) ? (a =~ /^\d+/)[-1] as Integer : 1000000
						def n2 = (b =~ /^\d+/) ? (b =~ /^\d+/)[-1] as Integer : 1000000
						return n1 == n2 ? a <=> b : n1 <=> n2;
					}

					def inputFiles = sorted.collect { String fn ->
						def f = cssInputDir.file(fn).asFile

						if(f.exists())
						{
							return f
						}

						return project.file(css.get(fn))
					}

					def imports = inputFiles.collect { "@import \"${it.absolutePath}\";" }.join("\n")

					cssTmpFile.get().asFile.text = imports
				}
			})

			def buildModuleCss = project.tasks.register("buildModule${mcap}Css", PostCssTask, { PostCssTask t ->
				t.dependsOn ToolsPlugin.ENSURE_BUILD_TOOLS_TASK
				t.dependsOn prepareModule

				t.input.add(cssTmpFile.get().asFile)
				t.output.set(cssOutputFile)
				t.config.value(configProvider("postcss", module, false))
				t.browsers.convention(project.provider { getExt().babel.get().presets.get().get('production') })
				t.continuous.set(project.gradle.startParameter.continuous)

				t
			})

			buildModule.configure {
				it.dependsOn buildModuleCss
			}

			buildModulesCss.configure {
				it.dependsOn buildModuleCss
			}

			conf.outputs.get().all { OutputConfig out ->
				def ocap = out.name.capitalize()

				((Collection<String>) out.presets.get()).each { String preset ->
					def webpackTask = project.tasks.register("buildModule${mcap}${ocap}${preset.capitalize()}", WebpackTask, { WebpackTask t ->
						t.enabled = jsInputDir.file("index.js").asFile.exists()

						t.dependsOn installDependencies

						t.env.set(out.name)
						t.module.set(module)
						t.browsersListEnv.set(preset)
						t.libraries.set(conf.libraries)
						t.config.value(configProvider("webpack", module))
						t.babelConfig.value(configProvider("babel", module))
						t.outputDirectory.set(moduleDir.map { it.dir(out.name) })
						t.continuous.set(project.gradle.startParameter.continuous)

						t.environmentProperty "VERSION", getExt().version
						t.environmentProvider "SENTRY_RELEASE", getExt().sentry.map { it.release.get() }

						((Collection<String>) conf.libraries.get()).each { String name ->
							t.inputs.files t.manifestDirectory.file("manifest-${name}-${out.name}-${preset}.json")

							t.dependsOn project.tasks.named("buildLibrary${name.capitalize()}${out.name.capitalize()}${preset.capitalize()}")
						}

						inputDirs.each {
							t.inputs.dir it
						}

						t.inputs.dir jsInputDir
						t.inputs.files getExt().webpack.map { it.inputs }
						t
					})

					buildModulesJs.configure {
						it.dependsOn webpackTask
					}

					buildModule.configure {
						it.dependsOn webpackTask
					}
				}
			}
		}

		/*********************************** DIST **************************************/

		def applySources = { DistributionConfig conf, AbstractCopyTask t ->
			t.dependsOn installDependencies

			def a = { String k, Collection<CopyFromNamed> v ->
				v.each {
					if(k)
					{
						t.dependsOn "build${k.capitalize()}${it.name.capitalize()}"
					}

					def spec = t.instantiator.newInstance(CopySpecWrapper.class, t)

					it.configure.delegate = spec
					it.configure.setResolveStrategy(Closure.DELEGATE_FIRST)

					if(it.name)
					{
						def dir = project.layout.buildDirectory.dir("${k == 'library' ? 'lib' : k}/${it.name}").get()

						switch(it.configure.getMaximumNumberOfParameters())
						{
							case 0:
								it.configure.call()
								break
							case 1:
								it.configure.call(dir)
								break
							case 2:
								it.configure.call(dir, spec)
								break
							default:
								throw new IllegalStateException("Too many parameters: ${it.configure.getMaximumNumberOfParameters()}")
						}
					}
					else
					{
						switch(it.configure.getMaximumNumberOfParameters())
						{
							case 0:
								it.configure.call()
								break
							case 1:
								it.configure.call(spec)
								break
							default:
								throw new IllegalStateException("Too many parameters: ${it.configure.getMaximumNumberOfParameters()}")
						}
					}
				}
			}

			if(conf.excludeMaps.get())
			{
				logger.debug("Excluding maps from output")

				t.exclude "**/*.map"
			}

			a(null, conf.copyFrom)
			a("module", conf.copyFromModule)
			a("library", conf.copyFromLibrary)
		}

		def createArchiveTasks = { DistributionConfig conf, Map<Class, TaskProvider> archiveTasks, Provider<Directory> dst, Provider<String> version, String prefix, TaskProvider<? extends Task> distTask, boolean versionDependentPackageName ->
			def dcap = conf.name.capitalize()
			def pcap = prefix.capitalize()
			Collection<TaskProvider> ret = []

			archiveTasks.keySet().each { taskClass ->
				def name = taskClass.simpleName

				def task = project.tasks.register("dist${dcap}${pcap}${name}", taskClass, { AbstractArchiveTask t ->
					t.enabled = conf.enabled.get()

					def versionFile = dst.map { it.file(".version${name}") }

					t.destinationDirectory.set(dst)
					t.archiveBaseName.set(conf.archive.map { it.name.get() })
					t.archiveVersion.set(version)
					t.archiveFileName.set(conf.archive.map { it.name.get() + '.' + t.archiveExtension.get() })

					if(t instanceof Tar)
					{
						t.compression = Compression.GZIP
						t.archiveExtension.set("tar.gz")
					}

					applySources(conf, t)

					((Collection<String>) conf.dependencies.get()).each { dep ->
						t.dependsOn "dist${dep.capitalize()}${name}"
						t.mustRunAfter "writeDist${dep.capitalize()}${name}Version"
					}

					AbstractOutputConfig c = null;

					if(t instanceof SystemPackagingTask)
					{
						c = conf.repo.get()

						// via delegate to SystemPackagingExtension
						t.packageName = versionDependentPackageName ? "${conf.repo.get().name.get()}-${version.get()}" : conf.repo.get().name.get()

						org.gradle.internal.deprecation.DeprecationLogger.whileDisabled {
							t.version = versionDependentPackageName ? '1' : version.get()
						}

						t.os = Os.LINUX
						t.summary = conf.repo.get().name.get()

						t.into conf.repo.get().path

						def p = conf.repo.get().path.get().substring(1)

						if(p[p.length()-1] == "/")
						{
							p = p.substring(0, p.length()-1)
						}

						t.inputs.property "packageName", ""
						t.inputs.property "exten.packageName", ""
						t.inputs.properties.sort().each { k, v ->
							if(k.startsWith("rootSpec\$") && k.endsWith(".destPath") && v.toString().startsWith(p))
							{
								t.inputs.property k, ""
							}
						}

						t.outputs.upToDateWhen { true }

						t.doFirst {
							((Collection<String>) conf.dependencies.get()).each { dep ->
								def d = getExt().distributions.getByName(dep)
								def n = d.repo.get().name.get()
								def v = project.layout.buildDirectory.dir("dist/${dep}/.version-${name.toLowerCase()}").get().asFile.text

								t.requires "${n}-${v}"
							}
						}
					}
					else
					{
						c = conf.archive.get()
					}

					if(c)
					{
						t.enabled &= c.enabled.get()

						Closure a = c.packageConfigurators.get().get(name.toLowerCase())

						if(a)
						{
							a.setDelegate(t)
							a()
						}
					}

					t
				})

				def writeDistVersion = project.tasks.register("writeDist${dcap}${pcap}${name}Version", { Task t ->
					t.enabled = task.map { it.enabled }
					t.onlyIf { task.map { it.didWork }.get() }

					def versionFile = dst.map { it.file(".version-${name.toLowerCase()}") }.get().asFile

					t.inputs.property "version", getExt().version
					t.outputs.file versionFile

					t.doLast {
						versionFile.text = getExt().version.get()
					}
				})

				task.configure {
					it.finalizedBy writeDistVersion
				}

				distTask?.configure {
					it.dependsOn task
				}

				archiveTasks[taskClass].configure {
					((Task) it).dependsOn task
				}

				ret.push(task)
			}

			return ret
		}

		ext.distributions.all { DistributionConfig conf ->
			conf.archive.get().name.convention(conf.name)
			conf.repo.get().name.convention(conf.name)

			def dcap = conf.name.capitalize()
			def version = conf.library.get() ? getExt().librariesVersion : getExt().version
			def dst = project.layout.buildDirectory.dir("dist/${conf.name}")
			def dstCopy = project.layout.buildDirectory.dir("dist2/${conf.name}")

			def copyTask = project.tasks.register("dist${dcap}Copy", Copy, { Copy t ->
				t.into dstCopy

				applySources(conf, t)

				t
			})

			def distTask = project.tasks.register("dist${dcap}All", { Task t ->
				t.dependsOn copyTask
				t
			})

			Map<Class, TaskProvider> archiveTasks = [:].with {
				put(Tar, distAllTar)
				put(Zip, distAllZip)
				put(Deb, distAllDeb)
				put(Rpm, distAllRpm)
				it
			}

			createArchiveTasks(conf, archiveTasks, dst, version, "", distTask, true)

			distAll.configure {
				it.dependsOn distTask
			}
		}

		/*********************************** RUNTIME **************************************/

		ext.runtimes.all { RuntimeConfig conf ->
			def rcap = conf.name.capitalize()
			def outputDir = project.layout.buildDirectory.dir("runtime/${conf.name}")
			def outputNames = (Collection<String>) conf.outputs.get()
			def moduleNames = (Collection<String>) conf.modules.get()
			def outputs = outputNames.collect { getExt().outputs.getByName(it) }
			def modules = moduleNames.collect { getExt().modules.getByName(it) }
			def libraryNames = modules.collectMany([] as Set) { it.libraries.get() as Collection<String> }

			def templateTask = project.tasks.register("buildRuntime${rcap}Index", { Task t ->
				t.dependsOn buildPresetRegexes

				t.inputs.file conf.template
				t.outputs.dir outputDir

				def templateFile = conf.template.get().asFile

				if(!templateFile.exists())
				{
					throw new RuntimeException("Template file ${templateFile} does not exist!")
				}

				def template = new groovy.text.StreamingTemplateEngine().createTemplate(templateFile.text)
				def presetNames = [] as Set

				outputs.each { out ->
					((Collection<String>) out.presets.get()).each { preset ->
						presetNames.add(preset)

						moduleNames.each { module ->
							t.dependsOn "buildModule${module.capitalize()}${out.name.capitalize()}${preset.capitalize()}"
						}

						libraryNames.each { lib ->
							t.dependsOn "buildLibrary${lib.capitalize()}${out.name.capitalize()}${preset.capitalize()}"
						}
					}
				}

				t.doLast {
					def binding = [:] + conf.templateVars.get()

					def libraryData = libraryNames.collectEntries { n ->
						[ n, outputs.collectEntries {
								[
									it.name,
									((Collection<String>) it.presets.get()).collectEntries { pn ->
										[ pn, [:] ]
									}
								]
							}
						]
					}

					def moduleData = moduleNames.collectEntries { n ->
						[ n, outputs.collectEntries {
								[
									it.name,
									((Collection<String>) it.presets.get()).collectEntries { pn ->
										[ pn, [:] ]
									}
								]
							}
						]
					}

					outputs.each { out ->
						((Collection<String>) out.presets.get()).each { preset ->
							libraryNames.each { name ->
								def fn = "${name}-${out.name}-${preset}.js"
								def file = project.layout.buildDirectory.file("lib/${name}/${out.name}/${fn}").get().asFile

								Map d = libraryData[name][out.name][preset] as Map
								d.size = file.length()
								d.name = fn
							}

							moduleNames.each { name ->
								def fn = "${name}-main-${out.name}-${preset}.js"
								def file = project.layout.buildDirectory.file("module/${name}/${out.name}/${fn}").get().asFile

								Map d = moduleData[name][out.name][preset] as Map
								d.size = file.length()
								d.name = fn
							}
						}
					}

					def presetRegexes = presetNames.collectEntries {
						[ it, project.layout.buildDirectory.file("regex/${it}.regex").get().asFile.text.trim() ]
					}

					if(!presetRegexes.containsKey(conf.defaultPreset.get()))
					{
						logger.error("Default preset {} not found!", conf.defaultPreset.get())
					}

					binding +=
					[
						jstk: getExt(),
						moduleNames: moduleNames,
						libraryNames: libraryNames,
						outputNames: outputNames,
						modules: moduleData,
						libraries: libraryData,
						outputs: ((List<String>) conf.outputs.get()).collect { getExt().outputs.getByName(it) },
						defaultPreset: conf.defaultPreset.get(),
						indexFileName: conf.indexFileName.get(),
						presetRegexes: presetRegexes,
					]

					String rendered = template.make(binding)

					outputDir.get().asFile.mkdirs()
					outputDir.get().file(conf.indexFileName.get()).asFile.text = rendered
				}
			})

			def runtimeTask = project.tasks.register("buildRuntime${rcap}", { Task t ->
				t.dependsOn templateTask
				t
			})

			if(conf.distribution.present)
			{
				DistributionConfig dconf = conf.distribution.get()

				dconf.archive.get().name.convention(dconf.name)
				dconf.repo.get().name.convention(dconf.name)

				def dcap = dconf.name.capitalize()
				def version = getExt().version
				def dst = project.layout.buildDirectory.dir("dist/runtime/${dconf.name}")
				def dstCopy = project.layout.buildDirectory.dir("dist2/runtime/${dconf.name}")

				def copyTask = project.tasks.register("dist${dcap}RuntimeCopy", Copy, { Copy t ->
					t.dependsOn runtimeTask

					t.into dstCopy

					applySources(dconf, t)

					t
				})

				def distTask = project.tasks.register("dist${dcap}RuntimeAll", { Task t ->
					t.dependsOn copyTask
					t
				})

				Map<Class, TaskProvider> archiveTasks = [:].with {
					put(Tar, distRuntimesTar)
					put(Zip, distRuntimesZip)
					put(Deb, distRuntimesDeb)
					put(Rpm, distRuntimesRpm)
					it
				}

				createArchiveTasks(dconf, archiveTasks, dst, version, "runtime", distTask, false).each { t ->
					t.configure { Task tt ->
						tt.dependsOn runtimeTask
					}
				}

				distRuntimes.configure {
					it.dependsOn distTask
				}
			}

			buildRuntimes.configure {
				it.dependsOn runtimeTask
			}
		}

		} // afterEvaluate gradle bug

		/*********************************** SENTRY *************************************/

		def sentryCreateRelease = project.tasks.register("sentryCreateRelease", SentryTask, { SentryTask t ->
			t.dependsOn buildLibraries, buildModules

			t.args "releases", "new", t.release.get()
			t
		})

		def sentryUploadArtifacts = project.tasks.register("sentryUploadArtifacts", SentryUploadTask, { SentryUploadTask t ->
			t.dependsOn buildLibraries, buildModules
			t.dependsOn sentryCreateRelease

			t
		})

		def sentryUploadSourceMaps = project.tasks.register("sentryUploadSourceMaps", SentryTask, { SentryTask t ->
			t.dependsOn buildLibraries, buildModules
			t.dependsOn sentryCreateRelease

			t.args "releases", "files", t.release.get(), "upload-sourcemaps"
			t.args "--rewrite", "--validate"

			t.doFirst {
				t.args project.fileTree(project.buildDir) { ConfigurableFileTree ft ->
					ft.exclude "**/*.css.map"
					ft.include "lib/*/*/*.map"
					ft.include "module/*/*/*.map"
				}.files.collect { it.absolutePath }
			}

			t
		})

		def sentrySetCommits = project.tasks.register("sentrySetCommits", SentryTask, { SentryTask t ->
			t.dependsOn sentryCreateRelease

			t.onlyIf { project.file("${project.projectDir}/.git").exists() }

			t.workingDir project.projectDir
			t.args "releases", "set-commits", t.release.get(), "--auto"
			t
		})

		def sentryFinalizeRelease = project.tasks.register("sentryFinalizeRelease", SentryTask, { SentryTask t ->
			t.dependsOn sentryCreateRelease, sentryUploadArtifacts, sentryUploadSourceMaps, sentrySetCommits

			t.args "releases", "finalize", t.release.get()
			t
		})

		sentry.configure {
			it.dependsOn sentryCreateRelease, sentryUploadArtifacts, sentryUploadSourceMaps, sentrySetCommits, sentryFinalizeRelease
		}
	}

	public Provider<RegularFile> configProvider(String app, String name, boolean isLibrary = false, boolean libraryFallbackToMain = false)
	{
		return project.provider({
			RegularFile a = name ? project.layout.projectDirectory.file("${app}.config.${name}.js") : null

			if(!a || !a.asFile.exists())
			{
				a = project.layout.projectDirectory.file(isLibrary ? "${app}.config.libs.js" : "${app}.config.js")
			}

			if(isLibrary && libraryFallbackToMain && !a.asFile.exists())
			{
				a = project.layout.projectDirectory.file("${app}.config.js")
			}

			if(!a.asFile.exists())
			{
				a = getExt().toolsDirectory.file(isLibrary ? "${app}/config.libs.js" : "${app}/config.js").get()
			}

			if(isLibrary && libraryFallbackToMain && !a.asFile.exists())
			{
				a = getExt().toolsDirectory.file("${app}/config.js").get()
			}

			return a
		})
	}
}
