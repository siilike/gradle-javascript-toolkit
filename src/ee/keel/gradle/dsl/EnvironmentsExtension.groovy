package ee.keel.gradle.dsl

import javax.inject.Inject

import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input

import groovy.transform.CompileStatic
import groovy.transform.Internal

@CompileStatic
abstract class EnvironmentsExtension extends JsToolkitModel
{
	@Input
	protected final Map<String, EnvironmentExtension> vars = new LinkedHashMap<>()

	EnvironmentsExtension(Project project)
	{
		super(project)
	}

	@Internal
	def methodMissing(String name, Object args)
	{
		def a = objectFactory.newInstance(EnvironmentExtension, name, project)

		def c = ((Object[]) args)[0] as Closure
		c.delegate = a
		c.setResolveStrategy(Closure.DELEGATE_FIRST)
		c()

		vars.put(name, a)
	}

	@Internal
	EnvironmentExtension propertyMissing(String k)
	{
		return vars.get(k)
	}

	EnvironmentExtension getByName(String k)
	{
		return vars.get(k)
	}

	def all(Closure c)
	{
		vars.each { k, v ->
			c.delegate = v
			c.setResolveStrategy(Closure.DELEGATE_FIRST)
			c()
		}

		return vars
	}
}

@CompileStatic
abstract class EnvironmentExtension extends JsToolkitModel
{
	@Input
	final String name

	@Input
	protected final Map<String, ?> vars = new LinkedHashMap<>()

	@Input
	protected final Map<String, Closure> closureVars = new LinkedHashMap<>()

	@Inject
	EnvironmentExtension(String name, Project project)
	{
		super(project)

		this.name = name
	}

	@Internal
	def methodMissing(String k, Object args0)
	{
		Object[] args = (Object[]) args0;

		if(args.length != 1)
		{
			throw new IllegalArgumentException("${k} has too many arguments")
		}

		vars.remove(k)
		closureVars.put(k, (Closure) args[0])
	}

	@Internal
	void propertyMissing(String k, Object v)
	{
		closureVars.remove(k)
		vars.put(k, v)
	}

	@Internal
	def propertyMissing(String k)
	{
		if(vars.containsKey(k))
		{
			return vars.get(k)
		}
		else if(closureVars.containsKey(k))
		{
			def ret = closureVars.get(k)
			ret.delegate = this
			ret.setResolveStrategy(Closure.DELEGATE_FIRST)
			return ret()
		}

		throw new MissingPropertyException("${name} has no variable named ${k}")
	}

	def getByName(String k)
	{
		return propertyMissing(k)
	}

	def getVars()
	{
		return vars
	}

	def getClosureVars()
	{
		return vars
	}
}

@CompileStatic
class EnvironmentHelper
{
	protected final Provider<EnvironmentExtension> provider

	public EnvironmentHelper(Provider<EnvironmentExtension> provider)
	{
		this.provider = provider
	}

	def getProvider()
	{
		return provider
	}

	def propertyMissing(String k)
	{
		return provider.get().getByName(k)
	}
}
