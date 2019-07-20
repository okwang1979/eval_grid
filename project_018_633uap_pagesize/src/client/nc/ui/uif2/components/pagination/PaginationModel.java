package nc.ui.uif2.components.pagination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import nc.ui.ml.NCLangRes;
import nc.ui.uif2.model.ModelDataDescriptor;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;

import org.apache.commons.collections.map.LRUMap;


/**
 * ��ҳ��Ϣ��ģ�Ͷ���
 * 
 * @author lkp
 *
 */
public class PaginationModel {
	
	/** 
	 * ��Ҫע��ķ�ҳ��ѯ����
	 */
	private IPaginationQueryService paginationQueryService = null;
	
	private EventListenerList listenerList = new EventListenerList();
	
	private int cachePageCount = 10;
	
	private List<String> objectPks = new ArrayList<String>();
	
	private Map<String, Object> contentMap = null;
	 
	//ÿҳ��¼����
	private int pageSize = 100;
	//��ǰҳ��
	private int pageIndex = 0;
	//��ҳ��
	private int pageCount;  
	
	//��ѡ�������PKs
	private Map<String, String> selectedPks = new HashMap<String, String>();
	
	public static final int MAX_PAGESIZE = 5000; 
	private int maxPageSize = MAX_PAGESIZE;
	
	private ModelDataDescriptor currentDataDescriptor = null;
	
	@SuppressWarnings("unchecked")
	public void init()
	{
		contentMap = new LRUMap(getPageSize()*getCachePageCount() + 1);
	}
	
	/**
	 * ���õ�ǰ���������pk 
	 * 
	 * @param objectPks
	 */
	public void setObjectPks(String[] pks) throws BusinessException{
		
		objectPks.clear();
		if(pks != null && pks.length > 0)
			objectPks.addAll(Arrays.asList(pks));
		this.refresh();
	}
	
	public void setObjectPks(String[] pks, ModelDataDescriptor mdd) throws BusinessException
	{
		this.currentDataDescriptor = mdd;
		if(mdd != null && mdd.getCount() == -1)
		{
			mdd.setCount(pks == null || pks.length == 0 ? 0 : pks.length);
		}
		setObjectPks(pks); 
		
	} 
	
	/**
	 * ��ȡ��ǰҳ���������Ϣ
	 * 
	 */
	public Object[] getCurrentDatas()
	{
//		List<Object> list = new ArrayList<Object>();
//		int upperIndex = ((pageIndex + 1) * pageSize - 1) > (this.objectPks.size() - 1) ? (this.objectPks.size() - 1) : ((pageIndex + 1) * pageSize - 1);
//		for(int i = pageIndex*pageSize; i <= upperIndex; i++)
//		{
//			list.add(this.contentMap.get(this.objectPks.get(i)));
//		}
//		return list.toArray();
//		
		String[] pks = getCurrentPks();
		if(pks == null || pks.length == 0)
			return null;
		Object[] datas = new Object[pks.length];
		for(int i = 0;i < datas.length; i++)
		{
			datas[i] = contentMap.get(pks[i]);
		}
		return datas;
	} 
	
	public String[] getCurrentPks()
	{
		List<String> list = new ArrayList<String>();
		int upperIndex = ((pageIndex + 1) * pageSize - 1) > (this.objectPks.size() - 1) ? (this.objectPks.size() - 1) : ((pageIndex + 1) * pageSize - 1);
		for(int i = pageIndex*pageSize; i <= upperIndex; i++)
		{
			list.add(objectPks.get(i));
		}
		return list.toArray(new String[0]);
	}
	
	/**
	 * ���õ�ǰ�ı༭ҳ
	 * 
	 * @param pageIndex
	 */
	public void setPageIndex(int pageIndex) throws BusinessException{
		this.pageIndex = pageIndex;
		structChange();
	}
	
	/**
	 * ��������ÿҳ�ļ�¼����
	 * 
	 * @param pageSize
	 * @throws BusinessException
	 */
	public void resetPageSize(int pageSize) throws BusinessException{
		this.setPageSize(pageSize);
		this.resizeCache(); 
		this.pageIndex = 0;
		structChange();
	}
	
	/**
	 * �������û����ҳ����
	 * 
	 * @param pageCount
	 */
	public void resetCachePageCount(int pageCount)
	{
		this.cachePageCount = pageCount;
		this.resizeCache();
	}
	
	/**
	 * �ڵ�ǰ�༭ҳ��ָ����ɾ����¼
	 * 
	 * @param indexs
	 */
	public void removePkByIndexs(int[] indexs)
	{
		for(int i = 0; i < indexs.length; i++)
		{
			String pk = this.objectPks.get(indexs[i] + getPageSize()*getPageIndex());
			contentMap.remove(pk);
			objectPks.remove(pk);
			if(selectedPks.containsKey(pk))
				selectedPks.remove(pk);
		}
	} 
	
	public void removePks(String[] pks)
	{
		if(pks == null || pks.length == 0)
			return ;
		for(String pk : pks)
		{
			contentMap.remove(pk);
			objectPks.remove(pk);
			if(selectedPks.containsKey(pk))
				selectedPks.remove(pk);
		}
	}
	
	
	
	/**
	 * �ڵ�ǰ�༭ҳ����һ����¼��Ϣ
	 *  
	 * @param index
	 * @param pk
	 * @param obj
	 */
	public void insertPkByIndexs(int index, String pk, Object obj)
	{
		int insertIndex = index + getPageSize()*getPageIndex();
		objectPks.add(insertIndex, pk);
		contentMap.put(pk, obj);
	}
	
	public void addSelectPk(String[] pks)
	{
		if(pks != null && pks.length > 0)
			for(String pk : pks)
				selectedPks.put(pk, pk);
	}
	
	public void removeSelectPk(String[] pks)
	{
		if(pks != null && pks.length > 0)
			for(String pk : pks)
				selectedPks.remove(pk);
	}
	
	public String[] getAllSelectedPks()
	{
		return selectedPks.keySet().toArray(new String[0]);
	}
	 
	public Object[] getAllSelectedObjects() throws BusinessException
	{
		String[] pks = getAllSelectedPks();
		if(pks == null || pks.length == 0)
			return null;
		
		List<Integer> indexList = new ArrayList<Integer>();
		List<String> pkList = new ArrayList<String>();
		Object[] objs = new Object[pks.length];
		for(int i = 0; i < objs.length; i++)
		{
			if(!this.contentMap.containsKey(pks[i]))
			{
				pkList.add(pks[i]);
				indexList.add(i);
				objs[i] = null;
			}else
			{
				objs[i] = this.contentMap.get(pks[i]);
			}
		}
		
		if(!pkList.isEmpty())
		{
			Object[] getObjs = this.getPaginationQueryService().queryObjectByPks(pkList.toArray(new String[0]));
			for(int i = 0; i< getObjs.length; i++)
			{    
				Integer index = indexList.get(i);
				String pk = pkList.get(i);
				Object value = getObjs[i];
				//�Ѿ���ɾ��������
				if(value == null)
				{
					this.objectPks.remove(pk);
					this.selectedPks.remove(pk);
					objs[index] = null;
				}else
				{
					this.contentMap.put(pk, value);
					objs[index] = value;
				}
			}
		}
		
		List<Object> objList = new ArrayList<Object>();
		for(int i = 0;i < objs.length; i++)
			if(objs[i] != null)
				objList.add(objs[i]);
		
		return objList.toArray();
	}
	
	/** 
	 * ����ȫ��ѡ��
	 */
	public void setAllSelectedPks()
	{
		if(objectPks != null && !objectPks.isEmpty())
		{
			addSelectPk(objectPks.toArray(new String[0]));
		}
	}
	
	public void clearSelectedPks()
	{
		this.selectedPks.clear();
	}
	
	/**
	 * ���»���������Ϣ
	 * 
	 * @param pk
	 * @param obj
	 */
	public void update(String pk, Object obj)
	{
		this.contentMap.put(pk, obj);
	}
	
	public void next() throws BusinessException
	{
		if(hasNext())
			setPageIndex(this.getPageIndex() + 1);
	}
	
	public void last() throws BusinessException
	{
		if(hasPre())
			setPageIndex(this.getPageIndex() - 1);
	}
	
	public void toTheLast() throws BusinessException
	{
		if(!isTheLast())
			setPageIndex(this.getPageCount() - 1);
	}
	
	public void toTheFirst() throws BusinessException
	{
		if(!isTheFirst())
			setPageIndex(0);
	}
	
	/**
	 * �Ƿ�����һҳ
	 * @return
	 */
	public boolean hasNext()
	{
		return pageIndex < pageCount - 1;
	}
	/**
	 * �Ƿ������һ��ҳ
	 * @return
	 */
	public boolean isTheLast()
	{
		return pageIndex == pageCount - 1;
	}
	/**
	 * �Ƿ�����һҳ
	 * @return
	 */
	public boolean hasPre()
	{
		return pageIndex > 0;
	}
	/**
	 * �Ƿ��ǵ�һҳ
	 * @return
	 */
	public boolean isTheFirst()
	{
		return pageIndex == 0;
	}
	
	/**
	 * �ж�һ��pk�Ƿ���ڱ�ѡ��״̬
	 * @param pk
	 * @return
	 */
	public boolean isSelected(String pk)
	{
		return selectedPks.containsKey(pk);
	}
	
	private void structChange() throws BusinessException
	{
		this.prepareData();
		this.resizePageCount();
		this.fireStructChanged();
		this.fireDataReady();
	}
	
	private void resizePageCount()
	{
		if(objectPks == null || objectPks.size() == 0)
			pageCount = 0;
		else
		{
			int totalCount = objectPks.size();
			if(totalCount%pageSize == 0)
			{
				pageCount = totalCount/pageSize;
			}else
			{
				pageCount = totalCount/pageSize + 1;
			}
		}
	}
	
	protected void prepareData() throws BusinessException
	{ 
		if(this.getPaginationQueryService() == null)
			throw new IllegalStateException(NCLangRes.getInstance().getStrByID("uif2", "PaginationModel-000000")/*�����ڷ�ҳģ�������÷�ҳ���ݲ�ѯ����:IPaginationQueryService��*/);
		
		if(this.objectPks == null || this.objectPks.size() == 0)
			return ;
		
		List<String> needQryPks = this.getNeedQryList();
		while(!needQryPks.isEmpty())
		{
			//ÿ�β�ѯ�Ķ���һ��ҳ��Ĵ�С�������ǰ��Ҫ��ѯ����Ϣ�����㹻�ҵ�ǰ�������һҳ������Ҫ�����ѯ��¼Pk
			int needQryCount = this.pageSize - needQryPks.size();
			if(needQryCount > 0 && (getUpperIndex() < this.objectPks.size() - 1))
			{
				for(int i =  getUpperIndex() + 1; i < this.objectPks.size(); i++)
				{
					if(needQryPks.size() < getPageSize() && !this.contentMap.containsKey(this.objectPks.get(i)))
						needQryPks.add(this.objectPks.get(i));
					if(needQryPks.size() == getPageSize())
						break;
				}
			}
			
			//��ѯ��ʵ���ݣ����ɾ�����������
			Object[] objs = this.getPaginationQueryService().queryObjectByPks(needQryPks.toArray(new String[0]));
			for(int i = 0; i< objs.length; i++)
			{    
				//�Ѿ���ɾ��������
				if(objs[i] == null)
				{
					this.objectPks.remove(needQryPks.get(i));
				}else
				{
					this.contentMap.put(needQryPks.get(i), objs[i]);
				}
			}
			
			needQryPks = this.getNeedQryList();
		}
	}
	
	
	/**
	 * 
	 * ������л��棬��ʾ��һҳ����
	 */
	public void refresh() throws BusinessException
	{
		this.contentMap.clear();
		this.selectedPks.clear();
		this.setPageIndex(0);
	}
	
	/**
	 * ������л��棬��ʾ��ǰҳ����
	 */
	public void pageRefresh() throws BusinessException
	{
		this.contentMap.clear();
		this.selectedPks.clear();
		this.setPageIndex(getPageIndex());
	}
	
	public void addPaginationModelListener(IPaginationModelListener listener)
	{
		listenerList.remove(IPaginationModelListener.class, listener);
		listenerList.add(IPaginationModelListener.class, listener);
	}
	
	public void removePaginationModelListener(IPaginationModelListener l)
	{
		listenerList.remove(IPaginationModelListener.class, l);
	}

	public IPaginationQueryService getPaginationQueryService() {
		return paginationQueryService;
	}

	public void setPaginationQueryService(IPaginationQueryService queryService) {
		this.paginationQueryService = queryService;
	}

	public List<String> getObjectPks() {
		return objectPks;
	}

	public void setPageSize(int pageSize){
		this.pageSize = pageSize;
	}
	
	public int getPageSize() {
		return pageSize;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public int getPageCount() {
		return pageCount;
	}
	
	public int getCachePageCount() {
		return cachePageCount;
	}
	
	public void setCachePageCount(int cachePageCount) {
		this.cachePageCount = cachePageCount;
	}
	
	public int getPageIndexofPk(String pk)
	{
		if(StringUtil.isEmptyWithTrim(pk) || !this.objectPks.contains(pk))
			return -1;
		int index = this.objectPks.indexOf(pk);
		if(this.getPageSize() == 0)
			return -1;
		return index/this.getPageSize();
	}
	
	private List<String> getNeedQryList()
	{
		List<String> needQryPks = new ArrayList<String>();
		int upperIndex = getUpperIndex();
		for(int i = pageIndex*pageSize; i <= upperIndex; i++)
		{
			String pk = this.objectPks.get(i);
			if(!this.contentMap.containsKey(pk))
				needQryPks.add(pk);
		}
		
		return needQryPks;
	}
	
	private int getUpperIndex()
	{
		int upperIndex = ((pageIndex + 1) * pageSize - 1) > (this.objectPks.size() - 1) ? (this.objectPks.size() - 1) : ((pageIndex + 1) * pageSize - 1);
		return upperIndex;
	}
	
	private void fireStructChanged()
	{
		IPaginationModelListener[] ls = listenerList.getListeners(IPaginationModelListener.class);
		for (IPaginationModelListener listener : ls) {
			listener.onStructChanged();
		}
	}
	
	private void fireDataReady()
	{
		IPaginationModelListener[] ls = listenerList.getListeners(IPaginationModelListener.class);
		for (IPaginationModelListener listener : ls) {
			listener.onDataReady();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void resizeCache()
	{
		Map<String, Object> tempMap = this.contentMap;
		this.contentMap = new LRUMap(getPageSize()*getCachePageCount() + 1);
		this.contentMap.putAll(tempMap);
	}

	public int getMaxPageSize() {
		return maxPageSize;
	}

	public void setMaxPageSize(int maxPageSize) {
		this.maxPageSize = maxPageSize;
	}

	public ModelDataDescriptor getCurrentDataDescriptor() {
		return currentDataDescriptor;
	}

}
