import java.io.*;
import java.util.*;

class TDBN{
	Random rand;

	RBM rbm1[];
	CRBM rbm2;
	
	dataset ds;

	int degree = 3;
	int step = 3;

	double output[][];
	double outputB[][];
	double outputhidden[][][];
	double outputhidden2[][];

	public TDBN(String[] files) throws IOException{
		ds = new dataset(files);
		rand = new Random();

		rbm1 = new RBM[ds.bodyParts.length];
		
		for(int i = 0; i < rbm1.length; i++)
			rbm1[i] = new RBM(ds.bodyParts[i].DOF, true, 30, false);

		int hn = 0;
		for(int i = 0; i < rbm1.length; i++)
			hn += rbm1[i].hiddenLayer.size();
		
		rbm2 = new CRBM(
				hn
				, false, 
				15, false, degree);
		
		for(int i = 0; i < rbm2.hiddenLayer.size(); i++)
			rbm2.hiddenLayer.get(i).fixed = true;
	}

	
	public void train(int train_epoch){
		int mode = 0, chosen;

		for(int epoch = 0; epoch < train_epoch;epoch++){
			if(epoch % 10 == 0)
				for(int i = 0; i < rbm1.length; i++)
					rbm1[i].hiddenLayer.temp *= 0.99;

			if(epoch % 10 == 0) System.err.println("epoch " + epoch);

			if(epoch > 5)
				for(int i = 0; i < rbm1.length; i++)
					rbm1[i].mom = 0.9;

			for(int t = 0; t < ds.getTotalFrameLength(); t++){
				mode = rand.nextInt(ds.modeNum);
				chosen = rand.nextInt(ds.getFrameLength(mode));
				initMode(mode, chosen);
				for(int i = 0; i < rbm1.length; i++)
					rbm1[i].train();
			}
		}
	}

	public void train2(int train_epoch){
		int mode = 0, chosen;
		for(int i = 0; i < rbm1.length; i++)
			rbm1[i].hiddenLayer.temp = 0.6;
		
		for(int epoch = 0; epoch < train_epoch;epoch++){
			System.err.println("epoch " + epoch);
			if(epoch > 5) rbm2.mom =0.9;

			if(epoch % 2 == 0){
				rbm2.visibleLayer.temp *= 0.99;
				rbm2.hiddenLayer.temp *= 0.99;
			}
			
			for(int t = 0; t < ds.getTotalFrameLength(); t++){
				mode = rand.nextInt(ds.modeNum);
				chosen = rand.nextInt(ds.getFrameLength(mode) - step * (degree));
				initMode2(mode, chosen);
				rbm2.train();
			}
		}
	}
		
	public void initRealtime(){
		for(int i = 0; i < rbm1.length; i++)
			rbm1[i].hiddenLayer.temp = 0.6;
		rbm2.visibleLayer.temp = 0.6;
		rbm2.hiddenLayer.temp = 0.6;
		int recordLen = 200;
		output = new double[recordLen][ds.getDOF()];
		outputB = new double[1][ds.getDOF()];
		outputhidden = new double[rbm1.length][][];
		for(int i = 0; i < rbm1.length; i++)
			outputhidden[i] = new double[recordLen][rbm1[i].hiddenLayer.size()];
		outputhidden2 = new double[recordLen][rbm2.hiddenLayer.size()];

		initMode2(1, 0);
	}
	
	public String[] stepRealtime(double control){
		for(int i = 0; i < rbm2.hiddenLayer.size(); i++){
			rbm2.hiddenLayer.get(i).val = control;
		}

		rbm2.vUpdate(false);

		transferValuesDown();
		for(int i = 0; i < rbm1.length; i++)
			rbm1[i].vUpdate(false);
		
		
		for(int frame = outputhidden2.length-1; frame > 0; frame--){
			for(int i = 0; i < output[frame].length; i++)
				output[frame][i] = output[frame-1][i];
			for(int j = 0; j < outputhidden.length; j++)
				for(int i = 0; i < outputhidden[j][frame].length; i++)
					outputhidden[j][frame][i] = outputhidden[j][frame-1][i];
			for(int i = 0; i < outputhidden2[frame].length; i++)
				outputhidden2[frame][i] = outputhidden2[frame-1][i];			
		}
		
		int b = 0;
		for(int i = 0; i < rbm1.length; i++){
			for(int j = 0; j < rbm1[i].visibleLayer.size(); j++, b++){
				output[0][b] = rbm1[i].visibleLayer.get(j).val;
				outputB[0][b] = rbm1[i].visibleLayer.get(j).val;
			}
		}
		for(int i = 0; i < rbm1.length; i++)
			for(int j = 0; j < rbm1[i].hiddenLayer.size(); j++)
				outputhidden[i][0][j] = rbm1[i].hiddenLayer.get(j).val;

		for(int i = 0; i < rbm2.hiddenLayer.size(); i++)
			outputhidden2[0][i] = rbm2.hiddenLayer.get(i).val;
		
		
		rbm2.passValues();
		StringBuilder sb = new StringBuilder();
		ds.printAMC(outputB, sb);
		
		return sb.toString().split("\n");
	}

	public void initMode(int mode,int chosen){
		for(int i = 0; i < rbm1.length; i++)
			for(int vnum = 0; vnum < rbm1[i].visibleLayer.size(); vnum++)
				rbm1[i].visibleLayer.get(vnum).val = ds.getNormVal(i,mode, chosen, vnum);
	}

	public void initMode2(int mode,int chosen){	
		for(int k = 0; k < degree + 1; k++){
			rbm2.passValues();
			initMode(mode,chosen);
			
			for(int i = 0; i < rbm1.length; i++)
				rbm1[i].hUpdate(false);

			transferValuesUp();
			chosen += step;
		}		
		
		if(mode < 5){
			for(int i = 0; i < rbm2.hiddenLayer.size(); i++)
				rbm2.hiddenLayer.get(i).val = 0;
		}else{
			for(int i = 0; i < rbm2.hiddenLayer.size(); i++)
				rbm2.hiddenLayer.get(i).val = 1;
		}
	}

	void transferValuesUp(){
		int n = 0;
		for(int j = 0; j < rbm1.length; j++){
			for(int i = 0; i < rbm1[j].hiddenLayer.size(); i++){
				rbm2.visibleLayer.get(n).val = rbm1[j].hiddenLayer.get(i).val;
				n++;
			}
		}
	}
	
	void transferValuesDown(){
		int n = 0;
		for(int j = 0; j < rbm1.length; j++){
			for(int i = 0; i < rbm1[j].hiddenLayer.size(); i++){
				rbm1[j].hiddenLayer.get(i).val = rbm2.visibleLayer.get(n).val;
				n++;
			}
		}
	}
	
	static void printArray(double data[][]){
		for(int i = 0; i < data.length; i++){
			for(int j = 0; j < data[i].length; j++)
				System.out.print(data[i][j] + " ");
			System.out.println();
		}	
	}

}


