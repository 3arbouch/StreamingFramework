package epfl.lcav.view;

import java.util.ArrayList;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import epfl.lcav.model.Device;
import epfl.lcav.model.DeviceAcceptor;
// TODO FULLY COMPLETE JAVA DOC
/**
 * 
 * @author wissem
 *
 */
public class DeviceListModel implements ListModel<Device> {
	
	private ArrayList<Device> list=null;
	
	public DeviceListModel () {
		this.list=DeviceAcceptor.getInstance().getDevicesList().getList();
	}
	@Override
	public int getSize() {
		
		return this.list.size();
	}

	@Override
	public Device getElementAt(int index) {
		return this.list.get(index);
		
	}

	@Override
	public void addListDataListener(ListDataListener l) {
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		
	}

}
