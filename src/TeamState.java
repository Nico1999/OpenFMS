import java.util.HashMap;

/**
 * Created by nicolasmachado on 4/6/16.
 */
public class TeamState
{
	private static HashMap<Integer,TeamState> states = new HashMap<>();
	public TeamState(int teamNumber)
	{
		this.teamNumber = teamNumber;
		states.put(teamNumber,this);
	}

	public static TeamState stateForTeamNumber(int teamNumber)
	{
		if(!states.containsKey(teamNumber))
		{
			TeamState team = new TeamState(teamNumber);
			states.put(teamNumber,team);
			return team;
		}
		return states.get(teamNumber);
	}

	boolean eStopped;
	int teamNumber;
	byte commVersion;
	boolean commsActive;
	boolean radioPing;
	boolean RIOPing;
	boolean enabled;
	boolean isAuto;
	double robotBattery;
	byte laptopBatteryPercent;
	byte laptopCPUPercent;
	int packetIndex;
	String RIOVersion;
	boolean RIOVersionGood;
	String WPIlibVersion;
	boolean WPIlibVersionGood;
	String DSVersion;
	boolean DSVersionGood;
	String PDPVersion;
	String PCMVersion;
	String CANJagVersion;
	String CANTalonVersion;
	byte fieldRadioSignalStrength;
	int fieldRadioBandwidthUtilization;
	int lostPackets;
	int sentPackets;
	int averageTripTime;
	byte robotRadioSignalStrength;
	boolean taskRunning;
}
