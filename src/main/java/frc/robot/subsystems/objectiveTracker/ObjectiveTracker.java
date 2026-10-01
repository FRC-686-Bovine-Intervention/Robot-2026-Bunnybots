package frc.robot.subsystems.objectiveTracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import frc.robot.subsystems.objectiveTracker.ObjectiveTracker.CarrotGoal;
import frc.util.LoggedTracer;
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
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 10; i < 15; i++) {
                    if (shelfStates[i] == 0) return false;
                }
                return true;
            }
        },
        Level2Fill(Optional.of(ShelfLevel.Level2), false, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 5; i < 10; i++) {
                    if (shelfStates[i] == 0) return false;
                }
                return true;
            }
        },
        Level1Fill(Optional.of(ShelfLevel.Level1), false, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 0; i < 5; i++) {
                    if (shelfStates[i] == 0) return false;
                }
                return true;
            }
        },
        OvenSpam(Optional.empty(), false, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                return ovenCarrotsCount + ovenCakesCount > 5;
            }
        },
        Level3StockedUp(Optional.of(ShelfLevel.Level3), true, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                int total = 0;
                for (int i = 10; i < 15; i++) {
                    total += shelfStates[i] != 0 ? 1 : 0;
                }
                return total >= 3;
            }
        },
        Level2StockedUp(Optional.of(ShelfLevel.Level2), true, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                int total = 0;
                for (int i = 5; i < 10; i++) {
                    total += shelfStates[i] != 0 ? 1 : 0;
                }
                return total >= 3;
            }
        },
        Level1StockedUp(Optional.of(ShelfLevel.Level1), true, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                int total = 0;
                for (int i = 0; i < 5; i++) {
                    total += shelfStates[i] != 0 ? 1 : 0;
                }
                return total >= 3;
            }
        },
        Level3BakedUp(Optional.of(ShelfLevel.Level3), false, true) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 10; i < 15; i++) {
                    if (shelfStates[i] == 2) return true;
                }
                return false;
            }
        },
        Level2BakedUp(Optional.of(ShelfLevel.Level2), false, true) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 5; i < 10; i++) {
                    if (shelfStates[i] == 2) return true;
                }
                return false;
            }
        },
        Level1BakedUp(Optional.of(ShelfLevel.Level1), false, true) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 0; i < 5; i++) {
                    if (shelfStates[i] == 2) return true;
                }
                return false;
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
        public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
            return false;
        }
    }

    private Pair<CarrotGoal, Integer> selectedCarrotGoal = new Pair<>(CarrotGoal.SHELF, 0);
    private Pair<CarrotGoal, Integer> selectedCarrotCakeGoal = new Pair<>(CarrotGoal.SHELF, 0);

    private Mode mode = Mode.Dumb;

    private final int[] shelfStates = new int[15];
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
        ScoreCarrot(true),
        ScoreCarrotCake(true),
        ;

        public final boolean isScoreObjective;
        ObjectiveType(boolean isScoreObjective) {
            this.isScoreObjective = isScoreObjective;
        }
    }

	public ObjectiveTracker(ShelfTrackerIO io) {
		System.out.println("[Init ObjectiveTracker] Instantiating ObjectiveTracker with " + io.getClass().getSimpleName());
		this.io = io;

        updateShelfPositions();
		updateIncompletePriorities();
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

        var shelfChanged = inputs.shelfQueue.length > 0;
        for (var shelfSnapshot : inputs.shelfQueue) {
            for (int i = 0; i < shelfStates.length; i++) {
                long value = i < shelfSnapshot.length ? shelfSnapshot[i] : 0;
                shelfStates[i] = value == 1 || value == 2 ? (int) value : 0;
            }
        }
		LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Update Shelf State");

		var ovenCarrotsChanged = false;
		for (var changedOvenCarrots : inputs.ovenCarrotsQueue) {
			ovenCarrotsChanged = true;
			ovenCarrotsCount += changedOvenCarrots;
		}
		LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Update Oven Carrots Count");
		
		var ovenCakesChanged = false;
		for (var changedOvenCakes : inputs.ovenCakesQueue) {
			ovenCakesChanged = true;
			ovenCakesCount += changedOvenCakes;
		}
		LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Update Oven Cakes Count");

		var strategyChanged = false;
		for (var changedPriority : this.inputs.priorityListQueue) {
            strategyChanged = true;
            var oldIndex = changedPriority[0];
            var newIndex = changedPriority[1];
            Collections.swap(this.fullStrategy, oldIndex, newIndex);
        }
        LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Update Full Strategy");

		var bakerModeChanged = false;
        for (var changedBakerMode : this.inputs.bakerMode) {
            bakerModeChanged = true;
            this.bakerModeState = changedBakerMode;
        }
        LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Update Baker Mode State");

        if (shelfChanged) {
            this.updateShelfPositions();
		}
		LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Update Available Shelf Positions");

        if (shelfChanged || ovenCarrotsChanged || ovenCakesChanged) {
			this.updateIncompletePriorities();
		}
		LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Update Incompete Priorities");

		for (var priority : this.fullStrategy) {
			Logger.recordOutput(
				switch (priority) {
					case Level3Fill -> "ObjectiveTracker/Priorities/Fill/Level 3";
					case Level2Fill -> "ObjectiveTracker/Priorities/Fill/Level 2";
					case Level1Fill -> "ObjectiveTracker/Priorities/Fill/Level 1";
					case OvenSpam -> "ObjectiveTracker/Priorities/Other/Oven Spam";
					case Level3StockedUp -> "ObjectiveTracker/Priorities/SRP/Level 3";
					case Level2StockedUp -> "ObjectiveTracker/Priorities/SRP/Level 2";
					case Level1StockedUp -> "ObjectiveTracker/Priorities/SRP/Level 1";
					case Level3BakedUp -> "ObjectiveTracker/Priorities/BRP/Level 3";
					case Level2BakedUp -> "ObjectiveTracker/Priorities/BRP/Level 2";
					case Level1BakedUp -> "ObjectiveTracker/Priorities/BRP/Level 1";
				},
                priority.isCompleted(this.shelfStates, this.ovenCarrotsCount, this.ovenCakesCount)
			);
		}
		LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Log Priorities");
        
        this.io.setShelfState(this.shelfStates);
        this.io.setOvenCarrotsCount(this.ovenCarrotsCount);
		this.io.setOvenCakesCount(this.ovenCakesCount);
		this.io.setBakerModeState(this.bakerModeState);
		this.io.setPriorityList(this.fullStrategy.stream().mapToInt(Enum::ordinal).toArray());

		Logger.recordOutput("Objective Tracker/Priorities/Strategy/Full", this.fullStrategy.toArray(Priority[]::new));
        Logger.recordOutput("Objective Tracker/Priorities/Strategy/Uncomplete", this.uncompletedPriorities.toArray(Priority[]::new));
        LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Periodic");
        LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker");
    }

	private void updateShelfPositions() {
		this.availableShelfPositions.clear();
		var filledShelfPositions = new ArrayList<Pose3d>(15);
        for (int i = 0; i < shelfStates.length; i++) {
            if (shelfStates[i] == 0) {
				availableShelfPositions.add(Shelf.positions[i]);
			} else {
				filledShelfPositions.add(Shelf.shelfs.getOurs().positions[i].pose);
			}
			Logger.recordOutput("Objective Tracker/Reef/Coral", filledShelfPositions.toArray(Pose3d[]::new));
		}
	}

	public void determineGoal(Pose2d currentPose, boolean hasCarrot, boolean hasCarrotCake) {
		if (mode == Mode.Smart) {
			
		}
	}
}
