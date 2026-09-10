package frc.robot.constants;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Meter;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Radians;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import frc.util.flipping.AllianceFlipped;
import frc.util.geometry.PoseBoundingBoxUtil.BoundingBox;
import frc.util.geometry.PoseBoundingBoxUtil.OrBox;
import frc.util.geometry.PoseBoundingBoxUtil.RectangularBoundingBox;
import frc.util.geometry.PoseBoundingBoxUtil.VerticalLine;

public final class FieldConstants {
	public static final Distance fieldLength = Inches.of(651.2);
	public static final Distance fieldWidth =  Inches.of(317.7);

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
}
