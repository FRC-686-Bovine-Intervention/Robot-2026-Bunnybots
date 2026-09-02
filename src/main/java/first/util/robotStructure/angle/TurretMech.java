package first.util.robotStructure.angle;

import org.wpilib.math.geometry.Transform3d;
import org.wpilib.math.linalg.VecBuilder;
import org.wpilib.math.linalg.Vector;
import org.wpilib.math.numbers.N3;

public class TurretMech extends AngularMech {
	private static final Vector<N3> axis = VecBuilder.fill(0,0,1);

	public TurretMech(Transform3d base) {
		super(base, axis);
	}
}
