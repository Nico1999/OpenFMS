import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by nicolasmachado on 4/7/16.
 */
public class DSUDPStatusHandler extends SimpleChannelInboundHandler<DatagramPacket>
{

	public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
	{
		ByteBuf bb = packet.content();
			int packetIndex = bb.readUnsignedShort();
			System.out.printf("PacketIndex is %d\n",packetIndex);
			byte commVersion = bb.readByte();
			System.out.printf("CommVersion is %d\n",commVersion);
			byte statusMask = bb.readByte();
			boolean isAuto = (statusMask & 2) == 2;
			boolean isEnabled = (statusMask & 4) == 4;
			boolean rioPing = (statusMask & 8) == 8;
			boolean radioPing = (statusMask & 16) == 16;
			boolean commsActive = (statusMask & 32) == 32;
			boolean estopped = (statusMask & 64) == 64;
			int teamNumber = bb.readUnsignedShort();
			int value = bb.readUnsignedShort();
			double robotBattery = value/256.0;
			System.out.printf("Robot Battery Voltage is %f\n",robotBattery);
			TeamState currentTeam = TeamState.stateForTeamNumber(teamNumber);
			System.out.printf("Team Number is %d\n",teamNumber);
			currentTeam.packetIndex = packetIndex;
			currentTeam.commVersion = commVersion;
			currentTeam.isAuto = isAuto;
			System.out.printf("Auto is %b\n",isAuto);
			currentTeam.enabled = isEnabled;
			System.out.printf("Enabled is %b\n",isEnabled);
			currentTeam.RIOPing = rioPing;
			System.out.printf("RIOPing is %b\n",rioPing);
			currentTeam.radioPing = radioPing;
			System.out.printf("RadioPing is %b\n",radioPing);
			currentTeam.commsActive = commsActive;
			System.out.printf("CommsActive is %b\n",commsActive);
			currentTeam.eStopped = estopped;
			System.out.printf("eStopped is %b\n",estopped);
			currentTeam.robotBattery = robotBattery;
			if(bb.writerIndex() > 22)
			{
				int num = 0;
				boolean flag = true;
				while (flag)
				{
					int num3 = bb.readByte();
					if (bb.getByte(num+1) >=0 && bb.getByte(num+1) <=4)
					{
						if (num3 > 1)
						{
							switch(bb.readByte())
							{
								case 0:
									currentTeam.fieldRadioSignalStrength = bb.readByte();
									currentTeam.fieldRadioBandwidthUtilization = bb.readUnsignedShort();
									break;
								case 1:
									currentTeam.lostPackets = bb.readUnsignedShort();
									currentTeam.sentPackets = bb.readUnsignedShort();
									currentTeam.averageTripTime = bb.readByte();
									break;
								case 2:
									currentTeam.laptopBatteryPercent = bb.readByte();
									currentTeam.laptopCPUPercent = bb.readByte();
									break;
								case 3:
									currentTeam.robotRadioSignalStrength = bb.readByte();
									currentTeam.fieldRadioBandwidthUtilization =  bb.readUnsignedShort();
									break;
								case 4:
									break;
								default:
									break;
							}
						}
					}
					else
					{
						break;
					}
					num = (num + num3) + 1;
					if (num >= bb.capacity())
					{
						flag = false;
					}
				}
			}
		}
	}
}
