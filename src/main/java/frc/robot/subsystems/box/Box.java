package frc.robot.subsystems.box;

import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Command.InterruptionBehavior;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.util.LoggedTracer;
import frc.util.loggerUtil.tunables.LoggedTunable;
import org.littletonrobotics.junction.Logger;

import java.util.function.DoubleSupplier;

import static edu.wpi.first.units.Units.Volts;

public class Box extends SubsystemBase {
	private final BoxIO io;
	private final BoxIOInputsAutoLogged inputs = new BoxIOInputsAutoLogged();
	private final BoxSensorsIO boxSensors;
	private final BoxSensorsIOInputsAutoLogged boxSensorsInputs = new BoxSensorsIOInputsAutoLogged();

	private static final LoggedTunable<Voltage> idleVoltage = LoggedTunable.from("Box/Idle Volts", Volts::of, 0.0);
	private static final LoggedTunable<Voltage> stageVoltage = LoggedTunable.from("Box/Feed Volts", Volts::of, 2.0);
	private static final LoggedTunable<Voltage> ejectVoltage = LoggedTunable.from("Box/Eject Volts", Volts::of, 2.0);
	private static final LoggedTunable<Voltage> reverseVoltage = LoggedTunable.from("Box/Reverse Volts", Volts::of, -2.0);

	public Box(BoxIO io, BoxSensorsIO boxSensors) {
		super("Box");

		System.out.println("[Init Box] Instantiating Box with " + io.getClass().getSimpleName() + " and SensorsIO " + boxSensors.getClass().getSimpleName());
		this.io = io;
		this.boxSensors = boxSensors;
	}

	@Override
	public void periodic() {
		LoggedTracer.logEpoch("CommandScheduler Periodic/Subsystem/Box/Before");
		this.io.updateInputs(this.inputs);
		this.boxSensors.updateInputs(this.boxSensorsInputs);
		LoggedTracer.logEpoch("CommandScheduler Periodic/Subsystem/Box/Update Inputs");
		Logger.processInputs("Inputs/Box", this.inputs);
		Logger.processInputs("Inputs/BoxSensors", this.boxSensorsInputs);
		LoggedTracer.logEpoch("CommandScheduler Periodic/Subsystem/Box/Process Inputs");
	}

	private Command genVoltsCommand(String name, DoubleSupplier ejectVoltSupplier, DoubleSupplier feedVoltSupplier, InterruptionBehavior interruptionBehavior) {
		final var box = this;
		return new Command() {
			{
				this.setName(name);
				this.addRequirements(box);
			}

			@Override
			public void execute() {
				box.io.setEjectVolts(ejectVoltSupplier.getAsDouble());
				box.io.setFeedVolts(feedVoltSupplier.getAsDouble());
			}

			@Override
			public void end(boolean interrupted) {
				box.io.setEjectVolts(0.0);
				box.io.setFeedVolts(0.0);
			}

			@Override
			public InterruptionBehavior getInterruptionBehavior() {
				return interruptionBehavior;
			}
		};
	}

	public Command idle() {
		return this.genVoltsCommand("Idle", () -> idleVoltage.get().in(Volts), () -> idleVoltage.get().in(Volts), InterruptionBehavior.kCancelSelf);
	}

	public Command passiveStage() {
		return this.genVoltsCommand("Passive Stage", () -> idleVoltage.get().in(Volts), () -> stageVoltage.get().in(Volts), InterruptionBehavior.kCancelSelf);
	}

	public Command stage() {
		return this.genVoltsCommand("Stage", () -> idleVoltage.get().in(Volts), () -> stageVoltage.get().in(Volts), InterruptionBehavior.kCancelSelf);
	}

	public Command eject() {
		return this.genVoltsCommand("Eject", () -> ejectVoltage.get().in(Volts), () -> ejectVoltage.get().in(Volts), InterruptionBehavior.kCancelSelf);
	}

	public Command reverse() {
		return this.genVoltsCommand("Reverse", () -> reverseVoltage.get().in(Volts), () -> reverseVoltage.get().in(Volts), InterruptionBehavior.kCancelSelf);
	}
}
