package frc.robot.constants;

import static edu.wpi.first.units.Units.Feet;
import static edu.wpi.first.units.Units.Inches;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.units.measure.Distance;
import frc.util.flipping.AllianceFlipped;

public final class FieldConstants {
	public static final Distance fieldLength = Feet.of(54);
	public static final Distance fieldWidth =  Feet.of(27);

	// public static final AllianceFlipped<VerticalLine> allianceBox = AllianceFlipped.fromBlue(new VerticalLine(fieldLength.div(2.0).in(Meters), false));

	public static final AprilTagFieldLayout apriltagLayout;
	static {
		AprilTagFieldLayout a = null;
		try {
			a = AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltAndymark);
		} catch(Exception e) {
			e.printStackTrace();
		}
		apriltagLayout = a;
	}

	public static final Distance carrotDiameter = Inches.of(6);
	public static final Distance carrotLength = Inches.of(18);

	public static final Distance innerKitchenLineToWall = Inches.of(75.5);
	public static final Distance kitchenLowerY = Inches.of(106.031);
	public static final Distance kitchenUpperY = fieldWidth;
	public static final Distance outerKitchenLineToWall = Inches.of(125);
	public static final Distance outerKitchenExtension = Inches.of(43);
	public static final Distance outerKitchenExtensionY = fieldWidth.minus(outerKitchenExtension);
	public static final Distance innerKitchenLineLength = fieldWidth.minus(kitchenLowerY).minus(outerKitchenExtension);
	public static final Distance shelfThickness = Inches.of(7);
	public static final Distance shelfLength = Inches.of(97);
	public static final Distance shelfToDriverStation = Inches.of(12);
	public static final Distance tapeThickness = Inches.of(2);

	public static final Distance prestagedCarrotsToCenterLine = Inches.of(84);
	public static final Distance prestagedCarrotsLowerY = Inches.of(75);
	public static final Distance prestagedCarrotsLineLength = fieldWidth.minus(prestagedCarrotsLowerY).minus(prestagedCarrotsLowerY);

	public static final Distance carrotFourLowerY = Inches.of(111);
	public static final Distance carrotFiveCenterY = prestagedCarrotsLowerY.plus(carrotDiameter.div(2));
	public static final Distance carrotFourCenterY = carrotFourLowerY.plus(carrotLength.div(2));
	public static final Distance carrotThreeCenterY = fieldWidth.div(2);
	public static final Distance carrotTwoCenterY = fieldWidth.minus(carrotFourCenterY);
	public static final Distance carrotOneCenterY = fieldWidth.minus(carrotFiveCenterY);

	public static final AllianceFlipped<Pose2d> ovenStartingPose = AllianceFlipped.fromBlue(new Pose2d(RobotConstants.centerToSideBumper, kitchenLowerY.plus(RobotConstants.centerToFrontBumper), Rotation2d.kCW_90deg));
	public static final AllianceFlipped<Pose2d> ovenKitchenLineStartingPose = AllianceFlipped.fromBlue(new Pose2d(innerKitchenLineToWall.minus(RobotConstants.centerToFrontBumper), kitchenLowerY.plus(RobotConstants.centerToSideBumper), Rotation2d.kZero));
	public static final AllianceFlipped<Pose2d> centerKitchenLineStartingPose = AllianceFlipped.fromBlue(new Pose2d(innerKitchenLineToWall.minus(RobotConstants.centerToFrontBumper), kitchenLowerY.plus(innerKitchenLineLength.div(2.0)), Rotation2d.kZero));
	public static final AllianceFlipped<Pose2d> upperKitchenLineStartingPose = AllianceFlipped.fromBlue(new Pose2d(innerKitchenLineToWall.minus(RobotConstants.centerToFrontBumper), kitchenLowerY.plus(innerKitchenLineLength).minus(RobotConstants.centerToSideBumper), Rotation2d.kZero));
	public static final AllianceFlipped<Pose2d> shelfStartingPose = AllianceFlipped.fromBlue(new Pose2d(RobotConstants.centerToSideBumper, kitchenUpperY.minus(shelfThickness).minus(RobotConstants.centerToFrontBumper), Rotation2d.kCCW_90deg));
}
