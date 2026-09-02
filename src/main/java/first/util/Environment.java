package first.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;
import org.wpilib.driverstation.Alert;
import org.wpilib.driverstation.RobotState;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.PubSubOption;
import org.wpilib.networktables.StringArrayPublisher;
import org.wpilib.networktables.StringEntry;
import org.wpilib.networktables.StringPublisher;

public enum Environment {
	Practice,
	Competition,
	Demo,
	;
	private static final String key = "SmartDashboard/Environment Chooser";
	private static final StringPublisher namePublisher;
	private static final StringPublisher typePublisher;
	private static final StringArrayPublisher optionsPublisher;
	private static final StringPublisher defaultPublisher;
	private static final StringPublisher activePublisher;
	private static final StringEntry selectedEntry;
	private static String[] ntArray;
	private static final LoggableInputs inputs = new LoggableInputs() {
		@Override
		public void toLog(LogTable table) {
			table.put(key, ntArray);
		}

		@Override
		public void fromLog(LogTable table) {
			ntArray = table.get(key, ntArray);
		}
	};

	private static final Map<String, Environment> map;
	private static int selectionPriority;
	private static String selectedName;
	private static Environment selectedValue;

	private static final Alert fms_alert = new Alert("FMS detected, Competition Environment selected", Alert.Level.LOW);
	private static final Alert fms_no_comp_alert = new Alert("FMS detected but selected Environment is not Competition", Alert.Level.MEDIUM);
	private static final Alert demo_alert = new Alert("Demo Environment selected, Robot functionality restricted", Alert.Level.MEDIUM);

	static {
		var practiceName = "Practice";
		var competitionName = "Competition";
		var demoName = "Demo";

		map = new HashMap<>(3);
		map.put(practiceName, Practice);
		map.put(competitionName, Competition);
		map.put(demoName, Demo);

		selectedName = practiceName;

		var table = NetworkTableInstance.getDefault().getTable(key);
		namePublisher = table.getStringTopic(".name").publish();
		typePublisher = table.getStringTopic(".type").publish();
		optionsPublisher = table.getStringArrayTopic("options").publish();
		defaultPublisher = table.getStringTopic("default").publish();
		activePublisher = table.getStringTopic("active").publish();
		selectedEntry = table.getStringTopic("selected").getEntry(selectedName, PubSubOption.EXCLUDE_SELF);

		namePublisher.set(key);
		typePublisher.set("String Chooser");

		optionsPublisher.set(new String[] {
			practiceName,
			competitionName,
			demoName
		});

		defaultPublisher.set(selectedName);
		activePublisher.set(selectedName);
		selectedEntry.set(selectedName);

		selectionPriority = 0;
	}

	public static void periodic() {
		ntArray = selectedEntry.readQueueValues();
		Logger.processInputs("NetworkInputs", inputs);
		for (var selected : ntArray) {
			selectedName = selected;
			selectionPriority = 2;
		}
		if (selectionPriority <= 1 && RobotState.isFMSAttached() && !selectedName.equals("Competition")) {
			selectedName = "Competition";
			selectedEntry.set(selectedName);
			selectionPriority = 1;
		}
		selectedValue = map.get(selectedName);
		activePublisher.set(selectedName);

		fms_alert.set(RobotState.isFMSAttached() && isCompetition());
		fms_no_comp_alert.set(RobotState.isFMSAttached() && !isCompetition());
		demo_alert.set(isDemo());
	}

	public static Environment getEnvironment() {
		return selectedValue;
	}
	public static boolean is(Environment is) {
		return is.equals(selectedValue);
	}
	public static boolean isPractice() {
		return is(Practice);
	}
	public static boolean isCompetition() {
		return is(Competition);
	}
	public static boolean isDemo() {
		return is(Demo);
	}

	public static <T> Supplier<T> switchVar(T prac_comp, T demo) {
		return switchVar(prac_comp, prac_comp, demo);
	}
	public static <T> Supplier<T> switchVar(T prac, T comp, T demo) {
		return () -> switch(selectedValue) {
			default -> prac;
			case Competition -> comp;
			case Demo -> demo;
		};
	}
}
