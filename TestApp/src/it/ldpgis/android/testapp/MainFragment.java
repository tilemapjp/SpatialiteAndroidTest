package it.ldpgis.android.testapp;

import java.io.File;
import java.io.IOException;

import jsqlite.Database;
import jsqlite.Stmt;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainFragment extends SherlockFragment {

	ProgressDialog wait;

	private static void vDebug(String msg) {
		Log.v("TestApp", "[MainFragment] " + msg);
	}
	
	private static void wDebug(String msg) {
		Log.w("TestApp", "[MainFragment] " + msg);
	}
	
	private static void eDebug(String msg) {
		Log.e("TestApp", "[MainFragment] " + msg);
	}
	
	@Override
	public void onCreate(Bundle args) {
		super.onCreate(args);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		vDebug("[onCreateView] Function called");
		
		vDebug("[onCreateView] #### Copying application data...");
		try {
		
			AssetHelper.CopyAsset(this.getActivity(),
					this.getActivity().getFilesDir(),
					getString(R.string.test_db));
		
		} catch (IOException e) {
			eDebug("[onCreateView] Exception while doing CopyAsset(): " + e);
		}
		vDebug("[onCreateView] #### Data copied");
		

		StringBuilder sb = new StringBuilder();
		sb.append("<b>Test queries for SpatialIndex support</b><br/><br/>");
		
		File spatialDbFile = new File(this.getActivity().getFilesDir() + "/" + this.getActivity().getString(R.string.test_db));            
		vDebug("[onCreateView] ----- Database file used: " + spatialDbFile.getAbsolutePath());
		
		Database db = new jsqlite.Database();
		
		try {
			db.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                | jsqlite.Constants.SQLITE_OPEN_CREATE);
		
		} catch (Exception e) {
			sb.append("Exception: " + e + "<br/>");
			eDebug("[onCreateView] Exception: " + e);
			e.printStackTrace();
		}
		
		String[] queries = {
				"SELECT spatialite_version()",
				"SELECT proj4_version()",
				"SELECT geos_version()",
				"SELECT lwgeom_version()",
				"Select checkspatialindex()",
				"Select createspatialindex('regions','Geometry')",
				"select checkspatialindex()",
				"SELECT count(*) FROM regions",
				"SELECT count(*) FROM idx_regions_Geometry", 
				"SELECT count(*) FROM regions WHERE pk_uid IN (SELECT rowid FROM SpatialIndex WHERE \"f_table_name\"='regions' AND " +
						"\"f_geometry_column\"='Geometry' AND \"search_frame\" = " +
						"GeomFromEWKT(\"SRID=4326;POLYGON((12.02 41.52,19.38 42.70,19.38 35.70,11.30 36.06,12.02 41.52))\"))"
					 };
		
		for (int i = 0; i < queries.length; i++ ) {
			try {
				
				//String query = "Select count(*) from idx_regions_Geometry";
				String query = queries[i];
				sb.append("<i>Query</i>: " + query + "<br/>");
				wDebug("[onCreateView] ----- Query: " + query);
				
				Stmt stmt = db.prepare(query);
				if (stmt.step()) {
					wDebug("[onCreateView] ----- Count: " + stmt.column_string(0));
					sb.append("<i>Result</i>: " + stmt.column_string(0) + "<br/>");
				} else {
					sb.append("<i>No results</i><br/>");
				}
				
				stmt.close();
			} catch (Exception e) {
				sb.append("<b>Exception</b>: " + e + "<br/>");
				eDebug("[onCreateView] Exception: " + e);
				e.printStackTrace();
				
			}
			sb.append("<br/>");
		}
		
		/*
		try {
			String query = "SELECT count(*) FROM regions WHERE pk_uid IN (SELECT rowid FROM SpatialIndex WHERE \"f_table_name\"='regions' AND " +
					"\"f_geometry_column\"='Geometry' AND \"search_frame\" = GeomFromEWKT(\"SRID=4326;POLYGON((12.02 41.52,19.38 42.70,19.38 35.70,11.30 36.06,12.02 41.52))\"))";
			wDebug("[onCreateView] ----- Query: " + query);
			sb.append("<i>Query</i>: " + query + "<br/>");
			
			Stmt stmt = db.prepare(query);
			if (stmt.step()) {
				wDebug("[onCreateView] ----- Count: " + stmt.column_string(0));
				sb.append("<i>Result</i>: " + stmt.column_string(0) + "<br/>");
			} else {
				sb.append("<i>No results</i><br/>");
			}
			stmt.close();
			
		} catch (Exception e) {
			sb.append("<b>Exception</b>: " + e + "<br/>");
			eDebug("[onCreateView] Exception: " + e);
			e.printStackTrace();
			
		}
		*/
			
		View view = null;
		try {
			view = inflater.inflate(R.layout.mainfragment, container, false);
			TextView textview = (TextView) view.findViewById(R.id.textview);
			textview.setText(Html.fromHtml(sb.toString()));
			
		} catch (Exception e) {
			eDebug("[onCreateView] Exception: " + e);
		}
		return view;
	}

}
