package ee.keel.gradle.task

class Deb extends com.netflix.gradle.plugins.deb.Deb
{
	@Override
	public String assembleArchiveName()
	{
		return getArchiveBaseName().get() + "." + getArchiveExtension().get()
	}
}

class Rpm extends com.netflix.gradle.plugins.rpm.Rpm
{
	@Override
	public String assembleArchiveName()
	{
		return getArchiveBaseName().get() + "." + getArchiveExtension().get()
	}
}
