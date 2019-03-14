package nc.itf.hbbb.hbreport;

import java.util.List;

import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.hbbb.hbreport.UnionReportVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.pub.BusinessException;

public interface IHBReportQueryService {

    public UnionReportVO[] queryUnionReport(String cond) throws BusinessException;

    /**
     * 根据快速查询条件查询合并报表
     * 
     * @param queryCond
     * @param showColumns
     * @return
     * @throws UFOSrvException
     */
    public List<RepDataQueryResultVO> queryRepDataByCondAndType(IUfoQueryCondVO queryCond, String[] showColumns,
            String repType) throws UFOSrvException;
    
    public List<RepDataQueryResultVO> queryHbRepDataAndReportDataByCondAndType(IUfoQueryCondVO queryCond, String[] showColumns,
            String repType) throws UFOSrvException;


}
