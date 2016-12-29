package com.konst.grabewifi.filedialog.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.konst.grabewifi.R;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to represents the files that can be selected by the user. 
 */
public class FileItem extends LinearLayout {

	// ----- Attributes ----- //
	
	/**
	 * The file which is represented by this item.
	 */
	private File file;
	
	/**
	 * The image in which show the file's icon.
	 */
	private final ImageView icon;
	
	/**
	 * The label in which show the file's name.
	 */
	private final TextView label;
	
	/**
	 * A boolean indicating if the item can be selected.
	 */
	private boolean selectable;
	
	/**
	 * The listeners for the click event.
	 */
	private final List<OnFileClickListener> listeners;

	// ----- Constructor ----- //

	/**
	 * The class main constructor.
	 *
	 * @param context The application's context.
	 */
	public FileItem(Context context) {
		super(context);

		// Define the layout.
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.daidalos_file_item, this, true);

		// Initialize attributes.
        file = null;
        selectable = true;
        icon = (ImageView) findViewById(R.id.imageViewIcon);
        label = (TextView) findViewById(R.id.textViewLabel);
        listeners = new LinkedList<>();

		// Add a listener for the click event.
        setOnClickListener(clickListener);
	}

	/**
	 * A class constructor.
	 *
	 * @param context The application's context.
	 * @param file The file represented by this item
	 */
	public FileItem(Context context, File file) {
		this(context);

		// Set the file.
        setFile(file);
	}

	/**
	 * A class constructor.
	 *
	 * @param context The application's context.
	 * @param file The file represented by this item.
	 * @param label The label of this item.
	 */
	public FileItem(Context context, File file, String label) {
		this(context, file);

		// Set the label.
        setLabel(label);
	}

	// ----- Get() and Set() methods ----- //

	/**
	 * Defines the file represented by this item.
	 *
	 * @param file A file.
	 */
	public void setFile(File file) {
		if(file != null) {
			this.file = file;

			// Replace the label by the file's name.
            setLabel(file.getName());

			// Change the icon, depending if the file is a folder or not.
            updateIcon();
		}
	}

	/**
	 * Returns the file represented by this item.
	 *
	 * @return A file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Changes the label of this item, which by default is the file's name.
	 *
	 * This method must be called after invoking the method setFile(), otherwise
	 * the label is going to be overwritten with the file's name.
	 *
	 * @param label A string value.
	 */
	public void setLabel(String label) {
		// Verify if 'label' is not null.
		if(label == null) label = "";

		// Change the label.
		this.label.setText(label);
	}

	/**
	 * Verifies if the item can be selected.
	 *
	 * @return 'true' if the item can be selected, 'false' if not.
	 */
	public boolean isSelectable() {
		return selectable;
	}

	/**
	 * Defines if the item can be selected or not.
	 *
	 * @param selectable 'true' if the item can be selected, 'false' if not.
	 */
	public void setSelectable(boolean selectable) {
		// Save the value.
		this.selectable = selectable;

		// Update the icon.
        updateIcon();
	}

	// ----- Miscellaneous methods ----- //

	/**
	 * Updates the icon according to if the file is a folder and if it can be selected.
	 */
	private void updateIcon() {
		// Define the icon.
		int icon;
		if(file != null && file.isDirectory()) {
			icon = selectable ? R.drawable.ic_folder : R.drawable.folder_gray;
		} else {
			icon = selectable ? R.drawable.ic_file : R.drawable.document_gray;
		}

		// Set the icon.
		this.icon.setImageDrawable(getResources().getDrawable( icon ));

		// Change the color of the text.
		if(icon != R.drawable.document_gray && icon != R.drawable.folder_gray) {
            label.setTextColor(getResources().getColor(R.color.background_item_list));
		} else {
            label.setTextColor(getResources().getColor(R.color.daidalos_inactive_file));
		}
	}

	// ----- Events ----- //

	/**
	 * Listener for the click event.
	 */
	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// Verify if the item can be selected.
			if(selectable) {
				// Call the listeners.
				for (OnFileClickListener listener : listeners) {
					listener.onClick(FileItem.this);
				}
			}
		}
	};

	/**
	 * Add a listener for the click event.
	 *
	 * @param listener The listener to add.
	 */
	public void addListener(FileItem.OnFileClickListener listener) {
        listeners.add(listener);
	}

	/**
	 * Removes a listener for the click event.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeListener(FileItem.OnFileClickListener listener) {
        listeners.remove(listener);
	}
	
	/**
	 * Removes all the listeners for the click event.
	 */
	public void removeAllListeners() {
        listeners.clear();
	}
	
	/**
	 * Interface definition for a callback to be invoked when a FileItem is clicked. 
	 */
	public interface OnFileClickListener {
		/**
		 * Called when a FileItem has been clicked.
		 * 
		 * @param source The source of the event.
		 */
		void onClick(FileItem source);
	}
}
