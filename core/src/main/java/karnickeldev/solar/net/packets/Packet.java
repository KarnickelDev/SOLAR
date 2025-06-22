package karnickeldev.solar.net.packets;

import karnickeldev.solar.net.network.core.Serializable;

public interface Packet extends Serializable {

    int MAX_SEQUENCE = 0xFFFF;

    short getType();

    int getWorldId();

    long getSimTimeMicros();

    int getSequenceId();

    default boolean isFastHandled() {
        return false;
    }

}
