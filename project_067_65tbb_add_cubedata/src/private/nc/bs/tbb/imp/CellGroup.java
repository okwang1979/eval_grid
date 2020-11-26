package nc.bs.tbb.imp;

import java.util.ArrayList;
import java.util.List;

import nc.vo.mdm.cube.DataCell;

public class CellGroup {
	private List<DataCell> persionCells = new ArrayList<>();
	
	private List<DataCell> otherCells  = new ArrayList<DataCell>();

	public List<DataCell> getPersionCells() {
		return persionCells;
	}

	public List<DataCell> getOtherCells() {
		return otherCells;
	}
	
	public void addPersionCell(DataCell cell){
		this.persionCells.add(cell);
	}
	
	public void addOtherCell(DataCell cell){
		this.otherCells.add(cell);
	}
	
	

}
