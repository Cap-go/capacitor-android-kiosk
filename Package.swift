// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapgoCapacitorAndroidKiosk",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "CapgoCapacitorAndroidKiosk",
            targets: ["CapacitorAndroidKioskPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "CapacitorAndroidKioskPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/CapacitorAndroidKioskPlugin"),
        .testTarget(
            name: "CapacitorAndroidKioskPluginTests",
            dependencies: ["CapacitorAndroidKioskPlugin"],
            path: "ios/Tests/CapacitorAndroidKioskPluginTests")
    ]
)
