package first.robot.constants;

import static org.wpilib.units.Units.Inches;

import org.wpilib.units.measure.Distance;
import org.wpilib.vision.apriltag.AprilTagFieldLayout;
import org.wpilib.vision.apriltag.AprilTagFields;

public final class FieldConstants {
	public static final Distance fieldLength = Inches.of(651.2);
	public static final Distance fieldWidth =  Inches.of(317.7);

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
