package first.robot.constants;

import static org.wpilib.units.Units.Hertz;
import static org.wpilib.units.Units.Inches;
import static org.wpilib.units.Units.KilogramSquareMeters;
import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.Pounds;
import static org.wpilib.units.Units.Seconds;

import org.wpilib.math.geometry.Translation2d;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.Frequency;
import org.wpilib.units.measure.Mass;
import org.wpilib.units.measure.MomentOfInertia;
import org.wpilib.units.measure.Time;

import first.robot.Robot;

public final class RobotConstants {
	public static final boolean tuningMode = false;

	public static final Mass robotWeight = Pounds.of(125);
	public static final MomentOfInertia robotMOI = KilogramSquareMeters.of(6);

	public static final Distance frameLength = Inches.of(27.0);
	public static final Distance frameWidth = Inches.of(27.0);

	public static final Distance centerToFrontFrame = frameLength.div(2.0);
	public static final Distance centerToSideFrame = frameWidth.div(2.0);

	public static final Distance bumperThickness = Inches.of(3.625);

	public static final Distance centerToFrontBumper = centerToFrontFrame.plus(bumperThickness);
	public static final Distance centerToSideBumper = centerToSideFrame.plus(bumperThickness);

	public static final Translation2d flBumperCorner = new Translation2d(centerToFrontBumper, centerToSideBumper);
	public static final Translation2d frBumperCorner = new Translation2d(centerToFrontBumper, centerToSideBumper.unaryMinus());
	public static final Translation2d blBumperCorner = new Translation2d(centerToFrontBumper.unaryMinus(), centerToSideBumper);
	public static final Translation2d brBumperCorner = new Translation2d(centerToFrontBumper.unaryMinus(), centerToSideBumper.unaryMinus());

	/**Distance between back bumper and front bumper, aka in the X axis */
	public static final Distance robotLength = centerToFrontBumper.times(2.0);
	/**Distance between left bumper and right bumper, aka in the Y axis */
	public static final Distance robotWidth = centerToSideBumper.times(2.0);

	public static final Distance centerToBumperCorner = Meters.of(Math.hypot(centerToFrontBumper.in(Meters), centerToSideBumper.in(Meters)));

	public static final double controllerUpdatePeriodSecs = Robot.defaultPeriodSecs;
	public static final Time controllerUpdatePeriod = Seconds.of(controllerUpdatePeriodSecs);
	public static final Frequency controllerUpdateFrequency = controllerUpdatePeriod.asFrequency();
	public static final double controllerUpdateFrequencyHz = controllerUpdateFrequency.in(Hertz);
}
