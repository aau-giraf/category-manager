package dk.aau.cs.giraf.pictoadmin;

import java.util.ArrayList;

import dk.aau.cs.giraf.pictogram.Pictogram;

/**
 * Never used
 *@author SW605f13 Parrot-group
 */
public class CheckoutGridHandler {
	ArrayList<Pictogram> checkoutList = new ArrayList<Pictogram>();
	
	public CheckoutGridHandler(ArrayList<Pictogram> list) {
		this.checkoutList = list;
	}
	
	/**
	 * Assess the checkout gridview and load the pictograms into an ArrayList
	 * @return ArrayList of checkout pictograms
	 */
	public long[] getCheckoutList() {
		long[] checkout = new long[checkoutList.size()];
		int i = 0;
		for(Pictogram p : checkoutList){
			checkout[i] = p.getPictogramID();
			i++;
		}
		
		return checkout;
	}
}
