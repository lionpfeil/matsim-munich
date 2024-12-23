package org.matsim.run;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.events.IterationEndsEvent;

public class ScoreSummationEventHandler implements IterationEndsListener {

    private double sumOfSelectedPlanScores = 0.0;

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        // Check if this is the last iteration
        int lastIteration = event.getServices().getConfig().controler().getLastIteration();
        if (event.getIteration() == lastIteration) {
            sumOfSelectedPlanScores = 0.0;  // reset in case the handler is reused

            // Sum the scores of the selected plans of all persons in the population
            for (Person person : event.getServices().getScenario().getPopulation().getPersons().values()) {
                if (person.getSelectedPlan() != null && person.getSelectedPlan().getScore() != null) {
                    sumOfSelectedPlanScores += person.getSelectedPlan().getScore();
                }
            }
        }
    }

    /**
     * Get the sum of the scores of selected plans computed in the last iteration.
     */
    public double getSumOfSelectedPlanScores() {
        return sumOfSelectedPlanScores;
    }
}
