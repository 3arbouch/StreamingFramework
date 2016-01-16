package epfl.lcav.activities;

import epfl.lcav.clientaudiostreaming.R;

import epfl.lcav.communication.WaitingForinstructionsAsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.Activity;
import android.content.Context;

public class WaitingForInstructionsActivity extends Activity {

	private static Context context;
	ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waiting_for_instructions);


		
		ListView listView = (ListView) this.findViewById(R.id.list);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		listView.setAdapter(adapter);

		WaitingForInstructionsActivity.context = getApplicationContext();
		new WaitingForinstructionsAsyncTask(this).execute();

	}

	public void showOnList(String message) {
		adapter.add(message);
	}

	public static Context getWaitingForInstructionContext() {
		return WaitingForInstructionsActivity.context;
	}

}
