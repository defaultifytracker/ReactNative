import BMRTSDK

@objc(Defaultify)
class Defaultify: NSObject {
  @objc
  func defaultifyLaunch(_ token: String){
      if #available(iOS 13.0, *) {
          print("I'm here");
          BMRT.launch(token: token);
      } else {
          // Fallback on earlier versions
      }
  }
}
