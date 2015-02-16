package com.prototype.p2p.routing;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class RoutingActivity extends ActionBarActivity {

    private TextView mStatusView;
    private I_Router mRouter;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_routingactivity);

        mStatusView = ( TextView ) findViewById( R.id.status );
        mStatusView.setGravity( Gravity.LEFT );
        mStatusView.setTextColor( Color.BLUE );

        mRouter = new UDPMulticastRouter( new Handler() {

            @Override
            public void handleMessage( Message msg ) {
                String sendMessage = msg.getData().getString( "Send" );
                String recvMessage = msg.getData().getString( "Recv" );

                if ( sendMessage != null )
                    mStatusView.append( "To " + sendMessage + "\n" );

                if ( recvMessage != null )
                    mStatusView.append( "From " + recvMessage  + "\n" );
            }
        } );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_routingactivity, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if ( id == R.id.action_settings ) {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    protected void onDestroy() {
        mRouter.close();
        super.onDestroy();
    }

    public void clickSend( View v ) {
        EditText messageView = ( EditText ) this.findViewById( R.id.inputMessage );
        if ( messageView != null ) {
            String messageString = messageView.getText().toString();
            if ( !messageString.isEmpty() ) {
                mRouter.sendMessage( messageString );
            }
            messageView.setText( "" );
        }
    }
}
