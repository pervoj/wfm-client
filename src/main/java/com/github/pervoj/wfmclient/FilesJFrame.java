/* FilesJFrame.java
 *
 * Copyright (C) 2021 Vojtěch Perník <pervoj@gmx.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.pervoj.wfmclient;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.pervoj.jiconfont.FontAwesomeSolid;
import java.awt.Color;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import jiconfont.swing.IconFontSwing;

/**
 * Main JFrame of this application
 *
 * @author Vojtěch Perník <pervoj@gmx.com>
 */
public class FilesJFrame extends javax.swing.JFrame {
    private SettingsManager config;
    private ArrayList<String> serverList;

    /**
     * Main JFrame constructor method
     */
    public FilesJFrame() {
        IconFontSwing.register(FontAwesomeSolid.getIconFont());
        
        try { // Try load settings
            config = new SettingsManager();
        } catch (Exception e) {
            // Show error and close application if something went wrong
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error loading settings", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Init components and move the window to the center of the screen
        initComponents();
        setLocationRelativeTo(null);
        
        serverList = new ArrayList<>(); // Define server array list instance
        
        try { // Try sort server list file
            sortList();
        } catch (Exception e) {
            // Show error if something went wrong
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error sorting server list", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Method for loading file tree after the frame is shown
     */
    public void initList() {
        loadList();
    }

    /**
     * Method for loading server list file to array list and file tree
     */
    private void loadList() {
        serverList.clear(); // Clear server array list
        
        // Read server list file and load it to array list
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(config.getServerListFile()), StandardCharsets.UTF_8))) {
            String s;
            while ((s = br.readLine()) != null) {
                serverList.add(s);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error reading server list", JOptionPane.ERROR_MESSAGE);
        }
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Connected servers"); // Define file tree root item
        
        for (int i = 0; i < serverList.size(); i++) { // Iterate server array list
            try { // Try add to root item server item from server api
                root.add(new ApiParser().getFilesNode(serverList.get(i).split("///")[1], serverList.get(i).split("///")[0]));
            } catch (Exception e) {
                // Show error and information message if something went wrong
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error connecting server " + serverList.get(i).split("///")[0], JOptionPane.ERROR_MESSAGE);
                JOptionPane.showMessageDialog(this, "An error occurred while connecting to server " + serverList.get(i).split("///")[0] + " (" + serverList.get(i).split("///")[1] + "). "
                        + "This server will NOT be displayed in server tree, but you can remove it in remove dialog. "
                        + "Until you remove this server this way, it is likely that the same error will occur again. "
                        + "If you are sure that the error is on the server side and will be resolved soon, you do not need to remove the server.", "Important message", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        filesJTree.setModel(new DefaultTreeModel(root)); // Set tree model from root item
    }
    
    /**
     * Method for saving server list file from array list
     * 
     * @throws Exception when writing to file fails
     */
    private void saveList() throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config.getServerListFile()), StandardCharsets.UTF_8))) {
            for (int i = 0; i < serverList.size(); i++) {
                bw.write(serverList.get(i));
                bw.newLine();
            }
            bw.flush();
        }
    }
    
    /**
     * Method for sorting server list file
     * 
     * @throws Exception when reading file or writing to file fails
     */
    private void sortList() throws Exception {
        ArrayList<String> serverList = new ArrayList<>(); // Define instance of array list
        
        // Load server list file to this array list
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(config.getServerListFile()), StandardCharsets.UTF_8))) {
            String s;
            while ((s = br.readLine()) != null) {
                serverList.add(s);
            }
        }
        
        Collections.sort(serverList); // Sort array list
        
        // Write array list back to server list file
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config.getServerListFile()), StandardCharsets.UTF_8))) {
            for (int i = 0; i < serverList.size(); i++) {
                bw.write(serverList.get(i));
                bw.newLine();
            }
            bw.flush();
        }
    }
    
    /**
     * Method for remove server action
     */
    private void addServer() {
        // Open add dialog
        AddServerJDialog dialog = new AddServerJDialog(this, true, serverList);
        dialog.setVisible(true);
        
        try {
            if (!dialog.isCanceled()) { //If dialog wasn't canceled
                // Save server list to file
                saveList();
                sortList();
                loadList();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error adding server", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Method for remove server action
     */
    private void removeServer() {
        // Open remove dialog
        RemoveServerJDialog dialog = new RemoveServerJDialog(this, true, serverList);
        dialog.setVisible(true);
        
        try { // Try save new server list and load it
            if (!dialog.isCanceled()) { // If dialog wasn't canceled
                // Save server list to file
                saveList();
                sortList();
                loadList();
            }
        } catch (Exception e) {
            // Show error if something went wrong
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error removing server", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        topJToolBar = new javax.swing.JToolBar();
        addJButton = new javax.swing.JButton();
        editJButton = new javax.swing.JButton();
        removeJButton = new javax.swing.JButton();
        refreshJButton = new javax.swing.JButton();
        filesJScrollPane = new javax.swing.JScrollPane();
        filesJTree = new javax.swing.JTree();
        topJMenuBar = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        aboutJMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        quitJMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        addServerJMenuItem = new javax.swing.JMenuItem();
        editServerJMenuItem = new javax.swing.JMenuItem();
        removeServerJMenuItem = new javax.swing.JMenuItem();
        refreshJMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("WFM Client");

        topJToolBar.setFloatable(false);
        topJToolBar.setRollover(true);
        topJToolBar.setMargin(new java.awt.Insets(5, 5, 5, 5));

        addJButton.setIcon(IconFontSwing.buildIcon(FontAwesomeSolid.PLUS, 32, new Color(173, 173, 173)));
        addJButton.setToolTipText("Add server");
        addJButton.setFocusable(false);
        addJButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        addJButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        addJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addJButtonActionPerformed(evt);
            }
        });
        topJToolBar.add(addJButton);

        editJButton.setIcon(IconFontSwing.buildIcon(FontAwesomeSolid.EDIT, 32, new Color(173, 173, 173)));
        editJButton.setToolTipText("Edit server");
        editJButton.setFocusable(false);
        editJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        topJToolBar.add(editJButton);

        removeJButton.setIcon(IconFontSwing.buildIcon(FontAwesomeSolid.MINUS, 32, new Color(173, 173, 173)));
        removeJButton.setToolTipText("Remove server");
        removeJButton.setFocusable(false);
        removeJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        removeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeJButtonActionPerformed(evt);
            }
        });
        topJToolBar.add(removeJButton);

        refreshJButton.setIcon(IconFontSwing.buildIcon(FontAwesomeSolid.SYNC_ALT, 32, new Color(173, 173, 173)));
        refreshJButton.setToolTipText("Refresh list");
        refreshJButton.setFocusable(false);
        refreshJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshJButtonActionPerformed(evt);
            }
        });
        topJToolBar.add(refreshJButton);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        filesJTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        filesJTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                filesJTreeMouseClicked(evt);
            }
        });
        filesJScrollPane.setViewportView(filesJTree);

        jMenu1.setText("File");

        aboutJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        aboutJMenuItem.setIcon(IconFontSwing.buildIcon(FontAwesomeSolid.INFO_CIRCLE, 16, new Color(173, 173, 173)));
        aboutJMenuItem.setText("About application");
        jMenu1.add(aboutJMenuItem);
        jMenu1.add(jSeparator1);

        quitJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        quitJMenuItem.setIcon(IconFontSwing.buildIcon(FontAwesomeSolid.TIMES_CIRCLE, 16, new Color(173, 173, 173)));
        quitJMenuItem.setText("Quit");
        quitJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitJMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(quitJMenuItem);

        topJMenuBar.add(jMenu1);

        jMenu2.setText("Servers");
        jMenu2.setToolTipText("");

        addServerJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        addServerJMenuItem.setIcon(IconFontSwing.buildIcon(FontAwesomeSolid.PLUS, 16, new Color(173, 173, 173)));
        addServerJMenuItem.setText("Add server");
        addServerJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerJMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(addServerJMenuItem);

        editServerJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        editServerJMenuItem.setIcon(IconFontSwing.buildIcon(FontAwesomeSolid.EDIT, 16, new Color(173, 173, 173)));
        editServerJMenuItem.setText("Edit server");
        jMenu2.add(editServerJMenuItem);

        removeServerJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        removeServerJMenuItem.setIcon(IconFontSwing.buildIcon(FontAwesomeSolid.MINUS, 16, new Color(173, 173, 173)));
        removeServerJMenuItem.setText("Remove server");
        removeServerJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeServerJMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(removeServerJMenuItem);

        refreshJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        refreshJMenuItem.setIcon(IconFontSwing.buildIcon(FontAwesomeSolid.SYNC_ALT, 16, new Color(173, 173, 173)));
        refreshJMenuItem.setText("Refresh list");
        refreshJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshJMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(refreshJMenuItem);

        topJMenuBar.add(jMenu2);

        setJMenuBar(topJMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topJToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filesJScrollPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(topJToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filesJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * On click action for file tree
     * 
     * @param evt Mouse event
     */
    private void filesJTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filesJTreeMouseClicked
        TreePath tp = filesJTree.getPathForLocation(evt.getX(), evt.getY()); // Set tree path from mouse position
        
        if (tp != null) { // Was the mouse click on one of the file tree items?
            if (SwingUtilities.isRightMouseButton(evt)) { // Was it right click?
                // Here will be some right click menu opening code
            } else if (evt.getClickCount() == 2 && !evt.isConsumed()) { // Was ist double click?
                evt.consume();
                
                try { // Try to get file path and download this file
                    // Parse the tree path
                    String path = tp.toString();
                    path = path.replaceAll("\\[", "");
                    path = path.replaceAll("]", "");
                    path = path.replaceAll(", ", "/");

                    if (path.split("/").length > 2) { // Check if the double click was on file (or directory), which belongs to one of the servers (check if the path has more than 2 items)
                        String serverName = path.split("/")[1]; // Get server (which the file or directory belongs to) name

                        // Get server url for this server name from server list
                        String serverUrl = "";
                        for (int i = 0; i < serverList.size(); i++) {
                            String item = serverList.get(i);
                            if (item.split("///")[0].equals(serverName)) {
                                serverUrl = item.split("///")[1];
                                break;
                            }
                        }

                        String filePath = path.substring(path.split("/")[0].length() + path.split("/")[1].length() + 2); // Get file path from tree path
                        
                        if (new ApiParser().isFile(serverUrl, filePath.replaceAll(" ", "%20"))) { // Check if the selected item is file
                            // Get file URL from server url and file path
                            String fileUrl = serverUrl;
                            if (!fileUrl.endsWith("/")) {
                                fileUrl += "/";
                            }
                            fileUrl += filePath;

                            // Define File for downloaded file
                            File downloadedFile = new File(config.getDownloadDir() + File.separator + serverName + File.separator + filePath.replace("/", File.separator));
                            
                            // Create parent directories for downloaded file, if don't exist
                            if (!downloadedFile.getParentFile().exists()) {
                                downloadedFile.getParentFile().mkdirs();
                            }
                            
                            // Create empty file for downloaded file, if doesn't exist
                            if (!downloadedFile.exists()) {
                                downloadedFile.createNewFile();
                            }

                            // Download and open the file
                            new ApiParser().downloadFile(fileUrl.replaceAll(" ", "%20"), downloadedFile.getAbsolutePath());
                            Desktop.getDesktop().open(downloadedFile);
                        }
                    }
                } catch (Exception e) {
                    // Show error if something went wrong
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Error downloading file", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_filesJTreeMouseClicked

    /**
     * On click action for toolbar "add server" button
     * 
     * @param evt Action event
     */
    private void addJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJButtonActionPerformed
        addServer();
    }//GEN-LAST:event_addJButtonActionPerformed

    /**
     * On click action for menu "add server" item
     * 
     * @param evt Action event
     */
    private void addServerJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerJMenuItemActionPerformed
        addServer();
    }//GEN-LAST:event_addServerJMenuItemActionPerformed

    /**
     * On click action for menu "quit" item
     * 
     * @param evt Action event
     */
    private void quitJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitJMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_quitJMenuItemActionPerformed

    /**
     * On click action for toolbar "remove server" button
     * 
     * @param evt Action event
     */
    private void removeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeJButtonActionPerformed
        removeServer();
    }//GEN-LAST:event_removeJButtonActionPerformed

    /**
     * On click action for menu "remove server" item
     * 
     * @param evt Action event
     */
    private void removeServerJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeServerJMenuItemActionPerformed
        removeServer();
    }//GEN-LAST:event_removeServerJMenuItemActionPerformed

    /**
     * On click action for toolbar "refresh" button
     * 
     * @param evt Action event
     */
    private void refreshJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshJButtonActionPerformed
        loadList();
    }//GEN-LAST:event_refreshJButtonActionPerformed

    /**
     * On click action for menu "refresh" item
     * 
     * @param evt Action event
     */
    private void refreshJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshJMenuItemActionPerformed
        loadList();
    }//GEN-LAST:event_refreshJMenuItemActionPerformed

    /**
     * Main method
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and display the form
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FilesJFrame frame = new FilesJFrame();
                frame.setVisible(true);
                frame.initList();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutJMenuItem;
    private javax.swing.JButton addJButton;
    private javax.swing.JMenuItem addServerJMenuItem;
    private javax.swing.JButton editJButton;
    private javax.swing.JMenuItem editServerJMenuItem;
    private javax.swing.JScrollPane filesJScrollPane;
    private javax.swing.JTree filesJTree;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuItem quitJMenuItem;
    private javax.swing.JButton refreshJButton;
    private javax.swing.JMenuItem refreshJMenuItem;
    private javax.swing.JButton removeJButton;
    private javax.swing.JMenuItem removeServerJMenuItem;
    private javax.swing.JMenuBar topJMenuBar;
    private javax.swing.JToolBar topJToolBar;
    // End of variables declaration//GEN-END:variables
}
