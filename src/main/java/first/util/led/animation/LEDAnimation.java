package first.util.led.animation;

import org.wpilib.command3.Command;

public abstract class LEDAnimation {
	public boolean flag;

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public Command setFlagCommand() {
		return Command.noRequirements(coroutine -> {
			setFlag(true);
			while (true) {
				coroutine.yield();
			}
		})
		.whenCanceled(() -> setFlag(false))
		.until(() -> !flag)
		.named("Set Flag");
	}

	public void applyIfFlagged() {
		if (flag) {
			apply();
		}
	}
	public abstract void apply();
}
