package karnickeldev.solar.render.orbitupdate;

import karnickeldev.solar.ecs.ClientECS;

/**
 * @author KarnickelDev
 * @since 21.10.2025
 **/
record OrbitFrameContext(
    ClientECS ecs,
    long simTimeMicros,
    OrbitUpdaterImpl.FrameData nextFrameData
) {}
