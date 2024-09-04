#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(Defaultify, NSObject)

RCT_EXTERN_METHOD(defaultifyLaunch:(NSString *)token)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
