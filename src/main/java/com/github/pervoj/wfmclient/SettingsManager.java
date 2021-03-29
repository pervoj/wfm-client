/* SettingsManager.java
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Class for loading configuration
 *
 * @author Vojtěch Perník <pervoj@gmx.com>
 */
public class SettingsManager {
    private File configDir;
    private File serverList;
    private File downloadDir;
    
    /**
     * Settings loader class constructor method
     * 
     * @throws Exception when loading fails
     */
    public SettingsManager() throws Exception {
        // Set configuration directory
        configDir = new File(getAppDataDir());
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // Set server list file
        serverList = new File(configDir.getAbsolutePath() + File.separator + "servers");
        if (!serverList.exists()) {
            serverList.createNewFile();
        }
        
        // Set cofiguration file with directory for downloads path
        downloadDir = new File(configDir.getAbsolutePath() + File.separator + "downloads");
        if (!downloadDir.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(downloadDir), StandardCharsets.UTF_8))) {
                bw.write(System.getProperty("user.home") + File.separator + "wfmclient");
                bw.flush();
            }
        }
        
        // Set directory for downloads
        if (!new File(getDownloadDir()).exists()) {
            new File(getDownloadDir()).mkdirs();
        }
    }
    
    /**
     * Getter for cofiguration directory
     * 
     * @return Configuration directory File
     */
    public File getConfigDir() {
        return configDir;
    }
    
    /**
     * Getter for server list file
     * 
     * @return Server list file File
     */
    public File getServerListFile() {
        return serverList;
    }
    
    /**
     * Getter for cofiguration file with directory for downloads path
     * 
     * @return Cofiguration file with directory for downloads path File
     */
    public File getDownloadDirFile() {
        return downloadDir;
    }
    
    /**
     * Getter for directory for downloads path
     * 
     * @return Directory for downloads path
     */
    public String getDownloadDir() throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(downloadDir), StandardCharsets.UTF_8))) {
            return br.readLine();
        }
    }
    
    /**
     * Getter for AppData directory path for this app
     * 
     * @return AppData directory path for this app
     */
    private String getAppDataDir() {
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            // On MacOS:
            return System.getProperty("user.home") + "/Library/Preferences/wfmclient";
        } else if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            // On Windows:
            return System.getProperty("user.home") + "\\AppData\\Local\\pervoj\\wfmclient";
        } else {
            // On Linux or other Unix system:
            return System.getProperty("user.home") + "/.config/wfmclient";
        }
    }
}
