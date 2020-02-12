package ee.keel.gradle;

import java.io.IOException;
import java.io.OutputStream;

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;

public class StreamLogger extends OutputStream
{
	protected final byte[] buf;
	protected int ptr = 0;

	protected final Logger logger;
	protected final LogLevel level;

	public StreamLogger(Logger logger, LogLevel level, int bufferSize)
	{
		this.buf = new byte[bufferSize];
		this.logger = logger;
		this.level = level;
	}

	public StreamLogger(Logger logger, LogLevel level)
	{
		this(logger, level, 4096);
	}

	@Override
	public void write(int b) throws IOException
	{
		if(b == '\n')
		{
			flush();
		}
		else
		{
			if(ptr == buf.length)
			{
				flush();
			}

			buf[ptr++] = (byte)b;
		}
	}

	@Override
	public void flush() throws IOException
	{
		if(ptr > 0)
		{
			logger.log(level, new String(buf, 0, ptr));
			ptr = 0;
		}
	}

	@Override
	public void close() throws IOException
	{
		flush();
	}
}
