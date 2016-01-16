package epfl.lcav.model;

//TODO add java doc
//FIXME investigate if we can use the same rtpManager to create the send stream 
/*
 * 
 * @(#)AVReceive2.java	1.3 01/03/13
 *
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.io.IOException;
import java.net.*;
import java.util.Vector;

import javax.media.*;
import javax.media.rtp.*;
import javax.media.rtp.event.*;
import javax.media.rtp.rtcp.*;
import javax.media.protocol.*;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.control.BufferControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;

import epfl.lcav.attributes.SharedAttributes;
import epfl.lcav.shared.ServerAttributes;
import epfl.lcav.view.DeviceFrame;
import epfl.lcav.view.MainView;

/**
 * AVReceive2 to receive RTP transmission using the new RTP API.
 */
public class AVReceive2 implements ReceiveStreamListener, SessionListener,
 Runnable, DataSinkListener {
	public AVReceive2() {
		System.out.println("creating a an AVReceiver thread instance ");
		AVReceive2.closedwindow=false; 
	}
	
	String sessions[] = null;
	RTPManager mgrs[] = null;
	static Vector mParticipantResourcesManagerVector = null;



	public static volatile boolean closedwindow = false;
	public static volatile  Object closeSync = new Object();

	/*public AVReceive2(String sessions[]) {
		this.sessions = sessions;
		System.err.println("Starting session: " + this.sessions);
	}*/

	@SuppressWarnings("rawtypes")
	protected boolean initialize() {

		try {
			InetAddress ipAddr;
			// Class to encapsulate a pair of Internet address and a pair of
			// ports for use in RTPSM methods.
			SessionAddress localAddr = new SessionAddress();
			SessionAddress destAddr;

			mgrs = new RTPManager[sessions.length];
			if(mgrs!=null) {
				System.out.println("In initialize method mgrs is not null "+ mgrs.length);
			}
			AVReceive2.mParticipantResourcesManagerVector = new Vector(); 
			SessionLabel session;

			// Open the RTP sessions.
			for (int i = 0; i < sessions.length; i++) {
				// Parse the session addresses.
				try {
					session = new SessionLabel(sessions[i]);
				} catch (IllegalArgumentException e) {
					System.err
							.println("Failed to parse the session address given: "
									+ sessions[i]);
					e.printStackTrace();
					return false;
				}

				System.err.println("  - Open RTP session for: addr: "
						+ session.addr + " port: " + session.port + " ttl: "
						+ session.ttl);
				// Adding the formats of interest Linear PCM with different sampling rates
				mgrs[i] = (RTPManager) RTPManager.newInstance();
				mgrs[i].addFormat(new AudioFormat(AudioFormat.LINEAR, 8000, 16,
						1, AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED),
						SharedAttributes.getHashMap().get(8000));
				mgrs[i].addFormat(new AudioFormat(AudioFormat.LINEAR, 11025,
						16, 1, AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED),
						SharedAttributes.getHashMap().get(11025));
				mgrs[i].addFormat(new AudioFormat(AudioFormat.LINEAR, 16000,
						16, 1, AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED),
						SharedAttributes.getHashMap().get(16000));
				mgrs[i].addFormat(new AudioFormat(AudioFormat.LINEAR, 22050,
						16, 1, AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED),
						SharedAttributes.getHashMap().get(22050));
				mgrs[i].addFormat(new AudioFormat(AudioFormat.LINEAR, 44100,
						16, 1, AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED),
						SharedAttributes.getHashMap().get(44100));

	 // Interface that generates the callback for all SessionEvents.
				 // These events are LocalCollisionEvent that pertain to the
				 // local participant and NewParticipantEvent that will inform
				 // the listener of every new/unique participant that joins the
				 // session. For all other state transitions event of the
				 // participant i.e. Active/Inactive/Timeout/ByeEvent etc. see
				 // ReceiveStreamListener

				mgrs[i].addSessionListener(this);
				 // Adds a ReceiveStreamListener. This listener listens to all
				 // the events that notify state transitions for a particular
				 // ReceiveStream.
				mgrs[i].addReceiveStreamListener(this);

				ipAddr = InetAddress.getByName(session.addr);

				if (ipAddr.isMulticastAddress()) {
					// local and remote address pairs are identical:
					localAddr = new SessionAddress(ipAddr, session.port,
							session.ttl);
					destAddr = new SessionAddress(ipAddr, session.port,
							session.ttl);
				} else {
					localAddr = new SessionAddress(InetAddress.getLocalHost(),
							session.port);
					destAddr = new SessionAddress(ipAddr, session.port);
				}

				mgrs[i].initialize(localAddr);

				// You can try out some other buffer size to see
				// if you can get better smoothness.
				BufferControl bc = (BufferControl) mgrs[i]
						.getControl("javax.media.control.BufferControl");
				if (bc != null) {
					System.err.println("Session Manager, original buffer size  :  "+bc.getBufferLength());
					bc.setBufferLength(300);//default value bc.setBufferLength(350);
					System.err.println("Session Manager, actual buffer size  :  "+bc.getBufferLength());
				}
				mgrs[i].addTarget(destAddr);
			}

		} catch (Exception e) {
			System.err.println("Cannot create the RTP Session: "
					+ e.getMessage());
			e.printStackTrace();
			return false;
		}
		if (mgrs!=null) {
			System.out.println("Just before the return true statment in initialize  MGRS is null !!! Why ????????"+ mgrs.length);
		}
		return true;
	}

	

	/**
	 * Clean-up method Terminate all the threads operating for this session and
	 * close all the streams: dataSink stream, sendStream and player stream for
	 * all participants of the session. Finally close session managers.
	 */
	protected void close() {

		for (int i = 0; i < AVReceive2.mParticipantResourcesManagerVector.size(); i++) {
			try {
				// close the send stream
				if (((ParticipantResourcesManager) AVReceive2.mParticipantResourcesManagerVector.elementAt(i)).sendStream != null) {
					((ParticipantResourcesManager) AVReceive2.mParticipantResourcesManagerVector.elementAt(i)).sendStream
					.close();
				}
					
				// close the datasink
				((ParticipantResourcesManager) AVReceive2.mParticipantResourcesManagerVector.elementAt(i)).datasink
						.close();
				// we don't need to close the player 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		AVReceive2.mParticipantResourcesManagerVector.removeAllElements();

		// close the RTP session.
		if (mgrs==null) {
			System.out.println("yazebbi how can this shit happen !!");
		}
		for (int i = 0; i < mgrs.length; i++) {
			if (mgrs[i] != null) {
				System.out.println("CLOSING THE RTP SESSION !!! ");
				mgrs[i].removeTargets("Closing session from AVReceive2");
				mgrs[i].dispose();
				mgrs[i] = null;
			}
		}
	}

	/**
	 * Finds a corresponding resource manager to this participant  
	 * @param part the participant (i.e. the device) for which 
	 * @return the corresponding resource manager for this participant, null if no manager was found 
	 */
	ParticipantResourcesManager findParticipantResourceManager(Participant part) {
		for (int i = 0; i < AVReceive2.mParticipantResourcesManagerVector.size(); i++) {
			ParticipantResourcesManager mLinker = (ParticipantResourcesManager) AVReceive2.mParticipantResourcesManagerVector
					.elementAt(i);
			if (mLinker.participant == part)
				return mLinker;
		}
		return null;
	}
	
	
	/**
	 * SessionListener. Method called back in the SessionListener to notify
	 * listener of all Session Events. SessionEvents could be one of
	 * NewParticipantEvent or LocalCollisionEvent.
	 */
	public synchronized void update(SessionEvent evt) {
		if (evt instanceof NewParticipantEvent) {
			Participant p = ((NewParticipantEvent) evt).getParticipant();
			System.err.println("  - A new participant had just joined: "
					+ p.getCNAME());
			
		}
	}
	
	/**
	 * 
	 * @param ds
	 * @param stream
	 * @param relatedFrame
	 * @return
	 * @throws InvalidSessionAddressException
	 * @throws IOException
	 * @throws UnsupportedFormatException
	 */
	public SendStream prepareSendStream(DataSource ds, ReceiveStream stream, DeviceFrame relatedFrame) throws InvalidSessionAddressException, IOException, UnsupportedFormatException {
		SendStream sendStream=null;
		RTPManager sendRtpManager = RTPManager
				.newInstance();
		sendRtpManager.addFormat(new AudioFormat(
				AudioFormat.LINEAR, relatedFrame.getSelectedSamplingRate(), relatedFrame.getSelectedBitsPerSample(), relatedFrame.getSelectedChannel(),
				AudioFormat.LITTLE_ENDIAN, AudioFormat.SIGNED),
				SharedAttributes.getHashMap().get(relatedFrame.getSelectedSamplingRate()));

		// create the local end-point for the 
		//local interface on any local port
		SessionAddress localAddress = new SessionAddress();

		// initialize the RTPManager
		sendRtpManager.initialize(localAddress);

		// add the ReceiveStreamListener if you need to
		// receive
		// data and do other application specific stuff //
		// ...

		// specify the remote endpoint of this unicast
		// session
		// the address string and port numbers in the
		// following
		// lines need to be replaced with your values.
		InetAddress ipAddress = relatedFrame
				.getSelectedDevice().getSocket()
				.getInetAddress();
		SessionAddress remoteAddress = new SessionAddress(
				ipAddress, ServerAttributes.getInstance().getRTPAudioTransmitPort());

		// open the connection s
		sendRtpManager.addTarget(remoteAddress);

		sendStream = sendRtpManager
				.createSendStream(ds, 0);
		SourceDescription[] srcDescription = { ((SourceDescription) stream
				.getSenderReport().getSourceDescription()
				.get(0)) };

		sendStream.setSourceDescription(srcDescription);
		System.err.println("SSRC:------ "
				+ sendStream.getSSRC());
		
		return sendStream;
	}

	/**
	 * ReceiveStreamListener Interface that generates the callback for all
	 * RTPSessionManager Events. This interface will generate callbacks for
	 * events that pertain to a particular RTPRecvStream. This interface will
	 * also generate callbacks that pertain to state transitions of
	 * active/inactive of a passive participant as well.i.e. Active, Inactive,
	 * Timeout,ByeEvent will also be generated for passive participants and
	 * RTPRecvStream will be null in that case.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void update(ReceiveStreamEvent evt) {

		Participant participant = evt.getParticipant(); // could be null.

		ReceiveStream stream = evt.getReceiveStream(); // could be null.

		Player p = null  ; 
		
		if (evt instanceof RemotePayloadChangeEvent) {
			System.err.println("  - Received an RTP PayloadChangeEvent.");
			System.err.println("Sorry, cannot handle payload change.");
		}
		else if (evt instanceof StreamMappedEvent) {
			if (stream != null && stream.getDataSource() != null) {
				try {
					stream = ((StreamMappedEvent) evt).getReceiveStream();
					ParticipantResourcesManager amLinker = null;
					System.err.println("A new Stream has arrived");
					// check if the ReceiveStream is a new stream i.e coming
					// from a new participant
					DataSource ds = Manager
							.createCloneableDataSource(stream
									.getDataSource());
					DataSource clone1 = null;
					DataSource clone2 = null;
					DataSource clone3 = null;
					
					SendStream sendStream=null;
					//Preparing the sending process 
					
					DeviceFrame relatedFrame = null ; 
					// if we want to forward to another device
					if ((relatedFrame=MainView.deviceFrameHashMap.get(participant.getCNAME()))
								.isForward()) { 
						System.out.println("Another Device Selected");
						sendStream=prepareSendStream(ds, stream, relatedFrame);
						clone2 = ((SourceCloneable) ds).createClone();
						clone1 = ((SourceCloneable) ds).createClone();
						clone3 = ((SourceCloneable) ds).createClone(); ;
					} else {
						ds=null;
						clone2 = Manager.createCloneableDataSource(stream
								.getDataSource());
						clone1 = ((SourceCloneable) clone2).createClone();
						clone3 = ((SourceCloneable) clone2).createClone();
					}
					
					// Preparing the player 
				    //p = javax.media.Manager.createPlayer(clone1);
				    
				    // Preparing the writing process 

					
					if ((amLinker = findParticipantResourceManager(stream.getParticipant())) == null) {
						// i.e a new stream is mapped
						// in this case create the sendStream, dataSink and the
						// player that correspond to this participant('cause Stream = participant)
						// add a new audiomediaLinker, with the recently created attributes, to the audiomediaLinker

						String outputURL = ServerAttributes.getInstance().getRecordingDirectory()+"/"
								+ participant.getCNAME() +"Exp0"+ ".wav";
						DataSink dsink = new DataSourceHandler(outputURL, relatedFrame.getSelectedSamplingRate(), relatedFrame.getSelectedBitsPerSample(), relatedFrame.getSelectedChannel()) ;
						
						dsink.addDataSinkListener(this);
						dsink.setSource(clone2);
						System.err.println("Starting transcoding");
						dsink.open();
						
						DataSourceAnalyzer da=new DataSourceAnalyzer(participant.getCNAME());
						
						// adding the audiomediaLinker to the list
						amLinker = new ParticipantResourcesManager(stream.getParticipant(), p, sendStream,
								(DataSourceHandler)dsink,da);
						amLinker.receiveStream=stream;
						AVReceive2.mParticipantResourcesManagerVector.add(amLinker);
						amLinker.dataAnalyzer.setSource(clone3);
					} else {
						// we increment the count of how many times this participant is being sending a stream
						amLinker.mcount++;
						amLinker.receiveStream=stream;
						amLinker.sendStream=sendStream;
						amLinker.dataAnalyzer.setSource(clone3);
						//amLinker.player=p;
						// if the "option write to a new file" is activated then we have to create a totally new data sink that points to a new file
						// but before getting to this we must close the previous dataSink
						// if the option is not activated then we just modify the dataSource of our dataSink
						if (relatedFrame.isSavingOptionActivated()){
							String outputURL = ServerAttributes.getInstance().getRecordingDirectory()+"/"
									+ participant.getCNAME()+"Exp"+amLinker.mcount + ".wav";
							DataSink dsink = new DataSourceHandler(outputURL, relatedFrame.getSelectedSamplingRate(), relatedFrame.getSelectedBitsPerSample(), relatedFrame.getSelectedChannel()) ;
							dsink.addDataSinkListener(this);
	
	
							dsink.setSource(clone2);
							System.err.println("Starting transcoding");
							dsink.open();
							//close the previous dataSink
							amLinker.datasink.close();
							//now we can add the new dataSink
							amLinker.datasink=(DataSourceHandler)dsink;
						} else {
							amLinker.datasink.setSource(clone2);
						}
					}
					
					//starting the services
					MainView.deviceFrameHashMap.get(participant.getCNAME()).getParamInfoPan().setinfoMonitor(amLinker.mcount, true);
					if (amLinker.sendStream!=null) amLinker.sendStream.start(); // start the send stream if it exists
					//MainView.deviceFrameHashMap.get(participant.getCNAME())
						//	.setPlayer(amLinker.player); // start the player (when it is realized)

					//MainView.deviceFrameHashMap.get(participant.getCNAME()).setinfoMonitor(amLinker.mcount, true);

					amLinker.datasink.start(); // start the data sink 
					amLinker.dataAnalyzer.start(); 
					
				} catch (Exception e) {
					System.err.println("NewReceiveStreamEvent exception "
							+ e.getMessage());
					e.printStackTrace();
					ParticipantResourcesManager amLinker;
					if((amLinker = findParticipantResourceManager(stream.getParticipant())) != null) {
						System.err.println("An error occured.");
						if (amLinker.datasink!=null) {
							System.err.println("\tClosing datasink.");
							amLinker.datasink.close();
						}
						//if (amLinker.player!=null) {
						//	System.err.println("\tClosing player.");
						//	amLinker.player.close();
						//}
						if (amLinker.sendStream!=null) {
							System.err.println("\tClosing sendStream.");
							amLinker.sendStream.close();
						}
						System.err.println("\tRemoving the medialinker corresponding to this stream");
						AVReceive2.mParticipantResourcesManagerVector.remove(amLinker); //remove this participant from the list
					}
					return;
				}
			}

		}
		
		else if (evt instanceof InactiveReceiveStreamEvent) {
			participant = evt.getParticipant();
			System.out.println("Member "+participant.getCNAME()+" is inactive");
			//FIXME close whatever to be closed 
		}

		else if (evt instanceof ByeEvent) {
			if (stream!=null) {
				System.err.println("  - Got \"bye\" from: "
						+ participant.getCNAME());

				ParticipantResourcesManager amLinker;
				if((amLinker = findParticipantResourceManager(stream.getParticipant())) != null) {
					if (amLinker.datasink!=null) {
						System.err.println("\tClosing datasink.");
						amLinker.datasink.close();
					}
					//if (amLinker.player!=null) {
						//System.err.println("\tClosing player.");
						//amLinker.player.close();
					//}
					if (amLinker.sendStream!=null) {
						System.err.println("\tClosing sendStream.");
						amLinker.sendStream.close();
					}
					System.err.println("\tRemoving the medialinker corresponding to this stream");
					AVReceive2.mParticipantResourcesManagerVector.remove(amLinker); //remove this participant from the list
				}
			}

		}
	}


	public void dataSinkUpdate(DataSinkEvent evt) {

		if (evt instanceof EndOfStreamEvent) {
			System.err.println("Got EndOfStreamEvent from "+(DataSourceHandler)evt.getSourceDataSink());
		} else if (evt instanceof DataSinkErrorEvent) {
			System.err.println("Got DataSinkErrorEvent from "+(DataSourceHandler)evt.getSourceDataSink());
		}
	}

	/**
	 * A utility class to parse the session addresses.
	 */
	class SessionLabel {

		public String addr = null;
		public int port;
		public int ttl = 1;

		SessionLabel(String session) throws IllegalArgumentException {

			int off;
			String portStr = null, ttlStr = null;

			if (session != null && session.length() > 0) {
				while (session.length() > 1 && session.charAt(0) == '/')
					session = session.substring(1);

				// Now see if there's a addr specified.
				off = session.indexOf('/');
				if (off == -1) {
					if (!session.equals(""))
						addr = session;
				} else {
					addr = session.substring(0, off);
					session = session.substring(off + 1);
					// Now see if there's a port specified
					off = session.indexOf('/');
					if (off == -1) {
						if (!session.equals(""))
							portStr = session;
					} else {
						portStr = session.substring(0, off);
						session = session.substring(off + 1);
						// Now see if there's a ttl specified
						off = session.indexOf('/');
						if (off == -1) {
							if (!session.equals(""))
								ttlStr = session;
						} else {
							ttlStr = session.substring(0, off);
						}
					}
				}
			}

			if (addr == null)
				throw new IllegalArgumentException();

			if (portStr != null) {
				try {
					Integer integer = Integer.valueOf(portStr);
					if (integer != null)
						port = integer.intValue();
				} catch (Throwable t) {
					throw new IllegalArgumentException();
				}
			} else
				throw new IllegalArgumentException();

			if (ttlStr != null) {
				try {
					Integer integer = Integer.valueOf(ttlStr);
					if (integer != null)
						ttl = integer.intValue();
				} catch (Throwable t) {
					throw new IllegalArgumentException();
				}
			}
		}
	}


	/**
	 * Data structure that links a mapped received stream (a participant) to its
	 * dataSink object, its sendStrem and to its player
	 * 
	 * @author wissem
	 *
	 */
	class ParticipantResourcesManager {
		int mcount=0;
		ReceiveStream receiveStream;
		Player player;
		SendStream sendStream;
		DataSourceHandler datasink;
		Participant participant;
		DataSourceAnalyzer dataAnalyzer;

		public ParticipantResourcesManager(Participant part, Player p, SendStream s,
				DataSourceHandler d, DataSourceAnalyzer da) {
			this.participant = part;
			this.player = p;
			this.sendStream = s;
			this.datasink = d;
			this.dataAnalyzer=da;
		}
	}

	

	public void start(String argv[]) {

		this.sessions = argv;
		System.err.println("Starting session: " + this.sessions);

		if (!this.initialize()) {
			System.err.println("Failed to initialize the sessions.");
		} else {
			// we wait until we receive a close command
			// wait for a close notification 
			try {
				synchronized (AVReceive2.closeSync) {
					while (!AVReceive2.closedwindow) {
							AVReceive2.closeSync.wait();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("CLOSING AVRECEIVER!!!");
		this.close();
	}

	@Override
	public void run() {
		String[] arg = new String[] { ServerAttributes.getInstance().getRTPSessionAddress()+"/"+ServerAttributes.getInstance().getRTPAudioReceivePort() };
		start(arg);
		
	}

	
	
	

}// end of AVReceive2

