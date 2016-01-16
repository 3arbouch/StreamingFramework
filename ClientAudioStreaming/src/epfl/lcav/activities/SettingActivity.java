package epfl.lcav.activities;

import epfl.lacav.fragments.SettingsFragment;
import android.os.Bundle;
import android.app.Activity;

/**
 * This activity describes the setting GUI. The GUI is formed by the fragment 
 * SettingsFragemnt defined in epfl.lcav.fragments package
 * @author MohamedBenArbia
 *
 */
public class SettingActivity extends Activity {
     
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
	}

	

}
