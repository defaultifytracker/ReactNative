#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(Defaultify, NSObject)

RCT_EXTERN_METHOD(launchBmrt:(NSString *)token)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
