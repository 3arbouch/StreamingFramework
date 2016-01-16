package epfl.lacav.fragments;

import epfl.lcav.clientaudiostreaming.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * This class is describes a setting fragment whose elements are declared 
 * in preferences.xml files
 * The preference is used  to principally put  the IP address of the server 
 * @author MohamedBenArbia
 *
 */
public class SettingsFragment extends PreferenceFragment {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefrences);
    }

}
