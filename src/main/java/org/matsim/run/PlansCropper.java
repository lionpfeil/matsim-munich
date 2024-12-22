package org.matsim.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashSet;
import java.util.Set;

public class PlansCropper {

    // Files
    private static final String configFile          = "/Users/lion.pfeil/IdeaProjects/matsim-munich/scenarios/tumTbBase/configBase.xml";
    private static final String inputPlansFile      = "/Users/lion.pfeil/IdeaProjects/matsim-munich/original-input-data/munich-v1.0-5pct.plans.xml.gz";
    private static final String outputPlansFile     = "/Users/lion.pfeil/IdeaProjects/matsim-munich/original-input-data/cropped_munich-v1.0-5pct.plans.xml.gz";

    // Bounding box
    private static final double minX = 4465854.0;
    private static final double maxX = 4475738.0;
    private static final double minY = 5330749.0;
    private static final double maxY = 5337178.0;

    public static void main(String[] args) {

        // 1) Load your existing Config
        Config config = ConfigUtils.loadConfig(configFile);

        // 2) Create a fresh (empty) Scenario using your config
        //    (this will not automatically load a population or network from config)
        Scenario scenario = ScenarioUtils.createScenario(config);

        // 3) Read the population (plans) file you want to crop
        new PopulationReader(scenario).readFile(inputPlansFile);

        // 4) Crop the plans
        cropPlans(scenario.getPopulation(), minX, maxX, minY, maxY);

        // 5) Write out the cropped plans
        new PopulationWriter(scenario.getPopulation()).write(outputPlansFile);
        System.out.println("Cropped plans written to: " + outputPlansFile);
    }

    /**
     * Removes persons whose ANY plan contains ANY activity outside the bounding box.
     *
     * @param population MATSim population to crop
     * @param minX min X boundary
     * @param maxX max X boundary
     * @param minY min Y boundary
     * @param maxY max Y boundary
     */
    private static void cropPlans(Population population,
                                  double minX, double maxX,
                                  double minY, double maxY) {

        Set<Person> personsToRemove = new HashSet<>();

        for (Person person : population.getPersons().values()) {
            boolean removeThisPerson = false;

            // Check each plan
            for (Plan plan : person.getPlans()) {
                // Check each plan element
                for (PlanElement pe : plan.getPlanElements()) {
                    if (pe instanceof Activity) {
                        Activity act = (Activity) pe;
                        // If this activity's coordinates are out of bounds, mark person for removal
                        if (isOutsideBoundingBox(act, minX, maxX, minY, maxY)) {
                            removeThisPerson = true;
                            break; // Stop checking further elements in this plan
                        }
                    }
                }
                if (removeThisPerson) {
                    break; // Stop checking further plans if one plan is out-of-bounds
                }
            }

            if (removeThisPerson) {
                personsToRemove.add(person);
            }
        }

        // Remove the identified persons from the population
        for (Person p : personsToRemove) {
            population.removePerson(p.getId());
        }

        System.out.println("Removed " + personsToRemove.size() + " persons out of "
                + (population.getPersons().size() + personsToRemove.size())
                + " because they have activities outside the bounding box.");
    }

    /**
     * Helper method to check if an activity’s coordinates are out of the bounding box.
     */
    private static boolean isOutsideBoundingBox(Activity act,
                                                double minX, double maxX,
                                                double minY, double maxY) {
        if (act.getCoord() == null) {
            // If there's no direct coordinate in the activity, you could:
            // - skip it,
            // - or retrieve the link's coord from a network,
            // - or remove it by default if no location info is available.
            // Here, let's assume we remove it if we have no coordinate.
            return true;
        }

        double x = act.getCoord().getX();
        double y = act.getCoord().getY();
        return (x < minX || x > maxX || y < minY || y > maxY);
    }
}