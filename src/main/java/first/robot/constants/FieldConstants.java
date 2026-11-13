package first.robot.constants;

import static org.wpilib.units.Units.Inches;
import static org.wpilib.units.Units.Meters;

import java.io.IOException;
import java.util.List;

import org.wpilib.fields.Field;
import org.wpilib.fields.FieldTag;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.system.Filesystem;
import org.wpilib.units.measure.Distance;

import first.util.flipping.AllianceFlipped;

public final class FieldConstants {
	private static Field loadField() {
		try {
			return Field.loadFromResource(Filesystem.getDeployDirectory().getAbsolutePath() + "/Bunnybots_2026_Centered.json");
		} catch (IOException e) {
			throw new RuntimeException("Failed to load field configuration", e);
		}
	}

	public static final Field field = loadField();

	public static final Distance fieldLength = Meters.of(field.length);
	public static final Distance fieldWidth =  Meters.of(field.width);

	public static final List<FieldTag> fieldTags = field.tags;

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

	public static final AllianceFlipped<Pose2d> ovenStartingPose = AllianceFlipped.fromBlue(new Pose2d(RobotConstants.centerToSideBumper, kitchenLowerY.plus(RobotConstants.centerToFrontBumper), Rotation2d.CW_90DEG));
	public static final AllianceFlipped<Pose2d> ovenKitchenLineStartingPose = AllianceFlipped.fromBlue(new Pose2d(innerKitchenLineToWall.minus(RobotConstants.centerToFrontBumper), kitchenLowerY.plus(RobotConstants.centerToSideBumper), Rotation2d.ZERO));
	public static final AllianceFlipped<Pose2d> centerKitchenLineStartingPose = AllianceFlipped.fromBlue(new Pose2d(innerKitchenLineToWall.minus(RobotConstants.centerToFrontBumper), kitchenLowerY.plus(innerKitchenLineLength.div(2.0)), Rotation2d.ZERO));
	public static final AllianceFlipped<Pose2d> upperKitchenLineStartingPose = AllianceFlipped.fromBlue(new Pose2d(innerKitchenLineToWall.minus(RobotConstants.centerToFrontBumper), kitchenLowerY.plus(innerKitchenLineLength).minus(RobotConstants.centerToSideBumper), Rotation2d.ZERO));
	public static final AllianceFlipped<Pose2d> shelfStartingPose = AllianceFlipped.fromBlue(new Pose2d(RobotConstants.centerToSideBumper, kitchenUpperY.minus(shelfThickness).minus(RobotConstants.centerToFrontBumper), Rotation2d.CCW_90DEG));
}
