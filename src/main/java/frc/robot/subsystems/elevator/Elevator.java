package frc.robot.subsystems.elevator;

import edu.wpi.first.math.controller.ElevatorFeedforward;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.Timer;
import frc.util.FFConstants;
import frc.util.LoggedTracer;
import frc.util.NeutralMode;
import frc.util.PIDConstants;
import frc.util.loggerUtil.tunables.LoggedTunable;
import frc.util.robotStructure.linear.ExtenderMech;
import org.littletonrobotics.junction.Logger;

import java.util.Optional;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.InchesPerSecond;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.MetersPerSecondPerSecond;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Seconds;

public class Elevator {
	private final ElevatorIO io;
	private final ElevatorIOInputsAutoLogged inputs = new ElevatorIOInputsAutoLogged();

	private static final LoggedTunable<TrapezoidProfile.Constraints> profileConsts = LoggedTunable.fromDashboardUnits(
		"Superstructure/Elevator/Profile",
		InchesPerSecond,
		InchesPerSecond.per(Second),
		MetersPerSecond,
		MetersPerSecondPerSecond,
		new TrapezoidProfile.Constraints(
			120,
			240
		)
	);

	private static final LoggedTunable<FFConstants> ffConsts = LoggedTunable.from(
		"Superstructure/Elevator/FF",
		new FFConstants(
			0.8,
			0.4,
			2,
			0
		)
	);

	private static final LoggedTunable<PIDConstants> pidConsts = LoggedTunable.from(
		"Superstructure/Elevator/PID",
		new PIDConstants(
			50,
			0,
			0
		)
	);

	private static final LoggedTunable<Distance> autoRezeroMaxLength = LoggedTunable.from("Superstructure/Elevator/Auto Rezero/Max Length", Inches::of, 3);
	private static final LoggedTunable<Current> autoRezeroTorqueCurrentThreshold = LoggedTunable.from("Superstructure/Elevator/Auto Rezero/Torque Current Threshold", Amps::of, -50);
	private static final LoggedTunable<LinearVelocity> autoRezeroMaxVelo = LoggedTunable.from("Superstructure/Elevator/Auto Rezero/Max Velocity", InchesPerSecond::of, 0.5);
	private static final LoggedTunable<Time> autoRezeroDebounceTime = LoggedTunable.from("Superstructure/Elevator/Auto Rezero/Debounce Time", Seconds::of, 1);
	private final Timer autoRezeroDebounceTimer = new Timer();

	private TrapezoidProfile motionProfile = new TrapezoidProfile(profileConsts.get());
	private final State measuredState = new State();
	private final State setpointState = new State();
	private final State goalState = new State();
	private boolean motionProfiling = false;
	private final ElevatorFeedforward feedforward = new ElevatorFeedforward(0,0,0,0);

	private double lengthMeters = 0.0;
	private double velocityMetersPerSec = 0.0;

	public final ExtenderMech stage2Mech = new ExtenderMech(ElevatorConstants.stage2Base);
	public final ExtenderMech stage3Mech = new ExtenderMech(ElevatorConstants.stage3Base);
	public final ExtenderMech stage4Mech = new ExtenderMech(ElevatorConstants.stage4Base);

	private final Alert motorDisconnectedAlert = new Alert("Superstructure/Elevator/Alerts", "Motor Disconnected", AlertType.kError);
	private final Alert encoderDisconnectedAlert = new Alert("Superstructure/Elevator/Alerts", "Encoder Disconnected", AlertType.kError);
	private final Alert motorDisconnectedGlobalAlert = new Alert("Elevator Motor Disconnected!", AlertType.kError);
	private final Alert encoderDisconnectedGlobalAlert = new Alert("Elevator Encoder Disconnected!", AlertType.kError);

	public Elevator(ElevatorIO io) {
		System.out.println("[Init Elevator] Instantiating Elevator with " + io.getClass().getSimpleName());
		this.io = io;

		ffConsts.get().update(this.feedforward);
		this.io.configPID(pidConsts.get());
	}

	public void periodic() {
		LoggedTracer.logEpoch("CommandScheduler Periodic/Subsystem/Superstructure/Elevator/Before");
		this.io.updateInputs(this.inputs);
		LoggedTracer.logEpoch("CommandScheduler Periodic/Subsystem/Superstructure/Elevator/Update Inputs");
		Logger.processInputs("Inputs/Superstructure/Elevator", this.inputs);
		LoggedTracer.logEpoch("CommandScheduler Periodic/Subsystem/Superstructure/Elevator/Process Inputs");

		var stageDistMeters = ElevatorConstants.stage1LinearRelation.radiansToMeters(ElevatorConstants.sensorToMechanism.applyUnsigned(this.inputs.encoder.getPositionRads()));

		this.lengthMeters = stageDistMeters * ElevatorConstants.movingStageCount;
		this.velocityMetersPerSec = ElevatorConstants.stage1LinearRelation.radiansToMeters(ElevatorConstants.sensorToMechanism.applyUnsigned(this.inputs.encoder.getVelocityRadsPerSec())) * ElevatorConstants.movingStageCount;

		this.measuredState.position = this.getLengthMeters();
		this.measuredState.velocity = this.getVelocityMetersPerSec();

		Logger.recordOutput("Superstructure/Elevator/Length/Measured", this.getLengthMeters());
		Logger.recordOutput("Superstructure/Elevator/Velocity/Measured", this.getVelocityMetersPerSec());

		this.stage2Mech.setMeters(stageDistMeters);
		this.stage3Mech.setMeters(stageDistMeters);
		this.stage4Mech.setMeters(stageDistMeters);

		if (profileConsts.hasChanged(hashCode())) {
			this.motionProfile = new TrapezoidProfile(profileConsts.get());
		}
		if (ffConsts.hasChanged(hashCode())) {
			ffConsts.get().update(this.feedforward);
		}
		if (pidConsts.hasChanged(hashCode())) {
			this.io.configPID(pidConsts.get());
		}

		if (
			Math.abs(this.getLengthMeters()) < autoRezeroMaxLength.get().in(Meters)
			&& Math.abs(this.getVelocityMetersPerSec()) < autoRezeroMaxVelo.get().in(MetersPerSecond)
			&& this.inputs.motor.motor.getTorqueCurrentAmps() < autoRezeroTorqueCurrentThreshold.get().in(Amps)
		) {
			this.autoRezeroDebounceTimer.start();
		} else {
			this.autoRezeroDebounceTimer.stop();
			this.autoRezeroDebounceTimer.reset();
		}
		if (this.autoRezeroDebounceTimer.hasElapsed(autoRezeroDebounceTime.get().in(Seconds))) {
			this.io.configMagnetOffset(this.inputs.encoderMagnetOffsetRads - this.inputs.encoder.getPositionRads());
			this.autoRezeroDebounceTimer.reset();
		}
		Logger.recordOutput("Superstructure/Elevator/Auto Rezero Debounce Timer", this.autoRezeroDebounceTimer.get());

		this.motorDisconnectedAlert.set(!this.inputs.motorConnected);
		this.encoderDisconnectedAlert.set(!this.inputs.encoderConnected);
		this.motorDisconnectedGlobalAlert.set(!this.inputs.motorConnected);
		this.encoderDisconnectedGlobalAlert.set(!this.inputs.encoderConnected);

		LoggedTracer.logEpoch("CommandScheduler Periodic/Subsystem/Superstructure/Elevator/Periodic");
		LoggedTracer.logEpoch("CommandScheduler Periodic/Subsystem/Superstructure/Elevator");
	}

	public double getLengthMeters() {
		return this.lengthMeters;
	}
	public double getVelocityMetersPerSec() {
		return this.velocityMetersPerSec;
	}
	public double getAppliedVolts() {
		return this.inputs.motor.motor.getAppliedVolts();
	}

	public void setVolts(double volts) {
		this.motionProfiling = false;
		this.io.setVolts(volts);
	}
	public void stop(Optional<NeutralMode> neutralMode) {
		this.motionProfiling = false;
		this.io.stop(neutralMode);
	}
}
