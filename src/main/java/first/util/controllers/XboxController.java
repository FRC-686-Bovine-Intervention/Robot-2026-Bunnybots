package first.util.controllers;

import org.wpilib.command3.Scheduler;
import org.wpilib.command3.Trigger;
import org.wpilib.command3.Mechanism;
import org.wpilib.driverstation.GenericHID.RumbleType;

import first.util.controllers.Joystick.Axis;

public class XboxController {
	public final org.wpilib.driverstation.Gamepad hid;
	public final Joystick leftStick;
	public final Joystick rightStick;

	public final Axis leftTrigger;
	public final Axis rightTrigger;

	public final RumbleSystem leftRumble;
	public final RumbleSystem rightRumble;

	public XboxController(int port, String name) {
		this.hid = new org.wpilib.driverstation.Gamepad(port);

		this.leftStick = new Joystick(this.hid::getLeftX, this.hid::getLeftY).invertY();
		this.rightStick = new Joystick(this.hid::getRightX, this.hid::getRightY).invertY();

		this.leftTrigger = new Axis(this.hid::getLeftTriggerAxis);
		this.rightTrigger = new Axis(this.hid::getRightTriggerAxis);

		this.leftRumble = new RumbleSystem(name + "/Rumble/Left") {
			@Override
			public void setRumble(double rumble) {
				hid.setRumble(RumbleType.LEFT_RUMBLE, rumble);
			}
		};
		this.rightRumble = new RumbleSystem(name + "/Rumble/Right") {
			@Override
			public void setRumble(double rumble) {
				hid.setRumble(RumbleType.RIGHT_RUMBLE, rumble);
			}
		};
	}

	public Trigger a()                {return new Trigger(this.hid.southFace(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger b()                {return new Trigger(this.hid.eastFace(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger x()                {return new Trigger(this.hid.westFace(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger y()                {return new Trigger(this.hid.northFace(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger leftBumper()       {return new Trigger(this.hid.leftBumper(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger rightBumper()      {return new Trigger(this.hid.rightBumper(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger start()            {return new Trigger(this.hid.start(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger back()             {return new Trigger(this.hid.back(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger leftStickButton()  {return new Trigger(this.hid.leftStick(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger rightStickButton() {return new Trigger(this.hid.rightStick(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger povCenter()        {return new Trigger(this.hid.povCenter(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger povUp()            {return new Trigger(this.hid.povUp(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger povUpRight()       {return new Trigger(this.hid.povUpRight(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger povRight()         {return new Trigger(this.hid.povRight(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger povDownRight()     {return new Trigger(this.hid.povDownRight(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger povDown()          {return new Trigger(this.hid.povDown(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger povDownLeft()      {return new Trigger(this.hid.povDownLeft(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger povLeft()          {return new Trigger(this.hid.povLeft(Scheduler.getDefault().getDefaultEventLoop()));}
	public Trigger povUpLeft()        {return new Trigger(this.hid.povUpLeft(Scheduler.getDefault().getDefaultEventLoop()));}

	public boolean isConnected() {
		return this.hid.isConnected();
	}

	public static abstract class RumbleSystem extends Mechanism {
		public RumbleSystem(String name) {
			super(name);
		}

		public abstract void setRumble(double rumble);
	}
}
