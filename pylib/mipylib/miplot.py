#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-26
# Purpose: MeteoInfo plot module
# Note: Jython
#-----------------------------------------------------
import os
import inspect

from org.meteoinfo.chart import ChartPanel
from org.meteoinfo.data import XYListDataset, GridData
from org.meteoinfo.data.mapdata import MapDataManage
from org.meteoinfo.data.meteodata import MeteoDataInfo, DrawMeteoData
from org.meteoinfo.chart.plot import XY1DPlot, XY2DPlot, MapPlot, ChartPlotMethod
from org.meteoinfo.chart import Chart, ChartText, LegendPosition
from org.meteoinfo.script import ChartForm, MapForm
from org.meteoinfo.legend import MapFrame, LineStyles, PointBreak, PolylineBreak, LegendManage, LegendScheme
from org.meteoinfo.drawing import PointStyle
from org.meteoinfo.global import Extent
from org.meteoinfo.global.colors import ColorUtil, ColorMap
from org.meteoinfo.layout import MapLayout
from org.meteoinfo.map import MapView
from org.meteoinfo.laboratory.gui import FrmMain

from javax.swing import WindowConstants
from java.awt import Color, Font

import dimarray
from dimarray import DimArray, PyGridData

## Global ##
milapp = None
isinteractive = False
maplayout = MapLayout()
chartpanel = ChartPanel(Chart())
isholdon = False
c_plot = None
ismap = False
maplayer = None
#mapfn = os.path.join(inspect.getfile(inspect.currentframe()), '../../../map/country1.shp')
mapfn = os.path.join(inspect.getfile(inspect.currentframe()), 'D:/Temp/map/country1.shp')
mapfn = os.path.abspath(mapfn)
""""
if os.path.exists(mapfn):
    print 'Default map file: ' + mapfn
    maplayer = MapDataManage.loadLayer(mapfn)
    pgb = maplayer.getLegendScheme().getLegendBreaks().get(0)
    pgb.setDrawFill(False)
    pgb.setOutlineColor(Color.darkGray)    
"""
    
def map(map=True):
    global ismap
    if map:
        ismap = True
        print 'Switch to map mode'
    else:
        ismap = False
        print 'Switch to figure mode'

def hold(ishold):
    global c_plot
    if ishold:
        c_plot = chartpanel.getChart().getPlot()
    else:
        c_plot = None
        
def plot(*args, **kwargs):
    if ismap:
        map(False)

    #Parse args
    if c_plot is None:
        dataset = XYListDataset()
    else:
        dataset = c_plot.getDataset()
        if dataset is None:
            dataset = XYListDataset()
    xdatalist = []
    ydatalist = []
    styles = []
    c = 'x'
    for arg in args:
        if len(args) == 1:
            ydata = arg
            xdata = []
            for i in range(0, len(ydata)):
                xdata.append(i)
            xdatalist.append(xdata)
            ydatalist.append(ydata)
        else:
            if c == 'x':
                xdatalist.append(arg)                
                c = 'y'
            elif c == 'y':
                ydatalist.append(arg)
                c = 's'
            elif c == 's':
                if isinstance(arg, basestring):
                    styles.append(arg)
                    c = 'x'
                else:
                    styles.append('-')
                    xdatalist.append(arg)
                    c = 'y'
    while len(styles) < len(xdatalist):
        styles.append('-')
    
    #Add data series
    for i in range(0, len(xdatalist)):
        label = kwargs.pop('label', 'S_' + str(i + 1))
        dataset.addSeries(label, xdatalist[i], ydatalist[i])
    
    #Create XY1DPlot
    if c_plot is None:
        plot = XY1DPlot(dataset)
    else:
        plot = c_plot
        plot.setDataset(dataset)
    
    #Set plot data styles
    for i in range(0, len(styles)):
        idx = dataset.getSeriesCount() - len(styles) + i
        print 'Series index: ' + str(idx)
        __setplotstyle(plot, idx, styles[i], len(xdatalist[i]), **kwargs)
    
    #Paint dataset
    chart = chartpanel.getChart()
    if c_plot is None:
        chart.clearPlots()
        chart.setPlot(plot)
    #chart.setAntiAlias(True)
    chartpanel.setChart(chart)
    if isinteractive:
		chartpanel.paintGraphics()
    return plot    
 
def hist(x, bins=10, range=None, normed=False, cumulative=False, \
    bottom=None, histtype='bar', align='mid', \
    orientation='vertical', rwidth=None, log=False, **kwargs):
    
    return None
    
def scatter(*args, **kwargs):
    #Parse args
    if c_plot is None:
        dataset = XYListDataset()
    else:
        dataset = c_plot.getDataset()
        if dataset is None:
            dataset = XYListDataset()
    xdatalist = []
    ydatalist = []
    styles = []
    c = 'x'
    for arg in args:
        if len(args) == 1:
            ydata = arg
            xdata = []
            for i in range(0, len(ydata)):
                xdata.append(i)
            xdatalist.append(xdata)
            ydatalist.append(ydata)
        else:
            if c == 'x':
                xdatalist.append(arg)                
                c = 'y'
            elif c == 'y':
                ydatalist.append(arg)
                c = 's'
            elif c == 's':
                if isinstance(arg, basestring):
                    styles.append(arg)
                    c = 'x'
                else:
                    styles.append('-')
                    xdatalist.append(arg)
                    c = 'y'
    while len(styles) < len(xdatalist):
        styles.append('-')
    
    #Add data series
    for i in range(0, len(xdatalist)):
        label = kwargs.pop('label', 'S_' + str(i + 1))
        dataset.addSeries(label, xdatalist[i], ydatalist[i])
    
    #Create XY1DPlot
    if c_plot is None:
        plot = XY1DPlot(dataset)
    else:
        plot = c_plot
        plot.setDataset(dataset)
    
    #Set plot data styles
    for i in range(0, len(styles)):
        idx = dataset.getSeriesCount() - len(styles) + i
        print 'Series index: ' + str(idx)
        __setplotstyle(plot, idx, styles[i], len(xdatalist[i]), **kwargs)
    
    #Paint dataset
    chart = chartpanel.getChart()
    if c_plot is None:
        chart.clearPlots()
        chart.setPlot(plot)
    #chart.setAntiAlias(True)
    chartpanel.setChart(chart)
    if isinteractive:
		chartpanel.paintGraphics()
    return plot 
 
def figure():
    show()
    
def show():
    #print ismap
    if ismap:
        frame = MapForm(maplayout)
        frame.setSize(750, 540)
        frame.setLocationRelativeTo(None)
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
        frame.setVisible(True)
        maplayout.paintGraphics()
    else:
        if milapp == None:
            form = ChartForm(chartpanel)
            chartpanel.paintGraphics()
            form.setSize(600, 500)
            form.setLocationRelativeTo(None)
            form.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
            form.setVisible(True)     
        else:
            figureDock = milapp.getFigureDock()
            figureDock.addNewFigure('Figure 1', chartpanel)
    
def subplot(nrows, ncols, plot_number):
    global c_plot
    chart = chartpanel.getChart()
    chart.setRowNum(nrows)
    chart.setColumnNum(ncols)
    plot = chart.getPlot(plot_number)
    if plot is None:
        plot = XY1DPlot()
        plot_number -= 1
        rowidx = plot_number / ncols
        colidx = plot_number % ncols
        plot.rowIndex = rowidx
        plot.columnIndex = colidx
        chart.addPlot(plot)
    c_plot = plot
    return plot
    
def savefig(fname, width=None, height=None):
    if (not width is None) and (not height is None):
        chartpanel.setSize(width, height)
    chartpanel.paintGraphics()
    chartpanel.exportToPicture(fname)    
 	
def __setplotstyle(plot, idx, style, n, **kwargs):
    c = __getcolor(style)
    linewidth = kwargs.pop('linewidth', 1.0)
    #print 'Line width: ' + str(linewidth)
    caption = plot.getLegendBreak(idx).getCaption()
    pointStyle = __getpointstyle(style)
    lineStyle = __getlinestyle(style)
    if not pointStyle is None:
        if lineStyle is None:
            #plot.setChartPlotMethod(ChartPlotMethod.POINT)            
            pb = PointBreak()
            pb.setCaption(caption)
            pb.setSize(8)
            pb.setStyle(pointStyle)
            if not c is None:
                pb.setColor(c)
            plot.setLegendBreak(idx, pb)
        else:
            plot.setChartPlotMethod(ChartPlotMethod.LINE_POINT)
            plb = PolylineBreak()
            plb.setCaption(caption)
            plb.setSize(linewidth)
            plb.setStyle(lineStyle)
            plb.setDrawSymbol(True)
            plb.setSymbolStyle(pointStyle)
            plb.setSymbolInterval(__getsymbolinterval(n))
            if not c is None:
                plb.setColor(c)
                plb.setSymbolColor(c)
            plot.setLegendBreak(idx, plb)
    else:
        plot.setChartPlotMethod(ChartPlotMethod.LINE)
        plb = PolylineBreak()
        plb.setCaption(caption)
        plb.setSize(linewidth)
        if not c is None:
            plb.setColor(c)
        if not lineStyle is None:
            plb.setStyle(lineStyle)
        plot.setLegendBreak(idx, plb)
    
def __getlinestyle(style):
    lineStyle = None
    if '--' in style:
        lineStyle = LineStyles.Dash
    elif ':' in style:
        lineStyle = LineStyles.Dot
    elif '-.' in style:
        lineStyle = LineStyles.DashDot
    elif '-' in style:
        lineStyle = LineStyles.Solid
    
    return lineStyle
    
def __getpointstyle(style):
    pointStyle = None
    if 'o' in style:
        pointStyle = PointStyle.Circle
    elif 'D' in style:
        pointStyle = PointStyle.Diamond
    elif '+' in style:
        pointStyle = PointStyle.Plus
    elif 's' in style:
        pointStyle = PointStyle.Square
    elif '*' in style:
        pointStyle = PointStyle.StarLines
    elif '^' in style:
        pointStyle = PointStyle.UpTriangle
    elif 'x' in style:
        pointStyle = PointStyle.XCross
    
    return pointStyle
    
def __getcolor(style):
    c = None
    if style == 'red':
        c = Color.red
    elif style == 'black':
        c = Color.black
    elif style == 'blue':
        c = Color.blue
    elif style == 'green':
        c = Color.green
    elif style == 'white':
        c = Color.white
    elif style == 'yellow':
        c = Color.yellow
    elif 'r' in style:
        c = Color.red
    elif 'k' in style:
        c = Color.black
    elif 'b' in style:
        c = Color.blue
    elif 'g' in style:
        c = Color.green
    elif 'w' in style:
        c = Color.white
    elif 'c' in style:
        c = Color.cyan
    elif 'm' in style:
        c = Color.magenta
    elif 'y' in style:
        c = Color.yellow
        
    return c
    
def __getsymbolinterval(n):
    i = 1
    v = 20
    if n < v:
        i = 1
    else:
        i = n / v
    
    return i

def title(title, fontname='Arial', fontsize=14, bold=True, color='black'):
    if bold:
        font = Font(fontname, Font.BOLD, fontsize)
    else:
        font = Font(fontname, Font.PLAIN, fontsize)
    c = __getcolor(color)
    ctitile = ChartText(title, font)
    ctitile.setColor(c)
    chartpanel.getChart().getPlot().setTitle(ctitile)
    if isinteractive:
        chartpanel.paintGraphics()

def xlabel(label, fontname='Arial', fontsize=14, bold=False, color='black'):
    if bold:
        font = Font(fontname, Font.BOLD, fontsize)
    else:
        font = Font(fontname, Font.PLAIN, fontsize)
    c = __getcolor(color)
    plot = chartpanel.getChart().getPlot()
    axis = plot.getXAxis()
    axis.setLabel(label)
    axis.setDrawLabel(True)
    axis.setLabelFont(font)
    axis.setLabelColor(c)
    if isinteractive:
        chartpanel.paintGraphics()
    
def ylabel(label, fontname='Arial', fontsize=14, bold=False, color='black'):
    if bold:
        font = Font(fontname, Font.BOLD, fontsize)
    else:
        font = Font(fontname, Font.PLAIN, fontsize)
    c = __getcolor(color)
    plot = chartpanel.getChart().getPlot()
    axis = plot.getYAxis()
    axis.setLabel(label)
    axis.setDrawLabel(True)
    axis.setLabelFont(font)
    axis.setLabelColor(c)
    if isinteractive:
        chartpanel.paintGraphics()
    
def axis(limits):
    if len(limits) == 4:
        xmin = limits[0]
        xmax = limits[1]
        ymin = limits[2]
        ymax = limits[3]
        plot = chartpanel.getChart().getPlot()
        plot.setDrawExtent(Extent(xmin, xmax, ymin, ymax))
        if isinteractive:
            chartpanel.paintGraphics()
            
def legend(*args, **kwargs):
    plot = chartpanel.getChart().getPlot()
    plot.updateLegendScheme()
    legend = plot.getLegend()
    loc = kwargs.pop('loc', 'upper right')    
    lp = LegendPosition.fromString(loc)
    legend.setPosition(lp)
    if lp == LegendPosition.CUSTOM:
        x = kwargs.pop('x', 0)
        y = kwargs.pop('y', 0)
        legend.setX(x)
        legend.setY(y)
    plot.setDrawLegend(True)
    if isinteractive:
        chartpanel.paintGraphics()
        
def colorbar(layer, **kwargs):
    cmap = kwargs.pop('cmap', None)
    shrink = kwargs.pop('shrink', 1)
    plot = chartpanel.getChart().getPlot()
    ls = layer.getLegendScheme()
    legend = plot.getLegend()
    legend.setColorbar(True)
    legend.setLegendScheme(ls)
    legend.setPosition(LegendPosition.RIGHT_OUTSIDE)
    plot.setDrawLegend(True)
    if isinteractive:
        chartpanel.paintGraphics()

def contour(*args, **kwargs):
    n = len(args)    
    #print 'Args number: ' + str(n)
    #if isinstance(args[0], PyGridData):
    cmapstr = kwargs.pop('cmap', 'grads_rainbow')
    cmap = ColorUtil.getColorMap(cmapstr) 
    if n == 1:        
        if isinstance(args[0], DimArray):
            gdata = args[0].togriddata()
        else:
            gdata = args[0]
        ls = LegendManage.createLegendScheme(gdata.getminvalue(), gdata.getmaxvalue(), cmap)
        layer = __contour_griddata(gdata, ls)
        return layer
    #else:
    #    gdata = GridData()
    #    gdata.xArray = args[0]
    #    gdata.yArray = args[1]
    #    gdata.data = args[2]
    #    pygdata = PyGridData(gdata)
    #    plot = __contour_griddata(pygdata)
    #    return plot

def __contour_griddata(gdata, ls, fill=False):
    print 'GridData...'
    if fill:
        layer = DrawMeteoData.createShadedLayer(gdata.data, ls, 'layer', 'data', True)
    else:
        layer = DrawMeteoData.createContourLayer(gdata.data, ls, 'layer', 'data', True)
    mapview = MapView()
    mapview.setLockViewUpdate(True)
    mapview.addLayer(layer)
    mapview.setLockViewUpdate(False)
    plot = XY2DPlot(mapview)
    chart = Chart(plot)
    #chart.setAntiAlias(True)
    chartpanel.setChart(chart)
    if isinteractive:
        chartpanel.paintGraphics()
    return layer
        
def contourf(*args, **kwargs):
    n = len(args)    
    #print 'Args number: ' + str(n)
    cmapstr = kwargs.pop('cmap', 'grads_rainbow')
    cmap = ColorUtil.getColorMap(cmapstr)
    if n == 1:    
        if isinstance(args[0], DimArray):
            gdata = args[0].togriddata()
        else:
            gdata = args[0]
        ls = LegendManage.createLegendScheme(gdata.getminvalue(), gdata.getmaxvalue(), cmap)
        layer = __contour_griddata(gdata, ls, fill=True)
        return layer

def contourm(*args, **kwargs):
    n = len(args)    
    print 'Args number: ' + str(n)
    if n == 2:    
        __contour_griddata_m(args[0], args[1])
        
def contourfm(*args, **kwargs):
    n = len(args)    
    print 'Args number: ' + str(n)
    if n == 2:    
        plot = args[0]
        __contour_griddata_m(plot, args[1], fill=True)
        return plot
        
def __contour_griddata_m(plot, gdata, fill=False):
    print 'GridData...'
    if fill:
        layer = DrawMeteoData.createShadedLayer(gdata.data, 'layer', 'data')
    else:
        layer = DrawMeteoData.createContourLayer(gdata.data, 'layer', 'data')
    plot.getMapFrame().addLayer(0, layer)
    chart = Chart(plot)
    #chart.setAntiAlias(True)
    chartpanel.setChart(chart)
    if isinteractive:
        chartpanel.paintGraphics()
        
def geomap():
    mapview = MapView()
    #print 'Is GeoMap: ' + str(mapview.isGeoMap())
    plot = MapPlot(mapview)
    return plot
        
def geoshow(plot, layer):
    plot.getMapFrame().addLayer(layer)
    
def display(data):
    if not ismap:
        map()
    
    if c_meteodata is None:
        print 'The current meteodata is None!'
        return
    
    if isinstance(data, PyGridData):
        print 'PyGridData'
        layer = DrawMeteoData.createContourLayer(data.data, 'layer', 'data')
        mapview = MapView()
        mapview.setLockViewUpdate(True)
        mapview.addLayer(layer)
        mapview.setLockViewUpdate(False)
        plot = XY2DPlot(mapview)
        chart = Chart(plot)
        #chart.setAntiAlias(True)
        chartpanel.setChart(chart)
        if isinteractive:
            chartpanel.paintGraphics()
    elif isinstance(data, basestring):
        if c_meteodata.isGridData():
            gdata = c_meteodata.getGridData(data)
            layer = DrawMeteoData.createContourLayer(gdata, data, data)
            #if maplayout is None:
                #maplayout = MapLayout()
            mapFrame = maplayout.getActiveMapFrame()
            mapView = mapFrame.getMapView()
            mapView.setLockViewUpdate(True)
            mapFrame.addLayer(layer)
            maplayout.getActiveLayoutMap().zoomToExtentLonLatEx(mapView.getMeteoLayersExtent())
            mapView.setLockViewUpdate(False)
            if isinteractive:
                maplayout.paintGraphics()
    else:
        print 'Unkown data type!'
        print type(data)