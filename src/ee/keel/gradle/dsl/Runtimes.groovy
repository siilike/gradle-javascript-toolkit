package ee.keel.gradle.dsl

import javax.inject.Inject

import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile

import groovy.transform.CompileStatic

@CompileStatic
abstract class RuntimeConfig extends NamedJsToolkitModel
{
	@InputFile
	final RegularFileProperty template = objectFactory.fileProperty()

	@Input
	final MapProperty<String, Object> templateVars = objectFactory.mapProperty(String, Object)

	@Input
	final Property<String> indexFileName = objectFactory.property(String)

	@Input
	final ListProperty<String> modules = objectFactory.listProperty(String)

	@Input
	final ListProperty<String> outputs = objectFactory.listProperty(String)

	@Input
	final Property<String> defaultPreset = objectFactory.property(String)

	@Input
	final Property<DistributionConfig> distribution = objectFactory.property(DistributionConfig)

	@Inject
	public RuntimeConfig(String name, Project project)
	{
		super(name, project)

		indexFileName.convention("index.php")
		modules.convention(project.provider { [ name ] })
		outputs.convention(project.provider { [ "client" ] })
		defaultPreset.convention("production")
	}

	void templateVar(String k, Object v)
	{
		templateVars.put(k, v)
	}

	void module(String m)
	{
		modules.add(m)
	}

	void output(String m)
	{
		outputs.add(m)
	}

	void distribution(Closure c)
	{
		if(!distribution.present)
		{
			def a = objectFactory.newInstance(DistributionConfig, name, project)

			a.copy { CopySpec cc ->
				cc.from("${project.buildDir}/runtime/${name}/") {
					cc.include "**"
				}
			}

			distribution.set(a)
		}

		c.delegate = distribution.get()
		c.setResolveStrategy(Closure.DELEGATE_FIRST)
		c()
	}
}
