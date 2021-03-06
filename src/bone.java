/*
 * Class for bones
 */

public class bone {
	double SD[];
	double mean[];
	int DOF;
	String name;
	
	double dataOrig[][][];
	double dataNorm[][][];
	
	bone(String name){
		this.name = name;
	}
	
	void readData(String[][][] data){
		//count frames
		dataOrig = new double[data.length][][];
		for(int mi = 0; mi < data.length; mi++){
			int fn = 0;
			for(int li = 0; li < data[mi].length; li++){
				if(data[mi][li][0].equals(name)){
					fn++;
					DOF = data[mi][li].length - 1;
					if(name.equals("root")) DOF = 3;
				}
			}
			
			dataOrig[mi] = new double[fn][DOF];

			//read values
			fn = 0;
			for(int li = 0; li < data[mi].length; li++){
				if(data[mi][li][0].equals(name)){
					for(int i = 0; i < DOF; i++){
						dataOrig[mi][fn][i] = Double.parseDouble(data[mi][li][i+1]);
					}
					if(name.equals("root")){
						for(int i = 0; i < DOF; i++){
							dataOrig[mi][fn][i] = Double.parseDouble(data[mi][li][i+1 + 3]);
						}
					}
					fn++;
				}
			}
		}
		
		//calc mean
		mean = new double[DOF];
		for(int mi = 0; mi < dataOrig.length; mi++){
			for(int fi = 0; fi < dataOrig[mi].length; fi++){
				for(int i = 0; i < DOF; i++)
					mean[i] += dataOrig[mi][fi][i];
			}
		}
		
		for(int i = 0; i < DOF; i++)
			mean[i] /= getTotalFrameLength();
		
		//calc standard deviation
		SD = new double[DOF];
		for(int mi = 0; mi < dataOrig.length; mi++){
			for(int fi = 0; fi < dataOrig[mi].length; fi++){
				for(int i = 0; i < DOF; i++)
					SD[i] += Math.pow(dataOrig[mi][fi][i] - mean[i],2);
			}
		}
		for(int i = 0; i < DOF; i++)
			SD[i] = Math.sqrt(SD[i] / (getTotalFrameLength() - 1));
		
		//calc norm data
		dataNorm = new double[dataOrig.length][][];
		for(int i = 0; i < dataNorm.length; i++)
			dataNorm[i] = new double[dataOrig[i].length][dataOrig[i][0].length];
		
		for(int mi = 0; mi < dataOrig.length; mi++){
			for(int fi = 0; fi < dataOrig[mi].length; fi++){
				for(int i = 0; i < DOF; i++)
					dataNorm[mi][fi][i] = (dataOrig[mi][fi][i] - mean[i]) / SD[i];
			}
		}
	}

	int getModeNum(){
		return dataOrig.length;
	}
	
	int getFrameLength(int mode){
		return dataOrig[mode].length;
	}

	int getTotalFrameLength(){
		int totfr = 0;
		for(int mode = 0; mode < getModeNum(); mode++)
			totfr += getFrameLength(mode);
		return totfr;
	}
	
	void printAMC(double data[],int offset, StringBuilder sb){
		sb.append(name);
		if(name.equals("root"))
			sb.append(" 0 0 0");
		for(int i = 0; i < DOF; i++)
			sb.append(" " + (data[offset + i]*SD[i] + mean[i]));
		sb.append("\n");
	}
}
