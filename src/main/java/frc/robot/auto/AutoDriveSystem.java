package frc.robot.auto;
//hi jack - love hunter 
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.controller.HolonomicDriveController;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.controller.ProfiledPIDController;
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveMotorVoltages;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveWheelSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.util.Units;
import frc.robot.Variables.Auto;

public class AutoDriveSystem {

    // Talon Initialization
    WPI_TalonFX frontLeft = new WPI_TalonFX(Auto.frontLeft);
    WPI_TalonFX frontRight = new WPI_TalonFX(Auto.frontRight);
    WPI_TalonFX backLeft = new WPI_TalonFX(Auto.backLeft);
    WPI_TalonFX backRight = new WPI_TalonFX(Auto.backRight);

    // Gyro Initialization here!
    ADXRS450_Gyro m_Gyro = new ADXRS450_Gyro();

    // Mecanum Kinematics Setup - WHEEL POSITION RELATIVE TO CENTER OF ROBOT IN METERS NEED TO CONVERT TO METERS
    Translation2d m_frontLeftLocation = new Translation2d(Units.feetToMeters(Auto.kFrontLeft_x), Units.feetToMeters(Auto.kFrontLeft_y));
    Translation2d m_frontRightLocation = new Translation2d(Units.feetToMeters(Auto.kFrontRight_x), Units.feetToMeters(Auto.kFrontRight_y));
    Translation2d m_backLeftLocation = new Translation2d(Units.feetToMeters(Auto.kBackLeft_x), Units.feetToMeters(Auto.kBackLeft_y));
    Translation2d m_backRightLocation = new Translation2d(Units.feetToMeters(Auto.kBackRight_x), Units.feetToMeters(Auto.kBackRight_y));

    // Creating my kinematics object using the wheel locations.
    MecanumDriveKinematics kinematics = new MecanumDriveKinematics(m_frontLeftLocation, m_frontRightLocation, m_backLeftLocation, m_backRightLocation);

    // Odometry Object -- Initialized after resetting encoders
    MecanumDriveOdometry odometry;

    // Simple Feed Forward gotten from CHARACTERIZATION | kS | kV | kA |
    SimpleMotorFeedforward feedforward = new SimpleMotorFeedforward(Auto.kS, Auto.kV, Auto.kA);

    // Adjust based on how well the robot tracks the trajectory. NOT TUNED
    PIDController xController = new PIDController(1, 0, 0);
    PIDController yController = new PIDController(1.2, 0, 0);
    ProfiledPIDController thetaController = new ProfiledPIDController(1.1, 0, 0,  new TrapezoidProfile.Constraints(Math.PI, Math.PI));

    // PID for each wheel CHARACTERIZATION GETS YOU PRETTY CLOSE BUT MAKE SURE TO TUNE.. i think
    PIDController frontLeftPID = new PIDController(Auto.fl_kP, 0, 0);
    PIDController frontRightPID = new PIDController(Auto.fr_kP, 0, 0);
    PIDController backLeftPID = new PIDController(Auto.bl_kP, 0, 0);
    PIDController backRightPID = new PIDController(Auto.br_kP, 0, 0);

    // Robot pose object
    Pose2d pose = new Pose2d();

    // Holonomic Drive Controller
    public HolonomicDriveController HDC = new HolonomicDriveController(xController, yController, thetaController);

    /**
     * Gets a Rotation2d object from the gyro object
     * @return Rotation2d of current gyro heading
     */
    public Rotation2d getHeading() {
        return Rotation2d.fromDegrees(getGyroHeading()); 
    }

    /**
     * Gets gyro heading from -180 to 180. Normalized for wpilib
     * @return Gyro Heading [-180, 180]
     */
    public double getGyroHeading(){
        return Math.IEEEremainder(m_Gyro.getAngle(), 360) * -1;
    }

    /**
     * Gets current wheel speeds
     * @return MecanumDriveWheelSpeeeds with all 4 velocities in m/s
     */
    public MecanumDriveWheelSpeeds getSpeeds() { 
        return new MecanumDriveWheelSpeeds(
            ((frontLeft.getSelectedSensorVelocity() * 10 * Units.inchesToMeters(Auto.kWheelRadiusInches) * Math.PI * 2) / (Auto.kEncoderTicksPerRev * Auto.kGearRatio)),
            ((frontRight.getSelectedSensorVelocity() * 10 * Units.inchesToMeters(Auto.kWheelRadiusInches) * Math.PI * 2) / (Auto.kEncoderTicksPerRev * Auto.kGearRatio)),
            ((backLeft.getSelectedSensorVelocity() * 10 * Units.inchesToMeters(Auto.kWheelRadiusInches) * Math.PI * 2) / (Auto.kEncoderTicksPerRev * Auto.kGearRatio)),
            ((backRight.getSelectedSensorVelocity() * 10 * Units.inchesToMeters(Auto.kWheelRadiusInches) * Math.PI * 2) / (Auto.kEncoderTicksPerRev * Auto.kGearRatio))
        );
    }

    /**
     * Gets the robots current position relative to Autonomous starting spot
     * @return Pose2d of current robot position
     */
    public Pose2d getPose() {
        return pose;
    }

    public SimpleMotorFeedforward getFeedforward() {
        return feedforward;
    }

    public MecanumDriveKinematics getKinematics() {
        return kinematics;
    }

    public PIDController getXController(){
        return xController;
    }

    public PIDController getYController(){
        return yController;
    }

    public ProfiledPIDController getThetaController(){
        return thetaController;
    }

    public PIDController getFrontLeftPIDController() {
        return frontLeftPID;
    }

    public PIDController getFrontRightPIDController() {
        return frontRightPID;
    }

    public PIDController getBackLeftPIDController() {
        return backLeftPID;
    }

    public PIDController getBackRightPidController() {
        return backRightPID;
    }

    /**
     * Sets the voltages for each wheel of a Mecanum Drive
     * @param mdmv - MecanumDriveMotoVoltages object with each wheels voltages to set
     */
    public void setOutputVolts(MecanumDriveMotorVoltages mdmv) {
        frontLeft.setVoltage(mdmv.frontLeftVoltage);
        frontRight.setVoltage(mdmv.frontRightVoltage);
        backLeft.setVoltage(mdmv.rearLeftVoltage);
        backRight.setVoltage(mdmv.rearRightVoltage);
    }

    /**
     * Used to set velocity. Uses talons onboard pid.
     * @param wheelSpeeds
     */
    public void setOutputVelocity(MecanumDriveWheelSpeeds wheelSpeeds) {
        double frontLeftFFVolts = feedforward.calculate(wheelSpeeds.frontLeftMetersPerSecond);
        double frontLeftFF = frontLeftFFVolts / 12d; // Normalize to -1..1 as TalonSRX takes motor percent for arb feedforward, and they also do automatic voltage compensation
        double frontLeftRotationsPerSecond = (wheelSpeeds.frontLeftMetersPerSecond) / (2 * Units.inchesToMeters(Auto.kWheelRadiusInches) * Math.PI );
        double frontLeftTicksPerDs = (frontLeftRotationsPerSecond * Auto.kEncoderTicksPerRev * Auto.kGearRatio) / 10;
        frontLeft.set(TalonFXControlMode.Velocity, frontLeftTicksPerDs, DemandType.ArbitraryFeedForward, frontLeftFF);

        double backLeftFFVolts = feedforward.calculate(wheelSpeeds.rearLeftMetersPerSecond);
        double backLeftFF = backLeftFFVolts / 12d; // Normalize to -1..1 as TalonSRX takes motor percent for arb feedforward, and they also do automatic voltage compensation
        double backLeftRotationsPerSecond = (wheelSpeeds.rearLeftMetersPerSecond) / (2 * Units.inchesToMeters(Auto.kWheelRadiusInches) * Math.PI * Auto.kGearRatio);
        double backLeftTicksPerDs = (backLeftRotationsPerSecond * Auto.kEncoderTicksPerRev * Auto.kGearRatio) / 10; // THIS WHEEL HAS 1440 ENCODER TICKS PER REVOLUTION
        backLeft.set(TalonFXControlMode.Velocity, backLeftTicksPerDs, DemandType.ArbitraryFeedForward, backLeftFF);

        double frontRightFFVolts = feedforward.calculate(wheelSpeeds.frontRightMetersPerSecond);
        double frontRightFF = frontRightFFVolts / 12d;
        double frontRightRotationsPerSecond = (wheelSpeeds.frontRightMetersPerSecond) / (2 * Units.inchesToMeters(Auto.kWheelRadiusInches) * Math.PI * Auto.kGearRatio);
        double frontRightTicksPerDs = (frontRightRotationsPerSecond * Auto.kEncoderTicksPerRev * Auto.kGearRatio) / 10;
        frontLeft.set(TalonFXControlMode.Velocity, frontRightTicksPerDs, DemandType.ArbitraryFeedForward, frontRightFF);


        double backRightFFVolts = feedforward.calculate(wheelSpeeds.rearRightMetersPerSecond);
        double backRightFF = backRightFFVolts / 12d; // Normalize to -1..1 as TalonSRX takes motor percent for arb feedforward, and they also do automatic voltage compensation
        double backRightRotationsPerSecond = (wheelSpeeds.rearRightMetersPerSecond) / (2 * Units.inchesToMeters(Auto.kWheelRadiusInches) * Math.PI * Auto.kGearRatio);
        double backRightTicksPerDs = (backRightRotationsPerSecond * Auto.kEncoderTicksPerRev * Auto.kGearRatio) / 10;
        frontLeft.set(TalonFXControlMode.Velocity, backRightTicksPerDs, DemandType.ArbitraryFeedForward, backRightFF);
      }

    /**
     * Resets the encoders, odometry and gyro. Calling this sets the robots current position on the field to the new origin.
     */
    public void reset() {
        // Reset all sensors. 
        m_Gyro.reset();
        resetEncoders();
        odometry.resetPosition(new Pose2d(), getHeading());
    }

    /**
     * Updates the robot pose. Required to get called periodically.
     */
    public void periodic() {
        pose = odometry.update(getHeading(), getSpeeds());
    }

    public void putEncoder(){
        SmartDashboard.putNumber("Front Left Encoder", frontLeft.getSelectedSensorPosition());
        SmartDashboard.putNumber("Back Left Encoder", backLeft.getSelectedSensorPosition());
        SmartDashboard.putNumber("Front Right Encoder", frontRight.getSelectedSensorPosition());
        SmartDashboard.putNumber("Back Right Encoder", backRight.getSelectedSensorPosition());
    }

    public void resetEncoders(){
        frontLeft.setSelectedSensorPosition(0);
        backLeft.setSelectedSensorPosition(0);
        frontRight.setSelectedSensorPosition(0);
        backRight.setSelectedSensorPosition(0);
    }

    public void initializeOdometry(){
        odometry = new MecanumDriveOdometry(kinematics, getHeading());
    }

    public void setupMotorConfigs(){
        m_Gyro.calibrate();
        frontLeft.configFactoryDefault();
        // frontLeft.configPeakOutputForward(1);
        // frontLeft.configPeakOutputReverse(-1);
        frontLeft.setNeutralMode(NeutralMode.Brake);
        frontLeft.setSensorPhase(true);
        frontLeft.config_kP(0, Auto.fl_kP);
        // frontLeft.config_kI(0, Auto.fl_kI);
        // frontLeft.config_kD(0, Auto.fl_kD);

        frontRight.configFactoryDefault();
        // frontRight.configPeakOutputForward(1);
        // frontRight.configPeakOutputReverse(-1);
        frontRight.setNeutralMode(NeutralMode.Brake);
        frontRight.setSensorPhase(true);
        frontRight.setInverted(true);
        frontRight.config_kP(0, Auto.fr_kP);
        // frontRight.config_kI(0, Auto.fr_kI);
        // frontRight.config_kD(0, Auto.fr_kD);
        
        backLeft.configFactoryDefault();
        // backLeft.configPeakOutputForward(1);
        // backLeft.configPeakOutputReverse(-1)
        backLeft.setNeutralMode(NeutralMode.Brake);
        backLeft.setSensorPhase(true);
        backLeft.config_kP(0, Auto.bl_kP);
        // backLeft.config_kI(0, Auto.bl_kI);
        // backLeft.config_kD(0, Auto.bl_kD);

        backRight.configFactoryDefault();
        // backRight.configPeakOutputForward(1);
        // backRight.configPeakOutputReverse(-1);
        backRight.setNeutralMode(NeutralMode.Brake);
        backRight.setSensorPhase(true);
        backRight.setInverted(true);
        backRight.config_kP(0, Auto.br_kP);
        // backRight.config_kI(0, Auto.br_kI);
        // backRight.config_kD(0, Auto.br_kD);

    }

    public void putGyro(){
        SmartDashboard.putNumber("Gyro Angle", m_Gyro.getAngle());
    }

    public void putWheelVelocities(){
        SmartDashboard.putNumber("FL Actual:", getSpeeds().frontLeftMetersPerSecond);
        SmartDashboard.putNumber("BL Actual:", getSpeeds().rearLeftMetersPerSecond);
        SmartDashboard.putNumber("FR Actual:", getSpeeds().frontRightMetersPerSecond);
        SmartDashboard.putNumber("BR Actual:", getSpeeds().rearRightMetersPerSecond);
    }
}