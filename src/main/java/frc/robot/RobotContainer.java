// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Seconds;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.event.EventLoop;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import frc.robot.auto.AutoManager;
import frc.robot.auto.AutoSelector;
import frc.robot.constants.FieldConstants;
import frc.robot.constants.RobotConstants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.DriveConstants;
import frc.robot.subsystems.drive.commands.WheelRadiusCalibration;
import frc.robot.subsystems.drive.gyro.GyroIO;
import frc.robot.subsystems.drive.gyro.GyroIOPigeon2;
import frc.robot.subsystems.drive.modules.ModuleIO;
import frc.robot.subsystems.drive.modules.ModuleIOFalcon550;
import frc.robot.subsystems.drive.modules.ModuleIOSim;
import frc.robot.subsystems.drive.odometry.OdometryTimestampIO;
import frc.robot.subsystems.drive.odometry.OdometryTimestampIOOdometryThread;
import frc.robot.subsystems.drive.odometry.OdometryTimestampIOSim;
import frc.robot.subsystems.vision.apriltag.ApriltagVision;
import frc.robot.subsystems.vision.object.ObjectVision;
import frc.util.EdgeDetector;
import frc.util.PIDGains;
import frc.util.Perspective;
import frc.util.controllers.XboxController;
import frc.util.flipping.AllianceFlipUtil;
import frc.util.flipping.AllianceFlipped;
import frc.util.loggerUtil.tunables.LoggedTunable;
import frc.util.loggerUtil.tunables.LoggedTunableNumber;
import frc.util.robotStructure.Mechanism3d;

public class RobotContainer {
	// Subsystems
	public final Drive drive;

	// Vision
	public final ApriltagVision apriltagVision;
	public final ObjectVision objectVision;

	// public final Camera hopperCamera;
	// public final Camera hubZoomCamera;
	// public final Camera leftBroadCamera;
	// public final Camera rightBroadCamera;
	// public final Camera backBroadCamera;
	// public final Camera intakeCamera;

	// Auto
	public final AutoManager autoManager;

	// Event Loops
	public final EventLoop automationsLoop = new EventLoop();

	// Controllers
	private final XboxController driveController = new XboxController(0, "Drive Controller");

	@SuppressWarnings("unused")
	private final CommandJoystick simJoystick = new CommandJoystick(5);

	@SuppressWarnings("resource")
	public RobotContainer() {
		System.out.println("[Init RobotContainer] Creating " + RobotType.getMode().name() + " " + RobotType.getRobot().name());

		// Initialize subsystems with appropriate IO
		switch (RobotType.getMode()) {
			case REAL -> {
				this.drive = new Drive(
					new OdometryTimestampIOOdometryThread(),
					new GyroIOPigeon2(),
					Arrays.stream(DriveConstants.moduleConstants)
						.map(ModuleIOFalcon550::new)
						.toArray(ModuleIO[]::new)
				);
			}
			case SIM -> {
				this.drive = new Drive(
					new OdometryTimestampIOSim(),
					new GyroIO() {},
					Arrays.stream(DriveConstants.moduleConstants)
						.map(ModuleIOSim::new)
						.toArray(ModuleIO[]::new)
				);
			}
			default -> {
				this.drive = new Drive(
					new OdometryTimestampIO() {},
					new GyroIO() {},
					new ModuleIO(){},
					new ModuleIO(){},
					new ModuleIO(){},
					new ModuleIO(){}
				);
			}
		}

		// Initialize vision systems with camera pipelines
		this.apriltagVision = new ApriltagVision(
			// new ApriltagPipeline(this.hopperCamera, 0, 1.0)
			// new ApriltagPipeline(this.hubZoomCamera, 0, 1.0)
			// new ApriltagPipeline(this.leftBroadCamera, 0, 2.0),
			// new ApriltagPipeline(this.rightBroadCamera, 0, 2.0),
			// new ApriltagPipeline(this.backBroadCamera, 0, 2.0)
		);
		this.objectVision = new ObjectVision(

		);

		// Setup robot structure
		// this.drive.structureRoot
	// 	.addChild(this.backBroadCamera.mount)
		// ;

		// Register Mechanism3ds
		Mechanism3d.registerMechs(
			// this.climber.hook.mech
		);

		System.out.println("[Init RobotContainer] Configuring Commands");
		this.configureCommands();

		System.out.println("[Init RobotContainer] Configuring Notifications");

		System.out.println("[Init RobotContainer] Configuring Autonomous Modes");

		final var autoSelector = new AutoSelector("Auto Selector");
		// Add autonomous routines to autonomous selector
		// autoSelector.addDefaultRoutine(new ScoreFuel(this));
		// autoSelector.addRoutine(new DoubleSwipe(this));
		// autoSelector.addRoutine(new ResetPosition());
		// autoSelector.addRoutine(new Preloads(this));

		this.autoManager = new AutoManager(autoSelector);

		System.out.println("[Init RobotContainer] Configuring System Check");
		SmartDashboard.putData("System Check/Drive/Spin",
			new Command() {
				private final Drive.Rotational rotationalSubsystem = drive.rotationalSubsystem;
				private final Timer timer = new Timer();
				{
					addRequirements(this.rotationalSubsystem);
					setName("TEST Spin");
				}
				public void initialize() {
					this.timer.restart();
				}
				public void execute() {
					this.rotationalSubsystem.driveVelocity(Math.sin(this.timer.get()) * 3);
				}
				public void end(boolean interrupted) {
					this.timer.stop();
					this.rotationalSubsystem.stop();
				}
			}
		);
		SmartDashboard.putData("System Check/Drive/Circle",
			new Command() {
				private final Drive.Translational translationSubsystem = drive.translationSubsystem;
				private final Timer timer = new Timer();
				{
					addRequirements(this.translationSubsystem);
					setName("TEST Circle");
				}
				public void initialize() {
					this.timer.restart();
				}
				public void execute() {
					this.translationSubsystem.driveVelocity(
						new ChassisSpeeds(
							Math.cos(this.timer.get()) * 0.01,
							Math.sin(this.timer.get()) * 0.01,
							0
						)
					);
				}
				public void end(boolean interrupted) {
					this.timer.stop();
					this.translationSubsystem.stop();
				}
			}
		);

		SmartDashboard.putData("Wheel Calibration", Commands.defer(
			() ->
				new WheelRadiusCalibration(
					drive,
					WheelRadiusCalibration.VOLTAGE_RAMP_RATE.get(),
					WheelRadiusCalibration.MAX_VOLTAGE.get()
				)
				.withName("Wheel Calibration"),
				Set.of(drive.translationSubsystem, drive.rotationalSubsystem)
			)
		);

		if (RobotConstants.tuningMode) {
			new Alert("Tuning mode active", AlertType.kInfo).set(true);
		}
	}

	private void configureCommands() {
		// Construct Commands
		final var translationJoystick = this.driveController.leftStick
			.smoothRadialDeadband(0.05)
			.radialSensitivity(0.5)
		;
		final Supplier<ChassisSpeeds> desiredTranslationalRobotVelo = () -> {
			var joyX = +translationJoystick.y().getAsDouble();
			var joyY = -translationJoystick.x().getAsDouble();

			var perspectiveForward = Perspective.getCurrent().getForwardDirection();
			var fieldX = joyX * perspectiveForward.getCos() - joyY * perspectiveForward.getSin();
			var fieldY = joyX * perspectiveForward.getSin() + joyY * perspectiveForward.getCos();

			var robotRot = RobotState.getInstance().getEstimatedGlobalPose().getRotation();
			var robotX = fieldX * robotRot.getCos() - fieldY * -robotRot.getSin();
			var robotY = fieldX * -robotRot.getSin() + fieldY * robotRot.getCos();

			var driveX = robotX * DriveConstants.maxDriveSpeed.in(MetersPerSecond);
			var driveY = robotY * DriveConstants.maxDriveSpeed.in(MetersPerSecond);

			return new ChassisSpeeds(driveX, driveY, 0.0);
		};
		final var rotateAxis = this.driveController.leftTrigger
			.add(this.driveController.rightTrigger.invert())
			.smoothDeadband(0.01)
			.sensitivity(0.75)
		;
		final var flickJoystick = this.driveController.rightStick
			.roughRadialDeadband(0.75)
		;
		final var driveTranslationCommand = new Command() {
			{
				this.setName("Driver Controlled");
				this.addRequirements(drive.translationSubsystem);
			}

			@Override
			public void execute() {
				var joyX = +translationJoystick.y().getAsDouble();
				var joyY = -translationJoystick.x().getAsDouble();

				var perspectiveForward = Perspective.getCurrent().getForwardDirection();
				var fieldX = joyX * perspectiveForward.getCos() - joyY * perspectiveForward.getSin();
				var fieldY = joyX * perspectiveForward.getSin() + joyY * perspectiveForward.getCos();

				var robotRot = RobotState.getInstance().getEstimatedGlobalPose().getRotation();
				var robotX = fieldX * robotRot.getCos() - fieldY * -robotRot.getSin();
				var robotY = fieldX * -robotRot.getSin() + fieldY * robotRot.getCos();

				var driveX = robotX * DriveConstants.maxDriveSpeed.in(MetersPerSecond);
				var driveY = robotY * DriveConstants.maxDriveSpeed.in(MetersPerSecond);

				drive.translationSubsystem.driveVelocity(driveX, driveY);
			}

			@Override
			public void end(boolean interrupted) {
				drive.translationSubsystem.stop();
			}
		};
		final var driveRotateCommand = new Command() {
			{
				this.setName("Drive Controlled");
				this.addRequirements(drive.rotationalSubsystem);
			}

			@Override
			public void execute() {
				var omega = rotateAxis.getAsDouble() * DriveConstants.maxTurnRate.in(RadiansPerSecond) * 0.5;

				drive.rotationalSubsystem.driveVelocity(omega);
			}

			@Override
			public void end(boolean interrupted) {
				drive.rotationalSubsystem.stop();
			}
		};
		final var driveFlickStick = new Command() {
			private static final LoggedTunable<PIDGains> pidGains = LoggedTunable.from(
				"Controls/Flick Stick/PID",
				new PIDGains(
					5.0,
					0.0,
					0.0
				)
			);
			private static final LoggedTunable<Angle> angularThreshold = LoggedTunable.from("Controls/Flick Stick/Threshold", Degrees::of, 2.0);
			private static final LoggedTunable<Time> preciseTimeThreshold = LoggedTunable.from("Controls/Flick Stick/Precise Time", Seconds::of, 0.5);

			private static final AllianceFlipped<Rotation2d[]> snapPoints;
			static {
				final var blueArray = new Rotation2d[] {
					Rotation2d.kZero,
					Rotation2d.kCCW_90deg,
					Rotation2d.k180deg,
					Rotation2d.kCW_90deg,
				};
				final var redArray = new Rotation2d[blueArray.length];
				for (int i = 0; i < blueArray.length; i++) {
					redArray[i] = AllianceFlipUtil.flip(blueArray[i]);
				}
				snapPoints = new AllianceFlipped<>(blueArray, redArray);
			}

			private final PIDController pid = pidGains.get().update(new PIDController(0.0, 0.0, 0.0));
			private final Timer preciseTimer = new Timer();

			private double targetHeadingRads = 0.0;

			{
				this.setName("Flick Stick");
				this.addRequirements(drive.rotationalSubsystem);

				this.pid.enableContinuousInput(-Math.PI, Math.PI);
			}

			@Override
			public void initialize() {
				this.execute();
			}

			@Override
			public void execute() {
				if (flickJoystick.magnitude() > 0.0) {
					var flickRads = flickJoystick.radsFromPosYCCW();
					var flickX = Math.cos(flickRads);
					var flickY = Math.sin(flickRads);

					var perspectiveForward = Perspective.getCurrent().getForwardDirection();
					var fieldX = flickX * perspectiveForward.getCos() - flickY * perspectiveForward.getSin();
					var fieldY = flickX * perspectiveForward.getSin() + flickY * perspectiveForward.getCos();

					this.preciseTimer.start();
					if (this.preciseTimer.hasElapsed(preciseTimeThreshold.get().in(Seconds))) {
						this.targetHeadingRads = Math.atan2(fieldY, fieldX);
					} else {
						var ourSnapPoints = snapPoints.getOurs();
						Rotation2d closestSnapPoint = null;
						var closestDot = Double.NEGATIVE_INFINITY;
						for (int i = 0; i < ourSnapPoints.length; i++) {
							var snapPoint = ourSnapPoints[i];
							var dot = fieldX * snapPoint.getCos() + fieldY * snapPoint.getSin();
							if (dot >= closestDot) {
								closestDot = dot;
								closestSnapPoint = snapPoint;
							}
						}
						this.targetHeadingRads = closestSnapPoint.getRadians();
					}
				} else {
					this.preciseTimer.stop();
					this.preciseTimer.reset();
				}

				var pidOut = this.pid.calculate(RobotState.getInstance().getEstimatedGlobalPose().getRotation().getRadians(), this.targetHeadingRads);
				drive.rotationalSubsystem.driveVelocity(pidOut);
			}

			@Override
			public void end(boolean interrupted) {
				drive.rotationalSubsystem.stop();
				this.preciseTimer.stop();
				this.preciseTimer.reset();
			}

			@Override
			public boolean isFinished() {
				var robotRot = RobotState.getInstance().getEstimatedGlobalPose().getRotation();
				var targetHeadingX = Math.cos(this.targetHeadingRads);
				var targetHeadingY = Math.sin(this.targetHeadingRads);
				var dot = robotRot.getCos() * targetHeadingX + robotRot.getSin() * targetHeadingY;
				var thresholdDot = Math.cos(angularThreshold.get().in(Radians));
				return flickJoystick.magnitude() <= 0.0 && dot >= thresholdDot;
			}
		};

		final var driveTankCommand = new Command() {
			private static final LoggedTunableNumber offsetThreshold = LoggedTunable.from("Controls/Tank Drive/Offset/Threshold", 0.25);
			private static final LoggedTunable<Angle> offsetAngle = LoggedTunable.from("Controls/Tank Drive/Offset/Angle", Degrees::of, 20.0);

			private static final LoggedTunable<LinearVelocity> linearThreshold = LoggedTunable.from("Controls/Tank Drive/Velo Threshold", MetersPerSecond::of, 0.5);
			private static final LoggedTunable<AngularVelocity> maxOmega = LoggedTunable.from("Controls/Tank Drive/Max Omega", RotationsPerSecond::of, 1.5);

			private static final LoggedTunable<PIDGains> pidGains = LoggedTunable.from(
				"Controls/Tank Drive/Azimuth PID",
				new PIDGains(
					3.5,
					0.0,
					0.0
				)
			);
			private final PIDController pid = new PIDController(pidGains.get().kP(), pidGains.get().kI(), pidGains.get().kD());

			{
				this.setName("Tank");
				this.addRequirements(drive.translationSubsystem, drive.rotationalSubsystem);

				pid.enableContinuousInput(-Math.PI, Math.PI);
			}

			private double targetHeadingRads = 0.0;

			@Override
			public void initialize() {
				if (pidGains.hasChanged(this.hashCode())) {
					pidGains.get().update(this.pid);
				}

				this.targetHeadingRads = RobotState.getInstance().getEstimatedGlobalPose().getRotation().getRadians();
			}

			@Override
			public void execute() {
				var joyX = +translationJoystick.y().getAsDouble();
				var joyY = -translationJoystick.x().getAsDouble();

				var perspectiveForward = Perspective.getCurrent().getForwardDirection();
				var fieldX = joyX * perspectiveForward.getCos() - joyY * perspectiveForward.getSin();
				var fieldY = joyX * perspectiveForward.getSin() + joyY * perspectiveForward.getCos();

				var driveX = fieldX * DriveConstants.maxDriveSpeed.in(MetersPerSecond);
				var driveY = fieldY * DriveConstants.maxDriveSpeed.in(MetersPerSecond);

				var robotRot = RobotState.getInstance().getEstimatedGlobalPose().getRotation();
				double robotX;
				double robotY;

				if (Math.hypot(driveX, driveY) > linearThreshold.get().in(MetersPerSecond)) {
					var rawTargetHeadingRads = Math.atan2(fieldY, fieldX);
					var offsetRads = 0.0;
					if (rotateAxis.getAsDouble() >= offsetThreshold.getAsDouble()) {
						offsetRads = +offsetAngle.get().in(Radians);
					} else if (rotateAxis.getAsDouble() <= -offsetThreshold.getAsDouble()) {
						offsetRads = -offsetAngle.get().in(Radians);
					}
					var robotHeadingOffsetRads = MathUtil.angleModulus(robotRot.getRadians() - offsetRads);
					this.targetHeadingRads = MathUtil.angleModulus(rawTargetHeadingRads + offsetRads);
					var offsetX = Math.max(driveX * +Math.cos(robotHeadingOffsetRads) - driveY * -Math.sin(robotHeadingOffsetRads), 0.0);
					robotX = offsetX * +Math.cos(offsetRads);
					robotY = offsetX * -Math.sin(offsetRads);
				} else {
					robotX = driveX * +robotRot.getCos() - driveY * -robotRot.getSin();
					robotY = driveX * -robotRot.getSin() + driveY * +robotRot.getCos();
				}

				var pidOut = this.pid.calculate(robotRot.getRadians(), this.targetHeadingRads);

				var omega = MathUtil.clamp(pidOut, -maxOmega.get().in(RadiansPerSecond), +maxOmega.get().in(RadiansPerSecond));

				drive.translationSubsystem.driveVelocity(robotX, robotY);
				drive.rotationalSubsystem.driveVelocity(omega);
			}

			@Override
			public void end(boolean interrupted) {
				drive.translationSubsystem.stop();
				drive.rotationalSubsystem.stop();
			}
		};

		// this.hubZoomCamera.setDefaultCommand(this.hubZoomCamera.setPipelineIndex(0));
		// this.leftBroadCamera.setDefaultCommand(this.leftBroadCamera.setPipelineIndex(0));
		// this.rightBroadCamera.setDefaultCommand(this.rightBroadCamera.setPipelineIndex(0));
		// this.backBroadCamera.setDefaultCommand(this.backBroadCamera.setPipelineIndex(0));
		// this.intakeCamera.setDefaultCommand(this.intakeCamera.setPipelineIndex(0));

		// Bind automations
		// this.automationsLoop.bind(new BumpMitigation(this.drive));
		// this.automationsLoop.bind(new TrenchMitigation(this.drive, this.intake.slam, this.extensionSystem, this.shooter.hood, intakeDeployCommand));
		// this.automationsLoop.bind(new IntakeDeployHysteresis(this.intake.slam, intakeDeployCommand));
		// this.automationsLoop.bind(new HookAutoDeployHysteresis(this.climber.hook, climberHookAutoDeployCommand));
		// this.automationsLoop.bind(new AutoSpinUp(this.drive, this.shooter, intakeRollersIntakeCommand));
		// this.automationsLoop.bind(new AutoDriveAim(this.drive, this.shooter, intakeRollersIntakeCommand));

		// Setup position reset command
		this.driveController.leftStickButton().and(this.driveController.rightStickButton()).onTrue(Commands.runOnce(() -> RobotState.getInstance().resetPose(FieldConstants.hubIntakeFrontRobotPose.getOurs())).ignoringDisable(true));
		this.driveController.rightStickButton().onTrue(Commands.runOnce(() -> RobotState.getInstance().resetPose(new Pose2d(FieldConstants.hubIntakeFrontRobotPose.getOurs().getTranslation(), RobotState.getInstance().getEstimatedGlobalPose().getRotation()))).ignoringDisable(true));
		/*
		 * (A)
		 *  | Press: Deploy intake (if not deployed) and roll in
		 *  | Double Press: Retract intake
		 */
		final var intakeEdge = new EdgeDetector(false);
		final var intakeSupplier = this.driveController.a();
		final var intakeDoublePressThreshold = LoggedTunable.from("Controls/Intake/Double Press Threshold", Seconds::of, 0.25);
		final var intakeDoublePressTimer = new Timer();
		final var intakeForceHopperAgitateEdge = new EdgeDetector(false);
		final var intakeForceHopperAgitateSupplier = this.driveController.povUp();
		final var tankEdge = new EdgeDetector(false);
		final var tankSupplier = this.driveController.leftBumper();
	}
}
