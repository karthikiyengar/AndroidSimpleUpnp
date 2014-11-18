package com.android.AndroidSimpleUpnp;

import android.util.Log;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by karthik on 11/17/14.
 */
public class BrowseRegistryListener extends DefaultRegistryListener {
    public JSONArray jsonArray = new JSONArray();
    public JSONObject jsonObject;

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        deviceRemoved(device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        deviceAdded(device);
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        deviceRemoved(device);
    }

    public void deviceAdded(Device device) {
        try {
            Log.d("App", device.getDisplayString());
            jsonObject = new JSONObject();
            String deviceName = device.getDisplayString();
            String deviceUUID = device.getIdentity().getUdn().toString().substring(5);
            jsonObject.put("deviceName", deviceName);
            jsonObject.put("deviceUUID", deviceUUID);
            jsonArray.put(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deviceRemoved(Device device) {

        String removedDevice = device.getIdentity().getUdn().toString().substring(5);
        Log.d("App", "Rem: " + removedDevice);


        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String currentDevice = jsonObject.getString("deviceUUID");
                if (currentDevice.equals(removedDevice)) {
                    jsonArray.remove(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JSONArray returnDevices() {
        return jsonArray;
    }
}
