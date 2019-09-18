package nc.report.hbbb.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MathClass {

 

	private List<List<Integer>> results = new ArrayList<>();

	public static List<String> getTotal( double[] values,double mixValue, int maxPrint){
		MathClass math = new MathClass();
		math.printOther(new ArrayList<Integer>(),values,mixValue);
		List<MathInfo>  infos = new ArrayList<>();
		for(List<Integer> fValue:math.results){
			MathInfo info = new MathInfo(fValue, values);
			infos.add(info);
		}
		Collections.sort(infos);
		List<String> rtn = new ArrayList<String>();
		
		for(int i=0;i<infos.size()&&i<maxPrint;i++){
			rtn.add(infos.get(i).info);
		}
		 
		return rtn;
	}

	private MathClass() {

	}

	private void printOther(List<Integer> haveValue, double[] values,
			double mixValue) {
		if (values.length > 2) {
			double[] otherValues = Arrays.copyOfRange(values, 1,
					values.length);
			int max = (new Double(Math.ceil(mixValue / values[0]))).intValue();
			for (int i = 0; i <= max; i++) {
				List<Integer> tempValue = new ArrayList<>(haveValue);
				if (mixValue > i * values[0]) {
					tempValue.add(i);
					printOther(tempValue, otherValues, mixValue - i * values[0]);

				} else {
					tempValue.add(i);
					for (int j = 0; j < values.length - 1; j++) {
						tempValue.add(0);
					}
					results.add(tempValue);
					break;
				}
			}
		} else if (values.length == 2) {

			double otherValue = values[1];
			int max = (new Double(Math.ceil(mixValue / values[0]))).intValue();
			for (int i = max; i >= 0; i--) {
				List<Integer> tempValue = new ArrayList<>(haveValue);
				tempValue.add(i);
				if (mixValue < i * values[0]) {

					tempValue.add(0);

				} else {
					tempValue.add(getShopNumber(mixValue - i * values[0],
							otherValue));
				}
				results.add(tempValue);

			}

		}

	}

	private int getShopNumber(double maxNmuber, double proNumber) {
		int max = (new Double(Math.ceil(maxNmuber / proNumber))).intValue();

		return max;
	}



}
class MathInfo implements Comparable<MathInfo> {
	double value=0;
	String info="";
	
 public	MathInfo(List<Integer> fValue,double[] values){
		if(fValue.size()!=values.length){
			throw new RuntimeException("计算错误参数不一致");
		}
		int totalInt = 0;
		for(int i=0;i<fValue.size();i++){
			if(fValue.get(i)==0){
				continue;
			}
			totalInt = totalInt+fValue.get(i);
			value = value+fValue.get(i)*values[i];
			info = info+fValue.get(i)+"*"+values[i]+";";
		}
		
		info = value+":"+totalInt+":"+info;
		
	}

	@Override
	public int compareTo(MathInfo o) {

		return new Double(value).compareTo(new Double(o.value));
	}

}
