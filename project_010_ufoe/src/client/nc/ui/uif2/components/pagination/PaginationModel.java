/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package nc.ui.uif2.components.pagination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.EventListenerList;
import nc.ui.ml.NCLangRes;
import nc.ui.uif2.model.ModelDataDescriptor;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import org.apache.commons.collections.map.LRUMap;

public class PaginationModel {
	private IPaginationQueryService paginationQueryService;
	private EventListenerList listenerList;
	private int cachePageCount;
	private List<String> objectPks;
	private Map<String, Object> contentMap;
	private int pageSize;
	private int pageIndex;
	private int pageCount;
	private Map<String, String> selectedPks;
	public static final int MAX_PAGESIZE = 500;
	private int maxPageSize;
	private ModelDataDescriptor currentDataDescriptor;

	public PaginationModel() {
		this.paginationQueryService = null;

		this.listenerList = new EventListenerList();

		this.cachePageCount = 10;

		this.objectPks = new ArrayList();

		this.contentMap = null;

		this.pageSize = 100;

		this.pageIndex = 0;

		this.selectedPks = new HashMap();

		this.maxPageSize = 5000;

		this.currentDataDescriptor = null;
	}

	public void init() {
		this.contentMap = new LRUMap(getPageSize() * getCachePageCount() + 1);
	}

	public void setObjectPks(String[] pks) throws BusinessException {
		this.objectPks.clear();
		if ((pks != null) && (pks.length > 0))
			this.objectPks.addAll(Arrays.asList(pks));
		refresh();
	}

	public void setObjectPks(String[] pks, ModelDataDescriptor mdd)
			throws BusinessException {
		this.currentDataDescriptor = mdd;
		if ((mdd != null) && (mdd.getCount() == -1)) {
			mdd.setCount(((pks == null) || (pks.length == 0)) ? 0 : pks.length);
		}
		setObjectPks(pks);
	}

	public Object[] getCurrentDatas() {
		String[] pks = getCurrentPks();
		if ((pks == null) || (pks.length == 0))
			return null;
		Object[] datas = new Object[pks.length];
		for (int i = 0; i < datas.length; ++i) {
			datas[i] = this.contentMap.get(pks[i]);
		}
		return datas;
	}

	public String[] getCurrentPks() {
		List list = new ArrayList();
		int upperIndex = ((this.pageIndex + 1) * this.pageSize - 1 > this.objectPks
				.size() - 1) ? this.objectPks.size() - 1 : (this.pageIndex + 1)
				* this.pageSize - 1;
		for (int i = this.pageIndex * this.pageSize; i <= upperIndex; ++i) {
			list.add(this.objectPks.get(i));
		}
		return ((String[]) list.toArray(new String[0]));
	}

	public void setPageIndex(int pageIndex) throws BusinessException {
		this.pageIndex = pageIndex;
		structChange();
	}

	public void resetPageSize(int pageSize) throws BusinessException {
		setPageSize(pageSize);
		resizeCache();
		this.pageIndex = 0;
		structChange();
	}

	public void resetCachePageCount(int pageCount) {
		this.cachePageCount = pageCount;
		resizeCache();
	}

	public void removePkByIndexs(int[] indexs) {
		for (int i = 0; i < indexs.length; ++i) {
			String pk = (String) this.objectPks.get(indexs[i] + getPageSize()
					* getPageIndex());
			this.contentMap.remove(pk);
			this.objectPks.remove(pk);
			if (this.selectedPks.containsKey(pk))
				this.selectedPks.remove(pk);
		}
	}

	public void removePks(String[] pks) {
		if ((pks == null) || (pks.length == 0))
			return;
		for (String pk : pks) {
			this.contentMap.remove(pk);
			this.objectPks.remove(pk);
			if (this.selectedPks.containsKey(pk))
				this.selectedPks.remove(pk);
		}
	}

	public void insertPkByIndexs(int index, String pk, Object obj) {
		int insertIndex = index + getPageSize() * getPageIndex();
		this.objectPks.add(insertIndex, pk);
		this.contentMap.put(pk, obj);
	}

	public void addSelectPk(String[] pks) {
		if ((pks != null) && (pks.length > 0))
			for (String pk : pks)
				this.selectedPks.put(pk, pk);
	}

	public void removeSelectPk(String[] pks) {
		if ((pks != null) && (pks.length > 0))
			for (String pk : pks)
				this.selectedPks.remove(pk);
	}

	public String[] getAllSelectedPks() {
		return ((String[]) this.selectedPks.keySet().toArray(new String[0]));
	}

	public Object[] getAllSelectedObjects() throws BusinessException {
		String[] pks = getAllSelectedPks();
		if ((pks == null) || (pks.length == 0)) {
			return null;
		}
		List indexList = new ArrayList();
		List pkList = new ArrayList();
		Object[] objs = new Object[pks.length];
		for (int i = 0; i < objs.length; ++i) {
			if (!(this.contentMap.containsKey(pks[i]))) {
				pkList.add(pks[i]);
				indexList.add(Integer.valueOf(i));
				objs[i] = null;
			} else {
				objs[i] = this.contentMap.get(pks[i]);
			}
		}

		if (!(pkList.isEmpty())) {
			Object[] getObjs = getPaginationQueryService().queryObjectByPks(
					(String[]) pkList.toArray(new String[0]));
			for (int i = 0; i < getObjs.length; ++i) {
				Integer index = (Integer) indexList.get(i);
				String pk = (String) pkList.get(i);
				Object value = getObjs[i];

				if (value == null) {
					this.objectPks.remove(pk);
					this.selectedPks.remove(pk);
					objs[index.intValue()] = null;
				} else {
					this.contentMap.put(pk, value);
					objs[index.intValue()] = value;
				}
			}
		}

		List objList = new ArrayList();
		for (int i = 0; i < objs.length; ++i) {
			if (objs[i] != null)
				objList.add(objs[i]);
		}
		return objList.toArray();
	}

	public void setAllSelectedPks() {
		if ((this.objectPks == null) || (this.objectPks.isEmpty()))
			return;
		addSelectPk((String[]) this.objectPks.toArray(new String[0]));
	}

	public void clearSelectedPks() {
		this.selectedPks.clear();
	}

	public void update(String pk, Object obj) {
		this.contentMap.put(pk, obj);
	}

	public void next() throws BusinessException {
		if (hasNext())
			setPageIndex(getPageIndex() + 1);
	}

	public void last() throws BusinessException {
		if (hasPre())
			setPageIndex(getPageIndex() - 1);
	}

	public void toTheLast() throws BusinessException {
		if (!(isTheLast()))
			setPageIndex(getPageCount() - 1);
	}

	public void toTheFirst() throws BusinessException {
		if (!(isTheFirst()))
			setPageIndex(0);
	}

	public boolean hasNext() {
		return (this.pageIndex < this.pageCount - 1);
	}

	public boolean isTheLast() {
		return (this.pageIndex == this.pageCount - 1);
	}

	public boolean hasPre() {
		return (this.pageIndex > 0);
	}

	public boolean isTheFirst() {
		return (this.pageIndex == 0);
	}

	public boolean isSelected(String pk) {
		return this.selectedPks.containsKey(pk);
	}

	private void structChange() throws BusinessException {
		prepareData();
		resizePageCount();
		fireStructChanged();
		fireDataReady();
	}

	private void resizePageCount() {
		if ((this.objectPks == null) || (this.objectPks.size() == 0)) {
			this.pageCount = 0;
		} else {
			int totalCount = this.objectPks.size();
			if (totalCount % this.pageSize == 0) {
				this.pageCount = (totalCount / this.pageSize);
			} else
				this.pageCount = (totalCount / this.pageSize + 1);
		}
	}

	protected void prepareData() throws BusinessException {
		if (getPaginationQueryService() == null) {
			throw new IllegalStateException(NCLangRes.getInstance().getStrByID(
					"uif2", "PaginationModel-000000"));
		}
		if ((this.objectPks == null) || (this.objectPks.size() == 0)) {
			return;
		}
		List needQryPks = getNeedQryList();
		while (!(needQryPks.isEmpty())) {
			int needQryCount = this.pageSize - needQryPks.size();
			if ((needQryCount > 0)
					&& (getUpperIndex() < this.objectPks.size() - 1)) {
				for (int i = getUpperIndex() + 1; i < this.objectPks.size(); ++i) {
					if ((needQryPks.size() < getPageSize())
							&& (!(this.contentMap.containsKey(this.objectPks
									.get(i)))))
						needQryPks.add(this.objectPks.get(i));
					if (needQryPks.size() == getPageSize()) {
						break;
					}
				}
			}

			Object[] objs = getPaginationQueryService().queryObjectByPks(
					(String[]) needQryPks.toArray(new String[0]));
			for (int i = 0; i < objs.length; ++i) {
				if (i >= needQryPks.size()) {
					break;
				}

				if (objs[i] == null) {
					this.objectPks.remove(needQryPks.get(i));
				} else {
					this.contentMap.put((String) needQryPks.get(i), objs[i]);
				}
			}

			needQryPks = getNeedQryList();
		}
	}

	public void refresh() throws BusinessException {
		this.contentMap.clear();
		this.selectedPks.clear();
		setPageIndex(0);
	}

	public void pageRefresh() throws BusinessException {
		this.contentMap.clear();
		this.selectedPks.clear();
		setPageIndex(getPageIndex());
	}

	public void addPaginationModelListener(IPaginationModelListener listener) {
		this.listenerList.remove(IPaginationModelListener.class, listener);
		this.listenerList.add(IPaginationModelListener.class, listener);
	}

	public void removePaginationModelListener(IPaginationModelListener l) {
		this.listenerList.remove(IPaginationModelListener.class, l);
	}

	public IPaginationQueryService getPaginationQueryService() {
		return this.paginationQueryService;
	}

	public void setPaginationQueryService(IPaginationQueryService queryService) {
		this.paginationQueryService = queryService;
	}

	public List<String> getObjectPks() {
		return this.objectPks;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		resizeCache();
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public int getPageIndex() {
		return this.pageIndex;
	}

	public int getPageCount() {
		return this.pageCount;
	}

	public int getCachePageCount() {
		return this.cachePageCount;
	}

	public void setCachePageCount(int cachePageCount) {
		this.cachePageCount = cachePageCount;
	}

	public int getPageIndexofPk(String pk) {
		if ((StringUtil.isEmptyWithTrim(pk))
				|| (!(this.objectPks.contains(pk))))
			return -1;
		int index = this.objectPks.indexOf(pk);
		if (getPageSize() == 0)
			return -1;
		return (index / getPageSize());
	}

	private List<String> getNeedQryList() {
		List needQryPks = new ArrayList();
		int upperIndex = getUpperIndex();
		for (int i = this.pageIndex * this.pageSize; i <= upperIndex; ++i) {
			String pk = (String) this.objectPks.get(i);
			if (!(this.contentMap.containsKey(pk))) {
				needQryPks.add(pk);
			}
		}
		return needQryPks;
	}

	private int getUpperIndex() {
		int upperIndex = ((this.pageIndex + 1) * this.pageSize - 1 > this.objectPks
				.size() - 1) ? this.objectPks.size() - 1 : (this.pageIndex + 1)
				* this.pageSize - 1;
		return upperIndex;
	}

	private void fireStructChanged() {
		IPaginationModelListener[] ls = (IPaginationModelListener[]) this.listenerList
				.getListeners(IPaginationModelListener.class);
		for (IPaginationModelListener listener : ls)
			listener.onStructChanged();
	}

	private void fireDataReady() {
		IPaginationModelListener[] ls = (IPaginationModelListener[]) this.listenerList
				.getListeners(IPaginationModelListener.class);
		for (IPaginationModelListener listener : ls)
			listener.onDataReady();
	}

	private void resizeCache() {
		if (this.contentMap != null) {
			Map tempMap = this.contentMap;
			this.contentMap = new LRUMap(getPageSize() * getCachePageCount()
					+ 1);
			this.contentMap.putAll(tempMap);
		}
	}

	public int getMaxPageSize() {
		return this.maxPageSize;
	}

	public void setMaxPageSize(int maxPageSize) {
		this.maxPageSize = maxPageSize;
	}

	public ModelDataDescriptor getCurrentDataDescriptor() {
		return this.currentDataDescriptor;
	}
}