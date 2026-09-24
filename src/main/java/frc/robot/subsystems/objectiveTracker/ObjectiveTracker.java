package frc.robot.subsystems.objectiveTracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.Pair;
import frc.robot.subsystems.objectiveTracker.ObjectiveTracker.CarrotGoal;
import frc.util.VirtualSubsystem;

public class ObjectiveTracker extends VirtualSubsystem {
    private final ShelfTrackerIO io;
    private final ShelfTrackerIOInputsAutoLogged inputs = new ShelfTrackerIOInputsAutoLogged();

    public static enum CarrotGoal {
        SHELF,
        OVEN
        ;
    }

    public static enum Mode {
        Smart,
        Dumb
    }

    public static enum Priority {
        Level3Fill(Optional.of(ShelfLevel.Level3), false, false) {
            @Override
            public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 10; i < 15; i++) {
                    if (carrotStates[i] = false) return false;
                }
                return true;
            }
        },
        Level2Fill(Optional.of(ShelfLevel.Level2), false, false) {
            @Override
            public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 5; i < 10; i++) {
                    if (carrotStates[i] = false) return false;
                }
                return true;
            }
        },
        Level1Fill(Optional.of(ShelfLevel.Level1), false, false) {
            @Override
            public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 0; i < 5; i++) {
                    if (carrotStates[i] = false) return false;
                }
                return true;
            }
        },
        OvenSpam(Optional.empty(), false, false) {
            @Override
            public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
                return ovenCarrotsCount + ovenCakesCount > 5;
            }
        },
        Level3StockedUp(Optional.of(ShelfLevel.Level3), true, false) {
            @Override
            public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
                int total = 0;
                for (int i = 10; i < 15; i++) {
                    total += carrotStates[i] ? 1 : 0;
                }
                return total >= 3;
            }
        },
        Level2StockedUp(Optional.of(ShelfLevel.Level2), true, false) {
            @Override
            public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
                int total = 0;
                for (int i = 5; i < 10; i++) {
                    total += carrotStates[i] ? 1 : 0;
                }
                return total >= 3;
            }
        },
        Level1StockedUp(Optional.of(ShelfLevel.Level1), true, false) {
            @Override
            public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
                int total = 0;
                for (int i = 0; i < 5; i++) {
                    total += carrotStates[i] ? 1 : 0;
                }
                return total >= 3;
            }
        },
        Level3BakedUp(Optional.of(ShelfLevel.Level3), false, true) {
            @Override
            public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 10; i < 15; i++) {
                    if (carrotCakeStates[i]) return true;
                }
            }
        },
        Level2BakedUp(Optional.of(ShelfLevel.Level2), false, true) {
            @Override
            public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 5; i < 10; i++) {
                    if (carrotCakeStates[i]) return true;
                }
            }
        },
        Level1BakedUp(Optional.of(ShelfLevel.Level1), false, true) {
            @Override
            public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 0; i < 5; i++) {
                    if (carrotCakeStates[i]) return true;
                }
            }
        };

        public final Optional<ShelfLevel> level;
        public final boolean isSRP;
        public final boolean isBRP;
        Priority(Optional<ShelfLevel> level, boolean isSRP, boolean isBRP) {
            this.level = level;
            this.isSRP = isSRP;
            this.isBRP = isBRP;
        }
        public boolean isCompleted(boolean[] carrotStates, boolean[] carrotCakeStates, int ovenCarrotsCount, int ovenCakesCount) {
            return false;
        }
    }

    private Pair<CarrotGoal, Integer> selectedCarrotGoal = new Pair<>(CarrotGoal.SHELF, 0);
    private Pair<CarrotGoal, Integer> selectedCarrotCakeGoal = new Pair<>(CarrotGoal.SHELF, 0);

    private Mode mode = Mode.Dumb;

    private final boolean[] carrotStates = new boolean[] {
        false, false, false, false, false,
        false, false, false, false, false,
        false, false, false, false, false,
    };
    private final boolean[] carrotCakeStates = new boolean[] {
        false, false, false, false, false,
        false, false, false, false, false,
        false, false, false, false, false,
    };
    private int ovenCarrotsCount = 0;
    private int ovenCakesCount = 0;
    private boolean bakerModeState = false;

    private final Set<ShelfPosition> availableShelfPositions = new HashSet<>(15);

    private final List<Priority> fullStrategy = new ArrayList<>(List.of(
        Priority.Level3Fill,
        Priority.Level2Fill,
        Priority.Level1Fill,
        Priority.OvenSpam,
        Priority.Level3StockedUp,
        Priority.Level2StockedUp,
        Priority.Level1StockedUp,
        Priority.Level3BakedUp,
        Priority.Level2BakedUp,
        Priority.Level1BakedUp
    ));
    private final List<Priority> uncompletedPriorities = new ArrayList<>(fullStrategy.size());

    public static enum ObjectiveType {
        IntakeCarrot(false),
        ScoreCarrot(true),
        ScoreCarrotCake(true),
        ;

        public final boolean isScoreObjective;
        ObjectiveType(boolean isScoreObjective) {
            this.isScoreObjective = isScoreObjective;
        }
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Objective Tracker", inputs);

        if (inputs.mode != -1) {
            mode = Mode.values()[inputs.mode];
            inputs.mode = -1;
        }
        if (inputs.carrotGoal != -1) {
            if (inputs.carrotGoal >= 15) {
                selectedCarrotGoal = new Pair<>(CarrotGoal.OVEN, 0);
            } else {
                selectedCarrotGoal = new Pair<>(CarrotGoal.SHELF, inputs.carrotGoal);
            }
            inputs.carrotGoal = -1;
        }
        if (inputs.carrotCakeGoal != -1) {
            if (inputs.carrotCakeGoal >= 15) {
                selectedCarrotCakeGoal = new Pair<>(CarrotGoal.OVEN, 0);
            } else {
                selectedCarrotCakeGoal = new Pair<>(CarrotGoal.SHELF, inputs.carrotCakeGoal);
            }
            inputs.carrotCakeGoal = -1;
        }

        io.setMode(mode.ordinal());
        
        switch (selectedCarrotGoal.getFirst()) {
            case OVEN:
                io.setCarrotGoal(selectedCarrotGoal.getSecond() + 15);
                break;
            case SHELF:
                io.setCarrotGoal(selectedCarrotGoal.getSecond());
                break;
        }
        switch (selectedCarrotCakeGoal.getFirst()) {
            case OVEN:
                io.setCarrotCakeGoal(selectedCarrotCakeGoal.getSecond() + 15);
                break;
            case SHELF:
                io.setCarrotCakeGoal(selectedCarrotCakeGoal.getSecond());
                break;
        }

        var carrotsChanged = false;
        for (var changedCarrot : inputs.carrotQueue) {
            carrotsChanged = true;
            var carrotState = changedCarrot >= 0;
            var 
        }
    }
}
