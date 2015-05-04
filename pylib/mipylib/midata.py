#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo Dataset module
# Note: Jython
#-----------------------------------------------------
from org.meteoinfo.data import GridData, StationData, DataMath, TableData
from org.meteoinfo.data.meteodata import MeteoDataInfo
from org.meteoinfo.array import ArrayUtil

import dimdatafile
import dimvariable
import dimarray
import miarray
from dimdatafile import DimDataFile
from dimvariable import DimVariable
from dimarray import PyGridData
from miarray import MIArray

# Global variables
meteodatalist = []
c_meteodata = None

def isgriddata(gdata):
    return isinstance(gdata, PyGridData)
    
def isstationdata(sdata):
    return isinstance(sdata, PyStationData)

###############################################################         
# The encapsulate class of StationData
class PyStationData():
    
    # data must be a GridData object
    def __init__(self, data=None):
        self.data = data
    
    def add(self, other):
        gdata = None
        if isinstance(other, PyStationData):            
            gdata = PyStationData(self.data.add(other.data))
        else:
            gdata = PyStationData(self.data.add(other))
        return gdata
    
    def __add__(self, other):
        gdata = None
        print isinstance(other, PyStationData)
        if isinstance(other, PyStationData):            
            gdata = PyStationData(self.data.add(other.data))
        else:
            gdata = PyStationData(self.data.add(other))
        return gdata
        
    def __radd__(self, other):
        return PyStationData.__add__(self, other)
        
    def __sub__(self, other):
        gdata = None
        if isinstance(other, PyStationData):
            gdata = PyStationData(self.data.sub(other.data))
        else:
            gdata = PyStationData(self.data.sub(other))
        return gdata
        
    def __rsub__(self, other):
        gdata = None
        if isinstance(other, PyStationData):
            gdata = PyStationData(other.data.sub(self.data))
        else:
            gdata = PyStationData(DataMath.sub(other, self.data))
        return gdata
    
    def __mul__(self, other):
        gdata = None
        if isinstance(other, PyStationData):
            gdata = PyStationData(self.data.mul(other.data))
        else:
            gdata = PyStationData(self.data.mul(other))
        return gdata
        
    def __rmul__(self, other):
        return PyStationData.__mul__(self, other)
        
    def __div__(self, other):
        gdata = None
        if isinstance(other, PyStationData):
            gdata = PyStationData(self.data.div(other.data))
        else:
            gdata = PyStationData(self.data.div(other))
        return gdata
        
    def __rdiv__(self, other):
        gdata = None
        if isinstance(other, PyStationData):
            gdata = PyStationData(other.data.div(self.data))
        else:
            gdata = PyStationData(DataMath.div(other, self))
        return gdata
        
    # other must be a numeric data
    def __pow__(self, other):
        gdata = PyStationData(self.data.pow(other))
        return gdata

###############################################################        
#  The encapsulate class of TableData
class PyTableData():
    # Must be a TableData object
    def __init__(self, data=None):
        self.data = data
        
    def __getitem__(self, key):
        if isinstance(key, str):
            print key
            return self.data.getColumnData(key).getValidDataValues()
        return None

#################################################################        
def addfile_grads(fname):
    meteodata = MeteoDataInfo()
    meteodata.openGrADSData(fname)
    __addmeteodata(meteodata)
    datafile = DimDataFile(meteodata)
    return datafile
    
def addfile(fname):
    meteodata = MeteoDataInfo()
    meteodata.openData(fname)
    __addmeteodata(meteodata)
    datafile = DimDataFile(meteodata)
    return datafile
    
def addfile_nc(fname):
    meteodata = MeteoDataInfo()
    meteodata.openNetCDFData(fname)
    __addmeteodata(meteodata)
    datafile = DimDataFile(meteodata)
    return datafile
    
def addfile_arl(fname):
    meteodata = MeteoDataInfo()
    meteodata.openARLData(fname)
    __addmeteodata(meteodata)
    datafile = DimDataFile(meteodata)
    return datafile
    
def addfile_surfer(fname):
    meteodata = MeteoDataInfo()
    meteodata.openSurferGridData(fname)
    __addmeteodata(meteodata)
    datafile = DimDataFile(meteodata)
    return datafile
    
def addfile_mm5(fname):
    meteodata = MeteoDataInfo()
    meteodata.openMM5Data(fname)
    __addmeteodata(meteodata)
    datafile = DimDataFile(meteodata)
    return datafile
    
def addfile_micaps(fname):
    meteodata = MeteoDataInfo()
    meteodata.openMICAPSData(fname)
    __addmeteodata(meteodata)
    datafile = DimDataFile(meteodata)
    return datafile
    
def addfile_hyconc(fname):
    meteodata = MeteoDataInfo()
    meteodata.openHYSPLITConcData(fname)
    __addmeteodata(meteodata)
    datafile = DimDataFile(meteodata)
    return datafile

def __addmeteodata(meteodata):
    global c_meteodata, meteodatalist
    meteodatalist.append(meteodata)
    c_meteodata = meteodata
    #print 'Current meteodata: ' + c_meteodata.toString()        
    
def getgriddata(varname='var', timeindex=0, levelindex=0, yindex=None, xindex=None):
    if c_meteodata.isGridData():
        c_meteodata.setTimeIndex(timeindex)
        c_meteodata.setLevelIndex(levelindex)
        gdata = PyGridData(c_meteodata.getGridData(varname))
        return gdata
    else:
        return None
        
def getstationdata(varname='var', timeindex=0, levelindex=0):
    if c_meteodata.isStationData():
        c_meteodata.setTimeIndex(timeindex)
        c_meteodata.setLevelIndex(levelindex)
        sdata = PyStationData(c_meteodata.getStationData(varname))
        return sdata
    else:
        return None
        
def readtable(filename, **kwargs):
    delimiter = kwargs.pop('delimiter', ',')
    format = kwargs.pop('format', None)
    headerlines = kwargs.pop('headerlines', 0)
    encoding = kwargs.pop('encoding', 'UTF8')
    tdata = TableData()
    tdata.readASCIIFile(filename, delimiter, headerlines, format, encoding)
    return PyTableData(tdata)
    
def arange(start=0, stop=1, step=1, dtype=None):
    return MIArray(ArrayUtil.arrayRange(start, stop, step))