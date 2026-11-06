package frc.robot.subsystems.drive.modules;

import static edu.wpi.first.units.Units.Seconds;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.CoastOut;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.StaticBrake;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.signals.SensorDirectionValue;

import edu.wpi.first.math.util.Units;
import frc.robot.constants.RobotConstants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.DriveConstants;
import frc.robot.subsystems.drive.DriveConstants.ModuleConstants;
import frc.robot.subsystems.drive.odometry.OdometryThread;
import frc.robot.subsystems.drive.odometry.OdometryThread.DoubleBuffer;
import frc.util.FFGains;
import frc.util.PIDGains;
import frc.util.loggerUtil.inputs.LoggedEncodedMotor.EncodedMotorStatusSignalCache;
import frc.util.loggerUtil.inputs.LoggedEncoder.EncoderStatusSignalCache;

public class ModuleIOKraken implements ModuleIO {
    protected final TalonFX driveMotor;
	protected final TalonFX azimuthMotor;
	protected final CANcoder azimuthEncoder;

	private final TalonFXConfiguration driveConfig = new TalonFXConfiguration();
	private final TalonFXConfiguration azimuthConfig = new TalonFXConfiguration();
	private final CANcoderConfiguration encoderConfig = new CANcoderConfiguration();

	 private final BaseStatusSignal[] refreshSignals;
    private final BaseStatusSignal[] driveMotorConnectedSignals;
	private final BaseStatusSignal[] azimuthMotorConnectedSignals;
    private final BaseStatusSignal[] azimuthEncoderConnectedSignals;

    private final EncodedMotorStatusSignalCache driveMotorStatusSignalCache;
    private final EncodedMotorStatusSignalCache azimuthMotorStatusSignalCache;
    private final EncoderStatusSignalCache azimuthEncoderStatusSignalCache;

	private final DoubleBuffer drivePositionBuffer;
	private final DoubleBuffer azimuthPositionBuffer;

	private final VoltageOut driveVolts = new VoltageOut(0);
    private final VelocityVoltage driveVelocity = new VelocityVoltage(0);
    private final NeutralOut driveNeutralOutRequest = new NeutralOut();
    private final CoastOut driveCoastOutRequest = new CoastOut();
    private final StaticBrake driveStaticBrakeRequest = new StaticBrake();

    private final VoltageOut azimuthVolts = new VoltageOut(0);
    private final MotionMagicVoltage azimuthVelocity = new MotionMagicVoltage(0);
    private final NeutralOut azimuthNeutralOutRequest = new NeutralOut();
    private final CoastOut azimuthCoastOutRequest = new CoastOut();
    private final StaticBrake azimuthStaticBrakeRequest = new StaticBrake();

	public ModuleIOKraken(ModuleConstants config) {
		this.driveMotor = config.driveMotorID.talonFX();
		this.azimuthMotor = config.azimuthMotorID.talonFX();
		this.azimuthEncoder = config.azimuthEncoderID.get().cancoder();

		this.driveConfig.MotorOutput
			.withInverted(config.driveInverted)
			.withNeutralMode(NeutralModeValue.Brake)
		;

		this.driveConfig.ClosedLoopRamps
			.withVoltageClosedLoopRampPeriod(Seconds.of(0.075))
		;

		this.driveConfig.CurrentLimits
			.withSupplyCurrentLimitEnable(true)
		;

		this.azimuthConfig.MotorOutput
			.withInverted(InvertedValue.Clockwise_Positive)
			.withNeutralMode(NeutralModeValue.Brake)
		;

		this.azimuthConfig.CurrentLimits
			.withSupplyCurrentLimitEnable(true)
		;

		this.azimuthConfig.Feedback
			.withRemoteCANcoder(this.azimuthEncoder)
			.withRotorToSensorRatio(DriveConstants.azimuthMotorToCarriageRatio.then(DriveConstants.azimuthEncoderToCarriageRatio.inverse()).reductionUnsigned())
			.withSensorToMechanismRatio(DriveConstants.azimuthEncoderToCarriageRatio.reductionUnsigned())
		;

		this.azimuthEncoder.getConfigurator().refresh(this.encoderConfig.MagnetSensor);

		this.encoderConfig.MagnetSensor
			.withSensorDirection(SensorDirectionValue.CounterClockwise_Positive)
			.withMagnetOffset(config.encoderZeroOffset)
		;

		this.driveMotorStatusSignalCache = EncodedMotorStatusSignalCache.from(this.driveMotor);
        this.azimuthMotorStatusSignalCache = EncodedMotorStatusSignalCache.from(this.azimuthMotor);
        this.azimuthEncoderStatusSignalCache = EncoderStatusSignalCache.from(this.azimuthEncoder);
		this.refreshSignals = new BaseStatusSignal[] {
			this.driveMotorStatusSignalCache.encoder().position(),
			this.driveMotorStatusSignalCache.encoder().velocity(),
			this.driveMotorStatusSignalCache.motor().appliedVoltage(),
			this.driveMotorStatusSignalCache.motor().statorCurrent(),
			this.driveMotorStatusSignalCache.motor().supplyCurrent(),
			this.driveMotorStatusSignalCache.motor().torqueCurrent(),
			this.driveMotorStatusSignalCache.motor().deviceTemperature(),
		};
		this.driveMotorConnectedSignals = new BaseStatusSignal[] {
			this.driveMotorStatusSignalCache.encoder().position(),
			this.driveMotorStatusSignalCache.encoder().velocity(),
			this.driveMotorStatusSignalCache.motor().appliedVoltage(),
			this.driveMotorStatusSignalCache.motor().statorCurrent(),
			this.driveMotorStatusSignalCache.motor().supplyCurrent(),
			this.driveMotorStatusSignalCache.motor().torqueCurrent(),
			this.driveMotorStatusSignalCache.motor().deviceTemperature(),
		};
		this.azimuthMotorConnectedSignals = new BaseStatusSignal[] {
			this.azimuthMotorStatusSignalCache.encoder().position(),
			this.azimuthMotorStatusSignalCache.encoder().velocity(),
			this.azimuthMotorStatusSignalCache.motor().appliedVoltage(),
			this.azimuthMotorStatusSignalCache.motor().statorCurrent(),
			this.azimuthMotorStatusSignalCache.motor().supplyCurrent(),
			this.azimuthMotorStatusSignalCache.motor().torqueCurrent(),
			this.azimuthMotorStatusSignalCache.motor().deviceTemperature(),
		};
        this.azimuthEncoderConnectedSignals = new BaseStatusSignal[] {
            this.azimuthEncoderStatusSignalCache.position(),
            this.azimuthEncoderStatusSignalCache.velocity(),
        };

        BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.rioUpdateFrequency, this.driveMotorStatusSignalCache.encoder().getStatusSignals());
        BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.rioUpdateFrequency.div(2), this.driveMotorStatusSignalCache.motor().getStatusSignals());
        BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.rioUpdateFrequency, this.azimuthMotorStatusSignalCache.encoder().getStatusSignals());
        BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.rioUpdateFrequency.div(2), this.azimuthMotorStatusSignalCache.motor().getStatusSignals());
        BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.rioUpdateFrequency, this.azimuthEncoderStatusSignalCache.getStatusSignals());

        BaseStatusSignal.setUpdateFrequencyForAll(DriveConstants.odometryLoopFrequency, this.driveMotorStatusSignalCache.encoder().position());
        BaseStatusSignal.setUpdateFrequencyForAll(DriveConstants.odometryLoopFrequency, this.azimuthEncoderStatusSignalCache.position());
        this.driveMotor.optimizeBusUtilization();

        this.drivePositionBuffer = OdometryThread.getInstance().registerPhoenixDoubleSignal(this.driveMotorStatusSignalCache.encoder().position(), Units::rotationsToRadians);
        this.azimuthPositionBuffer = OdometryThread.getInstance().registerPhoenixDoubleSignal(this.azimuthEncoderStatusSignalCache.position(), Units::rotationsToRadians);
	}


	@Override
	public void configFF(FFGains ffGains) {
		ffGains.update(this.azimuthConfig.Slot0);
	}

	@Override
	public void configAzimuthPID(PIDGains pidGains) {
		pidGains.update(this.azimuthConfig.Slot0);
	}

	@Override
	public void configSendAzimuth() {
		this.azimuthMotor.getConfigurator().apply(this.azimuthConfig);
		this.azimuthEncoder.getConfigurator().apply(this.encoderConfig);
	}
}
