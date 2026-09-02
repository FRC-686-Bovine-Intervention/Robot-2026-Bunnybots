package first.util.robotStructure;

import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Transform3d;

public abstract class ChildBase implements Component {
	private final Transform3d base;
	private Component parent;

	public ChildBase(Transform3d base) {
		this.base = base;
	}

	public ChildBase setParent(Component parent) {
		this.parent = parent;
		return this;
	}

	@Override
	public ChildBase addChild(ChildBase child) {
		child.setParent(this);
		return this;
	}

	@Override
	public Transform3d getRobotRelative() {
		return this.parent.getRobotRelative().plus(this.base);
	}

	@Override
	public Pose3d getFieldRelative() {
		return this.parent.getFieldRelative().plus(this.base);
	}
}
