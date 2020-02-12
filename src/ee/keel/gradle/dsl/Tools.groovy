package ee.keel.gradle.dsl

import javax.inject.Inject

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional

import ee.keel.gradle.Utils
import groovy.transform.CompileStatic

@CompileStatic
abstract class PackagesConfig extends JsToolkitModel
{
	@Input
	final MapProperty<String, String> versions = objectFactory.mapProperty(String, String)

	@Optional
	@InputFile
	final RegularFileProperty configFile = objectFactory.fileProperty()

	@Inject
	public PackagesConfig(Project project)
	{
		super(project)
	}

	void version(String k, Object v)
	{
		versions.put(k, String.valueOf(v))
	}

	Map<String, String> getPackageVersions()
	{
		return versions.get()
	}

	void clearVersions()
	{
		versions.empty()
	}
}

@CompileStatic
abstract class WebpackConfig extends JsToolkitModel
{
	@Input
	final SetProperty<String> inputFields = objectFactory.setProperty(String)

	@Input
	final SetProperty<String> outputFields = objectFactory.setProperty(String)

	@InputFiles
	final ConfigurableFileCollection inputs = objectFactory.fileCollection()

	@Input
	final DirectoryProperty directory = objectFactory.directoryProperty()

	@Input
	final Property<Boolean> alwaysTranspile = project.objects.property(Boolean).convention(false)

	@Inject
	public WebpackConfig(Project project)
	{
		super(project)

		inputs.from(
			"${project.buildDir}/node_modules/",
			"${project.projectDir}/libs/",
			"${project.projectDir}/underlay/",
		)

		directory.convention(project.layout.projectDirectory.dir("webpack"))
	}

	void inputField(String k)
	{
		inputFields.add(k)
	}

	void outputField(String k)
	{
		outputFields.add(k)
	}
}

@CompileStatic
abstract class BabelConfig extends JsToolkitModel
{
	@InputFile
	final RegularFileProperty config = objectFactory.fileProperty()

	@Input
	final MapProperty<String, String> presets = objectFactory.mapProperty(String, String)

	@Input
	final Property<String> reactPragma = objectFactory.property(String)

	@Inject
	public BabelConfig(Project project)
	{
		super(project)

		presets.convention(project.provider {
			[
				"production": "> 0.5%, ie 11, Firefox ESR, not dead"
			]
		})

		reactPragma.convention("React.createElement")
	}

	void preset(String name, String value)
	{
		presets.put(name, value)
	}
}

@CompileStatic
abstract class ToolConfig extends JsToolkitModel
{
	@Input
	final Property<Object> version = objectFactory.property(Object).convention("local")

	@Input
	final Property<String> path = objectFactory.property(String)

	@Input
	final ListProperty<String> args = objectFactory.listProperty(String)

	@Inject
	public ToolConfig(Project project)
	{
		super(project)
	}

	void arg(Object... k)
	{
		args.addAll(k.collect { String.valueOf(it) })
	}
}

@CompileStatic
abstract class NodeConfig extends ToolConfig
{
	@Inject
	NodeConfig(Project project)
	{
		super(project)

		path.convention(project.provider {
			if(version.get() == "local")
			{
				if(!Utils.resolvePath("node"))
				{
					throw new IllegalStateException("Could not find node in \$PATH")
				}

				return "node"
			}

			return Utils.getExt(project).toolsDirectory.file("node/bin/node").get().asFile.absolutePath
		})
	}
}

@CompileStatic
abstract class PnpmConfig extends ToolConfig
{
	@Inject
	PnpmConfig(Project project)
	{
		super(project)

		path.convention(project.provider {
			if(version.get() == "local")
			{
				if(!Utils.resolvePath("pnpm"))
				{
					throw new IllegalStateException("Could not find pnpm in \$PATH")
				}

				return "pnpm"
			}

			return Utils.getExt(project).toolsDirectory.file("pnpm/bin/pnpm.js").get().asFile.absolutePath
		})
	}
}

@CompileStatic
abstract class PatchConfig extends NamedJsToolkitModel
{
	@Input
	final ListProperty<Object> apply = objectFactory.listProperty(Object)

	@Input
	final Property<Object> directory = objectFactory.property(Object)

	@Inject
	PatchConfig(String name, Project project)
	{
		super(name, project)
	}

	void apply(Object patch)
	{
		apply.add(patch)
	}
}
