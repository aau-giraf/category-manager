package dk.aau.cs.giraf.categorymanager.test;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.util.ArrayList;

import dk.aau.cs.giraf.categorymanager.PictogramAdapter;
import dk.aau.cs.giraf.oasis.lib.models.Pictogram;

/**
 * Created on 14/04/2015.
 */
public class PictogramAdapterTest extends ApplicationTestCase<Application> {
    private PictogramAdapter adapter;

    public PictogramAdapterTest() {
        super(Application.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ArrayList<Pictogram> data = new ArrayList<Pictogram>();

        Pictogram p1, p2;

        p1 = new Pictogram();
        p1.setName("p1");

        p2 = new Pictogram();
        p2.setName("p2");

        data.add(p1);
        data.add(p2);

        adapter = new PictogramAdapter(data, getContext());
    }

    public void testGetItem() {
        assertEquals("p1 was expected.", p1.getName(), ((Pictogram) adapter.getItem(0)).getName());
    }

    public void testGetItemId() {
        assertEquals("Wrong ID.", 0, adapter.getItemId(0));
    }

    public void testGetCount() {
        assertEquals("Amount incorrect.", 2, adapter.getCount());
    }
}