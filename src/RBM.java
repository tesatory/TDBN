/*
 * Class representing a Restricted Boltzmann Machine
 */
import java.util.Random;


public class RBM {
	Random rand;
	layer dummyLayer; // dummy layer with one unit with fixed value
	layer visibleLayer;
	layer hiddenLayer;
	edgeLayer vhEdges;
	edgeLayer vBias;
	edgeLayer hBias;
	double lrate = 0.001; // learning rate
	double mom = 0; // momentum
	int batchsz = 20; // batch size
	int batchcnt = 0;
	
	RBM(int vNum,boolean vReal, 
			int hNum,boolean hReal){
		if(vReal){
			visibleLayer = new relayer(vNum); // Gaussian units
		}else{
			visibleLayer = new bilayer(vNum); // binary units
		}
		if(hReal){
			hiddenLayer = new relayer(hNum);
		}else{
			hiddenLayer = new bilayer(hNum);
		}

		dummyLayer = new bilayer(1);
		dummyLayer.get(0).val = 1;

		vhEdges = new edgeLayer(visibleLayer, hiddenLayer);
		vBias = new edgeLayer(dummyLayer, visibleLayer);
		hBias = new edgeLayer(dummyLayer, hiddenLayer);
		
		rand = new Random();
	}

	void train(){
		hUpdate(true);
		edgesCalc(lrate / batchsz);
		gibbsSampling(1, true, false);
		edgesCalc(- lrate / batchsz);
		update();
	}
	
	synchronized void update(){
		batchcnt++;
		if(batchcnt == batchsz){
			edgesUpdate(mom);
			batchcnt = 0;
		}
	}
	
	void gibbsSampling(int sample, boolean lastDeterministic,boolean updateHiddenFirst){
		for(int i = 0; i < sample; i++){
			if(updateHiddenFirst){
				hUpdate(true);
				vUpdate(true);
			}else{
				vUpdate(true);
				hUpdate(true);
			}
		}
		if(lastDeterministic){
			if(updateHiddenFirst){
				hUpdate(false);
				vUpdate(false);
			}else{
				vUpdate(false);
				hUpdate(false);
			}
		}
	}
	
	void edgesCalc(double rate){
		vhEdges.calc(rate);
		vBias.calc(rate);
		hBias.calc(rate);
	}
	
	void edgesUpdate(double momentum){
		vhEdges.update(momentum);
		vBias.update(momentum);
		hBias.update(momentum);
	}
	void hUpdate(boolean sample){
		hBias.sendUp();
		vhEdges.sendUp();
		hiddenLayer.update();
		if(sample) hiddenLayer.sample();
	}
	void vUpdate(boolean sample){
		vBias.sendUp();
		vhEdges.sendDown();
		visibleLayer.update();
		if(sample) visibleLayer.sample();
	}
}
