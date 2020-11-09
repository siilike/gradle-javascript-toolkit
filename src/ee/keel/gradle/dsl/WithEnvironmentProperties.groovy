package ee.keel.gradle.dsl

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@CompileStatic
trait WithEnvironmentProperties
{
	private final Map<String, ? extends Provider> envProps = [:]

	@CompileDynamic
	void environmentProperty(String k, Property v)
	{
		envProps.put(k, v)

		if(v instanceof DirectoryProperty)
		{
			inputs.dir v
		}
		else if(v instanceof RegularFileProperty)
		{
			inputs.file v
		}
		else
		{
			inputs.property k, v
		}
	}

	@CompileDynamic
	void environmentProvider(String k, Provider v)
	{
		envProps.put(k, v)
		inputs.property k, v
	}

	@CompileDynamic
	void environmentDirProvider(String k, Provider<Directory> v)
	{
		envProps.put(k, v)
		inputs.dir v
	}

	@CompileDynamic
	void environmentFileProvider(String k, Provider<RegularFile> v)
	{
		envProps.put(k, v)
		inputs.file v
	}

	@CompileDynamic
	void applyEnvironmentProperties()
	{
		envProps.each { k, v ->
			applyEnvironmentProperty(k, v)
		}
	}

	@CompileDynamic
	void applyEnvironmentProperty(k, v)
	{
		def a = v.getOrElse("")

		if(a instanceof File)
		{
			environment k, a.absolutePath
		}
		else if(a instanceof FileSystemLocation)
		{
			environment k, a.asFile.absolutePath
		}
		else
		{
			environment k, a
		}
	}
}
