package cn.edu.shu.common.log;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MsgPane extends JPanel implements MsgListener {

    private JTextArea txtLog;
    private JPopupMenu popupMenu;

    public MsgPane() {
        super();
        initComponents();
    }

    private void initComponents() {
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtLog);

        popupMenu = new JPopupMenu();
        JMenuItem clearItem = new JMenuItem("Clear all");
        popupMenu.add(clearItem);
        clearItem.addActionListener(e -> txtLog.setText(""));

        txtLog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setVerticalGroup(layout.createParallelGroup().addComponent(scrollPane));
        layout.setHorizontalGroup(layout.createSequentialGroup().addContainerGap().addComponent(scrollPane).addContainerGap());
    }

    public void println(String message) {
        txtLog.append(message + "\r\n");
    }
}
