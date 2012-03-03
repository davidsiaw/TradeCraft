package nl.armeagle.TradeCraft;

import org.bukkit.inventory.ItemStack;

/**
 * 
 * @author ArmEagle
 * Store item type(ID) and optionally the data bit
 */
public class TradeCraftItem implements Comparable<TradeCraftItem> {
	public int id;
	public short data;
	
	TradeCraftItem(int id) {
		this(id, (short)0);
	}
	TradeCraftItem(int id, int data) {
		this(id, new Integer(data).shortValue());
	}
	TradeCraftItem(int id, short data) {
		this.id = id;
		this.data = data;
	}
	
	/**
	 * @param compare
	 * @throws NullPointerException if compare is null
	 * @return default < 0 > compare values
	 */
	@Override public int compareTo(TradeCraftItem compare) {
		if ( this == compare ) {
			return 0;
		}
		if ( this.id < compare.id ) {
			return -1;
		} else if ( this.id > compare.id ) {
			return 1;
		} else {
			if ( this.data < compare.data ) {
				return -1;
			} else if ( this.data > compare.data ) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	@Override public boolean equals(Object compare) {
		return (compare == null ? false : (compare instanceof TradeCraftItem? this.compareTo((TradeCraftItem)compare) == 0 : false));
	}
	@Override public int hashCode() {
		return this.id * 32768 + this.data;
	}

	@Override public String toString() {
		return "TradeCraftItem("+ this.id +";"+ this.data +")";
	}
	
	/** 
	 * @return The ID and data value of the item as a string 
	 */
	public String toShortString() {
		if ( this.data == 0 ) {
			return String.valueOf(this.id);
		} else {
			return this.id +";"+ this.data;
		}
	}
	
	private String dress(String str) {
		return str
				.substring(0, 1).toUpperCase()
				.concat(str.substring(1).toLowerCase());
	}
	
	public String toPrettyString() {
		ItemStack stack = new ItemStack(this.id, 1, this.data);
		String str = stack.getType().toString();

    	String[] words = str.split("\\(")[0].split("[ _]+");
    	String name = dress(words[0]);
    	for ( int word_ind = 1; word_ind < words.length; word_ind++ ) {
    		String word = words[word_ind];
    		name = name
    				.concat(" ")
    				.concat(dress(word));
    	}
    	
    	return name;
	}
	
}
