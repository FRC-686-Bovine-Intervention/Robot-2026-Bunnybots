package first.robot.constants;

import first.util.hardwareID.can.CANBus;
import first.util.hardwareID.can.CANDevice;

public class HardwareDevices {
	/*
	 * PDP Ports
	 * 12: FL Drive          11: FR Drive
	 * 13: FL Azimuth        10: FR Azimuth
	 * 14: L Intake Roller    9: R Intake Roller
	 * 15: Intake Slam        8: MPM
	 * 16: Radio              7: Rio
	 * 17: Radio              6: Climber
	 * 18: Indexer            5: Hood
	 * 19: Agitator           4: Feeder
	 * 20: LS Flywheel        3: RS Flywheel
	 * 21: LM Flywheel        2: RM Flywheel
	 * 22: BL Azimuth         1: BR Azimuth
	 * 23: BL Drive           0: BR Drive
	 */
	public static final CANBus busOne =   CANBus.systemcoreBus(0);
	public static final CANBus busTwo =   CANBus.systemcoreBus(1);
	public static final CANBus busThree = CANBus.systemcoreBus(2);
	public static final CANBus busFour =  CANBus.systemcoreBus(3);
	public static final CANBus busFive =  CANBus.systemcoreBus(4);

	public static final CANBus canivore = CANBus.canivore("canivore");

	// Drive
	public static final CANDevice pigeonID = busFive.id(0);
	// | Front Left
	public static final CANDevice frontLeftDriveMotorID = busOne.id(1);
	public static final CANDevice frontLeftAzimuthMotorID = busTwo.id(1);
	// | Front Right
	public static final CANDevice frontRightDriveMotorID = busOne.id(2);
	public static final CANDevice frontRightAzimuthMotorID = busTwo.id(2);
	// | Back Left
	public static final CANDevice backLeftDriveMotorID = busOne.id(3);
	public static final CANDevice backLeftAzimuthMotorID = busTwo.id(3);
	// | Back Right
	public static final CANDevice backRightDriveMotorID = busOne.id(4);
	public static final CANDevice backRightAzimuthMotorID = busTwo.id(4);

	// Intake
	// | Slam
	public static final CANDevice intakeSlamMotorID = busThree.id(5);
	public static final CANDevice intakeSlamEncoderID = busFive.id(5);
	// | Rollers
	public static final CANDevice intakeLeftRollerMotorID = busThree.id(6);
	public static final CANDevice intakeRightRollerMotorID = busThree.id(7);

	// Rollers
	// | Indexer
	public static final CANDevice indexerMotorID = busFour.id(8);
	// | Feeder
	public static final CANDevice leftFeederMotorID = busFour.id(10);
	public static final CANDevice rightFeederMotorID = busFour.id(9);

	// Shooter
	// | Hood
	public static final CANDevice hoodMotorID = busFour.id(11);
	// | Flywheels
	// | | Left Flywheel System
	public static final CANDevice leftBottomFlywheelMotorID = busFour.id(12);
	public static final CANDevice leftTopFlywheelMotorID = busFour.id(13);
	// | | Right Flywheel System
	public static final CANDevice rightBottomFlywheelMotorID = busFour.id(14);
	public static final CANDevice rightTopFlywheelMotorID = busFour.id(15);

	// Climber
	// | Hook
	public static final CANDevice climberHookMotorID = busFour.id(16);

	// CANdi
	public static final CANDevice candiID = busFive.id(0);
}
