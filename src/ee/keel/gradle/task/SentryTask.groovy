package ee.keel.gradle.task

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input

import ee.keel.gradle.Utils
import ee.keel.gradle.dsl.JsToolkitExtension
import groovy.transform.CompileStatic

@CompileStatic
class SentryTask extends Exec
{
	private final static Logger logger = Logging.getLogger(SentryTask)

	protected JsToolkitExtension jstk = Utils.getExt(project)

	@Input
	final Property<String> token = project.objects.property(String).convention(jstk.sentry.map { it.token.get() })

	@Input
	final Property<String> url = project.objects.property(String).convention(jstk.sentry.map { it.url.get() })

	@Input
	final Property<String> organization = project.objects.property(String).convention(jstk.sentry.map { it.organization.get() })

	@Input
	final Property<String> projectName = project.objects.property(String).convention(jstk.sentry.map { it.projectName.get() })

	@Input
	final Property<String> release = project.objects.property(String).convention(jstk.sentry.map { it.release.get() })

	@Input
	final RegularFileProperty cli = project.objects.fileProperty().convention(jstk.sentry.map { it.cli.get() })

	public SentryTask()
	{
		super();

		configure {
			setWorkingDir(project.buildDir)
			setExecutable(cli.get().asFile)
		}

		logging.captureStandardOutput(LogLevel.LIFECYCLE)
		logging.captureStandardError(LogLevel.WARN)
	}

	@Override
	protected void exec()
	{
		environment "SENTRY_NO_PROGRESS_BAR", "1"
		environment "SENTRY_DISABLE_UPDATE_CHECK", "1"
		environment "SENTRY_LOG_LEVEL", logger.isDebugEnabled() ? "debug" : "info"

		environment "SENTRY_AUTH_TOKEN", token.get()
		environment "SENTRY_URL", url.get()
		environment "SENTRY_ORG", organization.get()
		environment "SENTRY_PROJECT", projectName.get()
		environment "SENTRY_RELEASE", release.get()

		super.exec();
	}
}
