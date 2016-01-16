package epfl.lcav.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import epfl.lcav.model.Device;

public class DeviceCellRenderer extends JPanel implements
		ListCellRenderer<Device>, ActionListener {

	/**
	 * Inherited field
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<? extends Device> list,
			Device value, int index, boolean isSelected, boolean cellHasFocus) {

		JButton button = new JButton(value.getIdentifier());

		JPanel panel = new JPanel();

		panel.add(button); 

		if (isSelected && !value.isHaveUI()) {
			DeviceFrame frame = new DeviceFrame(value);
			frame.showFrame() ; 
			MainView.deviceFrameHashMap.put(value.getIdentifier(), frame);
			value.setHaveUI(true);
		}
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("I'AM HEEEEREEEEE");
		@SuppressWarnings("rawtypes")
		JComboBox cb = (JComboBox) e.getSource();
		String samplingRate = (String) cb.getSelectedItem();
		cb.setSelectedItem(samplingRate);

	}

}
