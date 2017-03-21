import java.util.Random;
class edge{
	static Random rand = new Random();
	int wind;
	double w;
	double dw;
	double v;
	node n1;
	node n2;
	public edge(){
		w = rand.nextGaussian() * 0.01;
	}
}

class edgeLayer{
	edge[] edges;
	int count;
	public edgeLayer(layer l1,layer l2){
		count = l1.size() * l2.size();
		edges = new edge[count];
		int c = 0;
		for(int i1 = 0; i1 < l1.size(); i1++){
			for(int i2 = 0; i2 < l2.size(); i2++){
				edges[c] = new edge();
				edges[c].n1 = l1.get(i1);
				edges[c].n2 = l2.get(i2);
				c++;
			}
		}
	}
	public void calc(double rate){
		for(int i = 0; i < count; i++)
			edges[i].dw += edges[i].n1.val * edges[i].n2.val * rate;
	}
	public void sendUp(){
		for(int i = 0; i < count; i++)
			edges[i].n2.buff += edges[i].w * edges[i].n1.val;
	}
	public void sendDown(){
		for(int i = 0; i < count; i++)
			edges[i].n1.buff += edges[i].w * edges[i].n2.val;
	}
	public void update(double mom){
		for(int i = 0; i < count; i++){
			edges[i].v = mom * edges[i].v + edges[i].dw;
			edges[i].dw = 0;
			edges[i].w += edges[i].v;
			edges[i].w -= edges[i].w * 0.000002;
		}
	}
	
	public double getEnergy(){
		double e = 0;
		for(int i = 0; i < count; i++)
			e -= edges[i].w * edges[i].n1.val * edges[i].n2.val;
		
		return e;
	}
}