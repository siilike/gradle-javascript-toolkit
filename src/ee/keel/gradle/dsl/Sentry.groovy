package ee.keel.gradle.dsl

import javax.inject.Inject

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

import ee.keel.gradle.Utils
import groovy.transform.CompileStatic

@CompileStatic
abstract class SentryConfig extends JsToolkitModel
{
	@Input
	final Property<Boolean> enabled = objectFactory.property(Boolean)

	@Input
	final Property<String> token = objectFactory.property(String)

	@Input
	final Property<String> url = objectFactory.property(String)

	@Input
	final Property<String> organization = objectFactory.property(String)

	@Input
	final Property<String> projectName = objectFactory.property(String)

	@Input
	final Property<String> release = objectFactory.property(String)

	@Input
	final RegularFileProperty cli = objectFactory.fileProperty()

	@Inject
	public SentryConfig(Project project)
	{
		super(project)

		enabled.convention(project.provider { Utils.getExt(project).environment.get() == "production" })
		projectName.convention(project.provider { Utils.getExt(project).name.get() })
		release.convention(project.provider { projectName.get()+"@"+Utils.getExt(project).version.get() })
		cli.convention(project.provider { Utils.getExt(project).toolsDirectory.file("node_modules/@sentry/cli/bin/sentry-cli").get() })
	}
}
