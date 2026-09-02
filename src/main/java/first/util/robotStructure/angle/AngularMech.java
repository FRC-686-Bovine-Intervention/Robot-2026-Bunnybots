package first.util.robotStructure.angle;

import static org.wpilib.units.Units.Radians;

import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.geometry.Transform3d;
import org.wpilib.math.geometry.Translation3d;
import org.wpilib.math.linalg.Vector;
import org.wpilib.math.numbers.N3;
import org.wpilib.units.AngleUnit;
import org.wpilib.units.Measure;

import first.util.robotStructure.Mechanism3d;

public class AngularMech extends Mechanism3d<AngleUnit> {
	public AngularMech(Transform3d base, Vector<N3> axis) {
		super(base, axis);
	}

	@Override
	public void set(Measure<AngleUnit> angle) {
		this.setRads(angle.in(Radians));
	}

	public void setRads(double angleRads) {
		this.transform = new Transform3d(Translation3d.kZero, new Rotation3d(axis, angleRads));
	}
}
