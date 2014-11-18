package com.android.AndroidSimpleUpnp;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.avtransport.callback.*;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karthik on 11/17/14.
 */

public class AndroidUpnpProvider {

    public BrowseRegistryListener registryListener = new BrowseRegistryListener();
    public AndroidUpnpService upnpService;
    public ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            // Refresh the list with all known devices

            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }

            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Search asynchronously for all devices
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };
    public GetPositionInfo gpi;
    public GetTransportInfo gti;
    public String value;
    public Service service;
    public JSONObject obj = new JSONObject();

    public void selectDevice(String uid) {
        try {
            Device device = upnpService.getControlPoint().getRegistry().getDevice(new UDN(uid), false); //boolean = false if embedded device
            service = device.findService(new UDAServiceId("AVTransport"));

            gti = new GetTransportInfo(service) {
                @Override
                public void received(ActionInvocation actionInvocation, TransportInfo transportInfo) {
                    try {
                        obj.put("currentState", transportInfo.getCurrentTransportState());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                }
            };
            upnpService.getControlPoint().execute(gti);

            gpi = new GetPositionInfo(service) {
                @Override
                public void received(ActionInvocation actionInvocation, PositionInfo positionInfo) {

                    try {
                        obj.put("elapsedSeconds", positionInfo.getTrackElapsedSeconds());
                        obj.put("absoluteTime", positionInfo.getAbsTime());
                        obj.put("elapsedPercent", positionInfo.getElapsedPercent());
                        obj.put("currentTrackURI", positionInfo.getTrackURI());
                        obj.put("duration", positionInfo.getTrackDuration());
                        obj.put("metadata", positionInfo.getTrackMetaData());
                        obj.put("remainingSeconds", positionInfo.getTrackRemainingSeconds());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                    Log.d("App", "Failed to get status: " + s);
                }
            };

            upnpService.getControlPoint().execute(gpi);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendStream(String url) {
        try {
            ActionCallback setAVTransportURIAction =
                    new SetAVTransportURI(service, url, "NO METADATA") {
                        @Override
                        public void failure(ActionInvocation arg0,
                                            UpnpResponse arg1, String arg2) {
                            Log.d("App", "Sending media failed");
                        }
                    };
            upnpService.getControlPoint().execute(setAVTransportURIAction);
        } catch (NullPointerException e) {
            Log.d("App", "Error: Have you selected the device yet?");
        }
    }

    public void play() {
        try {
            ActionCallback playAction =
                    new Play(service) {
                        @Override
                        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                            Log.d("App", "Play Failed! " + defaultMsg);
                        }
                    };
            upnpService.getControlPoint().execute(playAction);
        } catch (Exception e) {
            Log.d("App", "Error: Have you selected the device yet?");
        }
    }

    public void stop() {
        try {
            ActionCallback stopAction = new Stop(service) {
                @Override
                public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                    Log.d("App", "Stop Failed! " + arg2);
                }
            };
            upnpService.getControlPoint().execute(stopAction);
        } catch (Exception e) {
            Log.d("App", "Error: Have you selected the device yet?");
        }
    }

    public void pause() {
        try {
            ActionCallback pauseAction = new Pause(service) {
                @Override
                public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                    Log.d("App", "Pause Failed! " + arg2);
                }
            };
            upnpService.getControlPoint().execute(pauseAction);
        } catch (Exception e) {
            Log.d("App", "Error: Have you selected the device yet?");
        }
    }

    /* Seek using absolute time */
    public void seek(String time) {
        try {
            ActionCallback seekAction = new Seek(service, time) {
                @Override
                public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                    Log.d("App", "Seek Failed! " + arg2);
                }
            };
            upnpService.getControlPoint().execute(seekAction);
        } catch (Exception e) {
            Log.d("App", "Error: Have you selected the device yet?");
        }
    }

    public String getDetails() {
        upnpService.getControlPoint().execute(gpi);
        upnpService.getControlPoint().execute(gti);
        return obj.toString();
    }

    public String getDevices() {
        return registryListener.returnDevices().toString();
    }

    public void cleanup() {
        upnpService.getRegistry().removeListener(registryListener);
    }
}
