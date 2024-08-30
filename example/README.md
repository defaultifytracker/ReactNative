# react-native-defaultify

React Native bridge module for interacting with defaultify

## Installation

```sh
npm install react-native-defaultify
```

## Usage

```js
import * as React from 'react';
import { launchDefaultify, crashInitialise, NetworkLog } from 'react-native-defaultify';

// ...
const navigationRef = React.useRef(); // create ref for navigation

if (Platform.OS == 'ios') {
  launchDefaultify('<IOS-APP-TOKEN>');
} else if (Platform.OS === 'android') {
  launchDefaultify('<ANDROID-APP-TOKEN>');
}

React.useLayoutEffect(() => {
  NetworkLog();
}, []);

React.useEffect(() => {
  crashInitialise(navigationRef); // Pass reference of navigation
}, []);

return (
  <NavigationContainer ref={navigationRef}>
    <Stack.Navigator initialRouteName="screen1">
      <Stack.Screen name="screen1" component={screen1} />
      <Stack.Screen name="screen2" component={screen2} />
    </Stack.Navigator>
  </NavigationContainer>
);
```

## Requirements

For android minSdkVersion 24 is required.

## Extra Steps for Android

Step 1: Generate Access Token from GitLab

- Go to your GitLab profile.
- Select "Preferences".
- Inside Preferences, navigate to "Access Tokens".
- Create a new token.
- After creating the token replace the ‘YOUR_ACCESS_TOKEN_HERE’ with your token.

Step 2: Add the following to your android/build.gradle:

```android
maven {
    url "https://gitlab.appinvent.in/api/v4/projects/4676/packages/maven"
    credentials(HttpHeaderCredentials) {
        name = 'Private-Token'
        value = 'YOUR_ACCESS_TOKEN_HERE'
    }
    authentication {
        header(HttpHeaderAuthentication)
    }
}

```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
