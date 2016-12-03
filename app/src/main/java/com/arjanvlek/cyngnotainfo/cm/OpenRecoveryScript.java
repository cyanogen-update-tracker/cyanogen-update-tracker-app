/*
 * Copyright (C) 2014 Jared Rummler <jared@jrummyapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arjanvlek.cyngnotainfo.cm;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import eu.chainfire.libsuperuser.Shell;


/**
 * <p><a href="http://www.teamw.in/OpenRecoveryScript">OpenRecoveryScript</a> is an open scripting 
 * engine that any recovery and any app can use to as an API between app and recovery.  
 * This engine allows an app to tell a recovery to install zips, perform backups, etc.</p>
 * 
 * @author Jared Rummler
 *
 */
public class OpenRecoveryScript {

	private static final String SCRIPT_NAME = "openrecoveryscript";

	private static final File OPENRECOVERY_SCRIPT_FILE = new File("/cache/recovery", SCRIPT_NAME);

	private Builder builder;

	public OpenRecoveryScript(Builder builder) {
		this.builder = builder;
	}

	/**
	 * Reboot into recovery mode and run the script created from the {@link Builder}
	 * @return {@code true} if successfully rebooted into recovery mode.
	 */
	public boolean run() {
		return builder.run();
	}

	/**
	 * Class to build the commands for TeamWin Recovery's openrecoveryscript.
	 */
	public static class Builder {

		private StringBuilder mCommands;

		private File mScript;

		/**
		 * Build a custom script to run in TeamWin recovery.
		 * @param context a {@link Context}
		 */
		public Builder(Context context) {
			mCommands = new StringBuilder();
			mScript = new File(context.getFilesDir(), SCRIPT_NAME);
		}

		/**
		 * {@code wipe partition}
		 * @param partition the partition to wipe
		 * @return
		 */
		public Builder wipe(String partition) {
			mCommands.append("wipe " + partition);
			mCommands.append('\n');
			return this;
		}

		/**
		 * {@code wipe cache}
		 * @return
		 */
		public Builder wipe_cache() {
			wipe("cache");
			return this;
		}

		/**
		 * {@code wipe data}
		 * @return
		 */
		public Builder wipe_data() {
			wipe("data");
			return this;
		}

		/**
		 * {@code wipe dalvik}
		 * @return
		 */
		public Builder wipe_dalvik() {
			wipe("dalvik");
			return this;
		}

		/**
		 * {@code install path}
		 * @param zip the path to a ZIP file to flash
		 * @return
		 */
		public Builder install(String zip) {
			mCommands.append("install " + zip);
			mCommands.append('\n');
			return this;
		}

		/**
		 * {@code backup [SDCRBAOM] foldername}
		 * @param foldername fully qualified with /sdcard/TWRP etc.
		 * @param system {@code true} to backup the system partition
		 * @param data {@code true} to backup the data partition
		 * @param cache {@code true} to backup the cache partition
		 * @param recovery {@code true} to backup the recovery partition
		 * @param boot {@code true} to backup the boot partition
		 * @param androidSecure {@code true} to backup android secure
		 * @param useCompression {@code true} to use compression on the backup (takes longer)
		 * @param createMD5 {@code true} to create an md5sum of the backups
		 * @return
		 */
		public Builder backup(String foldername, boolean system, boolean data, 
				boolean cache, boolean recovery, boolean boot, boolean androidSecure, 
				boolean useCompression, boolean createMD5) {
			mCommands.append("backup ");
			if (system) mCommands.append("S");
			if (data) mCommands.append("D");
			if (cache) mCommands.append("C");
			if (recovery) mCommands.append("R");
			if (boot) mCommands.append("B");
			if (androidSecure) mCommands.append("A");
			if (useCompression) mCommands.append("O");
			if (createMD5) mCommands.append("M");
			if (TextUtils.isEmpty(foldername)) {
				mCommands.append(" " + foldername);
			}
			mCommands.append('\n');
			return this;
		}

		/**
		 * Backup system, data, and boot partition. Use compression. Do not create MD5.<br>
		 * {@code backup SDBOM foldername}
		 * @param foldername fully qualified with /sdcard/TWRP etc.
		 * @return
		 */
		public Builder backup(String foldername) {
			mCommands.append("backup SDBOM");
			if (foldername != null) {
				mCommands.append(" " + foldername);
			}
			mCommands.append('\n');
			return this;
		}

		/**
		 * Backup system, data, and boot partition. Use compression. Do not create MD5.<br>
		 * {@code backup SDBOM}
		 * @return
		 */
		public Builder backup() {
			return backup(null);
		}

		/**
		 * {@code restore foldername [SDCRBA]}
		 * @param foldername fully qualified with /sdcard/TWRP etc.
		 * @param system {@code true} to restore the system partition
		 * @param data {@code true} to restore the data partition
		 * @param cache {@code true} to restore the cache partition
		 * @param recovery {@code true} to restore the recovery partition
		 * @param boot {@code true} to restore the boot partition
		 * @param androidSecure {@code true} to restore android secure
		 * @return
		 */
		public Builder restore(String foldername, boolean system, boolean data, boolean cache, boolean recovery,
				boolean boot, boolean androidSecure) {
			mCommands.append("restore ");
			if (foldername != null) mCommands.append(foldername + " ");
			if (system) mCommands.append("S");
			if (data) mCommands.append("D");
			if (cache) mCommands.append("C");
			if (recovery) mCommands.append("R");
			if (boot) mCommands.append("B");
			if (androidSecure) mCommands.append("A");
			mCommands.append('\n');
			return this;
		}

		/**
		 * {@code mount path}
		 * @param path partition to mount
		 * @return
		 */
		public Builder mount(String path) {
			mCommands.append("mount " + path);
			mCommands.append('\n');
			return this;
		}

		/**
		 * {@code unmount path}
		 * @param path partition path
		 * @return
		 */
		public Builder unmount(String path) {
			mCommands.append("unmount " + path);
			mCommands.append('\n');
			return this;
		}

		/**
		 * {@code mkdir dir}
		 * @param dir path to directory
		 * @return
		 */
		public Builder mkdir(String dir) {
			mCommands.append("mkdir " + dir);
			mCommands.append('\n');
			return this;
		}

		/**
		 * Run a shell command.</p>
		 * 
		 * Example:</p>
		 * 
		 * {@code cmd("chmod 755 /system/xbin/busybox")}
		 * <br>would append<br>
		 * {@code cmd chmod 755 /system/xbin/busybox}
		 * <br>to the openrecoveryscript
		 * @param cmd
		 * @return
		 */
		public Builder cmd(String cmd) {
			mCommands.append("cmd " + cmd);
			mCommands.append('\n');
			return this;
		}

		/**
		 * {@code set variable value}
		 * @param variable TWRP variable
		 * @param value value to set the variable to
		 * @return
		 */
		public Builder set(String variable, String value) {
			mCommands.append("set " + variable + " " + value);
			mCommands.append('\n');
			return this;
		}

		protected boolean run() {
            try {
                writeToFile(mScript, mCommands.toString());
                makeDirectory("/cache/recovery");

                copy(mScript, OPENRECOVERY_SCRIPT_FILE);
                mScript.delete();
                return runAsRoot("reboot recovery");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

		}
	}

    public static void writeToFile(File file, String contents) throws IOException {
        OutputStream outputStream = new FileOutputStream(file);

        outputStream.write(contents.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    public static boolean makeDirectory(String dirname) {
        File file = new File(dirname);
        return file.mkdirs();
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }


    protected static boolean runAsRoot(String command) {
        if(Shell.SU.available()) {
            Shell.SU.run(command);
            return true;
        } else {
            return false;
        }
    }
}