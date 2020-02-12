package ee.keel.gradle.dsl

import javax.inject.Inject

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.util.PatternSet

import groovy.transform.CompileStatic

@CompileStatic
abstract class ModuleConfig extends NamedJsToolkitModel implements WithOutputs, WithIncludes
{
	@Input
	final ListProperty<String> libraries = objectFactory.listProperty(String)

	@Input
	final Property<BabelConfig> babel = objectFactory.property(BabelConfig)

	@Input
	final MapProperty<String, Object> css = objectFactory.mapProperty(String, Object)

	@Inject
	ModuleConfig(String name, Project project)
	{
		super(name, project)

		babel.convention(objectFactory.newInstance(BabelConfig, project))
	}

	void css(String name, source)
	{
		css.put(name, source)
	}

	void library(String name)
	{
		libraries.add(name)
	}
}