package ee.keel.gradle

import java.nio.file.Files
import java.nio.file.StandardCopyOption

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

import ee.keel.gradle.dsl.JsToolkitExtension
import ee.keel.gradle.task.DownloadToolTask
import ee.keel.gradle.task.PnpmInstallTask
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
abstract class ToolsPlugin implements Plugin<Project>
{
	private final static Logger logger = Logging.getLogger(ToolsPlugin)

	public static final String PLUGIN_PROPERTY_NAME = "__jsToolkitToolsPlugin"

	public static final String CLEAN_TOOLS_TASK = "cleanTools"
	public static final String INSTALL_BUILD_TOOLS_TASK = "installBuildTools"
	public static final String ENSURE_BUILD_TOOLS_TASK = "ensureBuildTools"

	protected Project project

	public ToolsPlugin()
	{
	}

	public JsToolkitExtension getExt()
	{
		return Utils.getExt(project);
	}

	@Override
	public void apply(Project project)
	{
		this.project = project

		project.plugins.apply(BasePlugin)

		project.extensions.extraProperties.set(PLUGIN_PROPERTY_NAME, this)

		project.afterEvaluate {
			createTasks(project)
		}
	}

	public Project getParentConfigProject()
	{
		def proj = project.parent

		while(proj)
		{
			def plugins = (Collection<Plugin>) proj.plugins // CompileStatic fail

			// plugins.findPlugin does not work
			if(plugins.find { it instanceof ToolsPlugin })
			{
				return proj
			}

			proj = proj.parent
		}

		return null
	}

	protected void createTasks(Project project)
	{
		if(getExt().preferParentTools.get())
		{
			getExt().preferParentTools.finalizeValue()

			def parent = getParentConfigProject()

			if(parent)
			{
				logger.warn("{} uses tools configuration from parent project {}", project.name, parent.name)

				def parentConfig = Utils.getExt(parent)

				getExt().toolsDirectory.set(parentConfig.toolsDirectory)
				getExt().node.get().path.set(parentConfig.node.get().path)
				getExt().pnpm.get().path.set(parentConfig.pnpm.get().path)

				def ensureBuildTools = project.tasks.register(ENSURE_BUILD_TOOLS_TASK, { Task it ->
					it.dependsOn parent.tasks.named(INSTALL_BUILD_TOOLS_TASK)
				})

				return
			}
		}

		def cleanTools = project.tasks.register(CLEAN_TOOLS_TASK, { Task it ->
			it.doLast {
				getExt().toolsDirectory.asFile.get().deleteDir()
			}
		})

		def downloadNode = project.tasks.register("downloadNode", DownloadToolTask, { DownloadToolTask it ->
			def version = getExt().node.map { it.version.get() }

			it.enabled = !project.gradle.startParameter.isOffline() && version.get() != "local"

			it.toolName.set("node")
			it.location.set(version)
			it.stripFirstDirectory.set(true)
		})

		def downloadPnpm = project.tasks.register("downloadPnpm", DownloadToolTask, { DownloadToolTask it ->
			def version = getExt().pnpm.map { it.version.get() }

			it.enabled = !project.gradle.startParameter.isOffline() && version.get() != "local"

			it.toolName.set("pnpm")
			it.location.set(version.map { v ->
				if(v =~ /^[0-9]+\.[0-9]+\.[0-9]+$/)
				{
					// npm view pnpm@latest dist.tarball
					return new URL("https://registry.npmjs.org/pnpm/-/pnpm-${v}.tgz")
				}

				return v
			})
			it.stripFirstDirectory.set(true)
		})

		def copyJsDeps = project.tasks.register("copyJsDependencies", { Task it ->
			[
				"babel/plugin-trace.js",
				"babel/config.js",
				"webpack/base.js",
				"webpack/base.config.js",
				"webpack/base.config.libs.js",
				"webpack/config.js",
				"webpack/config.libs.js",
				"webpack/plugin.js",
				"postcss/base.js",
				"postcss/base.config.js",
				"postcss/config.js",
				"postcss/postcss.config.js",
				"sentry/FrameRewriter.js",
				"logging/logger.js",
				"certs/cert.key",
				"certs/cert.crt",
			].each { f ->
				def outFile = new File(getExt().toolsDirectory.asFile.get(), f)

				it.outputs.file outFile

				it.doLast {
					Files.copy(ToolsPlugin.getResourceAsStream("/${f}"), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
				}
			}
		})

		def setupTools = project.tasks.register("setupTools", { Task it ->
			it.dependsOn downloadNode
			it.dependsOn downloadPnpm
			it.dependsOn copyJsDeps
		})

		def generateBuildToolsPackageJson = project.tasks.register("generateBuildToolsPackageJson", { Task it ->
			def configFile = getExt().packages.get().configFile

			it.dependsOn setupTools

			it.inputs.property "versions", getExt().packages.get().versions
			it.inputs.files getExt().toolsLockfile

			if(configFile.present)
			{
				it.inputs.files configFile
			}

			it.outputs.files getExt().toolsDirectory.file("package.json")
			it.outputs.files getExt().toolsDirectory.file("pnpm-lock.yaml")

			it.doFirst {
				def pkgjson = JsonOutput.toJson(
				[
					"dependencies": getExt().packages.get().packageVersions,
				])

				getExt().toolsDirectory.file("package.json").get().asFile.text = pkgjson
			}

			it.doFirst {
				def lf = getExt().toolsLockfile.asFile.get()

				if(lf.exists())
				{
					Files.copy(lf.toPath(), getExt().toolsDirectory.file("pnpm-lock.yaml").get().asFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
				}
			}
		})

		def installBuildToolsPnpm = project.tasks.register("installBuildToolsPnpm", PnpmInstallTask, { PnpmInstallTask it ->
			it.dependsOn setupTools
			it.dependsOn generateBuildToolsPackageJson

			it.inputs.files generateBuildToolsPackageJson.get().outputs

			it.setWorkingDir(getExt().toolsDirectory)
		})

		def saveBuildToolsLocks = project.tasks.register("saveBuildToolsLocks", { Task it ->
			it.dependsOn installBuildToolsPnpm

			it.inputs.files getExt().toolsDirectory.file("pnpm-lock.yaml")

			it.outputs.files getExt().toolsLockfile

			it.doLast {
				Files.copy(getExt().toolsDirectory.file("pnpm-lock.yaml").get().asFile.toPath(), getExt().toolsLockfile.asFile.get().toPath(), StandardCopyOption.REPLACE_EXISTING)
			}
		})

		def installBuildTools = project.tasks.register(INSTALL_BUILD_TOOLS_TASK, { Task it ->
			it.dependsOn(installBuildToolsPnpm)
		})

		def ensureBuildTools = project.tasks.register(ENSURE_BUILD_TOOLS_TASK, { Task it ->
			it.dependsOn INSTALL_BUILD_TOOLS_TASK
		})
	}
}
