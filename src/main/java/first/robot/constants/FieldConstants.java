package first.robot.constants;

import static org.wpilib.units.Units.Meters;

import java.util.List;

import org.wpilib.fields.Field;
import org.wpilib.fields.FieldTag;
import org.wpilib.fields.Fields;
import org.wpilib.units.measure.Distance;

public final class FieldConstants {
	public static final Field field = Fields.FRC_2026_REBUILT_ANDY_MARK.loadField();

	public static final Distance fieldLength = Meters.of(field.length);
	public static final Distance fieldWidth =  Meters.of(field.width);

	public static final List<FieldTag> fieldTags = field.tags;
}
