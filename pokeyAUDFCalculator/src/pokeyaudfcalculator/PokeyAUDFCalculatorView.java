/*
 * PokeyAUDFCalculatorView.java
 */

package pokeyaudfcalculator;

import java.awt.Color;
import javax.swing.JTextField;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * The application's main frame.
 */
public class PokeyAUDFCalculatorView extends FrameView {

    private final static String CLOCKCPU = "1.78979 MHz";
    private final static String CLOCK64KHZ = "63.9210 kHz";
    private final static String CLOCK15KHZ = "15.6999 kHz";

    private final static int REG8BITSSIZE = 255;
    private final static int REG16BITSSIZE = 65535;

    private final static double CLOCKCPUVAL = 1.78979*1000000;
    private final static double CLOCK64KHZVAL = 63.9210*1000;
    private final static double CLOCK15KHZVAL = 15.6999*1000;

    private int frequency = 440;
    private int audfregister = 71;
    private double clock = CLOCK64KHZVAL;
    private int registerSize = REG8BITSSIZE;

    public PokeyAUDFCalculatorView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = PokeyAUDFCalculatorApp.getApplication().getMainFrame();
            aboutBox = new PokeyAUDFCalculatorAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        PokeyAUDFCalculatorApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jComboBoxClock = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldAUDF = new javax.swing.JTextField();
        jTextFieldFreq = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jButtonComputeFreq = new javax.swing.JButton();
        jButtonComputeReg = new javax.swing.JButton();
        jRadioButton8bits = new javax.swing.JRadioButton();
        jRadioButton16bits = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabelClock = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu optionsMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jLabelStatusMessage = new javax.swing.JLabel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();

        mainPanel.setName("mainPanel"); // NOI18N

        jComboBoxClock.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "63.9210 kHz", "15.6999 kHz", "1.78979 MHz" }));
        jComboBoxClock.setName("jComboBoxClock"); // NOI18N
        jComboBoxClock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxClockActionPerformed(evt);
            }
        });

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(pokeyaudfcalculator.PokeyAUDFCalculatorApp.class).getContext().getResourceMap(PokeyAUDFCalculatorView.class);
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jTextFieldAUDF.setText(resourceMap.getString("jTextFieldAUDF.text")); // NOI18N
        jTextFieldAUDF.setToolTipText(resourceMap.getString("jTextFieldAUDF.toolTipText")); // NOI18N
        jTextFieldAUDF.setName("jTextFieldAUDF"); // NOI18N
        jTextFieldAUDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldAUDFActionPerformed(evt);
            }
        });
        jTextFieldAUDF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldAUDFFocusLost(evt);
            }
        });

        jTextFieldFreq.setText(resourceMap.getString("jTextFieldFreq.text")); // NOI18N
        jTextFieldFreq.setToolTipText(resourceMap.getString("jTextFieldFreq.toolTipText")); // NOI18N
        jTextFieldFreq.setName("jTextFieldFreq"); // NOI18N
        jTextFieldFreq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFreqActionPerformed(evt);
            }
        });
        jTextFieldFreq.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldFreqFocusLost(evt);
            }
        });

        jLabel4.setText(resourceMap.getString("jLabelFreq.text")); // NOI18N
        jLabel4.setName("jLabelFreq"); // NOI18N

        jButtonComputeFreq.setText(resourceMap.getString("jButtonComputeFreq.text")); // NOI18N
        jButtonComputeFreq.setToolTipText(resourceMap.getString("jButtonComputeFreq.toolTipText")); // NOI18N
        jButtonComputeFreq.setName("jButtonComputeFreq"); // NOI18N
        jButtonComputeFreq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonComputeFreqActionPerformed(evt);
            }
        });

        jButtonComputeReg.setText(resourceMap.getString("jButtonComputeReg.text")); // NOI18N
        jButtonComputeReg.setToolTipText(resourceMap.getString("jButtonComputeReg.toolTipText")); // NOI18N
        jButtonComputeReg.setName("jButtonComputeReg"); // NOI18N
        jButtonComputeReg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonComputeRegActionPerformed(evt);
            }
        });

        jRadioButton8bits.setSelected(true);
        jRadioButton8bits.setText(resourceMap.getString("jRadioButton8bits.text")); // NOI18N
        jRadioButton8bits.setName("jRadioButton8bits"); // NOI18N
        jRadioButton8bits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton8bitsActionPerformed(evt);
            }
        });

        jRadioButton16bits.setText(resourceMap.getString("jRadioButton16bits.text")); // NOI18N
        jRadioButton16bits.setName("jRadioButton16bits"); // NOI18N
        jRadioButton16bits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton16bitsActionPerformed(evt);
            }
        });

        jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabelClock.setText(resourceMap.getString("jLabelClock.text")); // NOI18N
        jLabelClock.setName("jLabelClock"); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton16bits)
                    .addComponent(jRadioButton8bits, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabelClock))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextFieldFreq)
                            .addComponent(jTextFieldAUDF)
                            .addComponent(jComboBoxClock, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonComputeReg, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                            .addComponent(jButtonComputeFreq, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE))))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap(29, Short.MAX_VALUE)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabelClock)
                    .addComponent(jComboBoxClock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldAUDF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonComputeFreq)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldFreq, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonComputeReg))
                .addGap(18, 18, 18)
                .addComponent(jRadioButton8bits)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton16bits)
                .addGap(20, 20, 20))
        );

        menuBar.setName("menuBar"); // NOI18N

        optionsMenu.setText(resourceMap.getString("optionsMenu.text")); // NOI18N
        optionsMenu.setName("optionsMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(pokeyaudfcalculator.PokeyAUDFCalculatorApp.class).getContext().getActionMap(PokeyAUDFCalculatorView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        optionsMenu.add(exitMenuItem);

        menuBar.add(optionsMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        jLabelStatusMessage.setText(resourceMap.getString("jLabelStatusMessage.text")); // NOI18N
        jLabelStatusMessage.setName("jLabelStatusMessage"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusMessageLabel)
                    .addComponent(jLabelStatusMessage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 145, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(statusMessageLabel)
                            .addComponent(statusAnimationLabel)
                            .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3))
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelStatusMessage)
                        .addContainerGap())))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonComputeFreqActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonComputeFreqActionPerformed
    {//GEN-HEADEREND:event_jButtonComputeFreqActionPerformed
        System.out.println("Compute Frequency");
        if(this.audfregister != 0)
        {
            // F = C / 2(N+1)
            this.frequency = (int) (this.clock / (2.0f * (this.audfregister + 1.0f)) );
            jTextFieldFreq.setText(String.valueOf(this.frequency));

            jLabelStatusMessage.setText("Note Frequency : " + frequency + " Hz");
            jLabelStatusMessage.setForeground(Color.black);
        }
        else
        {
            jLabelStatusMessage.setText("The AUDF register must be set !");
            jLabelStatusMessage.setForeground(Color.red);
            this.jTextFieldAUDF.requestFocus();
        }
    }//GEN-LAST:event_jButtonComputeFreqActionPerformed

    private void jButtonComputeRegActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonComputeRegActionPerformed
    {//GEN-HEADEREND:event_jButtonComputeRegActionPerformed
        System.out.println("Compute Register");
        if(this.frequency != 0)
        {
            int result = (int) ((this.clock / (2.0f * this.frequency)) - 1.0f );
            if(result > registerSize )
            {
                jLabelStatusMessage.setText("The register size is too small to handle the result !");
                jLabelStatusMessage.setForeground(Color.red);
                this.jTextFieldFreq.requestFocus();
            }
            else
            {
                this.audfregister = result;
                jTextFieldAUDF.setText(String.valueOf(this.audfregister));

                jLabelStatusMessage.setText("Register value => Dec: " + audfregister + " Hex: " + Integer.toHexString(audfregister) + " Bin: " + Integer.toBinaryString(audfregister));
                jLabelStatusMessage.setForeground(Color.black);
            }
        }
        else
        {
            jLabelStatusMessage.setText("The Frequency value must be set !");
            jLabelStatusMessage.setForeground(Color.red);
            this.jTextFieldFreq.requestFocus();
        }
    }//GEN-LAST:event_jButtonComputeRegActionPerformed

    private void jRadioButton16bitsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButton16bitsActionPerformed
    {//GEN-HEADEREND:event_jRadioButton16bitsActionPerformed
        System.out.println("Switch to 16bits");
        jRadioButton16bits.setSelected(true);
        jRadioButton8bits.setSelected(false);

        this.registerSize = REG16BITSSIZE;
    }//GEN-LAST:event_jRadioButton16bitsActionPerformed

    private void jRadioButton8bitsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButton8bitsActionPerformed
    {//GEN-HEADEREND:event_jRadioButton8bitsActionPerformed
        System.out.println("Switch to 8bits");
        jRadioButton16bits.setSelected(false);
        jRadioButton8bits.setSelected(true);

        this.registerSize = REG8BITSSIZE;

    }//GEN-LAST:event_jRadioButton8bitsActionPerformed

    private void jTextFieldFreqActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldFreqActionPerformed
    {//GEN-HEADEREND:event_jTextFieldFreqActionPerformed
        if(checkFrequencyValue(this.jTextFieldFreq.getText()))
        {
            this.frequency = Integer.parseInt(this.jTextFieldFreq.getText());
            jLabelStatusMessage.setText("Frequency set to : " + this.frequency + " Hz");
            jLabelStatusMessage.setForeground(Color.black);
        }
        else
        {
            jLabelStatusMessage.setText("This is not a valid value for the frequency");
            jLabelStatusMessage.setForeground(Color.red);
            this.jTextFieldFreq.requestFocus();
        }
    }//GEN-LAST:event_jTextFieldFreqActionPerformed

    private void jTextFieldFreqFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldFreqFocusLost
    {//GEN-HEADEREND:event_jTextFieldFreqFocusLost
        if(checkFrequencyValue(this.jTextFieldFreq.getText()))
        {
            this.frequency = Integer.parseInt(this.jTextFieldFreq.getText());
            jLabelStatusMessage.setText("Frequency set to : " + this.frequency + " Hz");
            jLabelStatusMessage.setForeground(Color.black);   
        }
        else
        {
            jLabelStatusMessage.setText("This is not a valid value for the frequency");
            jLabelStatusMessage.setForeground(Color.red);
            this.jTextFieldFreq.requestFocus();
        }
    }//GEN-LAST:event_jTextFieldFreqFocusLost

    private void jComboBoxClockActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jComboBoxClockActionPerformed
    {//GEN-HEADEREND:event_jComboBoxClockActionPerformed
        String val = (String) jComboBoxClock.getItemAt(jComboBoxClock.getSelectedIndex());        
        this.clock = getClockfreq(val);
        System.out.println("New clock set to : " + val + "(" + this.clock + ")");
    }//GEN-LAST:event_jComboBoxClockActionPerformed

    private void jTextFieldAUDFActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldAUDFActionPerformed
    {//GEN-HEADEREND:event_jTextFieldAUDFActionPerformed
        if(checkRegisterValue(this.jTextFieldAUDF.getText()))
        {
            this.audfregister = Integer.parseInt(this.jTextFieldAUDF.getText());
            jLabelStatusMessage.setText("AUF register set to : " + this.audfregister);
            jLabelStatusMessage.setForeground(Color.black);
        }
        else
        {
            jLabelStatusMessage.setText("This is not a valid value for the register");
            jLabelStatusMessage.setForeground(Color.red);
            this.jTextFieldAUDF.requestFocus();
        }
    }//GEN-LAST:event_jTextFieldAUDFActionPerformed

    private void jTextFieldAUDFFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldAUDFFocusLost
    {//GEN-HEADEREND:event_jTextFieldAUDFFocusLost
        if(checkRegisterValue(this.jTextFieldAUDF.getText()))
        {
            this.audfregister = Integer.parseInt(this.jTextFieldAUDF.getText());
            jLabelStatusMessage.setText("AUF register set to : " + this.audfregister);
            jLabelStatusMessage.setForeground(Color.black);
        }
        else
        {
            jLabelStatusMessage.setText("This is not a valid value for the register");
            jLabelStatusMessage.setForeground(Color.red);
            this.jTextFieldAUDF.requestFocus();
        }
    }//GEN-LAST:event_jTextFieldAUDFFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton jButtonComputeFreq;
    private javax.swing.JButton jButtonComputeReg;
    private javax.swing.JComboBox jComboBoxClock;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelClock;
    private javax.swing.JLabel jLabelStatusMessage;
    private javax.swing.JRadioButton jRadioButton16bits;
    private javax.swing.JRadioButton jRadioButton8bits;
    private javax.swing.JTextField jTextFieldAUDF;
    private javax.swing.JTextField jTextFieldFreq;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    private boolean checkFrequencyValue(String svalue)
    {
        boolean res = false;
        try
        {
            int val = Integer.parseInt(svalue);
            if(val >= 0 && val < 1000000) res = true;
        }
        catch(NumberFormatException ex)
        {
            res = false;
        }

        return res;
    }
    
    private boolean checkRegisterValue(String svalue)
    {
        boolean res = false;
        try
        {
            int val = Integer.parseInt(svalue);
            System.out.print(registerSize);
            if(val >= 0 && val <= registerSize) res = true;
        }
        catch(NumberFormatException ex)
        {
            res = false;
        }

        return res;
    }



    private double getClockfreq(String val)
    {
        //"1.78979 MHz", "63.9210 kHz", "15.6999 kHz"
        if(val.equalsIgnoreCase(CLOCK15KHZ))
        {
            return CLOCK15KHZVAL;
        }
        else if(val.equalsIgnoreCase(CLOCK64KHZ))
        {
            return CLOCK64KHZVAL;
        }
        else if(val.equalsIgnoreCase(CLOCKCPU))
        {
            return CLOCKCPUVAL;
        }

        return 0;
    }
}
