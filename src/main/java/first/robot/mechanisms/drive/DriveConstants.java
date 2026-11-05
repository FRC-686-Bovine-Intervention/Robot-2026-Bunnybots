package first.robot.mechanisms.drive;

public class DriveConstants {
    
    public static class ModuleConstants {
		public final String name;
		public final CANDevice driveMotorID;
		public final CANDevice azimuthMotorID;
        public final Optional<CANDevice> azimuthEncoderID;
		public final InvertedValue driveInverted;
		public final Angle encoderZeroOffset;
		public final Transform2d moduleTransform;
		public final Rotation2d positiveRotVec;
		ModuleConstants(String name, CANDevice driveMotorID, CANDevice turnMotorID, Optional<CANDevice> azimuthEncoderID, InvertedValue driveInverted, Angle encoderZeroOffset, Transform2d moduleTransform) {
			this.name = name;
			this.driveMotorID = driveMotorID;
			this.azimuthMotorID = turnMotorID;
			this.azimuthEncoderID = azimuthEncoderID;
			this.driveInverted = driveInverted;
			this.encoderZeroOffset = encoderZeroOffset;
			this.moduleTransform = moduleTransform;
			this.positiveRotVec = this.moduleTransform.getTranslation().getAngle().plus(Rotation2d.kCCW_90deg);
		}
	}

    public static final ModuleConstants[] moduleConstants = {
		new ModuleConstants(
			"Front Left",
			HardwareDevices.frontLeftDriveMotorID, HardwareDevices.frontLeftAzimuthMotorID, Optional.of(HardwareDevices.frontLeftAzimuthEncoderID),
			InvertedValue.CounterClockwise_Positive,
			Rotations.of(0.3238442),
			new Transform2d(
				new Translation2d(
					trackWidthX.div(+2),
					trackWidthY.div(+2)
				),
				Rotation2d.kCCW_90deg
			)
		),
		new ModuleConstants(
			"Front Right",
			HardwareDevices.frontRightDriveMotorID, HardwareDevices.frontRightAzimuthMotorID, Optional.of(HardwareDevices.frontRightAzimuthEncoderID),
			InvertedValue.CounterClockwise_Positive,
			Rotations.of(0.8459126),
			new Transform2d(
				new Translation2d(
					trackWidthX.div(+2),
					trackWidthY.div(-2)
				),
				Rotation2d.kZero
			)
		),
		new ModuleConstants(
			"Back Left",
			HardwareDevices.backLeftDriveMotorID, HardwareDevices.backLeftAzimuthMotorID, Optional.of(HardwareDevices.backLeftAzimuthEncoderID),
			InvertedValue.CounterClockwise_Positive,
			Rotations.of(0.000863050503),
			new Transform2d(
				new Translation2d(
					trackWidthX.div(-2),
					trackWidthY.div(+2)
				),
				Rotation2d.k180deg
			)
		),
		new ModuleConstants(
			"Back Right",
			HardwareDevices.backRightDriveMotorID, HardwareDevices.backRightAzimuthMotorID, Optional.of(HardwareDevices.backRightAzimuthEncoderID),
			InvertedValue.CounterClockwise_Positive,
			Rotations.of(0.5872595),
			new Transform2d(
				new Translation2d(
					trackWidthX.div(-2),
					trackWidthY.div(-2)
				),
				Rotation2d.kCW_90deg
			)
		),
	};
}
