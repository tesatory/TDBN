import java.util.Random;

abstract class node{
	double buff;
	double val;
	boolean fixed = false;
	layer layer;
	
	node(layer l){
		layer = l;
		buff = 0;
		val = 0;
	}
	
	abstract void update();
	abstract void sample();
}

class binode extends node{
	binode(layer l) {
		super(l);
	}
	void update(){
		if(!fixed) val = 1. / (1. + Math.exp(-buff / layer.temp));
		buff = 0;
	}
	void sample(){
		if(!fixed){
			if(Math.random() < val)
				val = 1;
			else
				val = 0;
		}
	}
}

class renode extends node{
	renode(layer l) {
		super(l);
	}
	static Random rand = new Random();
	void update(){
		if(!fixed) val = buff;
		buff = 0;
	}
	void sample(){
		val += rand.nextGaussian();
	}
}