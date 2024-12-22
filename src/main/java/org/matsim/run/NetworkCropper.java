package org.matsim.run;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashSet;
import java.util.Set;

public class NetworkCropper {

    // File paths
    private static final String configFile       = "/Users/lion.pfeil/IdeaProjects/matsim-munich/scenarios/tumTbBase/configBase.xml";
    private static final String inputNetworkFile = "/Users/lion.pfeil/IdeaProjects/matsim-munich/original-input-data/munich-v1.0-network.xml.gz";
    private static final String outputNetworkFile= "/Users/lion.pfeil/IdeaProjects/matsim-munich/original-input-data/cropped_munich-v1.0-network.xml.gz";

    // Bounding box
    private static final double minX = 4465854.0;
    private static final double maxX = 4475738.0;
    private static final double minY = 5330749.0;
    private static final double maxY = 5337178.0;

    public static void main(String[] args) {
        // 1) Load your existing Config
        Config config = ConfigUtils.loadConfig(configFile);

        // 2) Create a brand-new (empty) Network so it’s modifiable
        //    (instead of scenario.getNetwork(), which may be unmodifiable).
        Network networkToCrop = NetworkUtils.createNetwork();

        // 3) Read your original network file into this empty network
        new MatsimNetworkReader(networkToCrop).readFile(inputNetworkFile);

        // 4) Crop the network to the given bounding box
        cropNetwork(networkToCrop, minX, maxX, minY, maxY);

        // 5) (Optional) Use the MATSim NetworkCleaner to remove disconnected subnetworks
        //    so that the resulting network is fully connected.
        NetworkCleaner cleaner = new NetworkCleaner();
        cleaner.run(networkToCrop);

        // 6) Write the resulting cropped and cleaned network to file
        new NetworkWriter(networkToCrop).write(outputNetworkFile);
        System.out.println("Cropped and cleaned network written to: " + outputNetworkFile);
    }

    /**
     * Removes all nodes (and their links) from a network that lie
     * outside the specified bounding box.
     */
    private static void cropNetwork(Network network,
                                    double minX, double maxX,
                                    double minY, double maxY) {
        // 1. Collect IDs of nodes to remove
        Set<Id<Node>> nodesToRemove = new HashSet<>();
        for (Node node : network.getNodes().values()) {
            double x = node.getCoord().getX();
            double y = node.getCoord().getY();
            if (x < minX || x > maxX || y < minY || y > maxY) {
                nodesToRemove.add(node.getId());
            }
        }

        // 2. Collect IDs of links that need to be removed
        Set<Id<Link>> linksToRemove = new HashSet<>();
        for (Link link : network.getLinks().values()) {
            if (nodesToRemove.contains(link.getFromNode().getId())
                    || nodesToRemove.contains(link.getToNode().getId())) {
                linksToRemove.add(link.getId());
            }
        }

        // 3. Remove links first (to avoid dangling references)
        for (Id<Link> linkId : linksToRemove) {
            network.removeLink(linkId);
        }

        // 4. Remove the nodes
        for (Id<Node> nodeId : nodesToRemove) {
            network.removeNode(nodeId);
        }
    }
}