package examples;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import beast.app.BEASTVersion2;
import beast.app.util.Application;
import beast.core.Description;
import beast.core.Input;
import beast.core.Runnable;
import beast.core.Input.Validate;
import beast.util.LogAnalyser;

@Description("Application to demonstrate how to use a web browser for post processing." +
	 "It creates a simple chart using CanvasJS and uses classic style 'Inputs'. "
	 + "See MiniTracerCanvasJS for a version with modern style 'Inputs'. "
	 + "See MiniTracerPlotly for a version using the plotly library.")
public class MiniTracerClassic extends Runnable {
	public Input<File> traceLogInput = new Input<>("file", "Input file containing a trace log", Validate.REQUIRED);
	public Input<Integer> burninInput = new Input<>("burnin", "percentage of the log file to disregard as burn-in (default 10)", 10);
	public Input<String> traceInput = new Input<>("trace", "name of the parameter/log entry to plot", "posterior");

	private File tracelog;
	private Integer burnin = 10;
	private String trace = "posterior";

	public void initAndValidate() {
		this.tracelog = traceLogInput.get();
		this.burnin = burninInput.get();
		this.trace = traceInput.get();
	}
	
	@Override
	public void run() throws Exception {
		if (burnin < 0) {
			burnin = 0;
		}
		
		LogAnalyser traces = new LogAnalyser(tracelog.getAbsolutePath(), burnin, false, false);
		Double [] data = traces.getTrace(trace);
		Double [] labels = traces.getTrace("Sample");
		
		// create plot.ly chart see http://plot.ly/ for documentation
		String html = "<html>\n" + 
		"<title>BEAST " + new BEASTVersion2().getVersionString() + ": miniTracer</title>\n" +
		"<head>\n" +
		"<script src='plotly-latest.min.js'></script>\n" +
		"</head>\n" +
		"<body>\n" +
		"<h2>Trace of " + trace + " from " + tracelog.getPath() +"</h2>\n" +
		"<div id='chart'/>\n" +
		"<script>\n" +
		"var data = [\n" +
		"  {\n" +
		"    x: " + Arrays.toString(labels)+ ",\n" +
		"    y: " + Arrays.toString(data)+ ",\n" +
		"    type: 'scatter'\n" +
		"  }\n" +
		"];\n" +
		"\n" +
		"Plotly.newPlot('chart', data);\n" +
		"</script>\n" +
		"<p>This is a simple graph with all settings at default. "
		+ "Click the 'edit' button (which appears at the top of the graph when you hover over the graph) "
		+ "to change the colours and other styles. "
		+ "To see how the javascripts changes, click the JSON button in the style editor.</p>\n" +
		"</body>\n" +
		"</html>";
				
		// write html file in package dir + "/js/minitracer.html"
		String jsPath = Application.getPackagePath("BeastApp.addon.jar") + "js";
        FileWriter outfile = new FileWriter(jsPath + "/minitracer.html");
        outfile.write(html);
        outfile.close();
		
        // open html file in browser
        Application.openUrl("file://" + jsPath + "/minitracer.html");
	}
	
	public static void main(String[] args) throws Exception {
		Application app = new Application(new MiniTracerClassic(), "MiniTracer", args);
	}
}
