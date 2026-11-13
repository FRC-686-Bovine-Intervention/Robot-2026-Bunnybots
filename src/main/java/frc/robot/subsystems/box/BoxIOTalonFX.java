package frc.robot.subsystems.box;

import java.util.Optional;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.CoastOut;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.StaticBrake;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import frc.robot.constants.HardwareDevices;
import frc.robot.constants.RobotConstants;
import frc.util.loggerUtil.inputs.LoggedMotor.MotorStatusSignalCache;
import frc.util.NeutralMode;

public class BoxIOTalonFX implements BoxIO {
	private final TalonFX ejectMotor = HardwareDevices.boxEjectMotorID.talonFX();
	private final TalonFX feedMotor = HardwareDevices.boxFeedMotorID.talonFX();

	private final MotorStatusSignalCache ejectMotorStatusSignalCache;
	private final MotorStatusSignalCache feedMotorStatusSignalCache;

	private final BaseStatusSignal[] refreshSignals;
	private final BaseStatusSignal[] ejectMotorConnectedSignals;
	private final BaseStatusSignal[] feedMotorConnectedSignals;

	private final VoltageOut voltageRequest = new VoltageOut(0);
	private final NeutralOut neutralOutRequest = new NeutralOut();
	private final CoastOut coastOutRequest = new CoastOut();
	private final StaticBrake staticBrakeRequest = new StaticBrake();

	public BoxIOTalonFX() {
		var config = new TalonFXConfiguration();
		config.MotorOutput
			.withInverted(InvertedValue.Clockwise_Positive)
			.withNeutralMode(NeutralModeValue.Brake)
		;
		this.ejectMotor.getConfigurator().apply(config);
		this.feedMotor.getConfigurator().apply(config);

		this.ejectMotorStatusSignalCache = MotorStatusSignalCache.from(this.ejectMotor);
		this.feedMotorStatusSignalCache = MotorStatusSignalCache.from(this.feedMotor);

		this.refreshSignals = new BaseStatusSignal[] {
			this.ejectMotorStatusSignalCache.appliedVoltage(),
			this.ejectMotorStatusSignalCache.statorCurrent(),
			this.ejectMotorStatusSignalCache.supplyCurrent(),
			this.ejectMotorStatusSignalCache.torqueCurrent(),
			this.ejectMotorStatusSignalCache.deviceTemperature(),
		};
		this.ejectMotorConnectedSignals = new BaseStatusSignal[] {
			this.ejectMotorStatusSignalCache.appliedVoltage(),
			this.ejectMotorStatusSignalCache.statorCurrent(),
			this.ejectMotorStatusSignalCache.supplyCurrent(),
			this.ejectMotorStatusSignalCache.torqueCurrent(),
			this.ejectMotorStatusSignalCache.deviceTemperature(),
		};
		this.feedMotorConnectedSignals = new BaseStatusSignal[] {
			this.feedMotorStatusSignalCache.appliedVoltage(),
			this.feedMotorStatusSignalCache.statorCurrent(),
			this.feedMotorStatusSignalCache.supplyCurrent(),
			this.feedMotorStatusSignalCache.torqueCurrent(),
			this.feedMotorStatusSignalCache.deviceTemperature(),
		};

		BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.rioUpdateFrequency, this.ejectMotorStatusSignalCache.getStatusSignals());
		BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.rioUpdateFrequency, this.feedMotorStatusSignalCache.getStatusSignals());
		// BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.deviceFaultUpdateFrequency, FaultType.getFaultStatusSignals(this.motor));
		// BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.deviceFaultUpdateFrequency, FaultType.getStickyFaultStatusSignals(this.motor));
		this.ejectMotor.optimizeBusUtilization();
		this.feedMotor.optimizeBusUtilization();
	}

	@Override
	public void updateInputs(BoxIOInputs inputs) {
		BaseStatusSignal.refreshAll(this.refreshSignals);
		inputs.ejectMotorConnected = BaseStatusSignal.isAllGood(this.ejectMotorConnectedSignals);
		inputs.feedMotorConnected = BaseStatusSignal.isAllGood(this.feedMotorConnectedSignals);
		inputs.ejectMotor.updateFrom(this.ejectMotorStatusSignalCache);
		inputs.feedMotor.updateFrom(this.feedMotorStatusSignalCache);
	}

	@Override
	public void setEjectVolts(double volts) {
		this.ejectMotor.setControl(this.voltageRequest
			.withOutput(volts)
		);
	}

	@Override
	public void setFeedVolts(double volts) {
		this.feedMotor.setControl(this.voltageRequest
			.withOutput(volts)
		);
	}

	@Override
	public void stopEjectMotor(Optional<NeutralMode> neutralMode) {
		NeutralMode.selectControlRequest(neutralMode, neutralOutRequest, coastOutRequest, staticBrakeRequest);
	}
}
