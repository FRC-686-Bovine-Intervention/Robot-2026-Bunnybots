package first.util.led.strips.adapters;

import org.wpilib.util.Color;

import first.util.led.strips.LEDStrip;

public class SubStrip implements LEDStrip {
	private final LEDStrip strip;
	private final int startIndex;
	private final int endIndex;

	public SubStrip(int startIndex, LEDStrip strip) {
		this(startIndex, strip.getLength(), strip);
	}
	public SubStrip(int startIndex, int endIndex, LEDStrip strip) {
		this.startIndex = Math.max(startIndex, 0);
		this.endIndex = Math.min(endIndex, strip.getLength());
		this.strip = strip;
	}

	@Override
	public int getLength() {
		return this.endIndex - this.startIndex;
	}

	@Override
	public void setLED(int ledIndex, Color color) {
		int clamped = Math.max(0, Math.min(ledIndex, getLength() - 1));
		this.strip.setLED(clamped + this.startIndex, color);
	}
}
