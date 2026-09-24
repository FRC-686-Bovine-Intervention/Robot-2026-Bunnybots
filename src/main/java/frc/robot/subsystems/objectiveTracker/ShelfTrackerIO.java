package frc.robot.subsystems.objectiveTracker;

import org.littletonrobotics.junction.AutoLog;

public interface ShelfTrackerIO {
    @AutoLog
    public static class ShelfTrackerIOInputs {
        public int carrotGoal;
        public int carrotCakeGoal;

        public long[] carrotQueue = new long[0];
        public long[] carrotCakeQueue = new long[0];

        public long[] ovenCarrotsQueue = new long[0];
        public long[] ovenCakesQueue = new long[0];
        public int[][] priorityListQueue = new int[0][0];
        public boolean[] bakerMode = new boolean[0];

        public int mode = 0;
    }
    
    public default void updateInputs(ShelfTrackerIOInputs inputs) {}

    default void setMode(int value) {}
    default void setCarrotGoal(int value) {}
    default void setCarrotCakeGoal(int value) {}
    
    default void setCarrotState(boolean[] value) {}
    default void setCarrotCakeState(boolean[] value) {}
    default void setOvenCarrotsCount(int value) {}
    default void setOvenCakesCount(int value) {}
    default void setBakerModeState(boolean value) {}
    default void setPriorityList(int[] value) {}
}
