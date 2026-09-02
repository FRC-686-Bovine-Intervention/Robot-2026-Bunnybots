package first.util.geometry;

import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.Radians;

import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.geometry.Transform2d;
import org.wpilib.math.geometry.Transform3d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.math.geometry.Translation3d;
import org.wpilib.math.linalg.MatBuilder;
import org.wpilib.math.linalg.Matrix;
import org.wpilib.math.linalg.VecBuilder;
import org.wpilib.math.linalg.Vector;
import org.wpilib.math.numbers.N2;
import org.wpilib.math.numbers.N3;
import org.wpilib.math.util.MathUtil;
import org.wpilib.math.util.Nat;
import org.wpilib.units.AngleUnit;
import org.wpilib.units.DistanceUnit;
import org.wpilib.units.Measure;
import org.wpilib.units.measure.Angle;
import org.wpilib.math.kinematics.ChassisVelocities;

public class GeomUtil {
	public static final Transform2d rotate180Transform2d = new Transform2d(Translation2d.kZero, Rotation2d.k180deg);
	public static final Transform3d rotate180Transform3d = new Transform3d(Translation3d.kZero, new Rotation3d(Rotation2d.k180deg));

	public static Transform3d toTransform3d(Pose3d pose) {
		return new Transform3d(
			pose.getTranslation(),
			pose.getRotation()
		);
	}

	public static Rotation2d backwards(Rotation2d rotation) {
		return new Rotation2d(-rotation.getCos(), -rotation.getSin());
	}

	public static Rotation2d rotationFromVector(Vector<N2> vec) {
		return new Rotation2d(vec.get(0), vec.get(1));
	}
	public static Vector<N2> vectorFromRotation(Rotation2d rot) {
		return VecBuilder.fill(rot.getCos(), rot.getSin());
	}

	public static Vector<N2> vectorFromSpeeds(ChassisVelocities speeds) {
		return VecBuilder.fill(speeds.vx, speeds.vy);
	}
	public static Translation2d translationFromSpeeds(ChassisVelocities speeds) {
		return new Translation2d(speeds.vx, speeds.vy);
	}
	public static ChassisVelocities speedsFromVector2D(Vector<N2> vector) {
		return new ChassisVelocities(vector.get(0), vector.get(1), 0);
	}
	public static ChassisVelocities speedsFromVector3D(Vector<N3> vector) {
		return new ChassisVelocities(vector.get(0), vector.get(1), vector.get(2));
	}
	public static ChassisVelocities speedsFromTranslation(Translation2d translation) {
		return new ChassisVelocities(translation.getX(), translation.getY(), 0);
	}

	public static boolean isNear(Pose2d expected, Pose2d actual, double linearTolerance, double angularTolerance) {
		return isNear(expected.getTranslation(), actual.getTranslation(), linearTolerance) && isNear(expected.getRotation(), actual.getRotation(), angularTolerance);
	}
	public static boolean isNear(Pose2d expected, Pose2d actual, Measure<DistanceUnit> linearTolerance, Measure<AngleUnit> angularTolerance) {
		return isNear(expected.getTranslation(), actual.getTranslation(), linearTolerance) && isNear(expected.getRotation(), actual.getRotation(), angularTolerance);
	}
	public static boolean isNear(Translation2d expected, Translation2d actual, double tolerance) {
		return actual.getDistance(expected) <= tolerance;
	}
	public static boolean isNear(Translation2d expected, Translation2d actual, Measure<DistanceUnit> tolerance) {
		return isNear(expected, actual, tolerance.in(Meters));
	}
	public static boolean isNear(Rotation2d expected, Rotation2d actual, double tolerance) {
		return Math.abs(actual.minus(expected).getRadians()) <= tolerance;
	}
	public static boolean isNear(Rotation2d expected, Rotation2d actual, Measure<AngleUnit> tolerance) {
		return isNear(expected, actual, tolerance.in(Radians));
	}
	public static boolean isNear(ChassisVelocities expected, ChassisVelocities actual, double linearTolerance, double angularTolerance) {
		var bol = isNear(new Translation2d(expected.vx, expected.vy), new Translation2d(actual.vx, actual.vy), linearTolerance) && MathUtil.isNear(expected.omega, actual.omega, angularTolerance);
		return bol;
	}

	public static Matrix<N2, N2> rotationMatrix(Rotation2d rot) {
		return MatBuilder.fill(Nat.N2(), Nat.N2(),
			+rot.getCos(), -rot.getSin(),
			+rot.getSin(), +rot.getCos()
		);
	}

	public static class TransformUtil {
		public static Transform2d toTransform2d(Transform3d a) {
			return new Transform2d(a.getTranslation().toTranslation2d(), a.getRotation().toRotation2d());
		}
	}

	public static Rotation3dBuilder rotation3dBuilder() {
		return new Rotation3dBuilder();
	}

	public static class Rotation3dBuilder {
		private Rotation3d inner = new Rotation3d();

		private Rotation3dBuilder() {}

		public Rotation3d build() {
			return this.inner;
		}

		public Rotation3dBuilder roll(double rollRads) {
			this.inner = this.inner.rotateBy(new Rotation3d(rollRads, 0, 0));
			return this;
		}
		public Rotation3dBuilder roll(Angle roll) {
			return this.roll(roll.in(Radians));
		}

		public Rotation3dBuilder pitch(double pitchRads) {
			this.inner = this.inner.rotateBy(new Rotation3d(0, pitchRads, 0));
			return this;
		}
		public Rotation3dBuilder pitch(Angle pitch) {
			return this.pitch(pitch.in(Radians));
		}

		public Rotation3dBuilder yaw(double yawRads) {
			this.inner = this.inner.rotateBy(new Rotation3d(0, 0, yawRads));
			return this;
		}
		public Rotation3dBuilder yaw(Angle yaw) {
			return this.yaw(yaw.in(Radians));
		}
	}
}
