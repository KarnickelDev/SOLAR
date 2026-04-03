package karnickeldev.solar.logging.appender;

import karnickeldev.solar.logging.LogAppender;
import karnickeldev.solar.logging.LogEvent;
import karnickeldev.solar.logging.LogLevel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author KarnickelDev
 * @since 03.04.2026
 **/
public class FileAppender implements LogAppender {

    private final File file;

    private final BufferedWriter writer;
    private final StringBuilder buffer;
    private long lastFlush;

    public FileAppender(File file) throws IOException {
        this.file = file;

        this.writer = Files.newBufferedWriter(Path.of(file.toURI()),
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        this.buffer = new StringBuilder(1024*8);
        this.lastFlush = System.nanoTime();
    }

    @Override
    public void append(LogEvent e) {

        try {
            String raw_msg = LogFormatter.format(e.template, e.args);
            String msg = "[" + e.level.toString().trim() + "][" + e.tag + "] " + raw_msg + '\n';

            if(buffer.length() + msg.length() >= buffer.capacity()) {
                flushBuffer();
            }

            buffer.append(msg);

            // periodic flush
            if(System.nanoTime() - lastFlush > 3_000_000_000L) {
                flush();
            }

            // flush on error
            if(e.level == LogLevel.ERROR) {
                flush();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void flushBuffer() throws IOException {
        writer.write(buffer.toString());
        buffer.setLength(0);
    }

    public void flush() {
        try {
            flushBuffer();
            writer.flush();
            lastFlush = System.nanoTime();
        } catch (Exception ignored) {}
    }
}
