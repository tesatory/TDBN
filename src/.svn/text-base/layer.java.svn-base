abstract class layer{
	node[] nodes;
	int width;
	double temp = 1;
	
	public node get(int i){
		return nodes[i];
	}
	public node get(int x,int y){
		return nodes[width * y + x];
	}
	public int size(){
		return nodes.length;
	}
	public void update(){
		for(int i = 0; i < size(); i++)
			nodes[i].update();
	}
	public void sample(){
		for(int i = 0; i < size(); i++)
			nodes[i].sample();
	}
}
class bilayer extends layer{
	public bilayer(int sz){
		nodes = new node[sz];
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = new binode(this);
	}
	public bilayer(int _width,int _height){
		int sz = _width * _height;
		width = _width;
		nodes = new node[sz];
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = new binode(this);
	}
}
class relayer extends layer{
	public relayer(int sz){
		nodes = new node[sz];
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = new renode(this);
	}
	public relayer(int _width,int _height){
		int sz = _width * _height;
		width = _width;
		nodes = new node[sz];
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = new renode(this);
	}    
}

