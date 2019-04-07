package cn.edu.shu.client.ui;

import cn.edu.shu.client.listener.ConnectListener;
import cn.edu.shu.common.util.Constants;
import cn.edu.shu.common.util.MessageUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class ConnectPane extends JPanel {

    private static final String CONNECT = "connect";
    private static final String DISCONNECT = "disconnect";
    private JLabel lblUsername;
    private JTextField txtUsername;
    private JLabel lblPwd;
    private JPasswordField txtPwd;
    private JLabel lblHost;
    private JTextField txtHost;
    private JLabel lblPort;
    private JTextField txtPort;
    private JCheckBox cbAnonymous;
    private JButton btnConnect;
    private ConnectListener listener;

    ConnectPane(ConnectListener listener) {
        super();
        this.listener = listener;
        initComponents();
        setGroupLayout();
    }

    private void initComponents() {

        // new components
        lblUsername = new JLabel("Username: ");
        txtUsername = new JTextField();
        lblPwd = new JLabel("Password: ");
        txtPwd = new JPasswordField();
        lblHost = new JLabel("Host: ");
        txtHost = new JTextField();
        lblPort = new JLabel("Port: ");
        txtPort = new JTextField();
        txtPort.setText(String.valueOf(Constants.DEFAULT_PORT));
        cbAnonymous = new JCheckBox("Anonymous");
        btnConnect = new JButton(CONNECT);

        txtPwd.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    btnConnect.doClick();
                }
            }
        });

        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("connect".equals(btnConnect.getText()))
                    connect();
                else {
                    listener.fireDisconnect();
                    setInputEnabled(true);
                    btnConnect.setText(CONNECT);
                    listener.afterDisconnect();
                }
            }
        });

    }

    private void connect() {
        String host = txtHost.getText();
        String port = txtPort.getText();
        boolean anonymous = cbAnonymous.isSelected();
        int portNum = Constants.DEFAULT_PORT;
        String username = txtUsername.getText();
        char[] password = txtPwd.getPassword();
        if (host.isEmpty()) {
            MessageUtils.showErrorMessage(Constants.EMPTY_HOST);
            return;
        }
        if (!port.isEmpty()) {
            try {
                portNum = Integer.parseInt(txtPort.getText());
            } catch (NumberFormatException exception) {
                MessageUtils.showErrorMessage(Constants.INVALID_PORT);
                return;
            }
        }
        if (anonymous && listener.fireConnect(host, portNum)) {
            afterConnect();
            return;
        }
        if (!anonymous && username.isEmpty()) {
            MessageUtils.showErrorMessage(Constants.EMPTY_USER);
            return;
        }
        if (!anonymous && listener.fireConnect(host, portNum, username, new String(password))) {
            afterConnect();
            return;
        }
        MessageUtils.showErrorMessage(Constants.CONNECT_FAILED);
    }

    private void afterConnect() {
        btnConnect.setText(DISCONNECT);
        setInputEnabled(false);
        listener.afterConnect();
    }

    private void setGroupLayout() {
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        GroupLayout.SequentialGroup sequentialGroup = layout.createSequentialGroup();
        sequentialGroup.addContainerGap();
        //host
        sequentialGroup.addComponent(lblHost);
        sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        sequentialGroup.addComponent(txtHost, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE);
        sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        //port
        sequentialGroup.addComponent(lblPort);
        sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        sequentialGroup.addComponent(txtPort, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE);
        sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        //username
        sequentialGroup.addComponent(lblUsername);
        sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        sequentialGroup.addComponent(txtUsername, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE);
        sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        //password
        sequentialGroup.addComponent(lblPwd);
        sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        sequentialGroup.addComponent(txtPwd, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE);
        sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        //button
        sequentialGroup.addComponent(cbAnonymous);
        sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        sequentialGroup.addComponent(btnConnect);
        sequentialGroup.addContainerGap();

        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup();
        horizontalGroup.addGroup(sequentialGroup);

        layout.setHorizontalGroup(horizontalGroup);

        GroupLayout.ParallelGroup parallelGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        parallelGroup.addComponent(lblHost);
        parallelGroup.addComponent(txtHost);
        parallelGroup.addComponent(lblPort);
        parallelGroup.addComponent(txtPort);
        parallelGroup.addComponent(lblUsername);
        parallelGroup.addComponent(txtUsername);
        parallelGroup.addComponent(lblPwd);
        parallelGroup.addComponent(txtPwd);
        parallelGroup.addComponent(cbAnonymous);
        parallelGroup.addComponent(btnConnect);

        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addContainerGap();
        verticalGroup.addGroup(parallelGroup);
        verticalGroup.addContainerGap();

        layout.setVerticalGroup(verticalGroup);
    }

    private void setInputEnabled(boolean enabled) {
        txtHost.setEnabled(enabled);
        lblHost.setEnabled(enabled);
        txtPort.setEnabled(enabled);
        lblPort.setEnabled(enabled);
        txtUsername.setEnabled(enabled);
        lblUsername.setEnabled(enabled);
        txtPwd.setEnabled(enabled);
        lblPwd.setEnabled(enabled);
    }

}
