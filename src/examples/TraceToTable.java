package examples;


import java.io.File;
import java.io.FileWriter;
import java.util.List;

import beast.app.BEASTVersion2;
import beast.app.util.Application;
import beast.core.Description;
import beast.core.Param;
import beast.core.Runnable;
import beast.util.LogAnalyser;
import beast.util.OutputUtils;

@Description("Application to demonstrate how to use a web browser for post processing. "
		+ "Produces a HTML table with trace statistics, like LogCombiner, "
		+ "but this one looks better.")
public class TraceToTable extends Runnable {
	private File tracelog;
	private Integer burnin = 10;
	private String trace = "posterior";

	public TraceToTable() {}

	public TraceToTable(@Param(name="file", description="Input file containing a trace log") File tracelog,
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
		
		LogAnalyser traces = new LogAnalyser(tracelog.getAbsolutePath(), burnin, false, true);

		List<String> labels = traces.getLabels();
		StringBuilder b = new StringBuilder();
		b.append("<table>\n");
        b.append("<tr>" + formatHCell("item") +
        		formatHCell("mean") + formatHCell("stderr")  + formatHCell("stddev")  + formatHCell("median")  + formatHCell("95%HPDlo")  + formatHCell("95%HPDup")  + 
        		formatHCell("ACT")  + formatHCell("ESS")  + formatHCell("geometric-mean") + "</tr>\n");
        
        for (int i = 1; i < labels.size(); i++) {
        	if (i % 2 == 0) {
        		CLASS = " class='alt'";
        	} else {
        		CLASS = "";
        	}
        	b.append("<tr>" + formatHCell(labels.get(i)) +
                    format(traces.getMean(i)) +  format(traces.getStdError(i)) +  format(traces.getStdDev(i)) +
                    format(traces.getMedian(i)) +  format(traces.get95HPDlow(i)) +  format(traces.get95HPDup(i)) +
                    format(traces.getACT(i)) +  format(traces.getESS(i)) +  format(traces.getGeometricMean(i)) + "</tr>\n");

        }
		b.append("</table>\n");

		
		// create CanvasJS chart http://canvasjs.com/
		String html = "<html>\n" + 
		"<title>BEAST " + new BEASTVersion2().getVersionString() + ": miniTracer</title>\n" +
		"<head>  \n" +
		"<link rel='stylesheet' type='text/css' href='css/style.css'>\n" +
		"</head>\n" +
		"<body>\n" +
		"<h2>Trace  from " + tracelog.getPath() +"</h2>\n" +
		b.toString() +
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
	
	String TABLE_CELL = "TH";
	String CLASS = "";
	String formatHCell(String x) {
		return "<TH>" + x + "</TH>";
	}
	String format(double x) {
		return "<TD" + CLASS +">" + OutputUtils.format(x) + "</TD>";
	}

	public static void main(String[] args) throws Exception {
		Application app = new Application(new TraceToTable(), "MiniTracer", args);
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