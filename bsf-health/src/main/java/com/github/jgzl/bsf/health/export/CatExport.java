package com.github.jgzl.bsf.health.export;

import com.github.jgzl.bsf.health.base.AbstractExport;
import com.github.jgzl.bsf.health.base.Report;

/**
 * @author: chejiangyi
 * @version: 2019-08-14 09:59
 **/
public class CatExport extends AbstractExport {
//    CatStatus catStatus;
    @Override
    public void start() {
        super.start();
//        catStatus = new CatStatus();
//        StatusExtensionRegister.getInstance().register(catStatus);
    }
    @Override
    public void run(Report report){

    }

    @Override
    public void close() {
        super.close();
//        StatusExtensionRegister.getInstance().unregister(catStatus);
    }

//    public class CatStatus implements StatusExtension{
//        public CatStatus(){
//        }
//
//        @Override
//        public String getDescription(){
//            return "bsf性能报表";
//        }
//        @Override
//        public String getId(){
//            return "bsf性能报表";
//        }
//        @Override
//        public Map<String, String> getProperties(){
//            Map<String,String> map= new LinkedHashMap();
//            if(!ExportProperties.Default().isBsfCatEnabled()){
//                return map;
//            }
//            val healthProvider = ContextUtils.getBean(HealthCheckProvider.class,false);
//            if(healthProvider!=null) {
//                val report = healthProvider.getReport(false);
//                report.eachReport((String field, Report.ReportItem reportItem) -> {
//                    if(reportItem!=null&&reportItem.getValue() instanceof Number)
//                    {map.put(reportItem.getDesc(), reportItem.getValue().toString());}
//                    return reportItem;
//                });
//            }
//            return map;
//        }
//    }
}
