package karnickeldev.solar.logging;

import karnickeldev.solar.logging.appender.ConsoleAppender;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author KarnickelDev
 * @since 03.04.2026
 **/
public class LogManager {

    private static final short CAPACITY = 1024;

    private static final BlockingQueue<LogEvent> queue = new ArrayBlockingQueue<>(CAPACITY);
    private static final List<LogAppender> appenders = new CopyOnWriteArrayList<>();

    private static volatile LogLevel currentLevel = LogLevel.INFO;
    private static final AtomicBoolean running = new AtomicBoolean(false);

    private static Thread worker;

    public static void init() {
        if(running.getAndSet(true)) return;

        // add default appender
        addAppender(new ConsoleAppender());

        worker = Thread.ofPlatform().daemon().priority(Thread.NORM_PRIORITY)
            .name("Logging-Thread")
            .start(LogManager::processLoop);

        printHostSpecs();
    }

    public static void shutdown() {
        running.set(false);
        worker.interrupt();
    }

    // config

    public static void setLevel(LogLevel level) {
        currentLevel = level;
    }

    public static boolean isEnabled(LogLevel level) {
        return level.ordinal() >= currentLevel.ordinal();
    }

    public static void addAppender(LogAppender appender) {
        appenders.add(appender);
    }

    // queue
    static void enqueue(LogEvent event) {
        if(!isEnabled(event.level)) return;

        boolean success = queue.offer(event);
        if(!success) {
            // handle, maybe count missed logs
        }
    }

    // worker loop
    private static void processLoop() {
        try {
            while(running.get()) {
                LogEvent event = queue.take();

                for(LogAppender appender : appenders) {
                    appender.append(event);
                }
            }
        } catch (InterruptedException ignored) {}

        // flush remaining messages
        LogEvent event;
        while((event = queue.poll()) != null) {
            for(LogAppender appender : appenders) {
                appender.append(event);
            }
        }
        for(LogAppender appender : appenders) {
            appender.close();
        }
    }

    private static void printHostSpecs() {
        String template = """
            Host specs {
            OS:\s
              Name: {}
              Version: {}
              Architecture: {}
            Java:\s
              Version: {}
              Vendor: {}
              JVMName: {}
            \s
              Memory: {}
              Processors: {}
            }
            """;

        Object[] args = {
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch"),

            System.getProperty("java.version"),
            System.getProperty("java.vendor"),
            System.getProperty("java.vm.name"),

            getMemoryMB() + " MB",
            Runtime.getRuntime().availableProcessors()
        };

        enqueue(new LogEvent(LogLevel.INFO, LogTag.GENERAL.toString(), template, args, null));
    }

    private static long getMemoryMB() {
        long max =  Runtime.getRuntime().maxMemory();
        if(max == Long.MAX_VALUE) {
            return -1;
        } else return max / 1_000_000;
    }

}
