import Defaultify

@objc(Defaultify)
class Defaultify: NSObject {
  @objc
  func defaultifyLaunch(_ token: String){
      if #available(iOS 13.0, *) {
          print("I'm here");
          DFTFY.launch(token: token);
      } else {
          // Fallback on earlier versions
      }
  }
}
