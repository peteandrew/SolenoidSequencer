package uk.co.peterandrew.solenoidsequencer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.os.AsyncTask;
import uk.co.peterandrew.solenoidsequencer.SoundGridView.GridClickListener;
import uk.co.peterandrew.solenoidsequencer.TCPClient;

public class SolenoidSequencer extends Activity implements GridClickListener {
	private SoundGridView soundGridView;
	private TCPClient mTcpClient;
	
	// 0 - no command, 1 - SET, 2 - GET
	private int commandSent = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// connect to the server
        new connectTask().execute("");

        soundGridView = new SoundGridView(this);
        soundGridView.setGridClickListener(this);
        setContentView(soundGridView);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_solenoid_sequencer, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String message = "";
	
    	// Handle item selection
    	switch (item.getItemId()) {
        	case R.id.menuitem_start:
        		message = "START";
        		break;
    	    case R.id.menuitem_stop:
        		message = "STOP";
        		break;
        	case R.id.menuitem_refresh:
        		message = "GET";
        		commandSent = 2;
        		break;
        	default:
            	return super.onOptionsItemSelected(item);
    	}

		if (mTcpClient != null) {
        	mTcpClient.sendMessage(message);
		}    	
    	return true;
	}	
	
	
	public void onGridClick(int step, int instrument, boolean state) {
		Log.i("SolenoidServer", "step: " + step + ", instrument: " + instrument);

		String message = "SET " + instrument + " " + step + " ";
		if (state) {
			message += "1";
		} else {
			message += "0";
		}

		//sends the message to the server
		if (mTcpClient != null) {
        	mTcpClient.sendMessage(message);
        	commandSent = 1;
		}
    }
    
    
    public class connectTask extends AsyncTask<String,String,TCPClient> {
 
        @Override
        protected TCPClient doInBackground(String... message) {
            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();
            return null;
        }
 
 
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            switch (commandSent) {
            	case 0:
            	case 1:
            		Log.i("SolenoidServer", values[0]);
            		break;
            	case 2:
	            	String[] states = values[0].split(",");
					boolean state = false;
					int idx = 0;        

	            	for (int instrument = 0; instrument < 4; instrument++) {
    	        		for (int step = 0; step < 16; step++) {
    	        			idx = (instrument * 16) + step;
    	        			if (states[idx].equals("1")) {
    	        				state = true;
    	        			} else {
    	        				state = false;
    	        			}
    	        			soundGridView.setState(instrument, step, state);
						}
					}
            		break;
            }
        }
        
    }    
}