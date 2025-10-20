package karnickeldev.solar.render.orbitupdate;

import karnickeldev.solar.ecs.ClientECS;

/**
 * @author : KarnickelDev
 * @since : 21.10.2025
 **/
public record OrbitFrameContext(
    ClientECS ecs,
    double simTimeSec,
    OrbitUpdater.FrameData nextFrameData
) {}
