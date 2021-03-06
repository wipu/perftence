package org.fluentjava.perfence.graph.afreechart;

import org.afree.data.xy.XYSeries;
import org.afree.data.xy.XYSeriesCollection;
import org.afree.graphics.PaintType;
import org.fluentjava.perftence.graph.DatasetAdapter;

class XYSeriesAdapterForScatterPlot implements DatasetAdapter<ScatterPlotGraphData, PaintType> {

    private final XYSeries series;
    private final String yAxisTitle;
    private final static XYSeriesFactory SERIES_FACTORY = new XYSeriesFactory();

    XYSeriesAdapterForScatterPlot(final String legendTitle, final String yAxisTitle) {
        this.yAxisTitle = yAxisTitle;
        this.series = SERIES_FACTORY.newXYSeries(legendTitle);
    }

    private XYSeries series() {
        return this.series;
    }

    private String yAxisTitle() {
        return this.yAxisTitle;
    }

    @Override
    public void add(final Number x, final Number y) {
        series().add(x, y, false);
    }

    @Override
    public ScatterPlotGraphData graphData(final PaintType paint, final double range) {
        final XYSeriesCollection result = new XYSeriesCollection();
        result.addSeries(series());
        return new ScatterPlotGraphData(yAxisTitle(), result);
    }

}