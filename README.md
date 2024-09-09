# react-native-defaultify

React Native bridge module for interacting with defaultify

## Installation

```sh
npm install react-native-defaultify
```

## Usage

```js
import * as React from 'react';
import {
  launchDefaultify,
  crashInitialise,
  NetworkLog,
} from 'react-native-defaultify';

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

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
