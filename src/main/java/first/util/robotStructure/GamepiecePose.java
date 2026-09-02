package first.util.robotStructure;

import org.littletonrobotics.junction.Logger;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Transform3d;

public class GamepiecePose extends ChildBase {
	public GamepiecePose(Transform3d base) {
		super(base);
	}

	public void logAscopePose(String key, boolean active) {
		Logger.recordOutput(key,
			(active) ? (
				new Pose3d[]{
					this.getFieldRelative()
				}
			) : (
				new Pose3d[]{}
			)
		);
	}
}
