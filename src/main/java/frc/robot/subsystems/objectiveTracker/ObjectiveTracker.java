package frc.robot.subsystems.objectiveTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.Pair;
import frc.util.VirtualSubsystem;
import frc.util.math.MathExtraUtil;

public class ObjectiveTracker extends VirtualSubsystem {
    private final ShelfTrackerIO io;
    private final ShelfTrackerIOInputsAutoLogged inputs = new ShelfTrackerIOInputsAutoLogged();

    private static final int[] shelfState = { //Oven is always kept at 0
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0
	};

	private static final int[] carrotTargets = { //Ascends, then to oven
		0, 1, 2, 3, 4,
		5, 6, 7, 8, 9,
		10, 11, 12, 13, 14,
		15
	};

	private static final int[] cakeTargets = { //Ascends, then to oven
		0, 1, 2, 3, 4,
		5, 6, 7, 8, 9,
		10, 11, 12, 13, 14,
		15
	};

	private static final int[] carrotScores = {
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0
	};
	
	private static final int[] cakeScores = {
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0, 0, 0, 0, 0,
		0
	};

	private static final int targetSize = 16;


	public static enum Priority {
        Level3Fill(3, false, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 10; i < 15; i++) {
                    if (shelfStates[i] == 0) return false;
                }
                return true;
            }
        },
        Level2Fill(2, false, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 5; i < 10; i++) {
                    if (shelfStates[i] == 0) return false;
                }
                return true;
            }
        },
        Level1Fill(1, false, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 0; i < 5; i++) {
                    if (shelfStates[i] == 0) return false;
                }
                return true;
            }
        },
        OvenSpam(4, false, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                return false;
            }
        },
        Level3StockedUp(3, true, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                int total = 0;
                for (int i = 10; i < 15; i++) {
                    total += shelfStates[i] != 0 ? 1 : 0;
                }
                return total >= 3;
            }
        },
        Level2StockedUp(2, true, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                int total = 0;
                for (int i = 5; i < 10; i++) {
                    total += shelfStates[i] != 0 ? 1 : 0;
                }
                return total >= 3;
            }
        },
        Level1StockedUp(1, true, false) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                int total = 0;
                for (int i = 0; i < 5; i++) {
                    total += shelfStates[i] != 0 ? 1 : 0;
                }
                return total >= 3;
            }
        },
        Level3BakedUp(3, false, true) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 10; i < 15; i++) {
                    if (shelfStates[i] == 2) return true;
                }
                return false;
            }
        },
        Level2BakedUp(2, false, true) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 5; i < 10; i++) {
                    if (shelfStates[i] == 2) return true;
                }
                return false;
            }
        },
        Level1BakedUp(1, false, true) {
            @Override
            public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
                for (int i = 0; i < 5; i++) {
                    if (shelfStates[i] == 2) return true;
                }
                return false;
            }
        };

        public final int level;
        public final boolean isSRP;
        public final boolean isBRP;
        Priority(int level, boolean isSRP, boolean isBRP) {
            this.level = level;
            this.isSRP = isSRP;
            this.isBRP = isBRP;
        }
        public boolean isCompleted(int[] shelfStates, int ovenCarrotsCount, int ovenCakesCount) {
            return false;
        }
    }

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

	public ObjectiveTracker(ShelfTrackerIO io) {
		this.io = io;
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
	}

	private void updateTargetsByPriority() {
		for (int i = 0; i < targetSize; i++) {
			if (shelfState[i] == 0) {
				//Empty spot, can be considered, can assign a score to this location greater than zero
				/*
				  each priority has a ranking, so if a position fulfills that priority, it gets a score of (priority size + 1)-(priority rank)
				  assign no scoring based on the distance from the robot
				 */

				//Check SORTED uncompleted priorities:
                for (int j = 0; j < uncompletedPriorities.size(); j++) {
                    var priority = uncompletedPriorities.get(j);
                    //Check if shelf level matches:
                    int shelfLevel = i / 5 + 1; //Shelf levels are 1-indexed
                    if (priority.level == shelfLevel) {
                        //valid priority, must assign score for each type of item
                        int score = uncompletedPriorities.size() - j;
                        //If it's baked up, only cake can be placed there
                        if (priority.isBRP) {
                            cakeScores[i] += score;
                        } else {
                            carrotScores[i] += score;
                            cakeScores[i] += score;
                        }
                    }
                }
			}
		}

		//Insertion sorts the targets by their scores, highest to lowest
		MathExtraUtil.downwardsInsertionSort(cakeTargets, cakeScores);
		MathExtraUtil.downwardsInsertionSort(carrotTargets, carrotScores);

		for (int i = 2; i >= 0; i--) {
			if (cakeTargets[i] != 15) {
				//Not in oven, so this target can be occupied
				for (int j = 0; j < 3; j++) {
					if (carrotTargets[j] == cakeTargets[i]) {
						//Need to move carrotTargets[j] to bottom, move list up 1
						MathExtraUtil.moveValueToEnd(carrotTargets, j);
					}
				}
			}
		}

        for (int j = 0; j < targetSize; j++) {
            carrotScores[j] = 0;
            cakeScores[j] = 0;
        }
	}
}
