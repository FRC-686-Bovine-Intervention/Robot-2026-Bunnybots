package first.robot.mechanisms.drive.modules;

import static org.wpilib.units.Units.Amps;
import static org.wpilib.units.Units.Seconds;

import java.nio.DoubleBuffer;
import java.util.Optional;

import org.wpilib.units.Units;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.CoastOut;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.StaticBrake;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.swerve.SwerveDrivetrain.OdometryThread;
import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.SparkLowLevel.ControlType;

import first.robot.Robot;
import first.robot.constants.RobotConstants;
import first.robot.mechanisms.drive.DriveConstants;
import first.util.PIDGains;
import first.util.loggerUtil.inputs.LoggedEncodedMotor.EncodedMotorStatusSignalCache;
import first.util.loggerUtil.inputs.LoggedEncoder.EncoderStatusSignalCache;

public class ModuleIOTalonFX implements ModuleIO {
    protected final TalonFX driveMotor;
    protected final TalonFX azimuthMotor;
    protected final CANcoder azimuthEncoder;
    
    private final TalonFXConfiguration driveConfig = new TalonFXConfiguration();
    private final TalonFXConfiguration azimuthConfig = new TalonFXConfiguration();

    private final BaseStatusSignal[] refreshSignals;
    private final BaseStatusSignal[] driveMotorConnectedSignals;
    private final EncodedMotorStatusSignalCache driveMotorStatusSignalCache;
    private final BaseStatusSignal[] azimuthMotorConnectedSignals;
    private final EncodedMotorStatusSignalCache azimuthMotorStatusSignalCache;
    private final BaseStatusSignal[] azimuthEncoderConnectedSignals;
    private final EncoderStatusSignalCache azimuthEncoderStatusSignalCache;

    private final DoubleBuffer drivePositionBuffer;
    private final DoubleBuffer azimuthPositionBuffer;

    private final VoltageOut driveVolts = new VoltageOut(0);
    private final VelocityVoltage driveVelocity = new VelocityVoltage(0);
    private final NeutralOut driveNeutralOutRequest = new NeutralOut();
    private final CoastOut driveCoastOutRequest = new CoastOut();
    private final StaticBrake driveStaticBrakeRequest = new StaticBrake();

    private final VoltageOut azimuthVolts = new VoltageOut(0);
    private final VelocityVoltage azimuthVelocity = new VelocityVoltage(0);
    private final NeutralOut azimuthNeutralOutRequest = new NeutralOut();
    private final CoastOut azimuthCoastOutRequest = new CoastOut();
    private final StaticBrake azimuthStaticBrakeRequest = new StaticBrake();

    public ModuleIOTalonFX(ModuleConstants config) {
        this.driveMotor = config.driveMotorID.talonFX();
        this.azimuthMotor = config.azimuthMotorID.talonFX();
        this.azimuthEncoder = config.azimuthEncoderID.canCoder();
        
        this.driveConfig.MotorOutput
            .withInverted(config.driveInverted)
            .withNeutralMode(NeutralModeValue.Brake)
        ;
        this.azimuthConfig.MotorOutput
            .withInverted(config.azimuthInverted)
            .withNeutralMode(NeutralModeValue.Brake)
        ;

        this.driveConfig.CurrentLimits
            .withSupplyCurrentLimitEnable(true)
        ;
        
        this.azimuthConfig.CurrentLimits
            .withSupplyCurrentLimit(config.azimuthCurrentLimit)
            .withSupplyCurrentLowerLimit(config.azimuthCurrentLimit)
            .withSupplyCurrentLowerTime(Seconds.of(0))
            .withSupplyCurrentLimitEnable(true)
            .withStatorCurrentLimit(config.azimuthStatorCurrentLimit)
            .withStatorCurrentLimitEnable(true)
        ;

        this.azimuthConfig.Feedback
            .FeedbackRemoteSensorID(config.azimuthEncoderID.getDeviceID())
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

        BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.systemcoreUpdateFrequency, this.driveMotorStatusSignalCache.encoder().getStatusSignals());
        BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.systemcoreUpdateFrequency.div(2), this.driveMotorStatusSignalCache.motor().getStatusSignals());
        BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.systemcoreUpdateFrequency, this.azimuthMotorStatusSignalCache.encoder().getStatusSignals());
        BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.systemcoreUpdateFrequency.div(2), this.azimuthMotorStatusSignalCache.motor().getStatusSignals());
        BaseStatusSignal.setUpdateFrequencyForAll(RobotConstants.systemcoreUpdateFrequency, this.azimuthEncoderStatusSignalCache.getStatusSignals());

        BaseStatusSignal.setUpdateFrequencyForAll(DriveConstants.odometryLoopFrequency, this.driveMotorStatusSignalCache.encoder().position());
        BaseStatusSignal.setUpdateFrequencyForAll(DriveConstants.odometryLoopFrequency, this.azimuthEncoderStatusSignalCache.position());
        this.driveMotor.optimizeBusUtilization();

        this.drivePositionBuffer = OdometryThread.getInstance().registerPhoenixDoubleSignal(this.driveMotorStatusSignalCache.encoder().position(), Units::rotationsToRadians);
        this.azimuthPositionBuffer = OdometryThread.getInstance().registerGenericDoubleSignal(this.azimuthEncoder::getAbsolutePosition, Units::rotationsToRadians);
    }

    @Override
	public void updateInputs(ModuleIOInputs inputs) {
		BaseStatusSignal.refreshAll(this.refreshSignals);
		inputs.driveMotorConnected = BaseStatusSignal.isAllGood(this.driveMotorConnectedSignals);
		inputs.azimuthMotorConnected = true;
		inputs.azimuthEncoderConnected = true;
		inputs.driveMotor.updateFrom(this.driveMotorStatusSignalCache);
		inputs.azimuthMotor.updateFrom(this.azimuthMotorStatusSignalCache);
		inputs.azimuthEncoder.updateFrom(this.azimuthEncoderStatusSignalCache);

		inputs.odometrySampleCount = this.drivePositionBuffer.getSize();
		System.arraycopy(this.drivePositionBuffer.getInternalBuffer(), 0, inputs.odometryDriveRads, 0, inputs.odometrySampleCount);
		System.arraycopy(this.azimuthPositionBuffer.getInternalBuffer(), 0, inputs.odometryAzimuthRads, 0, inputs.odometrySampleCount);
		this.drivePositionBuffer.clear();
		this.azimuthPositionBuffer.clear();
	}

    @Override
	public void setDriveVolts(double volts) {
		this.driveMotor.setControl(this.driveVolts.withOutput(volts));
	}

	@Override
	public void setDriveVelocityRadPerSec(double velocityRadPerSec, double accelerationRadPerSecSqr, double feedforwardVolts, boolean overrideWithBrakeMode) {
		this.driveMotor.setControl(this.driveVelocity
			.withVelocity(Units.radiansToRotations(velocityRadPerSec))
			.withAcceleration(Units.radiansToRotations(accelerationRadPerSecSqr))
			.withFeedForward(feedforwardVolts)
			.withOverrideBrakeDurNeutral(overrideWithBrakeMode)
		);
	}

	@Override
	public void setAzimuthVolts(double volts) {
		this.azimuthMotor.setVoltage(volts);
	}

	@Override
	public void setAzimuthAngleRads(double angleRads, double feedforwardVolts) {
		this.azimuthClosedLoopController.setSetpoint(
			Units.radiansToRotations(angleRads),
			ControlType.kPosition,
			ClosedLoopSlot.kSlot0,
			feedforwardVolts
		);
	}

	@Override
	public void stopDrive(Optional<NeutralMode> neutralMode) {
		var controlRequest = NeutralMode.selectControlRequest(neutralMode, this.neutralOutRequest, this.coastOutRequest, this.staticBrakeRequest);
		this.driveMotor.setControl(controlRequest);
	}

	@Override
	public void stopAzimuth(Optional<NeutralMode> neutralMode) {
		//TODO Reimplement module turn brake mode
		// turnMotor.setIdleMode(enable ? IdleMode.kBrake : IdleMode.kCoast);
		this.azimuthMotor.stopMotor();
	}

	@Override
	public void configDrivePID(PIDGains pidGains) {
		var config = new Slot0Configs();
		this.driveMotor.getConfigurator().refresh(config);
		pidGains.update(config);
		this.driveMotor.getConfigurator().apply(config);
	}

	@Override
	public void configAzimuthPID(PIDGains pidGains) {
		pidGains.update(this.azimuthConfig.closedLoop);
		this.azimuthMotor.configure(this.azimuthConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
	}

	@Override
	public void configSendDrive() {
		this.driveMotor.getConfigurator().apply(this.driveConfig);
	}

	@Override
	public void configSendAzimuth() {
		this.azimuthMotor.configure(this.azimuthConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
	}
}
