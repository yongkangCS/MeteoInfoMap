#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo Dataset module
# Note: Jython
#-----------------------------------------------------
from org.meteoinfo.projection import ProjectionInfo
from org.meteoinfo.data import GridData
from ucar.ma2 import Array

# Dimension array
class DimArray():
    
    # array must be ucar.ma2.Array
    def __init__(self, array=None, dims=None, missingvalue=-9999.0, proj=None):
        self.array = array
        self.dims = dims
        self.missingvalue = missingvalue
        self.proj = proj
        
    def togriddata(self):
        xdata = self.dims[1].getDimValue()
        ydata = self.dims[0].getDimValue()
        gdata = GridData(self.array, xdata, ydata, self.missingvalue, self.proj)
        return PyGridData(gdata)
        
# The encapsulate class of GridData
class PyGridData():
    
    # griddata must be a GridData object
    def __init__(self, griddata=None, dataarray=None, xarray=None, yarray=None):
        if griddata != None:
            self.data = griddata
        else:
            self.data = GridData()
    
    def __getitem__(self, indices):
        print type(indices)
        if not isinstance(indices, tuple):
            print 'indices must be tuple!'
            return None
        
        if len(indices) != 2:
            print 'indices must be 2 dimension!'
            return None
            
        sxidx = 0 if indices[0].start is None else indices[0].start
        exidx = indices[0].stop is None and self.data.getXNum() or indices[0].stop
        xstep = indices[0].step is None and 1 or indices[0].step
        syidx = 0 if indices[1].start is None else indices[1].start
        eyidx = indices[1].stop is None and self.data.getYNum() or indices[1].stop
        ystep = indices[1].step is None and 1 or indices[1].step
        gdata = PyGridData(self.data.extract(sxidx, exidx, xstep, syidx, eyidx, ystep))
        return gdata
    
    def add(self, other):
        gdata = None
        if isinstance(other, PyGridData):            
            gdata = PyGridData(self.data.add(other.data))
        else:
            gdata = PyGridData(self.data.add(other))
        return gdata
    
    def __add__(self, other):
        gdata = None
        print isinstance(other, PyGridData)
        if isinstance(other, PyGridData):            
            gdata = PyGridData(self.data.add(other.data))
        else:
            gdata = PyGridData(self.data.add(other))
        return gdata
        
    def __radd__(self, other):
        return PyGridData.__add__(self, other)
        
    def __sub__(self, other):
        gdata = None
        if isinstance(other, PyGridData):
            gdata = PyGridData(self.data.sub(other.data))
        else:
            gdata = PyGridData(self.data.sub(other))
        return gdata
        
    def __rsub__(self, other):
        gdata = None
        if isinstance(other, PyGridData):
            gdata = PyGridData(other.data.sub(self.data))
        else:
            gdata = PyGridData(DataMath.sub(other, self.data))
        return gdata
    
    def __mul__(self, other):
        gdata = None
        if isinstance(other, PyGridData):
            gdata = PyGridData(self.data.mul(other.data))
        else:
            gdata = PyGridData(self.data.mul(other))
        return gdata
        
    def __rmul__(self, other):
        return PyGridData.__mul__(self, other)
        
    def __div__(self, other):
        gdata = None
        if isinstance(other, PyGridData):
            gdata = PyGridData(self.data.div(other.data))
        else:
            gdata = PyGridData(self.data.div(other))
        return gdata
        
    def __rdiv__(self, other):
        gdata = None
        if isinstance(other, PyGridData):
            gdata = PyGridData(other.data.div(self.data))
        else:
            gdata = PyGridData(DataMath.div(other, self))
        return gdata
        
    # other must be a numeric data
    def __pow__(self, other):
        gdata = PyGridData(self.data.pow(other))
        return gdata
        
    def getminvalue(self):
        return self.data.getMinValue()
        
    def getmaxvalue(self):
        return self.data.getMaxValue()