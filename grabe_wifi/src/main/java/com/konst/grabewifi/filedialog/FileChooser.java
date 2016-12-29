package com.konst.grabewifi.filedialog;

import android.content.Context;
import android.widget.LinearLayout;

/**
 * This interface defines all the methods that a file chooser must implement, in order to being able to make use of the class FileChooserUtils.
 */
interface FileChooser {

	/**
	 * Gets the root of the layout 'file_chooser.xml'.
	 * 
	 * @return A linear layout.
	 */
	LinearLayout getRootLayout();
	
	/**
	 * Set the name of the current folder.
	 * 
	 * @param name The current folder's name.
	 */
	void setCurrentFolderName(String name);
	
	/**
	 * Returns the current context of the file chooser.
	 * 
	 * @return The current context.
	 */
	Context getContext();
}
