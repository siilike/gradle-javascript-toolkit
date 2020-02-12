package ee.keel.gradle.dsl

import org.gradle.api.file.FileTreeElement
import org.gradle.api.provider.Property
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

import groovy.transform.CompileStatic

@CompileStatic
trait WithIncludes implements IJsToolkitModel
{
	@Input
	final Property<PatternFilterable> includes = objectFactory.property(PatternFilterable).convention((PatternSet) objectFactory.newInstance(PatternSet))

	void include(String... a)
	{
		includes.get().include(a)
	}

	void include(Iterable<String> a)
	{
		includes.get().include(a)
	}

	void include(Spec<FileTreeElement> a)
	{
		includes.get().include(a)
	}

	void include(Closure a)
	{
		includes.get().include(a)
	}

	void exclude(String... a)
	{
		includes.get().exclude(a)
	}

	void exclude(Iterable<String> a)
	{
		includes.get().exclude(a)
	}

	void exclude(Spec<FileTreeElement> a)
	{
		includes.get().exclude(a)
	}

	void exclude(Closure a)
	{
		includes.get().exclude(a)
	}
}
