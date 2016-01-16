package epfl.lcav.view.matlabLinker;

/*
 * Copyright (c) 2013, Joshua Kaplan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 *  - Neither the name of matlabcontrol nor the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import matlabcontrol.PermissiveSecurityManager;

/**
 * A GUI example to demonstrate the main functionality of controlling MATLAB with matlabcontrol. The code in this
 * class (and the rest of the package) is not intended to serve as an example for how to use matlabcontrol, instead
 * the application exists to interactively demonstrate the API's capabilities.
 * <br><br>
 * The icon is part of the Um collection created by <a href="mailto:mattahan@gmail.com">mattahan (Paul Davey)</a>. It is
 * licensed under the <a href="http://creativecommons.org/licenses/by-nc-sa/3.0/">CC Attribution-Noncommercial-Share
 * Alike 3.0 License</a>.
 * 
 * @author <a href="mailto:nonother@gmail.com">Joshua Kaplan</a>
 */
@SuppressWarnings("serial")
public class MatlabFrame extends JPanel
{    
    //Status messages
    private static final String STATUS_DISCONNECTED = "Connection Status: Disconnected",
                                STATUS_CONNECTING = "Connection Status: Connecting",
                                STATUS_CONNECTED_EXISTING = "Connection Status: Connected (Existing)",
                                STATUS_CONNECTED_LAUNCHED = "Connection Status: Connected (Launched)";
    
    //Panel/Pane sizes
    //private static final int PANEL_WIDTH = 660;
   /* private static final Dimension CONNECTION_PANEL_SIZE = new Dimension(PANEL_WIDTH, 70),
                                   RETURN_PANEL_SIZE = new Dimension(PANEL_WIDTH, 250),
                                   METHOD_PANEL_SIZE = new Dimension(PANEL_WIDTH, 110 + 28 * 3),//ArrayPanel.NUM_ENTRIES),
                                   DESCRIPTION_PANE_SIZE = new Dimension(PANEL_WIDTH, 200),
                                   COMMAND_PANEL_SIZE = new Dimension(PANEL_WIDTH, METHOD_PANEL_SIZE.height +
                                                                                   DESCRIPTION_PANE_SIZE.height),
                                   MAIN_PANEL_SIZE = new Dimension(PANEL_WIDTH, CONNECTION_PANEL_SIZE.height +
                                                                                COMMAND_PANEL_SIZE.height + 
                                                                                RETURN_PANEL_SIZE.height);*/
    //Factory to create proxy
    private final MatlabProxyFactory _factory;
    
    //Proxy to communicate with MATLAB
    private final AtomicReference<MatlabProxy> _proxyHolder = new AtomicReference<MatlabProxy>();
    
    //UI components
    private JButton roomImpResp=new JButton("Compute Room Impulse Response");
    private JButton pairwiseDist=new JButton("Compute pairwise distances");
    private JButton devLocation=new JButton("Compute devices locations");
    /**
     * Create the main GUI.
     */
    public MatlabFrame(String matlabLocation)
    {
       
        
        System.setSecurityManager(new PermissiveSecurityManager());
        
        
        //Panel that contains the over panels
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        //mainPanel.setPreferredSize(MAIN_PANEL_SIZE);
       // mainPanel.setSize(MAIN_PANEL_SIZE);
        this.add(mainPanel);
        
        //Connection panel, button to connect, progress bar
        final JPanel connectionPanel = new JPanel();
        connectionPanel.setBackground(mainPanel.getBackground());
        connectionPanel.setBorder(BorderFactory.createTitledBorder(STATUS_DISCONNECTED));
        //connectionPanel.setPreferredSize(CONNECTION_PANEL_SIZE);
        //connectionPanel.setSize(CONNECTION_PANEL_SIZE);
        final JButton connectionButton = new JButton("Connect");
        connectionPanel.add(connectionButton);
        final JProgressBar connectionBar = new JProgressBar();
        connectionPanel.add(connectionBar);
            
        //Structure the panels so that on resize the layout updates appropriately
        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.add(connectionPanel, BorderLayout.NORTH); 
        mainPanel.add(combinedPanel, BorderLayout.NORTH);
        mainPanel.add(roomImpResp, BorderLayout.CENTER);
        //mainPanel.add(devLocation, BorderLayout.CENTER);
        //mainPanel.add(pairwiseDist, BorderLayout.CENTER);
        
        //Create proxy factory
        MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
                .setUsePreviouslyControlledSession(true)
                .setMatlabLocation(matlabLocation)
                .build();
        _factory = new MatlabProxyFactory(options);

        //Connect to MATLAB when the Connect button is pressed
        connectionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    //Request a proxy
                    _factory.requestProxy(new MatlabProxyFactory.RequestCallback()
                    {
                        @Override
                        public void proxyCreated(final MatlabProxy proxy)
                        {
                            _proxyHolder.set(proxy);
                        
                            proxy.addDisconnectionListener(new MatlabProxy.DisconnectionListener()
                            {
                                @Override
                                public void proxyDisconnected(MatlabProxy proxy)
                                {
                                    _proxyHolder.set(null); 
    
                                    //Visual update
                                    EventQueue.invokeLater(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            connectionPanel.setBorder(BorderFactory.createTitledBorder(STATUS_DISCONNECTED));
                                            connectionBar.setValue(0);
                                            connectionButton.setEnabled(true);
                                        }
                                    });
                                }
                            });
                    
                            //Visual update
                            EventQueue.invokeLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    String status;
                                    if(proxy.isExistingSession())
                                    {
                                        status = STATUS_CONNECTED_EXISTING;
                                    }
                                    else
                                    {
                                        status = STATUS_CONNECTED_LAUNCHED;
                                        
                                    }
                                    connectionPanel.setBorder(BorderFactory.createTitledBorder(status));
                                    connectionBar.setValue(100);
                                    connectionBar.setIndeterminate(false);
                                }
                            });
                        }
                    });
                    
                    //Update GUI
                    connectionPanel.setBorder(BorderFactory.createTitledBorder(STATUS_CONNECTING));
                    connectionBar.setIndeterminate(true);
                    connectionButton.setEnabled(false);
                }
                catch(MatlabConnectionException exc)
                {
                	exc.printStackTrace();
                }
            }
        });       
    }

}