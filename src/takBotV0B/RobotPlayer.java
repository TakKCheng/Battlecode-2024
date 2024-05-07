package takBotV0B;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {


    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);
    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final int DIRECTIONSLENGTHMAX = 7;
    static Direction facingDirection = directions[0];

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {


        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.


            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // Make sure you spawn your robot in before you attempt to take any actions!
                // Robots not spawned in do not have vision of any tiles and cannot perform any actions.
                if (!rc.isSpawned()){
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    // Pick a random spawn location to attempt spawning in.
                    MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
                    if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
                }
                else{
                    if(rc.getRoundNum() == 1){

                    }
                    if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS){
                        if(rc.isMovementReady()){
                            setupMovement(rc);
                        }

                    }
                    else{
                        //move toward enemy if it has flag


                        if (rc.canPickupFlag(rc.getLocation())){
                            rc.pickupFlag(rc.getLocation());
                            rc.setIndicatorString("Holding a flag!");
                        }
                        if(!rc.hasFlag()) {
                            moveTowardFlag(rc);
                        }
                        // If we are holding an enemy flag, singularly focus on moving towards
                        // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
                        // to make sure setup phase has ended.
                        if (rc.hasFlag()){
                            MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                            MapLocation firstLoc = spawnLocs[0];
                            objectiveMovement(rc, firstLoc);
                        }
                        if(rc.isActionReady()){
                            attackNearby(rc);
                        }
                        // We can also move our code into different methods or classes to better organize it!
                    }
                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    //rotate direction clockwise
    public static Direction turnClockwise() throws GameActionException{
        //change direction into an the index
        switch (facingDirection){
            case NORTH:
                return Direction.NORTHEAST;
            case NORTHEAST:
                return Direction.EAST;
            case EAST:
                return Direction.SOUTHEAST;
            case SOUTHEAST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.SOUTHWEST;
            case SOUTHWEST:
                return Direction.WEST;
            case WEST:
                return Direction.NORTHWEST;
            case NORTHWEST:
                return Direction.NORTH;
            default:
                return facingDirection;
        }
    }

    //rotate direction clockwise
    public static Direction turnCounterClockwise() throws GameActionException{
        //change direction into an the index
        switch (facingDirection){
            case NORTH:
                return Direction.NORTHWEST;
            case NORTHEAST:
                return Direction.NORTH;
            case EAST:
                return Direction.NORTHEAST;
            case SOUTHEAST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.SOUTHWEST;
            case SOUTHWEST:
                return Direction.WEST;
            case WEST:
                return Direction.NORTHWEST;
            case NORTHWEST:
                return Direction.NORTH;
            default:
                return facingDirection;
        }
    }

    //bot action when blocked by lake or wall
    public static void blockedPath(RobotController rc) throws GameActionException{
        MapInfo facingLocation = rc.senseMapInfo(rc.adjacentLocation(facingDirection));
        if(facingLocation.isWater()){
            if (rc.getCrumbs() > GameConstants.FILL_COST) {
                rc.fill(facingLocation.getMapLocation());
            }
        }
        else {
            facingDirection = turnClockwise();
        }
    }



    //move toward enemy flag
    public static void moveTowardFlag(RobotController rc) throws GameActionException{
        MapLocation[] objflag = rc.senseBroadcastFlagLocations();
        if(objflag.length != 0){
            objectiveMovement(rc,objflag[0]);
        }
    }

    //bot attack nearby enemy
    public static void attackNearby(RobotController rc) throws GameActionException{
        RobotInfo[] bot = rc.senseNearbyRobots(4);

        for (RobotInfo robotInfo : bot) {
            if (rc.canAttack(robotInfo.getLocation())) {
                rc.attack(robotInfo.getLocation());

            }
        }


    }

    //move the bot in a certain direction
    public static void objectiveMovement(RobotController rc, MapLocation objective) throws  GameActionException{
        facingDirection = rc.getLocation().directionTo(objective);
        MapInfo facingLocation = rc.senseMapInfo(rc.adjacentLocation(facingDirection));
            if (rc.canMove(facingDirection)) {
                rc.move(facingDirection);
            }
            else if(facingLocation.isWater() && !rc.hasFlag()){
                if (rc.getCrumbs() > GameConstants.FILL_COST && rc.isActionReady()) {
                    rc.fill(facingLocation.getMapLocation());
                }
                else{
                    return;
                }
            }
            else if(!rc.hasFlag()) {
                facingDirection = turnClockwise();
                if (rc.canMove(facingDirection)) {
                    rc.move(facingDirection);
                }
            }
            else{
                facingDirection = turnClockwise();
            }
            if (rc.canMove(facingDirection)) {
            rc.move(facingDirection);
            }
    }

    //random movement
    public static void defaultMovement(RobotController rc) throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)){
            rc.move(dir);
        }
    }

    //bot gather crumbs in first 200 rounds
    public static void setupMovement(RobotController rc) throws GameActionException{
        MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
        if (crumbs.length != 0) {
            MapLocation firCrumb = crumbs[0];
            objectiveMovement(rc, firCrumb);
        }
        defaultMovement(rc);
    }

    //bot fill in water
    public static void setupActions(RobotController rc) throws GameActionException {
        MapInfo[] lakes = rc.senseNearbyMapInfos(2);
        for (MapInfo lake : lakes) {
            if (rc.isActionReady()) {
                if (lake.isWater() && rc.getCrumbs() > GameConstants.FILL_COST) {
                    rc.fill(lake.getMapLocation());
                }
            } else {
                break;
            }
        }

    }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)){
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }
}
