package org.apache.cordova.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;


public class NetworkServiceDiscovery extends CordovaPlugin {

	NsdManager mNsdManager;
  NsdManager.ResolveListener mResolveListener;
  NsdManager.DiscoveryListener mDiscoveryListener;
  NsdManager.RegistrationListener mRegistrationListener;
	public String mServiceName = "";
	public String SERVICE_TYPE = "";
	public String mService = "";

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if(action.equals("registerService")) {
			int port = args.getInt(0);

			this.registerService(SERVICE_TYPE, mServiceName, port);
			callbackContext.success();

			return true;
		} else if (action.equals("startDiscovery")) {
			this.discoverServices();
			callbackContext.success();
			return true;
		} else if (action.equals("stopDiscovery")) {

			this.stopDiscovery();
			callbackContext.success();

			return true;
		} else if (action.equals("getChosenServiceInfo")) {

			this.getChosenServiceInfo();
			callbackContext.success(mService.getHost(), mService.getPort());
			return true;
		} else if (action.equals("tearDown")) {

			this.tearDown();
			callbackContext.success();
			return true;
		} else if (action.equals("Initalize")) {

			String type = args.getString(0);
			String name = args.getString(1);

			this.initializeNsd(type, name);
			callbackContext.success();
			return true;
		}

		return false;
	}

	private void initializeNsd(String type, String name) {

		Context mContext = this.cordova.getActivity().getApplicationContext();
		mNsdManager = mContext.getSystemService(Context.NSD_SERVICE);

	  initializeResolveListener();
	  initializeDiscoveryListener();
	  initializeRegistrationListener();

  }

	private void registerService(String type, String name, int port) {
		NsdServiceInfo serviceInfo  = new NsdServiceInfo();
    serviceInfo.setPort(port);
    serviceInfo.setServiceName(mServiceName);
    serviceInfo.setServiceType(type);

    mNsdManager.registerService(
    	serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

	}

	private void discoverServices() {
    mNsdManager.discoverServices(
      SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
  }

	private void stopDiscovery() {
    mNsdManager.stopServiceDiscovery(mDiscoveryListener);
  }

	private NsdServiceInfo getChosenServiceInfo() {
    return mService;
  }

	private void tearDown() {
    mNsdManager.unregisterService(mRegistrationListener);
  }

	public void initializeRegistrationListener() {
	  mRegistrationListener = new NsdManager.RegistrationListener() {

      @Override
      public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
        mServiceName = NsdServiceInfo.getServiceName();
      }

      @Override
      public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
      }

      @Override
      public void onServiceUnregistered(NsdServiceInfo arg0) {
      }

      @Override
      public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
      }
	  };
	}

	public void initializeDiscoveryListener() {
    mDiscoveryListener = new NsdManager.DiscoveryListener() {

      @Override
      public void onDiscoveryStarted(String regType) {
        Log.d(TAG, "Service discovery started");
      }

      @Override
      public void onServiceFound(NsdServiceInfo service) {
        Log.d(TAG, "Service discovery success" + service);
        if (!service.getServiceType().equals(SERVICE_TYPE)) {
          Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
        } else if (service.getServiceName().equals(mServiceName)) {
          Log.d(TAG, "Same machine: " + mServiceName);
        } else if (service.getServiceName().contains(mServiceName)){
          mNsdManager.resolveService(service, mResolveListener);
        }
      }

      @Override
      public void onServiceLost(NsdServiceInfo service) {
        Log.e(TAG, "service lost" + service);
        if (mService == service) {
          mService = null;
        }
      }

      @Override
      public void onDiscoveryStopped(String serviceType) {
        Log.i(TAG, "Discovery stopped: " + serviceType);
      }

      @Override
      public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Discovery failed: Error code:" + errorCode);
        mNsdManager.stopServiceDiscovery(this);
      }

      @Override
      public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Discovery failed: Error code:" + errorCode);
        mNsdManager.stopServiceDiscovery(this);
      }
    };
  }

	public void initializeResolveListener() {
    mResolveListener = new NsdManager.ResolveListener() {

      @Override
      public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
        Log.e(TAG, "Resolve failed" + errorCode);
      }

      @Override
      public void onServiceResolved(NsdServiceInfo serviceInfo) {
        Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

        if (serviceInfo.getServiceName().equals(mServiceName)) {
          Log.d(TAG, "Same IP.");
          return;
        }

        mService = serviceInfo;
      }
    };
  }

}
