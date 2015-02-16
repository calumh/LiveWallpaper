package com.damian.main;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;
import damian.shim.R;

public class LiveWallpaperSettings extends PreferenceActivity
	implements SharedPreferences.OnSharedPreferenceChangeListener
{
	Preference button;
	Preference button2;
	SharedPreferences.Editor editor;
	ListPreference listPreference;
	EditTextPreference bottomPref;
	EditTextPreference statPref;
	@Override
	protected void onCreate(Bundle icicle)
	{	Preference button3;
		
		super.onCreate(icicle);
		getPreferenceManager().setSharedPreferencesName(LiveWallpaper.SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.livewallpaper_settings);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		//Share Prefences editor
		editor = getPreferenceManager().getSharedPreferences().edit();
		//img folder button
		button = (Preference)findPreference("button");
		if(getPreferenceManager().getSharedPreferences().getString("imgLoc", "Oliver").equals("Oliver")){
			button.setSummary("Labeled 'shime1' to 'shime46'");
		}else{
			button.setSummary("Labeled 'shime1' to 'shime46': \n"+getPreferenceManager().getSharedPreferences().getString("imgLoc", "Oliver"));
		}		
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				folderChoose();
				return true;
			}
		});
		//background button
		button2 = (Preference)findPreference("button2");
		if(getPreferenceManager().getSharedPreferences().getString("backLoc", "black").equals("black")){
			button2.setSummary("Select an image for the background");
		}else{
			button2.setSummary("Select an image for the background: \n"+getPreferenceManager().getSharedPreferences().getString("backLoc", "black"));
		}
		button2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Display display = getWindowManager().getDefaultDisplay(); 
		        int width = display.getWidth();
		        int height = display.getHeight();
		        Toast.makeText(getBaseContext(), "Select Image - " + (width) + " x " + height , Toast.LENGTH_LONG).show(); 
		        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK); 
		        photoPickerIntent.setType("image/*");
		        startActivityForResult(photoPickerIntent, 1);		       
		        return true;
			}
			

		});

		listPreference = (ListPreference) findPreference("listPref");
        String currentValue = listPreference.getValue();
        currentValue = listPreference.getValue();
        if(currentValue == null){
        updatePosPrefs("Center");	
        }else{
        updatePosPrefs(currentValue);    
        }
        listPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            	updatePosPrefs(newValue.toString());
                return true;
            }       
        });    
		
		
    	CharSequence currText = listPreference.getEntry();
    	String currValue= listPreference.getValue();
		listPreference.setSummary("Choose the position of the background relative to the screen: \n"+currText);
      	editor.putString("pos", currValue);
      	editor.commit();
//		listPreference.setSummary(currText);
//      	editor.putString("pos", currValue);
//      	editor.commit();
		
		//bottom height 
      	bottomPref = (EditTextPreference) findPreference("botHeight");
      	bottomPref.setSummary("Change the floor height. Might be necessary for some custom launchers: \n"+getPreferenceManager().getSharedPreferences().getInt("bot", 0));
//        updateBotPrefs(currentBValue);    
        bottomPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            	updateBotPrefs(Integer.parseInt((String) newValue)); 
                return true;
            }       
        }); 
        
		//stat height 
      	statPref = (EditTextPreference) findPreference("statHeight");
      	statPref.setSummary("Change the status bar height used. Might be necessary for some custom launchers: \n"+getPreferenceManager().getSharedPreferences().getInt("stat", 0));
//        updateBotPrefs(currentBValue);    
      	statPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            	updateStatPrefs(Integer.parseInt((String) newValue)); 
                return true;
            }       
        }); 
		//default button
		button3 = (Preference)findPreference("button3");
		button3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				setDefaults();
				return true;
			}
		});
	}
	public int getStatusBarHeight() {
	    int result = 0;
	    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	    if (resourceId > 0) {
	        result = getResources().getDimensionPixelSize(resourceId);
	    }
	    System.out.println(result);
	    return result;
	}
	private String getRealPathFromURI(Uri contentUri) {          
		String [] proj={MediaColumns.DATA};  
		Cursor cursor = managedQuery( contentUri,  
		        proj, // Which columns to return  
		        null,       // WHERE clause; which rows to return (all rows)  
		        null,       // WHERE clause selection arguments (none)  
		        null); // Order-by clause (ascending by name)  
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);  
		cursor.moveToFirst();  
		return cursor.getString(column_index);
	}
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) { 
	super.onActivityResult(requestCode, resultCode, data); 
	if (requestCode == 1) {
	if (resultCode == Activity.RESULT_OK) { 
	  Uri selectedImage = data.getData();   
	  String RealPath;
	  SharedPreferences customSharedPreference = getSharedPreferences(LiveWallpaper.SHARED_PREFS_NAME, Context.MODE_PRIVATE); 
	  SharedPreferences.Editor editor = customSharedPreference.edit ();
	  RealPath = getRealPathFromURI (selectedImage);
	  editor.putString("backLoc", RealPath);
	  button2.setSummary("Select an image for the background: \n"+RealPath);
	  editor.commit(); 
	}}}
	
	@Override
	protected void onDestroy()
	{
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
	}
	//folder choosing popup
	private void folderChoose(){
	       	File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
	        FileDialog fileDialog = new FileDialog(this, mPath);
	        fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
	          public void directorySelected(File directory) {
	              Log.d(getClass().getName(), "selected dir " + directory.toString());
	          	  button.setSummary("Labeled 'shime1' to 'shime46': \n"+directory.toString());
	          	  editor.putString("imgLoc", directory.toString());
	          	  editor.commit();
	          }
	        });
	        fileDialog.setSelectDirectoryOption(true);
	        fileDialog.showDialog();
	}
	private void setDefaults(){
		Display display = getWindowManager().getDefaultDisplay();
		button2.setSummary("Select an image for the background");
		button.setSummary("Labeled 'shime1' to 'shime46'");
		
		bottomPref.setSummary("Change the floor height. Might be necessary for some custom launchers: \n"+(display.getHeight()-(display.getHeight()/4)));
		editor.putString("imgLoc", "Oliver");
		editor.putString("backLoc", "black");
		editor.putString("pos", "Center");
		editor.putInt("bot", (display.getHeight()-(display.getHeight()/4)));
		editor.putInt("stat", getStatusBarHeight());
		editor.commit();
		statPref.setSummary("Change the status bar height used. Might be necessary for some custom launchers: \n"+getStatusBarHeight());
		listPreference.setSummary("Choose the position of the background relative to the screen: \n"+getPreferenceManager().getSharedPreferences().getString("pos","black"));
//		button.setSummary("Select an image for the background");
//		button2.setSummary("Choose the position of the background relative to the screen");
	}
	private void updatePosPrefs(String newValue){
		int index = listPreference.findIndexOfValue(newValue);
		if(index == -1){
			
		}else{
			CharSequence entry = listPreference.getEntries()[index];
			if(entry != null){
				listPreference.setSummary("Choose the position of the background relative to the screen: \n"+entry);
				
				editor.putString("pos", entry.toString());
		      	editor.commit();
			}
		}
      	
	}
	private void updateBotPrefs(int currentBValue){
		bottomPref.setSummary("Change the floor height. Might be necessary for some custom launchers: \n"+currentBValue);
		
		editor.putInt("bot", currentBValue);
	    editor.commit();
      	
	}
	private void updateStatPrefs(int currentSValue){
		statPref.setSummary("Change the status bar height used. Might be necessary for some custom launchers: \n"+currentSValue);
		
		editor.putInt("stat", currentSValue);
	    editor.commit();
	    System.out.println(getPreferenceManager().getSharedPreferences().getInt("stat", 5));
	}
}
