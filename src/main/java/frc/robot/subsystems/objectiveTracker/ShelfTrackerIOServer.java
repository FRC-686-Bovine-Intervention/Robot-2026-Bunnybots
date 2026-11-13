package frc.robot.subsystems.objectiveTracker;

import java.util.Arrays;

import edu.wpi.first.net.WebServer;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.BooleanSubscriber;
import edu.wpi.first.networktables.IntegerArraySubscriber;
import edu.wpi.first.networktables.IntegerArrayPublisher;
import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.IntegerSubscriber;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.wpilibj.Filesystem;

public class ShelfTrackerIOServer implements ShelfTrackerIO {
    private static final String toRobotTable = "/ShelfControls/ToRobot";
    private static final String toDashboardTable = "/ShelfControls/ToDashboard";

    private static final String modeTopicName = "Mode";
    private static final String carrotGoalTopicName = "CarrotGoal";
    private static final String carrotCakeGoalTopicName = "CarrotCakeGoal";

    private static final String shelfTopicName = "shelf";
    private static final String ovenCarrotsTopicName = "OvenCarrots";
    private static final String ovenCakesTopicName = "OvenCakes";
    private static final String bakerModeTopicName = "BakerMode";
    private static final String priorityListTopicName = "PriorityList";

    private final IntegerSubscriber modeSubscriber;
    private final IntegerSubscriber carrotGoalSubscriber;
    private final IntegerSubscriber carrotCakeGoalSubscriber;

    private final IntegerArraySubscriber shelfSubscriber;
    private final IntegerSubscriber ovenCarrotsCountSubscriber;
    private final IntegerSubscriber ovenCakesCountSubscriber;
    private final BooleanSubscriber bakerModeSubscriber;
    private final IntegerArraySubscriber priorityListSubscriber;

    private final IntegerPublisher modePublisher;
    private final IntegerPublisher carrotGoalPublisher;
    private final IntegerPublisher carrotCakeGoalPublisher;

    private final IntegerArrayPublisher shelfPublisher;
    private final IntegerPublisher ovenCarrotsCountPublisher;
    private final IntegerPublisher ovenCakesCountPublisher;
    private final BooleanPublisher bakerModePublisher;
    private final IntegerPublisher priorityListPublisher;
    
    public ShelfTrackerIOServer() {
        System.out.println("[Init] Creating ShelfTrackerIOServer");

        WebServer.start(5801, Filesystem.getDeployDirectory().getPath() + "/shelf_tracker");

        var inputTable = NetworkTableInstance.getDefault().getTable(toRobotTable);

        modeSubscriber = inputTable
            .getIntegerTopic(modeTopicName)
            .subscribe(0, PubSubOption.keepDuplicates(true));
        
        carrotGoalSubscriber = inputTable
            .getIntegerTopic(carrotGoalTopicName)
            .subscribe(0, PubSubOption.keepDuplicates(true));

        carrotCakeGoalSubscriber = inputTable
            .getIntegerTopic(carrotCakeGoalTopicName)
            .subscribe(0, PubSubOption.keepDuplicates(true));
        
        shelfSubscriber = inputTable
            .getIntegerArrayTopic(shelfTopicName)
            .subscribe(new long[0], PubSubOption.keepDuplicates(true));
        
        ovenCarrotsCountSubscriber = inputTable
            .getIntegerTopic(ovenCarrotsTopicName)
            .subscribe(0, PubSubOption.keepDuplicates(true));
        
        ovenCakesCountSubscriber = inputTable
            .getIntegerTopic(ovenCakesTopicName)
            .subscribe(0, PubSubOption.keepDuplicates(true));
        
        bakerModeSubscriber = inputTable
            .getBooleanTopic(bakerModeTopicName)
            .subscribe(false, PubSubOption.keepDuplicates(true));
        
        priorityListSubscriber = inputTable
            .getIntegerArrayTopic(priorityListTopicName)
            .subscribe(new long[] {}, PubSubOption.keepDuplicates(true));

        
        var outputTable = NetworkTableInstance.getDefault().getTable(toDashboardTable);

        modePublisher = outputTable.getIntegerTopic(modeTopicName).publish();
        carrotGoalPublisher = outputTable.getIntegerTopic(carrotGoalTopicName).publish();
        carrotCakeGoalPublisher = outputTable.getIntegerTopic(carrotCakeGoalTopicName).publish();

        shelfPublisher = outputTable.getIntegerArrayTopic(shelfTopicName).publish();
        ovenCarrotsCountPublisher = outputTable.getIntegerTopic(ovenCarrotsTopicName).publish();
        ovenCakesCountPublisher = outputTable.getIntegerTopic(ovenCakesTopicName).publish();
        bakerModePublisher = outputTable.getBooleanTopic(bakerModeTopicName).publish();
        priorityListPublisher = outputTable.getIntegerTopic(priorityListTopicName).publish();
    }

    @Override
    public void updateInputs(ShelfTrackerIOInputs inputs) {
        if (modeSubscriber.readQueue().length > 0) {
            inputs.mode = (int) modeSubscriber.get();
        }
        if (carrotGoalSubscriber.readQueue().length > 0) {
            inputs.carrotGoal = (int) carrotGoalSubscriber.get();
        }
        if (carrotCakeGoalSubscriber.readQueue().length > 0) {
            inputs.carrotCakeGoal = (int) carrotCakeGoalSubscriber.get();
        }

        inputs.shelfQueue = shelfSubscriber.readQueueValues();
        inputs.ovenCarrotsQueue = ovenCarrotsCountSubscriber.readQueueValues();
        inputs.ovenCakesQueue = ovenCakesCountSubscriber.readQueueValues();
        inputs.bakerMode = bakerModeSubscriber.readQueueValues();
        
        inputs.priorityListQueue = new int[0][0];
        var priorityListQueueValues = priorityListSubscriber.readQueueValues();
        if (priorityListQueueValues.length > 0) {
            for (int i = 0; i < priorityListQueueValues.length; i++) {
                var swaps = priorityListQueueValues[i];
                inputs.priorityListQueue = Arrays.copyOf(inputs.priorityListQueue, inputs.priorityListQueue.length + swaps.length);
                for (int j = 0; j < swaps.length; j++) {
                    var swap = (int) swaps[j];
                    inputs.priorityListQueue[inputs.priorityListQueue.length - swaps.length + j] = new int[] {(swap >> 3) & 0b111, swap & 0b111};
                }
            }
        }
    }

    @Override
    public void setMode(int value) {
        modePublisher.set(value);
    }

    @Override
    public void setCarrotGoal(int value) {
        carrotGoalPublisher.set(value);
    }

    @Override
    public void setCarrotCakeGoal(int value) {
        carrotCakeGoalPublisher.set(value);
    }

    @Override
    public void setShelfState(int[] value) {
        long[] shelf = Arrays.stream(value).asLongStream().toArray();
        shelfPublisher.set(shelf);
    }

    @Override
    public void setOvenCarrotsCount(int value) {
        ovenCarrotsCountPublisher.set(value);
    }

    @Override
    public void setOvenCakesCount(int value) {
        ovenCakesCountPublisher.set(value);
    }

    @Override
    public void setBakerModeState(boolean value) {
        bakerModePublisher.set(value);
    }

    @Override
    public void setPriorityList(int[] value) {
        int n = 0; //32 bit
        for (int b : value) {
            n = (n << 4) | b;
        }
        priorityListPublisher.set(n);
    }
}