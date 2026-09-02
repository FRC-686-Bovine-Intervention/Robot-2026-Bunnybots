package first.util.robotStructure.linear;

import org.wpilib.math.geometry.Transform3d;
import org.wpilib.math.linalg.VecBuilder;
import org.wpilib.math.linalg.Vector;
import org.wpilib.math.numbers.N3;

public class ExtenderMech extends LinearMech {
	private static final Vector<N3> axis = VecBuilder.fill(1,0,0);

	public ExtenderMech(Transform3d base) {
		super(base, axis);
	}
}
