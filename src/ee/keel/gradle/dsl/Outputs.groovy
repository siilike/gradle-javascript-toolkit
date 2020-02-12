package ee.keel.gradle.dsl

import javax.inject.Inject

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input

import groovy.transform.CompileStatic

@CompileStatic
abstract class OutputConfig extends NamedJsToolkitModel
{
	@Input
	final Property<Boolean> enabled = objectFactory.property(Boolean)

	@Input
	final Property<Boolean> minify = objectFactory.property(Boolean)

	@Input
	final SetProperty<String> presets = objectFactory.setProperty(String)

	@Inject
	OutputConfig(String name, Project project)
	{
		super(name, project)

		enabled.convention(true)
		minify.convention(true)
		presets.convention([ "production" ])
	}

	void preset(String... name)
	{
		presets.addAll(name)
	}
}
