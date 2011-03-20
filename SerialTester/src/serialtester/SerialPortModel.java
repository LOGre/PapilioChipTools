/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package serialtester;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author admin
 */
public class SerialPortModel extends AbstractListModel implements ComboBoxModel
{

    private String[] ports =
    {
        "No port"
    };
    private String selection = null;

    public SerialPortModel(String[] ports)
    {
        this.ports = ports;
    }

    public Object getElementAt(int index)
    {
        return ports[index];
    }

    public int getSize()
    {
        return ports.length;
    }

    public void setSelectedItem(Object anItem)
    {
        selection = (String) anItem; // to select and register an
    } // item from the pull-down list

    // Methods implemented from the interface ComboBoxModel
    public Object getSelectedItem()
    {
        return selection; // to add the selection to the combo box
    }

}
