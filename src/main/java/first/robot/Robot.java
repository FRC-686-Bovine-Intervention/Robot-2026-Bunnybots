// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.littletonrobotics.junction.LogFileUtil;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGReader;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;
import org.wpilib.command3.Scheduler;
import org.wpilib.net.WebServer;
import org.wpilib.system.Filesystem;

import com.ctre.phoenix6.SignalLogger;
import com.revrobotics.util.StatusLogger;


public class Robot extends LoggedRobot {
private final RobotContainer robotContainer;

public Robot() {
	StatusLogger.disableAutoLogging();
	SignalLogger.enableAutoLogging(false);

	System.out.println("[Init Robot] Recording AdvantageKit Metadata");
	Logger.recordMetadata("Robot", RobotType.getRobot().name());
	Logger.recordMetadata("Mode", RobotType.getMode().name());
		Logger.recordMetadata("ProjectName", BuildConstants.MAVEN_NAME);
		Logger.recordMetadata("BuildDate", BuildConstants.BUILD_DATE);
		Logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);
		Logger.recordMetadata("GitDate", BuildConstants.GIT_DATE);
		Logger.recordMetadata("GitBranch", BuildConstants.GIT_BRANCH);
		Logger.recordMetadata("GitDirty",
			switch (BuildConstants.DIRTY) {
				case 0 -> "All changes committed";
				case 1 -> "Uncomitted changes";
				default -> "Unknown";
			}
		);

	// Set up data receivers & replay source
		System.out.println("[Init Robot] Configuring AdvantageKit for " + RobotType.getMode().name() + " " + RobotType.getRobot().name());
		switch (RobotType.getMode()) {
			// Running on a real robot, log to a USB stick
			case REAL:
				Logger.addDataReceiver(new WPILOGWriter("/media/sda1/"));
				Logger.addDataReceiver(new NT4Publisher());
			break;

			// Running a physics simulator, log to local folder
			case SIM:
				Logger.addDataReceiver(new WPILOGWriter("logs/sim"));
				Logger.addDataReceiver(new NT4Publisher());
			break;

			// Replaying a log, set up replay source
			case REPLAY:
				setUseTiming(false); // Run as fast as possible
				String logPath = LogFileUtil.findReplayLog();
				Logger.setReplaySource(new WPILOGReader(logPath));
				Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
			break;
		}

		System.out.println("[Init Robot] Starting AdvantageKit");
		Logger.start();

	/*--------
	MISSING COMMAND LOGGER GOES HERE
	NEED TO SOLVE COMMAND LOGGING
	--------*/

	System.out.println("[Init Robot] Instantiating RobotContainer");
		this.robotContainer = new RobotContainer();
	System.out.println("[Init Robot] Starting Elastic Layout Webserver");
		WebServer.start(5800, Filesystem.getDeployDirectory().getPath() + "/elastic");

	//MORE TO COME
}

@Override
public void robotPeriodic() {
	Scheduler.getDefault().run();
}

@Override
public void disabledInit() {}

@Override
public void disabledPeriodic() {}

@Override
public void autonomousInit() {
	this.robotContainer.autoManager.endAuto();
		this.robotContainer.autoManager.startAuto();
}

@Override
public void autonomousPeriodic() {}

@Override
public void teleopInit() {
		this.robotContainer.autoManager.endAuto();
}

@Override
public void teleopPeriodic() {}

@Override
public void utilityInit() {
	Scheduler.getDefault().cancelAll();
}

@Override
public void utilityPeriodic() {}
}
