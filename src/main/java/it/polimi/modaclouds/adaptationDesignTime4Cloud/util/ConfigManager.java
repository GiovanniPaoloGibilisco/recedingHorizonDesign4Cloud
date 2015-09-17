package it.polimi.modaclouds.adaptationDesignTime4Cloud.util;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class ConfigManager {

	public static Path getPathToFile(String filePath) {
		File f = new File(filePath);
		if (f.exists())
			try {
				return f.toPath();
			} catch (Exception e) { }
		
		URL url = ConfigManager.class.getResource(filePath);
		if (url == null)
			url = ConfigManager.class.getResource("/" + filePath);
		if (url == null)
			return null;
		else
			return Paths.get(url.getPath());
	}

}
