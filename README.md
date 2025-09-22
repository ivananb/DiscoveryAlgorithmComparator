# Discovery Algorithm Comparator

A comprehensive JavaFX application for process mining, enabling visualization, analysis, and discovery of process models from Petri nets and event logs with advanced algorithm support and Murata reduction techniques.

---

## **Features**

- **Model Visualization**: Render Petri nets as interactive graphs
- **Log Generation**: Generate event logs from Petri net models
- **Process Discovery**: Mine process models from event logs using Heuristics Miner, Inductive Miner, Evolutionary Tree Miner, and Split Miner
- **Comparison**: Compare original and discovered models side by side
- **Murata Reduction**: Advanced silent transition removal using Murata reduction techniques for cleaner model visualization
- **Statistics**: Detailed before/after comparison of model complexity and reduction effectiveness
- **Parameter Customization**: Fine-tune mining and log generation with algorithm-specific parameter settings
- **Comprehensive File Support**: Import/export standard process mining files (.pnml, .xes)

---

## **Project Structure**

```plaintext
src
└── main
    ├── java
    │   └── main
    │       ├── MiningApp.java                       # Main application entry point
    │       ├── PnmlToModelConverter.java            # Converts PNML files to Petri net models
    │       │
    │       ├── algorithms                           # Process mining algorithm implementations
    │       │   ├── MiningAlgorithm.java             # Abstract base class for all algorithms
    │       │   ├── MiningAlgorithmSelector.java     # Factory for instantiating algorithms
    │       │   ├── EvolutionaryTreeMinerAlgorithm.java
    │       │   ├── HeuristicMinerAlgorithm.java
    │       │   ├── InductiveMinerAlgorithm.java
    │       │   └── SplitMinerAlgorithm.java
    │       │
    │       ├── controller                           # JavaFX controllers for UI logic
    │       │   ├── LogExplorerController.java       # Manages the log explorer view
    │       │   ├── MiningController.java            # Main window controller
    │       │   ├── ParametersSettingsController.java # Handles generic parameter settings
    │       │   └── StatisticsController.java        # Manages algorithm comparison and statistics
    │       │
    │       ├── controller/settings                  # Controllers for algorithm-specific settings panels
    │       │   ├── ETMParameterStorage.java         # Data storage for Evolutionary Tree Miner
    │       │   ├── EvolutionaryTreeMinerSettingsController.java
    │       │   ├── HeuristicMinerSettingsController.java
    │       │   ├── InductiveMinerSettingsController.java
    │       │   └── SplitMinerSettingsController.java
    │       │
    │       ├── utils                                # Utility and helper classes
    │       │   ├── ExportController.java            # Handles exporting results (XES, PNML, TXT)
    │       │   ├── FileHandler.java                 # Manages file input/output operations
    │       │   ├── MurataReduction.java             # Implements silent transition removal
    │       │   ├── Utils.java                       # Common utility methods and helpers
    │       │   └── VisualisationController.java     # Controls model visualization
    │       │
    │       └── visualisation
    │           └── PetriNetVisualiser.java          # Handles the rendering of Petri nets
    │
    └── resources
        ├── css                                      # Stylesheets for application styling
        │   ├── log-explorer.css
        │   ├── main.css
        │   ├── parameters-settings.css
        │   └── statistics.css
        │
        ├── view                                     # FXML files defining the user interface
        │   ├── logExplorer.fxml                     # Layout for the log explorer
        │   ├── main.fxml                            # Main application window layout
        │   ├── parameters-settings.fxml             # Layout for the settings dialog
        │   └── statistics.fxml                      # Layout for the statistics comparison window
        │
        └── view/settings                            # FXML files for algorithm-specific parameters
            ├── evolutionary-tree-miner-settings.fxml
            ├── heuristic-miner-settings.fxml
            ├── inductive-miner-settings.fxml
            └── split-miner-settings.fxml
```

---

## **User Interface Overview**
The application features a modern, intuitive interface with three main views:

### Main Application View (main.fxml)
- Dual Model Visualization: Side-by-side display of original and discovered models
- Interactive Controls: Real-time parameter adjustment for log generation and mining
- Algorithm Selection: Dropdown menu with all supported mining algorithms
- Murata Reduction Toggle: Checkbox for enabling/disabling silent transition removal
- Export Options: Multiple format export capabilities (XES, PNML, TXT)
- Status Indicators

### Log Explorer View (logExplorer.fxml)
- Interactive list of all traces in the event log
- DComprehensive display of individual trace contents
- Save trace information in readable text format

### Algorithm Settings View (parameters-settings.fxml)
- Extensive parameter customization with sliders and checkboxes
- Live value display for all adjustable parameters
- Save and reset to default configurations

---


### Statistical Reporting
The Murata reduction provides comprehensive statistics including:
- Structural Comparison: Places, transitions, arcs, and silent transitions before/after reduction
- Reduction Summary: Quantitative analysis of components removed
- Percentage Analysis: Relative reduction effectiveness for each component type
- Final Comparison: Original model vs reduced model structural equivalence
 
---

## **Requirements**

- Java 11
- Apache IvyDE (for dependency management)
- Grandle (for Split Miner algorithm functionalitiess)
- An IDE with IvyDE plugin (Eclipse) recommended

---

## **Installation**

### 1. Clone the repository

```bash
git clone https://github.com/ivananb/DiscoveryAlgorithmComparator.git
cd DiscoveryAlgorithmComparator
```
### 2. Build the application (For running Jar)
If you want to build the JAR yourself from the source code, ensure you have Apache Ant and Ivy installed, then run:
```bash
    ant main
```
or
```bash
    ant uber-jar
```
This will create the executable JAR file in the /target directory.

### 3. Run the application:
You can run the application in two ways:
- From your IDE: Run the MiningApp.java class as a Java Application
- Using the pre-built JAR file (Command Line): Navigate to the project directory and run the following command
```bash
    java -jar target/DiscoveryAlgorithmComparator.jar
```

---

## **Usage**

### **1. Loading Models and Logs**
- Import PNML File or XES Log: Click "Import File" and select a .pnml or .xes file
- The original Petri net will be visualized in the "Original Model" panel

### **2. Generating Event Logs**
- Set parameters in the control panel (if not selected the system will use dafault values):
   - Number of Traces: Quantity of traces to generate (default: 100)
   - Max Trace Length: Maximum length of each trace (default: 50)
- Click "Generate Event Log" to create synthetic logs using stochastic simulation

### **3. Process Model Mining**
- Select your desired mining algorithm from the dropdown menu
- Configure algorithm-specific parameters using the "Settings" button
- Click "Mine New Model" to discover process models from event logs
- The discovered process model appears in the "Discovered Model" panel

### **4. Murata Reduction**
- Toggle the "Remove Silent Transitions" checkbox to enable Murata reduction
- View the simplified model in the Discovered Model panel

### **5. Algorithm Performance Comparison**
- Click "Show Statistics" after generating a log to open the comparison window.
- Configure metric weights (Fitness, Precision, etc.) to influence the scoring, or keep the default values
- Toggle "Remove Silent Transitions" if you want to simplify models before evaluation
- Click "Run Algorithm Comparison" to execute all algorithms and automatically determine the best one based on a weighted overall score.
    Results are displayed in a detailed table with the winner highlighted

### **6. Exporting Results**
- **Export as XES**: Save generated event logs in standard XES format
- **Export as PNML**: Save discovered process models as PNML files
- **Export as TXT**: Save log information in plain text format from the Log Explorer

### **Navigation Controls**

- **Zoom**: Mouse wheel.
- **Pan**: Middle-click drag or Ctrl + Left-click drag.
- **Reset View**: Right-click and select **"Reset View"**.

---

## **Supported Algorithms**

- Heuristics Miner
- Inductive Miner
- Evolutionary Tree Miner
- Split Miner

---

## **Keyboard Shortcuts**

- **Ctrl + Mouse Wheel**: Zoom in/out.
- **Middle-click + Drag**: Pan.
- **Ctrl + Left-click + Drag**: Alternative pan.
---

## **Acknowledgments**

- **ProM Framework**: Process mining infrastructure and algorithms
- **Viz.js**: Graphviz implementation for JavaScript rendering
- **OpenXES**: XES standard implementation for event logs
- **Murata Reduction Techniques**: For advanced Petri net simplification methods
- **ProM Reduction Plugin**: For the Murata reduction algorithm implementation