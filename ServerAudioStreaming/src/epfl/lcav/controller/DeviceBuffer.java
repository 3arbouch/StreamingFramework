package epfl.lcav.controller;

import java.util.ArrayList;

import epfl.lcav.model.Device;

/**
 * Class that represents the set of devices connected to the server. Implements
 * the singleton pattern and handles the concurrent access to the stored devices 
 * 
 * @author wissem.allouchi@epfl.ch
 *
 */
public class DeviceBuffer {

	private static DeviceBuffer INSTANCE = null;

	private ArrayList<Device> list;

	private int currentIndex;

	private DeviceBuffer() {
		this.list = new ArrayList<Device>();
		this.currentIndex = 0;
	}

	/**
	 * this method returns the unique instance of the list of connected devices
	 * if it exists, if not it creates an instance and return it
	 * 
	 * @return the unique instance of the list of connected devices
	 */
	public static DeviceBuffer getInstance() {
		if (DeviceBuffer.INSTANCE == null)
			DeviceBuffer.INSTANCE=new DeviceBuffer(); 
		return INSTANCE;
		
			
	}


	/**
	 * Add the recently connected device to the connected device list
	 * 
	 * @param d
	 *            the newly connected device
	 */
	public synchronized void addDevice(Device d) {
		this.list.add(d);
		this.currentIndex++;
	}

	/**
	 * retrieve a device according to its id. this operation does not remove the
	 * device from the list
	 * 
	 * @param id
	 *            the id of the device to be returned
	 * @return the device that its identifier is the id set as parameter
	 */
	public synchronized Device getDevice(Device d) {
		int ind= this.list.indexOf(d);
		return this.list.get(ind);
	}
	/**
	 * removes a device according to its id. 
	 * 
	 * @param id
	 *            the id of the device to be removed
	 * @return the device that has been removed
	 */
	public synchronized boolean removeDevice(Device d) {
		this.currentIndex=this.currentIndex-1;
		return this.list.remove(d);
		
	}

	public int getCurrentIndex() {
		return currentIndex;
	}
	
	public String toString () {
		return this.list.toString();
	}

	public ArrayList<Device> getList() {
		return list;
	}
	
	
	
}
