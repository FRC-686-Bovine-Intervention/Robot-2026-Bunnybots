package first.util.robotStructure.linear;

import static org.wpilib.units.Units.Meters;

import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.geometry.Transform3d;
import org.wpilib.math.geometry.Translation3d;
import org.wpilib.math.linalg.Vector;
import org.wpilib.math.numbers.N3;
import org.wpilib.units.DistanceUnit;
import org.wpilib.units.Measure;

import first.util.robotStructure.Mechanism3d;

public class LinearMech extends Mechanism3d<DistanceUnit> {
	public LinearMech(Transform3d base, Vector<N3> axis) {
		super(base, axis);
	}

	@Override
	public void set(Measure<DistanceUnit> distance) {
		this.setMeters(distance.in(Meters));
	}

	public void setMeters(double distanceMeters) {
		this.transform = new Transform3d(new Translation3d(this.axis.times(distanceMeters)), Rotation3d.kZero);
	}
}
