package frc.robot.constants;

import static edu.wpi.first.units.Units.Inches;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.units.measure.Distance;

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
