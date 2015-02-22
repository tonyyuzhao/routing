package com.prototype.p2p.routing;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A simple IP mutlicast router using a static IP
 */
public class UDPMulticastRouter implements I_Router {
    public static final String TAG = "UDPMulticastRouter";

    private MulticastSocket mSocket;
    private Handler mUpdateHandler;

    private int QUEUE_CAPACITY = 100;
    private BlockingQueue<String> mMessageQueue = new ArrayBlockingQueue<>( QUEUE_CAPACITY );

    public UDPMulticastRouter( Handler handler) {
        try {
            mSocket = new MulticastSocket( 8000 );
        } catch ( IOException ioe ) {
            Log.e( TAG, "Error creating multicast socket: ", ioe );
            ioe.printStackTrace();
        }
        mUpdateHandler = handler;

        new Thread( new UDPMulticastSender() ).start();
        new Thread( new UDPMulticastReceiver() ).start();
    }

    @Override
    public void close() {
        mSocket.close();
        Log.d( TAG, "socket closed" );
    }

    @Override
    public void sendMessage( String msg ) {
        mMessageQueue.offer( msg );
    }

    private class UDPMulticastSender implements Runnable {

        @Override
        public void run() {
            try {
                while ( true ) {
                    String msg = mMessageQueue.take();
                    byte[] buf = msg.getBytes();

                    try {
                        DatagramPacket packet = new DatagramPacket( buf, buf.length,
                                InetAddress.getByName( "224.3.1.1" ), 8000 );
                        mSocket.send( packet );

                        Log.d( TAG, "Message sent to group" );
                        updateMessages( "Send", msg, packet.getSocketAddress().toString() );
                    } catch ( UnknownHostException uhe ) {
                        Log.e( TAG, "Error unknown host: ", uhe );
                        uhe.printStackTrace();
                    } catch ( IOException ioe ) {
                        Log.e( TAG, "Error sending message: ", ioe );
                        ioe.printStackTrace();
                    }
                }
            } catch ( InterruptedException ie ) {
                Log.e( TAG, "Error reading message queue: ", ie );
                ie.printStackTrace();
            }
        }
    }

    private class UDPMulticastReceiver implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress group = InetAddress.getByName( "224.3.1.1" );
                mSocket.joinGroup( group );

                byte[] buf = new byte[256];
                while ( true ) {
                    DatagramPacket packet = new DatagramPacket( buf, buf.length );
                    mSocket.receive( packet );

                    updateMessages( "Recv", new String( packet.getData(), 0, packet.getLength() ),
                            packet.getSocketAddress().toString() );
                }
            } catch ( SocketException se ) {
                Log.e( TAG, "Error creating socket: ", se );
                se.printStackTrace();
            } catch ( IOException ioe ) {
                Log.e( TAG, "Error creating multicast socket: ", ioe );
                ioe.printStackTrace();
            }
        }
    }

    public synchronized void updateMessages( String key, String msg, String source ) {
        Log.e( TAG, "Updating message: " + msg );

        Bundle messageBundle = new Bundle();
        messageBundle.putString( key, "<" + source + ">: " + msg  );

        Message message = new Message();
        message.setData( messageBundle );
        mUpdateHandler.sendMessage( message );
    }
}
