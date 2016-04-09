import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.SystemPropertyUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;


/**
 * Created by nicolasmachado on 4/6/16.
 */
public class DSTCPStatusHandler extends SimpleChannelInboundHandler<ByteBuf>
{
	private static Timer timer = new Timer();
	private byte getControlByte(TeamState state)
	{
		byte b = 0;
		if(state.isAuto)
		{
			b |=2;
		}
		else
		{
			b &= ~2;
		}
		if(state.enabled)
		{
			b |= 4;
		}
		else
		{
			b &= ~4;
		}
		if(state.eStopped)
		{
			b |= 128;
		}
		else
		{
			b &= ~128;
		}
		return b;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf bb) throws Exception
	{
		if(bb.readableBytes() > 0)
		{
			byte[] teamAddr = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getAddress();
			System.out.printf("Connected to %s!\n", ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());
			byte[] packet = new byte[bb.writerIndex()];
			bb.getBytes(0, packet);
			System.out.printf("Packet Contents are: %s\n", Arrays.toString(packet));
			final TeamState state = TeamState.stateForTeamNumber(teamAddr[1] * 10 + teamAddr[2]);
			while(bb.readableBytes() >= 3)
			{
				int num2 = bb.readUnsignedShort();
				System.out.printf("num2 is: %d\n", num2);
				if(bb.capacity() < (long) (num2 - 1))
				{
					break;
				}
				byte tagType = bb.readByte();
				System.out.printf("TagType is %d!\n", tagType);
				if(num2 - 1 > 0)
				{
					switch(tagType)
					{
						case 0:
							state.WPIlibVersion = bb.toString(0, num2 - 1, Charset.forName("US-ASCII"));
							bb.readerIndex(bb.readerIndex() + num2 - 1);
							System.out.printf("WPILIB Version: %s\n", state.WPIlibVersion);
							state.WPIlibVersionGood = state.WPIlibVersion.equals("2016 Java Release 5") || state.WPIlibVersion.equals("2016 C++ Release 5");
							break;
						case 1:
							state.RIOVersion = bb.toString(0, num2 - 1, Charset.forName("US-ASCII"));
							bb.readerIndex(bb.readerIndex() + num2 - 1);
							System.out.printf("RIO Version: %s\n", state.RIOVersion);
							state.RIOVersionGood = state.RIOVersion.equals("FRC_2016_v19");
							break;
						case 2:
							state.DSVersion = bb.toString(0, num2 - 1, Charset.forName("US-ASCII"));
							System.out.printf("DS Version: %s\n", state.DSVersion);
							bb.readerIndex(bb.readerIndex() + num2 - 1);
							state.DSVersionGood = true;
							break;
						case 3:
							state.PDPVersion = bb.toString(0, num2 - 1, Charset.forName("US-ASCII"));
							System.out.printf("PDP Version: %s\n", state.PDPVersion);
							bb.readerIndex(bb.readerIndex() + num2 - 1);
							break;
						case 4:
							state.PCMVersion = bb.toString(0, num2 - 1, Charset.forName("US-ASCII"));
							System.out.printf("PCM Version: %s\n", state.PCMVersion);
							bb.readerIndex(bb.readerIndex() + num2 - 1);
							break;
						case 5:
							state.CANJagVersion = bb.toString(0, num2 - 1, Charset.forName("US-ASCII"));
							System.out.printf("CAN Jaguar Version: %s\n", state.CANJagVersion);
							bb.readerIndex(bb.readerIndex() + num2 - 1);
							break;
						case 6:
							state.CANTalonVersion = bb.toString(0, num2 - 1, Charset.forName("US-ASCII"));
							System.out.printf("CAN Talon Version: %s\n", state.CANTalonVersion);
							bb.readerIndex(bb.readerIndex() + num2 - 1);
							break;
						case 24:
							int teamNumber = bb.readShort();
							System.out.printf("Team Number is %d!\n", teamNumber);
							break;
						default:
							bb.readBytes(num2 - 1);
							break;
					}
				}
			}
			if(!state.taskRunning)
			{
				timer.schedule(new TimerTask()
				{
					int i = 0;
					@Override
					public void run()
					{
						try
						{
							if(i<10)
							i++;
							else
							{
								i = 0;
								state.enabled = !state.enabled;
							}
							ByteBuf response = ByteBufAllocator.DEFAULT.buffer(22);
							response.writeShort(state.packetIndex++);
							response.writeByte(state.commVersion);
							response.writeByte(getControlByte(state));
							response.writeByte(0);
							response.writeByte(3);
							response.writeByte(2);
							response.writeShort(20);
							response.writeByte(1);
							Calendar now = Calendar.getInstance();
							response.writeInt(now.get(Calendar.MILLISECOND) * 1000);
							response.writeByte(now.get(Calendar.SECOND));
							response.writeByte(now.get(Calendar.MINUTE));
							response.writeByte(now.get(Calendar.HOUR));
							response.writeByte(now.get(Calendar.DAY_OF_MONTH));
							response.writeByte(now.get(Calendar.MONTH));
							response.writeByte(now.get(Calendar.YEAR) - 1900);
							response.writeShort(0);
							DatagramSocket sock = new DatagramSocket();
							sock.connect((((InetSocketAddress) ctx.channel().remoteAddress()).getAddress()), 1120);
							byte[] temp = new byte[22];
							response.getBytes(0, temp);
							sock.send(new DatagramPacket(temp, 22, (((InetSocketAddress) ctx.channel().remoteAddress()).getAddress()), 1120));
							sock.close();
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}, 0, 100);
				state.taskRunning = true;
			}
		}
	}
}


