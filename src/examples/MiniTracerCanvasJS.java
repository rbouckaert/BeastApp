package examples;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import beast.app.BEASTVersion2;
import beast.app.util.Application;
import beast.core.Description;
import beast.core.Param;
import beast.core.Runnable;
import beast.util.LogAnalyser;

@Description("Application to demonstrate how to use a web browser for post processing. "
		+ "It creates a simple chart using CanvasJS and uses new style 'Inputs'."
		+ "See MiniTracerClassic for a version with classic style 'Inputs'. "
		+ "See MiniTracerPlotly for a version using the plotly library.")
public class MiniTracerCanvasJS extends Runnable {
	private File tracelog;
	private Integer burnin = 10;
	private String trace = "posterior";

	public MiniTracerCanvasJS() {}

	public MiniTracerCanvasJS(@Param(name="file", description="Input file containing a trace log") File tracelog,
			@Param(name="burnin", description="percentage of the log file to disregard as burn-in (default 10)", defaultValue="10") Integer burnin,
			@Param(name="trace", description="name of the parameter/log entry to plot", defaultValue = "posterior") String trace) {
		this.tracelog = tracelog;
		this.burnin = burnin;
		this.trace = trace;
	}
	
	@Override
	public void initAndValidate() {
	}

	@Override
	public void run() throws Exception {
		if (burnin < 0) {
			burnin = 0;
		}
		
		LogAnalyser traces = new LogAnalyser(tracelog.getAbsolutePath(), burnin, false, false);
		Double [] data = traces.getTrace(trace);
		Double [] labels = traces.getTrace("Sample");
		
		// create CanvasJS chart http://canvasjs.com/
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
		String jsPath = Application.getPackagePath("BeastFX.addon.jar") + "js";
        FileWriter outfile = new FileWriter(jsPath + "/minitracer.html");
        outfile.write(html);
        outfile.close();
		
        // open html file in browser
        Application.openUrl("file://" + jsPath + "/minitracer.html");
	}

	public static void main(String[] args) throws Exception {
		Application app = new Application(new MiniTracerCanvasJS(), "MiniTracer", args);
	}

	/** obligatory getters and setters to coincide with @Param definitions **/
	public File getFile() {
		return tracelog;
	}

	public void setFile(File tracelog) {
		this.tracelog = tracelog;
	}

	public Integer getBurnin() {
		return burnin;
	}

	public void setBurnin(Integer burnin) {
		this.burnin = burnin;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}
}