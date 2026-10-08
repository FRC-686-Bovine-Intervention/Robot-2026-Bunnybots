package frc.robot.subsystems.objectiveTracker;

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

	public ObjectiveTracker(ShelfTrackerIO io) {
		this.io = io;
	}

	@Override
	public void periodic() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'periodic'");
	}

	private void updateTargetsByPriority() {
		for (int i = 0; i < targetSize; i++) {
			if (shelfState[i] == 0) {
				//Empty spot, can be considered, can assign a score to this location greater than zero
				/*
				  each priority has a ranking, so if a position fulfills that priority, it gets a score of (priority size + 1)-(priority rank)
				  assign no scoring based on the distance from the robot
				 */
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
	}
}
