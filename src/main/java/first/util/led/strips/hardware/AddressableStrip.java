package first.util.led.strips.hardware;

import org.wpilib.hardware.led.AddressableLED;
import org.wpilib.hardware.led.AddressableLEDBuffer;
import org.wpilib.util.Color;
import org.wpilib.util.Color8Bit;

import first.util.hardwareID.systemcorePorts.PWMPort;

public class AddressableStrip implements HardwareStrip {
	private final AddressableLED strip;
	private final AddressableLEDBuffer buffer;

	public AddressableStrip(PWMPort port, int length) {
		this(port.addressableLED(), length);
	}

	public AddressableStrip(AddressableLED leds, int length) {
		this.strip = leds;
		this.buffer = new AddressableLEDBuffer(length);
		this.strip.setLength(this.buffer.getLength());
		this.strip.setData(this.buffer);
	}

	@Override
	public int getLength() {
		return this.buffer.getLength();
	}

	@Override
	public void setLED(int ledIndex, Color color) {
		var color8bit = new Color8Bit(color);
		this.buffer.setRGB(ledIndex, color8bit.red, color8bit.green, color8bit.blue);
	}

	@Override
	public void refresh() {
		this.strip.setData(this.buffer);
	}
}
