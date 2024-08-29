import * as React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import Config from 'react-native-config';
import { Platform } from 'react-native';
import LoginScreen from './screens/LoginScreens/LoginScreen';
import HomeTabs from './screens/HomeScreens/HomeTabs';

import {
  launchDefaultify,
  crashInitialise,
  NetworkLog,
} from 'react-native-defaultify';

const Stack = createStackNavigator();

export default function App() {
  const navigationRef = React.useRef();

  React.useEffect(() => {
    if (Platform.OS === 'ios' && Config.DEFAULTIFY_KEY_IOS) {
      launchDefaultify('Config.DEFAULTIFY_KEY_IOS');
    } else if (Platform.OS === 'android' && Config.DEFAULTIFY_KEY_ANDROID) {
      launchDefaultify(Config.DEFAULTIFY_KEY_ANDROID);
    }
  }, []);

  React.useLayoutEffect(() => {
    NetworkLog();
  }, []);

  React.useEffect(() => {
    crashInitialise(navigationRef);
  }, []);

  return (
    <NavigationContainer ref={navigationRef}>
      <Stack.Navigator initialRouteName="Login">
        <Stack.Screen name="Login" component={LoginScreen} />
        <Stack.Screen
          name="HomeTab"
          component={HomeTabs}
          options={{ headerShown: false }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
