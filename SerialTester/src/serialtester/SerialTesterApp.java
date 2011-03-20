/*
 * SerialTesterApp.java
 */
package serialtester;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class SerialTesterApp extends SingleFrameApplication
{

    private String port;
    private int delay = 1000;
    private int baudrate = 115200;
    private boolean isSending = false;
    private SerialPort serialPort;
    private String[] portNames;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup()
    {
        show(new SerialTesterView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root)
    {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of SerialTesterApp
     */
    public static SerialTesterApp getApplication()
    {
        return Application.getInstance(SerialTesterApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args)
    {
        launch(SerialTesterApp.class, args);
    }

    void selectSerialPort(int index)
    {
        this.port = portNames[index];
        System.out.println("Selected port : " + port);
    }

    public SerialPortModel getSerialPortModel()
    {
        portNames = SerialPortList.getPortNames();

        for (int i = 0; i < portNames.length; i++)
        {
            System.out.println("[" + i + "] " + portNames[i]);
        }

        this.port = portNames[0];
        return new SerialPortModel(portNames);
    }

    public void setBaudRate(int value)
    {
        System.out.println("Set baud rate to " + value);
        this.baudrate = value;
    }

    public void setDelay(int value)
    {
        System.out.println("Set delay to " + value);
        this.delay = value;
    }

    public synchronized void sendData(Byte[] bytes) throws NoDataException, PleaseWaitException
    {
        if (isSending)
        {
            throw new PleaseWaitException();
        }

        isSending = true;
        ArrayList<Byte> byteList = new ArrayList<Byte>();
        for (int i = 0; i < bytes.length; i++)
        {
            Byte aByte = bytes[i];
            if (aByte != null)
            {
                byteList.add(aByte);
                System.out.println("Sent : " + aByte);
            }
        }
        if (byteList.isEmpty())
        {
            throw new NoDataException();
        }

        System.out.println("Set deta to " + port + " with delay " + delay);

        try
        {
            serialPort = new SerialPort(this.port);
            serialPort.openPort();
            serialPort.setParams(this.baudrate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            Iterator<Byte> it = byteList.iterator();
            while (it.hasNext())
            {
                Byte aByte = it.next();
                serialPort.writeBytes(new byte[] { aByte.byteValue() } );
                Thread.sleep(this.delay);
            }
            // close everything
            serialPort.closePort();
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(SerialTesterApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SerialPortException ex)
        {
            Logger.getLogger(SerialTesterApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        isSending = false;

    }

    public boolean checkValue(short val)
    {
        boolean res = true;
        if (val > 255 || val < 0)
        {
            res = false;
        }
        return res;
    }
}
