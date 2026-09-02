package first.util.led.animation;

import org.wpilib.driverstation.Alliance;
import org.wpilib.driverstation.MatchState;
import org.wpilib.system.Timer;
import org.wpilib.util.Color;

import first.util.led.functions.Gradient;
import first.util.led.functions.InterpolationFunction;
import first.util.led.functions.WaveFunction;
import first.util.led.strips.LEDStrip;

public class AllianceColorAnimation {
	private final LEDStrip strip;
	// private static final Color blueAllianceColor = Color.kFirstBlue;
	// private static final Color redAllianceColor = Color.kFirstRed;
	private final Gradient unknownGradient;
	private final Gradient blueGradient;
	private final Gradient redGradient;

	public AllianceColorAnimation(LEDStrip strip, Color blueAllianceColor, Color redAllianceColor) {
		this.strip = strip;
		this.unknownGradient = InterpolationFunction.linear.gradient(blueAllianceColor, redAllianceColor);
		this.blueGradient = InterpolationFunction.linear.gradient(blueAllianceColor, Color.BLACK);
		this.redGradient = InterpolationFunction.linear.gradient(redAllianceColor, Color.BLACK);
	}

	public void apply() {
		strip.apply((pos) -> {
			var alliance = MatchState.getAlliance();
			Gradient gradient;
			if (alliance.isEmpty()) {
				gradient = unknownGradient;
			} else {
				if (alliance.get() == Alliance.BLUE) {
					gradient = blueGradient;
				} else {
					gradient = redGradient;
				}
			}
			return gradient.apply(
				WaveFunction.Sinusoidal.applyAsDouble(
					pos * 4 - Timer.getTimestamp()
				)
			);
		});
	}
}
