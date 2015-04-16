#-----------------------------------------------------
# Author: Yaqiang Wang
# Date: 2014-12-27
# Purpose: MeteoInfo Dataset module
# Note: Jython
#-----------------------------------------------------
from org.meteoinfo.data.meteodata import MeteoDataInfo
from ucar.ma2 import Section
import dimvariable
from dimvariable import DimVariable

# Dimension dataset
class DimDataset():
    
    # dataset must be org.meteoinfo.data.meteodata.MeteoDataInfo
    def __init__(self, dataset=None):
        self.dataset = dataset
        self.filename = dataset.getFileName()
        self.nvar = dataset.getDataInfo().getVariableNum()
        self.missingvalue = dataset.getMissingValue()
        self.proj = dataset.getProjectionInfo()
        
    def __getitem__(self, key):
        if isinstance(key, str):
            print key
            return DimVariable(self.dataset.getDataInfo().getVariable(key), self)
        return None
        
    def read(self, varname, origin, size, stride=None):
        return self.dataset.read(varname, origin, size)
        
    def dump(self):
        return self.dataset.getInfoText()