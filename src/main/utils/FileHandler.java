package main.utils;

import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import main.PnmlToModelConverter;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class FileHandler {
	private final PnmlToModelConverter converter;

	public FileHandler(PnmlToModelConverter converter) {
		this.converter = converter;
	}

	public File showFileChooser(Window ownerWindow) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import Process Model or Log");
		FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All Supported Files", "*.pnml",
				"*.xes");
		FileChooser.ExtensionFilter pnmlFilter = new FileChooser.ExtensionFilter("PNML Files", "*.pnml");
		FileChooser.ExtensionFilter xesFilter = new FileChooser.ExtensionFilter("XES Files", "*.xes");

		fileChooser.getExtensionFilters().addAll(allFilter, pnmlFilter, xesFilter);
		fileChooser.setSelectedExtensionFilter(allFilter);
		
		// Set initial directory to the "inputs" folder within the project
        File inputsDir = getInputsDirectory();
        if (inputsDir != null && inputsDir.exists() && inputsDir.isDirectory()) {
            fileChooser.setInitialDirectory(inputsDir);
        }
        
		return fileChooser.showOpenDialog(ownerWindow);
	}

	public Petrinet loadPnmlFile(File pnmlFile) throws Exception {
		return converter.loadPetriNetFromPnml(pnmlFile);
	}

	public XLog loadXesFile(File xesFile) throws Exception {
		return converter.importXesLog(xesFile);
	}
	
	private File getInputsDirectory() {
        try {
            // Get the current working directory (project root)
            String currentDir = System.getProperty("user.dir");
            File projectDir = new File(currentDir);
            
            // Create the inputs directory path
            File inputsDir = new File(projectDir, "inputs");
            
            // Create the directory if it doesn't exist
            if (!inputsDir.exists()) {
                boolean created = inputsDir.mkdirs();
                if (created) {
                    System.out.println("Created inputs directory: " + inputsDir.getAbsolutePath());
                } else {
                    System.err.println("Failed to create inputs directory");
                    return null;
                }
            }
            
            return inputsDir;
        } catch (Exception e) {
            System.err.println("Error accessing inputs directory: " + e.getMessage());
            return null;
        }
    }
}