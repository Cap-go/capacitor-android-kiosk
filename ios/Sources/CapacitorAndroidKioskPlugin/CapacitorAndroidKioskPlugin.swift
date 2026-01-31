import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapacitorAndroidKioskPlugin)
public class CapacitorAndroidKioskPlugin: CAPPlugin, CAPBridgedPlugin {
    private let pluginVersion: String = "8.1.6"
    public let identifier = "CapacitorAndroidKioskPlugin"
    public let jsName = "CapacitorAndroidKiosk"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "isInKioskMode", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isSetAsLauncher", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "enterKioskMode", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "exitKioskMode", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setAsLauncher", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setAllowedKeys", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getPluginVersion", returnType: CAPPluginReturnPromise)
    ]

    @objc func isInKioskMode(_ call: CAPPluginCall) {
        call.reject("Kiosk mode is not available on iOS. This functionality is only supported on Android. For iOS, please use the device's Guided Access feature.")
    }

    @objc func isSetAsLauncher(_ call: CAPPluginCall) {
        call.reject("Launcher functionality is not available on iOS. This functionality is only supported on Android. For iOS, please use the device's Guided Access feature.")
    }

    @objc func enterKioskMode(_ call: CAPPluginCall) {
        call.reject("Kiosk mode is not available on iOS. This functionality is only supported on Android. For iOS, please use the device's Guided Access feature.")
    }

    @objc func exitKioskMode(_ call: CAPPluginCall) {
        call.reject("Kiosk mode is not available on iOS. This functionality is only supported on Android. For iOS, please use the device's Guided Access feature.")
    }

    @objc func setAsLauncher(_ call: CAPPluginCall) {
        call.reject("Launcher functionality is not available on iOS. This functionality is only supported on Android. For iOS, please use the device's Guided Access feature.")
    }

    @objc func setAllowedKeys(_ call: CAPPluginCall) {
        call.reject("Hardware key blocking is not available on iOS. This functionality is only supported on Android. For iOS, please use the device's Guided Access feature.")
    }

    @objc func getPluginVersion(_ call: CAPPluginCall) {
        call.resolve(["version": self.pluginVersion])
    }

}
