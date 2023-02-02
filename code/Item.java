
public class Item implements Comparable<Item>{
	Integer name;
	int utility;
	int remainUtility;
	
	public Item(Integer name, Integer utility) {
		this.name = name;
		this.utility = utility;
		this.remainUtility = 0;
	}
	
	
	public void setRemainUtility(int remainUtility) {
		this.remainUtility = remainUtility;
	}


	@Override
	public String toString() {
		return "Item [name=" + name + ", utility=" + utility + "]";
	}
	
    @Override
    public int compareTo(Item o) {
        return this.name.compareTo(o.name);
    }
}
