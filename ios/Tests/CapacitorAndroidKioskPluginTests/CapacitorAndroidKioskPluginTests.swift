import XCTest
@testable import CapacitorAndroidKioskPlugin

class CapacitorAndroidKioskPluginTests: XCTestCase {
    func testPluginVersion() {
        // Basic test to ensure the plugin loads
        let plugin = CapacitorAndroidKioskPlugin()
        XCTAssertNotNil(plugin)
    }
}
