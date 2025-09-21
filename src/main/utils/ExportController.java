package main.utils;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.pnml.exporting.PnmlExportNetToPNML;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ExportController {

	public static Petrinet discoveredPnmlFile;

	public static void exportLogAsXes(XLog log, File pnmlFile, String algorithmName, Window ownerWindow)
			throws Exception {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Event Log as XES");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XES Files", "*.xes"));
		fileChooser.setInitialFileName(getSuggestedFileName(pnmlFile, null, "xes"));

		File file = fileChooser.showSaveDialog(ownerWindow);
		if (file != null) {
			File outputFile = file.getName().toLowerCase().endsWith(".xes") ? file
					: new File(file.getAbsolutePath() + ".xes");
			XFactory factory = XFactoryRegistry.instance().currentDefault();
			XesXmlSerializer serializer = new XesXmlSerializer();
			serializer.serialize(log, new FileOutputStream(outputFile));
		}
	}

	public static void exportLogAsTxt(XLog log, File pnmlFile, String algorithmName, Window ownerWindow) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Event Log as Text");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
		fileChooser.setInitialFileName(getSuggestedFileName(pnmlFile, null, "txt"));

		File file = fileChooser.showSaveDialog(ownerWindow);

		if (file != null) {
			File outputFile = file.getName().toLowerCase().endsWith(".txt") ? file
					: new File(file.getAbsolutePath() + ".txt");

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
				writer.write(String.format("Event Log - Generated from %s - based on original file %s\n",
						algorithmName != null ? algorithmName : "Unknown Algorithm",
						pnmlFile != null ? pnmlFile.getName() : "Unknown"));

				writer.write("==========================================\n\n");

				int traceNumber = 1;
				for (XTrace trace : log) {
					writer.write(String.format("Trace %d (%d events):\n", traceNumber, trace.size()));

					int eventNumber = 1;
					for (XEvent event : trace) {
						String eventName = event.getAttributes().get("concept:name").toString();
						writer.write(String.format("  %d. %s\n", eventNumber, eventName));
						eventNumber++;
					}

					writer.write("\n");
					traceNumber++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void exportPnml(Petrinet net, File originalFile, String algorithm, Window window, boolean isReduced) throws Exception {
	    FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Save Discovered Model");

	    // Set initial file name based on original file or algorithm
	    String baseName = originalFile != null ? originalFile.getName().replaceFirst("[.][^.]+$", "")
	            : "discovered_model";

	    // Use getAlgorithmCode to get the shortened version
	    String algorithmCode = getAlgorithmCode(algorithm);
	    
	    // Add "_reduced" suffix if it's a reduced model
	    String reducedSuffix = isReduced ? "_reduced" : "";
	    fileChooser.setInitialFileName(baseName + "_" + algorithmCode + reducedSuffix + ".pnml");
	    
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNML Files", "*.pnml"));

	    File file = fileChooser.showSaveDialog(window);
	    if (file != null) {
	        File outputFile = file.getName().toLowerCase().endsWith(".pnml") ? file
	                : new File(file.getAbsolutePath() + ".pnml");
	        PluginContext context = new UIContext().getMainPluginContext();
	        PnmlExportNetToPNML exporter = new PnmlExportNetToPNML();
	        exporter.exportPetriNetToPNMLFile(context, net, outputFile);

	        formatPnmlFile(outputFile);
	    }
	}

	private static void formatPnmlFile(File file) throws Exception {
		String xml = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		xml = xml.replaceAll("><", ">\n<");
		Files.write(file.toPath(), xml.getBytes(StandardCharsets.UTF_8));
	}

	public static String getSuggestedFileName(File originalFile, String algorithmName, String fileType) {
		String baseName = "process";

		// Use original filename if available
		if (originalFile != null) {
			baseName = originalFile.getName().replace(".pnml", "").replace(".xes", "").replace(".txt", "");
		}

		// Only add algorithm identifier for non-XES files 
		if (algorithmName != null && !algorithmName.isEmpty() && !"xes".equalsIgnoreCase(fileType)) {
			String algorithmCode = getAlgorithmCode(algorithmName);
			baseName += "_" + algorithmCode;
		}

		// Add file type suffix
		return baseName + "." + fileType.toLowerCase();
	}

	public static String getAlgorithmCode(String algorithmName) {
		if (algorithmName == null)
			return "unknown";

		String normalized = algorithmName.trim().toLowerCase();

		switch (normalized) {
		case "evolutionary tree miner":
			return "etm";
		case "inductive miner":
			return "im";
		case "split miner":
			return "sm";
		case "heuristics miner":
			return "hm";
		default:
			return "unknown-algorithm";
		}
	}

}
