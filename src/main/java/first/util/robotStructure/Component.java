package first.util.robotStructure;

import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Transform3d;

public interface Component {
	public Transform3d getRobotRelative();
	public Pose3d getFieldRelative();

	public default Component addChild(ChildBase child) {
		child.setParent(this);
		return this;
	}
}
