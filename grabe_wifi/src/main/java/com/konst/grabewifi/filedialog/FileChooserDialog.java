package com.konst.grabewifi.filedialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.konst.grabewifi.R;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * A file chooser implemented in a Dialog. 
 */
public class FileChooserDialog extends Dialog implements FileChooser {

	// ----- Attributes ----- //
	
	/**
	 * The core of this file chooser.
	 */
	private final FileChooserCore core;
	
	/**
	 * The listeners for the event of select a file.
	 */
	private final List<OnFileSelectedListener> listeners;
	
	// ----- Constructors ----- //
	
	/**
	 * Creates a file chooser dialog which, by default, lists all the files in the SD card.
	 * 
	 * @param context The current context.
	 */
	public FileChooserDialog(Context context) {
		this(context, null);
	}

	/**
	 * Creates a file chooser dialog which lists all the file of a particular folder.
	 * 
	 * @param context The current context.
	 * @param folderPath The folder which files are going to be listed.
	 */
	public FileChooserDialog(Context context, String folderPath) {
		// Call superclass constructor.
		super(context);
        
		// Set layout.
		setContentView(R.layout.daidalos_file_chooser);

		// Maximize the dialog.
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.FILL_PARENT;
		getWindow().setAttributes(lp);
        
        // By default, load the SD card files.
		core = new FileChooserCore(this);
		core.loadFolder(folderPath);
        
        // Initialize attributes.
		listeners = new LinkedList<>();
		
		// Set the background color.
        LinearLayout layout = (LinearLayout) findViewById(R.id.rootLayout);
        layout.setBackgroundColor(context.getResources().getColor(R.color.daidalos_backgroud));
                
        // Add a listener for when a file is selected.
        core.addListener(new FileChooserCore.OnFileSelectedListener() {
			@Override
			public void onFileSelected(File folder, String name) {
				// Call to the listeners.
				for (OnFileSelectedListener listener : listeners) {
					listener.onFileSelected(FileChooserDialog.this, folder, name);
				}
			}

			@Override
			public void onFileSelected(File file) {
				// Call to the listeners.
				for (OnFileSelectedListener listener : listeners) {
					listener.onFileSelected(FileChooserDialog.this, file);
				}
			}
		});

		// Add a listener for when the cancel button is pressed.
		core.addListener(new FileChooserCore.OnCancelListener() {
			@Override
			public void onCancel() {
				// Close activity.
                FileChooserDialog.super.onBackPressed();
			}
		});
	}
	
    // ----- Events methods ----- //

	/**
	 * Add a listener for the event of a file selected.
	 * 
	 * @param listener The listener to add.
	 */
	public void addListener(OnFileSelectedListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes a listener for the event of a file selected.
	 * 
	 * @param listener The listener to remove.
	 */
	public void removeListener(OnFileSelectedListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Removes all the listeners for the event of a file selected.
	 */
	public void removeAllListeners() {
		listeners.clear();
	}
	
	/**
	 * Interface definition for a callback to be invoked when a file is selected. 
	 */
	public interface OnFileSelectedListener {
		/**
		 * Called when a file has been selected.
		 * 
		 * @param file The file selected.
		 */
		void onFileSelected(Dialog source, File file);
		
		/**
		 * Called when an user wants to be create a file.
		 * 
		 * @param folder The file's parent folder.
		 * @param name The file's name.
		 */
		void onFileSelected(Dialog source, File folder, String name);
	}
	
	// ----- Miscellaneous methods ----- //

	/**
	 * Set a regular expression to filter the files that can be selected.
	 * 
	 * @param filter A regular expression.
	 */
	public void setFilter(String filter) {
		core.setFilter(filter);
	}

	/**
	 * Set a regular expression to filter the folders that can be explored.
	 *
	 * @param folderFilter A regular expression.
	 */
	public void setFolderFilter(String folderFilter) {
		core.setFolderFilter(folderFilter);
	}

	/**
	 * Defines if only the files that can be selected (they pass the filter) must be show.
	 * 
	 * @param show 'true' if only the files that can be selected must be show or 'false' if all the files must be show.
	 */
	public void setShowOnlySelectable(boolean show) {
		core.setShowOnlySelectable(show);
	}
	
	/**
	 * Loads all the files of the SD card root.
	 */
	public void loadFolder() {
		core.loadFolder();
	}
	
	/**
	 * Loads all the files of a folder in the file chooser.
	 * 
	 * If no path is specified ('folderPath' is null) the root folder of the SD card is going to be used.
	 * 
	 * @param folderPath The folder's path.
	 */
	public void loadFolder(String folderPath) {
		core.loadFolder(folderPath);
	}
	
	/**
	 * Defines if the chooser is going to be used to select folders, instead of files.
	 * 
	 * @param folderMode 'true' for select folders or 'false' for select files.
	 */
	public void setFolderMode(boolean folderMode) {
		core.setFolderMode(folderMode);
	}
	
	/**
	 * Defines if the user can create files, instead of only select files.
	 * 
	 * @param canCreate 'true' if the user can create files or 'false' if it can only select them.
	 */
	public void setCanCreateFiles(boolean canCreate) {
		core.setCanCreateFiles(canCreate);
	}

	/**
	 * Defines if the cancel button must be show.
	 *
	 * @param canShow 'true' if the user can create files or 'false' if it can only select them.
	 */
	public void setShowCancelButton(boolean canShow) {
		core.setShowCancelButton(canShow);
	}

	/**
	 * Defines the value of the labels.
	 * 
	 * @param labels The labels.
	 */
	public void setLabels(FileChooserLabels labels) {
		core.setLabels(labels);
	}
	
	/**
	 * Allows to define if a confirmation dialog must be show when selecting o creating a file.
	 * 
	 * @param onSelect 'true' for show a confirmation dialog when selecting a file, 'false' if not.
	 * @param onCreate 'true' for show a confirmation dialog when creating a file, 'false' if not.
	 */
	public void setShowConfirmation(boolean onSelect, boolean onCreate) {
		core.setShowConfirmationOnCreate(onCreate);
		core.setShowConfirmationOnSelect(onSelect);
	}
	
	/**
	 * Allows to define if, in the title, must be show only the current folder's name or the full file's path..
	 * 
	 * @param show 'true' for show the full path, 'false' for show only the name.
	 */
	public void setShowFullPath(boolean show) {
		core.setShowFullPathInTitle(show);
	}
	
    // ----- FileChooser methods ----- //
    
	@Override
	public LinearLayout getRootLayout() {
		View root = findViewById(R.id.rootLayout);
		return (root instanceof LinearLayout)? (LinearLayout)root : null;
	}  

	@Override
	public void setCurrentFolderName(String name) {
		setTitle(name);
	}
}
