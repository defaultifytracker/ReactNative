import { NativeModules, Platform } from 'react-native';
import XHRInterceptor from 'react-native/Libraries/Network/XHRInterceptor';
import AsyncStorage from '@react-native-async-storage/async-storage';
import type { logsType } from './types/logs';

const LINKING_ERROR =
  `The package 'react-native-defaultify' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Defaultify = NativeModules.Defaultify
  ? NativeModules.Defaultify
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function launchDefaultify(token: string) {
  return Defaultify.defaultifyLaunch(token);
}

export async function crashInitialise(navigationRef: any) {
  getCrashReport();
  const currentRoute = navigationRef.current?.getCurrentRoute();

  const globalErrorHandler = async (error: any, isFatal: unknown) => {
    const stackTrace = error.stack;
    await saveErrorToStorage(error, isFatal, stackTrace);
  };

  const defaultHandler =
    ErrorUtils.getGlobalHandler && ErrorUtils.getGlobalHandler();

  ErrorUtils.setGlobalHandler((error, isFatal) => {
    globalErrorHandler(error, isFatal);
    if (defaultHandler) {
      defaultHandler(error, isFatal);
    }
  });

  const saveErrorToStorage = async (
    error: any,
    _isFatal: unknown,
    stackTrace: unknown
  ) => {
    const errorInfo = {
      errorMessage: error.toString(),
      stackTrace: stackTrace,
      location: currentRoute?.name,
    };

    try {
      await AsyncStorage.setItem('crashReport', JSON.stringify(errorInfo));
    } catch (storageError) {
      console.error('Failed to save error to storage', storageError);
    }
  };
}

async function getCrashReport() {
  try {
    const error = await AsyncStorage.getItem('crashReport');
    if (error) {
      const errorInfo = JSON.parse(error);
      if (errorInfo) {
        const reportSent = await sendCrashReport(JSON.stringify(errorInfo));
        if (reportSent) {
          await AsyncStorage.removeItem('crashReport');
        }
      }
    }
  } catch (error) {
    console.error('Failed to retrieve or send crash report', error);
  }
}

export async function sendCrashReport(crashData: string): Promise<boolean> {
  try {
    await Defaultify.sendCrashReport(crashData);
    return true;
  } catch (error) {
    console.error('Failed to send crash report', error);
    return false;
  }
}

export async function NetworkLog() {
  let logs: logsType = {
    requestHeaders: {},
    requestMethod: {},
    requestTime: '',
    requestURL: '',
    responseHeaders: {},
    responseStatus: '',
    response: '',
    requestPayload: '',
  };
  XHRInterceptor.enableInterception();

  XHRInterceptor.setSendCallback((...obj: any) => {
    logs.requestPayload = obj[0];
  });

  XHRInterceptor.setResponseCallback(
    async (
      _status: string,
      _timeout: string,
      response: string,
      _responseURL: string,
      _responseType: string,
      obj: any
    ) => {
      const blob = new Blob([response], {
        type: 'application/json',
        lastModified: Date.now(),
      });

      const responseData = new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = function (event: any) {
          try {
            const json = JSON.parse(event.target.result);
            resolve(json);
          } catch (e) {
            reject(e);
          }
        };
        reader.onerror = function (error) {
          reject(error);
        };
        reader.readAsText(blob);
      });

      logs.requestHeaders = obj?._headers;
      logs.requestHeaders = {
        contentType: obj?._headers['content-type'],
        ...obj?._headers,
      };
      logs.requestMethod = obj?._method;
      logs.requestTime = obj?._lowerCaseResponseHeaders?.date;
      logs.requestURL = obj?.responseURL;
      logs.responseHeaders = obj?.responseHeaders;
      logs.responseStatus = obj?.status;
      logs.response = JSON.stringify(await responseData.then((data) => data));

      await Defaultify.sendNetworkLog(JSON.stringify(logs));
    }
  );
}
