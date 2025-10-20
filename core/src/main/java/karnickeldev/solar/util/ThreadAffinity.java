package karnickeldev.solar.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.NativeLongByReference;

/**
 * @author : KarnickelDev
 * @since : 19.10.2025
 **/
public interface ThreadAffinity extends Library {

    boolean THREAD_AFFINITY_ENABLED = true;

    ThreadAffinity INSTANCE = Native.load(Platform.isWindows() ? "kernel32" : "c", ThreadAffinity.class);

    // Linux / macOS
    int sched_setaffinity(int pid, int cpusetsize, NativeLongByReference mask);

    // Windows
    WinNT.HANDLE GetCurrentThread();
    long SetThreadAffinityMask(WinNT.HANDLE hThread, long dwThreadAffinityMask);

    static void pinToCore(int coreId) {
        if(!THREAD_AFFINITY_ENABLED) return;

        if(Platform.isWindows()) {
            var h = ThreadAffinity.INSTANCE.GetCurrentThread();
            long mask = 1L << coreId;
            long prev = ThreadAffinity.INSTANCE.SetThreadAffinityMask(h, mask);
            if(prev == 0) {
                Logger.error("Setting Thread Affinity failed: " + Native.getLastError());
            }
        } else {
            long mask = 1L << coreId;
            var ref = new NativeLongByReference(new NativeLong(mask));
            int pid = 0;
            int res = ThreadAffinity.INSTANCE.sched_setaffinity(pid, Long.BYTES, ref);
            if (res != 0) {
                Logger.error("Setting Thread Affinity failed: " + Native.getLastError());
            }
        }
    }

}
