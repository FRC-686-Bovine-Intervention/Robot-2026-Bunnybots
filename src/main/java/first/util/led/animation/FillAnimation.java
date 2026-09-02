package first.util.led.animation;

import org.wpilib.util.Color;

import first.util.led.strips.LEDStrip;

public class FillAnimation extends LEDAnimation {
	private final LEDStrip strip;
	private final Color color;

	public FillAnimation(LEDStrip strip, Color color) {
		this.strip = strip;
		this.color = color;
	}

	public void apply() {
		strip.apply(color);
	}
}
