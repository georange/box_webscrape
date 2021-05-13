package com.sheret.box.scraper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxResource;
import com.box.sdk.BoxItem.Info;

public class BoxScraper {
	
	// determines where the output folder will go 
	private static String start = "/Users/a9953/Desktop/";
	
	// determines whether the output folder will have nested folders or not
	private static final boolean noFolders = true;

	private static Logger logger = LoggerFactory.getLogger(BoxScraper.class);
	private static final String DEVELOPER_TOKEN = "Hk69ECl6kTt6z2ufgkN4oy3VYPNlzObz";

	public static void main(String[] args) {
		BoxAPIConnection api = new BoxAPIConnection(DEVELOPER_TOKEN);
		Info shareInfo = BoxFolder.getSharedItem(api, "https://americanstandard.box.com/v/GROHEUSLibrary");

		// if all files are to be dumped into a single directory, start it here
		if (noFolders) {
			String itemName = shareInfo.getName();	
			start = start + itemName + "/";
			new File(start).mkdirs();
		}
	
		scrape (shareInfo, start);
		System.out.println("Done.");
	}
	
	@SuppressWarnings("unused")
	private static void scrape(BoxItem.Info boxInfo, String path) {
		String itemName = boxInfo.getName();		 
		BoxResource boxResource = boxInfo.getResource();
		
		// if item is a folder that's NOT called Low Res or Lifestyle, go deeper
		if (boxResource instanceof BoxFolder) {
			File dir;
			if (noFolders == false) {
				path = path + itemName + "/";
				new File(path).mkdirs();
				dir = new File(path);
			}
			
			if ((itemName != null) && (itemName.equalsIgnoreCase("Low Res") == false) 
					&& (itemName.equalsIgnoreCase("LowRes") == false)
					&& (itemName.equalsIgnoreCase("Lifestyle") == false)) {
					
				BoxFolder resourceFolder = (BoxFolder)boxResource;
				Iterable<BoxItem.Info> children = resourceFolder.getChildren();
				Iterator<BoxItem.Info> childrenIt = children.iterator();
				
				while (childrenIt.hasNext()) {
					scrape(childrenIt.next(), path);
				}
			}
			if (noFolders == false) {
				// if folder is empty at end, delete it to reduce clutter
				if (dir.isDirectory()) {
					if (dir.list().length == 0) {
						dir.delete();
						System.out.println("Directory is deleted : " + dir.getAbsolutePath());
					}
				}	
			}	
			
			// if item is a file with extension tif or pdf, save file
		} else if (boxResource instanceof BoxFile) {
			File temp = new File(path + itemName + "/");
			if (temp.exists() == false) {
				String itemExt = getExtension(itemName);
				if (itemExt.equals("tif") || itemExt.equals("pdf") || itemExt.equals("tiff")) {
					String itemId = boxInfo.getID();
					BoxFile file = (BoxFile) boxResource;
					FileOutputStream stream;

					System.out.println("Downloading to... " + path + itemName + "   ID... " + itemId);

					try {
						stream = new FileOutputStream(path + itemName + "/");
						file.download(stream);
						stream.close();
					} catch (Exception e) {
						logger.debug("An error has occured: " + e.toString());
						e.printStackTrace();
					}
				}
			}
		}
		return;
	}
	
	// little private method to return the extension of a filename
	private static String getExtension(String filename) {
		return filename.substring(filename.indexOf(".")+1);
	}

}
