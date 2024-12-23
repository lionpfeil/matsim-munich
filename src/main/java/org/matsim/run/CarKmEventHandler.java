package org.matsim.run;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
public class CarKmEventHandler implements LinkLeaveEventHandler {

    private final Network network;
    private double totalCarMeters = 0.0;

    public CarKmEventHandler(Network network) {
        this.network = network;
    }

    // If your version of LinkLeaveEventHandler does NOT mark this as an @Override, just remove @Override
    @Override
    public void handleEvent(LinkLeaveEvent event) {
        double linkLength = network.getLinks().get(event.getLinkId()).getLength();
        totalCarMeters += linkLength;
    }

    // In older versions, you might not be able to use @Override here if the signature differs.
    // If you get a compile error, remove @Override or rename the parameter.
    @Override
    public void reset(int iteration) {
        totalCarMeters = 0.0;
    }

    public double getTotalCarKilometers() {
        return totalCarMeters / 1000.0;
    }
}