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
		
		String html = "<html>\n" + 
		"<title>BEAST " + new BEASTVersion2().getVersionString() + ": miniTracer</title>\n" +
		"<head>  \n" +
		"  <script type=\"text/javascript\">\n" +
		"  window.onload = function () {\n" +
		"\n" +
		"    var chart = new CanvasJS.Chart(\"chartContainer\",\n" +
		"    {\n" +
		"      zoomEnabled: true, \n" +
		"      title:{\n" +
		"        text: \"Trace of " + trace + " from " + tracelog.getPath() +"\"\n" +
		"      },\n" +
		"      axisY:{\n" +
		"        includeZero: false\n" +
		"      },\n" +
		"      data: data  // data from below\n" +
		"   });\n" +
		"\n" +
		"    chart.render();\n" +
		"  }\n" +
		"       \n" +
		"    \n" +
		"    var x= " + Arrays.toString(labels)+ " \n" +
		"    var y= " + Arrays.toString(data)+ " \n" +
		"    var data = []; var dataSeries = { type: \"line\" };\n" +
		"    var dataPoints = [];\n" +
		"    for (var i = 0; i < x.length; i += 1) {\n" +
		"         dataPoints.push({\n" +
		"          x: x[i],\n" +
		"          y: y[i]\n" +
		"           });\n" +
		"        }\n" +
		"     dataSeries.dataPoints = dataPoints;\n" +
		"     data.push(dataSeries);               \n" +
		"  \n" +
		"  </script>\n" +
		"  <script type=\"text/javascript\" src=\"canvasjs.min.js\"></script></head>\n" +
		"<body>\n" +
		"    <div id=\"chartContainer\" style=\"height: 90%; width: 100%;\">\n" +
		"    </div>\n" +
		"<p>Try dragging your mouse across the chart to zoom inside charts.</p>\n" + 
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
