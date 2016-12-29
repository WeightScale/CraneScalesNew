package com.konst.grabewifi.filedialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.konst.grabewifi.R;
import com.konst.grabewifi.filedialog.view.FileItem;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the common features of a file chooser.
 */
class FileChooserCore {

	// ----- Attributes ----- //

	/**
	 * The file chooser in which all the operations are performed.
	 */
	private FileChooser chooser;

	/**
	 * The listeners for the event of select a file.
	 */
	private List<OnFileSelectedListener> fileSelectedListeners;

	/**
	 * The listeners for the event of select a file.
	 */
	private List<OnCancelListener> cancelListeners;

	/**
	 * A regular expression for filter the files.
	 */
	private String filter;

	/**
	 * A regular expression for filter the folders.
	 */
	private String folderFilter;

	/**
	 * A boolean indicating if only the files that can be selected (they pass the filter) must be show.
	 */
	private boolean showOnlySelectable;

	/**
	 * A boolean indicating if the user can create files.
	 */
	private boolean canCreateFiles;

	/**
	 * A boolean indicating if the chooser is going to be used to select folders.
	 */
	private boolean folderMode;

	/**
	 * A boolean indicating if the chooser is going to be used to select folders.
	 */
	private boolean showCancelButton;

	/**
	 * A file that indicates the folder that is currently being displayed.
	 */
	private File currentFolder;

	/**
	 * This attribut allows to override the default value of the labels.
	 */
	private FileChooserLabels labels;

	/**
	 * A boolean that indicates if a confirmation dialog must be displaying when selecting a file.
	 */
	private boolean showConfirmationOnSelect;

	/**
	 * A boolean that indicates if a confirmation dialog must be displaying when creating a file.
	 */
	private boolean showConfirmationOnCreate;

	/**
	 * A boolean indicating if the folder's full path must be show in the title.
	 */
	private boolean showFullPathInTitle;

	// ---- Static attributes ----- //

	/**
	 * Static attribute for save the folder displayed by default.
	 */
	private static File defaultFolder;

	/*
	  Static constructor.
	 */
	static {
		defaultFolder = null;
	}

	// ----- Constructor ----- //

	/**
	 * Creates an instance of this class.
	 *
	 * @param fileChooser The graphical file chooser.
	 */
	public FileChooserCore(FileChooser fileChooser) {
		// Initialize attributes.
		chooser = fileChooser;
		fileSelectedListeners = new LinkedList<>();
		cancelListeners = new LinkedList<>();
		filter = null;
		folderFilter = null;
		showOnlySelectable = false;
		setCanCreateFiles(false);
		setFolderMode(false);
		currentFolder = null;
		labels = null;
		showConfirmationOnCreate = false;
		showConfirmationOnSelect = false;
		showFullPathInTitle = false;
		showCancelButton = false;

		// Add listener for the buttons.
		LinearLayout root = chooser.getRootLayout();
		Button addButton = (Button) root.findViewById(R.id.buttonAdd);
		addButton.setOnClickListener(addButtonClickListener);
		Button okButton = (Button) root.findViewById(R.id.buttonOk);
		okButton.setOnClickListener(okButtonClickListener);
		Button cancelButton = (Button) root.findViewById(R.id.buttonCancel);
		cancelButton.setOnClickListener(cancelButtonClickListener);
	}

	// ----- Events methods ----- //

	/**
	 * Implementation of the click listener for when the add button is clicked.
	 */
	private final View.OnClickListener addButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// Get the current context.
			Context context = v.getContext();

			// Create an alert dialog.
			AlertDialog.Builder alert = new AlertDialog.Builder(context);

			// Define the dialog's labels.
			String title = context.getString(folderMode ? R.string.daidalos_create_folder : R.string.daidalos_create_file);
			if(labels != null && labels.createFileDialogTitle != null) title = labels.createFileDialogTitle;
			String message = context.getString(folderMode ? R.string.daidalos_enter_folder_name : R.string.daidalos_enter_file_name);
			if(labels != null && labels.createFileDialogMessage != null) message = labels.createFileDialogMessage;
			String posButton = (labels != null && labels.createFileDialogAcceptButton != null)? labels.createFileDialogAcceptButton : context.getString(R.string.daidalos_accept);
			String negButton = (labels != null && labels.createFileDialogCancelButton != null)? labels.createFileDialogCancelButton : context.getString(R.string.daidalos_cancel);

			// Set the title and the message.
			alert.setTitle( title );
			alert.setMessage( message );

			// Set an EditText view to get the file's name.
			final EditText input = new EditText(context);
			input.setSingleLine();
			alert.setView(input);

			// Set the 'ok' and 'cancel' buttons.
			alert.setPositiveButton(posButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					String fileName = input.getText().toString();
					// Verify if a value has been entered.
					if(fileName != null && !fileName.isEmpty()) {
						// Notify the listeners.
						notifyFileListeners(currentFolder, fileName);
					}
				}
			});
			alert.setNegativeButton(negButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing, automatically the dialog is going to be closed.
				}
			});

			// Show the dialog.
			alert.show();
		}
	};

	/**
	 * Implementation of the click listener for when the ok button is clicked.
	 */
	private final View.OnClickListener okButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// Notify the listeners.
			notifyFileListeners(currentFolder, null);
		}
	};

	/**
	 * Implementation of the click listener for when the cancel button is clicked.
	 */
	private final View.OnClickListener cancelButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// Notify the listeners.
			notifyCancelListeners();
		}
	};

	/**
	 * Implementation of the click listener for when a file item is clicked.
	 */
	private final FileItem.OnFileClickListener fileItemClickListener = new FileItem.OnFileClickListener() {
		@Override
		public void onClick(FileItem source) {
			// Verify if the item is a folder.
			File file = source.getFile();
			if(file.isDirectory()) {
				// Open the folder.
				loadFolder(file);
			} else {
				// Notify the listeners.
				notifyFileListeners(file, null);
			}
		}
	};

	/**
	 * Add a listener for the event of a file selected.
	 *
	 * @param listener The listener to add.
	 */
	public void addListener(OnFileSelectedListener listener) {
		fileSelectedListeners.add(listener);
	}

	/**
	 * Removes a listener for the event of a file selected.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeListener(OnFileSelectedListener listener) {
		fileSelectedListeners.remove(listener);
	}

	/**
	 * Add a listener for the event of a file selected.
	 *
	 * @param listener The listener to add.
	 */
	public void addListener(OnCancelListener listener) {
		cancelListeners.add(listener);
	}

	/**
	 * Removes a listener for the event of a file selected.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeListener(OnCancelListener listener) {
		cancelListeners.remove(listener);
	}

	/**
	 * Removes all the listeners for the event of a file selected.
	 */
	public void removeAllListeners() {
		fileSelectedListeners.clear();
		cancelListeners.clear();
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
		void onFileSelected(File file);

		/**
		 * Called when an user wants to be create a file.
		 *
		 * @param folder The file's parent folder.
		 * @param name The file's name.
		 */
		void onFileSelected(File folder, String name);
	}

	/**
	 * Interface definition for a callback to be invoked when the cancel button is clicked.
	 */
	public interface OnCancelListener {
		/**
		 * Called when the cancel button is clicked.
		 */
		void onCancel();
	}

	/**
	 * Notify to all listeners that the cancel button has been pressed.
	 */
	private void notifyCancelListeners() {
		for (OnCancelListener cancelListener : cancelListeners) {
			cancelListener.onCancel();
		}
	}

	/**
	 * Notify to all listeners that a file has been selected or created.
	 *
	 * @param file The file or folder selected or the folder in which the file must be created.
	 * @param name The name of the file that must be created or 'null' if a file was selected (instead of being created).
	 */
	private void notifyFileListeners(final File file, final String name) {
		// Determine if a file has been selected or created.
		final boolean creation = name != null && !name.isEmpty();

		// Verify if a confirmation dialog must be show.
		if((creation && showConfirmationOnCreate || !creation && showConfirmationOnSelect)) {
			// Create an alert dialog.
			Context context = chooser.getContext();
			AlertDialog.Builder alert = new AlertDialog.Builder(context);

			// Define the dialog's labels.
			String message;
			if(labels != null && ((creation && labels.messageConfirmCreation != null) || (!creation && labels.messageConfirmSelection != null)))  {
				message = creation? labels.messageConfirmCreation : labels.messageConfirmSelection;
			} else {
				if(folderMode) {
					message = context.getString(creation? R.string.daidalos_confirm_create_folder : R.string.daidalos_confirm_select_folder);
				} else {
					message = context.getString(creation? R.string.daidalos_confirm_create_file : R.string.daidalos_confirm_select_file);
				}
			}
			if(message != null) message = message.replace("$file_name", name!=null? name : file.getName());
			String posButton = (labels != null && labels.labelConfirmYesButton != null)? labels.labelConfirmYesButton : context.getString(R.string.daidalos_yes);
			String negButton = (labels != null && labels.labelConfirmNoButton != null)? labels.labelConfirmNoButton : context.getString(R.string.daidalos_no);

			// Set the message and the 'yes' and 'no' buttons.
			alert.setMessage( message );
			alert.setPositiveButton(posButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// Notify to listeners.
					for (OnFileSelectedListener fileSelectedListener : fileSelectedListeners) {
						if (creation) {
							fileSelectedListener.onFileSelected(file, name);
						} else {
							fileSelectedListener.onFileSelected(file);
						}
					}
				}
			});
			alert.setNegativeButton(negButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing, automatically the dialog is going to be closed.
				}
			});

			// Show the dialog.
			alert.show();
		} else {
			// Notify to listeners.
			for (OnFileSelectedListener fileSelectedListener : fileSelectedListeners) {
				if (creation) {
					fileSelectedListener.onFileSelected(file, name);
				} else {
					fileSelectedListener.onFileSelected(file);
				}
			}
		}
	}

	// ----- Get and set methods ----- //

	/**
	 * Allows to define if a confirmation dialog must be show when selecting a file.
	 *
	 * @param show 'true' for show the confirmation dialog, 'false' for not show the dialog.
	 */
	public void setShowConfirmationOnSelect(boolean show) {
		showConfirmationOnSelect = show;
	}

	/**
	 * Allows to define if a confirmation dialog must be show when creating a file.
	 *
	 * @param show 'true' for show the confirmation dialog, 'false' for not show the dialog.
	 */
	public void setShowConfirmationOnCreate(boolean show) {
		showConfirmationOnCreate = show;
	}

	/**
	 * Allows to define if, in the title, must be show only the current folder's name or the full file's path..
	 *
	 * @param show 'true' for show the full path, 'false' for show only the name.
	 */
	public void setShowFullPathInTitle(boolean show) {
		showFullPathInTitle = show;
	}

	/**
	 * Defines the value of the labels.
	 *
	 * @param labels The labels.
	 */
	public void setLabels(FileChooserLabels labels) {
		this.labels = labels;

		// Verify if the buttons for add a file or select a folder has been modified.
		if(labels != null) {
			LinearLayout root = chooser.getRootLayout();

			if(labels.labelAddButton != null) {
				Button addButton = (Button) root.findViewById(R.id.buttonAdd);
				addButton.setText(labels.labelAddButton);
			}

			if(labels.labelSelectButton != null) {
				Button okButton = (Button) root.findViewById(R.id.buttonOk);
				okButton.setText(labels.labelSelectButton);
			}

			if(labels.labelCancelButton != null) {
				Button cancelButton = (Button) root.findViewById(R.id.buttonCancel);
				cancelButton.setText(labels.labelCancelButton);
			}
		}
	}

	/**
	 * Set a regular expression to filter the files that can be selected.
	 *
	 * @param filter A regular expression.
	 */
	public void setFilter(String filter) {
		if(filter == null || filter.isEmpty()) {
			this.filter = null;
		} else {
			this.filter = filter;
		}

		// Reload the list of files.
		loadFolder(currentFolder);
	}

	/**
	 * Set a regular expression to filter the folders that can be explored.
	 *
	 * @param folderFilter A regular expression.
	 */
	public void setFolderFilter(String folderFilter) {
		if(folderFilter == null || folderFilter.isEmpty()) {
			this.folderFilter = null;
		} else {
			this.folderFilter = folderFilter;
		}

		// Reload the list of files.
		loadFolder(currentFolder);
	}

	/**
	 * Defines if the chooser is going to be used to select folders, instead of files.
	 *
	 * @param folderMode 'true' for select folders or 'false' for select files.
	 */
	public void setFolderMode(boolean folderMode) {
		this.folderMode = folderMode;

		// Show or hide the 'Ok' button.
		updateButtonsLayout();

		// Reload the list of files.
		loadFolder(currentFolder);
	}

	/**
	 * Defines if the chooser is going to be used to select folders, instead of files.
	 *
	 * @param showCancelButton 'true' for show the cancel button or 'false' for not showing it.
	 */
	public void setShowCancelButton(boolean showCancelButton) {
		this.showCancelButton = showCancelButton;

		// Show or hide the 'Cancel' button.
		updateButtonsLayout();
	}

	/**
	 * Defines if the user can create files, instead of only select files.
	 *
	 * @param canCreate 'true' if the user can create files or 'false' if it can only select them.
	 */
	public void setCanCreateFiles(boolean canCreate) {
		canCreateFiles = canCreate;

		// Show or hide the 'Add' button.
		updateButtonsLayout();
	}

	/**
	 * Defines if only the files that can be selected (they pass the filter) must be show.
	 *
	 * @param show 'true' if only the files that can be selected must be show or 'false' if all the files must be show.
	 */
	public void setShowOnlySelectable(boolean show) {
		showOnlySelectable = show;

		// Reload the list of files.
		loadFolder(currentFolder);
	}

	/**
	 * Returns the current folder.
	 *
	 * @return The current folder.
	 */
	public File getCurrentFolder() {
		return currentFolder;
	}

	// ----- Miscellaneous methods ----- //

	/**
	 * Changes the height of the layout for the buttons, according if the buttons are visible or not. 
	 */
	private void updateButtonsLayout() {
		// Get the buttons layout.
		LinearLayout root = chooser.getRootLayout();

		// Verify if the 'Add' button is visible or not.
		View addButton = root.findViewById(R.id.buttonAdd);
		addButton.setVisibility(canCreateFiles ? View.VISIBLE : View.GONE);

		// Verify if the 'Ok' button is visible or not.
		View okButton = root.findViewById(R.id.buttonOk);
		okButton.setVisibility(folderMode ? View.VISIBLE : View.GONE);

		// Verify if the 'Cancel' button is visible or not.
		View cancelButton = root.findViewById(R.id.buttonCancel);
		cancelButton.setVisibility(showCancelButton ? View.VISIBLE : View.GONE);
	}

	/**
	 * Loads all the files of the SD card root.
	 */
	public void loadFolder() {
		loadFolder(defaultFolder);
	}

	/**
	 * Loads all the files of a folder in the file chooser.
	 *
	 * If no path is specified ('folderPath' is null) the root folder of the SD card is going to be used.
	 *
	 * @param folderPath The folder's path.
	 */
	public void loadFolder(String folderPath) {
		// Get the file path.
		File path = null;
		if(folderPath != null && !folderPath.isEmpty()) {
			path = new File(folderPath);
		}

		loadFolder(path);
	}

	/**
	 * Loads all the files of a folder in the file chooser.
	 *
	 * If no path is specified ('folder' is null) the root folder of the SD card is going to be used.
	 *
	 * @param folder The folder.
	 */
	public void loadFolder(File folder) {
		// Remove previous files.
		LinearLayout root = chooser.getRootLayout();
		LinearLayout layout = (LinearLayout) root.findViewById(R.id.linearLayoutFiles);
		layout.removeAllViews();

		// Get the file path.
		if(folder == null || !folder.exists()) {
			if(defaultFolder != null) {
				currentFolder = defaultFolder;
			} else {
				currentFolder = Environment.getExternalStorageDirectory();
			}
		} else {
			currentFolder = folder;
		}

		// Verify if the path exists.
		if(currentFolder.exists()) {
			List<FileItem> fileItems = new LinkedList<>();

			// Add the parent folder.
			if(currentFolder.getParent() != null) {
				File parent = new File(currentFolder.getParent());
				if(parent.exists()) {
					FileItem parentFolder = new FileItem(chooser.getContext(), parent, "..");
					parentFolder.setSelectable(folderFilter == null || parent.getAbsolutePath().matches(folderFilter));
					fileItems.add(parentFolder);
				}
			}

			// Verify if the file is a directory.
			if(currentFolder.isDirectory()) {
				// Get the folder's files.
				File[] fileList = currentFolder.listFiles();
				if(fileList != null) {
					// Order the files alphabetically and separating folders from files.
					Arrays.sort(fileList, new Comparator<File>() {
						@Override
						public int compare(File file1, File file2) {
							if(file1 != null && file2 != null) {
								if(file1.isDirectory() && (!file2.isDirectory())) return -1;
								if(file2.isDirectory() && (!file1.isDirectory())) return 1;
								return file1.getName().compareTo(file2.getName());
							}
							return 0;
						}
					});

					// Iterate all the files in the folder.
					for (File aFileList : fileList) {
						// Verify if file can be selected.
						boolean selectable = true;
						if (!aFileList.isDirectory()) {
							// File is selectable as long the user is not selecting folders and if pass the filter (if defined).
							selectable = !folderMode && (filter == null || aFileList.getName().matches(filter));
						} else {
							// Folders can be selected iif pass the filter (if defined).
							selectable = folderFilter == null || aFileList.getAbsolutePath().matches(folderFilter);
						}

						// Verify if the file must be show.
						if (selectable || !showOnlySelectable) {
							// Create the file item and add it to the list.
							FileItem fileItem = new FileItem(chooser.getContext(), aFileList);
							fileItem.setSelectable(selectable);
							fileItems.add(fileItem);
						}
					}
				}

				// Set the name of the current folder.
				String currentFolderName = showFullPathInTitle ? currentFolder.getPath() : currentFolder.getName();
				chooser.setCurrentFolderName(currentFolderName);
			} else {
				// The file is not a folder, add only this file.
				fileItems.add(new FileItem(chooser.getContext(), currentFolder));
			}


			// Add click listener and add the FileItem objects to the layout.
			for (FileItem fileItem : fileItems) {
				fileItem.addListener(fileItemClickListener);
				layout.addView(fileItem);
			}

			// Refresh default folder.
			defaultFolder = currentFolder;
		}
	}
}
