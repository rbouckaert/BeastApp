package beast.app.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import beast.app.BEASTVersion2;
import beast.app.util.Application;
import beast.core.Input;
import beast.core.util.Log;
import beast.evolution.tree.coalescent.CompoundPopulationFunction;
import beast.evolution.tree.coalescent.CompoundPopulationFunction.Type;
import beast.math.statistic.DiscreteStatistics;
import beast.util.HeapSort;



public class EBSPAnalyserApp extends beast.core.Runnable {
	public Input<File> fileInput = new Input<>("file", "file containing EBSP log");
	public Input<Integer> burninInput = new Input<>("burnin", "percentage of the log file to disregard as burn-in (default 10)", 10);
	public Input<CompoundPopulationFunction.Type> typeInput = new Input<>("type", "population function type. Should match the one used to generate the EBSP log file", CompoundPopulationFunction.Type.STEPWISE, CompoundPopulationFunction.Type.values());
	
	@Override
	public void initAndValidate() {
	}

	@Override
    public void run() throws Exception {

		String fileName = fileInput.get().getPath();
		int burnInPercentage = burninInput.get(); 
		CompoundPopulationFunction.Type type = typeInput.get(); 

		logln("Processing " + fileName);
        BufferedReader fin = new BufferedReader(new FileReader(fileName));
        String str;
        int data = 0;
        // first, sweep through the log file to determine size of the log
        while (fin.ready()) {
            str = fin.readLine();
            // terrible hackish code, must improve later
            if( str.charAt(0) == '#' ) {
                int i = str.indexOf("spec=");
                if( i > 0 ) {
                   if( str.indexOf("type=\"stepwise\"") > 0 ) {
                      type = Type.STEPWISE;
                   }  else if( str.indexOf("type=\"linear\"") > 0 ) {
                      type = Type.LINEAR;
                   }
                }
            }
            if (str.indexOf('#') < 0 && str.matches(".*[0-9a-zA-Z].*")) {
                data++;
            }
        }
        final int burnIn = data * burnInPercentage / 100;
        logln(" skipping " + burnIn + " line\n\n");
        data = -burnIn - 1;
        fin.close();
        fin = new BufferedReader(new FileReader(fileName));

        // process log
        final List<List<Double>> times = new ArrayList<>();
        final List<List<Double>> popSizes = new ArrayList<>();
        double[] alltimes = null;
        while (fin.ready()) {
            str = fin.readLine();
            if (str.indexOf('#') < 0 && str.matches(".*[0-9a-zA-Z].*")) {
                if (++data > 0) {
                    final String[] strs = str.split("\t");
                    final List<Double> times2 = new ArrayList<>();
                    final List<Double> popSizes2 = new ArrayList<>();
                    if (alltimes == null) {
                        alltimes = new double[strs.length - 1];
                    }
                    for (int i = 1; i < strs.length; i++) {
                        final String[] strs2 = strs[i].split(":");
                        final Double time = Double.parseDouble(strs2[0]);
                        alltimes[i - 1] += time;
                        if (strs2.length > 1) {
                            times2.add(time);
                            popSizes2.add(Double.parseDouble(strs2[1]));
                        }
                    }
                    times.add(times2);
                    popSizes.add(popSizes2);

                }
            }
        }

        if (alltimes == null) {
            //burn-in too large?
            return;
        }

        // take average of coalescent times
        for (int i = 0; i < alltimes.length; i++) {
            alltimes[i] /= times.size();
        }

        // generate output
        //out.println("time\tmean\tmedian\t95HPD lower\t95HPD upper");
        final double[] popSizeAtTimeT = new double[times.size()];
        int[] indices = new int[times.size()];

        
        List<Double> t = new ArrayList<>();
        List<Double> upper = new ArrayList<>();
        List<Double> m = new ArrayList<>();
        List<Double> lower = new ArrayList<>();
        for (final double time : alltimes) {
            for (int j = 0; j < popSizeAtTimeT.length; j++) {
                popSizeAtTimeT[j] = calcPopSize(type, times.get(j), popSizes.get(j), time);
            }

            HeapSort.sort(popSizeAtTimeT, indices);

            t.add(time);

            //out.print(DiscreteStatistics.mean(popSizeAtTimeT) + "\t");
            m.add(DiscreteStatistics.median(popSizeAtTimeT));

            double[] hpdInterval = DiscreteStatistics.HPDInterval(0.95, popSizeAtTimeT, indices);
            lower.add(hpdInterval[0]);
            upper.add(hpdInterval[1]);
        }
        
        
        
        String html = "<html>\n" +
        		"<title>BEAST " + new BEASTVersion2().getVersionString() + ": EBSPAnalyser</title>\n" +
        		"<head>\n" +
        		"<script src='plotly-latest.min.js'></script>\n" +
        		"</head>\n" +
        		"<body>\n" +
        		"<h2>EBSP Demographic reconstruction</h2>\n" +
        		"<div id='chart'/>\n" +
        		"<script>\n" +
        		"\n" +
        		"xaxis = " + t.toString() +";\n" +
        		"\n" +
        		"var trace1 = {\n" +
        		"  x: xaxis,\n" +
        		"  y: " + upper.toString() + ",\n" +
        		"  type: 'scatter',\n" +
        		"    fill:'tonexty',\n" +
        		"    fillcolor:'rgb(255, 153, 0)',\n" +
        		"    name:'95% HPD up'\n" +
        		"};\n" +
        		"\n" +
        		"var traceM = {\n" +
        		"  x: xaxis,\n" +
        		"  y: " + m.toString() + ",\n" +
        		"  type: 'scatter',\n" +
        		"    fill:'tonexty',\n" +
        		"    fillcolor:'rgba(255, 0, 0)',\n" +
        		"    name:'Median'\n" +
        		"\n" +
        		"};\n" +
        		"\n" +
        		"var trace2 = {\n" +
        		"  x: xaxis,\n" +
        		"  y: " + lower.toString() + ",\n" +
        		"  type: 'scatter',\n" +
        		"      fill:'tonexty',\n" +
        		"    mode:'lines+markers',\n" +
        		"    fillcolor:'rgba(255, 255, 255, 0.5)',\n" +
        		"    name:'95% HPD low'\n" +
        		"\n" +
        		"};\n" +
        		"\n" +
        		"var data = [trace2, traceM, trace1];\n" +
        		"\n" +
        		"var layout = {\n" +
        		"  xaxis: {\n" +
        		"//    type: 'log',\n" +
        		"    autorange: true\n" +
        		"  },\n" +
        		"  yaxis: {\n" +
        		"    type: 'log',\n" +
        		"    autorange: true\n" +
        		"  }\n" +
        		"};\n" +
        		"\n" +
        		"Plotly.newPlot('chart', data, layout);\n" +
        		"</script>\n" +
        		"<p>This is a simple graph with all settings at default. Click the 'edit' button (which appears at the top of the graph when you hover over the graph) to change the colours and other styles. To see how the javascripts changes, click the JSON button in the style editor.</p>\n" +
        		"</body>\n" +
        		"</html>";
        
		// write html file in package dir + "/js/minitracer.html"
		String jsPath = Application.getPackagePath("BeastApp.addon.jar") + "js";
		String EBSPFile = jsPath + "/EBSP.html";
        FileWriter outfile = new FileWriter(EBSPFile);
        outfile.write(html);
        outfile.close();
		
        // open html file in browser
        Application.openUrl("file://" + EBSPFile);
    }
    
    private void logln(String string) {
		Log.info.println(string);
	}

	private double calcPopSize(CompoundPopulationFunction.Type type, List<Double> xs, List<Double> ys, double d) {
        // TODO completely untested
        // assume linear
        //assert typeName.equals("Linear");

        final int n = xs.size();
        final double xn = xs.get(n - 1);
        if (d >= xn) {
            return ys.get(n - 1);
        }
        assert d >= xs.get(0);

        int i = 1;
        while (d >= xs.get(i)) {
            ++i;
        }
        // d < xs.get(i)

        double x0 = xs.get(i-1);
        double x1 = xs.get(i);
        double y0 = ys.get(i-1);
        double y1 = ys.get(i);
        assert x0 <= d && d <= x1 : "" + x0 + "," + x1 + "," + d;
        switch (type) {
            case LINEAR:
                final double p = (d * (y1 - y0) + (y0 * x1 - y1 * x0)) / (x1 - x0);
                assert p > 0;
                return p;
            case STEPWISE:
                assert y1 > 0;
                return y1;
        }
        return 0;
    }

    
    public static void main(String[] args) throws Exception {
		new Application(new EBSPAnalyserApp(), "EBSPAnalyse", args);
	}
}
