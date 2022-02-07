package algorithm;

import java.util.ArrayList;
import java.util.List;
import models.Station;
import models.MRT;

public class NearestTrainAlgorithm {

    /** Used to tell the algorithm the user is moving forwards. */
    public static final int MOVING_FORWARDS = 1;

    /** Used to tell the algorithm the user is moving backwards. */
    public static final int MOVING_BACKWARDS = 0;

    /**
     * Used to find the next and following trains on a given line in a given direction.
     *
     * @param direction The direction the user is moving in (1 means the user is moving
     *                 forwards, 0 means the user is moving backwards).
     * @param currentStation The station to check from.
     * @param stations List of stations on a given line (from the lowest line code to
     *                 the highest).
     * @param trains List of all the trains on a given line.
     */
    // TODO: Make the MRT class a subclass of an abstract class tentatively named PassengerTrain.
    public static MRT[] nextAndFollowing(int direction, Station currentStation, List<Station> stations, List<MRT> trains) {
        final MRT[] nextAndUpcomingTrain = new MRT[2];
        final List<MRT> trainsToCheck = new ArrayList<>(3);

        /*
         * From here onwards, the forward variable serves an additional purpose.
         * Whereas the forward variable was once used to determine the direction the
         * user is travelling in, it is now also used to determine the direction the
         * we will be 'counting' in.
         *
         * If the station the user selected is either the first or last station of a
         * given line, the direction they are going in is set to backwards or forwards
         * respectively.
         */
        for (int i = 0; i < stations.size(); i++) {
            
            if (currentStation.equals(stations.get(stations.size() - 1))) {
                direction = 1;
            } else if (currentStation.equals(stations.get(0))) {
                direction = 0;
            }
        }

        /*
         * User is moving backwards. We count from the beginning of the line and
         * continue doing so until we find the station the user is at. Then, we check
         * if there are trains at each subsequent station while incrementing the
         * distance variable for each station checked. If there is a train and the
         * direction of the train is backwards (0), add it and the current value of
         * distance to the list of trains to check against (trainAtStation).
         */
        if (direction == 0) {
               
            for (MRT train : trains) {
                int distance = 0;
                boolean found = false;
                
                for (Station station : stations) {

                    found = currentStation.equals(station) ? true : found;

                    if (train.getStation().equals(station) && found && train.getDirection() == 0) {
                        train.setDistance(distance);
                        trainsToCheck.add(train);

                        break;
                    }
                    
                    distance = found ? distance + 1 : distance;
                }
            }

            /*
             * This variable stores the name of the train that is closest to the
             * source station. [*]
             */
            MRT nextTrain = null;

            // Determine the next and upcoming train.
            for (int i = 0; i < 2; i++) {
                int lowestDistance = Integer.MAX_VALUE;
                // Stores the name of the closest train.
                MRT closestTrain = null;

                for (MRT train : trainsToCheck) {
                    /*
                     * If the distance of the current train from the source station is
                     * lower than the current lowest distance and the train's distance
                     * is not 0, the lowest distance is updated and the closest train
                     * will be the current train. [*]
                     */
                    if (train.getDistance() < lowestDistance && train.getDistance() > 0) {

                        /*
                         * If the current train is already the 'next' train, it is
                         * ignored. [*]
                         */
                        if (!train.equals(nextTrain)) {
                            lowestDistance = train.getDistance();
                            closestTrain = train;
                        }
                    }
                }

                /*
                 * If this is the first iteration of the for loop, the closest train
                 * will become the next train. [*]
                 */
                if (i == 0) {
                    nextTrain = closestTrain;
                }

                /*
                 * The first element in the nextAndUpcomingTrain array will be the
                 * name of next train. The second element will be the name of the
                 * upcoming train. [*]
                 */
                nextAndUpcomingTrain[i] = closestTrain != null ? closestTrain : null;
            }
        } else {
            /*
             * User is moving forwards. We count from the end of the line and continue
             * doing so until we find the station the user is at. Then, we check if there
             * are trains at each subsequent station while incrementing the distance
             * variable for each station checked. If there is a train and the direction of
             * the train is forwards (1), add it and the current value of distance to the
             * list of trains to check against (trainAtStation).
             */

            for (int i = trains.size() - 1; i >= 0; i--) {
                int distance = 0;
                boolean found = false;
                
                for (int j = stations.size() - 1; j >= 0; j--) {

                    found = currentStation.equals(stations.get(j)) ? true : found;

                    if (trains.get(i).getStation().equals(stations.get(j)) && found && trains.get(i).getDirection() == 1) {
                        trains.get(i).setDistance(distance);
                        trainsToCheck.add(trains.get(i));
                        
                        break;
                    }
                    
                    distance = found ? distance + 1 : distance;
                }
            }

            /*
             * Determine the next and upcoming train. Refer to the comments marked [*]
             * for an explanation.
             */
            MRT nextTrain = null;

            for (int i = 0; i < 2; i++) {
                int lowestDistance = Integer.MAX_VALUE;
                MRT closestTrain = null;

                for (MRT train : trainsToCheck) {
                
                    if (train.getDistance() < lowestDistance && train.getDistance() > 0) {
                    
                        if (!train.equals(nextTrain)) {
                            lowestDistance = train.getDistance();
                            closestTrain = train;
                        }
                    }
                }

                if (i == 0) {
                    nextTrain = closestTrain;
                }

                nextAndUpcomingTrain[i] = closestTrain != null ? closestTrain : null;
            }
        }

        return nextAndUpcomingTrain;
    }
}