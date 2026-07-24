import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        IosAppModuleKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}