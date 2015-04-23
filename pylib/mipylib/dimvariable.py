#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo Dataset module
# Note: Jython
#-----------------------------------------------------
from org.meteoinfo.data.meteodata import Variable, Dimension
import dimarray
from dimarray import DimArray, PyGridData

# Dimension variable
class DimVariable():
    
    # variable must be org.meteoinfo.data.meteodata.Variable
    def __init__(self, variable=None, dataset=None):
        self.variable = variable
        self.dataset = dataset
        self.name = variable.getName()
        self.ndim = variable.getDimNumber()
        
    # get dimension length
    def getdimlen(self, idx):
        return self.variable.getDimLength(idx)
        
    def __len__(self):
        len = 1;
        for dim in self.variable.getDimensions():
            len = len * dim.getDimLength()            
        return len
        
    def __getitem__(self, indices):
        #print type(indices)
        if not isinstance(indices, tuple):
            print 'indices must be tuple!'
            return None
        
        if len(indices) != self.ndim:
            print 'indices must be ' + str(self.ndim) + ' dimensions!'
            return None
            
        origin = []
        size = []
        stride = []
        dims = []
        for i in range(0, self.ndim):   
            if isinstance(indices[i], int):
                sidx = indices[i]
                eidx = indices[i]
                step = 1
            else:
                sidx = 0 if indices[i].start is None else indices[i].start
                eidx = indices[i].stop is None and self.getdimlen(i)-1 or indices[i].stop
                step = indices[i].step is None and 1 or indices[i].step
            origin.append(sidx)
            n = eidx - sidx + 1
            size.append(n)
            stride.append(step)
            if n > 1:
                dim = self.variable.getDimension(i)
                dims.append(dim.extract(sidx, eidx, step))
                    
        array = self.dataset.read(self.name, origin, size, stride)
        data = DimArray(array, dims, self.dataset.missingvalue, self.dataset.proj)
        return data