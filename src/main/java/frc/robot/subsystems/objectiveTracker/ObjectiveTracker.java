package frc.robot.subsystems.objectiveTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.littletonrobotics.junction.Logger;

import frc.util.LoggedTracer;
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

	private int selectedCarrotGoal = 15;
	private int selectedCarrotCakeGoal = 15;
	private int selectedCarrotTarget = 15;
	private int selectedCarrotCakeTarget = 15;

	private Mode mode = Mode.Dumb;

	private int ovenCarrotsCount = 0;
	private int ovenCakesCount = 0;
	private boolean bakerModeState = false;

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
			selectedCarrotGoal = inputs.carrotGoal;
			inputs.carrotGoal = -1;
		}
		if (inputs.carrotCakeGoal != -1) {
			selectedCarrotCakeGoal = inputs.carrotCakeGoal;
			inputs.carrotCakeGoal = -1;
		}

		io.setMode(mode.ordinal());

		io.setCarrotGoal(selectedCarrotGoal);
		io.setCarrotCakeGoal(selectedCarrotCakeGoal);

		var shelfChanged = inputs.shelfQueue.length > 0;
		for (var shelfSnapshot : inputs.shelfQueue) {
			for (int i = 0; i < shelfState.length; i++) {
				long value = i < shelfSnapshot.length ? shelfSnapshot[i] : 0;
				shelfState[i] = value == 1 || value == 2 ? (int) value : 0;
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

		for (var changedBakerMode : this.inputs.bakerMode) {
			this.bakerModeState = changedBakerMode;
		}
		LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Update Baker Mode State");

		if (shelfChanged || ovenCarrotsChanged || ovenCakesChanged || strategyChanged) {
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
				priority.isCompleted(this.shelfState, this.ovenCarrotsCount, this.ovenCakesCount)
			);
		}
		LoggedTracer.logEpoch("CommandScheduler Periodic/VirtualSubsystem Periodic/ObjectiveTracker/Log Priorities");
	}

	private void updateIncompletePriorities() {
		uncompletedPriorities.clear();
		for (var priority : this.fullStrategy) {
			if (!priority.isCompleted(shelfState, ovenCarrotsCount, ovenCakesCount)) {
				uncompletedPriorities.add(priority);
			}
		}
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

	public int getCarrotTarget() {
		return selectedCarrotTarget;
	}

	public int getCarrotCakeTarget() {
		return selectedCarrotCakeTarget;
	}

	public void determineGoals() {
		if (mode == Mode.Smart) {
			if (!this.bakerModeState) {
				this.updateTargetsByPriority();
				selectedCarrotTarget = carrotTargets[0];
				selectedCarrotCakeTarget = cakeTargets[0];
			} else {
				selectedCarrotTarget = 15;
				selectedCarrotCakeTarget = 15;
			}
		} else {
			selectedCarrotTarget = selectedCarrotGoal;
			selectedCarrotCakeTarget = selectedCarrotCakeGoal;
		}
	}
}
