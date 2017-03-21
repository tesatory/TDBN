/*
 * Class for reading .amc motion files
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class dataset {

	boneGroup bodyParts[];
	int modeNum;
	
	// we divided joint-angles into 5 parts, and trained 5 separate RBMs with them
	String bodyBones[][] = {
			{"root","lowerback","upperback","thorax","lowerneck","upperneck","head"},
			{"rclavicle","rhumerus","rradius","rwrist","rhand","rfingers","rthumb"},
			{"lclavicle","lhumerus","lradius","lwrist","lhand","lfingers","lthumb"},
			{"rfemur","rtibia","rfoot","rtoes"},
			{"lfemur","ltibia","lfoot","ltoes"}
			};

	dataset(String path[]) throws IOException{
		BufferedReader amc;
		String line;
		String[][][] data;
	
		modeNum = path.length;
		data = new String[path.length][][];
		
		for(int pi = 0; pi < path.length; pi++){
			int ln = 0;
			amc = new BufferedReader(new FileReader(path[pi]));	    
			while((line = amc.readLine()) != null){
				ln++;
			}
			data[pi] = new String[ln][];
			amc = new BufferedReader(new FileReader(path[pi]));
			ln = 0;
			while((line = amc.readLine()) != null){
				data[pi][ln] = line.split(" ");
				ln++;
			}
		}
		
		bodyParts = new boneGroup[bodyBones.length];
		for(int i = 0; i < bodyParts.length; i++){
			bodyParts[i] = new boneGroup(bodyBones[i]);
		}

		for(int i = 0; i < bodyParts.length; i++){
			bodyParts[i].readData(data);
		}
	}

	int getFrameLength(int mode){
		return bodyParts[0].getFrameLength(mode);
	}
	
	int getTotalFrameLength(){
		return bodyParts[0].getTotalFrameLength();
	}
	int getDOF(){
		int DOF = 0;
		for(int i = 0; i < bodyParts.length; i++){
			DOF += bodyParts[i].DOF;
		}
		return DOF;
	}
	
	double getOrigVal(int mode,int fr,int di){
		for(int i = 0; i < bodyParts.length; i++){
			if(di < bodyParts[i].DOF ) return bodyParts[i].getOrigVal(mode,fr, di);
			di -= bodyParts[i].DOF;
		}
		throw new IndexOutOfBoundsException();
	}

	double getOrigVal(int part,int mode,int fr,int di){
		return bodyParts[part].getOrigVal(mode,fr, di);
	}

	double getNormVal(int mode,int fr,int di){
		for(int i = 0; i < bodyParts.length; i++){
			if(di < bodyParts[i].DOF ) return bodyParts[i].getNormVal(mode,fr, di);
			di -= bodyParts[i].DOF;
		}
		throw new IndexOutOfBoundsException();
	}

	double getNormVal(int part,int mode,int fr,int di){
		return bodyParts[part].getNormVal(mode,fr, di);
	}
	
	double[][] getOrigData(int mode){
		double[][] body;
		double[][] part;
		int b = 0;
		body = new double[getFrameLength(mode)][getDOF()];
		for(int i = 0; i < bodyParts.length; i++){
			part = bodyParts[i].getOrigData(mode);
			
			for(int d = 0; d < part.length; d++){
				for(int c = 0; c < part[d].length; c++){
					body[d][b+c] = part[d][c];
				}
			}
			b += bodyParts[i].DOF;
		}
		return body;
	}
	
	double convertToOrig(int mode,int di,double val){
		for(int i = 0; i < bodyParts.length; i++){
			if(di < bodyParts[i].DOF ) return bodyParts[i].convertToOrig(mode, di, val);
			di -= bodyParts[i].DOF;
		}
		throw new IndexOutOfBoundsException();
	}
	
	double convertToOrig(int part,int mode,int di,double val){
		return bodyParts[part].convertToOrig(mode, di, val);	
	}
	
	// prints data in .amc format
	void printAMC(double data[][], StringBuilder sb){
		double d[];
		int b;
		for(int frame = 0; frame < data.length; frame++){
			sb.append((frame + 1) + "\n");
			
			b = 0;
			for(int i = 0; i < bodyParts.length; i++){
				d = new double[bodyParts[i].DOF];
				for(int j = 0; j < d.length; j++)
					d[j] = data[frame][j + b];
				bodyParts[i].printAMC(d, 0, sb);
				b += bodyParts[i].DOF;
			}
		}
	}
}
