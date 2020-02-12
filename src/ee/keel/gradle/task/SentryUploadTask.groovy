package ee.keel.gradle.task

import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import groovy.transform.CompileStatic

@CompileStatic
class SentryUploadTask extends SentryTask
{
	private final static Logger logger = Logging.getLogger(SentryUploadTask)

	public SentryUploadTask()
	{
		super();

		setExecutable("sh")
	}

	@Override
	protected void exec()
	{
		def files = project.fileTree(project.buildDir) { ConfigurableFileTree ft ->
			ft.include "lib/*/*/*.js"
			ft.include "module/*/*/*.js"
		}.files

		logger.lifecycle("Uploading files to Sentry: {}", files)

		def executable = cli.get().asFile.absolutePath

		args "-c", "#!/bin/sh\nset -e -u -x\n" + files.collect { "${executable} releases files \"${release.get()}\" upload \"${it.absolutePath}\" '~/${it.name}'" }.join("\n")

		super.exec();
	}
}
