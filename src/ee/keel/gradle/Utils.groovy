package ee.keel.gradle

import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

import org.gradle.api.Plugin
import org.gradle.api.Project

import ee.keel.gradle.dsl.EnvironmentExtension
import ee.keel.gradle.dsl.JsToolkitExtension
import groovy.transform.CompileStatic

@CompileStatic
class Utils
{
	public static Plugin getPlugin(Project project, String which)
	{
		def ret = (Plugin) project.extensions.extraProperties.get(which)

		if(!ret)
		{
			throw new IllegalStateException("Plugin ${which.replace('__', '')} not defined!")
		}

		return ret
	}

	public static ToolsPlugin getToolsPlugin(Project project)
	{
		return (ToolsPlugin) getPlugin(project, ToolsPlugin.PLUGIN_PROPERTY_NAME)
	}

	public static JsToolkitExtension getExt(Project project)
	{
		return (JsToolkitExtension) project.extensions.getByName(BasePlugin.PLUGIN_EXTENSION_NAME)
	}

	public static EnvironmentExtension getEnvExt(Project project)
	{
		return (EnvironmentExtension) project.extensions.getByName(BasePlugin.ENV_EXTENSION_NAME)
	}

	public static String resolvePath(String name)
	{
		return System.getenv("PATH")
			.split(Pattern.quote(File.pathSeparator))
			.collect { Paths.get(it).resolve(name) }
			.find { Files.exists(it) && Files.isExecutable(it) }
	}
}
