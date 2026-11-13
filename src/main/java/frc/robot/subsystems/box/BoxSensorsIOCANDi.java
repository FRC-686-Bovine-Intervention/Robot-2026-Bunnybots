package frc.robot.subsystems.box;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.CANdiConfiguration;
import com.ctre.phoenix6.hardware.CANdi;
import com.ctre.phoenix6.signals.S1CloseStateValue;
import com.ctre.phoenix6.signals.S1FloatStateValue;
import com.ctre.phoenix6.signals.S2CloseStateValue;
import com.ctre.phoenix6.signals.S2FloatStateValue;
import frc.robot.constants.HardwareDevices;

public class BoxSensorsIOCANDi implements BoxSensorsIO {
	private final CANdi candi = HardwareDevices.candiID.candi();

	private final StatusSignal<Boolean> s2Signal;

	private final BaseStatusSignal[] refreshSignals;

	public BoxSensorsIOCANDi() {
		var config = new CANdiConfiguration();

		config.DigitalInputs
			.withS1FloatState(S1FloatStateValue.FloatDetect)
			.withS1CloseState(S1CloseStateValue.CloseWhenLow)
			.withS2FloatState(S2FloatStateValue.FloatDetect)
			.withS2CloseState(S2CloseStateValue.CloseWhenLow)
		;

		this.candi.getConfigurator().apply(config);

		this.s2Signal = this.candi.getS2Closed();

		this.refreshSignals = new BaseStatusSignal[] {
			this.s2Signal,
		};
	}

	@Override
	public void updateInputs(BoxSensorsIOInputs inputs) {
		BaseStatusSignal.refreshAll(this.refreshSignals);

		inputs.ejectSensor = this.s2Signal.getValue();
	}
}
