package cn.ibizlab.runner.servicerunner.runtime;

import net.ibizsys.central.cloud.core.ServiceSystemRuntime;
import net.ibizsys.central.dataentity.IDataEntityRuntime;
import net.ibizsys.central.dataentity.service.IDEService;
import net.ibizsys.model.IPSDynaInstService;
import net.ibizsys.model.IPSSystemService;
import net.ibizsys.model.PSModelServiceImpl;
import net.ibizsys.model.res.IPSSysUtil;
import net.ibizsys.runtime.res.ISysUtilRuntime;

public class SystemRuntimeBase extends ServiceSystemRuntime implements ISystemRuntime {

    @Override
    public String getName() {
        return "IBizServiceRunner";
    }

    @Override
    protected IPSSystemService createPSSystemService() throws Exception {
        PSModelServiceImpl psModelServiceImpl = new PSModelServiceImpl();
        psModelServiceImpl.setPSModelFolderPath("/model/cn/ibizlab/runner/servicerunner", true);
        return psModelServiceImpl;
    }


}
