package first.robot.constants;

import static org.wpilib.units.Units.Meters;

import java.io.IOException;
import java.util.List;

import org.wpilib.fields.Field;
import org.wpilib.fields.FieldTag;
import org.wpilib.system.Filesystem;
import org.wpilib.units.measure.Distance;

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
}
