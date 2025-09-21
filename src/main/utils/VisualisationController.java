package main.utils;

import javafx.concurrent.Worker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.web.WebView;
import main.visualisation.PetriNetVisualiser;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** Controller for managing WebView visualisations of Petri net models
 */
public class VisualisationController {
	private static final String HTML_PATH = "/visPage.html";
	private final Map<WebView, Boolean> webViewInitialized = new HashMap<>();
	
	/** Initialize the given WebView for displaying Petri net models
	 * @param webView
	 */
	public void initializeWebView(WebView webView) {
		if (webView == null)
			return;

		if (webViewInitialized.containsKey(webView))
			return;
		webViewInitialized.put(webView, false);

		URL url = getClass().getResource(HTML_PATH);
		if (url == null) {
			System.err.println("Could not find HTML file: " + HTML_PATH);
			return;
		}

		// Enable JavaScript console logging
		webView.getEngine().setOnAlert(event -> {
			System.out.println("JS Alert: " + event.getData());
		});

		// Add mouse event handlers for panning
		setupMousePanning(webView);

		// Load the HTML content
		webView.getEngine().load(url.toExternalForm());

		// Add listener to handle when content is loaded
		webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				System.out.println("WebView loaded successfully");
				webViewInitialized.put(webView, true);
			}
		});
	}
	
	/** Set up mouse event handlers for panning the visualisation
	 * @param webView
	 */
	private void setupMousePanning(WebView webView) {
		// Disable native context menu
		webView.setContextMenuEnabled(false);

		// Variables to track drag state
		final double[] dragStartX = { 0 };
		final double[] dragStartY = { 0 };
		final boolean[] isDragging = { false };

		// Mouse pressed handler - start drag
		webView.setOnMousePressed(event -> {
			if (event.isMiddleButtonDown() || (event.isPrimaryButtonDown() && event.isControlDown())) {
				dragStartX[0] = event.getX();
				dragStartY[0] = event.getY();
				isDragging[0] = true;
				webView.setCursor(javafx.scene.Cursor.MOVE);
			}
		});

		// Mouse dragged handler - pan the view
		webView.setOnMouseDragged(event -> {
			if (isDragging[0]) {
				double deltaX = event.getX() - dragStartX[0];
				double deltaY = event.getY() - dragStartY[0];

				// Execute JavaScript to pan the visualization
				webView.getEngine().executeScript(String.format("panVisualization(%f, %f)", deltaX, deltaY));

				dragStartX[0] = event.getX();
				dragStartY[0] = event.getY();
			}
		});

		// Mouse released handler - end drag
		webView.setOnMouseReleased(event -> {
			isDragging[0] = false;
			webView.setCursor(javafx.scene.Cursor.DEFAULT);
		});

		webView.setOnScroll(event -> {
			if (event.isControlDown()) {
				double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
				webView.getEngine().executeScript(String.format("transform.k *= %f; applyTransform();", zoomFactor));
				event.consume();
			}
		});
	}
	
	/** Display a Petri net model in the given WebView
	 * @param webView The WebView to display the model in
	 * @param petriNet The Petri net model to display
	 * @param title Optional title for the model
	 */
	public void displayModelInWebView(WebView webView, Petrinet petriNet, String title) {
		if (webView == null || petriNet == null) {
			System.err.println("WebView or Petrinet is null");
			return;
		}

		String dotString = PetriNetVisualiser.getVisualisationString(petriNet, title);
		if (dotString == null || dotString.isEmpty()) {
			System.err.println("Empty DOT string generated");
			return;
		}

		// Escape single quotes for JavaScript
		String escapedDotString = dotString.replace("'", "\\'");
		String script = "setModel('" + escapedDotString + "')";

		// Execute the script when the WebView is ready
		if (webViewInitialized.getOrDefault(webView, false)) {
			webView.getEngine().executeScript(script);
		} else {
			webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
				if (newState == Worker.State.SUCCEEDED) {
					webView.getEngine().executeScript(script);
				}
			});
		}
	}
	
	/** Set up context menus for the WebView
	 * @param webView
	 * @param webViewType
	 */
	public void setupWebViewContextMenus(WebView webView, String webViewType) {
		ContextMenu contextMenu = new ContextMenu();

		// Common menu items
		MenuItem zoomIn = new MenuItem("Zoom In (Mouse Wheel)");
		MenuItem zoomOut = new MenuItem("Zoom Out (Mouse Wheel)");
		MenuItem resetView = new MenuItem("Reset View");
		MenuItem panInfo = new MenuItem("Pan: Middle-click drag or Ctrl+Left-click drag");

		zoomIn.setOnAction(e -> webView.getEngine().executeScript("transform.k *= 1.2; applyTransform();"));
		zoomOut.setOnAction(e -> webView.getEngine().executeScript("transform.k *= 0.8; applyTransform();"));
		resetView.setOnAction(e -> webView.getEngine().executeScript("centerView()"));

		// Add items to menu
		contextMenu.getItems().addAll(zoomIn, zoomOut, resetView, new SeparatorMenuItem(), panInfo);

		// Set context menu
		webView.setOnContextMenuRequested(event -> {
			contextMenu.show(webView, event.getScreenX(), event.getY());
			event.consume();
		});
	}
	
	/** Clear the current model from the WebView
	 * @param webView
	 */
	public void clearWebView(WebView webView) {
	    try {
	        webView.getEngine().executeScript("if (typeof clearModel === 'function') clearModel()");
	    } catch (Exception e) {
	        // Fallback: load empty content
	        webView.getEngine().loadContent("<html><body></body></html>");
	    }
	}

}