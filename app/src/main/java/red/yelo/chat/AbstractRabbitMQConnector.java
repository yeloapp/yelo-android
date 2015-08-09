
/*
 *
 *  * Copyright (C) 2015 yelo.red
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */package red.yelo.chat;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;

/**
 * Base class for objects that connect to a RabbitMQ Broker
 */
public abstract class AbstractRabbitMQConnector {

    private static final String TAG = "AbstractRabbitMQConnector";

    protected final String mServer;
    protected final String mExchange;
    protected final String mVirtualHost;
    protected final int mPort;
    protected final ExchangeType mExchangeType;

    private OnDisconnectCallback mOnDisconnectCallback;

    public enum ExchangeType {

        DIRECT("direct"),
        TOPIC("topic"),
        FANOUT("fanout");

        public final String key;

        private ExchangeType(final String key) {
            this.key = key;
        }
    }

    protected Channel mChannel;
    protected Connection mConnection;

    private boolean mRunning;

    public AbstractRabbitMQConnector(final String server, final int port, final String virtualHost, final String exchange, final ExchangeType exchangeType) {
        mServer = server;
        mPort = port;
        mVirtualHost = virtualHost;
        mExchange = exchange;
        mExchangeType = exchangeType;

    }

    /**
     * Disconnect from the broker
     *
     * @param manual <code>true</code> if the disconnection is manual(logout),
     *               <code>false</code> if it happened through an error/loss of
     *               network etc
     */
    public void dispose(final boolean manual) {
        mRunning = false;

        try {
            if (mConnection != null) {
                mConnection.close();
                Logger.d(TAG, "connection is closed");
            }
            if (mChannel != null) {

                Logger.d(TAG, "channel is aborted");
                mChannel.abort();
            }
        } catch (final IOException e) {
            Logger.d(TAG, "connection is closed");
            e.printStackTrace();
        } catch (final AlreadyClosedException e) {
            e.printStackTrace();
        }
        catch (final ShutdownSignalException e)
        {
            Logger.d(TAG,"exception");
        }
        finally {

            if (mOnDisconnectCallback != null) {
                mOnDisconnectCallback.onDisconnect(manual);
            }
        }

    }

    public void setOnDisconnectCallback(final OnDisconnectCallback callback) {
        mOnDisconnectCallback = callback;
    }

    public OnDisconnectCallback getOnDisconnectCallback() {
        return mOnDisconnectCallback;
    }

    public boolean isRunning() {
        return mRunning;
    }

    protected void setIsRunning(final boolean running) {
        mRunning = running;
    }

    /**
     * Connect to the broker and create the exchange
     *
     * @return success
     */
    protected boolean connectToRabbitMQ(final String userName,
                                        final String password) {
        if ((mChannel != null) && mChannel.isOpen()) {
            return true;
        }
        try {
            final ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mServer);
            connectionFactory.setUsername(userName);
            connectionFactory.setPassword(password);
            connectionFactory.setVirtualHost(mVirtualHost);
            connectionFactory.setPort(mPort);
            // if (AbstractYeloActivity.mainActivityIsOpen()) {
            connectionFactory.setRequestedHeartbeat(AppConstants.HEART_BEAT);
            Logger.d(TAG, AppConstants.HEART_BEAT + "");
//            }
//            else{
//                connectionFactory.setRequestedHeartbeat(AppConstants.HEART_BEAT_BACKGROUND);
//                Logger.d(TAG,AppConstants.HEART_BEAT_BACKGROUND+"");
//
//            }
            mConnection = connectionFactory.newConnection();
            mChannel = mConnection.createChannel();
            mChannel.exchangeDeclare(mExchange, mExchangeType.key);

            return true;
        } catch (final Exception e) {
            e.printStackTrace();

            return false;
        }
    }


    /**
     * Publish a message to a queue
     *
     * @param queueName  The Queue to publish to
     * @param routingKey The routing key
     * @param message    The message to publish
     */
    public void publish(final String queueName, final String routingKey,
                        final String message) {
        if ((mChannel != null) && mChannel.isOpen()) {
            try {
                mChannel.basicPublish(queueName, routingKey, null, message
                        .getBytes(HTTP.UTF_8));
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Callback interface for when the Chat consumer gets disconnected
     */
    public static interface OnDisconnectCallback {

        /**
         * Callback method to be triggered when the connector disconnects
         *
         * @param manual <code>true</code> if the chat was manually
         *               disconnected(user logout), <code>false</code> if it
         *               happened due to an error/loss of network
         */
        public void onDisconnect(boolean manual);
    }
}
