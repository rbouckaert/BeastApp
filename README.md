BeastApp is a BEAST 2 packages (available from http://beast2.org, source: https://github.com/CompEvol/beast2/) and requires the latest version of the BEASTLabs package (https://github.com/BEAST2-Dev/BEASTLabs/).

It contains a few examples on how to write BEAST post processing applications

The package contains the following examples:
* src/examples/MiniTracerCanvasJS uses CanvasJS to output a trace
* src/examples/MiniTracerClassic as MiniTracerCanvasJS, but with classic style Inputs
* src/examples/MiniTracerClassic uses Plolty to output a trace
* src/examples/TraceToTable outputs trace stats in a table
* src/beast/tools/app/EBSPAnalyserApp contains a version of EBSPAnalyser that outputs through plotly

The js directory contains the CanvasJS and Plotly charting libraries, as well as a style file (in js/css) for making tables look presentable.
