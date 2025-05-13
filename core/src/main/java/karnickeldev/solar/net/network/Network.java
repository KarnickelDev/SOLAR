package karnickeldev.solar.net.network;

public interface Network {

    /**
     * Sends out buffered packets and polls incoming packets
     *
     * @return True if any work was done
     */
    boolean updateNetwork();

}
