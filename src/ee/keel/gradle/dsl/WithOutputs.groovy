package ee.keel.gradle.dsl

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.provider.DefaultProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

import groovy.transform.CompileStatic

@CompileStatic
trait WithOutputs implements IJsToolkitModel
{
	@Input
	final Property<NamedDomainObjectContainer<OutputConfig>> outputs = new DefaultProperty<>(NamedDomainObjectContainer)

	void outputs(Closure c)
	{
		if(!outputs.present)
		{
			outputs.value(objectFactory.domainObjectContainer(OutputConfig, { name -> objectFactory.newInstance(OutputConfig, name, project) }))
		}

		outputs.get().configure(c)
	}
}
