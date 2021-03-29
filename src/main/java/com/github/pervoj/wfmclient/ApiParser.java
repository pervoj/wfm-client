/* ApiParser.java
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

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class for comunication between application and WFM api on server
 *
 * @author Vojtěch Perník <pervoj@gmx.com>
 */
public class ApiParser {
    /**
     * Method for recursive filling directory tree node with subdirectories and subfiles
     * 
     * @param rootNode Directory tree node which you want to fill
     * @param allFiles Complete array of files
     * @param files Array of subdirectories and files which belongs to this directory
     */
    private void addChilds(DefaultMutableTreeNode rootNode, String[] allFiles, String[] files) {
        for (String file : files) { // Iterate file array
            DefaultMutableTreeNode subItem = new DefaultMutableTreeNode(getNameFromPath(file)); // Define subitem tree node
            
            if (isDir(file, allFiles)) { // Has iterated item another subitems?
                addChilds(subItem, allFiles, filterDir(file, allFiles)); // Fill subitem with its subitems
            }
            
            rootNode.add(subItem); // Add subitem to root tree node
        }
    }
    
    /**
     * Returns last part of path - file (directory) name
     * 
     * @param path File (directory) path for which you want to get name
     * @return File (directory) name
     */
    private String getNameFromPath(String path) {
        String[] splitted = path.split("/");
        return splitted[splitted.length - 1];
    }
    
    /**
     * Has this directory (file) another subdirectories (subfiles)?
     * 
     * @param dir Directory
     * @param files Complete array of files
     * @return true - this directory has another subdirectories (subfiles), false - this is file or empty directory
     */
    private boolean isDir(String dir, String[] files) {
        boolean isDir = false; // Set value to false
        
        // Iterate complete file list and check if iterated file belongs to controlled directory
        for (String file : files) { // Iterate complete file list
            if (!file.equals(dir) && file.startsWith(dir)) { // If isn't iterated controlled directory and if iterated file (directory) starts with controlled path
                isDir = true; // Set value to false
                break; // Break cycle
            }
        }
        
        return isDir; // Return value
    }
    
    /**
     * Filter complete file list only to files, which belongs to filtered directory
     * 
     * @param dir Directory to filter
     * @param files Complete file list
     * @return Filtered array of files
     */
    private String[] filterDir(String dir, String[] files) {
        if (dir == null) { // If will be iterated root directory
            ArrayList<String> filelist = new ArrayList<>(); // Define file array list
            
            // Iterate complete file list and fill array list with files, which doesn't belong to any directory
            for (String file : files) {
                if (!file.contains("/")) {
                    filelist.add(file);
                }
            }
            
            // Put array list to array
            String[] filearray = new String[filelist.size()]; 
            for (int i = 0; i < filelist.size(); i++) {
                filearray[i] = filelist.get(i);
            }
            
            return filearray; // Return array
        } else {
            ArrayList<String> filelist = new ArrayList<>(); // Define file array list
            
            String[] splittedDir = dir.split("/"); // Split iterated directory path
            
            for (String file : files) { // Iterate complete file list
                String[] splittedFile = file.split("/"); // Split iterated file path
                
                // If isn't iterated controlled directory, if iterated file belongs to controlled directory and if file belongs directly to controlled directory
                if (!file.equals(dir) && file.startsWith(dir) && (splittedDir.length == (splittedFile.length - 1))) {
                    filelist.add(file); // Add file to file array list
                }
            }
            
            // Put array list to array
            String[] filearray = new String[filelist.size()];
            for (int i = 0; i < filelist.size(); i++) {
                filearray[i] = filelist.get(i);
            }
            
            return filearray; // Return array
        }
    }
    
    /**
     * Returns tree node filled with files and directories for specified WFM server
     * 
     * @param url WFM server URL
     * @param title Title for WFM server
     * @return Tree node filled with files and directories
     * @throws Exception when on specified URL isn't WFM server
     */
    public DefaultMutableTreeNode getFilesNode(String url, String title) throws Exception {
        DefaultMutableTreeNode files = new DefaultMutableTreeNode(title); // Define tree node
        
        if (!getApiContent(url + "?check-api").equals("web-file-manager")) { // Check if API returns "web-file-manager", if not:
            throw new Exception(title + " isn't WFM server!"); // Throw exception
        } else {
            String[] filesArray = getApiContent(url + "?api").split("\n"); // Get file list from WFM URL
            addChilds(files, filesArray, filterDir(null, filesArray)); // Add files to tree node
        }
        
        return files; // Return tree node
    }
    
    /**
     * Returns API URL content
     * 
     * @param url WFM server URL
     * @return Content of API URL
     * @throws Exception when something went wrong
     */
    public String getApiContent(String url) throws Exception {
        URL urlClass = new URL(url); // Define URL class instance
        Scanner sc = new Scanner(urlClass.openStream()); // Retrieve content of API URL
        StringBuffer sb = new StringBuffer(); // Define StringBuffer class instance to hold the result
        
        // Put whole URL content
        while(sc.hasNext()) {
            sb.append(sc.next() + " ");
        }
        
        String result = sb.toString(); // Get content from string buffer
        
        // Crop content around API content and remove HTML tags
        result = result.split("<div id=\"wfm-api\">")[1];
        result = result.split("</div>")[0];
        result = result.trim();
        result = result.replaceAll("<br>", "\n");
        result = result.replaceAll("<[^>]*>", "");
        
        return result; // Return content
    }
    
    /**
     * Check if file (directory) on server is file
     * 
     * @param url WFM server URL
     * @param path File path
     * @return true - it is file, false - it isn't file
     * @throws Exception when something went wrong
     */
    public boolean isFile(String url, String path) throws Exception {
        return getApiContent(url + "?api-type=" + path).equals("file");
    }
    
    /**
     * Download file from URL to specified path
     * 
     * @param url URL of file on server
     * @param path Path to downloaded file
     * @throws Exception when something went wrong
     */
    public void downloadFile(String url, String path) throws Exception {
        // Define variables
        ReadableByteChannel readableByteChannel = null;
        FileOutputStream fileOutputStream = null;
        FileChannel fileChannel = null;
        
        // Try download file
        try {
            readableByteChannel = Channels.newChannel(new URL(url).openStream());
            fileOutputStream = new FileOutputStream(path);
            fileChannel = fileOutputStream.getChannel();
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } finally {
            // Close all
            if (readableByteChannel != null) readableByteChannel.close();
            if (fileOutputStream != null) fileOutputStream.close();
            if (fileChannel != null) fileChannel.close();
        }
    }
}
