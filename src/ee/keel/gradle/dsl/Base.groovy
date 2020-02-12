package ee.keel.gradle.dsl

import javax.inject.Inject

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory

import groovy.transform.CompileStatic

@CompileStatic
interface IJsToolkitModel
{
	ObjectFactory getObjectFactory()
	ProviderFactory getProviderFactory()
	Project getProject()
}

@CompileStatic
abstract class JsToolkitModel implements IJsToolkitModel
{
	protected final Project project

	public JsToolkitModel(Project project)
	{
		this.project = project
	}

	public JsToolkitModel()
	{
		this(null)
	}

	@Inject
	abstract ObjectFactory getObjectFactory()

	@Inject
	abstract ProviderFactory getProviderFactory()

	Project getProject()
	{
		return project
	}
}

@CompileStatic
abstract class NamedJsToolkitModel extends JsToolkitModel
{
	final String name

	public NamedJsToolkitModel(String name, Project project)
	{
		super(project)

		this.name = name
	}
}
