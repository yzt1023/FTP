/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.ui.category;

import cn.edu.shu.common.util.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;

public abstract class CategoryPane extends JPanel {
    // current file path
    JLabel lblCategory;
    JTextField txtCategory;
    private JButton imgUp;
    // tree and table
    JTable ctgTable;
    JTree ctgTree;
    private JScrollPane scrollTable;
    private JScrollPane scrollTree;
    FileTreeNode root;
    // tableMenu
    JPopupMenu tableMenu;
    ActionListener menuListener;
    private JMenuItem openItem;
    private JMenuItem newItem;
    private JMenuItem deleteItem;
    private JMenuItem renameItem;
    private JMenuItem refreshItem;
    private JPopupMenu treeMenu;

    CategoryPane() {
        super();
        initComponents();
        setGroupLayout();
    }

    public void initComponents() {
        // new components
        lblCategory = new JLabel();
        txtCategory = new JTextField();
        imgUp = new JButton();
        // file table
        ctgTable = new JTable();
        scrollTable = new JScrollPane(ctgTable);
        tableMenu = new JPopupMenu();
        //file tree
        scrollTree = new JScrollPane();
        scrollTree.getViewport().setBackground(Color.WHITE);

        initMenuListener();
        initPopupMenu();
        initFileTable();
        initFilePath();

    }

    /**
     * initial tree popup menu item action cn.edu.shu.listener
     */
    private void initMenuListener() {
        menuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                switch (command) {
                    case "Upload":
                        uploadFile();
                        break;
                    case "Download":
                        downloadFile();
                        break;
                    case "Open":
                        openFile();
                        break;
                    case "New folder":
                        newFolder();
                        break;
                    case "Delete":
                        deleteFile();
                        break;
                    case "Rename":
                        int row = ctgTable.getSelectedRow();
                        ctgTable.editCellAt(row, 1);
                        break;
                    case "Refresh":
                        refreshTable();
                }
            }
        };
    }

    /**
     * initial table popup menu:
     * set icon and add action cn.edu.shu.listener
     */
    void initPopupMenu() {
        openItem = new JMenuItem("Open", new ImageIcon(Utils.getResourcePath(getClass(), "folder_open.png")));
        newItem = new JMenuItem("New folder", new ImageIcon(Utils.getResourcePath(getClass(), "create_folder.png")));
        deleteItem = new JMenuItem("Delete", new ImageIcon(Utils.getResourcePath(getClass(), "delete_folder.png")));
        renameItem = new JMenuItem("Rename", new ImageIcon(Utils.getResourcePath(getClass(), "rename.png")));
        refreshItem = new JMenuItem("Refresh", new ImageIcon(Utils.getResourcePath(getClass(), "refresh.png")));

        tableMenu.add(openItem);
        tableMenu.add(newItem);
        tableMenu.add(deleteItem);
        tableMenu.add(renameItem);
        tableMenu.addSeparator();
        tableMenu.add(refreshItem);

        openItem.addActionListener(menuListener);
        newItem.addActionListener(menuListener);
        deleteItem.addActionListener(menuListener);
        renameItem.addActionListener(menuListener);
        refreshItem.addActionListener(menuListener);
    }

    /**
     * initial file table:
     * some personalized setting
     * double click to enter folder or open file
     * tree popup menu provide some common setting such as adding or deleting folder
     */
    private void initFileTable() {
        // set properties
        ctgTable.setAutoCreateRowSorter(true);
        ctgTable.setShowGrid(false);
        ctgTable.setRowHeight(18);
        // table header align left
        ((DefaultTableCellRenderer) ctgTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        // icon width
        scrollTable.getViewport().setBackground(Color.WHITE);
        // edit cell while right click replace of double click
        DefaultCellEditor cellEditor = (DefaultCellEditor) ctgTable.getDefaultEditor(String.class);
        cellEditor.setClickCountToStart(3);

        scrollTable.addMouseListener(new MouseAdapter() {
            // windows
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenuItems(false);
                    tableMenu.show(e.getComponent(), e.getX(), e.getY());
                }
                ctgTable.clearSelection();
            }

        });

        ctgTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = ctgTable.rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && row >= 0 && row < ctgTable.getRowCount()) {
                    doubleClickPerformed(row);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
            }

            // windows
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenuItems(true);
                    tableMenu.show(e.getComponent(), e.getX(), e.getY());
                    int row = ctgTable.rowAtPoint(e.getPoint());
                    int[] rows = ctgTable.getSelectedRows();
                    boolean isSelected = false;
                    for (int r : rows) {
                        if (row == r) {
                            isSelected = true;
                            break;
                        }
                    }
                    if (!isSelected) {
                        ctgTable.setRowSelectionInterval(row, row);
                    }
                }
            }
        });

    }

    /**
     * initial file tree after set home dir:
     * set original tree node,
     * add action cn.edu.shu.listener while expand/click node
     * initial tree popup menu, with functions of expand/collapse/refresh node
     */
    void initFileTree() {
        ctgTree = new JTree(root);
        scrollTree.setViewportView(ctgTree);
        loadChildrenNode(root);
        // the first row expanded by default
        ctgTree.expandRow(0);
        ctgTree.setSelectionRow(0);
        root.setFirstExpand(false);

        ctgTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ctgTree.setCellRenderer(new FileTreeCellRenderer());  // display the system icon

        treeMenu = new JPopupMenu();
        JMenuItem collapseItem = new JMenuItem("Collapse");
        JMenuItem expandItem = new JMenuItem("Expand");
        JMenuItem reloadItem = new JMenuItem("Reload");
        treeMenu.add(collapseItem);
        treeMenu.add(expandItem);
        treeMenu.add(reloadItem);

        collapseItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ctgTree.collapsePath(ctgTree.getSelectionPath());
            }
        });
        expandItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ctgTree.expandPath(ctgTree.getSelectionPath());
            }
        });
        reloadItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileTreeNode node = (FileTreeNode) ctgTree.getSelectionPath().getLastPathComponent();
                reloadNode(node);
            }
        });

        ctgTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int row = ctgTree.getRowForLocation(e.getX(), e.getY());
                if (row < 0 || row >= ctgTree.getRowCount())
                    return;
                ctgTree.setSelectionRow(row);
                if (e.isPopupTrigger()) {
                    treeMenu.show(e.getComponent(), e.getX(), e.getY());
                } else {
                    FileTreeNode node = (FileTreeNode) ctgTree.getPathForRow(row).getLastPathComponent();
                    if (node.isFirstExpand()) {
                        loadChildrenNode(node);
                        node.setFirstExpand(false);
                        ctgTree.updateUI();
                    }
                    setCurrentTable(node.getUserObject());
                }
            }
        });
    }

    /**
     * initial file path:
     * add key cn.edu.shu.listener for path input field,
     * and add action cn.edu.shu.listener for button to up to parent dir
     */
    private void initFilePath() {
        // set properties
        imgUp.setIcon(new ImageIcon(Utils.getResourcePath(getClass(), "up.png")));
        imgUp.setContentAreaFilled(false);
        imgUp.setBorder(null);

        // add action cn.edu.shu.listener
        imgUp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                imgUp.setBorder(BorderFactory.createLoweredBevelBorder());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                imgUp.setBorder(null);
                changeToParentDirectory();
            }
        });

        txtCategory.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    changeDirectory();
                }
            }
        });
    }

    /**
     * set group layout, JLabel, JTextField, JTree, JTable included
     */
    private void setGroupLayout() {
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        // horizontal
        GroupLayout.SequentialGroup sequential1 = layout.createSequentialGroup();
        sequential1.addContainerGap();
        sequential1.addComponent(lblCategory);
        sequential1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        sequential1.addComponent(imgUp);
        sequential1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        sequential1.addComponent(txtCategory);
        sequential1.addContainerGap();

        GroupLayout.SequentialGroup sequential2 = layout.createSequentialGroup();
        sequential2.addContainerGap();
        sequential2.addComponent(scrollTree, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE);
        sequential2.addComponent(scrollTable);
        sequential2.addContainerGap();

        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup();
        horizontalGroup.addGroup(sequential1);
        horizontalGroup.addGroup(sequential2);
        layout.setHorizontalGroup(horizontalGroup);

        // vertical
        GroupLayout.ParallelGroup parallel1 = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        parallel1.addComponent(lblCategory);
        parallel1.addComponent(imgUp);
        parallel1.addComponent(txtCategory);

        GroupLayout.ParallelGroup parallel2 = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        parallel2.addComponent(scrollTree);
        parallel2.addComponent(scrollTable);

        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addContainerGap();
        verticalGroup.addGroup(parallel1);
        verticalGroup.addGroup(parallel2);
        verticalGroup.addContainerGap();
        layout.setVerticalGroup(verticalGroup);
    }

    /**
     * load children for node for establish tree use
     *
     * @param node tree node to load children
     */
    abstract void loadChildrenNode(FileTreeNode node);

    /**
     * remove original nodes, add new nodes after making some modifications to node children
     *
     * @param node node to rebuild children nodes
     */
    abstract void reloadChildrenNode(FileTreeNode node);

    /**
     * rebuild two layer nodes for good looks
     *
     * @param node node to rebuild generation nodes
     */
    private void reloadNode(FileTreeNode node) {
        reloadChildrenNode(node);
        ctgTree.updateUI();
    }

    /**
     * set if menu item enable;
     * while click scroll pane, some items shall be gray
     * add they can be click while user click the file table
     *
     * @param rowSelected if the file row be selected
     */
    void showMenuItems(boolean rowSelected) {
        openItem.setEnabled(rowSelected);
        deleteItem.setEnabled(rowSelected);
        renameItem.setEnabled(rowSelected);
    }

    /**
     * change to the path that user input in text field
     */
    abstract void changeDirectory();

    /**
     * change to the parent directory after user click the up button
     */
    abstract void changeToParentDirectory();

    /**
     * do nothing, should be override in subclass
     */
    void uploadFile() {
    }

    /**
     * do nothing, should be override in subclass
     */
    void downloadFile() {
    }

    abstract void enterFolder(Object object);

    abstract void openFile();

    abstract void doubleClickPerformed(int row);

    abstract void refreshTable();

    abstract void deleteFile();

    abstract void newFolder();

    @Override
    public void setEnabled(boolean enabled) {
        lblCategory.setEnabled(enabled);
        txtCategory.setEnabled(enabled);
        imgUp.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    abstract void setCurrentTable(Object object);

}
