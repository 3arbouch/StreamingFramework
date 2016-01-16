package epfl.lcav.view;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import epfl.lcav.model.Device;
/**
 * 
 * @author mohamed
 *
 */
public class DeviceCellRenderForDeviceFrame extends JPanel implements
		ListCellRenderer<Device> {

	/**
	 * Inherited field
	 */
	private static final long serialVersionUID = 1L;

	private Device concernedDevice;

	public DeviceCellRenderForDeviceFrame(Device device) {
		this.concernedDevice = device;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Device> list,
			Device value, int index, boolean isSelected, boolean cellHasFocus) {
		JPanel panel = new JPanel();
		JButton button;
		if (!value.equals(this.concernedDevice)) {
			button = new JButton(value.getIdentifier());
			panel.add(button);

		}

		else {
			button = new JButton("Server");
			panel.add(button);
		}

		if (isSelected) {
			System.out.println("The device " + value + "is selected");
			button.setBackground(Color.white);
			button.setForeground(new Color(255, 0, 0));
		} else {
			button.setBackground(Color.white);
			button.setForeground(Color.black);

		}
		return panel;
	}
}
